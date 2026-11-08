package com.glowup.app.ui.screens.fitness

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import com.glowup.app.ui.components.DetailHeader
import com.glowup.app.ui.components.ProLockedScreen
import com.glowup.app.ui.components.goBack
import com.glowup.app.ui.data.ProGate
import com.glowup.app.ui.data.fitness.FitnessStore
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
            DetailHeader("Workout") { navController.goBack() }
            LazyColumn(Modifier.weight(1f), contentPadding = PaddingValues(24.dp, 18.dp, 24.dp, 16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                item {
                    Column(Modifier.fillMaxWidth().clip(RoundedCornerShape(26.dp)).background(Ink).padding(20.dp)) {
                        Text(plan.title, color = Color.White, fontSize = 21.sp, fontWeight = FontWeight.Bold, lineHeight = 26.sp)
                        Text(plan.subtitle, color = Color.White.copy(alpha = 0.6f), fontSize = 12.5.sp, modifier = Modifier.padding(top = 2.dp))
                        Spacer(Modifier.height(16.dp))
                        Row(horizontalArrangement = Arrangement.spacedBy(24.dp)) {
                            Fact("${plan.minutes}", "minutes")
                            Fact("${plan.blocks.size}", "exercises")
                            Fact("$totalSets", "sets")
                        }
                        Spacer(Modifier.height(14.dp))
                        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) { plan.muscles.take(4).forEach { MuscleChip(it, onDark = true) } }
                    }
                    Spacer(Modifier.height(10.dp))
                    Text("Tap an exercise to see how to do it", color = TextSecondary, fontSize = 12.sp, modifier = Modifier.padding(start = 4.dp, bottom = 2.dp))
                }
                itemsIndexed(plan.blocks) { i, block ->
                    val e = block.exercise
                    Row(
                        Modifier.fillMaxWidth().clip(RoundedCornerShape(20.dp)).background(CardNeutral).clickable { navController.navigate("exercise/${e.id}") }.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        ExerciseArt(e, 56.dp, 14.dp)
                        Spacer(Modifier.width(14.dp))
                        Column(Modifier.weight(1f)) {
                            Text("${i + 1}. ${e.name}", fontSize = 15.sp, fontWeight = FontWeight.SemiBold)
                            Text(
                                if (block.max) "As many as you can · goal ${block.target}" else "${block.sets} × ${e.doseLabel(block.target)} · rest ${block.restSeconds}s",
                                color = TextSecondary, fontSize = 12.5.sp,
                            )
                            FitnessStore.bestFor(e.id)?.let { Text("Your best: $it", color = Green, fontSize = 11.5.sp, fontWeight = FontWeight.SemiBold) }
                        }
                        Icon(Icons.Filled.ChevronRight, contentDescription = null, tint = TextSecondary)
                    }
                }
                item {
                    Text(
                        "Warm up with a minute of marching or arm circles first. Stop if anything hurts sharply — a burn in the muscle is fine, pain in a joint isn't.",
                        color = TextSecondary, fontSize = 12.sp, lineHeight = 17.sp, modifier = Modifier.padding(4.dp, 10.dp),
                    )
                }
            }
            Box(Modifier.padding(24.dp, 8.dp, 24.dp, 24.dp)) {
                Row(
                    Modifier.fillMaxWidth().height(58.dp).clip(RoundedCornerShape(999.dp)).background(Ink).clickable { navController.navigate("player/$key") },
                    horizontalArrangement = Arrangement.Center, verticalAlignment = Alignment.CenterVertically,
                ) {
                    Icon(Icons.Filled.PlayArrow, contentDescription = null, tint = Color.White)
                    Spacer(Modifier.width(6.dp))
                    Text("Start workout", color = Color.White, fontSize = 15.5.sp, fontWeight = FontWeight.SemiBold)
                }
            }
        }
    }
}

@Composable
private fun Fact(value: String, label: String) {
    Column {
        Text(value, color = Color.White, fontSize = 24.sp, fontWeight = FontWeight.Bold)
        Text(label, color = Color.White.copy(alpha = 0.55f), fontSize = 11.5.sp)
    }
}
