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
import com.glowup.app.ui.data.MoodStore
import com.glowup.app.ui.data.TodoBucket
import com.glowup.app.ui.data.TodoStore
import com.glowup.app.ui.theme.*

private data class QuickAccess(val label: String, val icon: Int, val bg: Color, val route: String? = null)

private val quickAccessItems = listOf(
    QuickAccess("Looks", R.drawable.ic_looks, CoralTint, "looks"),
    QuickAccess("Sleep", R.drawable.ic_moon, BlueTint, "sleep"),
    QuickAccess("Confidence", R.drawable.ic_heart, GreenTint, "confidence"),
    QuickAccess("Talk", R.drawable.ic_speech, CoralTint, "communication"),
    QuickAccess("To-do", R.drawable.ic_list, PurpleTint, "todo"),
    QuickAccess("Quotes", R.drawable.ic_quote, MaroonTint, "quotes_notes"),
)

private const val QUOTE_OF_THE_DAY = "\"Small, boring habits — done daily — beat big plans done never.\""

@Composable
fun HomeScreen(navController: NavHostController) {
    Box(modifier = Modifier.fillMaxSize().background(Surface).systemBarsPadding()) {
        Column(modifier = Modifier.fillMaxSize()) {

            Row(
                modifier = Modifier.fillMaxWidth().padding(24.dp, 32.dp, 24.dp, 0.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Column {
                    Text("Good morning", color = TextSecondary, fontSize = 13.sp)
                    Text("Hey, Arjun", style = MaterialTheme.typography.headlineMedium, fontSize = 24.sp)
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
                items(quickAccessItems) { item ->
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Box(
                            modifier = Modifier
                                .size(52.dp)
                                .background(item.bg, CircleShape)
                                .let { m -> item.route?.let { r -> m.clickable { navController.navigate(r) } } ?: m },
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
                    Text("This week", color = Color.White.copy(alpha = 0.55f), fontSize = 11.5.sp)
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text("${MoodStore.streakDays}-day streak", color = Color.White, fontSize = 24.sp, fontWeight = FontWeight.Bold)
                        Spacer(Modifier.width(6.dp))
                        Image(painterResource(R.drawable.ic_flame), contentDescription = null, modifier = Modifier.size(20.dp))
                    }
                    Text("${MoodStore.points} points · Level ${MoodStore.level}", color = Color.White.copy(alpha = 0.65f), fontSize = 12.5.sp)
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
                    Text(QUOTE_OF_THE_DAY, fontSize = 13.sp, fontWeight = FontWeight.Medium, lineHeight = 18.sp, modifier = Modifier.padding(top = 2.dp))
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
                        if (!plan.done && plan.icon != null) {
                            Image(painterResource(plan.icon), contentDescription = null, modifier = Modifier.size(32.dp))
                        }
                    }
                }
                item {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 8.dp)
                            .clip(RoundedCornerShape(24.dp))
                            .background(PurpleTint)
                            .padding(18.dp, 16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                    ) {
                        Image(painterResource(R.drawable.ic_social), contentDescription = null, modifier = Modifier.size(40.dp))
                        Column {
                            Text("GOAL OF THE DAY", color = Color(0xFF6B4FD1), fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
                            Text("Start one new conversation today", fontWeight = FontWeight.SemiBold, fontSize = 14.5.sp)
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
