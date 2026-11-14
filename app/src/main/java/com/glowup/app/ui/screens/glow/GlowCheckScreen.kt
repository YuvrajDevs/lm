package com.glowup.app.ui.screens.glow

import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.selection.toggleable
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.TrendingUp
import androidx.compose.material.icons.filled.Accessibility
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Face
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.MonitorWeight
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.material.icons.outlined.PhoneAndroid
import androidx.compose.material.icons.outlined.Quiz
import androidx.compose.material.icons.outlined.Straighten
import androidx.compose.material.icons.outlined.Tune
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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import com.glowup.app.BuildConfig
import com.glowup.app.R
import com.glowup.app.ui.components.ConsentSheet
import com.glowup.app.ui.components.goBack
import com.glowup.app.ui.data.Consent
import com.glowup.app.ui.data.ConsentStore
import com.glowup.app.ui.data.GroomingStore
import com.glowup.app.ui.data.ProGate
import com.glowup.app.ui.data.ProStore
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
import com.glowup.app.ui.design.AppButton
import com.glowup.app.ui.design.ButtonVariant
import com.glowup.app.ui.design.LineHeight
import com.glowup.app.ui.design.Overline
import com.glowup.app.ui.design.ScreenSubtitle
import com.glowup.app.ui.design.ScreenTitle
import com.glowup.app.ui.design.Shapes
import com.glowup.app.ui.design.Space
import com.glowup.app.ui.design.TextSize
import com.glowup.app.ui.screens.fitness.ChoicePill
import com.glowup.app.ui.screens.fitness.MeasureRow
import com.glowup.app.ui.theme.*
import java.time.DayOfWeek

private enum class Step { INTRO, GOALS, FACE, POSTURE, BODY, QUESTIONS, RESULTS, PLAN }

