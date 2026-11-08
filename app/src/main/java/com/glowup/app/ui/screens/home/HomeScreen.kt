package com.glowup.app.ui.screens.home

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.AcUnit
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import com.glowup.app.R
import com.glowup.app.ui.components.GlowUpBottomNav
import com.glowup.app.ui.components.NavTab
import com.glowup.app.ui.data.DailyContent
import com.glowup.app.ui.data.FocusStore
import com.glowup.app.ui.data.program.Program
import com.glowup.app.ui.data.program.ProgramStore
import com.glowup.app.ui.data.ProfileStore
import com.glowup.app.ui.data.ProgressStore
import com.glowup.app.ui.data.TodoBucket
import com.glowup.app.ui.data.TodoStore
import com.glowup.app.ui.data.RoutineStore
import com.glowup.app.ui.data.RoutineLinks
import com.glowup.app.ui.data.RoutineSession
import com.glowup.app.ui.theme.*

private data class QuickAccess(val label: String, val icon: Int, val bg: Color, val route: String)

private val quickAccessItems = listOf(
    QuickAccess("Learn", R.drawable.ic_mind, YellowTint, "learn"),
    QuickAccess("Focus", R.drawable.ic_goal, BlueTint, "focus"),
    QuickAccess("Fitness", R.drawable.ic_determined, GreenTint, "fitness"),
    QuickAccess("Looks", R.drawable.ic_looks, CoralTint, "looks"),
    QuickAccess("Sleep", R.drawable.ic_moon, BlueTint, "sleep"),
    QuickAccess("Mood", R.drawable.ic_mood_peaceful, GreenTint, "mood_insights"),
    QuickAccess("Confidence", R.drawable.ic_heart, GreenTint, "confidence"),
    QuickAccess("Talk", R.drawable.ic_speech, CoralTint, "communication"),
    QuickAccess("To-do", R.drawable.ic_list, PurpleTint, "todo"),
    QuickAccess("Quotes", R.drawable.ic_quote, MaroonTint, "quotes_notes"),
)

