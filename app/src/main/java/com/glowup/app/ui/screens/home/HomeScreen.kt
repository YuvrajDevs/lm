package com.glowup.app.ui.screens.home

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
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
import com.glowup.app.ui.data.ProfileStore
import com.glowup.app.ui.data.ProgressStore
import com.glowup.app.ui.data.TodoBucket
import com.glowup.app.ui.data.TodoStore
import com.glowup.app.ui.theme.*

private data class QuickAccess(val label: String, val icon: Int, val bg: Color, val route: String)

private val quickAccessItems = listOf(
    QuickAccess("Looks", R.drawable.ic_looks, CoralTint, "looks"),
    QuickAccess("Sleep", R.drawable.ic_moon, BlueTint, "sleep"),
    QuickAccess("Confidence", R.drawable.ic_heart, GreenTint, "confidence"),
    QuickAccess("Talk", R.drawable.ic_speech, CoralTint, "communication"),
    QuickAccess("To-do", R.drawable.ic_list, PurpleTint, "todo"),
    QuickAccess("Quotes", R.drawable.ic_quote, MaroonTint, "quotes_notes"),
)

@Composable
fun HomeScreen(navController: NavHostController) {
    val focus = ProfileStore.focusAreas.toList()
    val orderedQuickAccess = quickAccessItems.sortedBy { item -> if (item.label in focus) 0 else 1 }
    val streak = ProgressStore.streakDays()
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
                Box(
                    modifier = Modifier
                        .size(52.dp)
                        .background(Ink, CircleShape)
                        .clickable { navController.navigate("notifications") },
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(Icons.Filled.Notifications, contentDescription = "Notifications", tint = Color.White, modifier = Modifier.size(20.dp))
                }
            }

            Spacer(Modifier.height(16.dp))

            LazyRow(
                contentPadding = PaddingValues(horizontal = 24.dp),
                horizontalArrangement = Arrangement.spacedBy(18.dp),
            ) {
                items(orderedQuickAccess) { item ->
                    Column(
                        modifier = Modifier.clip(RoundedCornerShape(16.dp)).clickable { navController.navigate(item.route) },
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

            // Streak hero card
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp)
                    .clip(RoundedCornerShape(28.dp))
                    .background(Ink)
                    .clickable { navController.navigate("streak_detail") }
                    .padding(20.dp, 18.dp),
            ) {
                Column {
                    Text(if (streak == 0) "Start your streak today" else "Current streak", color = Color.White.copy(alpha = 0.55f), fontSize = 11.5.sp)
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(if (streak == 1) "1-day streak" else "$streak-day streak", color = Color.White, fontSize = 24.sp, fontWeight = FontWeight.Bold)
                        Spacer(Modifier.width(6.dp))
                        Image(painterResource(R.drawable.ic_flame), contentDescription = null, modifier = Modifier.size(20.dp))
                    }
                    Text("${ProgressStore.points} points · Level ${ProgressStore.level}", color = Color.White.copy(alpha = 0.65f), fontSize = 12.5.sp)
                }
                Image(
                    painterResource(R.drawable.ic_streak),
                    contentDescription = null,
                    modifier = Modifier.align(Alignment.BottomEnd).size(66.dp),
                )
            }

            Spacer(Modifier.height(14.dp))

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp)
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

            Spacer(Modifier.height(18.dp))

            val planItems = TodoStore.items.filter { it.bucket == TodoBucket.TODAY }
            val doneCount = planItems.count { it.done }

            LazyColumn(
                modifier = Modifier.weight(1f),
                contentPadding = PaddingValues(horizontal = 24.dp, vertical = 0.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                item {
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(bottom = 4.dp),
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
                        if (plan.done) {
                            Image(painterResource(R.drawable.ic_check), contentDescription = null, modifier = Modifier.size(26.dp))
                        } else {
                            Box(modifier = Modifier.size(24.dp).background(Color.Transparent, CircleShape))
                        }
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
                item { Spacer(Modifier.height(90.dp)) }
            }
        }

        Box(modifier = Modifier.align(Alignment.BottomCenter)) {
            GlowUpBottomNav(navController, NavTab.HOME)
        }
    }
}
