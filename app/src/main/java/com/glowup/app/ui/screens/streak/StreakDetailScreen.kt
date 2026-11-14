package com.glowup.app.ui.screens.streak

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AcUnit
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
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
import com.glowup.app.ui.components.goBack
import com.glowup.app.ui.data.MoodStore
import com.glowup.app.ui.data.ProStore
import com.glowup.app.ui.data.ProgressStore
import com.glowup.app.ui.design.LineHeight
import com.glowup.app.ui.design.Shapes
import com.glowup.app.ui.design.Space
import com.glowup.app.ui.design.TextSize
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
    val todayLogged = MoodStore.moodOn(today) != null
    val streak = ProgressStore.streakDays(today)

    Box(modifier = Modifier.fillMaxSize().background(Surface).systemBarsPadding()) {
        Column(modifier = Modifier.fillMaxSize()) {
            DetailHeader(stringResource(R.string.streak_detail_your_streak)) { navController.goBack() }

            Column(modifier = Modifier.weight(1f).verticalScroll(rememberScrollState()).padding(Space.Gutter, Space.L, Space.Gutter, Space.Gutter)) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(Shapes.Large)
                        .background(Ink)
                        .padding(Space.Xl, Space.L)
                ) {
                    Column {
                        Text(
                            stringResource(R.string.streak_detail_1_s_points_this_week, ProgressStore.pointsThisWeek(today)),
                            color = Color.White.copy(alpha = 0.55f),
                            fontSize = TextSize.Caption
                        )
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                if (streak ==
                                    1
                                ) {
                                    stringResource(R.string.streak_detail_1_day_streak)
                                } else {
                                    stringResource(R.string.streak_detail_1_s_day_streak, streak)
                                },
                                color = Color.White,
                                fontSize = TextSize.TitleLarge,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(Modifier.width(Space.S))
                            Image(painterResource(R.drawable.ic_flame), contentDescription = null, modifier = Modifier.size(20.dp))
                        }
                        Text(
                            stringResource(
                                R.string.streak_detail_1_s_points_level_2,
                                ProgressStore.points,
                                ProgressStore.level,
                                ProgressStore.POINTS_PER_LEVEL - ProgressStore.pointsIntoLevel
                            ),
                            color = Color.White.copy(alpha = 0.65f),
                            fontSize = TextSize.BodySmall
                        )
                    }
                    Image(painterResource(R.drawable.ic_streak), contentDescription = null, modifier = Modifier.align(Alignment.BottomEnd).size(58.dp))
                }

                Spacer(Modifier.height(Space.M))
                FreezeCard(today)

                Spacer(Modifier.height(Space.Xl))

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                    Text(monthLabel, fontSize = TextSize.BodyLarge, fontWeight = FontWeight.SemiBold)
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(Modifier.size(8.dp).clip(CircleShape).background(Green))
                        Text(stringResource(R.string.streak_detail_active), color = TextSecondary, fontSize = TextSize.Caption, fontWeight = FontWeight.Medium)
                        Box(Modifier.size(8.dp).clip(CircleShape).background(Blue))
                        Text(stringResource(R.string.streak_detail_frozen), color = TextSecondary, fontSize = TextSize.Caption, fontWeight = FontWeight.Medium)
                    }
                }
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
                                    Spacer(Modifier.size(34.dp))
                                } else {
                                    val isToday = day == today.dayOfMonth
                                    val date = yearMonth.atDay(day)
                                    val mood = MoodStore.moodOn(date)
                                    val icon = mood?.let { MoodStore.iconFor(it) }
                                    val active = ProgressStore.isActive(date)
                                    val frozen = ProgressStore.isFrozen(date)
                                    Box(
                                        modifier = Modifier
                                            .size(34.dp)
                                            .clip(CircleShape)
                                            .background(
                                                when {
                                                    active && icon == null -> GreenTint
                                                    frozen -> BlueTint
                                                    isToday -> PurpleTint
                                                    else -> Color.Transparent
                                                }
                                            )
                                            .clickable(enabled = isToday) { navController.navigate("mood_checkin") },
                                        contentAlignment = Alignment.Center
                                    ) {
                                        if (icon != null) {
                                            Image(painterResource(icon), contentDescription = mood, modifier = Modifier.size(26.dp))
                                        } else if (frozen) {
                                            Icon(
                                                Icons.Filled.AcUnit,
                                                contentDescription = stringResource(R.string.streak_detail_frozen_day),
                                                tint = Blue,
                                                modifier = Modifier.size(16.dp)
                                            )
                                        } else {
                                            Text(
                                                day.toString(),
                                                color = if (day > today.dayOfMonth) TextMuted else Ink,
                                                fontSize = TextSize.BodySmall,
                                                fontWeight = if (isToday) FontWeight.Bold else FontWeight.Medium
                                            )
                                        }
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
                        .padding(bottom = Space.S)
                        .clip(Shapes.Card)
                        .background(PurpleTint)
                        .clickable { navController.navigate("mood_insights") }
                        .padding(Space.L, Space.L),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(stringResource(R.string.streak_detail_mood_patterns_amp_year_in), fontSize = TextSize.BodyLarge, fontWeight = FontWeight.SemiBold)
                        Text(stringResource(R.string.streak_detail_see_what_lifts_your_mood), color = PurpleDeep, fontSize = TextSize.Caption)
                    }
                    Icon(Icons.Filled.ChevronRight, contentDescription = null, tint = Ink, modifier = Modifier.size(18.dp))
                }

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(Shapes.Pill)
                        .background(if (todayLogged) ChipNeutral else Ink)
                        .clickable { navController.navigate("mood_checkin") }
                        .padding(vertical = Space.L),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        if (todayLogged) {
                            stringResource(
                                R.string.streak_detail_update_today_s_mood
                            )
                        } else {
                            stringResource(R.string.streak_detail_log_today_s_mood)
                        },
                        color = if (todayLogged) Ink else Color.White,
                        fontSize = TextSize.BodyLarge,
                        fontWeight = FontWeight.SemiBold
                    )
                }

                Spacer(Modifier.height(Space.L))
            }
        }
    }
}

