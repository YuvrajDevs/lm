package com.glowup.app.ui.screens.focus

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
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
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.glowup.app.R
import com.glowup.app.blocker.Blocker
import com.glowup.app.ui.components.DetailHeader
import com.glowup.app.ui.components.ProBadge
import com.glowup.app.ui.components.ReMakeUDialog
import com.glowup.app.ui.components.goBack
import com.glowup.app.ui.components.rememberNotificationPermission
import com.glowup.app.ui.data.BlockStore
import com.glowup.app.ui.data.FocusStore
import com.glowup.app.ui.data.ProStore
import com.glowup.app.ui.design.Shapes
import com.glowup.app.ui.design.Space
import com.glowup.app.ui.design.TextSize
import com.glowup.app.ui.theme.*
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.format.TextStyle
import java.util.Locale
import kotlinx.coroutines.delay

@Composable
fun FocusScreen(navController: NavHostController) {
    var label by remember { mutableStateOf(FocusStore.lastLabel) }
    var minutes by remember { mutableStateOf(FocusStore.lastMinutes) }
    var now by remember { mutableLongStateOf(System.currentTimeMillis()) }
    var confirmEnd by remember { mutableStateOf(false) }
    var blockApps by remember { mutableStateOf(BlockStore.focusApps.isNotEmpty()) }
    var strict by remember { mutableStateOf(false) }
    var emergency by remember { mutableStateOf(false) }
    val context = LocalContext.current
    val active = FocusStore.active
    val askPermission = rememberNotificationPermission()

    LaunchedEffect(active) {
        while (FocusStore.active != null) {
            now = System.currentTimeMillis()
            FocusStore.finishIfDue(now)
            delay(500)
        }
    }

    Box(modifier = Modifier.fillMaxSize().background(Surface).systemBarsPadding()) {
        Column(modifier = Modifier.fillMaxSize()) {
            DetailHeader(stringResource(R.string.focus_focus)) { navController.goBack() }

            Column(
                modifier = Modifier.weight(1f).verticalScroll(rememberScrollState()).padding(top = Space.M),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                if (active == null) {
                    LazyRow(contentPadding = PaddingValues(horizontal = Space.Gutter), horizontalArrangement = Arrangement.spacedBy(Space.S)) {
                        items(FocusStore.labels) { option ->
                            Pill(option, selected = option == label) { label = option }
                        }
                    }
                    Spacer(Modifier.height(Space.S))
                    LazyRow(contentPadding = PaddingValues(horizontal = Space.Gutter), horizontalArrangement = Arrangement.spacedBy(Space.S)) {
                        items(FocusStore.durations) { option ->
                            Pill("$option min", selected = option == minutes) { minutes = option }
                        }
                    }
                }

                Spacer(Modifier.height(Space.Gutter))
                val totalMillis = (active?.minutes ?: minutes) * 60_000L
                val remaining = active?.remaining(now) ?: totalMillis
                FocusRing(
                    progress = 1f - remaining.toFloat() / totalMillis,
                    time = formatRemaining(remaining),
                    caption = when {
                        active == null -> label
                        active.isRunning -> "${active.label} · stay with it"
                        else -> "${active.label} · paused"
                    }
                )
                Spacer(Modifier.height(Space.Gutter))

                if (active == null) {
                    BlockOptions(blockApps, strict, onBlock = {
                        blockApps = it
                        if (!it) strict = false
                    }, onStrict = { strict = it }, onSetup = { navController.navigate("blocker") })
                    Spacer(Modifier.height(Space.M))
                    Text(
                        stringResource(R.string.focus_put_your_phone_face_down),
                        color = TextSecondary,
                        fontSize = TextSize.BodySmall,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(horizontal = Space.Huge)
                    )
                } else {
                    Text(
                        stringResource(R.string.focus_1_s_points_when_you, active.minutes / FocusStore.MINUTES_PER_POINT),
                        color = Coral,
                        fontSize = TextSize.BodySmall,
                        fontWeight = FontWeight.SemiBold
                    )
                }

                Spacer(Modifier.height(Space.L))
                Row(
                    Modifier.padding(horizontal = Space.Gutter).fillMaxWidth().clip(Shapes.Card).background(BlueTint)
                        .clickable { navController.navigate("blocker") }.padding(Space.L, Space.M),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(Modifier.weight(1f)) {
                        Text(stringResource(R.string.focus_app_blocker_amp_mindful_pause), fontSize = TextSize.BodyLarge, fontWeight = FontWeight.SemiBold)
                        Text(stringResource(R.string.focus_block_apps_set_schedules_pause), color = Navy, fontSize = TextSize.Caption)
                    }
                    Text("›", fontSize = TextSize.TitleLarge, color = Navy)
                }
                Spacer(Modifier.height(Space.L))
                StatsSection()
                Spacer(Modifier.height(Space.Xl))
            }

            Row(
                modifier = Modifier.padding(Space.Gutter, Space.S, Space.Gutter, Space.Section),
                horizontalArrangement = Arrangement.spacedBy(Space.S)
            ) {
                when {
                    active == null -> ActionButton("Start $minutes-minute focus", Ink, Color.White, Modifier.weight(1f)) {
                        val canBlock = blockApps && BlockStore.focusApps.isNotEmpty() && Blocker.ready(context)
                        FocusStore.start(minutes, label, blockApps = canBlock, strict = strict && canBlock)
                        now = System.currentTimeMillis()
                        askPermission()
                    }
                    active.isRunning && active.strict -> {
                        ActionButton("Strict focus · stay with it", Ink, Color.White, Modifier.weight(1f)) { }
                        ActionButton("Emergency", ChipNeutral, Maroon, Modifier.weight(0.6f)) { emergency = true }
                    }
                    active.isRunning -> {
                        ActionButton("Pause", Ink, Color.White, Modifier.weight(1f)) { FocusStore.pause() }
                        ActionButton("End", ChipNeutral, Ink, Modifier.weight(1f)) { confirmEnd = true }
                    }
                    else -> {
                        ActionButton("Resume", Ink, Color.White, Modifier.weight(1f)) {
                            FocusStore.resume()
                            now = System.currentTimeMillis()
                        }
                        ActionButton("End", ChipNeutral, Ink, Modifier.weight(1f)) { confirmEnd = true }
                    }
                }
            }
        }

        if (emergency) {
            EmergencyExit(onCancel = { emergency = false }, onEnd = {
                FocusStore.cancel()
                emergency = false
            })
        }

        if (confirmEnd) {
            ReMakeUDialog(
                onDismissRequest = { confirmEnd = false },
                title = "End this focus block?",
                message = "Ending early won't count towards today's minutes or your focus streak.",
                secondaryText = "Keep going",
                primaryText = "End",
                primaryDestructive = true,
                onPrimary = {
                    FocusStore.cancel()
                    confirmEnd = false
                }
            )
        }

        FocusStore.justFinished?.let { done ->
            val streak = FocusStore.streakDays()
            ReMakeUDialog(
                onDismissRequest = { FocusStore.acknowledgeFinished() },
                onPrimary = { FocusStore.acknowledgeFinished() },
                title = "$done minutes of focus",
                message = "+${done / FocusStore.MINUTES_PER_POINT} points · ${if (streak == 1) "1-day" else "$streak-day"} focus streak. " +
                    "Take a 5-minute break — stand up, drink water, look out of a window.",
                primaryText = "Nice",
                secondaryText = null
            )
        }
    }
}

