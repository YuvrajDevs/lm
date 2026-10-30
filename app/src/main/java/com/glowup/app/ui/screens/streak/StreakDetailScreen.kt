package com.glowup.app.ui.screens.streak

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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import com.glowup.app.R
import com.glowup.app.ui.components.DetailHeader
import com.glowup.app.ui.data.MoodStore
import com.glowup.app.ui.theme.*
import java.time.LocalDate
import java.time.YearMonth
import java.time.format.TextStyle
import java.util.Locale

@Composable
fun StreakDetailScreen(navController: NavHostController) {
    val today = LocalDate.now()
    val yearMonth = YearMonth.from(today)
    val monthLabel = yearMonth.month.getDisplayName(TextStyle.FULL, Locale.getDefault()) + " " + yearMonth.year
    val daysInMonth = yearMonth.lengthOfMonth()
    val leadingBlanks = yearMonth.atDay(1).dayOfWeek.value - 1
    val todayLogged = MoodStore.moodLog.containsKey(today.dayOfMonth)

    Box(modifier = Modifier.fillMaxSize().background(Surface).systemBarsPadding()) {
        Column(modifier = Modifier.fillMaxSize()) {
            DetailHeader("Your streak") { navController.popBackStack() }

            Column(modifier = Modifier.weight(1f).verticalScroll(rememberScrollState()).padding(24.dp, 18.dp, 24.dp, 24.dp)) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(26.dp))
                        .background(Ink)
                        .padding(20.dp, 18.dp),
                ) {
                    Column {
                        Text("This week", color = Color.White.copy(alpha = 0.55f), fontSize = 11.5.sp)
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text("${MoodStore.streakDays}-day streak", color = Color.White, fontSize = 22.sp, fontWeight = FontWeight.Bold)
                            Spacer(Modifier.width(6.dp))
                            Image(painterResource(R.drawable.ic_flame), contentDescription = null, modifier = Modifier.size(20.dp))
                        }
                        Text("${MoodStore.points} points · Level ${MoodStore.level}", color = Color.White.copy(alpha = 0.65f), fontSize = 12.5.sp)
                    }
                    Image(painterResource(R.drawable.ic_streak), contentDescription = null, modifier = Modifier.align(Alignment.BottomEnd).size(58.dp))
                }

                Spacer(Modifier.height(22.dp))

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                    Text(monthLabel, fontSize = 15.sp, fontWeight = FontWeight.SemiBold)
                    Text("Mood log", color = TextSecondary, fontSize = 12.sp, fontWeight = FontWeight.Medium)
                }
                Spacer(Modifier.height(12.dp))

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    listOf("M", "T", "W", "T", "F", "S", "S").forEach {
                        Text(it, color = TextSecondary, fontSize = 11.5.sp, fontWeight = FontWeight.Medium, modifier = Modifier.weight(1f), textAlign = TextAlign.Center)
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
                                    Spacer(Modifier.size(34.dp))
                                } else {
                                    val isToday = day == today.dayOfMonth
                                    val mood = MoodStore.moodLog[day]
                                    val icon = mood?.let { MoodStore.iconFor(it) }
                                    Box(
                                        modifier = Modifier
                                            .size(34.dp)
                                            .clip(CircleShape)
                                            .background(if (isToday) PurpleTint else Color.Transparent)
                                            .clickable(enabled = isToday) { navController.navigate("mood_checkin") },
                                        contentAlignment = Alignment.Center,
                                    ) {
                                        if (icon != null) {
                                            Image(painterResource(icon), contentDescription = mood, modifier = Modifier.size(26.dp))
                                        } else {
                                            Text(
                                                day.toString(),
                                                color = if (day > today.dayOfMonth) TextMuted else Ink,
                                                fontSize = 12.5.sp,
                                                fontWeight = if (isToday) FontWeight.Bold else FontWeight.Medium,
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                Spacer(Modifier.height(24.dp))

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(999.dp))
                        .background(if (todayLogged) ChipNeutral else Ink)
                        .clickable { navController.navigate("mood_checkin") }
                        .padding(vertical = 16.dp),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        if (todayLogged) "Update today's mood" else "Log today's mood",
                        color = if (todayLogged) Ink else Color.White,
                        fontSize = 14.5.sp,
                        fontWeight = FontWeight.SemiBold,
                    )
                }

                Spacer(Modifier.height(16.dp))
            }
        }
    }
}
