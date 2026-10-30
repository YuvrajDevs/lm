package com.glowup.app.ui.screens.routines

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import com.glowup.app.ui.components.GlowUpBottomNav
import com.glowup.app.ui.components.NavTab
import com.glowup.app.ui.components.StreakChip
import com.glowup.app.ui.components.WeekStreakDots
import com.glowup.app.ui.data.RoutineStore
import com.glowup.app.ui.theme.*

@Composable
fun RoutinesScreen(navController: NavHostController) {
    var selectedFilter by remember { mutableStateOf("All") }
    val allCards = RoutineStore.items
    val routineCards = if (selectedFilter == "All") allCards else allCards.filter { it.area.startsWith(selectedFilter.uppercase()) }

    Box(modifier = Modifier.fillMaxSize().background(Surface).systemBarsPadding()) {
        Column(modifier = Modifier.fillMaxSize()) {

            Text(
                "Your routines",
                style = MaterialTheme.typography.headlineMedium,
                modifier = Modifier.padding(24.dp, 32.dp, 24.dp, 0.dp),
            )

            Spacer(Modifier.height(14.dp))

            LazyRow(
                contentPadding = PaddingValues(horizontal = 24.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                items(listOf("All", "Looks", "Fitness", "Mind", "Social", "Style", "Sleep", "Life admin")) { filter ->
                    val count = if (filter == "All") allCards.size else allCards.count { it.area.startsWith(filter.uppercase()) }
                    val selected = filter == selectedFilter
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(999.dp))
                            .background(if (selected) Ink else ChipNeutral)
                            .clickable { selectedFilter = filter }
                            .padding(16.dp, 9.dp),
                    ) {
                        Text("$filter · $count", color = if (selected) Color.White else Ink, fontSize = 13.sp, fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Medium)
                    }
                }
            }

            Spacer(Modifier.height(16.dp))

            LazyColumn(
                modifier = Modifier.weight(1f),
                contentPadding = PaddingValues(24.dp, 0.dp, 24.dp, 110.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                items(routineCards, key = { it.id }) { card ->
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(24.dp))
                            .background(card.tint)
                            .clickable { navController.navigate("routine_detail/${card.id}") }
                            .padding(18.dp, 16.dp),
                    ) {
                        Column(modifier = Modifier.fillMaxWidth(0.74f)) {
                            Text(card.area, color = card.accent, fontSize = 10.5.sp, fontWeight = FontWeight.SemiBold)
                            Text(card.title, fontSize = 15.sp, fontWeight = FontWeight.SemiBold, modifier = Modifier.padding(top = 2.dp, bottom = 8.dp))
                            StreakChip(card.streakDays, card.accent)
                            Spacer(Modifier.height(8.dp))
                            WeekStreakDots(card.week, card.accent)
                        }
                        Image(
                            painterResource(card.icon),
                            contentDescription = null,
                            modifier = Modifier.align(Alignment.TopEnd).size(48.dp),
                        )
                    }
                }
            }
        }

        Box(modifier = Modifier.align(Alignment.BottomCenter)) {
            GlowUpBottomNav(navController, NavTab.ROUTINES)
        }

        Box(
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(24.dp, 0.dp, 24.dp, 110.dp)
                .size(58.dp)
                .clip(RoundedCornerShape(999.dp))
                .background(Ink)
                .clickable { navController.navigate("add_routine") },
            contentAlignment = Alignment.Center,
        ) {
            Icon(Icons.Filled.Add, contentDescription = "Add routine", tint = Color.White, modifier = Modifier.size(22.dp))
        }
    }
}
