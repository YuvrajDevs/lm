package com.glowup.app.ui.screens.sleep

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
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.outlined.Bedtime
import androidx.compose.material.icons.outlined.DeleteOutline
import androidx.compose.material.icons.outlined.WbSunny
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.TimePicker
import androidx.compose.material3.TimePickerDefaults
import androidx.compose.material3.rememberTimePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import com.glowup.app.ui.components.DetailHeader
import com.glowup.app.ui.components.GlowUpDialog
import com.glowup.app.ui.components.GlowUpOptionRow
import com.glowup.app.ui.components.goBack
import com.glowup.app.ui.data.DebtLevel
import com.glowup.app.ui.data.ProfileStore
import com.glowup.app.ui.data.SleepEntry
import com.glowup.app.ui.data.SleepLogStore
import com.glowup.app.ui.data.SleepMath
import com.glowup.app.ui.data.SleepStore
import com.glowup.app.ui.data.formatClock
import com.glowup.app.ui.data.formatDuration
import com.glowup.app.ui.screens.mood.moodScoreColors
import com.glowup.app.ui.theme.*
import java.time.LocalDate
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import java.time.format.TextStyle
import java.util.Locale

private val qualityLabels = listOf("Awful", "Poor", "Okay", "Good", "Great")

private fun defaultWake(now: LocalTime = LocalTime.now()): LocalTime =
    if (now.hour in 4..12) now.withMinute(now.minute / 5 * 5).withSecond(0).withNano(0) else SleepStore.wakeTime

