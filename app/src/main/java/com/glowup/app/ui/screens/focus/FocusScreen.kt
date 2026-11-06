package com.glowup.app.ui.screens.focus

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
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
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import com.glowup.app.R
import com.glowup.app.ui.components.DetailHeader
import com.glowup.app.ui.components.GlowUpDialog
import com.glowup.app.ui.components.goBack
import com.glowup.app.ui.components.rememberNotificationPermission
import com.glowup.app.ui.data.FocusStore
import com.glowup.app.ui.theme.*
import kotlinx.coroutines.delay
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.format.TextStyle
import java.util.Locale

@Composable
fun FocusScreen(navController: NavHostController) {
    var label by remember { mutableStateOf(FocusStore.lastLabel) }
    var minutes by remember { mutableStateOf(FocusStore.lastMinutes) }
    var now by remember { mutableLongStateOf(System.currentTimeMillis()) }
    var confirmEnd by remember { mutableStateOf(false) }
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
            DetailHeader("Focus") { navController.goBack() }

            Column(
                modifier = Modifier.weight(1f).verticalScroll(rememberScrollState()).padding(top = 12.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                if (active == null) {
                    LazyRow(contentPadding = PaddingValues(horizontal = 24.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        items(FocusStore.labels) { option ->
                            Pill(option, selected = option == label) { label = option }
                        }
                    }
                    Spacer(Modifier.height(10.dp))
                    LazyRow(contentPadding = PaddingValues(horizontal = 24.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        items(FocusStore.durations) { option ->
                            Pill("$option min", selected = option == minutes) { minutes = option }
                        }
                    }
                }

                Spacer(Modifier.height(24.dp))
                val totalMillis = (active?.minutes ?: minutes) * 60_000L
                val remaining = active?.remaining(now) ?: totalMillis
                FocusRing(
                    progress = 1f - remaining.toFloat() / totalMillis,
                    time = formatRemaining(remaining),
                    caption = when {
                        active == null -> label
                        active.isRunning -> "${active.label} · stay with it"
                        else -> "${active.label} · paused"
                    },
                )
                Spacer(Modifier.height(24.dp))

                if (active == null) {
                    Text(
                        "Put your phone face down. The timer keeps running if you leave the app.",
                        color = TextSecondary,
                        fontSize = 13.sp,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(horizontal = 40.dp),
                    )
                } else {
                    Text(
                        "+${active.minutes / FocusStore.MINUTES_PER_POINT} points when you finish",
                        color = Coral,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold,
                    )
                }

                Spacer(Modifier.height(28.dp))
                StatsSection()
                Spacer(Modifier.height(20.dp))
            }

            Row(
                modifier = Modifier.padding(24.dp, 8.dp, 24.dp, 28.dp),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                when {
                    active == null -> ActionButton("Start $minutes-minute focus", Ink, Color.White, Modifier.weight(1f)) {
                        FocusStore.start(minutes, label)
                        now = System.currentTimeMillis()
                        askPermission()
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

        if (confirmEnd) {
            GlowUpDialog(
                onDismissRequest = { confirmEnd = false },
                title = "End this focus block?",
                message = "Ending early won't count towards today's minutes or your focus streak.",
                secondaryText = "Keep going",
                primaryText = "End",
                primaryDestructive = true,
                onPrimary = {
                    FocusStore.cancel()
                    confirmEnd = false
                },
            )
        }

        FocusStore.justFinished?.let { done ->
            val streak = FocusStore.streakDays()
            GlowUpDialog(
                onDismissRequest = { FocusStore.acknowledgeFinished() },
                onPrimary = { FocusStore.acknowledgeFinished() },
                title = "$done minutes of focus",
                message = "+${done / FocusStore.MINUTES_PER_POINT} points · ${if (streak == 1) "1-day" else "$streak-day"} focus streak. " +
                    "Take a 5-minute break — stand up, drink water, look out of a window.",
                primaryText = "Nice",
                secondaryText = null,
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
                Coral, -90f, 360f * progress.coerceIn(0f, 1f), false, Offset(inset, inset), arcSize,
                style = Stroke(stroke, cap = StrokeCap.Round),
            )
        }
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(time, fontSize = 48.sp, fontWeight = FontWeight.Bold)
            Text(caption, color = TextSecondary, fontSize = 13.sp)
        }
    }
}

@Composable
private fun StatsSection() {
    val today = LocalDate.now()
    val week = FocusStore.week(today)
    val maxMinutes = maxOf(week.maxOrNull() ?: 0, 30)
    val streak = FocusStore.streakDays(today)

    Column(modifier = Modifier.padding(horizontal = 24.dp).fillMaxWidth()) {
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            StatCard("Today", "${FocusStore.minutesOn(today)} min", Modifier.weight(1f))
            StatCard("Focus streak", if (streak == 1) "1 day" else "$streak days", Modifier.weight(1f), showFlame = streak > 0)
        }
        Spacer(Modifier.height(10.dp))
        Column(
            modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(22.dp)).background(CardNeutral).padding(18.dp),
        ) {
            Text("This week", fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
            Text("${week.sum()} min total", color = TextSecondary, fontSize = 12.sp)
            Spacer(Modifier.height(14.dp))
            Row(
                modifier = Modifier.fillMaxWidth().height(110.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Bottom,
            ) {
                week.forEachIndexed { index, value ->
                    val day = DayOfWeek.of(index + 1)
                    val isToday = day == today.dayOfWeek
                    Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.weight(1f)) {
                        Text(if (value > 0) "$value" else "", color = TextSecondary, fontSize = 10.sp)
                        Box(
                            modifier = Modifier
                                .width(18.dp)
                                .height((70f * value / maxMinutes).coerceAtLeast(4f).dp)
                                .clip(RoundedCornerShape(6.dp))
                                .background(if (value > 0) (if (isToday) Coral else Ink) else ChipNeutral),
                        )
                        Spacer(Modifier.height(6.dp))
                        Text(
                            day.getDisplayName(TextStyle.NARROW, Locale.getDefault()),
                            color = if (isToday) Ink else TextSecondary,
                            fontSize = 11.sp,
                            fontWeight = if (isToday) FontWeight.Bold else FontWeight.Medium,
                        )
                    }
                }
            }
        }
        Spacer(Modifier.height(10.dp))
        Text(
            "All time: ${FocusStore.totalMinutes / 60}h ${FocusStore.totalMinutes % 60}m across ${FocusStore.totalSessions} sessions",
            color = TextSecondary,
            fontSize = 12.sp,
            modifier = Modifier.fillMaxWidth(),
            textAlign = TextAlign.Center,
        )
    }
}

@Composable
private fun StatCard(label: String, value: String, modifier: Modifier, showFlame: Boolean = false) {
    Column(modifier = modifier.clip(RoundedCornerShape(20.dp)).background(CardNeutral).padding(16.dp)) {
        Text(label, color = TextSecondary, fontSize = 12.sp)
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(value, fontSize = 20.sp, fontWeight = FontWeight.Bold)
            if (showFlame) {
                Spacer(Modifier.width(4.dp))
                Image(painterResource(R.drawable.ic_flame), contentDescription = null, modifier = Modifier.size(18.dp))
            }
        }
    }
}

@Composable
private fun Pill(label: String, selected: Boolean, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(999.dp))
            .background(if (selected) Ink else ChipNeutral)
            .clickable(onClick = onClick)
            .padding(16.dp, 10.dp),
    ) {
        Text(label, color = if (selected) Color.White else Ink, fontSize = 13.5.sp, fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Medium)
    }
}

@Composable
private fun ActionButton(label: String, background: Color, content: Color, modifier: Modifier, onClick: () -> Unit) {
    Box(
        modifier = modifier
            .height(58.dp)
            .clip(RoundedCornerShape(999.dp))
            .background(background)
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        Text(label, color = content, fontSize = 15.5.sp, fontWeight = FontWeight.SemiBold)
    }
}

private fun formatRemaining(millis: Long): String {
    val totalSeconds = ((millis + 999) / 1000).toInt()
    val hours = totalSeconds / 3600
    val minutes = totalSeconds % 3600 / 60
    val seconds = totalSeconds % 60
    return if (hours > 0) "%d:%02d:%02d".format(hours, minutes, seconds) else "%02d:%02d".format(minutes, seconds)
}
