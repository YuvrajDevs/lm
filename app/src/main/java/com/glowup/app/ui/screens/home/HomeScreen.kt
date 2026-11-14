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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import com.glowup.app.R
import com.glowup.app.ui.components.NavTab
import com.glowup.app.ui.components.ProCrownButton
import com.glowup.app.ui.components.ReMakeUBottomNav
import com.glowup.app.ui.data.DailyContent
import com.glowup.app.ui.data.ProStore
import com.glowup.app.ui.data.ProfileStore
import com.glowup.app.ui.data.ProgressStore
import com.glowup.app.ui.data.RoutineEntry
import com.glowup.app.ui.data.RoutineLinks
import com.glowup.app.ui.data.RoutineSession
import com.glowup.app.ui.data.RoutineStore
import com.glowup.app.ui.data.TodoBucket
import com.glowup.app.ui.data.TodoEntry
import com.glowup.app.ui.data.TodoStore
import com.glowup.app.ui.data.glow.GlowCheckStore
import com.glowup.app.ui.design.LineHeight
import com.glowup.app.ui.design.Shapes
import com.glowup.app.ui.design.Space
import com.glowup.app.ui.design.TextSize
import com.glowup.app.ui.theme.*
import java.time.LocalDate

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
    QuickAccess("Quotes", R.drawable.ic_quote, MaroonTint, "quotes_notes")
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
    LaunchedEffect(Unit) {
        if (GlowCheckStore.openAfterOnboarding) {
            GlowCheckStore.openAfterOnboarding = false
            navController.navigate("glow_check")
        }
    }
    val (goalArea, goal) = DailyContent.goalOfTheDay(focus)
    val goalAdded = TodoStore.items.any { it.title == goal }

    Box(modifier = Modifier.fillMaxSize().background(Surface).systemBarsPadding()) {
        Column(modifier = Modifier.fillMaxSize()) {
            HomeHeader(navController)

            Spacer(Modifier.height(Space.L))

            QuickAccessRow(orderedQuickAccess) { navController.navigate(it) }

            Spacer(Modifier.height(Space.M))

            val planItems = TodoStore.items.filter { it.bucket == TodoBucket.TODAY }
            val doneCount = planItems.count { it.done }

            LazyColumn(
                modifier = Modifier.weight(1f).testTag("home_feed"),
                contentPadding = PaddingValues(horizontal = Space.Gutter, vertical = 0.dp),
                verticalArrangement = Arrangement.spacedBy(Space.S)
            ) {
                ProgressStore.freezeNotice?.let { notice ->
                    item {
                        FreezeBanner(
                            title = stringResource(R.string.home_freeze_saved_title),
                            text = pluralStringResource(R.plurals.home_freeze_saved_body, notice.daysCovered, notice.daysCovered, notice.streak),
                            onDismiss = { ProgressStore.dismissFreezeNotice() }
                        )
                    }
                }
                if (ProgressStore.freezeJustEarned && ProgressStore.freezeNotice == null) {
                    item {
                        FreezeBanner(
                            title = stringResource(R.string.home_freeze_earned_title),
                            text = stringResource(R.string.home_freeze_earned_body, ProgressStore.freezes, ProgressStore.maxFreezes),
                            onDismiss = { ProgressStore.acknowledgeFreezeEarned() }
                        )
                    }
                }
                item { GlowCheckCard(navController) }
                item { TodaySessionCard(navController) }
                item { CheckInsRow(navController) }
                item {
                    Row(horizontalArrangement = Arrangement.spacedBy(Space.S)) {
                        StreakTile(streak, Modifier.weight(1f)) { navController.navigate("streak_detail") }
                        FocusTile(Modifier.weight(1f)) { navController.navigate("focus") }
                    }
                }
                item {
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(top = Space.S, bottom = Space.Xs),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(stringResource(R.string.home_today_s_plan), style = MaterialTheme.typography.titleLarge, fontSize = TextSize.Title)
                        Text(
                            stringResource(R.string.common_1_s_of_2_s, doneCount, planItems.size),
                            color = TextSecondary,
                            fontSize = TextSize.BodySmall,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
                items(planItems, key = { it.id }) { plan ->
                    PlanItemRow(plan) { TodoStore.toggle(plan.id) }
                }
                if (planItems.isEmpty()) {
                    item {
                        Text(
                            stringResource(R.string.home_nothing_planned_yet_add_a),
                            color = TextSecondary,
                            fontSize = TextSize.BodySmall,
                            modifier = Modifier.padding(vertical = Space.S)
                        )
                    }
                }
                item {
                    GoalOfTheDayCard(goalArea, goal, goalAdded) { TodoStore.add(goal, TodoBucket.TODAY, "Goal of the day · $goalArea") }
                }
                val routines = RoutineStore.items.toList()
                val today = LocalDate.now()
                item {
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(top = Space.L, bottom = Space.Xs),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(stringResource(R.string.common_routine_routines), style = MaterialTheme.typography.titleLarge, fontSize = TextSize.Title)
                            Text(
                                if (routines.isEmpty()) {
                                    stringResource(R.string.home_habits_you_repeat_every_day)
                                } else {
                                    "${routines.count { it.isDoneOn(today) }} of ${routines.size} done today"
                                },
                                color = TextSecondary,
                                fontSize = TextSize.BodySmall,
                                fontWeight = FontWeight.Medium
                            )
                        }
                        Text(
                            if (routines.isEmpty()) stringResource(R.string.common_add) else stringResource(R.string.home_see_all),
                            fontSize = TextSize.BodySmall,
                            fontWeight = FontWeight.SemiBold,
                            modifier = Modifier
                                .clip(Shapes.Pill)
                                .background(ChipNeutral)
                                .clickable { navController.navigate(if (routines.isEmpty()) "add_routine" else "routines") }
                                .padding(Space.M, Space.S)
                        )
                    }
                }
                items(routines, key = { "routine-${it.id}" }) { routine ->
                    RoutineRow(
                        routine = routine,
                        today = today,
                        onOpen = { route ->
                            RoutineSession.start(routine.id)
                            navController.navigate(route)
                        },
                        onToggle = { RoutineStore.toggleToday(routine.id) }
                    )
                }
                item {
                    Spacer(Modifier.height(Space.M))
                    QuoteCard { navController.navigate("quotes_notes") }
                }
                item { Spacer(Modifier.height(90.dp)) }
            }
        }

        Box(modifier = Modifier.align(Alignment.BottomCenter)) {
            ReMakeUBottomNav(navController, NavTab.HOME)
        }
    }
}

