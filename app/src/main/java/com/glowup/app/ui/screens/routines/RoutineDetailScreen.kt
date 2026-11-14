package com.glowup.app.ui.screens.routines

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
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
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.glowup.app.R
import com.glowup.app.ui.components.DetailHeader
import com.glowup.app.ui.components.ReMakeUDialog
import com.glowup.app.ui.components.ReMakeUOptionRow
import com.glowup.app.ui.components.StreakChip
import com.glowup.app.ui.components.WeekStreakDots
import com.glowup.app.ui.components.goBack
import com.glowup.app.ui.components.rememberNotificationPermission
import com.glowup.app.ui.data.RoutineStore
import com.glowup.app.ui.data.formatClock
import com.glowup.app.ui.design.Shapes
import com.glowup.app.ui.design.Space
import com.glowup.app.ui.design.TextSize
import com.glowup.app.ui.theme.*
import java.time.LocalDate
import java.time.LocalTime
import java.time.YearMonth
import java.time.format.TextStyle
import java.util.Locale

@Composable
fun RoutineDetailScreen(navController: NavHostController, routineId: Int) {
    val askPermission = rememberNotificationPermission()
    val routine = RoutineStore.find(routineId) ?: return

    val today = LocalDate.now()
    val yearMonth = YearMonth.from(today)
    val monthLabel = yearMonth.month.getDisplayName(TextStyle.FULL, Locale.getDefault()) + " " + yearMonth.year
    val daysInMonth = yearMonth.lengthOfMonth()
    val leadingBlanks = yearMonth.atDay(1).dayOfWeek.value - 1 // Monday = 0
    var showReminderPicker by remember { mutableStateOf(false) }
    var showDeleteConfirm by remember { mutableStateOf(false) }

    Box(modifier = Modifier.fillMaxSize().background(Surface).systemBarsPadding()) {
        Column(modifier = Modifier.fillMaxSize()) {
            DetailHeader(routine.title) { navController.goBack() }

            Column(modifier = Modifier.weight(1f).verticalScroll(rememberScrollState()).padding(Space.Gutter, Space.L, Space.Gutter, Space.Gutter)) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(Shapes.Large)
                        .background(routine.tint)
                        .padding(Space.Xl, Space.L)
                ) {
                    Column(modifier = Modifier.fillMaxWidth(0.7f)) {
                        Text(routine.area, color = routine.accent, fontSize = TextSize.Micro, fontWeight = FontWeight.SemiBold)
                        Text(
                            routine.title,
                            fontSize = TextSize.Title,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(top = Space.Xs, bottom = Space.M)
                        )
                        StreakChip(routine.streakDays(today), routine.accent)
                        Spacer(Modifier.height(Space.S))
                        WeekStreakDots(routine.week(today), routine.accent)
                    }
                    Image(
                        painterResource(routine.icon),
                        contentDescription = null,
                        modifier = Modifier.align(Alignment.TopEnd).size(56.dp)
                    )
                }

                Spacer(Modifier.height(Space.Xl))

                Text(monthLabel, fontSize = TextSize.BodyLarge, fontWeight = FontWeight.SemiBold)
                Spacer(Modifier.height(Space.M))

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    listOf("M", "T", "W", "T", "F", "S", "S").forEach {
                        Text(
                            it,
                            color = TextSecondary,
                            fontSize = TextSize.Caption,
                            fontWeight = FontWeight.Medium,
                            modifier = Modifier.weight(1f),
                            textAlign = TextAlign.Center
                        )
                    }
                }
                Spacer(Modifier.height(Space.S))

                val cells = buildList {
                    repeat(leadingBlanks) { add(0) }
                    for (day in 1..daysInMonth) add(day)
                }
                cells.chunked(7).forEach { week ->
                    Row(modifier = Modifier.fillMaxWidth().padding(vertical = Space.Xs), horizontalArrangement = Arrangement.SpaceBetween) {
                        week.forEach { day ->
                            Box(modifier = Modifier.weight(1f), contentAlignment = Alignment.Center) {
                                if (day == 0) {
                                    Spacer(Modifier.size(32.dp))
                                } else {
                                    val isToday = day == today.dayOfMonth
                                    val isCompleted = routine.isDoneOn(yearMonth.atDay(day))
                                    Box(
                                        modifier = Modifier
                                            .size(32.dp)
                                            .clip(CircleShape)
                                            .background(if (isCompleted) routine.accent else Color.Transparent)
                                            .let { m -> if (isToday && !isCompleted) m.background(ChipNeutral, CircleShape) else m },
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            day.toString(),
                                            color = if (isCompleted) {
                                                Color.White
                                            } else if (day > today.dayOfMonth) {
                                                TextMuted
                                            } else {
                                                Ink
                                            },
                                            fontSize = TextSize.BodySmall,
                                            fontWeight = if (isToday) FontWeight.Bold else FontWeight.Medium
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                Spacer(Modifier.height(Space.Gutter))

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(Shapes.Medium)
                        .background(CardNeutral)
                        .clickable { showReminderPicker = true }
                        .padding(Space.L, Space.M),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(stringResource(R.string.routine_detail_daily_reminder), fontSize = TextSize.Body, fontWeight = FontWeight.SemiBold)
                        Text(
                            routine.reminderTime?.let { "Every day at ${formatClock(it)} · skipped once you're done" }
                                ?: stringResource(R.string.common_setup_off),
                            color = TextSecondary,
                            fontSize = TextSize.Caption
                        )
                    }
                    Text(stringResource(R.string.common_setup_change), color = TextSecondary, fontSize = TextSize.BodySmall, fontWeight = FontWeight.SemiBold)
                }

                Spacer(Modifier.height(Space.M))

                val doneToday = routine.isDoneOn(today)
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(Shapes.Pill)
                        .background(if (doneToday) ChipNeutral else Ink)
                        .clickable { RoutineStore.toggleToday(routine.id) }
                        .padding(vertical = Space.L),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        if (doneToday) {
                            stringResource(
                                R.string.routine_detail_mark_as_not_done_today
                            )
                        } else {
                            stringResource(R.string.routine_detail_mark_today_as_done)
                        },
                        color = if (doneToday) Ink else Color.White,
                        fontSize = TextSize.BodyLarge,
                        fontWeight = FontWeight.SemiBold
                    )
                }

                Text(
                    stringResource(R.string.routine_detail_delete_routine),
                    color = Maroon,
                    fontSize = TextSize.Body,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier
                        .align(Alignment.CenterHorizontally)
                        .padding(top = Space.L)
                        .clickable { showDeleteConfirm = true }
                )

                Spacer(Modifier.height(Space.L))
            }
        }

        if (showReminderPicker) {
            ReMakeUDialog(
                onDismissRequest = { showReminderPicker = false },
                title = "Daily reminder",
                primaryText = "Close",
                secondaryText = null,
                content = {
                    Column {
                        ReMakeUOptionRow("Off", selected = routine.reminderTime == null) { RoutineStore.setReminder(routine.id, null) }
                        routineReminderTimes.forEach { time ->
                            ReMakeUOptionRow(formatClock(time), selected = routine.reminderTime == time) {
                                RoutineStore.setReminder(routine.id, time)
                                askPermission()
                            }
                        }
                    }
                }
            )
        }

        if (showDeleteConfirm) {
            ReMakeUDialog(
                onDismissRequest = { showDeleteConfirm = false },
                title = "Delete this routine?",
                message = "Its streak and history will be removed. Points you've already earned stay.",
                secondaryText = "Cancel",
                primaryText = "Delete",
                primaryDestructive = true,
                onPrimary = {
                    showDeleteConfirm = false
                    navController.goBack()
                    RoutineStore.remove(routine.id)
                }
            )
        }
    }
}

private val routineReminderTimes = listOf(6, 7, 8, 9, 12, 18, 19, 20, 21, 22).map { LocalTime.of(it, 0) } +
    LocalTime.of(21, 30)
