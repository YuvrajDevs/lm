package com.glowup.app.ui.screens.glow

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Accessibility
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Face
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.MonitorWeight
import androidx.compose.material.icons.outlined.Quiz
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import com.glowup.app.BuildConfig
import com.glowup.app.ui.components.ConsentSheet
import com.glowup.app.ui.components.goBack
import com.glowup.app.ui.data.Consent
import com.glowup.app.ui.data.ConsentStore
import com.glowup.app.ui.data.GroomingStore
import com.glowup.app.ui.data.ProfileStore
import com.glowup.app.ui.data.RoutineStore
import com.glowup.app.ui.data.fitness.FitnessGoal
import com.glowup.app.ui.data.fitness.FitnessLevel
import com.glowup.app.ui.data.fitness.FitnessStore
import com.glowup.app.ui.data.fitness.Programmes
import com.glowup.app.ui.data.glow.CheckResult
import com.glowup.app.ui.data.glow.FaceMetrics
import com.glowup.app.ui.data.glow.FocusArea
import com.glowup.app.ui.data.glow.GlowCheckStore
import com.glowup.app.ui.data.glow.GlowQuestions
import com.glowup.app.ui.data.glow.GlowScoring
import com.glowup.app.ui.data.glow.PostureAnalysis
import com.glowup.app.ui.screens.fitness.ChoicePill
import com.glowup.app.ui.screens.fitness.NumberStepper
import com.glowup.app.ui.theme.*
import java.time.DayOfWeek

private enum class Step { INTRO, GOALS, FACE, POSTURE, BODY, QUESTIONS, RESULTS, PLAN }