@Composable
private fun CheckCircle(done: Boolean) {
    if (done) {
        Image(painterResource(R.drawable.ic_check), contentDescription = stringResource(R.string.common_done), modifier = Modifier.size(26.dp))
    } else {
        Box(
            Modifier
                .size(26.dp)
                .clip(CircleShape)
                .border(2.dp, TextMuted, CircleShape)
        )
    }
}

@Composable
private fun HomeHeader(navController: NavHostController) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(Space.Gutter, Space.Xxl, Space.Gutter, 0.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column {
            Text(DailyContent.greeting(), color = TextSecondary, fontSize = TextSize.BodySmall)
            Text(stringResource(R.string.home_hey_1_s, ProfileStore.firstName), style = MaterialTheme.typography.headlineMedium, fontSize = TextSize.Headline)
        }
        Row(horizontalArrangement = Arrangement.spacedBy(Space.S)) {
            ProCrownButton { ProStore.showPaywall() }
            Box(
                modifier = Modifier
                    .size(52.dp)
                    .clip(CircleShape)
                    .background(Ink)
                    .clickable { navController.navigate("notifications") },
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    Icons.Filled.Notifications,
                    contentDescription = stringResource(R.string.home_notifications),
                    tint = Color.White,
                    modifier = Modifier.size(20.dp)
                )
            }
        }
    }
}

@Composable
private fun QuickAccessRow(items: List<QuickAccess>, onOpen: (String) -> Unit) {
    LazyRow(
        contentPadding = PaddingValues(horizontal = Space.Gutter),
        horizontalArrangement = Arrangement.spacedBy(Space.M)
    ) {
        items(items) { item ->
            Column(
                modifier = Modifier.clip(Shapes.Medium).clickable { onOpen(item.route) }.padding(horizontal = Space.Xs),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Box(
                    modifier = Modifier
                        .size(52.dp)
                        .clip(CircleShape)
                        .background(item.bg),
                    contentAlignment = Alignment.Center
                ) {
                    Image(painterResource(item.icon), contentDescription = null, modifier = Modifier.size(22.dp), contentScale = ContentScale.Fit)
                }
                Spacer(Modifier.height(Space.S))
                Text(item.label, fontSize = TextSize.Caption, fontWeight = FontWeight.Medium)
            }
        }
    }
}