@OptIn(ExperimentalLayoutApi::class)
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
                if (includeBody) height else null, if (includeBody) weight else null, if (includeBody) waist else null
            )
            GlowCheckStore.save(r)
            result = r
        }
    }
    fun back() {
        val i = steps.indexOf(step)
        if (i == 0 || step == Step.RESULTS) {
            navController.goBack()
        } else {
            scanning = false
            step = steps[i - 1]
        }
    }

    Column(Modifier.fillMaxSize().background(Surface).systemBarsPadding()) {
        Row(Modifier.fillMaxWidth().padding(Space.L, Space.M, Space.Gutter, 0.dp), verticalAlignment = Alignment.CenterVertically) {
            Box(Modifier.size(42.dp).clip(CircleShape).clickable { navController.goBack() }, contentAlignment = Alignment.Center) {
                Icon(Icons.Filled.Close, contentDescription = stringResource(R.string.common_close), tint = Ink)
            }
            Spacer(Modifier.width(Space.S))
            val progressSteps = steps.filter { it != Step.INTRO }
            Row(Modifier.weight(1f), horizontalArrangement = Arrangement.spacedBy(Space.Xs)) {
                progressSteps.forEach { s ->
                    Box(Modifier.weight(1f).height(4.dp).clip(Shapes.Pill).background(if (steps.indexOf(s) <= steps.indexOf(step)) Ink else ChipNeutral))
                }
            }
        }

        Column(
            Modifier.weight(1f).verticalScroll(
                remember(step) {
                    ScrollState(0)
                },
                enabled = !scanning
            ).padding(Space.Gutter, Space.Xl, Space.Gutter, Space.L)
        ) {
            when (step) {
                Step.INTRO -> {
                    ScreenTitle(
                        if (previous ==
                            null
                        ) {
                            stringResource(R.string.glow_check_your_glow_check)
                        } else {
                            stringResource(R.string.glow_check_time_for_a_rescan)
                        }
                    )
                    ScreenSubtitle(
                        if (previous == null) {
                            stringResource(R.string.glow_check_three_minutes_to_find_what)
                        } else {
                            "We'll measure again and show you what's changed since ${previous.date.dayOfMonth} ${previous.date.month.name.lowercase().replaceFirstChar {
                                it.uppercase()
                            }}."
                        }
                    )
                    Spacer(Modifier.height(Space.Xl))
                    IntroRow(Icons.Outlined.Quiz, "A few quick questions", "About your goals, skin, sleep and habits")
                    IntroRow(Icons.Filled.Face, "Optional face scan", "Face shape and a symmetry baseline")
                    IntroRow(Icons.Filled.Accessibility, "Optional posture scan", "Side-on, to measure your neck angle")
                    IntroRow(Icons.Filled.Check, "Your map and plan", "Your top 3 areas, your strengths, and daily routines")
                    Spacer(Modifier.height(Space.L))
                    Column(Modifier.fillMaxWidth().clip(Shapes.Card).background(GreenTint).padding(Space.L)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Filled.Lock, contentDescription = null, tint = Green, modifier = Modifier.size(16.dp))
                            Spacer(Modifier.width(Space.S))
                            Text(stringResource(R.string.glow_check_our_promise), color = GreenDeep, fontSize = TextSize.Body, fontWeight = FontWeight.Bold)
                        }
                        Spacer(Modifier.height(Space.S))
                        listOf(
                            "No ratings out of 10 and no \"attractiveness score\" — ever.",
                            "No photo is taken. The camera is read live and only numbers are kept.",
                            "We only point at things you can actually change."
                        ).forEach { Text("• $it", color = GreenDeep, fontSize = TextSize.BodySmall, lineHeight = LineHeight.Compact) }
                    }
                }
                Step.GOALS -> {
                    ScreenTitle(stringResource(R.string.glow_check_what_do_you_want_to))
                    ScreenSubtitle(stringResource(R.string.glow_check_pick_as_many_as_you))
                    Spacer(Modifier.height(Space.L))
                    FlowRow(horizontalArrangement = Arrangement.spacedBy(Space.S), verticalArrangement = Arrangement.spacedBy(Space.S)) {
                        GlowQuestions.goals.filter { !(minor && it == "Body") || true }.forEach { g ->
                            ChoicePill(g, g in goals) { goals = if (g in goals) goals - g else goals + g }
                        }
                    }
                    Spacer(Modifier.height(Space.Gutter))
                    Text(stringResource(R.string.glow_check_time_you_can_give_each), fontSize = TextSize.BodyLarge, fontWeight = FontWeight.SemiBold)
                    Spacer(Modifier.height(Space.S))
                    Row(horizontalArrangement = Arrangement.spacedBy(Space.S)) {
                        GlowQuestions.minuteOptions.forEach { m ->
                            ChoicePill(stringResource(R.string.common_1_s_min, m), minutes == m, Modifier.weight(1f)) {
                                minutes =
                                    m
                            }
                        }
                    }
                    Text(
                        stringResource(R.string.glow_check_we_ll_keep_your_plan),
                        color = TextSecondary,
                        fontSize = TextSize.Caption,
                        modifier = Modifier.padding(top = Space.S)
                    )
                }
                Step.FACE -> {
                    ScreenTitle(stringResource(R.string.glow_check_face_scan))
                    ScreenSubtitle(stringResource(R.string.glow_check_optional_hold_your_phone_at))
                    Spacer(Modifier.height(Space.L))
                    when {
                        face != null -> FaceResultCard(face!!) {
                            face = null
                            scanning = true
                        }
                        scanning -> CameraScanner(
                            ScanMode.FACE,
                            onFace = {
                                face = it
                                manualShape = it.shape
                                scanning = false
                            },
                            onPermissionDenied = { scanning = false },
                            modifier = Modifier.fillMaxWidth().aspectRatio(0.78f)
                        )
                        else -> {
                            ScanStart("Start face scan", "Takes about 5 seconds") {
                                if (ConsentStore.has(Consent.FACE_SCAN)) scanning = true else askConsent = Consent.FACE_SCAN
                            }
                            if (BuildConfig.DEBUG) {
                                DebugLink("Developer: use a sample face") {
                                    face = FaceMetrics("Oval", 1.32, 0.9, 0.82, 91, 1.15)
                                    manualShape =
                                        "Oval"
                                }
                            }
                            Spacer(Modifier.height(Space.Xl))
                            Text(stringResource(R.string.glow_check_or_pick_your_face_shape), fontSize = TextSize.Body, fontWeight = FontWeight.SemiBold)
                            Spacer(Modifier.height(Space.S))
                            FlowRow(horizontalArrangement = Arrangement.spacedBy(Space.S), verticalArrangement = Arrangement.spacedBy(Space.S)) {
                                GroomingStore.faceShapes.forEach { s -> ChoicePill(s, manualShape == s) { manualShape = if (manualShape == s) null else s } }
                            }
                        }
                    }
                }
                Step.POSTURE -> {
                    ScreenTitle(stringResource(R.string.glow_check_posture_scan))
                    ScreenSubtitle(stringResource(R.string.glow_check_optional_prop_your_phone_up))
                    Spacer(Modifier.height(Space.L))
                    when {
                        neck != null -> PostureResultCard(neck!!) {
                            neck = null
                            scanning = true
                        }
                        scanning -> CameraScanner(
                            ScanMode.POSTURE,
                            onPosture = {
                                neck = it
                                scanning = false
                            },
                            onPermissionDenied = { scanning = false },
                            modifier = Modifier.fillMaxWidth().aspectRatio(0.62f)
                        )
                        else -> {
                            ScanStart("Start posture scan", "You'll hear a beep when it's done", Icons.Filled.Accessibility) {
                                if (ConsentStore.has(Consent.FACE_SCAN)) scanning = true else askConsent = Consent.FACE_SCAN
                            }
                            if (BuildConfig.DEBUG) DebugLink("Developer: use a sample posture") { neck = 66 }
                            Spacer(Modifier.height(Space.Gutter))
                            Overline(stringResource(R.string.glow_check_for_the_best_read))
                            Spacer(Modifier.height(Space.S))
                            IntroRow(Icons.Outlined.PhoneAndroid, "Phone at chest height", "Lean it on a shelf or a stack of books")
                            IntroRow(Icons.Outlined.Straighten, "About 2 m back", "Head, shoulders and hips all in frame")
                            IntroRow(Icons.Filled.Accessibility, "Stand as you normally do", "No need to straighten up — we want your everyday posture")
                        }
                    }
                }
                Step.BODY -> {
                    ScreenTitle(stringResource(R.string.glow_check_body))
                    ScreenSubtitle(stringResource(R.string.glow_check_optional_body_fat_is_the))
                    Spacer(Modifier.height(Space.L))
                    if (!includeBody) {
                        AppButton(stringResource(R.string.glow_check_add_height_weight_amp_waist), variant = ButtonVariant.Primary) {
                            if (ConsentStore.has(Consent.BODY)) includeBody = true else askConsent = Consent.BODY
                        }
                        Spacer(Modifier.height(Space.Gutter))
                        Overline(stringResource(R.string.glow_check_why_it_helps))
                        Spacer(Modifier.height(Space.S))
                        IntroRow(Icons.Outlined.Tune, "A sharper plan", "Ranks body composition fairly against your other areas")
                        IntroRow(Icons.AutoMirrored.Outlined.TrendingUp, "A trend, not a verdict", "Update it weekly in Fitness to see progress")
                        IntroRow(Icons.Outlined.Lock, "Stays on this phone", "Only the numbers you type — never a photo")
                    } else {
                        MeasureRow("Height", "$height", "cm", { height = (height - 1).coerceAtLeast(120) }, { height = (height + 1).coerceAtMost(230) })
                        MeasureRow("Weight", "%.1f".format(weight), "kg", { weight = (weight - 0.5).coerceAtLeast(30.0) }, { weight += 0.5 })
                        MeasureRow("Waist", waist?.let { "%.0f".format(it) } ?: "—", "cm", { waist = ((waist ?: 81.0) - 1).coerceAtLeast(40.0) }, {
                            waist =
                                (waist ?: 79.0) + 1
                        }, "At the belly button, relaxed")
                    }
                }
                Step.QUESTIONS -> {
                    ScreenTitle(stringResource(R.string.glow_check_quick_questions))
                    ScreenSubtitle(stringResource(R.string.glow_check_tap_the_closest_answer_skip))
                    Spacer(Modifier.height(Space.S))
                    GlowQuestions.all.forEach { q ->
                        Spacer(Modifier.height(Space.L))
                        Text(q.text, fontSize = TextSize.BodyLarge, fontWeight = FontWeight.SemiBold)
                        q.hint?.let { Text(it, color = TextSecondary, fontSize = TextSize.Caption, modifier = Modifier.padding(top = 2.dp)) }
                        Spacer(Modifier.height(Space.S))
                        FlowRow(horizontalArrangement = Arrangement.spacedBy(Space.S), verticalArrangement = Arrangement.spacedBy(Space.S)) {
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
            Column(Modifier.padding(Space.Gutter, Space.S, Space.Gutter, Space.Gutter)) {
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
                AppButton(label, variant = if (secondary) ButtonVariant.Secondary else ButtonVariant.Primary) { next() }
                if (step != Step.INTRO && step != Step.RESULTS) {
                    Text(
                        stringResource(R.string.common_back),
                        color = TextSecondary,
                        fontSize = TextSize.Body,
                        fontWeight = FontWeight.SemiBold,
                        modifier = Modifier.align(Alignment.CenterHorizontally).padding(top = Space.M).clickable {
                            back()
                        }
                    )
                }
            }
        } else if (scanning) {
            Text(
                stringResource(R.string.glow_check_cancel_scan),
                color = TextSecondary,
                fontSize = TextSize.Body,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier.align(Alignment.CenterHorizontally).padding(Space.L, Space.S, Space.L, Space.Gutter).clickable { scanning = false }
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
            onDecline = { askConsent = null }
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
private fun IntroRow(icon: ImageVector, title: String, body: String) {
    Row(Modifier.fillMaxWidth().padding(vertical = Space.S), verticalAlignment = Alignment.CenterVertically) {
        Box(Modifier.size(44.dp).clip(CircleShape).background(CardNeutral), contentAlignment = Alignment.Center) {
            Icon(icon, contentDescription = null, tint = Ink, modifier = Modifier.size(20.dp))
        }
        Spacer(Modifier.width(Space.M))
        Column {
            Text(title, fontSize = TextSize.BodyLarge, fontWeight = FontWeight.SemiBold)
            Text(body, color = TextSecondary, fontSize = TextSize.BodySmall)
        }
    }
}

@Composable
private fun ScanStart(title: String, sub: String, icon: ImageVector = Icons.Filled.Face, onClick: () -> Unit) {
    Column(
        Modifier.fillMaxWidth().clip(Shapes.Large).background(Ink).clickable(onClick = onClick).padding(Space.Xl),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(Modifier.size(56.dp).clip(CircleShape).background(Color.White.copy(alpha = 0.12f)), contentAlignment = Alignment.Center) {
            Icon(icon, contentDescription = null, tint = Color.White)
        }
        Spacer(Modifier.height(Space.S))
        Text(title, color = Color.White, fontSize = TextSize.Title, fontWeight = FontWeight.Bold)
        Text(sub, color = Color.White.copy(alpha = 0.6f), fontSize = TextSize.BodySmall)
        Spacer(Modifier.height(Space.S))
        Text(stringResource(R.string.glow_check_no_photo_is_taken_or), color = Mint, fontSize = TextSize.Caption, fontWeight = FontWeight.SemiBold)
    }
}

@Composable
private fun DebugLink(text: String, onClick: () -> Unit) {
    Text(
        text,
        color = Purple,
        fontSize = TextSize.BodySmall,
        fontWeight = FontWeight.SemiBold,
        modifier = Modifier.padding(top = Space.S).clickable(onClick = onClick)
    )
}

@Composable
private fun FaceResultCard(face: FaceMetrics, onRetake: () -> Unit) {
    Column(Modifier.fillMaxWidth().clip(Shapes.Large).background(PurpleTint).padding(Space.Xl)) {
        Text(stringResource(R.string.glow_check_measured), color = Purple, fontSize = TextSize.Micro, fontWeight = FontWeight.Bold, letterSpacing = 0.4.sp)
        Text(stringResource(R.string.common_1_s_face, face.shape), fontSize = TextSize.Headline, fontWeight = FontWeight.Bold)
        Text(stringResource(R.string.glow_check_symmetry_1_s_100_most, face.symmetry), color = PurpleDeep, fontSize = TextSize.BodySmall)
        Spacer(Modifier.height(Space.M))
        Text(
            stringResource(R.string.glow_check_retake),
            fontSize = TextSize.BodySmall,
            fontWeight = FontWeight.SemiBold,
            modifier = Modifier.clip(Shapes.Pill).background(Color.White).clickable(onClick = onRetake).padding(Space.L, Space.S)
        )
    }
}

@Composable
private fun PostureResultCard(angle: Int, onRetake: () -> Unit) {
    val level = PostureAnalysis.level(angle)
    Column(Modifier.fillMaxWidth().clip(Shapes.Large).background(BlueTint).padding(Space.Xl)) {
        Text(stringResource(R.string.glow_check_measured), color = Navy, fontSize = TextSize.Micro, fontWeight = FontWeight.Bold, letterSpacing = 0.4.sp)
        Text("$angle° · ${level.label}", fontSize = TextSize.Headline, fontWeight = FontWeight.Bold)
        Text(stringResource(R.string.glow_check_90_means_your_ear_is), color = Navy, fontSize = TextSize.BodySmall, lineHeight = LineHeight.Compact)
        Spacer(Modifier.height(Space.M))
        Text(
            stringResource(R.string.glow_check_retake),
            fontSize = TextSize.BodySmall,
            fontWeight = FontWeight.SemiBold,
            modifier = Modifier.clip(Shapes.Pill).background(Color.White).clickable(onClick = onRetake).padding(Space.L, Space.S)
        )
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
        !ProGate.programmeLocked(ideal) -> ideal
        result.focus.any { it.area == FocusArea.BODY } -> "first30"
        else -> null
    }
    val chosen = remember { mutableStateMapOf<String, Boolean>().apply { plan.routines.forEach { put(it.title, true) } } }
    var takeProgramme by remember { mutableStateOf(programmeChoice != null) }
    val existing = RoutineStore.items.map { it.title.lowercase() }.toSet()

    ScreenTitle(stringResource(R.string.glow_check_your_daily_plan))
    ScreenSubtitle(stringResource(R.string.glow_check_about_1_s_minutes_a, plan.dailyMinutes))
    Spacer(Modifier.height(Space.L))
    plan.routines.forEach { item ->
        val already = item.title.lowercase() in existing
        val on = chosen[item.title] == true && !already
        Row(
            Modifier.fillMaxWidth().padding(
                bottom = Space.S
            ).clip(Shapes.Card).background(CardNeutral).toggleable(value = on || already, enabled = !already, role = Role.Checkbox) {
                chosen[item.title] =
                    it
            }.padding(Space.L, Space.M),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(Modifier.size(24.dp).clip(CircleShape).background(if (on || already) Ink else Color.White), contentAlignment = Alignment.Center) {
                if (on || already) Icon(Icons.Filled.Check, contentDescription = null, tint = Color.White, modifier = Modifier.size(15.dp))
            }
            Spacer(Modifier.width(Space.M))
            Column(Modifier.weight(1f)) {
                Text(item.title, fontSize = TextSize.BodyLarge, fontWeight = FontWeight.SemiBold)
                Text(
                    item.area.title + (if (item.weekly) stringResource(R.string.glow_check_weekly) else stringResource(R.string.glow_check_daily)) +
                        if (already) stringResource(R.string.glow_check_already_in_your_routines) else "",
                    color = TextSecondary,
                    fontSize = TextSize.Caption
                )
            }
        }
    }
    programmeChoice?.let { id ->
        val programme = Programmes.get(id)!!
        Spacer(Modifier.height(Space.S))
        Row(
            Modifier.fillMaxWidth().clip(Shapes.Card).background(programme.focus.tint).toggleable(value = takeProgramme, role = Role.Checkbox) {
                takeProgramme =
                    it
            }.padding(Space.L, Space.M),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(Modifier.size(24.dp).clip(CircleShape).background(if (takeProgramme) Ink else Color.White), contentAlignment = Alignment.Center) {
                if (takeProgramme) Icon(Icons.Filled.Check, contentDescription = null, tint = Color.White, modifier = Modifier.size(15.dp))
            }
            Spacer(Modifier.width(Space.M))
            Column(Modifier.weight(1f)) {
                Text(stringResource(R.string.common_start_1_s, programme.title), fontSize = TextSize.BodyLarge, fontWeight = FontWeight.SemiBold)
                Text(programme.tagline, color = TextSecondary, fontSize = TextSize.Caption)
            }
        }
    }
    if (ideal != null && ideal != programmeChoice) {
        Text(
            stringResource(R.string.glow_check_1_s_fits_your_map, Programmes.get(ideal)!!.title),
            color = Purple,
            fontSize = TextSize.BodySmall,
            fontWeight = FontWeight.SemiBold,
            modifier = Modifier.padding(top = Space.S).clickable { ProStore.showPaywall("Unlock ${Programmes.get(ideal)!!.title} and every programme") }
        )
    }
    Spacer(Modifier.height(Space.Xl))
    AppButton(stringResource(R.string.glow_check_add_to_my_day), variant = ButtonVariant.Primary) {
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
                        startWeightKg = result.weightKg, waistCm = result.waistCm, targetKg = null
                    )
                }
                FitnessStore.startProgramme(programmeId)
            }
        }
        navController.navigate("glow_results") { popUpTo("glow_check") { inclusive = true } }
    }
    Text(
        stringResource(R.string.common_not_now),
        color = TextSecondary,
        fontSize = TextSize.Body,
        fontWeight = FontWeight.SemiBold,
        modifier = Modifier.align(Alignment.CenterHorizontally).padding(top = Space.M).clickable {
            navController.navigate("glow_results") { popUpTo("glow_check") { inclusive = true } }
        }
    )
}