@Composable
fun HomeScreen(navController: NavHostController) {
    val focus = ProfileStore.focusAreas.toList()
    val orderedQuickAccess = quickAccessItems.sortedBy { item ->
        when (item.label) {
            "Learn", "Focus", "Fitness" -> 0
            in focus -> 1
            else -> 2
        }
    }
    val streak = ProgressStore.streakDays()
    androidx.compose.runtime.LaunchedEffect(Unit) {
        if (com.glowup.app.ui.data.glow.GlowCheckStore.openAfterOnboarding) {
            com.glowup.app.ui.data.glow.GlowCheckStore.openAfterOnboarding = false
            navController.navigate("glow_check")
        }
    }
    val (goalArea, goal) = DailyContent.goalOfTheDay(focus)
    val goalAdded = TodoStore.items.any { it.title == goal }

    Box(modifier = Modifier.fillMaxSize().background(Surface).systemBarsPadding()) {
        Column(modifier = Modifier.fillMaxSize()) {

            Row(
                modifier = Modifier.fillMaxWidth().padding(24.dp, 32.dp, 24.dp, 0.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Column {
                    Text(DailyContent.greeting(), color = TextSecondary, fontSize = 13.sp)
                    Text("Hey, ${ProfileStore.firstName}", style = MaterialTheme.typography.headlineMedium, fontSize = 24.sp)
                }
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    com.glowup.app.ui.components.ProCrownButton { com.glowup.app.ui.data.ProStore.showPaywall() }
                    Box(
                        modifier = Modifier
                            .size(52.dp)
                            .clip(CircleShape)
                            .background(Ink)
                            .clickable { navController.navigate("notifications") },
                        contentAlignment = Alignment.Center,
                    ) {
                        Icon(Icons.Filled.Notifications, contentDescription = "Notifications", tint = Color.White, modifier = Modifier.size(20.dp))
                    }
                }
            }

            Spacer(Modifier.height(16.dp))

            LazyRow(
                contentPadding = PaddingValues(horizontal = 24.dp),
                horizontalArrangement = Arrangement.spacedBy(18.dp),
            ) {
                items(orderedQuickAccess) { item ->
                    Column(
                        modifier = Modifier.clip(RoundedCornerShape(16.dp)).clickable { navController.navigate(item.route) }.padding(horizontal = 4.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                    ) {
                        Box(
                            modifier = Modifier
                                .size(52.dp)
                                .clip(CircleShape)
                                .background(item.bg),
                            contentAlignment = Alignment.Center,
                        ) {
                            Image(painterResource(item.icon), contentDescription = null, modifier = Modifier.size(22.dp), contentScale = ContentScale.Fit)
                        }
                        Spacer(Modifier.height(6.dp))
                        Text(item.label, fontSize = 11.5.sp, fontWeight = FontWeight.Medium)
                    }
                }
            }

            Spacer(Modifier.height(14.dp))

            val planItems = TodoStore.items.filter { it.bucket == TodoBucket.TODAY }
            val doneCount = planItems.count { it.done }

            LazyColumn(
                modifier = Modifier.weight(1f),
                contentPadding = PaddingValues(horizontal = 24.dp, vertical = 0.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                ProgressStore.freezeNotice?.let { notice ->
                    item {
                        FreezeBanner(
                            title = "Streak saved",
                            text = (if (notice.daysCovered == 1) "A freeze covered the day you missed" else "${notice.daysCovered} freezes covered the days you missed") +
                                " — your ${notice.streak}-day streak is safe.",
                            onDismiss = { ProgressStore.dismissFreezeNotice() },
                        )
                    }
                }
                if (ProgressStore.freezeJustEarned && ProgressStore.freezeNotice == null) {
                    item {
                        FreezeBanner(
                            title = "You earned a streak freeze",
                            text = "7 days in a row! If you miss a day, it'll protect your streak automatically. ${ProgressStore.freezes} of ${ProgressStore.maxFreezes} banked.",
                            onDismiss = { ProgressStore.acknowledgeFreezeEarned() },
                        )
                    }
                }
                item { GlowCheckCard(navController) }
                item { TodaySessionCard(navController) }
                item {
                    Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        StreakTile(streak, Modifier.weight(1f)) { navController.navigate("streak_detail") }
                        FocusTile(Modifier.weight(1f)) { navController.navigate("focus") }
                    }
                }
                item {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(24.dp))
                            .background(MaroonTint)
                            .clickable { navController.navigate("quotes_notes") }
                            .padding(18.dp, 14.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                    ) {
                        Image(painterResource(R.drawable.ic_quote), contentDescription = null, modifier = Modifier.size(26.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text("QUOTE OF THE DAY", color = Maroon, fontSize = 10.sp, fontWeight = FontWeight.SemiBold, letterSpacing = 0.4.sp)
                            Text(DailyContent.quoteOfTheDay().text, fontSize = 13.sp, fontWeight = FontWeight.Medium, lineHeight = 18.sp, modifier = Modifier.padding(top = 2.dp))
                        }
                    }
                }
                item {
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(top = 10.dp, bottom = 4.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                    ) {
                        Text("Today's plan", style = MaterialTheme.typography.titleLarge, fontSize = 17.sp)
                        Text("$doneCount of ${planItems.size} done", color = TextSecondary, fontSize = 12.5.sp, fontWeight = FontWeight.Medium)
                    }
                }
                items(planItems, key = { it.id }) { plan ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(20.dp))
                            .background(plan.tint)
                            .clickable { TodoStore.toggle(plan.id) }
                            .padding(14.dp, 12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                    ) {
                        CheckCircle(plan.done)
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                plan.title,
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 14.sp,
                                textDecoration = if (plan.done) TextDecoration.LineThrough else null,
                            )
                            plan.meta?.let { Text(it, color = TextSecondary, fontSize = 11.5.sp) }
                        }
                        val icon = plan.icon
                        if (!plan.done && icon != null) {
                            Image(painterResource(icon), contentDescription = null, modifier = Modifier.size(32.dp))
                        }
                    }
                }
                if (planItems.isEmpty()) {
                    item {
                        Text(
                            "Nothing planned yet — add a to-do or pick up today's goal below.",
                            color = TextSecondary,
                            fontSize = 13.sp,
                            modifier = Modifier.padding(vertical = 8.dp),
                        )
                    }
                }
                item {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 8.dp)
                            .clip(RoundedCornerShape(24.dp))
                            .background(PurpleTint)
                            .clickable(enabled = !goalAdded) { TodoStore.add(goal, TodoBucket.TODAY, "Goal of the day · $goalArea") }
                            .padding(18.dp, 16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                    ) {
                        Image(painterResource(R.drawable.ic_social), contentDescription = null, modifier = Modifier.size(40.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text("GOAL OF THE DAY · ${goalArea.uppercase()}", color = Color(0xFF6B4FD1), fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
                            Text(goal, fontWeight = FontWeight.SemiBold, fontSize = 14.5.sp)
                            Text(
                                if (goalAdded) "Added to today's plan" else "Tap to add it to today's plan",
                                color = Color(0xFF6B4FD1),
                                fontSize = 11.5.sp,
                                modifier = Modifier.padding(top = 2.dp),
                            )
                        }
                    }
                }
                val routines = RoutineStore.items.toList()
                val today = java.time.LocalDate.now()
                item {
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(top = 18.dp, bottom = 4.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Column {
                            Text("Routines", style = MaterialTheme.typography.titleLarge, fontSize = 17.sp)
                            Text(
                                if (routines.isEmpty()) "Habits you repeat every day"
                                else "${routines.count { it.isDoneOn(today) }} of ${routines.size} done today",
                                color = TextSecondary, fontSize = 12.5.sp, fontWeight = FontWeight.Medium,
                            )
                        }
                        Text(
                            if (routines.isEmpty()) "Add" else "See all",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.SemiBold,
                            modifier = Modifier
                                .clip(RoundedCornerShape(999.dp))
                                .background(ChipNeutral)
                                .clickable { navController.navigate(if (routines.isEmpty()) "add_routine" else "routines") }
                                .padding(14.dp, 7.dp),
                        )
                    }
                }
                items(routines, key = { "routine-${it.id}" }) { routine ->
                    val done = routine.isDoneOn(today)
                    val routineStreak = routine.streakDays(today)
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(20.dp))
                            .background(CardNeutral)
                            .clickable {
                                val route = RoutineLinks.routeFor(routine)
                                if (!done && route != null) {
                                    RoutineSession.start(routine.id)
                                    navController.navigate(route)
                                } else {
                                    RoutineStore.toggleToday(routine.id)
                                }
                            }
                            .padding(14.dp, 12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                    ) {
                        // The circle always ticks it off right here.
                        Box(Modifier.clip(CircleShape).clickable { RoutineStore.toggleToday(routine.id) }) { CheckCircle(done) }
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                routine.title,
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 14.sp,
                                color = if (done) TextSecondary else Ink,
                                textDecoration = if (done) TextDecoration.LineThrough else null,
                            )
                            val route = RoutineLinks.routeFor(routine)
                            Text(
                                routine.category + (if (routine.frequency == "WEEKLY") " · Weekly" else "") +
                                    if (!done && route != null) " · Tap to start" else "",
                                color = TextSecondary, fontSize = 11.5.sp,
                            )
                        }
                        if (routineStreak > 0) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Image(painterResource(R.drawable.ic_flame), contentDescription = null, modifier = Modifier.size(14.dp))
                                Spacer(Modifier.width(3.dp))
                                Text("$routineStreak", fontSize = 13.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                        Box(
                            Modifier.size(34.dp).clip(CircleShape).background(routine.tint),
                            contentAlignment = Alignment.Center,
                        ) {
                            Image(painterResource(routine.icon), contentDescription = null, modifier = Modifier.size(20.dp))
                        }
                    }
                }
                item { Spacer(Modifier.height(90.dp)) }
            }
        }

        Box(modifier = Modifier.align(Alignment.BottomCenter)) {
            GlowUpBottomNav(navController, NavTab.HOME)
        }
    }
}