@Composable
fun SleepLogScreen(navController: NavHostController) {
    val today = LocalDate.now()
    var editing by remember { mutableStateOf(SleepLogStore.on(today) == null) }
    var formDate by remember { mutableStateOf(today) }
    var bed by remember { mutableStateOf(SleepLogStore.on(today)?.bed ?: SleepStore.bedtime) }
    var wake by remember { mutableStateOf(SleepLogStore.on(today)?.wake ?: defaultWake()) }
    var quality by remember { mutableStateOf(SleepLogStore.on(today)?.quality) }
    var pickTime by remember { mutableStateOf<String?>(null) }
    var showNeed by remember { mutableStateOf(false) }
    var confirmDelete by remember { mutableStateOf<SleepEntry?>(null) }

    fun edit(entry: SleepEntry) {
        formDate = entry.date
        bed = entry.bed
        wake = entry.wake
        quality = entry.quality
        editing = true
    }

    Box(modifier = Modifier.fillMaxSize().background(Surface).systemBarsPadding()) {
        Column(modifier = Modifier.fillMaxSize()) {
            DetailHeader("Sleep log") { navController.goBack() }

            Column(modifier = Modifier.weight(1f).verticalScroll(rememberScrollState()).padding(24.dp, 18.dp, 24.dp, 28.dp)) {
                if (editing) {
                    CheckInCard(
                        date = formDate,
                        today = today,
                        bed = bed,
                        wake = wake,
                        quality = quality,
                        onBed = { bed = it },
                        onWake = { wake = it },
                        onPickBed = { pickTime = "bed" },
                        onPickWake = { pickTime = "wake" },
                        onQuality = { quality = it },
                        onSave = {
                            SleepLogStore.log(SleepEntry(formDate, bed, wake, quality ?: 3))
                            editing = false
                        },
                        onCancel = if (SleepLogStore.on(today) != null || formDate != today) ({ editing = false; formDate = today }) else null,
                    )
                } else {
                    SleepLogStore.on(today)?.let { LastNightCard(it) { edit(it) } }
                    Spacer(Modifier.height(10.dp))
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(20.dp))
                            .background(Yellow)
                            .clickable { navController.navigate("morning") }
                            .padding(18.dp, 14.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text("Start your morning routine", fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
                            Text("Breathing, stretching and meditation", color = Color(0xFF7A6600), fontSize = 12.sp)
                        }
                        Icon(Icons.Filled.ChevronRight, contentDescription = null, tint = Ink, modifier = Modifier.size(16.dp))
                    }
                }

                Spacer(Modifier.height(14.dp))
                DebtCard(today)

                Spacer(Modifier.height(14.dp))
                StatsRow(today)

                Spacer(Modifier.height(24.dp))
                Text("Last 14 nights", fontSize = 17.sp, fontWeight = FontWeight.Bold)
                Text("Dashed line is your ${formatDuration(SleepLogStore.needMinutes)} sleep need", color = TextSecondary, fontSize = 12.sp, modifier = Modifier.padding(top = 2.dp))
                Spacer(Modifier.height(12.dp))
                NightsChart(today)

                Spacer(Modifier.height(14.dp))
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(20.dp))
                        .background(CardNeutral)
                        .clickable { showNeed = true }
                        .padding(18.dp, 15.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text("Your sleep need", fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
                        Text("Used to work out sleep debt", color = TextSecondary, fontSize = 12.sp)
                    }
                    Text(formatDuration(SleepLogStore.needMinutes), fontSize = 14.sp, fontWeight = FontWeight.Bold)
                    Icon(Icons.Filled.ChevronRight, contentDescription = null, tint = TextSecondary, modifier = Modifier.padding(start = 4.dp).size(16.dp))
                }

                val history = SleepLogStore.entries.values.sortedByDescending { it.date }.take(14)
                if (history.isNotEmpty()) {
                    Spacer(Modifier.height(24.dp))
                    Text("History", fontSize = 17.sp, fontWeight = FontWeight.Bold)
                    Text("Tap a night to edit it", color = TextSecondary, fontSize = 12.sp, modifier = Modifier.padding(top = 2.dp, bottom = 10.dp))
                    history.forEach { entry ->
                        HistoryRow(entry, onClick = { edit(entry) }, onDelete = { confirmDelete = entry })
                    }
                }
            }
        }

        pickTime?.let { which ->
            TimePickerDialog(
                title = if (which == "bed") "Went to bed" else "Woke up",
                initial = if (which == "bed") bed else wake,
                onConfirm = { if (which == "bed") bed = it else wake = it; pickTime = null },
                onDismiss = { pickTime = null },
            )
        }

        if (showNeed) {
            GlowUpDialog(
                onDismissRequest = { showNeed = false },
                title = "Your sleep need",
                message = if (ProfileStore.isMinor) "Teens usually need 8–10 hours a night." else "Most adults need 7–9 hours. If you wake up tired after 8, try a little more.",
                primaryText = "Done",
                secondaryText = null,
                content = {
                    Column(Modifier.heightIn(max = 340.dp).verticalScroll(rememberScrollState())) {
                        SleepLogStore.needOptions.forEach { minutes ->
                            GlowUpOptionRow(formatDuration(minutes), selected = minutes == SleepLogStore.needMinutes) { SleepLogStore.updateNeed(minutes) }
                        }
                    }
                },
            )
        }

        confirmDelete?.let { entry ->
            GlowUpDialog(
                onDismissRequest = { confirmDelete = null },
                title = "Delete this night?",
                message = "${dayLabel(entry.date, today)}: ${formatClock(entry.bed)} – ${formatClock(entry.wake)}. This can't be undone.",
                secondaryText = "Cancel",
                primaryText = "Delete",
                primaryDestructive = true,
                onPrimary = {
                    SleepLogStore.remove(entry.date)
                    if (entry.date == today) editing = true
                    confirmDelete = null
                },
            )
        }
    }
}

private fun dayLabel(date: LocalDate, today: LocalDate): String = when (date) {
    today -> "Last night"
    today.minusDays(1) -> "Night before"
    else -> date.format(DateTimeFormatter.ofPattern("EEE d MMM", Locale.getDefault()))
}

