package com.glowup.app.ui.screens.fitness

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.MonitorWeight
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.glowup.app.R
import com.glowup.app.ui.components.ConsentSheet
import com.glowup.app.ui.components.ReMakeUDialog
import com.glowup.app.ui.components.ReMakeUOptionRow
import com.glowup.app.ui.components.goBack
import com.glowup.app.ui.components.rememberNotificationPermission
import com.glowup.app.ui.data.Consent
import com.glowup.app.ui.data.ConsentStore
import com.glowup.app.ui.data.ProGate
import com.glowup.app.ui.data.ProStore
import com.glowup.app.ui.data.ProfileStore
import com.glowup.app.ui.data.fitness.BodyEntry
import com.glowup.app.ui.data.fitness.FitnessGoal
import com.glowup.app.ui.data.fitness.FitnessLevel
import com.glowup.app.ui.data.fitness.FitnessStore
import com.glowup.app.ui.data.fitness.Programmes
import com.glowup.app.ui.data.fitness.QuickSessions
import com.glowup.app.ui.data.formatClock
import com.glowup.app.ui.design.AppButton
import com.glowup.app.ui.design.ButtonVariant
import com.glowup.app.ui.design.LineHeight
import com.glowup.app.ui.design.ScreenSubtitle
import com.glowup.app.ui.design.ScreenTitle
import com.glowup.app.ui.design.Shapes
import com.glowup.app.ui.design.Space
import com.glowup.app.ui.design.TextSize
import com.glowup.app.ui.theme.*
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.LocalTime
import java.time.format.TextStyle
import java.util.Locale
import kotlin.math.roundToInt

private val reminderOptions = listOf(
    LocalTime.of(6, 30), LocalTime.of(7, 0), LocalTime.of(7, 30), LocalTime.of(8, 0), LocalTime.of(12, 30),
    LocalTime.of(17, 30), LocalTime.of(18, 0), LocalTime.of(19, 0), LocalTime.of(20, 0), LocalTime.of(21, 0)
)