@OptIn(androidx.compose.foundation.layout.ExperimentalLayoutApi::class)
@Composable
fun GlowCheckScreen(navController: NavHostController) {
    val minor = ProfileStore.isMinor
    val previous = GlowCheckStore.latest
    val steps = remember { Step.entries.filter { !(it == Step.BODY && minor) } }
    var step by remember { mutableStateOf(Step.INTRO) }
    var goals by remember { mutableStateOf(previous?.goals ?: ProfileStore.focusAreas.mapNotNull(::goalFromFocus).toSet()) }
    var minutes by remember { mutableStateOf(previous?.minutesPerDay ?: 20) }
    var face by remember { mutableStateOf<FaceMetrics?>(null) }
    var manualShape by remember { mutableStateOf(ProfileStore.faceShape) }
    var neck by remember { mutableStateOf<Int?>(null) }
    var scanning by remember { mutableStateOf(false) }
    var askConsent by remember { mutableStateOf<Consent?>(null) }
    var height by remember { mutableStateOf(FitnessStore.heightCm ?: previous?.heightCm ?: 172) }
    var weight by remember { mutableStateOf(FitnessStore.latestBody()?.weightKg ?: previous?.weightKg ?: 72.0) }
    var waist by remember { mutableStateOf(FitnessStore.latestBody()?.waistCm ?: previous?.waistCm) }
    var includeBody by remember { mutableStateOf(ConsentStore.has(Consent.BODY) && (FitnessStore.latestBody() != null || previous?.weightKg != null)) }
    val answers = remember { mutableStateMapOf<String, String>().apply { previous?.answers?.let(::putAll) } }
    var result by remember { mutableStateOf<CheckResult?>(null) }

    fun next() {
        val i = steps.indexOf(step)
        if (i < steps.lastIndex) step = steps[i + 1]
        if (step == Step.RESULTS && result == null) {
            val r = GlowCheckStore.build(
                goals, minutes, answers.toMap(), face, manualShape, neck,
                if (includeBody) height else null, if (includeBody) weight else null, if (includeBody) waist else null,
            )
            GlowCheckStore.save(r)
            result = r
        }
    }
    fun back() {
        val i = steps.indexOf(step)
        if (i == 0 || step == Step.RESULTS) navController.goBack() else { scanning = false; step = steps[i - 1] }
    }

    Column(Modifier.fillMaxSize().background(Surface).systemBarsPadding()) {
        Row(Modifier.fillMaxWidth().padding(16.dp, 12.dp, 24.dp, 0.dp), verticalAlignment = Alignment.CenterVertically) {
            Box(Modifier.size(42.dp).clip(CircleShape).clickable { navController.goBack() }, contentAlignment = Alignment.Center) {
                Icon(Icons.Filled.Close, contentDescription = "Close", tint = Ink)
            }
            Spacer(Modifier.width(8.dp))
            val progressSteps = steps.filter { it != Step.INTRO }
            Row(Modifier.weight(1f), horizontalArrangement = Arrangement.spacedBy(5.dp)) {
                progressSteps.forEach { s ->
                    Box(Modifier.weight(1f).height(4.dp).clip(RoundedCornerShape(999.dp)).background(if (steps.indexOf(s) <= steps.indexOf(step)) Ink else ChipNeutral))
                }
            }
        }

        Column(Modifier.weight(1f).verticalScroll(remember(step) { androidx.compose.foundation.ScrollState(0) }, enabled = !scanning).padding(24.dp, 22.dp, 24.dp, 16.dp)) {
            when (step) {
                Step.INTRO -> {
                    Title(if (previous == null) "Your Glow-Up Check" else "Time for a rescan")
                    Sub(
                        if (previous == null) "Three minutes to find what will make the biggest difference for you — and build a plan around it."
                        else "We'll measure again and show you what's changed since ${previous.date.dayOfMonth} ${previous.date.month.name.lowercase().replaceFirstChar { it.uppercase() }}.",
                    )
                    Spacer(Modifier.height(20.dp))
                    IntroRow(Icons.Outlined.Quiz, "A few quick questions", "About your goals, skin, sleep and habits")
                    IntroRow(Icons.Filled.Face, "Optional face scan", "Face shape and a symmetry baseline")
                    IntroRow(Icons.Filled.Accessibility, "Optional posture scan", "Side-on, to measure your neck angle")
                    IntroRow(Icons.Filled.Check, "Your map and plan", "Your top 3 areas, your strengths, and daily routines")
                    Spacer(Modifier.height(16.dp))
                    Column(Modifier.fillMaxWidth().clip(RoundedCornerShape(20.dp)).background(GreenTint).padding(16.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Filled.Lock, contentDescription = null, tint = Green, modifier = Modifier.size(16.dp))
                            Spacer(Modifier.width(8.dp))
                            Text("Our promise", color = Color(0xFF1E7A4F), fontSize = 13.5.sp, fontWeight = FontWeight.Bold)
                        }
                        Spacer(Modifier.height(6.dp))
                        listOf(
                            "No ratings out of 10 and no \"attractiveness score\" — ever.",
                            "No photo is taken. The camera is read live and only numbers are kept.",
                            "We only point at things you can actually change.",
                        ).forEach { Text("• $it", color = Color(0xFF1E7A4F), fontSize = 12.5.sp, lineHeight = 18.sp) }
                    }
                }
                Step.GOALS -> {
                    Title("What do you want to improve?")
                    Sub("Pick as many as you like.")
                    Spacer(Modifier.height(16.dp))
                    FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        GlowQuestions.goals.filter { !(minor && it == "Body") || true }.forEach { g ->
                            ChoicePill(g, g in goals) { goals = if (g in goals) goals - g else goals + g }
                        }
                    }
                    Spacer(Modifier.height(24.dp))
                    Text("Time you can give each day", fontSize = 15.sp, fontWeight = FontWeight.SemiBold)
                    Spacer(Modifier.height(10.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        GlowQuestions.minuteOptions.forEach { m -> ChoicePill("$m min", minutes == m, Modifier.weight(1f)) { minutes = m } }
                    }
                    Text("We'll keep your plan within this.", color = TextSecondary, fontSize = 12.sp, modifier = Modifier.padding(top = 8.dp))
                }
                Step.FACE -> {
                    Title("Face scan")
                    Sub("Optional. Hold your phone at eye level in even light, hair off your forehead, neutral face.")
                    Spacer(Modifier.height(16.dp))
                    when {
                        face != null -> FaceResultCard(face!!) { face = null; scanning = true }
                        scanning -> CameraScanner(
                            ScanMode.FACE,
                            onFace = { face = it; manualShape = it.shape; scanning = false },
                            onPermissionDenied = { scanning = false },
                            modifier = Modifier.fillMaxWidth().aspectRatio(0.78f),
                        )
                        else -> {
                            ScanStart("Start face scan", "Takes about 5 seconds") {
                                if (ConsentStore.has(Consent.FACE_SCAN)) scanning = true else askConsent = Consent.FACE_SCAN
                            }
                            if (BuildConfig.DEBUG) DebugLink("Developer: use a sample face") { face = FaceMetrics("Oval", 1.32, 0.9, 0.82, 91, 1.15); manualShape = "Oval" }
                            Spacer(Modifier.height(20.dp))
                            Text("Or pick your face shape yourself", fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
                            Spacer(Modifier.height(8.dp))
                            FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                GroomingStore.faceShapes.forEach { s -> ChoicePill(s, manualShape == s) { manualShape = if (manualShape == s) null else s } }
                            }
                        }
                    }
                }
                Step.POSTURE -> {
                    Title("Posture scan")
                    Sub("Optional. Prop your phone up at chest height, step back about 2 m and stand side-on, relaxed — how you normally stand.")
                    Spacer(Modifier.height(16.dp))
                    when {
                        neck != null -> PostureResultCard(neck!!) { neck = null; scanning = true }
                        scanning -> CameraScanner(
                            ScanMode.POSTURE,
                            onPosture = { neck = it; scanning = false },
                            onPermissionDenied = { scanning = false },
                            modifier = Modifier.fillMaxWidth().aspectRatio(0.62f),
                        )
                        else -> {
                            ScanStart("Start posture scan", "You'll hear a beep when it's done") {
                                if (ConsentStore.has(Consent.FACE_SCAN)) scanning = true else askConsent = Consent.FACE_SCAN
                            }
                            if (BuildConfig.DEBUG) DebugLink("Developer: use a sample posture") { neck = 66 }
                        }
                    }
                }
                Step.BODY -> {
                    Title("Body")
                    Sub("Optional. Body fat is the biggest single factor in how defined your face looks, so this makes your plan much more accurate.")
                    Spacer(Modifier.height(16.dp))
                    if (!includeBody) {
                        Box(
                            Modifier.fillMaxWidth().height(52.dp).clip(RoundedCornerShape(999.dp)).background(Ink).clickable {
                                if (ConsentStore.has(Consent.BODY)) includeBody = true else askConsent = Consent.BODY
                            },
                            contentAlignment = Alignment.Center,
                        ) { Text("Add height, weight & waist", color = Color.White, fontSize = 14.5.sp, fontWeight = FontWeight.SemiBold) }
                    } else {
                        MeasureRow("Height", "$height", "cm", { height = (height - 1).coerceAtLeast(120) }, { height = (height + 1).coerceAtMost(230) })
                        MeasureRow("Weight", "%.1f".format(weight), "kg", { weight = (weight - 0.5).coerceAtLeast(30.0) }, { weight += 0.5 })
                        MeasureRow("Waist", waist?.let { "%.0f".format(it) } ?: "—", "cm", { waist = ((waist ?: 81.0) - 1).coerceAtLeast(40.0) }, { waist = (waist ?: 79.0) + 1 }, "At the belly button, relaxed")
                    }
                }
                Step.QUESTIONS -> {
                    Title("Quick questions")
                    Sub("Tap the closest answer. Skip any you're not sure about.")
                    Spacer(Modifier.height(8.dp))
                    GlowQuestions.all.forEach { q ->
                        Spacer(Modifier.height(16.dp))
                        Text(q.text, fontSize = 15.sp, fontWeight = FontWeight.SemiBold)
                        q.hint?.let { Text(it, color = TextSecondary, fontSize = 12.sp, modifier = Modifier.padding(top = 2.dp)) }
                        Spacer(Modifier.height(8.dp))
                        FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            q.options.forEach { o -> ChoicePill(o, answers[q.id] == o) { if (answers[q.id] == o) answers.remove(q.id) else answers[q.id] = o } }
                        }
                    }
                }
                Step.RESULTS -> result?.let { r ->
                    ResultsContent(r, GlowCheckStore.results.filter { it.date != r.date }.maxByOrNull { it.date }, navController)
                }
                Step.PLAN -> result?.let { r -> PlanContent(r, navController) }
            }
        }

        if (step != Step.PLAN && !scanning) {
            Column(Modifier.padding(24.dp, 8.dp, 24.dp, 24.dp)) {
                val label = when (step) {
                    Step.INTRO -> "Let's go"
                    Step.FACE -> if (face == null && manualShape == null) "Skip" else "Continue"
                    Step.POSTURE -> if (neck == null) "Skip" else "Continue"
                    Step.BODY -> if (includeBody) "Continue" else "Skip"
                    Step.QUESTIONS -> "See my results"
                    Step.RESULTS -> "Build my plan"
                    else -> "Continue"
                }
                val secondary = label == "Skip"
                Box(
                    Modifier.fillMaxWidth().height(56.dp).clip(RoundedCornerShape(999.dp)).background(if (secondary) ChipNeutral else Ink).clickable { next() },
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        label, color = if (secondary) Ink else Color.White, fontSize = 15.sp, fontWeight = FontWeight.SemiBold,
                    )
                }
                if (step != Step.INTRO && step != Step.RESULTS) {
                    Text("Back", color = TextSecondary, fontSize = 14.sp, fontWeight = FontWeight.SemiBold, modifier = Modifier.align(Alignment.CenterHorizontally).padding(top = 12.dp).clickable { back() })
                }
            }
        } else if (scanning) {
            Text(
                "Cancel scan", color = TextSecondary, fontSize = 14.sp, fontWeight = FontWeight.SemiBold,
                modifier = Modifier.align(Alignment.CenterHorizontally).padding(16.dp, 8.dp, 16.dp, 24.dp).clickable { scanning = false },
            )
        }
    }

    askConsent?.let { consent ->
        ConsentSheet(
            consent,
            if (consent == Consent.BODY) Icons.Filled.MonitorWeight else Icons.Filled.Face,
            onAllow = {
                askConsent = null
                if (consent == Consent.BODY) includeBody = true else scanning = true
            },
            onDecline = { askConsent = null },
        )
    }
}