@Composable
private fun CheckInCard(
    date: LocalDate,
    today: LocalDate,
    bed: LocalTime,
    wake: LocalTime,
    quality: Int?,
    onBed: (LocalTime) -> Unit,
    onWake: (LocalTime) -> Unit,
    onPickBed: () -> Unit,
    onPickWake: () -> Unit,
    onQuality: (Int) -> Unit,
    onSave: () -> Unit,
    onCancel: (() -> Unit)?,
) {
    val minutes = SleepEntry(date, bed, wake, 3).minutes
    val need = SleepLogStore.needMinutes
    Column(modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(28.dp)).background(PurpleTint).padding(20.dp, 18.dp)) {
        Text(
            if (date == today) (if (LocalTime.now().hour in 4..11) "GOOD MORNING" else "LAST NIGHT") else "EDITING " + date.format(DateTimeFormatter.ofPattern("EEE d MMM", Locale.getDefault())).uppercase(),
            color = Color(0xFF6B4FD1), fontSize = 11.sp, fontWeight = FontWeight.SemiBold, letterSpacing = 0.4.sp,
        )
        Text(if (date == today) "How did you sleep?" else "Edit this night", fontSize = 22.sp, fontWeight = FontWeight.Bold)
        Spacer(Modifier.height(14.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            TimeTile("Went to bed", Icons.Outlined.Bedtime, bed, Modifier.weight(1f), onBed, onPickBed)
            TimeTile("Woke up", Icons.Outlined.WbSunny, wake, Modifier.weight(1f), onWake, onPickWake)
        }
        Spacer(Modifier.height(12.dp))
        val diff = minutes - need
        Text(
            buildString {
                append(formatDuration(minutes))
                append(" asleep · ")
                append(
                    when {
                        diff >= 0 -> if (diff < 15) "right on your need" else "${formatDuration(diff)} over your need"
                        else -> "${formatDuration(-diff)} short of your ${formatDuration(need)}"
                    },
                )
            },
            color = Color(0xFF5A4A8C), fontSize = 13.sp, fontWeight = FontWeight.SemiBold,
        )
        if (minutes > 16 * 60) {
            Text("That's over 16 hours — check the times.", color = Maroon, fontSize = 12.sp, modifier = Modifier.padding(top = 4.dp))
        }
        Spacer(Modifier.height(16.dp))
        Text("How rested do you feel?", fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
        Spacer(Modifier.height(8.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            qualityLabels.forEachIndexed { i, label ->
                val value = i + 1
                val on = quality == value
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(16.dp))
                        .background(if (on) Color.White else Color.White.copy(alpha = 0.55f))
                        .border(2.dp, if (on) Ink else Color.Transparent, RoundedCornerShape(16.dp))
                        .clickable { onQuality(value) }
                        .padding(vertical = 10.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    Box(Modifier.size(14.dp).clip(CircleShape).background(moodScoreColors[i]))
                    Spacer(Modifier.height(6.dp))
                    Text(label, fontSize = 11.5.sp, fontWeight = FontWeight.SemiBold, maxLines = 1)
                }
            }
        }
        Spacer(Modifier.height(16.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            onCancel?.let {
                Box(
                    Modifier.weight(1f).height(52.dp).clip(RoundedCornerShape(999.dp)).background(Color.White).clickable(onClick = it),
                    contentAlignment = Alignment.Center,
                ) { Text("Cancel", fontSize = 14.5.sp, fontWeight = FontWeight.SemiBold) }
            }
            Box(
                Modifier
                    .weight(if (onCancel != null) 1.4f else 1f)
                    .height(52.dp)
                    .clip(RoundedCornerShape(999.dp))
                    .background(if (quality != null) Ink else Color(0xFFD8D5CE))
                    .clickable(enabled = quality != null, onClick = onSave),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    when {
                        quality == null -> "Pick how rested you feel"
                        SleepLogStore.on(date) == null -> "Save · +${SleepLogStore.POINTS_PER_LOG} points"
                        else -> "Save changes"
                    },
                    color = Color.White, fontSize = 14.5.sp, fontWeight = FontWeight.SemiBold,
                )
            }
        }
    }
}

@Composable
private fun TimeTile(label: String, icon: ImageVector, time: LocalTime, modifier: Modifier, onChange: (LocalTime) -> Unit, onPick: () -> Unit) {
    Column(modifier = modifier.clip(RoundedCornerShape(20.dp)).background(Color.White).padding(12.dp, 12.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(icon, contentDescription = null, tint = Purple, modifier = Modifier.size(15.dp))
            Spacer(Modifier.width(6.dp))
            Text(label, color = TextSecondary, fontSize = 11.5.sp, fontWeight = FontWeight.Medium)
        }
        Text(
            formatClock(time),
            fontSize = 22.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(vertical = 4.dp).clip(RoundedCornerShape(8.dp)).clickable(onClick = onPick),
        )
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            StepButton(Icons.Filled.Remove, "15 minutes earlier") { onChange(time.minusMinutes(15)) }
            StepButton(Icons.Filled.Add, "15 minutes later") { onChange(time.plusMinutes(15)) }
        }
    }
}

@Composable
private fun StepButton(icon: ImageVector, description: String, onClick: () -> Unit) {
    Box(Modifier.size(34.dp).clip(CircleShape).background(ChipNeutral).clickable(onClick = onClick), contentAlignment = Alignment.Center) {
        Icon(icon, contentDescription = description, tint = Ink, modifier = Modifier.size(16.dp))
    }
}