@Composable
private fun TodaySessionCard(navController: NavHostController) {
    val next = ProgramStore.next ?: return
    val doneToday = ProgramStore.doneToday()
    val locked = com.glowup.app.ui.data.ProGate.sessionLocked(next.index)
    val learningStreak = ProgramStore.streakDays()
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(28.dp))
            .background(next.area.tint)
            .clickable {
                if (locked) com.glowup.app.ui.data.ProStore.showPaywall("You've finished the free weeks — keep going with the full programme")
                else navController.navigate("session/${next.index}")
            }
            .padding(20.dp, 18.dp),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    (if (doneToday) "UP NEXT" else "TODAY") + " · DAY ${next.day} OF ${Program.TOTAL}",
                    color = next.area.accent,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.SemiBold,
                    letterSpacing = 0.4.sp,
                )
                Spacer(Modifier.height(4.dp))
                Text(next.title, fontSize = 19.sp, fontWeight = FontWeight.Bold, lineHeight = 24.sp)
                Spacer(Modifier.height(4.dp))
                Text(
                    "${next.area.label} · ${next.minutes} min · +${next.points} points",
                    color = TextSecondary,
                    fontSize = 12.5.sp,
                )
            }
            Image(painterResource(next.area.icon), contentDescription = null, modifier = Modifier.size(52.dp))
        }
        Spacer(Modifier.height(14.dp))
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier.clip(RoundedCornerShape(999.dp)).background(Ink).padding(18.dp, 10.dp),
            ) {
                Text(if (locked) "Unlock with Pro" else if (doneToday) "Keep going" else "Start session", color = Color.White, fontSize = 13.5.sp, fontWeight = FontWeight.SemiBold)
            }
            Spacer(Modifier.width(12.dp))
            when {
                doneToday -> {
                    Image(painterResource(R.drawable.ic_check), contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(Modifier.width(4.dp))
                    Text("Today's streak secured", color = Green, fontSize = 12.5.sp, fontWeight = FontWeight.SemiBold)
                }
                learningStreak > 0 -> {
                    Image(painterResource(R.drawable.ic_flame), contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(Modifier.width(4.dp))
                    Text("Keep your $learningStreak-day streak", color = Coral, fontSize = 12.5.sp, fontWeight = FontWeight.SemiBold)
                }
            }
        }
    }
}

