package com.glowup.app.ui.screens.fitness

import android.speech.tts.TextToSpeech
import androidx.activity.compose.BackHandler
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.TrendingDown
import androidx.compose.material.icons.automirrored.filled.TrendingUp
import androidx.compose.material.icons.automirrored.filled.VolumeOff
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import com.glowup.app.ui.components.GlowUpDialog
import com.glowup.app.ui.components.goBack
import com.glowup.app.ui.data.ProGate
import com.glowup.app.ui.data.ProStore
import com.glowup.app.ui.data.fitness.Direction
import com.glowup.app.ui.data.fitness.Exercise
import com.glowup.app.ui.data.fitness.ExerciseLibrary
import com.glowup.app.ui.data.fitness.FitnessStore
import com.glowup.app.ui.data.fitness.Measure
import com.glowup.app.ui.data.fitness.SetLog
import com.glowup.app.ui.data.fitness.WorkoutResult
import com.glowup.app.ui.theme.*
import kotlinx.coroutines.delay
import java.util.Locale
import kotlin.math.roundToInt

private enum class Phase { READY, WORK, LOG, REST, SUMMARY, SAVED }

private data class SetStep(val blockIndex: Int, val set: Int)

private val effortLabels = listOf("Easy", "Steady", "Hard", "Very hard", "All-out")

