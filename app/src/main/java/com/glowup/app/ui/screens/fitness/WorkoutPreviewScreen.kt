package com.glowup.app.ui.screens.fitness

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.glowup.app.R
import com.glowup.app.ui.components.DetailHeader
import com.glowup.app.ui.components.ProLockedScreen
import com.glowup.app.ui.components.goBack
import com.glowup.app.ui.data.ProGate
import com.glowup.app.ui.data.fitness.FitnessStore
import com.glowup.app.ui.design.AppButton
import com.glowup.app.ui.design.ButtonVariant
import com.glowup.app.ui.design.LineHeight
import com.glowup.app.ui.design.Shapes
import com.glowup.app.ui.design.Space
import com.glowup.app.ui.design.TextSize
import com.glowup.app.ui.theme.*

/** Whether a workout key points at Pro-only content. */
fun workoutLocked(key: String): Boolean {
    val parts = key.split(".")
    return when (parts.getOrNull(0)) {
        "p" -> ProGate.programmeLocked(parts.getOrNull(1).orEmpty())
        "q" -> ProGate.quickLocked(parts.getOrNull(2)?.toIntOrNull() ?: 0)
        else -> false
    }
}

@Composable
fun WorkoutPreviewScreen(navController: NavHostController, key: String) {
    val plan = remember(key) { FitnessStore.planFor(key) } ?: return
    if (workoutLocked(key)) {
        ProLockedScreen(navController, plan.title, "This workout is part of Pro — every programme, 15–30 minute sessions and targets that adjust to you.")
        return
    }
    val totalSets = plan.blocks.sumOf { it.sets }

    Box(Modifier.fillMaxSize().background(Surface).systemBarsPadding()) {
        Column(Modifier.fillMaxSize()) {
            DetailHeader(stringResource(R.string.common_workout_preview_workout)) { navController.goBack() }
            LazyColumn(
                Modifier.weight(1f),
                contentPadding = PaddingValues(Space.Gutter, Space.L, Space.Gutter, Space.L),
                verticalArrangement = Arrangement.spacedBy(Space.S)
            ) {
                item {
                    Column(Modifier.fillMaxWidth().clip(Shapes.Large).background(Ink).padding(Space.Xl)) {
                        Text(plan.title, color = Color.White, fontSize = TextSize.TitleLarge, fontWeight = FontWeight.Bold, lineHeight = LineHeight.Title)
                        Text(plan.subtitle, color = Color.White.copy(alpha = 0.6f), fontSize = TextSize.BodySmall, modifier = Modifier.padding(top = 2.dp))
                        Spacer(Modifier.height(Space.L))
                        Row(horizontalArrangement = Arrangement.spacedBy(Space.Gutter)) {
                            Fact("${plan.minutes}", "minutes")
                            Fact("${plan.blocks.size}", "exercises")
                            Fact("$totalSets", "sets")
                        }
                        Spacer(Modifier.height(Space.M))
                        Row(horizontalArrangement = Arrangement.spacedBy(Space.S)) { plan.muscles.take(4).forEach { MuscleChip(it, onDark = true) } }
                    }
                    Spacer(Modifier.height(Space.S))
                    Text(
                        stringResource(R.string.workout_preview_tap_an_exercise_to_see),
                        color = TextSecondary,
                        fontSize = TextSize.Caption,
                        modifier = Modifier.padding(start = Space.Xs, bottom = 2.dp)
                    )
                }
                itemsIndexed(plan.blocks) { i, block ->
                    val e = block.exercise
                    Row(
                        Modifier.fillMaxWidth().clip(Shapes.Card).background(CardNeutral).clickable {
                            navController.navigate("exercise/${e.id}")
                        }.padding(Space.M),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        ExerciseArt(e, 56.dp, 14.dp)
                        Spacer(Modifier.width(Space.M))
                        Column(Modifier.weight(1f)) {
                            Text("${i + 1}. ${e.name}", fontSize = TextSize.BodyLarge, fontWeight = FontWeight.SemiBold)
                            Text(
                                if (block.max) {
                                    stringResource(
                                        R.string.workout_preview_as_many_as_you_can,
                                        block.target
                                    )
                                } else {
                                    stringResource(R.string.workout_preview_1_s_2_s_rest, block.sets, e.doseLabel(block.target), block.restSeconds)
                                },
                                color = TextSecondary,
                                fontSize = TextSize.BodySmall
                            )
                            FitnessStore.bestFor(e.id)?.let {
                                Text(
                                    stringResource(R.string.common_your_best_1_s, it),
                                    color = Green,
                                    fontSize = TextSize.Caption,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }
                        }
                        Icon(Icons.Filled.ChevronRight, contentDescription = null, tint = TextSecondary)
                    }
                }
                item {
                    Text(
                        stringResource(R.string.workout_preview_warm_up_with_a_minute),
                        color = TextSecondary,
                        fontSize = TextSize.Caption,
                        lineHeight = LineHeight.Compact,
                        modifier = Modifier.padding(Space.Xs, Space.S)
                    )
                }
            }
            Box(Modifier.padding(Space.Gutter, Space.S, Space.Gutter, Space.Gutter)) {
                AppButton(stringResource(R.string.workout_preview_start_workout), variant = ButtonVariant.Primary, icon = Icons.Filled.PlayArrow) {
                    navController.navigate("player/$key")
                }
            }
        }
    }
}

@Composable
private fun Fact(value: String, label: String) {
    Column {
        Text(value, color = Color.White, fontSize = TextSize.Headline, fontWeight = FontWeight.Bold)
        Text(label, color = Color.White.copy(alpha = 0.55f), fontSize = TextSize.Caption)
    }
}