@Composable
private fun StreakTile(streak: Int, modifier: Modifier, onClick: () -> Unit) {
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(24.dp))
            .background(Ink)
            .clickable(onClick = onClick)
            .padding(16.dp),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Image(painterResource(R.drawable.ic_flame), contentDescription = null, modifier = Modifier.size(18.dp))
            Spacer(Modifier.width(4.dp))
            Text("$streak", color = Color.White, fontSize = 22.sp, fontWeight = FontWeight.Bold)
        }
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text("day streak", color = Color.White.copy(alpha = 0.6f), fontSize = 12.sp)
            if (ProgressStore.freezes > 0) {
                Spacer(Modifier.width(6.dp))
                Icon(androidx.compose.material.icons.Icons.Filled.AcUnit, contentDescription = "Streak freezes", tint = Color(0xFF8DB8F5), modifier = Modifier.size(12.dp))
                Text("${ProgressStore.freezes}", color = Color(0xFF8DB8F5), fontSize = 12.sp, fontWeight = FontWeight.Bold)
            }
        }
        Spacer(Modifier.height(6.dp))
        Text("Level ${ProgressStore.level} · ${ProgressStore.points} pts", color = Color.White, fontSize = 12.5.sp, fontWeight = FontWeight.SemiBold)
    }
}