@Composable
fun WorkoutPlayerScreen(navController: NavHostController, key: String) {
    val plan = remember(key) { FitnessStore.planFor(key) } ?: return
    if (workoutLocked(key) || plan.blocks.isEmpty()) {
        LaunchedEffect(Unit) { navController.goBack() }
        return
    }
    val context = LocalContext.current
    val steps = remember(plan) { plan.blocks.flatMapIndexed { bi, b -> (1..b.sets).map { SetStep(bi, it) } } }
    var stepIndex by remember { mutableIntStateOf(0) }
    var phase by remember { mutableStateOf(Phase.READY) }
    var secondsLeft by remember { mutableIntStateOf(5) }
    var paused by remember { mutableStateOf(false) }
    var elapsed by remember { mutableIntStateOf(0) }
    var logValue by remember { mutableIntStateOf(0) }
    var effort by remember { mutableIntStateOf(3) }
    var showExit by remember { mutableStateOf(false) }
    var showHow by remember { mutableStateOf(false) }
    var result by remember { mutableStateOf<WorkoutResult?>(null) }
    val logged = remember { mutableStateListOf<SetLog>() }
    val swaps = remember { mutableStateMapOf<Int, String>() }

    val step = steps[stepIndex.coerceAtMost(steps.lastIndex)]
    val block = plan.blocks[step.blockIndex]
    val exercise: Exercise = swaps[step.blockIndex]?.let(ExerciseLibrary::get) ?: block.exercise
    val workSeconds = if (exercise.measure == Measure.SECONDS) block.target * (if (exercise.perSide) 2 else 1) else 0

    // ---- Voice cues ----
    var ttsReady by remember { mutableStateOf(false) }
    val tts = remember {
        var engine: TextToSpeech? = null
        engine = TextToSpeech(context) { status ->
            if (status == TextToSpeech.SUCCESS) {
                engine?.language = Locale.getDefault()
                ttsReady = true
            }
        }
        engine
    }
    fun say(text: String) {
        if (FitnessStore.voiceOn && ttsReady) tts.speak(text, TextToSpeech.QUEUE_FLUSH, null, text)
    }

    val view = LocalView.current
    DisposableEffect(Unit) {
        view.keepScreenOn = true
        val window = (view.context as? android.app.Activity)?.window
        val bars = window?.let { androidx.core.view.WindowCompat.getInsetsController(it, view) }
        bars?.isAppearanceLightStatusBars = false
        bars?.isAppearanceLightNavigationBars = false
        onDispose {
            view.keepScreenOn = false
            bars?.isAppearanceLightStatusBars = true
            bars?.isAppearanceLightNavigationBars = true
            tts.stop()
            tts.shutdown()
        }
    }

    fun startWork() {
        phase = Phase.WORK
        secondsLeft = workSecondsFor(plan.blocks[steps[stepIndex].blockIndex].let { b -> swaps[steps[stepIndex].blockIndex]?.let(ExerciseLibrary::get) ?: b.exercise }, plan.blocks[steps[stepIndex].blockIndex].target)
        val b = plan.blocks[steps[stepIndex].blockIndex]
        val e = swaps[steps[stepIndex].blockIndex]?.let(ExerciseLibrary::get) ?: b.exercise
        say(if (b.max) "${e.name}. As many as you can." else "${e.name}. ${e.doseLabel(b.target).replace("sec", "seconds")}")
    }

    fun advance() {
        if (stepIndex >= steps.lastIndex) {
            phase = Phase.SUMMARY
            say("Workout complete. Great job.")
            return
        }
        val rest = block.restSeconds
        stepIndex++
        if (rest > 0) {
            phase = Phase.REST
            secondsLeft = rest
            val next = plan.blocks[steps[stepIndex].blockIndex]
            val nextEx = swaps[steps[stepIndex].blockIndex]?.let(ExerciseLibrary::get) ?: next.exercise
            say("Rest. Next, ${nextEx.name}.")
        } else startWork()
    }

    fun record(done: Int) {
        logged.add(SetLog(exercise.id, block.target, done.coerceAtLeast(0), block.max))
        advance()
    }

    // ---- Timers ----
    LaunchedEffect(phase, stepIndex, paused) {
        if (paused) return@LaunchedEffect
        val counting = phase == Phase.READY || phase == Phase.REST || (phase == Phase.WORK && exercise.measure == Measure.SECONDS)
        if (!counting) return@LaunchedEffect
        if (phase == Phase.READY && secondsLeft == 5) say("Get ready. ${exercise.name}.")
        while (secondsLeft > 0) {
            delay(1000)
            secondsLeft--
            if (secondsLeft in 1..3) say("$secondsLeft")
            if (phase == Phase.WORK && exercise.perSide && secondsLeft == workSeconds / 2) say("Switch sides")
        }
        when (phase) {
            Phase.READY, Phase.REST -> startWork()
            Phase.WORK -> record(block.target)
            else -> Unit
        }
    }
    LaunchedEffect(paused, phase) {
        while (!paused && phase != Phase.SUMMARY && phase != Phase.SAVED) {
            delay(1000)
            elapsed++
        }
    }

    BackHandler(enabled = phase != Phase.SAVED) { showExit = true }

    fun finish() {
        if (navController.popBackStack("fitness", inclusive = false)) return
        navController.goBack()
    }

    Box(Modifier.fillMaxSize().background(Ink).systemBarsPadding()) {
        when (phase) {
            Phase.SUMMARY -> Summary(
                plan.title, logged, elapsed, effort, onEffort = { effort = it },
                onSave = {
                    result = FitnessStore.saveWorkout(plan, logged.toList(), elapsed, effort)
                    phase = Phase.SAVED
                },
                onDiscard = { finish() },
            )
            Phase.SAVED -> Saved(result!!, logged, elapsed) { finish() }
            else -> Column(Modifier.fillMaxSize()) {
                // Top bar
                Row(Modifier.fillMaxWidth().padding(16.dp, 12.dp), verticalAlignment = Alignment.CenterVertically) {
                    RoundIcon(Icons.Filled.Close, "End workout") { showExit = true }
                    Spacer(Modifier.width(12.dp))
                    Box(Modifier.weight(1f).height(6.dp).clip(RoundedCornerShape(999.dp)).background(Color.White.copy(alpha = 0.15f))) {
                        val progress by animateFloatAsState(logged.size / steps.size.toFloat(), tween(400), label = "progress")
                        Box(Modifier.fillMaxWidth(progress).fillMaxHeight().clip(RoundedCornerShape(999.dp)).background(Color(0xFF7FD9A8)))
                    }
                    Spacer(Modifier.width(12.dp))
                    RoundIcon(if (FitnessStore.voiceOn) Icons.AutoMirrored.Filled.VolumeUp else Icons.AutoMirrored.Filled.VolumeOff, "Voice cues") {
                        FitnessStore.updateVoice(!FitnessStore.voiceOn)
                    }
                }
                Text(
                    "Exercise ${step.blockIndex + 1} of ${plan.blocks.size} · Set ${step.set} of ${block.sets}" + " · " + formatElapsed(elapsed),
                    color = Color.White.copy(alpha = 0.55f), fontSize = 12.5.sp, fontWeight = FontWeight.Medium,
                    modifier = Modifier.align(Alignment.CenterHorizontally),
                )

                Column(
                    Modifier.weight(1f).fillMaxWidth().verticalScroll(rememberScrollState()).padding(24.dp, 16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    if (phase == Phase.REST) {
                        Text("REST", color = Color.White.copy(alpha = 0.55f), fontSize = 13.sp, fontWeight = FontWeight.Bold, letterSpacing = 1.sp)
                        Spacer(Modifier.height(10.dp))
                        TimerRing(secondsLeft, block.restSeconds.coerceAtLeast(1), 200.dp, Color(0xFF8DB8F5))
                        Spacer(Modifier.height(20.dp))
                        Text("Next up", color = Color.White.copy(alpha = 0.55f), fontSize = 12.sp)
                        Spacer(Modifier.height(8.dp))
                        Row(
                            Modifier.fillMaxWidth().clip(RoundedCornerShape(22.dp)).background(Color.White.copy(alpha = 0.08f)).padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            ExerciseArt(exercise, 64.dp, 16.dp)
                            Spacer(Modifier.width(14.dp))
                            Column {
                                Text(exercise.name, color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                                Text(
                                    (if (block.max) "Max effort" else exercise.doseLabel(block.target)) + " · set ${step.set} of ${block.sets}",
                                    color = Color.White.copy(alpha = 0.6f), fontSize = 12.5.sp,
                                )
                            }
                        }
                    } else {
                        ExerciseArt(exercise, 190.dp, 32.dp)
                        Spacer(Modifier.height(16.dp))
                        Text(exercise.name, color = Color.White, fontSize = 24.sp, fontWeight = FontWeight.Bold, textAlign = TextAlign.Center)
                        Spacer(Modifier.height(8.dp))
                        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) { exercise.muscles.take(3).forEach { MuscleChip(it, onDark = true) } }
                        Spacer(Modifier.height(20.dp))
                        when (phase) {
                            Phase.READY -> {
                                Text("Get ready", color = Color.White.copy(alpha = 0.6f), fontSize = 14.sp)
                                Text("$secondsLeft", color = Color.White, fontSize = 72.sp, fontWeight = FontWeight.Bold)
                            }
                            Phase.WORK -> if (exercise.measure == Measure.SECONDS) {
                                TimerRing(secondsLeft, workSeconds.coerceAtLeast(1), 170.dp, Color(0xFF7FD9A8))
                                if (exercise.perSide) {
                                    Text(if (secondsLeft > workSeconds / 2) "First side" else "Second side", color = Color.White.copy(alpha = 0.6f), fontSize = 13.sp, modifier = Modifier.padding(top = 8.dp))
                                }
                            } else {
                                Text(if (block.max) "MAX" else "${block.target}", color = Color.White, fontSize = 76.sp, fontWeight = FontWeight.Bold, lineHeight = 80.sp)
                                Text(
                                    if (block.max) "as many good reps as you can · goal ${block.target}" else if (exercise.perSide) "reps each side" else "reps",
                                    color = Color.White.copy(alpha = 0.6f), fontSize = 14.sp,
                                )
                                FitnessStore.bestFor(exercise.id)?.let {
                                    Text("Your best: $it", color = Color(0xFF7FD9A8), fontSize = 12.5.sp, fontWeight = FontWeight.SemiBold, modifier = Modifier.padding(top = 4.dp))
                                }
                            }
                            Phase.LOG -> {
                                Text("How many did you get?", color = Color.White, fontSize = 17.sp, fontWeight = FontWeight.SemiBold)
                                Text(if (block.max) "Count only clean reps" else "Target was ${block.target}", color = Color.White.copy(alpha = 0.55f), fontSize = 13.sp)
                                Spacer(Modifier.height(12.dp))
                                NumberStepper("$logValue", { logValue = (logValue - 1).coerceAtLeast(0) }, { logValue++ }, onDark = true, big = true, unit = if (exercise.perSide) "each side" else "reps")
                                Spacer(Modifier.height(12.dp))
                                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                    listOf(block.target / 2, block.target - 2, block.target, block.target + 2).filter { it > 0 }.distinct().forEach { v ->
                                        Text(
                                            "$v", color = if (v == logValue) Ink else Color.White, fontSize = 14.sp, fontWeight = FontWeight.Bold,
                                            modifier = Modifier.clip(RoundedCornerShape(999.dp)).background(if (v == logValue) Color.White else Color.White.copy(alpha = 0.12f)).clickable { logValue = v }.padding(16.dp, 8.dp),
                                        )
                                    }
                                }
                            }
                            else -> Unit
                        }
                        if (phase == Phase.WORK) {
                            Spacer(Modifier.height(14.dp))
                            Text(exercise.cue, color = Color.White.copy(alpha = 0.6f), fontSize = 13.sp, textAlign = TextAlign.Center, lineHeight = 18.sp)
                            if (showHow) {
                                Spacer(Modifier.height(12.dp))
                                Column(Modifier.fillMaxWidth().clip(RoundedCornerShape(18.dp)).background(Color.White.copy(alpha = 0.08f)).padding(16.dp)) {
                                    exercise.steps.forEachIndexed { i, s ->
                                        Text("${i + 1}. $s", color = Color.White.copy(alpha = 0.85f), fontSize = 13.sp, lineHeight = 18.sp, modifier = Modifier.padding(vertical = 3.dp))
                                    }
                                }
                            }
                        }
                    }
                }

                // Controls
                Column(Modifier.padding(24.dp, 4.dp, 24.dp, 20.dp)) {
                    when (phase) {
                        Phase.READY -> WhitePill("Start now") { startWork() }
                        Phase.REST -> Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                            DarkPill("+15 s", Modifier.weight(1f)) { secondsLeft += 15 }
                            WhitePill("Skip rest", Modifier.weight(1.4f)) { startWork() }
                        }
                        Phase.WORK -> {
                            if (exercise.measure == Measure.SECONDS) {
                                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                                    DarkPill(if (paused) "Resume" else "Pause", Modifier.weight(1f), if (paused) Icons.Filled.PlayArrow else Icons.Filled.Pause) { paused = !paused }
                                    WhitePill("Finish set", Modifier.weight(1.4f)) {
                                        val done = (workSeconds - secondsLeft) / (if (exercise.perSide) 2 else 1)
                                        paused = false
                                        record(done)
                                    }
                                }
                            } else {
                                WhitePill("Done") {
                                    logValue = if (block.max) (FitnessStore.bestFor(exercise.id) ?: block.target / 2) else block.target
                                    phase = Phase.LOG
                                }
                            }
                            Spacer(Modifier.height(12.dp))
                            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
                                SmallLink(if (showHow) "Hide how-to" else "How to") { showHow = !showHow }
                                exercise.easier?.let { easierId ->
                                    SmallLink("Easier version") {
                                        swaps[step.blockIndex] = easierId
                                        val e = ExerciseLibrary.get(easierId)
                                        if (e.measure == Measure.SECONDS) secondsLeft = workSecondsFor(e, block.target)
                                        say(e.name)
                                    }
                                }
                                SmallLink("Skip set") { paused = false; record(0) }
                            }
                        }
                        Phase.LOG -> WhitePill("Save set") { record(logValue) }
                        else -> Unit
                    }
                }
            }
        }
    }

    if (showExit) {
        GlowUpDialog(
            onDismissRequest = { showExit = false },
            title = if (logged.isEmpty()) "Leave this workout?" else "End workout early?",
            message = if (logged.isEmpty()) "Nothing has been logged yet." else "You've done ${logged.size} of ${steps.size} sets. Save what you did so it counts, or throw it away.",
            secondaryText = if (logged.isEmpty()) "Keep going" else "Discard",
            onSecondary = if (logged.isEmpty()) null else ({ showExit = false; finish() }),
            secondaryDestructive = logged.isNotEmpty(),
            primaryText = if (logged.isEmpty()) "Leave" else "Save & finish",
            onPrimary = {
                showExit = false
                if (logged.isEmpty()) finish() else {
                    paused = true
                    phase = Phase.SUMMARY
                }
            },
        )
    }
}