private fun goalFromFocus(focus: String): String? = when (focus) {
    "Looks" -> "Face & jawline"
    "Style" -> "Style"
    "Confidence", "Talk" -> "Confidence"
    "Sleep" -> "Sleep"
    "Fitness" -> "Body"
    else -> null
}

@Composable
private fun Title(text: String) = Text(text, fontSize = 26.sp, fontWeight = FontWeight.Bold, lineHeight = 32.sp)

@Composable
private fun Sub(text: String) {
    Spacer(Modifier.height(8.dp))
    Text(text, color = TextSecondary, fontSize = 14.sp, lineHeight = 20.sp)
}

@Composable
private fun IntroRow(icon: ImageVector, title: String, body: String) {
    Row(Modifier.fillMaxWidth().padding(vertical = 7.dp), verticalAlignment = Alignment.CenterVertically) {
        Box(Modifier.size(44.dp).clip(CircleShape).background(CardNeutral), contentAlignment = Alignment.Center) {
            Icon(icon, contentDescription = null, tint = Ink, modifier = Modifier.size(20.dp))
        }
        Spacer(Modifier.width(14.dp))
        Column {
            Text(title, fontSize = 15.sp, fontWeight = FontWeight.SemiBold)
            Text(body, color = TextSecondary, fontSize = 12.5.sp)
        }
    }
}