@Composable
private fun PlanItemRow(plan: TodoEntry, onToggle: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(Shapes.Card)
            .background(plan.tint)
            .clickable(onClick = onToggle)
            .padding(Space.M, Space.M),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(Space.M)
    ) {
        CheckCircle(plan.done)
        Column(modifier = Modifier.weight(1f)) {
            Text(
                plan.title,
                fontWeight = FontWeight.SemiBold,
                fontSize = TextSize.Body,
                textDecoration = if (plan.done) TextDecoration.LineThrough else null
            )
            plan.meta?.let { Text(it, color = TextSecondary, fontSize = TextSize.Caption) }
        }
        val icon = plan.icon
        if (!plan.done && icon != null) {
            Image(painterResource(icon), contentDescription = null, modifier = Modifier.size(32.dp))
        }
    }
}

@Composable
private fun GoalOfTheDayCard(goalArea: String, goal: String, goalAdded: Boolean, onAdd: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = Space.S)
            .clip(Shapes.Large)
            .background(PurpleTint)
            .clickable(enabled = !goalAdded, onClick = onAdd)
            .padding(Space.L, Space.L),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(Space.M)
    ) {
        Image(painterResource(R.drawable.ic_social), contentDescription = null, modifier = Modifier.size(40.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                stringResource(R.string.home_goal_of_the_day_1, goalArea.uppercase()),
                color = PurpleText,
                fontSize = TextSize.Micro,
                fontWeight = FontWeight.SemiBold
            )
            Text(goal, fontWeight = FontWeight.SemiBold, fontSize = TextSize.BodyLarge)
            Text(
                if (goalAdded) stringResource(R.string.common_added_to_today_s_plan) else stringResource(R.string.home_tap_to_add_it_to),
                color = PurpleText,
                fontSize = TextSize.Caption,
                modifier = Modifier.padding(top = 2.dp)
            )
        }
    }
}

@Composable
private fun RoutineRow(routine: RoutineEntry, today: LocalDate, onOpen: (String) -> Unit, onToggle: () -> Unit) {
    val done = routine.isDoneOn(today)
    val routineStreak = routine.streakDays(today)
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(Shapes.Card)
            .background(CardNeutral)
            .clickable {
                val route = RoutineLinks.routeFor(routine)
                if (!done && route != null) onOpen(route) else onToggle()
            }
            .padding(Space.M, Space.M),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(Space.M)
    ) {
        // The circle always ticks it off right here.
        Box(Modifier.clip(CircleShape).clickable(onClick = onToggle)) { CheckCircle(done) }
        Column(modifier = Modifier.weight(1f)) {
            Text(
                routine.title,
                fontWeight = FontWeight.SemiBold,
                fontSize = TextSize.Body,
                color = if (done) TextSecondary else Ink,
                textDecoration = if (done) TextDecoration.LineThrough else null
            )
            val route = RoutineLinks.routeFor(routine)
            Text(
                routine.category + (if (routine.frequency == "WEEKLY") stringResource(R.string.home_weekly) else "") +
                    if (!done && route != null) stringResource(R.string.home_tap_to_start) else "",
                color = TextSecondary,
                fontSize = TextSize.Caption
            )
        }
        if (routineStreak > 0) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Image(painterResource(R.drawable.ic_flame), contentDescription = null, modifier = Modifier.size(14.dp))
                Spacer(Modifier.width(Space.Xs))
                Text("$routineStreak", fontSize = TextSize.BodySmall, fontWeight = FontWeight.Bold)
            }
        }
        Box(
            Modifier.size(34.dp).clip(CircleShape).background(routine.tint),
            contentAlignment = Alignment.Center
        ) {
            Image(painterResource(routine.icon), contentDescription = null, modifier = Modifier.size(20.dp))
        }
    }
}

@Composable
private fun QuoteCard(onOpen: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(Shapes.Large)
            .background(MaroonTint)
            .clickable(onClick = onOpen)
            .padding(Space.L, Space.M),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(Space.S)
    ) {
        Image(painterResource(R.drawable.ic_quote), contentDescription = null, modifier = Modifier.size(26.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                stringResource(R.string.home_quote_of_the_day),
                color = Maroon,
                fontSize = TextSize.Micro,
                fontWeight = FontWeight.SemiBold,
                letterSpacing = 0.4.sp
            )
            Text(
                DailyContent.quoteOfTheDay().text,
                fontSize = TextSize.BodySmall,
                fontWeight = FontWeight.Medium,
                lineHeight = LineHeight.Compact,
                modifier = Modifier.padding(top = 2.dp)
            )
        }
    }
}