private fun workSecondsFor(exercise: Exercise, target: Int) =
    if (exercise.measure == Measure.SECONDS) target * (if (exercise.perSide) 2 else 1) else 0

private fun formatElapsed(seconds: Int) = "%d:%02d".format(seconds / 60, seconds % 60)

@Composable
private fun RoundIcon(icon: androidx.compose.ui.graphics.vector.ImageVector, description: String, onClick: () -> Unit) {
    Box(Modifier.size(42.dp).clip(CircleShape).background(Color.White.copy(alpha = 0.1f)).clickable(onClick = onClick), contentAlignment = Alignment.Center) {
        Icon(icon, contentDescription = description, tint = Color.White, modifier = Modifier.size(20.dp))
    }
}

@Composable
private fun WhitePill(text: String, modifier: Modifier = Modifier.fillMaxWidth(), onClick: () -> Unit) {
    Box(modifier.height(58.dp).clip(RoundedCornerShape(999.dp)).background(Color.White).clickable(onClick = onClick), contentAlignment = Alignment.Center) {
        Text(text, color = Ink, fontSize = 16.sp, fontWeight = FontWeight.Bold)
    }
}

@Composable
private fun DarkPill(text: String, modifier: Modifier, icon: androidx.compose.ui.graphics.vector.ImageVector? = null, onClick: () -> Unit) {
    Row(
        modifier.height(58.dp).clip(RoundedCornerShape(999.dp)).background(Color.White.copy(alpha = 0.12f)).clickable(onClick = onClick),
        horizontalArrangement = Arrangement.Center, verticalAlignment = Alignment.CenterVertically,
    ) {
        icon?.let { Icon(it, contentDescription = null, tint = Color.White, modifier = Modifier.size(18.dp)); Spacer(Modifier.width(6.dp)) }
        Text(text, color = Color.White, fontSize = 15.sp, fontWeight = FontWeight.SemiBold)
    }
}