@Composable
private fun FocusRing(progress: Float, time: String, caption: String) {
    Box(modifier = Modifier.size(250.dp), contentAlignment = Alignment.Center) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val stroke = 18.dp.toPx()
            val inset = stroke / 2
            val arcSize = Size(size.width - stroke, size.height - stroke)
            drawArc(ChipNeutral, 0f, 360f, false, Offset(inset, inset), arcSize, style = Stroke(stroke))
            drawArc(
                Coral,
                -90f,
                360f * progress.coerceIn(0f, 1f),
                false,
                Offset(inset, inset),
                arcSize,
                style = Stroke(stroke, cap = StrokeCap.Round)
            )
        }
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(time, fontSize = TextSize.Number, fontWeight = FontWeight.Bold)
            Text(caption, color = TextSecondary, fontSize = TextSize.BodySmall)
        }
    }
}

@Composable
private fun StatsSection() {
    val today = LocalDate.now()
    val week = FocusStore.week(today)
    val maxMinutes = maxOf(week.maxOrNull() ?: 0, 30)
    val streak = FocusStore.streakDays(today)

    Column(modifier = Modifier.padding(horizontal = Space.Gutter).fillMaxWidth()) {
        Row(horizontalArrangement = Arrangement.spacedBy(Space.S)) {
            StatCard("Today", "${FocusStore.minutesOn(today)} min", Modifier.weight(1f))
            StatCard("Focus streak", if (streak == 1) "1 day" else "$streak days", Modifier.weight(1f), showFlame = streak > 0)
        }
        Spacer(Modifier.height(Space.S))
        Column(
            modifier = Modifier.fillMaxWidth().clip(Shapes.Card).background(CardNeutral).padding(Space.L)
        ) {
            Text(stringResource(R.string.focus_this_week), fontSize = TextSize.Body, fontWeight = FontWeight.SemiBold)
            Text(stringResource(R.string.focus_1_s_min_total, week.sum()), color = TextSecondary, fontSize = TextSize.Caption)
            Spacer(Modifier.height(Space.M))
            Row(
                modifier = Modifier.fillMaxWidth().height(110.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Bottom
            ) {
                week.forEachIndexed { index, value ->
                    val day = DayOfWeek.of(index + 1)
                    val isToday = day == today.dayOfWeek
                    Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.weight(1f)) {
                        Text(if (value > 0) "$value" else "", color = TextSecondary, fontSize = TextSize.Micro)
                        Box(
                            modifier = Modifier
                                .width(18.dp)
                                .height((70f * value / maxMinutes).coerceAtLeast(4f).dp)
                                .clip(Shapes.Small)
                                .background(if (value > 0) (if (isToday) Coral else Ink) else ChipNeutral)
                        )
                        Spacer(Modifier.height(Space.S))
                        Text(
                            day.getDisplayName(TextStyle.NARROW, Locale.getDefault()),
                            color = if (isToday) Ink else TextSecondary,
                            fontSize = TextSize.Micro,
                            fontWeight = if (isToday) FontWeight.Bold else FontWeight.Medium
                        )
                    }
                }
            }
        }
        Spacer(Modifier.height(Space.S))
        Text(
            stringResource(R.string.focus_all_time_1_sh_2, FocusStore.totalMinutes / 60, FocusStore.totalMinutes % 60, FocusStore.totalSessions),
            color = TextSecondary,
            fontSize = TextSize.Caption,
            modifier = Modifier.fillMaxWidth(),
            textAlign = TextAlign.Center
        )
    }
}

