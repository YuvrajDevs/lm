package com.glowup.app.ui.screens.routines

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import com.glowup.app.ui.components.goBack
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import com.glowup.app.ui.components.DetailHeader
import com.glowup.app.ui.components.GlowUpDialog
import com.glowup.app.ui.components.GlowUpOptionRow
import com.glowup.app.ui.components.StreakChip
import com.glowup.app.ui.components.WeekStreakDots
import com.glowup.app.ui.components.rememberNotificationPermission
import com.glowup.app.ui.data.RoutineStore
import com.glowup.app.ui.data.formatClock
import com.glowup.app.ui.theme.*
import java.time.LocalDate
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

            Column(modifier = Modifier.weight(1f).verticalScroll(rememberScrollState()).padding(24.dp, 18.dp, 24.dp, 24.dp)) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(26.dp))
                        .background(routine.tint)
                        .padding(20.dp, 18.dp),
                ) {
                    Column(modifier = Modifier.fillMaxWidth(0.7f)) {
                        Text(routine.area, color = routine.accent, fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
                        Text(routine.title, fontSize = 18.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(top = 4.dp, bottom = 12.dp))
                        StreakChip(routine.streakDays(today), routine.accent)
                        Spacer(Modifier.height(10.dp))
                        WeekStreakDots(routine.week(today), routine.accent)
                    }
                    Image(
                        painterResource(routine.icon),
                        contentDescription = null,
                        modifier = Modifier.align(Alignment.TopEnd).size(56.dp),
                    )
                }

                Spacer(Modifier.height(22.dp))

                Text(monthLabel, fontSize = 15.sp, fontWeight = FontWeight.SemiBold)
                Spacer(Modifier.height(12.dp))

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    listOf("M", "T", "W", "T", "F", "S", "S").forEach {
                        Text(it, color = TextSecondary, fontSize = 11.5.sp, fontWeight = FontWeight.Medium, modifier = Modifier.weight(1f), textAlign = androidx.compose.ui.text.style.TextAlign.Center)
                    }
                }
                Spacer(Modifier.height(8.dp))

                val cells = buildList {
                    repeat(leadingBlanks) { add(0) }
                    for (day in 1..daysInMonth) add(day)
                }
                cells.chunked(7).forEach { week ->
                    Row(modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp), horizontalArrangement = Arrangement.SpaceBetween) {
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
                                        contentAlignment = Alignment.Center,
                                    ) {
                                        Text(
                                            day.toString(),
                                            color = if (isCompleted) Color.White else if (day > today.dayOfMonth) TextMuted else Ink,
                                            fontSize = 12.5.sp,
                                            fontWeight = if (isToday) FontWeight.Bold else FontWeight.Medium,
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                Spacer(Modifier.height(24.dp))

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(18.dp))
                        .background(CardNeutral)
                        .clickable { showReminderPicker = true }
                        .padding(16.dp, 14.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Column {
                        Text("Daily reminder", fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
                        Text(
                            routine.reminderTime?.let { "Every day at ${formatClock(it)} · skipped once you're done" } ?: "Off",
                            color = TextSecondary,
                            fontSize = 12.sp,
                        )
                    }
                    Text("Change", color = TextSecondary, fontSize = 12.5.sp, fontWeight = FontWeight.SemiBold)
                }

                Spacer(Modifier.height(14.dp))

                val doneToday = routine.isDoneOn(today)
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(999.dp))
                        .background(if (doneToday) ChipNeutral else Ink)
                        .clickable { RoutineStore.toggleToday(routine.id) }
                        .padding(vertical = 16.dp),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        if (doneToday) "Mark as not done today" else "Mark today as done",
                        color = if (doneToday) Ink else Color.White,
                        fontSize = 14.5.sp,
                        fontWeight = FontWeight.SemiBold,
                    )
                }

                Text(
                    "Delete routine",
                    color = Maroon,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier
                        .align(Alignment.CenterHorizontally)
                        .padding(top = 18.dp)
                        .clickable { showDeleteConfirm = true },
                )

                Spacer(Modifier.height(16.dp))
            }
        }

        if (showReminderPicker) {
            GlowUpDialog(
                onDismissRequest = { showReminderPicker = false },
                title = "Daily reminder",
                primaryText = "Close",
                secondaryText = null,
                content = {
                    Column {
                        GlowUpOptionRow("Off", selected = routine.reminderTime == null) { RoutineStore.setReminder(routine.id, null) }
                        routineReminderTimes.forEach { time ->
                            GlowUpOptionRow(formatClock(time), selected = routine.reminderTime == time) {
                                RoutineStore.setReminder(routine.id, time)
                                askPermission()
                            }
                        }
                    }
                },
            )
        }

        if (showDeleteConfirm) {
            GlowUpDialog(
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
                },
            )
        }
    }
}

private val routineReminderTimes = listOf(6, 7, 8, 9, 12, 18, 19, 20, 21, 22).map { java.time.LocalTime.of(it, 0) } +
    java.time.LocalTime.of(21, 30)
