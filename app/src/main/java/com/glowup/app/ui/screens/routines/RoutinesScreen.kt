package com.glowup.app.ui.screens.routines

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
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
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.glowup.app.R
import com.glowup.app.ui.components.NavTab
import com.glowup.app.ui.components.ReMakeUBottomNav
import com.glowup.app.ui.components.StreakChip
import com.glowup.app.ui.components.WeekStreakDots
import com.glowup.app.ui.data.RoutineStore
import com.glowup.app.ui.design.Shapes
import com.glowup.app.ui.design.Space
import com.glowup.app.ui.design.TextSize
import com.glowup.app.ui.theme.*
import java.time.LocalDate

@Composable
fun RoutinesScreen(navController: NavHostController) {
    var selectedFilter by remember { mutableStateOf("All") }
    val allCards = RoutineStore.items
    val routineCards = if (selectedFilter == "All") allCards else allCards.filter { it.area.startsWith(selectedFilter.uppercase()) }

    Box(modifier = Modifier.fillMaxSize().background(Surface).systemBarsPadding()) {
        Column(modifier = Modifier.fillMaxSize()) {
            Text(
                stringResource(R.string.routines_your_routines),
                style = MaterialTheme.typography.headlineMedium,
                modifier = Modifier.padding(Space.Gutter, Space.Xxl, Space.Gutter, 0.dp)
            )

            Spacer(Modifier.height(Space.M))

            LazyRow(
                contentPadding = PaddingValues(horizontal = Space.Gutter),
                horizontalArrangement = Arrangement.spacedBy(Space.S)
            ) {
                items(listOf("All", "Looks", "Fitness", "Mind", "Social", "Style", "Sleep", "Life admin")) { filter ->
                    val count = if (filter == "All") allCards.size else allCards.count { it.area.startsWith(filter.uppercase()) }
                    val selected = filter == selectedFilter
                    Box(
                        modifier = Modifier
                            .clip(Shapes.Pill)
                            .background(if (selected) Ink else ChipNeutral)
                            .clickable { selectedFilter = filter }
                            .padding(Space.L, Space.S)
                    ) {
                        Text(
                            "$filter · $count",
                            color = if (selected) Color.White else Ink,
                            fontSize = TextSize.BodySmall,
                            fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Medium
                        )
                    }
                }
            }

            Spacer(Modifier.height(Space.L))

            LazyColumn(
                modifier = Modifier.weight(1f),
                contentPadding = PaddingValues(Space.Gutter, 0.dp, Space.Gutter, 190.dp),
                verticalArrangement = Arrangement.spacedBy(Space.M)
            ) {
                if (routineCards.isEmpty()) {
                    item {
                        Text(
                            if (selectedFilter ==
                                "All"
                            ) {
                                stringResource(R.string.routines_no_routines_yet_tap_to)
                            } else {
                                stringResource(R.string.routines_no_1_s_routines_yet, selectedFilter)
                            },
                            color = TextSecondary,
                            fontSize = TextSize.Body,
                            modifier = Modifier.padding(vertical = Space.M)
                        )
                    }
                }
                items(routineCards, key = { it.id }) { card ->
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(Shapes.Large)
                            .background(card.tint)
                            .clickable { navController.navigate("routine_detail/${card.id}") }
                            .padding(Space.L, Space.L)
                    ) {
                        Column(modifier = Modifier.fillMaxWidth(0.74f)) {
                            Text(card.area, color = card.accent, fontSize = TextSize.Micro, fontWeight = FontWeight.SemiBold)
                            Text(
                                card.title,
                                fontSize = TextSize.BodyLarge,
                                fontWeight = FontWeight.SemiBold,
                                modifier = Modifier.padding(top = 2.dp, bottom = Space.S)
                            )
                            StreakChip(card.streakDays(), card.accent)
                            Spacer(Modifier.height(Space.S))
                            WeekStreakDots(card.week(), card.accent)
                        }
                        Image(
                            painterResource(card.icon),
                            contentDescription = null,
                            modifier = Modifier.align(Alignment.TopEnd).size(48.dp)
                        )
                        val doneToday = card.isDoneOn(LocalDate.now())
                        Box(
                            modifier = Modifier
                                .align(Alignment.BottomEnd)
                                .size(44.dp)
                                .clip(CircleShape)
                                .background(if (doneToday) card.accent else Color.White)
                                .clickable { RoutineStore.toggleToday(card.id) },
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                Icons.Filled.Check,
                                contentDescription = if (doneToday) "Mark ${card.title} not done" else "Mark ${card.title} done today",
                                tint = if (doneToday) Color.White else card.accent,
                                modifier = Modifier.size(22.dp)
                            )
                        }
                    }
                }
            }
        }

        Box(modifier = Modifier.align(Alignment.BottomCenter)) {
            ReMakeUBottomNav(navController, NavTab.ROUTINES)
        }

        Box(
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(Space.Gutter, 0.dp, Space.Gutter, 110.dp)
                .size(58.dp)
                .clip(Shapes.Pill)
                .background(Ink)
                .clickable { navController.navigate("add_routine") },
            contentAlignment = Alignment.Center
        ) {
            Icon(Icons.Filled.Add, contentDescription = stringResource(R.string.routines_add_routine), tint = Color.White, modifier = Modifier.size(22.dp))
        }
    }
}
