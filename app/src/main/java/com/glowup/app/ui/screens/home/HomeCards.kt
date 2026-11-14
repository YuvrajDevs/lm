package com.glowup.app.ui.screens.home

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AcUnit
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import com.glowup.app.R
import com.glowup.app.ui.data.FocusStore
import com.glowup.app.ui.data.MoodStore
import com.glowup.app.ui.data.ProGate
import com.glowup.app.ui.data.ProStore
import com.glowup.app.ui.data.ProgressStore
import com.glowup.app.ui.data.SleepLogStore
import com.glowup.app.ui.data.fitness.FitnessStore
import com.glowup.app.ui.data.formatDuration
import com.glowup.app.ui.data.glow.GlowCheckStore
import com.glowup.app.ui.data.program.Program
import com.glowup.app.ui.data.program.ProgramStore
import com.glowup.app.ui.design.LineHeight
import com.glowup.app.ui.design.Shapes
import com.glowup.app.ui.design.Space
import com.glowup.app.ui.design.TextSize
import com.glowup.app.ui.theme.*
import java.time.LocalDate

@Composable
internal fun GlowCheckCard(navController: NavHostController) {
    val latest = GlowCheckStore.latest
    val due = GlowCheckStore.daysUntilRescan()
    if (latest == null) {
        Row(
            modifier = Modifier.fillMaxWidth().clip(Shapes.Hero).background(Ink).clickable { navController.navigate("glow_check") }.padding(Space.Xl, Space.L),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(Modifier.weight(1f)) {
                Text(
                    stringResource(R.string.home_your_personal_plan),
                    color = Color.White.copy(alpha = 0.55f),
                    fontSize = TextSize.Micro,
                    fontWeight = FontWeight.SemiBold,
                    letterSpacing = 0.4.sp
                )
                Text(stringResource(R.string.home_take_the_glow_check), color = Color.White, fontSize = TextSize.TitleLarge, fontWeight = FontWeight.Bold)
                Text(
                    stringResource(R.string.home_3_minutes_find_what_will),
                    color = Color.White.copy(alpha = 0.65f),
                    fontSize = TextSize.BodySmall,
                    lineHeight = LineHeight.Compact
                )
            }
            Spacer(Modifier.width(Space.S))
            Image(painterResource(R.drawable.ic_streak), contentDescription = null, modifier = Modifier.size(54.dp))
        }
    } else {
        Row(
            modifier = Modifier.fillMaxWidth().clip(Shapes.Card).background(CardNeutral).clickable {
                navController.navigate("glow_results")
            }.padding(Space.L, Space.M),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(Modifier.weight(1f)) {
                Text(stringResource(R.string.home_your_glow_map), fontSize = TextSize.BodyLarge, fontWeight = FontWeight.Bold)
                Text(
                    latest.focus.joinToString(" · ") { it.area.title }.ifBlank { "All areas looking good" },
                    color = TextSecondary,
                    fontSize = TextSize.BodySmall,
                    maxLines = 1
                )
            }
            Text(
                if (due == 0) stringResource(R.string.home_rescan_due) else stringResource(R.string.home_rescan_in_1_s_d, due.toString()),
                color = if (due == 0) Color.White else Ink,
                fontSize = TextSize.Caption,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.clip(Shapes.Pill).background(if (due == 0) Coral else Color.White).padding(Space.S, Space.Xs)
            )
        }
    }
}

