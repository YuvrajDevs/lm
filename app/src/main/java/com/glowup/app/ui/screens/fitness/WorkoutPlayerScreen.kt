package com.glowup.app.ui.screens.fitness

import android.app.Activity
import android.speech.tts.TextToSpeech
import androidx.activity.compose.BackHandler
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.VolumeOff
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material.icons.filled.Close
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.view.WindowCompat
import androidx.navigation.NavHostController
import com.glowup.app.R
import com.glowup.app.ui.components.ReMakeUDialog
import com.glowup.app.ui.components.goBack
import com.glowup.app.ui.data.fitness.Exercise
import com.glowup.app.ui.data.fitness.ExerciseLibrary
import com.glowup.app.ui.data.fitness.FitnessStore
import com.glowup.app.ui.data.fitness.Measure
import com.glowup.app.ui.data.fitness.SetLog
import com.glowup.app.ui.data.fitness.WorkoutResult
import com.glowup.app.ui.design.AppButton
import com.glowup.app.ui.design.ButtonVariant
import com.glowup.app.ui.design.LineHeight
import com.glowup.app.ui.design.Shapes
import com.glowup.app.ui.design.Space
import com.glowup.app.ui.design.TextSize
import com.glowup.app.ui.theme.*
import java.util.Locale
import kotlinx.coroutines.delay

private enum class Phase { READY, WORK, LOG, REST, SUMMARY, SAVED }

private data class SetStep(val blockIndex: Int, val set: Int)

