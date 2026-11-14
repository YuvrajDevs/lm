package com.glowup.app.ui.screens.learn

import com.glowup.app.ui.components.ProBadge
import com.glowup.app.ui.data.ProStore
import com.glowup.app.ui.data.ProGate
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import com.glowup.app.R
import com.glowup.app.ui.components.ReMakeUBottomNav
import com.glowup.app.ui.components.ReMakeUDialog
import com.glowup.app.ui.components.NavTab
import com.glowup.app.ui.data.program.Program
import com.glowup.app.ui.data.program.ProgramArea
import com.glowup.app.ui.data.program.ProgramSession
import com.glowup.app.ui.data.program.ProgramStore
import com.glowup.app.ui.data.program.SessionState
import com.glowup.app.ui.theme.*
import com.glowup.app.ui.design.LineHeight
import com.glowup.app.ui.design.Shapes
import com.glowup.app.ui.design.TextSize

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
            contentPadding = PaddingValues(bottom = 110.dp),
        ) {
            item {
                Text(
                    "Your programme",
                    style = MaterialTheme.typography.headlineMedium,
                    modifier = Modifier.padding(24.dp, 32.dp, 24.dp, 4.dp),
                )
                Text(
                    "12 weeks · 84 sessions · 7 areas",
                    color = TextSecondary,
                    fontSize = TextSize.BodySmall,
                    modifier = Modifier.padding(horizontal = 24.dp),
                )
                Spacer(Modifier.height(16.dp))
            }

            item {
                Column(
                    modifier = Modifier
                        .padding(horizontal = 24.dp)
                        .fillMaxWidth()
                        .clip(Shapes.Large)
                        .background(Ink)
                        .padding(20.dp, 18.dp),
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                if (ProgramStore.isFinished) "PROGRAMME COMPLETE" else "WEEK $currentWeek · ${Program.phaseFor(currentWeek).uppercase()}",
                                color = Color.White.copy(alpha = 0.55f),
                                fontSize = TextSize.Micro,
                                fontWeight = FontWeight.SemiBold,
                                letterSpacing = 0.4.sp,
                            )
                            Text("$done of ${Program.TOTAL} done", color = Color.White, fontSize = TextSize.Headline, fontWeight = FontWeight.Bold)
                        }
                        Column(horizontalAlignment = Alignment.End) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Image(painterResource(R.drawable.ic_flame), contentDescription = null, modifier = Modifier.size(20.dp))
                                Spacer(Modifier.width(4.dp))
                                Text("$streak", color = Color.White, fontSize = TextSize.TitleLarge, fontWeight = FontWeight.Bold)
                            }
                            Text("day streak", color = Color.White.copy(alpha = 0.55f), fontSize = TextSize.Micro)
                        }
                    }
                    Spacer(Modifier.height(14.dp))
                    ProgressBar(done.toFloat() / Program.TOTAL, Color.White, Color.White.copy(alpha = 0.15f))
                    Spacer(Modifier.height(8.dp))
                    Text(
                        if (done == 0) "Do your first session to start your streak" else "Best streak ${ProgramStore.bestStreak().let { if (it == 1) "1 day" else "$it days" }} · ${Program.TOTAL - done} sessions to go",
                        color = Color.White.copy(alpha = 0.55f),
                        fontSize = TextSize.Caption,
                    )
                }
                Spacer(Modifier.height(14.dp))
            }

            item {
                TodayCard(next, onOpen = { open(it) })
                Spacer(Modifier.height(22.dp))
            }

            item {
                Text("Areas", fontSize = TextSize.Title, fontWeight = FontWeight.SemiBold, modifier = Modifier.padding(horizontal = 24.dp))
                Spacer(Modifier.height(10.dp))
                LazyRow(contentPadding = PaddingValues(horizontal = 24.dp), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    items(ProgramArea.entries) { area -> AreaCard(area) }
                }
                Spacer(Modifier.height(22.dp))
            }

            item {
                Text("Milestones", fontSize = TextSize.Title, fontWeight = FontWeight.SemiBold, modifier = Modifier.padding(horizontal = 24.dp))
                Spacer(Modifier.height(10.dp))
                Row(
                    modifier = Modifier.padding(horizontal = 24.dp).fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                ) {
                    (1..Program.WEEKS).forEach { week -> WeekBadge(week, ProgramStore.weekDone(week)) }
                }
                Spacer(Modifier.height(24.dp))
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
                secondaryText = null,
            )
        }
    }
}

