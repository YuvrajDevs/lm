package com.glowup.app.ui.screens.learn

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import com.glowup.app.R
import com.glowup.app.ui.components.NavTab
import com.glowup.app.ui.components.ProBadge
import com.glowup.app.ui.components.ReMakeUBottomNav
import com.glowup.app.ui.components.ReMakeUDialog
import com.glowup.app.ui.data.ProGate
import com.glowup.app.ui.data.ProStore
import com.glowup.app.ui.data.program.Program
import com.glowup.app.ui.data.program.ProgramArea
import com.glowup.app.ui.data.program.ProgramSession
import com.glowup.app.ui.data.program.ProgramStore
import com.glowup.app.ui.data.program.SessionState
import com.glowup.app.ui.design.LineHeight
import com.glowup.app.ui.design.Shapes
import com.glowup.app.ui.design.Space
import com.glowup.app.ui.design.TextSize
import com.glowup.app.ui.theme.*

@Composable
fun LearnScreen(navController: NavHostController) {
    val next = ProgramStore.next
    val currentWeek = (next ?: Program.sessions.last()).week
    var expandedWeeks by remember { mutableStateOf(setOf(currentWeek)) }
    var lockedMessage by remember { mutableStateOf<String?>(null) }
    val streak = ProgramStore.streakDays()
    val done = ProgramStore.completedCount

    fun open(session: ProgramSession) {
        if (ProGate.sessionLocked(session.index)) {
            ProStore.showPaywall("Weeks 3–12 of the programme are part of Pro")
            return
        }
        when (ProgramStore.state(session.index)) {
            SessionState.DONE, SessionState.AVAILABLE -> navController.navigate("session/${session.index}")
            SessionState.LOCKED -> lockedMessage = "Day ${session.day} unlocks after you finish day ${session.day - 1}."
        }
    }

    Box(modifier = Modifier.fillMaxSize().background(Surface).systemBarsPadding()) {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(bottom = 110.dp)
        ) {
            item {
                Text(
                    stringResource(R.string.learn_your_programme),
                    style = MaterialTheme.typography.headlineMedium,
                    modifier = Modifier.padding(Space.Gutter, Space.Xxl, Space.Gutter, Space.Xs)
                )
                Text(
                    stringResource(R.string.learn_12_weeks_84_sessions_7),
                    color = TextSecondary,
                    fontSize = TextSize.BodySmall,
                    modifier = Modifier.padding(horizontal = Space.Gutter)
                )
                Spacer(Modifier.height(Space.L))
            }

            item {
                Column(
                    modifier = Modifier
                        .padding(horizontal = Space.Gutter)
                        .fillMaxWidth()
                        .clip(Shapes.Large)
                        .background(Ink)
                        .padding(Space.Xl, Space.L)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                if (ProgramStore.isFinished) {
                                    stringResource(
                                        R.string.learn_programme_complete
                                    )
                                } else {
                                    stringResource(R.string.learn_week_1_s_2_s, currentWeek, Program.phaseFor(currentWeek).uppercase())
                                },
                                color = Color.White.copy(alpha = 0.55f),
                                fontSize = TextSize.Micro,
                                fontWeight = FontWeight.SemiBold,
                                letterSpacing = 0.4.sp
                            )
                            Text(
                                stringResource(R.string.common_1_s_of_2_s, done, Program.TOTAL),
                                color = Color.White,
                                fontSize = TextSize.Headline,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        Column(horizontalAlignment = Alignment.End) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Image(painterResource(R.drawable.ic_flame), contentDescription = null, modifier = Modifier.size(20.dp))
                                Spacer(Modifier.width(Space.Xs))
                                Text("$streak", color = Color.White, fontSize = TextSize.TitleLarge, fontWeight = FontWeight.Bold)
                            }
                            Text(stringResource(R.string.common_day_streak), color = Color.White.copy(alpha = 0.55f), fontSize = TextSize.Micro)
                        }
                    }
                    Spacer(Modifier.height(Space.M))
                    ProgressBar(done.toFloat() / Program.TOTAL, Color.White, Color.White.copy(alpha = 0.15f))
                    Spacer(Modifier.height(Space.S))
                    Text(
                        if (done ==
                            0
                        ) {
                            stringResource(R.string.learn_do_your_first_session_to)
                        } else {
                            "Best streak ${ProgramStore.bestStreak().let { if (it == 1) "1 day" else "$it days" }} · ${Program.TOTAL - done} sessions to go"
                        },
                        color = Color.White.copy(alpha = 0.55f),
                        fontSize = TextSize.Caption
                    )
                }
                Spacer(Modifier.height(Space.M))
            }

            item {
                TodayCard(next, onOpen = { open(it) })
                Spacer(Modifier.height(Space.Xl))
            }

            item {
                Text(
                    stringResource(R.string.learn_areas),
                    fontSize = TextSize.Title,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.padding(horizontal = Space.Gutter)
                )
                Spacer(Modifier.height(Space.S))
                LazyRow(contentPadding = PaddingValues(horizontal = Space.Gutter), horizontalArrangement = Arrangement.spacedBy(Space.S)) {
                    items(ProgramArea.entries) { area -> AreaCard(area) }
                }
                Spacer(Modifier.height(Space.Xl))
            }

            item {
                Text(
                    stringResource(R.string.learn_milestones),
                    fontSize = TextSize.Title,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.padding(horizontal = Space.Gutter)
                )
                Spacer(Modifier.height(Space.S))
                Row(
                    modifier = Modifier.padding(horizontal = Space.Gutter).fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    (1..Program.WEEKS).forEach { week -> WeekBadge(week, ProgramStore.weekDone(week)) }
                }
                Spacer(Modifier.height(Space.Gutter))
            }

            (1..Program.WEEKS).forEach { week ->
                val expanded = week in expandedWeeks
                item(key = "week-$week") {
                    WeekHeader(week, expanded) {
                        expandedWeeks = if (expanded) expandedWeeks - week else expandedWeeks + week
                    }
                }
                if (expanded) {
                    items(Program.sessionsInWeek(week), key = { "session-${it.index}" }) { session ->
                        SessionRow(session, ProgramStore.state(session.index)) { open(session) }
                    }
                }
            }
        }

        Box(modifier = Modifier.align(Alignment.BottomCenter)) {
            ReMakeUBottomNav(navController, NavTab.LEARN)
        }

        lockedMessage?.let { message ->
            ReMakeUDialog(
                onDismissRequest = { lockedMessage = null },
                title = "Not unlocked yet",
                message = message,
                primaryText = "Got it",
                secondaryText = null
            )
        }
    }
}