@Composable
private fun LastNightCard(entry: SleepEntry, onEdit: () -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(28.dp)).background(PurpleTint).clickable(onClick = onEdit).padding(20.dp, 18.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text("LAST NIGHT", color = Color(0xFF6B4FD1), fontSize = 11.sp, fontWeight = FontWeight.SemiBold, letterSpacing = 0.4.sp)
            Text(formatDuration(entry.minutes), fontSize = 26.sp, fontWeight = FontWeight.Bold)
            Text("${formatClock(entry.bed)} – ${formatClock(entry.wake)} · felt ${qualityLabels[entry.quality - 1].lowercase()}", color = Color(0xFF5A4A8C), fontSize = 12.5.sp)
        }
        Text("Edit", fontSize = 13.sp, fontWeight = FontWeight.SemiBold, modifier = Modifier.clip(RoundedCornerShape(999.dp)).background(Color.White).padding(14.dp, 8.dp))
    }
}

@Composable
private fun DebtCard(today: LocalDate) {
    val debt = SleepLogStore.debtMinutes(today)
    val nights = SleepLogStore.recent(today).size
    if (debt == null) {
        Column(modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(22.dp)).background(CardNeutral).padding(18.dp, 16.dp)) {
            Text("Sleep debt", fontSize = 15.sp, fontWeight = FontWeight.Bold)
            Spacer(Modifier.height(4.dp))
            val more = SleepMath.MIN_NIGHTS_FOR_DEBT - nights
            Text(
                "Log $more more night${if (more == 1) "" else "s"} to see how much sleep you owe yourself. It takes 10 seconds each morning.",
                color = TextSecondary, fontSize = 12.5.sp, lineHeight = 18.sp,
            )
        }
        return
    }
    val level = SleepMath.level(debt)
    val (bg, accent) = when (level) {
        DebtLevel.LOW -> GreenTint to Green
        DebtLevel.MODERATE -> YellowTint to Color(0xFFB08A00)
        DebtLevel.HIGH -> CoralTint to Coral
    }
    Column(modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(22.dp)).background(bg).padding(18.dp, 16.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Column(modifier = Modifier.weight(1f)) {
                Text("SLEEP DEBT", color = accent, fontSize = 11.sp, fontWeight = FontWeight.Bold, letterSpacing = 0.4.sp)
                Text(if (debt == 0) "None" else formatDuration(debt), fontSize = 28.sp, fontWeight = FontWeight.Bold)
            }
            Text(
                level.label,
                color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold,
                modifier = Modifier.clip(RoundedCornerShape(999.dp)).background(accent).padding(12.dp, 6.dp),
            )
        }
        Spacer(Modifier.height(6.dp))
        Text(
            when (level) {
                DebtLevel.LOW -> "You're well rested. Keep your bed and wake times steady to stay here."
                DebtLevel.MODERATE -> "Pay it back gently: go to bed 30–45 minutes earlier for the next few nights rather than one long lie-in."
                DebtLevel.HIGH -> "You're running on empty. Expect lower focus and mood — prioritise earlier nights this week, and avoid caffeine after 2 pm."
            },
            color = Ink.copy(alpha = 0.8f), fontSize = 12.5.sp, lineHeight = 18.sp,
        )
        Spacer(Modifier.height(8.dp))
        Text("Estimated from $nights logged night${if (nights == 1) "" else "s"} in the last 2 weeks. Not a medical measurement.", color = TextSecondary, fontSize = 11.sp)
    }
}

@Composable
private fun StatsRow(today: LocalDate) {
    val week = SleepLogStore.recent(today, 7)
    val average = SleepMath.averageMinutes(week)
    val consistency = SleepLogStore.consistency(today)
    Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
        StatTile(
            "7-night average",
            average?.let(::formatDuration) ?: "—",
            if (average == null) "Log a night to start" else "${week.size} night${if (week.size == 1) "" else "s"} logged",
            Modifier.weight(1f),
        )
        StatTile(
            "Consistency",
            consistency?.let { "$it" } ?: "—",
            consistency?.let(SleepMath::consistencyLabel) ?: "Needs ${SleepMath.MIN_NIGHTS_FOR_CONSISTENCY} nights",
            Modifier.weight(1f),
        )
    }
}

@Composable
private fun StatTile(label: String, value: String, sub: String, modifier: Modifier) {
    Column(modifier = modifier.clip(RoundedCornerShape(22.dp)).background(CardNeutral).padding(16.dp, 14.dp)) {
        Text(label, color = TextSecondary, fontSize = 11.5.sp, fontWeight = FontWeight.Medium)
        Text(value, fontSize = 22.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(vertical = 2.dp))
        Text(sub, color = TextSecondary, fontSize = 11.5.sp)
    }
}