@Composable
private fun TodayCard(next: ProgramSession?, onOpen: (ProgramSession) -> Unit) {
    if (next == null) {
        Column(
            modifier = Modifier
                .padding(horizontal = 24.dp)
                .fillMaxWidth()
                .clip(Shapes.Large)
                .background(GreenTint)
                .padding(20.dp),
        ) {
            Text("You finished all 84 sessions", fontSize = TextSize.Title, fontWeight = FontWeight.Bold)
            Text("Revisit any session below — the drills work even better the second time.", color = TextSecondary, fontSize = TextSize.BodySmall)
        }
        return
    }
    val doneToday = ProgramStore.doneToday()
    Column(
        modifier = Modifier
            .padding(horizontal = 24.dp)
            .fillMaxWidth()
            .clip(Shapes.Large)
            .background(next.area.tint)
            .clickable { onOpen(next) }
            .padding(20.dp, 18.dp),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    (if (doneToday) "UP NEXT · DAY ${next.day}" else "TODAY · DAY ${next.day}") + " · ${next.area.label.uppercase()}",
                    color = next.area.accent,
                    fontSize = TextSize.Micro,
                    fontWeight = FontWeight.SemiBold,
                    letterSpacing = 0.4.sp,
                )
                Spacer(Modifier.height(4.dp))
                Text(next.title, fontSize = TextSize.TitleLarge, fontWeight = FontWeight.Bold, lineHeight = LineHeight.Title)
                Spacer(Modifier.height(4.dp))
                Text(next.hook, color = TextSecondary, fontSize = TextSize.BodySmall, lineHeight = LineHeight.Compact)
            }
            Image(painterResource(next.area.icon), contentDescription = null, modifier = Modifier.size(56.dp))
        }
        Spacer(Modifier.height(14.dp))
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            Box(
                modifier = Modifier
                    .clip(Shapes.Pill)
                    .background(Ink)
                    .padding(18.dp, 10.dp),
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        Icons.Filled.PlayArrow,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(16.dp),
                    )
                    Spacer(Modifier.width(6.dp))
                    Text(
                        if (doneToday) "Keep going · ${next.minutes} min" else "Start · ${next.minutes} min",
                        color = Color.White,
                        fontSize = TextSize.Body,
                        fontWeight = FontWeight.SemiBold,
                    )
                }
            }
            Text("+${next.points} points", color = next.area.accent, fontSize = TextSize.BodySmall, fontWeight = FontWeight.SemiBold)
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
            .padding(14.dp),
    ) {
        Image(painterResource(area.icon), contentDescription = null, modifier = Modifier.size(30.dp))
        Spacer(Modifier.height(8.dp))
        Text(area.label, fontSize = TextSize.Body, fontWeight = FontWeight.SemiBold)
        Text(area.tagline, color = TextSecondary, fontSize = TextSize.Micro, lineHeight = LineHeight.Tight, minLines = 2, maxLines = 2)
        Spacer(Modifier.height(10.dp))
        ProgressBar(done / 12f, area.accent, Color.White)
        Spacer(Modifier.height(6.dp))
        Text(
            "$done/12 · " + if (streak > 0) "$streak-week streak" else "no streak yet",
            color = area.accent,
            fontSize = TextSize.Micro,
            fontWeight = FontWeight.SemiBold,
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
            .background(if (complete) Ink else if (done > 0) YellowTint else ChipNeutral)
            .let { if (done in 1 until Program.DAYS_PER_WEEK) it.border(1.5.dp, Color(0xFFE0B800), CircleShape) else it },
        contentAlignment = Alignment.Center,
    ) {
        if (complete) {
            Icon(Icons.Filled.Check, contentDescription = "Week $week complete", tint = Color.White, modifier = Modifier.size(13.dp))
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
            .padding(24.dp, 12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text("Week $week", fontSize = TextSize.Title, fontWeight = FontWeight.SemiBold)
            Text(Program.phaseFor(week), color = TextSecondary, fontSize = TextSize.Caption)
        }
        Text("$done/${Program.DAYS_PER_WEEK}", color = if (done == Program.DAYS_PER_WEEK) Green else TextSecondary, fontSize = TextSize.BodySmall, fontWeight = FontWeight.SemiBold)
        Spacer(Modifier.width(8.dp))
        Icon(if (expanded) Icons.Filled.KeyboardArrowUp else Icons.Filled.KeyboardArrowDown, contentDescription = null, tint = TextSecondary)
    }
}

@Composable
private fun SessionRow(session: ProgramSession, state: SessionState, onClick: () -> Unit) {
    val locked = state == SessionState.LOCKED
    Row(
        modifier = Modifier
            .padding(24.dp, 4.dp)
            .fillMaxWidth()
            .clip(Shapes.Medium)
            .background(if (state == SessionState.AVAILABLE) session.area.tint else CardNeutral)
            .let { if (state == SessionState.AVAILABLE) it.border(2.dp, session.area.accent, Shapes.Medium) else it }
            .clickable(onClick = onClick)
            .padding(14.dp, 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
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
                    },
                ),
            contentAlignment = Alignment.Center,
        ) {
            when (state) {
                SessionState.DONE -> Icon(Icons.Filled.Check, contentDescription = "Done", tint = Color.White, modifier = Modifier.size(18.dp))
                SessionState.AVAILABLE -> Icon(Icons.Filled.PlayArrow, contentDescription = "Start", tint = Color.White, modifier = Modifier.size(18.dp))
                else -> Icon(Icons.Filled.Lock, contentDescription = "Locked", tint = TextMuted, modifier = Modifier.size(15.dp))
            }
        }
        Column(modifier = Modifier.weight(1f)) {
            Text(
                "DAY ${session.day} · ${session.area.label.uppercase()}",
                color = if (locked) TextMuted else session.area.accent,
                fontSize = TextSize.Micro,
                fontWeight = FontWeight.SemiBold,
                letterSpacing = 0.3.sp,
            )
            Text(session.title, fontSize = TextSize.BodyLarge, fontWeight = FontWeight.SemiBold, color = if (locked) TextSecondary else Ink)
        }
        if (ProGate.sessionLocked(session.index)) ProBadge() else Text("${session.minutes}m", color = TextMuted, fontSize = TextSize.Caption)
    }
}

@Composable
internal fun ProgressBar(fraction: Float, fill: Color, track: Color, height: Int = 6) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(height.dp)
            .clip(Shapes.Pill)
            .background(track),
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth(fraction.coerceIn(0f, 1f))
                .fillMaxHeight()
                .clip(Shapes.Pill)
                .background(fill),
        )
    }
}
