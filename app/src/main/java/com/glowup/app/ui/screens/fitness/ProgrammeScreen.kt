package com.glowup.app.ui.screens.fitness

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bedtime
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import com.glowup.app.ui.components.DetailHeader
import com.glowup.app.ui.components.ProLockedScreen
import com.glowup.app.ui.components.goBack
import com.glowup.app.ui.data.ProGate
import com.glowup.app.ui.data.fitness.FitnessStore
import com.glowup.app.ui.data.fitness.Programmes
import com.glowup.app.ui.theme.*

@Composable
fun ProgrammeScreen(navController: NavHostController, id: String) {
    val programme = Programmes.get(id) ?: return
    if (ProGate.programmeLocked(id)) {
        ProLockedScreen(navController, programme.title, "${programme.title} is part of Pro — along with every 30-day programme, longer sessions and auto-progression.")
        return
    }
    val done = FitnessStore.daysDone(id)
    val next = FitnessStore.nextDay(id)
    val active = FitnessStore.activeProgramme == id
    val preview = programme.dayPlan(next ?: 1, FitnessStore.level, FitnessStore::bonusFor)

    Box(Modifier.fillMaxSize().background(Surface).systemBarsPadding()) {
        Column(Modifier.fillMaxSize()) {
            DetailHeader(programme.title) { navController.goBack() }
            Column(Modifier.weight(1f).verticalScroll(rememberScrollState()).padding(24.dp, 18.dp, 24.dp, 24.dp)) {
                Column(Modifier.fillMaxWidth().clip(RoundedCornerShape(28.dp)).background(programme.focus.tint).padding(20.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(Modifier.size(46.dp).clip(CircleShape).background(Color.White), contentAlignment = Alignment.Center) {
                            Icon(programme.focus.icon(), contentDescription = null, tint = programme.focus.accent)
                        }
                        Spacer(Modifier.width(12.dp))
                        Column {
                            Text(programme.tagline.uppercase(), color = programme.focus.accent, fontSize = 11.sp, fontWeight = FontWeight.Bold, letterSpacing = 0.4.sp)
                            Text("30 days · ${30 - programme.restDays.size} workouts", fontSize = 13.sp, color = TextSecondary)
                        }
                    }
                    Spacer(Modifier.height(12.dp))
                    Text(programme.about, fontSize = 14.sp, lineHeight = 20.sp)
                    Spacer(Modifier.height(14.dp))
                    Box(Modifier.fillMaxWidth().height(8.dp).clip(RoundedCornerShape(999.dp)).background(Color.White)) {
                        Box(Modifier.fillMaxWidth(done.size / 30f).fillMaxHeight().clip(RoundedCornerShape(999.dp)).background(programme.focus.accent))
                    }
                    Text(if (next == null) "Completed — all 30 days!" else "${done.size} of 30 days done", color = programme.focus.accent, fontSize = 12.sp, fontWeight = FontWeight.SemiBold, modifier = Modifier.padding(top = 6.dp))
                }

                programme.honestNote?.let { note ->
                    Spacer(Modifier.height(12.dp))
                    Row(Modifier.fillMaxWidth().clip(RoundedCornerShape(18.dp)).background(CardNeutral).padding(14.dp, 12.dp)) {
                        Icon(Icons.Outlined.Info, contentDescription = null, tint = Ink, modifier = Modifier.size(18.dp))
                        Spacer(Modifier.width(10.dp))
                        Text(note, fontSize = 12.5.sp, lineHeight = 18.sp, color = Ink.copy(alpha = 0.8f))
                    }
                }

                Spacer(Modifier.height(22.dp))
                Text("Your 30 days", fontSize = 17.sp, fontWeight = FontWeight.Bold)
                Text("Tap any finished or upcoming day to preview it", color = TextSecondary, fontSize = 12.sp, modifier = Modifier.padding(top = 2.dp, bottom = 12.dp))
                (1..30).chunked(5).forEach { row ->
                    Row(Modifier.fillMaxWidth().padding(bottom = 8.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        row.forEach { day ->
                            val isDone = day in done
                            val isNext = day == next
                            val rest = programme.isRestDay(day)
                            Box(
                                Modifier
                                    .weight(1f)
                                    .aspectRatio(1f)
                                    .clip(RoundedCornerShape(16.dp))
                                    .background(
                                        when {
                                            isDone -> programme.focus.accent
                                            isNext -> Ink
                                            rest -> CardNeutral
                                            else -> programme.focus.tint
                                        },
                                    )
                                    .then(if (isNext) Modifier.border(2.dp, Ink, RoundedCornerShape(16.dp)) else Modifier)
                                    .clickable(enabled = !rest) { navController.navigate("workout/p.$id.$day") },
                                contentAlignment = Alignment.Center,
                            ) {
                                when {
                                    isDone -> Icon(Icons.Filled.Check, contentDescription = "Day $day done", tint = Color.White, modifier = Modifier.size(18.dp))
                                    rest -> Icon(Icons.Filled.Bedtime, contentDescription = "Day $day rest", tint = TextMuted, modifier = Modifier.size(16.dp))
                                    day == 30 -> Icon(Icons.Filled.EmojiEvents, contentDescription = "Day 30", tint = if (isNext) Color.White else programme.focus.accent, modifier = Modifier.size(18.dp))
                                    else -> Text("$day", color = if (isNext) Color.White else Ink, fontSize = 14.sp, fontWeight = FontWeight.Bold, textAlign = TextAlign.Center)
                                }
                            }
                        }
                    }
                }

                if (next != null && !preview.restDay) {
                    Spacer(Modifier.height(14.dp))
                    Text("Up next · ${preview.title}", fontSize = 15.sp, fontWeight = FontWeight.Bold)
                    Spacer(Modifier.height(8.dp))
                    preview.blocks.forEach { b ->
                        Row(Modifier.fillMaxWidth().padding(vertical = 5.dp), verticalAlignment = Alignment.CenterVertically) {
                            ExerciseArt(b.exercise, 40.dp, 12.dp)
                            Spacer(Modifier.width(12.dp))
                            Text(b.exercise.name, fontSize = 14.sp, fontWeight = FontWeight.SemiBold, modifier = Modifier.weight(1f))
                            Text(if (b.max) "Max" else "${b.sets} × ${b.exercise.doseLabel(b.target)}", color = TextSecondary, fontSize = 12.5.sp)
                        }
                    }
                }
            }

            Column(Modifier.padding(24.dp, 8.dp, 24.dp, 24.dp)) {
                Box(
                    Modifier.fillMaxWidth().height(56.dp).clip(RoundedCornerShape(999.dp)).background(Ink).clickable {
                        if (!active) FitnessStore.startProgramme(id)
                        if (next != null) {
                            if (preview.restDay) navController.goBack() else navController.navigate("workout/${preview.key}")
                        }
                    },
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        when {
                            next == null -> if (active) "Programme complete" else "Make this my programme"
                            !active -> if (done.isEmpty()) "Start programme" else "Switch to this · resume day $next"
                            preview.restDay -> "Today is a rest day"
                            else -> "Start day $next"
                        },
                        color = Color.White, fontSize = 15.sp, fontWeight = FontWeight.SemiBold,
                    )
                }
                if (active && next != null) {
                    Text(
                        "Stop this programme", color = TextSecondary, fontSize = 13.sp, fontWeight = FontWeight.SemiBold,
                        modifier = Modifier.align(Alignment.CenterHorizontally).padding(top = 12.dp).clickable { FitnessStore.stopProgramme() },
                    )
                }
            }
        }
    }
}