@Composable
private fun TodayCard(next: ProgramSession?, onOpen: (ProgramSession) -> Unit) {
    if (next == null) {
        Column(
            modifier = Modifier
                .padding(horizontal = Space.Gutter)
                .fillMaxWidth()
                .clip(Shapes.Large)
                .background(GreenTint)
                .padding(Space.Xl)
        ) {
            Text(stringResource(R.string.learn_you_finished_all_84_sessions), fontSize = TextSize.Title, fontWeight = FontWeight.Bold)
            Text(stringResource(R.string.learn_revisit_any_session_below_the), color = TextSecondary, fontSize = TextSize.BodySmall)
        }
        return
    }
    val doneToday = ProgramStore.doneToday()
    Column(
        modifier = Modifier
            .padding(horizontal = Space.Gutter)
            .fillMaxWidth()
            .clip(Shapes.Large)
            .background(next.area.tint)
            .clickable { onOpen(next) }
            .padding(Space.Xl, Space.L)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    (if (doneToday) stringResource(R.string.learn_up_next_day_1_s, next.day) else stringResource(R.string.learn_today_day_1_s, next.day)) +
                        " · ${next.area.label.uppercase()}",
                    color = next.area.accent,
                    fontSize = TextSize.Micro,
                    fontWeight = FontWeight.SemiBold,
                    letterSpacing = 0.4.sp
                )
                Spacer(Modifier.height(Space.Xs))
                Text(next.title, fontSize = TextSize.TitleLarge, fontWeight = FontWeight.Bold, lineHeight = LineHeight.Title)
                Spacer(Modifier.height(Space.Xs))
                Text(next.hook, color = TextSecondary, fontSize = TextSize.BodySmall, lineHeight = LineHeight.Compact)
            }
            Image(painterResource(next.area.icon), contentDescription = null, modifier = Modifier.size(56.dp))
        }
        Spacer(Modifier.height(Space.M))
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(Space.S)) {
            Box(
                modifier = Modifier
                    .clip(Shapes.Pill)
                    .background(Ink)
                    .padding(Space.L, Space.S)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        Icons.Filled.PlayArrow,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(Modifier.width(Space.S))
                    Text(
                        if (doneToday) {
                            stringResource(
                                R.string.learn_keep_going_1_s_min,
                                next.minutes
                            )
                        } else {
                            stringResource(R.string.learn_start_1_s_min, next.minutes)
                        },
                        color = Color.White,
                        fontSize = TextSize.Body,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
            Text(
                stringResource(R.string.common_1_s_points, next.points),
                color = next.area.accent,
                fontSize = TextSize.BodySmall,
                fontWeight = FontWeight.SemiBold
            )
        }
    }
}

@Composable
private fun AreaCard(area: ProgramArea) {
    val done = ProgramStore.areaDone(area)
    val streak = ProgramStore.areaStreakWeeks(area)
    Column(
        modifier = Modifier
            .width(150.dp)
            .clip(Shapes.Card)
            .background(area.tint)
            .padding(Space.M)
    ) {
        Image(painterResource(area.icon), contentDescription = null, modifier = Modifier.size(30.dp))
        Spacer(Modifier.height(Space.S))
        Text(area.label, fontSize = TextSize.Body, fontWeight = FontWeight.SemiBold)
        Text(area.tagline, color = TextSecondary, fontSize = TextSize.Micro, lineHeight = LineHeight.Tight, minLines = 2, maxLines = 2)
        Spacer(Modifier.height(Space.S))
        ProgressBar(done / 12f, area.accent, Color.White)
        Spacer(Modifier.height(Space.S))
        Text(
            "$done/12 · " + if (streak > 0) "$streak-week streak" else "no streak yet",
            color = area.accent,
            fontSize = TextSize.Micro,
            fontWeight = FontWeight.SemiBold
        )
    }
}

