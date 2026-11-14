package com.glowup.app.ui.screens.sleep

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronRight
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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.glowup.app.R
import com.glowup.app.ui.components.DetailHeader
import com.glowup.app.ui.components.ReMakeUDialog
import com.glowup.app.ui.components.ReMakeUOptionRow
import com.glowup.app.ui.components.goBack
import com.glowup.app.ui.data.ProfileStore
import com.glowup.app.ui.data.SleepEntry
import com.glowup.app.ui.data.SleepLogStore
import com.glowup.app.ui.data.SleepStore
import com.glowup.app.ui.data.formatClock
import com.glowup.app.ui.data.formatDuration
import com.glowup.app.ui.design.Shapes
import com.glowup.app.ui.design.Space
import com.glowup.app.ui.design.TextSize
import com.glowup.app.ui.theme.*
import java.time.LocalDate
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import java.util.Locale

internal val qualityLabels = listOf("Awful", "Poor", "Okay", "Good", "Great")

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
            DetailHeader(stringResource(R.string.sleep_log_sleep_log)) { navController.goBack() }

            Column(modifier = Modifier.weight(1f).verticalScroll(rememberScrollState()).padding(Space.Gutter, Space.L, Space.Gutter, Space.Section)) {
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
                        onCancel = if (SleepLogStore.on(today) != null || formDate != today) {
                            (
                                {
                                    editing = false
                                    formDate = today
                                }
                                )
                        } else {
                            null
                        }
                    )
                } else {
                    SleepLogStore.on(today)?.let { LastNightCard(it) { edit(it) } }
                    Spacer(Modifier.height(Space.S))
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(Shapes.Card)
                            .background(Yellow)
                            .clickable { navController.navigate("morning") }
                            .padding(Space.L, Space.M),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(stringResource(R.string.sleep_log_start_your_morning_routine), fontSize = TextSize.Body, fontWeight = FontWeight.SemiBold)
                            Text(stringResource(R.string.sleep_log_breathing_stretching_and_meditation), color = Amber, fontSize = TextSize.Caption)
                        }
                        Icon(Icons.Filled.ChevronRight, contentDescription = null, tint = Ink, modifier = Modifier.size(16.dp))
                    }
                }

                Spacer(Modifier.height(Space.M))
                DebtCard(today)

                Spacer(Modifier.height(Space.M))
                StatsRow(today)

                Spacer(Modifier.height(Space.Gutter))
                Text(stringResource(R.string.sleep_log_last_14_nights), fontSize = TextSize.Title, fontWeight = FontWeight.Bold)
                Text(
                    stringResource(R.string.sleep_log_dashed_line_is_your_1, formatDuration(SleepLogStore.needMinutes)),
                    color = TextSecondary,
                    fontSize = TextSize.Caption,
                    modifier = Modifier.padding(top = 2.dp)
                )
                Spacer(Modifier.height(Space.M))
                NightsChart(today)

                Spacer(Modifier.height(Space.M))
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(Shapes.Card)
                        .background(CardNeutral)
                        .clickable { showNeed = true }
                        .padding(Space.L, Space.L),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(stringResource(R.string.sleep_log_your_sleep_need), fontSize = TextSize.Body, fontWeight = FontWeight.SemiBold)
                        Text(stringResource(R.string.sleep_log_used_to_work_out_sleep), color = TextSecondary, fontSize = TextSize.Caption)
                    }
                    Text(formatDuration(SleepLogStore.needMinutes), fontSize = TextSize.Body, fontWeight = FontWeight.Bold)
                    Icon(Icons.Filled.ChevronRight, contentDescription = null, tint = TextSecondary, modifier = Modifier.padding(start = Space.Xs).size(16.dp))
                }

                val history = SleepLogStore.entries.values.sortedByDescending { it.date }.take(14)
                if (history.isNotEmpty()) {
                    Spacer(Modifier.height(Space.Gutter))
                    Text(stringResource(R.string.common_history), fontSize = TextSize.Title, fontWeight = FontWeight.Bold)
                    Text(
                        stringResource(R.string.sleep_log_tap_a_night_to_edit),
                        color = TextSecondary,
                        fontSize = TextSize.Caption,
                        modifier = Modifier.padding(top = 2.dp, bottom = Space.S)
                    )
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
                onConfirm = {
                    if (which == "bed") bed = it else wake = it
                    pickTime = null
                },
                onDismiss = { pickTime = null }
            )
        }

        if (showNeed) {
            ReMakeUDialog(
                onDismissRequest = { showNeed = false },
                title = "Your sleep need",
                message = if (ProfileStore.isMinor) "Teens usually need 8–10 hours a night." else "Most adults need 7–9 hours. If you wake up tired after 8, try a little more.",
                primaryText = "Done",
                secondaryText = null,
                content = {
                    Column(Modifier.heightIn(max = 340.dp).verticalScroll(rememberScrollState())) {
                        SleepLogStore.needOptions.forEach { minutes ->
                            ReMakeUOptionRow(formatDuration(minutes), selected = minutes == SleepLogStore.needMinutes) { SleepLogStore.updateNeed(minutes) }
                        }
                    }
                }
            )
        }

        confirmDelete?.let { entry ->
            ReMakeUDialog(
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
                }
            )
        }
    }
}

internal fun dayLabel(date: LocalDate, today: LocalDate): String = when (date) {
    today -> "Last night"
    today.minusDays(1) -> "Night before"
    else -> date.format(DateTimeFormatter.ofPattern("EEE d MMM", Locale.getDefault()))
}
