package com.glowup.app.ui.screens.communication

import com.glowup.app.ui.components.ProBadge
import com.glowup.app.ui.data.ProStore
import com.glowup.app.ui.data.ProGate
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
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
import com.glowup.app.ui.components.goBack
import com.glowup.app.ui.components.DetailHeader
import com.glowup.app.ui.data.LessonStore
import com.glowup.app.ui.theme.*
import com.glowup.app.ui.design.LineHeight
import com.glowup.app.ui.design.Shapes
import com.glowup.app.ui.design.TextSize

@Composable
fun CommunicationScreen(navController: NavHostController) {
    val lessons = LessonStore.lessons
    val doneCount = lessons.count { LessonStore.isDone(it.id) }
    val byTrack = lessons.groupBy { it.track }

    Box(modifier = Modifier.fillMaxSize().background(Surface).systemBarsPadding()) {
        Column(modifier = Modifier.fillMaxSize()) {
            DetailHeader("Communication") { navController.goBack() }

            LazyColumn(
                modifier = Modifier.weight(1f),
                contentPadding = PaddingValues(24.dp, 16.dp, 24.dp, 24.dp),
            ) {
                item {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(Shapes.Hero)
                            .background(Ink)
                            .padding(18.dp),
                    ) {
                        Text("SPEAKING PRACTICE", color = Color.White.copy(alpha = 0.55f), fontSize = TextSize.Micro, fontWeight = FontWeight.SemiBold)
                        Spacer(Modifier.height(3.dp))
                        Text("Talk for 1 minute — get your pace, fillers and pauses", color = Color.White, fontSize = TextSize.Title, fontWeight = FontWeight.Bold, lineHeight = LineHeight.Relaxed)
                        Spacer(Modifier.height(14.dp))
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(50.dp)
                                .clip(Shapes.Pill)
                                .background(Color.White)
                                .clickable { navController.navigate("speaking_practice") },
                            horizontalArrangement = Arrangement.Center,
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Box(modifier = Modifier.size(12.dp).clip(CircleShape).background(Coral))
                            Spacer(Modifier.width(10.dp))
                            Text("Start practice", color = Ink, fontSize = TextSize.BodyLarge, fontWeight = FontWeight.SemiBold)
                        }
                        Spacer(Modifier.height(8.dp))
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Text(
                                "30 scenarios", color = Color.White, fontSize = TextSize.BodySmall, fontWeight = FontWeight.SemiBold, textAlign = TextAlign.Center,
                                modifier = Modifier.weight(1f).clip(Shapes.Pill).background(Color.White.copy(alpha = 0.12f)).clickable { navController.navigate("scenarios") }.padding(vertical = 11.dp),
                            )
                            Text(
                                "Your progress", color = Color.White, fontSize = TextSize.BodySmall, fontWeight = FontWeight.SemiBold, textAlign = TextAlign.Center,
                                modifier = Modifier.weight(1f).clip(Shapes.Pill).background(Color.White.copy(alpha = 0.12f)).clickable { navController.navigate("speech_history") }.padding(vertical = 11.dp),
                            )
                        }
                    }
                    Spacer(Modifier.height(22.dp))
                }

                item {
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                        Text("Your lessons", fontSize = TextSize.Title, fontWeight = FontWeight.SemiBold)
                        Text("$doneCount of ${lessons.size} done", color = TextSecondary, fontSize = TextSize.BodySmall, fontWeight = FontWeight.Medium)
                    }
                    Spacer(Modifier.height(14.dp))
                }

                byTrack.forEach { (track, trackLessons) ->
                    item {
                        Text(track.uppercase(), color = TextSecondary, fontSize = TextSize.Caption, fontWeight = FontWeight.SemiBold, letterSpacing = 0.4.sp)
                        Spacer(Modifier.height(10.dp))
                    }
                    items(trackLessons, key = { it.id }) { lesson ->
                        val done = LessonStore.isDone(lesson.id)
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(bottom = 10.dp)
                                .clip(Shapes.Card)
                                .background(lesson.tint)
                                .clickable {
                                    if (ProGate.talkLessonLocked(lesson.id)) ProStore.showPaywall("${lesson.title} is a Pro lesson")
                                    else navController.navigate("lesson_detail/${lesson.id}")
                                }
                                .padding(14.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                        ) {
                            Image(painterResource(lesson.icon), contentDescription = null, modifier = Modifier.size(40.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    if (done) "DONE" else "${lesson.minutes} MIN",
                                    color = if (done) TextSecondary else lesson.accent,
                                    fontSize = TextSize.Micro,
                                    fontWeight = FontWeight.SemiBold,
                                )
                                Text(
                                    lesson.title,
                                    color = Ink,
                                    fontSize = TextSize.BodyLarge,
                                    fontWeight = FontWeight.SemiBold,
                                )
                            }
                            if (ProGate.talkLessonLocked(lesson.id)) {
                                ProBadge()
                            } else if (done) {
                                Text("✓", color = Green, fontSize = TextSize.Title, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                    item { Spacer(Modifier.height(8.dp)) }
                }
            }
        }
    }
}