@Composable
private fun WeekBadge(week: Int, done: Int) {
    val complete = done == Program.DAYS_PER_WEEK
    Box(
        modifier = Modifier
            .size(24.dp)
            .clip(CircleShape)
            .background(
                if (complete) {
                    Ink
                } else if (done > 0) {
                    YellowTint
                } else {
                    ChipNeutral
                }
            )
            .let { if (done in 1 until Program.DAYS_PER_WEEK) it.border(1.5.dp, Color(0xFFE0B800), CircleShape) else it },
        contentAlignment = Alignment.Center
    ) {
        if (complete) {
            Icon(
                Icons.Filled.Check,
                contentDescription = stringResource(R.string.learn_week_1_s_complete, week),
                tint = Color.White,
                modifier = Modifier.size(13.dp)
            )
        } else {
            Text("$week", color = if (done > 0) Ink else TextMuted, fontSize = TextSize.Micro, fontWeight = FontWeight.SemiBold, textAlign = TextAlign.Center)
        }
    }
}

@Composable
private fun WeekHeader(week: Int, expanded: Boolean, onToggle: () -> Unit) {
    val done = ProgramStore.weekDone(week)
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onToggle)
            .padding(Space.Gutter, Space.M),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(stringResource(R.string.learn_week_1_s, week), fontSize = TextSize.Title, fontWeight = FontWeight.SemiBold)
            Text(Program.phaseFor(week), color = TextSecondary, fontSize = TextSize.Caption)
        }
        Text(
            "$done/${Program.DAYS_PER_WEEK}",
            color = if (done ==
                Program.DAYS_PER_WEEK
            ) {
                Green
            } else {
                TextSecondary
            },
            fontSize = TextSize.BodySmall,
            fontWeight = FontWeight.SemiBold
        )
        Spacer(Modifier.width(Space.S))
        Icon(if (expanded) Icons.Filled.KeyboardArrowUp else Icons.Filled.KeyboardArrowDown, contentDescription = null, tint = TextSecondary)
    }
}

@Composable
private fun SessionRow(session: ProgramSession, state: SessionState, onClick: () -> Unit) {
    val locked = state == SessionState.LOCKED
    Row(
        modifier = Modifier
            .padding(Space.Gutter, Space.Xs)
            .fillMaxWidth()
            .clip(Shapes.Medium)
            .background(if (state == SessionState.AVAILABLE) session.area.tint else CardNeutral)
            .let { if (state == SessionState.AVAILABLE) it.border(2.dp, session.area.accent, Shapes.Medium) else it }
            .clickable(onClick = onClick)
            .padding(Space.M, Space.M),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(Space.M)
    ) {
        Box(
            modifier = Modifier
                .size(34.dp)
                .clip(CircleShape)
                .background(
                    when (state) {
                        SessionState.DONE -> session.area.accent
                        SessionState.AVAILABLE -> Ink
                        else -> ChipNeutral
                    }
                ),
            contentAlignment = Alignment.Center
        ) {
            when (state) {
                SessionState.DONE -> Icon(
                    Icons.Filled.Check,
                    contentDescription = stringResource(R.string.common_done),
                    tint = Color.White,
                    modifier = Modifier.size(18.dp)
                )
                SessionState.AVAILABLE -> Icon(
                    Icons.Filled.PlayArrow,
                    contentDescription = stringResource(R.string.common_start),
                    tint = Color.White,
                    modifier = Modifier.size(18.dp)
                )
                else -> Icon(Icons.Filled.Lock, contentDescription = stringResource(R.string.learn_locked), tint = TextMuted, modifier = Modifier.size(15.dp))
            }
        }
        Column(modifier = Modifier.weight(1f)) {
            Text(
                stringResource(R.string.learn_day_1_s_2_s, session.day, session.area.label.uppercase()),
                color = if (locked) TextMuted else session.area.accent,
                fontSize = TextSize.Micro,
                fontWeight = FontWeight.SemiBold,
                letterSpacing = 0.3.sp
            )
            Text(session.title, fontSize = TextSize.BodyLarge, fontWeight = FontWeight.SemiBold, color = if (locked) TextSecondary else Ink)
        }
        if (ProGate.sessionLocked(
                session.index
            )
        ) {
            ProBadge()
        } else {
            Text(stringResource(R.string.learn_1_sm, session.minutes), color = TextMuted, fontSize = TextSize.Caption)
        }
    }
}

@Composable
internal fun ProgressBar(fraction: Float, fill: Color, track: Color, height: Int = 6) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(height.dp)
            .clip(Shapes.Pill)
            .background(track)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth(fraction.coerceIn(0f, 1f))
                .fillMaxHeight()
                .clip(Shapes.Pill)
                .background(fill)
        )
    }
}