@Composable
private fun NightsChart(today: LocalDate) {
    val need = SleepLogStore.needMinutes
    val days = (13 downTo 0).map { today.minusDays(it.toLong()) }
    val maxMinutes = maxOf(need + 120, days.maxOfOrNull { SleepLogStore.on(it)?.minutes ?: 0 } ?: 0)
    Column(modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(22.dp)).background(CardNeutral).padding(14.dp, 16.dp)) {
        Canvas(modifier = Modifier.fillMaxWidth().height(130.dp)) {
            val gap = 5.dp.toPx()
            val barW = (size.width - gap * 13) / 14
            days.forEachIndexed { i, day ->
                val x = i * (barW + gap)
                val entry = SleepLogStore.on(day)
                if (entry == null) {
                    drawCircle(Color(0xFFDAD7D0), radius = 2.5.dp.toPx(), center = Offset(x + barW / 2, size.height - 3.dp.toPx()))
                } else {
                    val h = (entry.minutes / maxMinutes.toFloat()) * size.height
                    val color = when {
                        entry.minutes >= need -> Green
                        entry.minutes >= need - 60 -> Color(0xFFFFC94D)
                        else -> Coral
                    }
                    drawRoundRect(color, Offset(x, size.height - h), Size(barW, h), CornerRadius(4.dp.toPx()))
                }
            }
            val needY = size.height - need / maxMinutes.toFloat() * size.height
            drawLine(
                Ink.copy(alpha = 0.45f), Offset(0f, needY), Offset(size.width, needY),
                strokeWidth = 1.5.dp.toPx(), pathEffect = PathEffect.dashPathEffect(floatArrayOf(8f, 8f)),
            )
        }
        Spacer(Modifier.height(6.dp))
        Row {
            days.forEach { day ->
                Text(
                    day.dayOfWeek.getDisplayName(TextStyle.NARROW, Locale.getDefault()),
                    color = if (day == today) Ink else TextSecondary,
                    fontSize = 10.sp,
                    fontWeight = if (day == today) FontWeight.Bold else FontWeight.Medium,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.weight(1f),
                )
            }
        }
    }
}

@Composable
private fun HistoryRow(entry: SleepEntry, onClick: () -> Unit, onDelete: () -> Unit) {
    val today = LocalDate.now()
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 8.dp)
            .clip(RoundedCornerShape(18.dp))
            .background(CardNeutral)
            .clickable(onClick = onClick)
            .padding(16.dp, 12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(Modifier.size(10.dp).clip(CircleShape).background(moodScoreColors[entry.quality - 1]))
        Spacer(Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(dayLabel(entry.date, today), fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
            Text("${formatClock(entry.bed)} – ${formatClock(entry.wake)} · ${qualityLabels[entry.quality - 1]}", color = TextSecondary, fontSize = 12.sp)
        }
        Text(formatDuration(entry.minutes), fontSize = 14.sp, fontWeight = FontWeight.Bold)
        Box(Modifier.padding(start = 6.dp).size(34.dp).clip(CircleShape).clickable(onClick = onDelete), contentAlignment = Alignment.Center) {
            Icon(Icons.Outlined.DeleteOutline, contentDescription = "Delete night", tint = TextSecondary, modifier = Modifier.size(18.dp))
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun TimePickerDialog(title: String, initial: LocalTime, onConfirm: (LocalTime) -> Unit, onDismiss: () -> Unit) {
    val state = rememberTimePickerState(initial.hour, initial.minute, is24Hour = android.text.format.DateFormat.is24HourFormat(androidx.compose.ui.platform.LocalContext.current))
    GlowUpDialog(
        onDismissRequest = onDismiss,
        title = title,
        secondaryText = "Cancel",
        primaryText = "Set",
        onPrimary = { onConfirm(LocalTime.of(state.hour, state.minute)) },
        content = {
            Box(Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                TimePicker(
                    state = state,
                    colors = TimePickerDefaults.colors(
                        clockDialColor = ChipNeutral,
                        selectorColor = Ink,
                        timeSelectorSelectedContainerColor = Ink,
                        timeSelectorSelectedContentColor = Color.White,
                        timeSelectorUnselectedContainerColor = ChipNeutral,
                        periodSelectorSelectedContainerColor = Ink,
                        periodSelectorSelectedContentColor = Color.White,
                    ),
                )
            }
        },
    )
}