@Composable
private fun SmallLink(text: String, onClick: () -> Unit) {
    Text(text, color = Color.White.copy(alpha = 0.7f), fontSize = 13.sp, fontWeight = FontWeight.SemiBold, modifier = Modifier.clip(RoundedCornerShape(999.dp)).clickable(onClick = onClick).padding(10.dp, 6.dp))
}

@Composable
private fun TimerRing(seconds: Int, total: Int, size: Dp, color: Color) {
    val fraction by animateFloatAsState((seconds / total.toFloat()).coerceIn(0f, 1f), tween(900), label = "ring")
    Box(Modifier.size(size), contentAlignment = Alignment.Center) {
        Canvas(Modifier.fillMaxSize()) {
            val stroke = 12.dp.toPx()
            val inset = stroke / 2
            drawArc(Color.White.copy(alpha = 0.1f), 0f, 360f, false, Offset(inset, inset), Size(this.size.width - stroke, this.size.height - stroke), style = Stroke(stroke))
            drawArc(color, -90f, 360f * fraction, false, Offset(inset, inset), Size(this.size.width - stroke, this.size.height - stroke), style = Stroke(stroke, cap = StrokeCap.Round))
        }
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(if (seconds >= 60) formatElapsed(seconds) else "$seconds", color = Color.White, fontSize = if (size > 180.dp) 60.sp else 52.sp, fontWeight = FontWeight.Bold)
            Text("seconds", color = Color.White.copy(alpha = 0.5f), fontSize = 12.sp)
        }
    }
}