@Composable
private fun ScanStart(title: String, sub: String, onClick: () -> Unit) {
    Column(
        Modifier.fillMaxWidth().clip(RoundedCornerShape(24.dp)).background(Ink).clickable(onClick = onClick).padding(22.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Box(Modifier.size(56.dp).clip(CircleShape).background(Color.White.copy(alpha = 0.12f)), contentAlignment = Alignment.Center) {
            Icon(Icons.Filled.Face, contentDescription = null, tint = Color.White)
        }
        Spacer(Modifier.height(10.dp))
        Text(title, color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.Bold)
        Text(sub, color = Color.White.copy(alpha = 0.6f), fontSize = 12.5.sp)
        Spacer(Modifier.height(10.dp))
        Text("No photo is taken or saved", color = Color(0xFF7FD9A8), fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
    }
}

@Composable
private fun DebugLink(text: String, onClick: () -> Unit) {
    Text(text, color = Purple, fontSize = 12.5.sp, fontWeight = FontWeight.SemiBold, modifier = Modifier.padding(top = 10.dp).clickable(onClick = onClick))
}

@Composable
private fun FaceResultCard(face: FaceMetrics, onRetake: () -> Unit) {
    Column(Modifier.fillMaxWidth().clip(RoundedCornerShape(24.dp)).background(PurpleTint).padding(20.dp)) {
        Text("MEASURED", color = Purple, fontSize = 11.sp, fontWeight = FontWeight.Bold, letterSpacing = 0.4.sp)
        Text("${face.shape} face", fontSize = 24.sp, fontWeight = FontWeight.Bold)
        Text("Symmetry ${face.symmetry}/100 · most people land between 88 and 97", color = Color(0xFF5A4A8C), fontSize = 13.sp)
        Spacer(Modifier.height(12.dp))
        Text("Retake", fontSize = 13.sp, fontWeight = FontWeight.SemiBold, modifier = Modifier.clip(RoundedCornerShape(999.dp)).background(Color.White).clickable(onClick = onRetake).padding(16.dp, 8.dp))
    }
}

@Composable
private fun PostureResultCard(angle: Int, onRetake: () -> Unit) {
    val level = PostureAnalysis.level(angle)
    Column(Modifier.fillMaxWidth().clip(RoundedCornerShape(24.dp)).background(BlueTint).padding(20.dp)) {
        Text("MEASURED", color = Navy, fontSize = 11.sp, fontWeight = FontWeight.Bold, letterSpacing = 0.4.sp)
        Text("$angle° · ${level.label}", fontSize = 24.sp, fontWeight = FontWeight.Bold)
        Text("90° means your ear is right above your shoulder. Above 75° is upright.", color = Navy, fontSize = 13.sp, lineHeight = 18.sp)
        Spacer(Modifier.height(12.dp))
        Text("Retake", fontSize = 13.sp, fontWeight = FontWeight.SemiBold, modifier = Modifier.clip(RoundedCornerShape(999.dp)).background(Color.White).clickable(onClick = onRetake).padding(16.dp, 8.dp))
    }
}

@Composable
private fun MeasureRow(title: String, value: String, unit: String, onMinus: () -> Unit, onPlus: () -> Unit, hint: String? = null) {
    Row(Modifier.fillMaxWidth().padding(bottom = 10.dp).clip(RoundedCornerShape(20.dp)).background(CardNeutral).padding(16.dp, 12.dp), verticalAlignment = Alignment.CenterVertically) {
        Column(Modifier.weight(1f)) {
            Text(title, fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
            hint?.let { Text(it, color = TextSecondary, fontSize = 11.5.sp) }
        }
        NumberStepper(value, onMinus, onPlus, unit = unit)
    }
}

@Composable
private fun ColumnScope.PlanContent(result: CheckResult, navController: NavHostController) {
    val minor = ProfileStore.isMinor
    val plan = remember(result) { GlowScoring.plan(result, minor) }
    val ideal = plan.programmeId
    // Free users get a free programme now; the better-fitting Pro one is offered, never forced.
    val programmeChoice = when {
        ideal == null -> null
        !com.glowup.app.ui.data.ProGate.programmeLocked(ideal) -> ideal
        result.focus.any { it.area == FocusArea.BODY } -> "first30"
        else -> null
    }
    val chosen = remember { mutableStateMapOf<String, Boolean>().apply { plan.routines.forEach { put(it.title, true) } } }
    var takeProgramme by remember { mutableStateOf(programmeChoice != null) }
    val existing = RoutineStore.items.map { it.title.lowercase() }.toSet()

    Title("Your daily plan")
    Sub("About ${plan.dailyMinutes} minutes a day, focused on your top areas. Untick anything you don't want.")
    Spacer(Modifier.height(16.dp))
    plan.routines.forEach { item ->
        val already = item.title.lowercase() in existing
        val on = chosen[item.title] == true && !already
        Row(
            Modifier.fillMaxWidth().padding(bottom = 8.dp).clip(RoundedCornerShape(20.dp)).background(CardNeutral).clickable(enabled = !already) { chosen[item.title] = !(chosen[item.title] ?: false) }.padding(16.dp, 14.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Box(Modifier.size(24.dp).clip(CircleShape).background(if (on || already) Ink else Color.White), contentAlignment = Alignment.Center) {
                if (on || already) Icon(Icons.Filled.Check, contentDescription = null, tint = Color.White, modifier = Modifier.size(15.dp))
            }
            Spacer(Modifier.width(12.dp))
            Column(Modifier.weight(1f)) {
                Text(item.title, fontSize = 14.5.sp, fontWeight = FontWeight.SemiBold)
                Text(
                    item.area.title + (if (item.weekly) " · weekly" else " · daily") + if (already) " · already in your routines" else "",
                    color = TextSecondary, fontSize = 12.sp,
                )
            }
        }
    }
    programmeChoice?.let { id ->
        val programme = Programmes.get(id)!!
        Spacer(Modifier.height(8.dp))
        Row(
            Modifier.fillMaxWidth().clip(RoundedCornerShape(22.dp)).background(programme.focus.tint).clickable { takeProgramme = !takeProgramme }.padding(16.dp, 14.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Box(Modifier.size(24.dp).clip(CircleShape).background(if (takeProgramme) Ink else Color.White), contentAlignment = Alignment.Center) {
                if (takeProgramme) Icon(Icons.Filled.Check, contentDescription = null, tint = Color.White, modifier = Modifier.size(15.dp))
            }
            Spacer(Modifier.width(12.dp))
            Column(Modifier.weight(1f)) {
                Text("Start ${programme.title}", fontSize = 14.5.sp, fontWeight = FontWeight.SemiBold)
                Text(programme.tagline, color = TextSecondary, fontSize = 12.sp)
            }
        }
    }
    if (ideal != null && ideal != programmeChoice) {
        Text(
            "${Programmes.get(ideal)!!.title} fits your map best — it's part of Pro.",
            color = Purple, fontSize = 12.5.sp, fontWeight = FontWeight.SemiBold,
            modifier = Modifier.padding(top = 10.dp).clickable { com.glowup.app.ui.data.ProStore.showPaywall("Unlock ${Programmes.get(ideal)!!.title} and every programme") },
        )
    }
    Spacer(Modifier.height(20.dp))
    Box(
        Modifier.fillMaxWidth().height(56.dp).clip(RoundedCornerShape(999.dp)).background(Ink).clickable {
            plan.routines.filter { chosen[it.title] == true && it.title.lowercase() !in existing }.forEach {
                RoutineStore.add(it.category, it.title, if (it.weekly) "WEEKLY" else "DAILY")
            }
            val programmeId = programmeChoice
            if (takeProgramme && programmeId != null) {
                run {
                    if (!FitnessStore.setUp) {
                        FitnessStore.completeSetup(
                            goal = if (result.focus.any { it.area == FocusArea.BODY } && !minor) FitnessGoal.LOSE_FAT else FitnessGoal.GET_TONED,
                            level = FitnessLevel.BEGINNER,
                            days = setOf(DayOfWeek.MONDAY, DayOfWeek.WEDNESDAY, DayOfWeek.FRIDAY),
                            minutes = 10, reminder = null, heightCm = result.heightCm, useLb = false,
                            startWeightKg = result.weightKg, waistCm = result.waistCm, targetKg = null,
                        )
                    }
                    FitnessStore.startProgramme(programmeId)
                }
            }
            navController.navigate("glow_results") { popUpTo("glow_check") { inclusive = true } }
        },
        contentAlignment = Alignment.Center,
    ) { Text("Add to my day", color = Color.White, fontSize = 15.sp, fontWeight = FontWeight.SemiBold) }
    Text(
        "Not now", color = TextSecondary, fontSize = 14.sp, fontWeight = FontWeight.SemiBold,
        modifier = Modifier.align(Alignment.CenterHorizontally).padding(top = 12.dp).clickable {
            navController.navigate("glow_results") { popUpTo("glow_check") { inclusive = true } }
        },
    )
}