@Composable
internal fun TodaySessionCard(navController: NavHostController) {
    val next = ProgramStore.next ?: return
    val doneToday = ProgramStore.doneToday()
    val locked = ProGate.sessionLocked(next.index)
    val learningStreak = ProgramStore.streakDays()
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(Shapes.Hero)
            .background(next.area.tint)
            .clickable {
                if (locked) {
                    ProStore.showPaywall("You've finished the free weeks — keep going with the full programme")
                } else {
                    navController.navigate("session/${next.index}")
                }
            }
            .padding(Space.Xl, Space.L)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    (
                        if (doneToday) {
                            stringResource(
                                R.string.home_up_next
                            )
                        } else {
                            stringResource(R.string.common_today)
                        }
                        ) + " · DAY ${next.day} OF ${Program.TOTAL}",
                    color = next.area.accent,
                    fontSize = TextSize.Micro,
                    fontWeight = FontWeight.SemiBold,
                    letterSpacing = 0.4.sp
                )
                Spacer(Modifier.height(Space.Xs))
                Text(next.title, fontSize = TextSize.TitleLarge, fontWeight = FontWeight.Bold, lineHeight = LineHeight.Title)
                Spacer(Modifier.height(Space.Xs))
                Text(
                    stringResource(R.string.home_1_s_2_s_min, next.area.label, next.minutes, next.points),
                    color = TextSecondary,
                    fontSize = TextSize.BodySmall
                )
            }
            Spacer(Modifier.width(Space.M))
            Image(painterResource(next.area.icon), contentDescription = null, modifier = Modifier.size(52.dp))
        }
        Spacer(Modifier.height(Space.M))
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier.clip(Shapes.Pill).background(Ink).padding(Space.L, Space.S)
            ) {
                Text(
                    if (locked) {
                        stringResource(R.string.common_unlock_with_pro)
                    } else if (doneToday) {
                        stringResource(R.string.home_keep_going)
                    } else {
                        stringResource(R.string.home_start_session)
                    },
                    color = Color.White,
                    fontSize = TextSize.Body,
                    fontWeight = FontWeight.SemiBold
                )
            }
            Spacer(Modifier.width(Space.M))
            when {
                doneToday -> {
                    Image(painterResource(R.drawable.ic_check), contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(Modifier.width(Space.Xs))
                    Text(stringResource(R.string.home_today_s_streak_secured), color = Green, fontSize = TextSize.BodySmall, fontWeight = FontWeight.SemiBold)
                }
                learningStreak > 0 -> {
                    Image(painterResource(R.drawable.ic_flame), contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(Modifier.width(Space.Xs))
                    Text(
                        stringResource(R.string.home_keep_your_1_s_day, learningStreak),
                        color = Coral,
                        fontSize = TextSize.BodySmall,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
        }
    }
}

/** Today's quick check-ins across the app, each showing whether it's done. */
@Composable
internal fun CheckInsRow(navController: NavHostController) {
    val today = LocalDate.now()
    val fitness = FitnessStore
    data class Chip(val label: String, val sub: String, val done: Boolean, val route: String, val tint: Color)
    val chips = buildList {
        add(Chip(stringResource(R.string.common_mood), MoodStore.moodOn(today) ?: "Check in", MoodStore.moodOn(today) != null, "mood_checkin", GreenTint))
        val night = SleepLogStore.on(today)
        add(Chip(stringResource(R.string.common_sleep), night?.let { formatDuration(it.minutes) } ?: "Log night", night != null, "sleep_log", PurpleTint))
        if (fitness.setUp) {
            val done = fitness.doneOn(today)
            add(
                Chip(
                    stringResource(R.string.common_workout_preview_workout),
                    if (done) {
                        "Done"
                    } else if (fitness.isTrainingDay(today) ||
                        fitness.activeProgramme != null
                    ) {
                        "Today"
                    } else {
                        "Optional"
                    },
                    done,
                    "fitness",
                    CoralTint
                )
            )
            if (fitness.weighInDue() &&
                fitness.latestBody() != null
            ) {
                add(Chip(stringResource(R.string.home_weigh_in), "Due", false, "body_checkin", YellowTint))
            }
        } else {
            add(Chip(stringResource(R.string.common_workout_preview_workout), "Set up", false, "fitness", CoralTint))
        }
    }

    @Composable
    fun CheckChip(c: Chip, modifier: Modifier) {
        Row(
            modifier.clip(Shapes.Medium).background(c.tint).clickable { navController.navigate(c.route) }.padding(Space.S, Space.S),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(Modifier.size(20.dp).clip(CircleShape).background(if (c.done) Ink else Color.White), contentAlignment = Alignment.Center) {
                if (c.done) {
                    Icon(
                        Icons.Filled.Check,
                        contentDescription = stringResource(R.string.common_done),
                        tint = Color.White,
                        modifier = Modifier.size(14.dp)
                    )
                }
            }
            Spacer(Modifier.width(Space.S))
            Column {
                Text(c.label, fontSize = TextSize.BodySmall, fontWeight = FontWeight.Bold, maxLines = 1)
                Text(c.sub, color = TextSecondary, fontSize = TextSize.Caption, maxLines = 1)
            }
        }
    }
    // Up to three check-ins share the width evenly; a fourth (or large system text) turns the row into a scroller.
    if (chips.size <= 3 && LocalDensity.current.fontScale <= 1.15f) {
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(Space.S)) {
            chips.forEach { CheckChip(it, Modifier.weight(1f)) }
        }
    } else {
        LazyRow(horizontalArrangement = Arrangement.spacedBy(Space.S)) {
            items(chips) { CheckChip(it, Modifier) }
        }
    }
}

@Composable
internal fun StreakTile(streak: Int, modifier: Modifier, onClick: () -> Unit) {
    Column(
        modifier = modifier
            .clip(Shapes.Large)
            .background(Ink)
            .clickable(onClick = onClick)
            .padding(Space.L)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Image(painterResource(R.drawable.ic_flame), contentDescription = null, modifier = Modifier.size(18.dp))
            Spacer(Modifier.width(Space.Xs))
            Text("$streak", color = Color.White, fontSize = TextSize.TitleLarge, fontWeight = FontWeight.Bold)
        }
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(stringResource(R.string.common_day_streak), color = Color.White.copy(alpha = 0.6f), fontSize = TextSize.Caption)
            if (ProgressStore.freezes > 0) {
                Spacer(Modifier.width(Space.S))
                Icon(Icons.Filled.AcUnit, contentDescription = stringResource(R.string.common_streak_freezes), tint = Sky, modifier = Modifier.size(12.dp))
                Text("${ProgressStore.freezes}", color = Sky, fontSize = TextSize.Caption, fontWeight = FontWeight.Bold)
            }
        }
        Spacer(Modifier.height(Space.S))
        Text(
            stringResource(R.string.home_level_1_s_2_s, ProgressStore.level, ProgressStore.points),
            color = Color.White,
            fontSize = TextSize.BodySmall,
            fontWeight = FontWeight.SemiBold
        )
    }
}