@Composable
fun FitnessSetupScreen(navController: NavHostController) {
    val editing = FitnessStore.setUp
    val minor = ProfileStore.isMinor
    var step by remember { mutableStateOf(0) }
    var goal by remember { mutableStateOf(FitnessStore.goal) }
    var testMode by remember { mutableStateOf(!editing) }
    var pushups by remember { mutableStateOf(5) }
    var plank by remember { mutableStateOf(30) }
    var squats by remember { mutableStateOf(20) }
    var level by remember { mutableStateOf(FitnessStore.level) }
    var days by remember { mutableStateOf(FitnessStore.days) }
    var minutes by remember { mutableStateOf(FitnessStore.minutes) }
    var reminder by remember { mutableStateOf(FitnessStore.reminder ?: if (editing) null else LocalTime.of(18, 0)) }
    var showReminder by remember { mutableStateOf(false) }
    var addBody by remember { mutableStateOf(FitnessStore.latestBody() != null) }
    var askBodyConsent by remember { mutableStateOf(false) }
    var lb by remember { mutableStateOf(FitnessStore.useLb) }
    var height by remember { mutableStateOf(FitnessStore.heightCm ?: 170) }
    var weight by remember { mutableStateOf(FitnessStore.latestBody()?.weightKg ?: 70.0) }
    var waist by remember { mutableStateOf<Double?>(FitnessStore.latestBody()?.waistCm) }
    var target by remember { mutableStateOf(FitnessStore.targetKg) }
    val askNotifications = rememberNotificationPermission()

    val lastStep = 4
    val chosenLevel = if (testMode) FitnessStore.levelFromTest(pushups, plank, squats) else level
    val recommended = when {
        chosenLevel == FitnessLevel.BEGINNER -> "first30"
        goal == FitnessGoal.LOSE_FAT -> "fatburn"
        goal == FitnessGoal.BUILD_MUSCLE -> "lean"
        goal == FitnessGoal.GET_TONED -> "abs30"
        else -> "first30"
    }
    val recommendedFree = if (ProGate.programmeLocked(recommended)) "first30" else recommended

    fun finish(startProgramme: String?) {
        FitnessStore.completeSetup(
            goal = goal ?: FitnessGoal.CONSISTENT,
            level = chosenLevel,
            days = days,
            minutes = minutes,
            reminder = reminder,
            heightCm = if (addBody) height else FitnessStore.heightCm,
            useLb = lb,
            startWeightKg = if (addBody && !editing) weight else null,
            waistCm = if (addBody) waist else null,
            targetKg = if (addBody && !minor) target else null
        )
        if (editing && addBody && FitnessStore.latestBody() == null) {
            FitnessStore.logBody(BodyEntry(LocalDate.now(), weight, waist))
        }
        startProgramme?.let(FitnessStore::startProgramme)
        if (reminder != null) askNotifications()
        navController.goBack()
    }

    val canContinue = when (step) {
        0 -> goal != null
        2 -> days.isNotEmpty()
        else -> true
    }

    Column(Modifier.fillMaxSize().background(Surface).systemBarsPadding()) {
        Row(Modifier.fillMaxWidth().padding(Space.Gutter, Space.Gutter, Space.Gutter, 0.dp), horizontalArrangement = Arrangement.spacedBy(Space.S)) {
            repeat(lastStep + 1) { i ->
                Box(Modifier.weight(1f).height(4.dp).clip(Shapes.Pill).background(if (i <= step) Ink else ChipNeutral))
            }
        }

        Column(Modifier.weight(1f).verticalScroll(rememberScrollState()).padding(Space.Gutter, Space.Gutter, Space.Gutter, Space.L)) {
            when (step) {
                0 -> {
                    ScreenTitle(stringResource(R.string.fitness_setup_what_s_your_main_goal))
                    ScreenSubtitle(stringResource(R.string.fitness_setup_we_ll_shape_your_sessions))
                    Spacer(Modifier.height(Space.L))
                    FitnessGoal.entries.filter { !minor || it.minorSafe }.forEach { g ->
                        BigChoice(if (minor) g.minorLabel else g.label, g.blurb.takeIf { !minor || g != FitnessGoal.LOSE_FAT } ?: "", goal == g) { goal = g }
                    }
                }
                1 -> {
                    ScreenTitle(stringResource(R.string.fitness_setup_where_are_you_starting))
                    ScreenSubtitle(stringResource(R.string.fitness_setup_be_honest_we_d_rather))
                    Spacer(Modifier.height(Space.L))
                    Row(Modifier.fillMaxWidth().clip(Shapes.Pill).background(ChipNeutral).padding(Space.Xs)) {
                        listOf(true to "Quick test", false to "I'll choose").forEach { (mode, label) ->
                            Box(
                                Modifier.weight(1f).clip(Shapes.Pill).background(
                                    if (testMode ==
                                        mode
                                    ) {
                                        Ink
                                    } else {
                                        Color.Transparent
                                    }
                                ).clickable { testMode = mode }.padding(vertical = Space.S),
                                contentAlignment = Alignment.Center
                            ) { Text(label, color = if (testMode == mode) Color.White else Ink, fontSize = TextSize.Body, fontWeight = FontWeight.SemiBold) }
                        }
                    }
                    Spacer(Modifier.height(Space.L))
                    if (testMode) {
                        Text(stringResource(R.string.fitness_setup_do_each_one_now_or), color = TextSecondary, fontSize = TextSize.BodySmall)
                        Spacer(Modifier.height(Space.M))
                        TestRow("Push-ups in a row", "Knee push-ups count as half", "$pushups", "reps", {
                            pushups = (pushups - 1).coerceAtLeast(0)
                        }, { pushups++ })
                        TestRow("Plank hold", "Forearm plank, good form", "$plank", "seconds", { plank = (plank - 5).coerceAtLeast(0) }, { plank += 5 })
                        TestRow("Squats in 60 seconds", "Thighs to about parallel", "$squats", "reps", { squats = (squats - 1).coerceAtLeast(0) }, { squats++ })
                        Spacer(Modifier.height(Space.S))
                        Row(
                            Modifier.fillMaxWidth().clip(Shapes.Medium).background(GreenTint).padding(Space.L, Space.M),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(stringResource(R.string.fitness_setup_your_level), fontSize = TextSize.Body)
                            Text(chosenLevel.label, fontSize = TextSize.Body, fontWeight = FontWeight.Bold, color = Green)
                        }
                    } else {
                        FitnessLevel.entries.forEach { l ->
                            BigChoice(
                                l.label,
                                when (l) {
                                    FitnessLevel.BEGINNER -> "New to training or coming back after a long break"
                                    FitnessLevel.INTERMEDIATE -> "You can do around 10 push-ups and hold a 45-second plank"
                                    FitnessLevel.ADVANCED -> "20+ push-ups, 90-second plank — you train regularly"
                                },
                                level == l
                            ) { level = l }
                        }
                    }
                }
                2 -> {
                    ScreenTitle(stringResource(R.string.fitness_setup_when_will_you_train))
                    ScreenSubtitle(stringResource(R.string.fitness_setup_consistency_beats_intensity_most_people))
                    Spacer(Modifier.height(Space.L))
                    Text(stringResource(R.string.fitness_setup_days), fontSize = TextSize.Body, fontWeight = FontWeight.SemiBold)
                    Spacer(Modifier.height(Space.S))
                    Row(horizontalArrangement = Arrangement.spacedBy(Space.S)) {
                        DayOfWeek.entries.forEach { d ->
                            val on = d in days
                            Box(
                                Modifier.weight(1f).aspectRatio(1f).clip(CircleShape).background(if (on) Ink else ChipNeutral).clickable {
                                    days =
                                        if (on) days - d else days + d
                                },
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    d.getDisplayName(TextStyle.SHORT, Locale.getDefault()).take(2),
                                    color = if (on) Color.White else Ink,
                                    fontSize = TextSize.BodySmall,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }
                        }
                    }
                    Text(
                        stringResource(R.string.fitness_setup_1_s_days_a_week, days.size),
                        color = TextSecondary,
                        fontSize = TextSize.Caption,
                        modifier = Modifier.padding(top = Space.S)
                    )
                    Spacer(Modifier.height(Space.Xl))
                    Text(stringResource(R.string.fitness_setup_session_length_minutes), fontSize = TextSize.Body, fontWeight = FontWeight.SemiBold)
                    Spacer(Modifier.height(Space.S))
                    Row(horizontalArrangement = Arrangement.spacedBy(Space.S)) {
                        QuickSessions.minuteOptions.forEach { m ->
                            val locked = ProGate.quickLocked(m)
                            ChoicePill("$m", minutes == m, Modifier.weight(1f)) {
                                if (locked) ProStore.showPaywall("Sessions longer than 10 minutes are part of Pro") else minutes = m
                            }
                        }
                    }
                    if (!ProStore.isPro) {
                        Text(
                            stringResource(R.string.fitness_setup_15_30_minute_sessions_come),
                            color = TextSecondary,
                            fontSize = TextSize.Caption,
                            modifier = Modifier.padding(top = Space.S)
                        )
                    }
                    Spacer(Modifier.height(Space.Xl))
                    Text(stringResource(R.string.common_reminder), fontSize = TextSize.Body, fontWeight = FontWeight.SemiBold)
                    Spacer(Modifier.height(Space.S))
                    Row(
                        Modifier.fillMaxWidth().clip(Shapes.Medium).background(CardNeutral).clickable { showReminder = true }.padding(Space.L, Space.M),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            reminder?.let {
                                "On training days at ${formatClock(it)}"
                            } ?: stringResource(R.string.common_setup_off),
                            fontSize = TextSize.Body,
                            modifier = Modifier.weight(1f)
                        )
                        Text(stringResource(R.string.common_setup_change), fontSize = TextSize.BodySmall, fontWeight = FontWeight.SemiBold, color = Purple)
                    }
                }
                3 -> {
                    ScreenTitle(stringResource(R.string.fitness_setup_track_your_progress))
                    ScreenSubtitle(stringResource(R.string.fitness_setup_optional_a_weekly_weigh_in))
                    Spacer(Modifier.height(Space.L))
                    if (!addBody) {
                        Column(Modifier.fillMaxWidth().clip(Shapes.Card).background(PurpleTint).padding(Space.L)) {
                            Icon(Icons.Filled.MonitorWeight, contentDescription = null, tint = Purple)
                            Spacer(Modifier.height(Space.S))
                            Text(
                                if (minor) {
                                    stringResource(
                                        R.string.fitness_setup_height_and_weight_help_us
                                    )
                                } else {
                                    stringResource(R.string.fitness_setup_height_weight_and_waist_give)
                                },
                                fontSize = TextSize.Body,
                                lineHeight = LineHeight.Body
                            )
                            Spacer(Modifier.height(Space.M))
                            AppButton(stringResource(R.string.fitness_setup_add_my_measurements), variant = ButtonVariant.Primary) {
                                if (ConsentStore.has(Consent.BODY)) addBody = true else askBodyConsent = true
                            }
                        }
                    } else {
                        Row(Modifier.fillMaxWidth().clip(Shapes.Pill).background(ChipNeutral).padding(Space.Xs)) {
                            listOf(false to "kg", true to "lb").forEach { (isLb, label) ->
                                Box(
                                    Modifier.weight(1f).clip(Shapes.Pill).background(
                                        if (lb ==
                                            isLb
                                        ) {
                                            Ink
                                        } else {
                                            Color.Transparent
                                        }
                                    ).clickable { lb = isLb }.padding(vertical = Space.S),
                                    contentAlignment = Alignment.Center
                                ) { Text(label, color = if (lb == isLb) Color.White else Ink, fontSize = TextSize.BodySmall, fontWeight = FontWeight.SemiBold) }
                            }
                        }
                        Spacer(Modifier.height(Space.M))
                        MeasureRow("Height", "$height", "cm", { height = (height - 1).coerceAtLeast(120) }, { height = (height + 1).coerceAtMost(230) })
                        val shown = if (lb) weight * 2.20462 else weight
                        MeasureRow("Weight", "%.1f".format(shown), if (lb) "lb" else "kg", {
                            weight = (weight - if (lb) 0.4536 else 0.5).coerceAtLeast(30.0)
                        }, {
                            weight +=
                                if (lb) 0.4536 else 0.5
                        })
                        MeasureRow(
                            "Waist (optional)",
                            waist?.let { "%.0f".format(it) } ?: "—",
                            "cm",
                            { waist = ((waist ?: 80.0) - 1).coerceAtLeast(40.0) },
                            { waist = (waist ?: 79.0) + 1 },
                            hint = "Measure at the belly button"
                        )
                        if (!minor && goal != FitnessGoal.CONSISTENT) {
                            val t = target
                            MeasureRow(
                                "Target weight (optional)",
                                t?.let { "%.1f".format(if (lb) it * 2.20462 else it) } ?: "—",
                                if (lb) "lb" else "kg",
                                {
                                    target = ((t ?: weight) - if (lb) 0.4536 else 0.5).coerceAtLeast(35.0)
                                },
                                { target = (t ?: weight) + if (lb) 0.4536 else 0.5 }
                            )
                            if (t != null && goal == FitnessGoal.LOSE_FAT && weight > t) {
                                val weeksMin = ((weight - t) / (weight * 0.01)).roundToInt()
                                Text(
                                    stringResource(R.string.fitness_setup_at_a_safe_pace_up, weeksMin),
                                    color = TextSecondary,
                                    fontSize = TextSize.Caption,
                                    lineHeight = LineHeight.Compact,
                                    modifier = Modifier.padding(top = 2.dp)
                                )
                            }
                        }
                    }
                }
                else -> {
                    ScreenTitle(if (editing) stringResource(R.string.fitness_setup_plan_updated) else stringResource(R.string.fitness_setup_your_plan_is_ready))
                    Spacer(Modifier.height(Space.M))
                    Column(Modifier.fillMaxWidth().clip(Shapes.Large).background(Ink).padding(Space.Xl)) {
                        listOf(
                            "Goal" to (goal?.let { if (minor) it.minorLabel else it.label } ?: "—"),
                            "Level" to chosenLevel.label,
                            "Days" to days.sorted().joinToString(" ") { it.getDisplayName(TextStyle.SHORT, Locale.getDefault()) },
                            "Sessions" to "$minutes min",
                            "Reminder" to (reminder?.let(::formatClock) ?: "Off")
                        ).forEach { (k, v) ->
                            Row(Modifier.fillMaxWidth().padding(vertical = Space.Xs)) {
                                Text(k, color = Color.White.copy(alpha = 0.55f), fontSize = TextSize.BodySmall, modifier = Modifier.width(90.dp))
                                Text(v, color = Color.White, fontSize = TextSize.Body, fontWeight = FontWeight.SemiBold)
                            }
                        }
                    }
                    if (!editing) {
                        Spacer(Modifier.height(Space.L))
                        val programme = Programmes.get(recommendedFree)!!
                        Text(stringResource(R.string.fitness_setup_we_recommend), fontSize = TextSize.Body, fontWeight = FontWeight.SemiBold)
                        Spacer(Modifier.height(Space.S))
                        Column(Modifier.fillMaxWidth().clip(Shapes.Card).background(programme.focus.tint).padding(Space.L)) {
                            Text(programme.title, fontSize = TextSize.Title, fontWeight = FontWeight.Bold)
                            Text(
                                programme.about,
                                color = TextSecondary,
                                fontSize = TextSize.BodySmall,
                                lineHeight = LineHeight.Compact,
                                modifier = Modifier.padding(top = Space.Xs)
                            )
                        }
                        if (recommended != recommendedFree) {
                            Text(
                                stringResource(R.string.fitness_setup_1_s_fits_your_goal, Programmes.get(recommended)!!.title),
                                color = TextSecondary,
                                fontSize = TextSize.Caption,
                                modifier = Modifier.padding(top = Space.S).clickable {
                                    ProStore.showPaywall("Unlock every 30-day programme")
                                }
                            )
                        }
                    }
                }
            }
        }

        Column(Modifier.padding(Space.Gutter, Space.S, Space.Gutter, Space.Gutter)) {
            AppButton(
                when {
                    step == 3 && !addBody -> stringResource(R.string.fitness_setup_skip_for_now)
                    step < lastStep -> stringResource(R.string.common_setup_continue)
                    editing -> stringResource(R.string.fitness_setup_save_plan)
                    else -> stringResource(R.string.common_start_1_s, Programmes.get(recommendedFree)!!.title)
                },
                variant = ButtonVariant.Primary,
                enabled = canContinue
            ) {
                if (step <
                    lastStep
                ) {
                    step++
                } else {
                    finish(if (editing) null else recommendedFree)
                }
            }
            Text(
                if (step == lastStep &&
                    !editing
                ) {
                    stringResource(R.string.fitness_setup_just_do_quick_sessions)
                } else if (step ==
                    0
                ) {
                    stringResource(R.string.common_cancel)
                } else {
                    stringResource(R.string.common_back)
                },
                color = TextSecondary,
                fontSize = TextSize.Body,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier.align(Alignment.CenterHorizontally).padding(top = Space.M).clickable {
                    when {
                        step == lastStep && !editing -> finish(null)
                        step == 0 -> navController.goBack()
                        else -> step--
                    }
                }
            )
        }
    }

    if (showReminder) {
        ReMakeUDialog(
            onDismissRequest = { showReminder = false },
            title = "Workout reminder",
            primaryText = "Done",
            secondaryText = null,
            content = {
                Column(Modifier.heightIn(max = 380.dp).verticalScroll(rememberScrollState())) {
                    ReMakeUOptionRow("Off", reminder == null) { reminder = null }
                    reminderOptions.forEach { t -> ReMakeUOptionRow(formatClock(t), reminder == t) { reminder = t } }
                }
            }
        )
    }
    if (askBodyConsent) {
        ConsentSheet(Consent.BODY, Icons.Filled.MonitorWeight, onAllow = {
            askBodyConsent = false
            addBody = true
        }, onDecline = { askBodyConsent = false })
    }
}