@Composable
private fun StatCard(label: String, value: String, modifier: Modifier, showFlame: Boolean = false) {
    Column(modifier = modifier.clip(Shapes.Card).background(CardNeutral).padding(Space.L)) {
        Text(label, color = TextSecondary, fontSize = TextSize.Caption)
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(value, fontSize = TextSize.TitleLarge, fontWeight = FontWeight.Bold)
            if (showFlame) {
                Spacer(Modifier.width(Space.Xs))
                Image(painterResource(R.drawable.ic_flame), contentDescription = null, modifier = Modifier.size(18.dp))
            }
        }
    }
}

@Composable
private fun Pill(label: String, selected: Boolean, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .clip(Shapes.Pill)
            .background(if (selected) Ink else ChipNeutral)
            .clickable(onClick = onClick)
            .padding(Space.L, Space.S)
    ) {
        Text(label, color = if (selected) Color.White else Ink, fontSize = TextSize.Body, fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Medium)
    }
}

@Composable
private fun ActionButton(label: String, background: Color, content: Color, modifier: Modifier, onClick: () -> Unit) {
    Box(
        modifier = modifier
            .height(58.dp)
            .clip(Shapes.Pill)
            .background(background)
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Text(label, color = content, fontSize = TextSize.BodyLarge, fontWeight = FontWeight.SemiBold)
    }
}