internal val effortLabels = listOf("Easy", "Steady", "Hard", "Very hard", "All-out")

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
        val window = (view.context as? Activity)?.window
        val bars = window?.let { WindowCompat.getInsetsController(it, view) }
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
        secondsLeft =
            workSecondsFor(
                plan.blocks[steps[stepIndex].blockIndex].let { b ->
                    swaps[steps[stepIndex].blockIndex]?.let(ExerciseLibrary::get) ?: b.exercise
                },
                plan.blocks[steps[stepIndex].blockIndex].target
            )
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
        } else {
            startWork()
        }
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
                plan.title,
                logged,
                elapsed,
                effort,
                onEffort = { effort = it },
                onSave = {
                    result = FitnessStore.saveWorkout(plan, logged.toList(), elapsed, effort)
                    phase = Phase.SAVED
                },
                onDiscard = { finish() }
            )
            Phase.SAVED -> Saved(result!!, logged, elapsed) { finish() }
            else -> Column(Modifier.fillMaxSize()) {
                // Top bar
                Row(Modifier.fillMaxWidth().padding(Space.L, Space.M), verticalAlignment = Alignment.CenterVertically) {
                    RoundIcon(Icons.Filled.Close, "End workout") { showExit = true }
                    Spacer(Modifier.width(Space.M))
                    Box(Modifier.weight(1f).height(6.dp).clip(Shapes.Pill).background(Color.White.copy(alpha = 0.15f))) {
                        val progress by animateFloatAsState(logged.size / steps.size.toFloat(), tween(400), label = "progress")
                        Box(Modifier.fillMaxWidth(progress).fillMaxHeight().clip(Shapes.Pill).background(Mint))
                    }
                    Spacer(Modifier.width(Space.M))
                    RoundIcon(if (FitnessStore.voiceOn) Icons.AutoMirrored.Filled.VolumeUp else Icons.AutoMirrored.Filled.VolumeOff, "Voice cues") {
                        FitnessStore.updateVoice(!FitnessStore.voiceOn)
                    }
                }
                Text(
                    stringResource(R.string.workout_player_exercise_1_s_of_2, step.blockIndex + 1, plan.blocks.size, step.set, block.sets) + " · " +
                        formatElapsed(elapsed),
                    color = Color.White.copy(alpha = 0.55f),
                    fontSize = TextSize.BodySmall,
                    fontWeight = FontWeight.Medium,
                    modifier = Modifier.align(Alignment.CenterHorizontally)
                )

                Column(
                    Modifier.weight(1f).fillMaxWidth().verticalScroll(rememberScrollState()).padding(Space.Gutter, Space.L),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    if (phase == Phase.REST) {
                        Text(
                            stringResource(R.string.workout_player_rest),
                            color = Color.White.copy(alpha = 0.55f),
                            fontSize = TextSize.BodySmall,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.sp
                        )
                        Spacer(Modifier.height(Space.S))
                        TimerRing(secondsLeft, block.restSeconds.coerceAtLeast(1), 200.dp, Sky)
                        Spacer(Modifier.height(Space.Xl))
                        Text(stringResource(R.string.workout_player_next_up), color = Color.White.copy(alpha = 0.55f), fontSize = TextSize.Caption)
                        Spacer(Modifier.height(Space.S))
                        Row(
                            Modifier.fillMaxWidth().clip(Shapes.Card).background(Color.White.copy(alpha = 0.08f)).padding(Space.M),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            ExerciseArt(exercise, 64.dp, 16.dp)
                            Spacer(Modifier.width(Space.M))
                            Column {
                                Text(exercise.name, color = Color.White, fontSize = TextSize.Title, fontWeight = FontWeight.Bold)
                                Text(
                                    (if (block.max) stringResource(R.string.workout_player_max_effort) else exercise.doseLabel(block.target)) +
                                        " · set ${step.set} of ${block.sets}",
                                    color = Color.White.copy(alpha = 0.6f),
                                    fontSize = TextSize.BodySmall
                                )
                            }
                        }
                    } else {
                        ExerciseArt(exercise, 190.dp, 32.dp)
                        Spacer(Modifier.height(Space.L))
                        Text(exercise.name, color = Color.White, fontSize = TextSize.Headline, fontWeight = FontWeight.Bold, textAlign = TextAlign.Center)
                        Spacer(Modifier.height(Space.S))
                        Row(horizontalArrangement = Arrangement.spacedBy(Space.S)) { exercise.muscles.take(3).forEach { MuscleChip(it, onDark = true) } }
                        Spacer(Modifier.height(Space.Xl))
                        when (phase) {
                            Phase.READY -> {
                                Text(stringResource(R.string.workout_player_get_ready), color = Color.White.copy(alpha = 0.6f), fontSize = TextSize.Body)
                                Text("$secondsLeft", color = Color.White, fontSize = TextSize.Jumbo, fontWeight = FontWeight.Bold)
                            }
                            Phase.WORK -> if (exercise.measure == Measure.SECONDS) {
                                TimerRing(secondsLeft, workSeconds.coerceAtLeast(1), 170.dp, Mint)
                                if (exercise.perSide) {
                                    Text(
                                        if (secondsLeft >
                                            workSeconds / 2
                                        ) {
                                            stringResource(R.string.workout_player_first_side)
                                        } else {
                                            stringResource(R.string.workout_player_second_side)
                                        },
                                        color = Color.White.copy(alpha = 0.6f),
                                        fontSize = TextSize.BodySmall,
                                        modifier = Modifier.padding(top = Space.S)
                                    )
                                }
                            } else {
                                Text(
                                    if (block.max) stringResource(R.string.workout_player_max) else "${block.target}",
                                    color = Color.White,
                                    fontSize = TextSize.Jumbo,
                                    fontWeight = FontWeight.Bold,
                                    lineHeight = 80.sp
                                )
                                Text(
                                    if (block.max) {
                                        stringResource(R.string.workout_player_as_many_good_reps_as, block.target)
                                    } else if (exercise.perSide) {
                                        stringResource(R.string.workout_player_reps_each_side)
                                    } else {
                                        stringResource(R.string.workout_player_reps)
                                    },
                                    color = Color.White.copy(alpha = 0.6f),
                                    fontSize = TextSize.Body
                                )
                                FitnessStore.bestFor(exercise.id)?.let {
                                    Text(
                                        stringResource(R.string.common_your_best_1_s, it),
                                        color = Mint,
                                        fontSize = TextSize.BodySmall,
                                        fontWeight = FontWeight.SemiBold,
                                        modifier = Modifier.padding(top = Space.Xs)
                                    )
                                }
                            }
                            Phase.LOG -> {
                                Text(
                                    stringResource(R.string.workout_player_how_many_did_you_get),
                                    color = Color.White,
                                    fontSize = TextSize.Title,
                                    fontWeight = FontWeight.SemiBold
                                )
                                Text(
                                    if (block.max) {
                                        stringResource(
                                            R.string.workout_player_count_only_clean_reps
                                        )
                                    } else {
                                        stringResource(R.string.workout_player_target_was_1_s, block.target)
                                    },
                                    color = Color.White.copy(alpha = 0.55f),
                                    fontSize = TextSize.BodySmall
                                )
                                Spacer(Modifier.height(Space.M))
                                NumberStepper("$logValue", {
                                    logValue = (logValue - 1).coerceAtLeast(0)
                                }, { logValue++ }, onDark = true, big = true, unit = if (exercise.perSide) "each side" else "reps")
                                Spacer(Modifier.height(Space.M))
                                Row(horizontalArrangement = Arrangement.spacedBy(Space.S)) {
                                    listOf(block.target / 2, block.target - 2, block.target, block.target + 2).filter { it > 0 }.distinct().forEach { v ->
                                        Text(
                                            "$v",
                                            color = if (v == logValue) Ink else Color.White,
                                            fontSize = TextSize.Body,
                                            fontWeight = FontWeight.Bold,
                                            modifier = Modifier.clip(Shapes.Pill).background(
                                                if (v ==
                                                    logValue
                                                ) {
                                                    Color.White
                                                } else {
                                                    Color.White.copy(alpha = 0.12f)
                                                }
                                            ).clickable { logValue = v }.padding(Space.L, Space.S)
                                        )
                                    }
                                }
                            }
                            else -> Unit
                        }
                        if (phase == Phase.WORK) {
                            Spacer(Modifier.height(Space.M))
                            Text(
                                exercise.cue,
                                color = Color.White.copy(alpha = 0.6f),
                                fontSize = TextSize.BodySmall,
                                textAlign = TextAlign.Center,
                                lineHeight = LineHeight.Compact
                            )
                            if (showHow) {
                                Spacer(Modifier.height(Space.M))
                                Column(Modifier.fillMaxWidth().clip(Shapes.Medium).background(Color.White.copy(alpha = 0.08f)).padding(Space.L)) {
                                    exercise.steps.forEachIndexed { i, s ->
                                        Text(
                                            "${i + 1}. $s",
                                            color = Color.White.copy(alpha = 0.85f),
                                            fontSize = TextSize.BodySmall,
                                            lineHeight = LineHeight.Compact,
                                            modifier = Modifier.padding(vertical = Space.Xs)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                // Controls
                Column(Modifier.padding(Space.Gutter, Space.Xs, Space.Gutter, Space.Xl)) {
                    when (phase) {
                        Phase.READY -> WhitePill(stringResource(R.string.workout_player_start_now)) { startWork() }
                        Phase.REST -> Row(horizontalArrangement = Arrangement.spacedBy(Space.S)) {
                            DarkPill("+15 s", Modifier.weight(1f)) { secondsLeft += 15 }
                            WhitePill(stringResource(R.string.workout_player_skip_rest), Modifier.weight(1.4f)) { startWork() }
                        }
                        Phase.WORK -> {
                            if (exercise.measure == Measure.SECONDS) {
                                Row(horizontalArrangement = Arrangement.spacedBy(Space.S)) {
                                    DarkPill(
                                        if (paused) stringResource(R.string.workout_player_resume) else stringResource(R.string.workout_player_pause),
                                        Modifier.weight(1f),
                                        if (paused) Icons.Filled.PlayArrow else Icons.Filled.Pause
                                    ) {
                                        paused =
                                            !paused
                                    }
                                    WhitePill(stringResource(R.string.workout_player_finish_set), Modifier.weight(1.4f)) {
                                        val done = (workSeconds - secondsLeft) / (if (exercise.perSide) 2 else 1)
                                        paused = false
                                        record(done)
                                    }
                                }
                            } else {
                                WhitePill(stringResource(R.string.common_done)) {
                                    logValue = if (block.max) (FitnessStore.bestFor(exercise.id) ?: block.target / 2) else block.target
                                    phase = Phase.LOG
                                }
                            }
                            Spacer(Modifier.height(Space.M))
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
                                SmallLink("Skip set") {
                                    paused = false
                                    record(0)
                                }
                            }
                        }
                        Phase.LOG -> WhitePill(stringResource(R.string.workout_player_save_set)) { record(logValue) }
                        else -> Unit
                    }
                }
            }
        }
    }

    if (showExit) {
        ReMakeUDialog(
            onDismissRequest = { showExit = false },
            title = if (logged.isEmpty()) "Leave this workout?" else "End workout early?",
            message = if (logged.isEmpty()) "Nothing has been logged yet." else "You've done ${logged.size} of ${steps.size} sets. Save what you did so it counts, or throw it away.",
            secondaryText = if (logged.isEmpty()) "Keep going" else "Discard",
            onSecondary = if (logged.isEmpty()) {
                null
            } else {
                (
                    {
                        showExit = false
                        finish()
                    }
                    )
            },
            secondaryDestructive = logged.isNotEmpty(),
            primaryText = if (logged.isEmpty()) "Leave" else "Save & finish",
            onPrimary = {
                showExit = false
                if (logged.isEmpty()) {
                    finish()
                } else {
                    paused = true
                    phase = Phase.SUMMARY
                }
            }
        )
    }
}

private fun workSecondsFor(exercise: Exercise, target: Int) = if (exercise.measure == Measure.SECONDS) target * (if (exercise.perSide) 2 else 1) else 0

internal fun formatElapsed(seconds: Int) = "%d:%02d".format(seconds / 60, seconds % 60)

@Composable
private fun RoundIcon(icon: ImageVector, description: String, onClick: () -> Unit) {
    Box(Modifier.size(42.dp).clip(CircleShape).background(Color.White.copy(alpha = 0.1f)).clickable(onClick = onClick), contentAlignment = Alignment.Center) {
        Icon(icon, contentDescription = description, tint = Color.White, modifier = Modifier.size(20.dp))
    }
}

@Composable
internal fun WhitePill(text: String, modifier: Modifier = Modifier.fillMaxWidth(), onClick: () -> Unit) {
    AppButton(text, variant = ButtonVariant.OnDarkPrimary, modifier = modifier, onClick = onClick)
}

@Composable
private fun DarkPill(text: String, modifier: Modifier, icon: ImageVector? = null, onClick: () -> Unit) {
    AppButton(text, variant = ButtonVariant.OnDarkSecondary, modifier = modifier, icon = icon, onClick = onClick)
}

@Composable
private fun SmallLink(text: String, onClick: () -> Unit) {
    Text(
        text,
        color = Color.White.copy(alpha = 0.7f),
        fontSize = TextSize.BodySmall,
        fontWeight = FontWeight.SemiBold,
        modifier = Modifier.clip(Shapes.Pill).clickable(onClick = onClick).padding(Space.S, Space.S)
    )
}

@Composable
private fun TimerRing(seconds: Int, total: Int, size: Dp, color: Color) {
    val fraction by animateFloatAsState((seconds / total.toFloat()).coerceIn(0f, 1f), tween(900), label = "ring")
    Box(Modifier.size(size), contentAlignment = Alignment.Center) {
        Canvas(Modifier.fillMaxSize()) {
            val stroke = 12.dp.toPx()
            val inset = stroke / 2
            drawArc(
                Color.White.copy(alpha = 0.1f),
                0f,
                360f,
                false,
                Offset(inset, inset),
                Size(this.size.width - stroke, this.size.height - stroke),
                style = Stroke(stroke)
            )
            drawArc(
                color,
                -90f,
                360f * fraction,
                false,
                Offset(inset, inset),
                Size(this.size.width - stroke, this.size.height - stroke),
                style = Stroke(stroke, cap = StrokeCap.Round)
            )
        }
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                if (seconds >=
                    60
                ) {
                    formatElapsed(seconds)
                } else {
                    "$seconds"
                },
                color = Color.White,
                fontSize = if (size > 180.dp) 60.sp else 52.sp,
                fontWeight = FontWeight.Bold
            )
            Text(stringResource(R.string.workout_player_seconds), color = Color.White.copy(alpha = 0.5f), fontSize = TextSize.Caption)
        }
    }
}