@Composable
internal fun FocusTile(modifier: Modifier, onClick: () -> Unit) {
    val active = FocusStore.active
    val focusStreak = FocusStore.streakDays()
    Column(
        modifier = modifier
            .clip(Shapes.Large)
            .background(BlueTint)
            .clickable(onClick = onClick)
            .padding(Space.L)
    ) {
        Text(
            when {
                active == null -> stringResource(R.string.common_1_s_min, FocusStore.minutesOn(LocalDate.now()))
                active.isRunning -> stringResource(R.string.home_focusing)
                else -> stringResource(R.string.home_paused)
            },
            fontSize = TextSize.TitleLarge,
            fontWeight = FontWeight.Bold,
            color = Navy
        )
        Text(if (active == null) stringResource(R.string.home_focused_today) else active.label, color = TextSecondary, fontSize = TextSize.Caption)
        Spacer(Modifier.height(Space.S))
        Text(
            if (active !=
                null
            ) {
                stringResource(R.string.home_tap_to_return)
            } else if (focusStreak >
                0
            ) {
                stringResource(R.string.home_1_s_day_focus_streak, focusStreak)
            } else {
                stringResource(R.string.home_start_a_focus_block)
            },
            color = Navy,
            fontSize = TextSize.BodySmall,
            fontWeight = FontWeight.SemiBold
        )
    }
}

@Composable
internal fun FreezeBanner(title: String, text: String, onDismiss: () -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth().clip(Shapes.Card).background(BlueTint).padding(Space.L, Space.M),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(Modifier.size(38.dp).clip(CircleShape).background(Blue), contentAlignment = Alignment.Center) {
            Icon(Icons.Filled.AcUnit, contentDescription = null, tint = Color.White, modifier = Modifier.size(19.dp))
        }
        Spacer(Modifier.width(Space.M))
        Column(modifier = Modifier.weight(1f)) {
            Text(title, fontSize = TextSize.BodyLarge, fontWeight = FontWeight.Bold)
            Text(text, color = Navy, fontSize = TextSize.BodySmall, lineHeight = LineHeight.Compact)
        }
        Box(
            Modifier.size(32.dp).clip(CircleShape).clickable(onClick = onDismiss),
            contentAlignment = Alignment.Center
        ) {
            Icon(Icons.Filled.Close, contentDescription = stringResource(R.string.home_dismiss), tint = Navy, modifier = Modifier.size(16.dp))
        }
    }
}