@Composable
private fun Summary(title: String, logged: List<SetLog>, seconds: Int, effort: Int, onEffort: (Int) -> Unit, onSave: () -> Unit, onDiscard: () -> Unit) {
    val scored = logged.filter { !it.max }
    val percent = if (scored.isEmpty()) 100 else (scored.sumOf { minOf(it.done, it.target) } * 100.0 / scored.sumOf { it.target }.coerceAtLeast(1)).roundToInt()
    val bests = logged.map { it.exerciseId }.distinct().filter { id ->
        val old = FitnessStore.bestFor(id)
        old != null && logged.filter { it.exerciseId == id }.maxOf { it.done } > old
    }
    Column(Modifier.fillMaxSize()) {
        Column(Modifier.weight(1f).verticalScroll(rememberScrollState()).padding(24.dp, 24.dp, 24.dp, 16.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            Text("SCORECARD", color = Color.White.copy(alpha = 0.55f), fontSize = 12.sp, fontWeight = FontWeight.Bold, letterSpacing = 1.sp)
            Text(title, color = Color.White, fontSize = 20.sp, fontWeight = FontWeight.Bold, textAlign = TextAlign.Center, modifier = Modifier.padding(top = 4.dp))
            Spacer(Modifier.height(18.dp))
            Box(Modifier.size(150.dp), contentAlignment = Alignment.Center) {
                Canvas(Modifier.fillMaxSize()) {
                    val stroke = 14.dp.toPx()
                    val inset = stroke / 2
                    val color = when { percent >= 90 -> Color(0xFF7FD9A8); percent >= 60 -> Color(0xFFFFD84D); else -> Color(0xFFFF9C79) }
                    drawArc(Color.White.copy(alpha = 0.1f), 0f, 360f, false, Offset(inset, inset), Size(size.width - stroke, size.height - stroke), style = Stroke(stroke))
                    drawArc(color, -90f, 3.6f * percent.coerceIn(0, 100), false, Offset(inset, inset), Size(size.width - stroke, size.height - stroke), style = Stroke(stroke, cap = StrokeCap.Round))
                }
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("$percent%", color = Color.White, fontSize = 34.sp, fontWeight = FontWeight.Bold)
                    Text("of targets", color = Color.White.copy(alpha = 0.55f), fontSize = 12.sp)
                }
            }
            Spacer(Modifier.height(10.dp))
            Text(
                when {
                    percent >= 95 -> "Every target hit. Strong."
                    percent >= 75 -> "Solid session — almost everything done."
                    percent >= 50 -> "Good effort. The reps you got still count."
                    else -> "Showing up is the win today. It gets easier."
                },
                color = Color.White.copy(alpha = 0.75f), fontSize = 14.sp, textAlign = TextAlign.Center,
            )
            Text("${formatElapsed(seconds)} · ${logged.size} sets", color = Color.White.copy(alpha = 0.5f), fontSize = 12.5.sp, modifier = Modifier.padding(top = 4.dp))

            Spacer(Modifier.height(20.dp))
            logged.groupBy { it.exerciseId }.forEach { (id, sets) ->
                val e = ExerciseLibrary.get(id)
                Row(Modifier.fillMaxWidth().padding(vertical = 6.dp), verticalAlignment = Alignment.CenterVertically) {
                    ExerciseArt(e, 40.dp, 12.dp)
                    Spacer(Modifier.width(12.dp))
                    Column(Modifier.weight(1f)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(e.name, color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
                            if (id in bests) {
                                Spacer(Modifier.width(6.dp))
                                Icon(Icons.Filled.EmojiEvents, contentDescription = "Personal best", tint = Color(0xFFFFD84D), modifier = Modifier.size(15.dp))
                                Text(" New best", color = Color(0xFFFFD84D), fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                        Spacer(Modifier.height(4.dp))
                        Row(horizontalArrangement = Arrangement.spacedBy(5.dp)) {
                            sets.forEach { s ->
                                val ratio = if (s.max || s.target == 0) 1.0 else s.done.toDouble() / s.target
                                val c = when { ratio >= 1 -> Color(0xFF7FD9A8); ratio >= 0.6 -> Color(0xFFFFD84D); else -> Color(0xFFFF9C79) }
                                Text(
                                    if (s.max) "${s.done}" else "${s.done}/${s.target}",
                                    color = Ink, fontSize = 11.5.sp, fontWeight = FontWeight.Bold,
                                    modifier = Modifier.clip(RoundedCornerShape(999.dp)).background(c).padding(8.dp, 3.dp),
                                )
                            }
                        }
                    }
                }
            }

            Spacer(Modifier.height(20.dp))
            Text("How hard was it?", color = Color.White, fontSize = 15.sp, fontWeight = FontWeight.SemiBold)
            Spacer(Modifier.height(10.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                effortLabels.forEachIndexed { i, label ->
                    val on = effort == i + 1
                    Text(
                        label, color = if (on) Ink else Color.White, fontSize = 11.5.sp, fontWeight = FontWeight.SemiBold, textAlign = TextAlign.Center, maxLines = 1,
                        modifier = Modifier.weight(1f).clip(RoundedCornerShape(999.dp)).background(if (on) Color.White else Color.White.copy(alpha = 0.1f)).clickable { onEffort(i + 1) }.padding(vertical = 9.dp),
                    )
                }
            }
        }
        Column(Modifier.padding(24.dp, 8.dp, 24.dp, 20.dp)) {
            WhitePill("Save workout", onClick = onSave)
            Text(
                "Discard", color = Color.White.copy(alpha = 0.6f), fontSize = 13.5.sp, fontWeight = FontWeight.SemiBold,
                modifier = Modifier.align(Alignment.CenterHorizontally).padding(top = 12.dp).clickable(onClick = onDiscard),
            )
        }
    }
}

@Composable
private fun Saved(result: WorkoutResult, logged: List<SetLog>, seconds: Int, onDone: () -> Unit) {
    Column(Modifier.fillMaxSize()) {
        Column(Modifier.weight(1f).verticalScroll(rememberScrollState()).padding(24.dp, 40.dp, 24.dp, 16.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            Box(Modifier.size(84.dp).clip(CircleShape).background(Color(0xFF7FD9A8)), contentAlignment = Alignment.Center) {
                Icon(Icons.Filled.EmojiEvents, contentDescription = null, tint = Ink, modifier = Modifier.size(42.dp))
            }
            Spacer(Modifier.height(16.dp))
            Text("Workout saved", color = Color.White, fontSize = 24.sp, fontWeight = FontWeight.Bold)
            Text("+${result.points} points · ${formatElapsed(seconds)}", color = Color.White.copy(alpha = 0.6f), fontSize = 14.sp, modifier = Modifier.padding(top = 4.dp))

            if (result.personalBests.isNotEmpty()) {
                Spacer(Modifier.height(20.dp))
                Column(Modifier.fillMaxWidth().clip(RoundedCornerShape(20.dp)).background(Color(0xFFFFD84D).copy(alpha = 0.15f)).padding(16.dp)) {
                    Text("New personal bests", color = Color(0xFFFFD84D), fontSize = 13.sp, fontWeight = FontWeight.Bold)
                    result.personalBests.forEach { id ->
                        Text("${ExerciseLibrary.get(id).name} · ${logged.filter { it.exerciseId == id }.maxOf { it.done }}", color = Color.White, fontSize = 14.sp, modifier = Modifier.padding(top = 4.dp))
                    }
                }
            }

            Spacer(Modifier.height(14.dp))
            Column(Modifier.fillMaxWidth().clip(RoundedCornerShape(20.dp)).background(Color.White.copy(alpha = 0.08f)).padding(16.dp)) {
                Text("Next time", color = Color.White, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                if (ProGate.progressionLocked) {
                    Row(Modifier.padding(top = 6.dp).clickable { ProStore.showPaywall("Let Glow-Up raise your targets as you get stronger") }, verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Filled.Lock, contentDescription = null, tint = Color(0xFFFFD84D), modifier = Modifier.size(14.dp))
                        Spacer(Modifier.width(6.dp))
                        Text("With Pro, targets go up automatically when you hit them twice in a row.", color = Color.White.copy(alpha = 0.7f), fontSize = 12.5.sp, lineHeight = 17.sp)
                    }
                } else if (result.changes.isEmpty()) {
                    Text("Same targets. Hit every set twice in a row and they'll go up.", color = Color.White.copy(alpha = 0.7f), fontSize = 12.5.sp, lineHeight = 17.sp, modifier = Modifier.padding(top = 6.dp))
                } else {
                    result.changes.forEach { c ->
                        val e = ExerciseLibrary.get(c.exerciseId)
                        Row(Modifier.padding(top = 6.dp), verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                if (c.direction == Direction.UP) Icons.AutoMirrored.Filled.TrendingUp else Icons.AutoMirrored.Filled.TrendingDown,
                                contentDescription = null, tint = if (c.direction == Direction.UP) Color(0xFF7FD9A8) else Color(0xFFFFD84D), modifier = Modifier.size(16.dp),
                            )
                            Spacer(Modifier.width(8.dp))
                            val unit = if (e.measure == Measure.REPS) "reps" else "sec"
                            Text(
                                "${e.name}: ${if (c.direction == Direction.UP) "+" else "−"}${e.step} $unit",
                                color = Color.White, fontSize = 13.5.sp,
                            )
                        }
                    }
                }
            }
        }
        Box(Modifier.padding(24.dp, 8.dp, 24.dp, 20.dp)) { WhitePill("Done", onClick = onDone) }
    }
}