private fun formatRemaining(millis: Long): String {
    val totalSeconds = ((millis + 999) / 1000).toInt()
    val hours = totalSeconds / 3600
    val minutes = totalSeconds % 3600 / 60
    val seconds = totalSeconds % 60
    return if (hours > 0) "%d:%02d:%02d".format(hours, minutes, seconds) else "%02d:%02d".format(minutes, seconds)
}

@Composable
private fun BlockOptions(block: Boolean, strict: Boolean, onBlock: (Boolean) -> Unit, onStrict: (Boolean) -> Unit, onSetup: () -> Unit) {
    val context = LocalContext.current
    val ready = Blocker.ready(context) && BlockStore.focusApps.isNotEmpty()
    Column(Modifier.padding(horizontal = Space.Gutter).fillMaxWidth().clip(Shapes.Card).background(CardNeutral).padding(Space.L, Space.M)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Column(Modifier.weight(1f)) {
                Text(stringResource(R.string.focus_block_distracting_apps), fontSize = TextSize.BodyLarge, fontWeight = FontWeight.SemiBold)
                Text(
                    if (ready) {
                        BlockStore.focusApps.size.let { n ->
                            "$n app${if (n == 1) "" else "s"} blocked until the timer ends"
                        }
                    } else {
                        "Set up the app blocker first"
                    },
                    color = TextSecondary,
                    fontSize = TextSize.Caption
                )
            }
            if (ready) {
                Switch(block, onBlock, colors = SwitchDefaults.colors(checkedTrackColor = Ink, checkedThumbColor = Color.White))
            } else {
                Text(
                    stringResource(R.string.focus_set_up),
                    color = Purple,
                    fontSize = TextSize.BodySmall,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.clip(Shapes.Pill).clickable(onClick = onSetup).padding(Space.S, Space.S)
                )
            }
        }
        if (ready && block) {
            Row(Modifier.padding(top = Space.S), verticalAlignment = Alignment.CenterVertically) {
                Column(Modifier.weight(1f)) {
                    Text(stringResource(R.string.focus_strict_mode), fontSize = TextSize.Body, fontWeight = FontWeight.SemiBold)
                    Text(stringResource(R.string.focus_no_pausing_or_ending_early), color = TextSecondary, fontSize = TextSize.Caption)
                }
                if (ProStore.isPro) {
                    Switch(strict, onStrict, colors = SwitchDefaults.colors(checkedTrackColor = Ink, checkedThumbColor = Color.White))
                } else {
                    ProBadge(Modifier.clickable { ProStore.showPaywall("Strict focus is part of Pro") })
                }
            }
        }
    }
}

/** The only way out of strict focus: type a sentence and wait 30 seconds. Doesn't count the session. */
@Composable
private fun EmergencyExit(onCancel: () -> Unit, onEnd: () -> Unit) {
    val phrase = "I choose to stop focusing"
    var typed by remember { mutableStateOf("") }
    var wait by remember { mutableStateOf(30) }
    LaunchedEffect(Unit) {
        while (wait > 0) {
            delay(1000)
            wait--
        }
    }
    ReMakeUDialog(
        onDismissRequest = onCancel,
        title = "Emergency exit",
        message = "Type \"$phrase\" to end strict focus. This session won't count.",
        secondaryText = "Keep focusing",
        primaryText = if (wait > 0) "Wait ${wait}s" else "End focus",
        primaryEnabled = wait == 0 && typed.trim().equals(phrase, ignoreCase = true),
        primaryDestructive = true,
        onPrimary = onEnd,
        content = {
            OutlinedTextField(
                value = typed,
                onValueChange = { typed = it.take(40) },
                singleLine = true,
                placeholder = { Text(phrase) },
                colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = Ink, unfocusedBorderColor = TextMuted),
                modifier = Modifier.fillMaxWidth()
            )
        }
    )
}