@Composable
private fun FreezeCard(today: LocalDate) {
    val banked = ProgressStore.freezes
    val max = ProgressStore.maxFreezes
    val next = ProgressStore.daysToNextFreeze(today)
    Column(modifier = Modifier.fillMaxWidth().clip(Shapes.Card).background(BlueTint).padding(Space.L, Space.L)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Column(modifier = Modifier.weight(1f)) {
                Text(stringResource(R.string.common_streak_freezes), fontSize = TextSize.BodyLarge, fontWeight = FontWeight.Bold)
                Text(
                    when {
                        next == null -> "Bank full — you're covered for $banked missed day${if (banked == 1) "" else "s"}"
                        // When today isn't done yet, today is one of the days still needed.
                        !ProgressStore.isActive(today) && next == 1 -> "Stay active today to earn one"
                        ProgressStore.isActive(today) && next == 1 -> "Earn one tomorrow"
                        else -> "Next freeze in $next days"
                    },
                    color = Navy,
                    fontSize = TextSize.BodySmall,
                    fontWeight = FontWeight.Medium
                )
            }
            Row(horizontalArrangement = Arrangement.spacedBy(Space.S)) {
                repeat(max) { i ->
                    val filled = i < banked
                    Box(
                        Modifier.size(34.dp).clip(CircleShape).background(if (filled) Blue else Color.White),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Filled.AcUnit, contentDescription = null, tint = if (filled) Color.White else TextMuted, modifier = Modifier.size(17.dp))
                    }
                }
            }
        }
        Spacer(Modifier.height(Space.S))
        Text(
            stringResource(R.string.streak_detail_every_7_days_in_a) +
                if (!ProStore.isPro) stringResource(R.string.streak_detail_pro_banks_up_to_1, ProgressStore.PRO_MAX_FREEZES) else "",
            color = TextSecondary,
            fontSize = TextSize.Caption,
            lineHeight = LineHeight.Compact
        )
    }
}