@Composable
private fun FocusTile(modifier: Modifier, onClick: () -> Unit) {
    val active = FocusStore.active
    val focusStreak = FocusStore.streakDays()
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(24.dp))
            .background(BlueTint)
            .clickable(onClick = onClick)
            .padding(16.dp),
    ) {
        Text(
            when {
                active == null -> "${FocusStore.minutesOn(java.time.LocalDate.now())} min"
                active.isRunning -> "Focusing"
                else -> "Paused"
            },
            fontSize = 22.sp,
            fontWeight = FontWeight.Bold,
            color = Navy,
        )
        Text(if (active == null) "focused today" else active.label, color = TextSecondary, fontSize = 12.sp)
        Spacer(Modifier.height(6.dp))
        Text(
            if (active != null) "Tap to return" else if (focusStreak > 0) "$focusStreak-day focus streak" else "Start a focus block",
            color = Navy,
            fontSize = 12.5.sp,
            fontWeight = FontWeight.SemiBold,
        )
    }
}

@Composable
private fun FreezeBanner(title: String, text: String, onDismiss: () -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(22.dp)).background(BlueTint).padding(16.dp, 14.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(Modifier.size(38.dp).clip(CircleShape).background(Blue), contentAlignment = Alignment.Center) {
            Icon(androidx.compose.material.icons.Icons.Filled.AcUnit, contentDescription = null, tint = Color.White, modifier = Modifier.size(19.dp))
        }
        Spacer(Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(title, fontSize = 14.5.sp, fontWeight = FontWeight.Bold)
            Text(text, color = Navy, fontSize = 12.5.sp, lineHeight = 17.sp)
        }
        Box(
            Modifier.size(32.dp).clip(CircleShape).clickable(onClick = onDismiss),
            contentAlignment = Alignment.Center,
        ) {
            Icon(androidx.compose.material.icons.Icons.Filled.Close, contentDescription = "Dismiss", tint = Navy, modifier = Modifier.size(16.dp))
        }
    }
}

@Composable
private fun CheckCircle(done: Boolean) {
    if (done) {
        Image(painterResource(R.drawable.ic_check), contentDescription = "Done", modifier = Modifier.size(26.dp))
    } else {
        Box(
            Modifier
                .size(26.dp)
                .clip(CircleShape)
                .border(2.dp, TextMuted, CircleShape),
        )
    }
}

@Composable
private fun GlowCheckCard(navController: NavHostController) {
    val latest = com.glowup.app.ui.data.glow.GlowCheckStore.latest
    val due = com.glowup.app.ui.data.glow.GlowCheckStore.daysUntilRescan()
    if (latest == null) {
        Row(
            modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(28.dp)).background(Ink).clickable { navController.navigate("glow_check") }.padding(20.dp, 18.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(Modifier.weight(1f)) {
                Text("YOUR PERSONAL PLAN", color = Color.White.copy(alpha = 0.55f), fontSize = 11.sp, fontWeight = FontWeight.SemiBold, letterSpacing = 0.4.sp)
                Text("Take the Glow-Up Check", color = Color.White, fontSize = 19.sp, fontWeight = FontWeight.Bold)
                Text("3 minutes · find what will make the biggest difference for you", color = Color.White.copy(alpha = 0.65f), fontSize = 12.5.sp, lineHeight = 17.sp)
            }
            Spacer(Modifier.width(10.dp))
            Image(painterResource(R.drawable.ic_streak), contentDescription = null, modifier = Modifier.size(54.dp))
        }
    } else {
        Row(
            modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(22.dp)).background(CardNeutral).clickable { navController.navigate("glow_results") }.padding(16.dp, 14.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(Modifier.weight(1f)) {
                Text("Your Glow-Up map", fontSize = 15.sp, fontWeight = FontWeight.Bold)
                Text(
                    latest.focus.joinToString(" · ") { it.area.title }.ifBlank { "All areas looking good" },
                    color = TextSecondary, fontSize = 12.5.sp, maxLines = 1,
                )
            }
            Text(
                if (due == 0) "Rescan due" else "Rescan in $due d",
                color = if (due == 0) Color.White else Ink, fontSize = 11.5.sp, fontWeight = FontWeight.Bold,
                modifier = Modifier.clip(RoundedCornerShape(999.dp)).background(if (due == 0) Coral else Color.White).padding(10.dp, 5.dp),
            )
        }
    }
}