@Composable
private fun BigChoice(title: String, body: String, selected: Boolean, onClick: () -> Unit) {
    Row(
        Modifier.fillMaxWidth().padding(
            bottom = Space.S
        ).clip(
            Shapes.Card
        ).background(if (selected) Ink else CardNeutral).selectable(selected = selected, role = Role.RadioButton, onClick = onClick).padding(Space.L, Space.L),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(Modifier.weight(1f)) {
            Text(title, color = if (selected) Color.White else Ink, fontSize = TextSize.BodyLarge, fontWeight = FontWeight.Bold)
            if (body.isNotBlank()) {
                Text(
                    body,
                    color = if (selected) Color.White.copy(alpha = 0.65f) else TextSecondary,
                    fontSize = TextSize.BodySmall,
                    lineHeight = LineHeight.Compact
                )
            }
        }
        if (selected) Icon(Icons.Filled.Check, contentDescription = null, tint = Color.White)
    }
}

@Composable
private fun TestRow(title: String, hint: String, value: String, unit: String, onMinus: () -> Unit, onPlus: () -> Unit) {
    Row(
        Modifier.fillMaxWidth().padding(bottom = Space.S).clip(Shapes.Card).background(CardNeutral).padding(Space.L, Space.M),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(Modifier.weight(1f)) {
            Text(title, fontSize = TextSize.BodyLarge, fontWeight = FontWeight.SemiBold)
            Text(hint, color = TextSecondary, fontSize = TextSize.Caption)
        }
        NumberStepper(value, onMinus, onPlus, unit = unit)
    }
}

@Composable
internal fun MeasureRow(title: String, value: String, unit: String, onMinus: () -> Unit, onPlus: () -> Unit, hint: String? = null) {
    Row(
        Modifier.fillMaxWidth().padding(bottom = Space.S).clip(Shapes.Card).background(CardNeutral).padding(Space.L, Space.M),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(Modifier.weight(1f)) {
            Text(title, fontSize = TextSize.Body, fontWeight = FontWeight.SemiBold)
            hint?.let { Text(it, color = TextSecondary, fontSize = TextSize.Caption) }
        }
        NumberStepper(value, onMinus, onPlus, unit = unit)
    }
}
