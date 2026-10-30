package com.glowup.app.ui.screens.communication

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
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
import com.glowup.app.ui.components.DetailHeader
import com.glowup.app.ui.data.LessonStore
import com.glowup.app.ui.theme.*

@Composable
fun CommunicationScreen(navController: NavHostController) {
    val lessons = LessonStore.lessons
    val doneCount = lessons.count { LessonStore.isDone(it.id) }
    val byTrack = lessons.groupBy { it.track }

    Box(modifier = Modifier.fillMaxSize().background(Surface).systemBarsPadding()) {
        Column(modifier = Modifier.fillMaxSize()) {
            DetailHeader("Communication") { navController.popBackStack() }

            LazyColumn(
                modifier = Modifier.weight(1f),
                contentPadding = PaddingValues(24.dp, 16.dp, 24.dp, 24.dp),
            ) {
                item {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(28.dp))
                            .background(Ink)
                            .padding(18.dp),
                    ) {
                        Text("SPEAKING PRACTICE", color = Color.White.copy(alpha = 0.55f), fontSize = 10.5.sp, fontWeight = FontWeight.SemiBold)
                        Spacer(Modifier.height(3.dp))
                        Text("Talk for 1 minute on a random word", color = Color.White, fontSize = 17.sp, fontWeight = FontWeight.Bold, lineHeight = 22.sp)
                        Spacer(Modifier.height(14.dp))
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(50.dp)
                                .clip(RoundedCornerShape(999.dp))
                                .background(Color.White)
                                .clickable { navController.navigate("speaking_practice") },
                            horizontalArrangement = Arrangement.Center,
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Box(modifier = Modifier.size(12.dp).clip(CircleShape).background(Coral))
                            Spacer(Modifier.width(10.dp))
                            Text("Start practice", color = Ink, fontSize = 14.5.sp, fontWeight = FontWeight.SemiBold)
                        }
                        Spacer(Modifier.height(8.dp))
                        Text(
                            "Your recording is never saved or uploaded.",
                            color = Color.White.copy(alpha = 0.45f),
                            fontSize = 11.sp,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.fillMaxWidth(),
                        )
                    }
                    Spacer(Modifier.height(22.dp))
                }

                item {
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                        Text("Your lessons", fontSize = 16.sp, fontWeight = FontWeight.SemiBold)
                        Text("$doneCount of ${lessons.size} done", color = TextSecondary, fontSize = 12.5.sp, fontWeight = FontWeight.Medium)
                    }
                    Spacer(Modifier.height(14.dp))
                }

                byTrack.forEach { (track, trackLessons) ->
                    item {
                        Text(track.uppercase(), color = TextSecondary, fontSize = 11.5.sp, fontWeight = FontWeight.SemiBold, letterSpacing = 0.4.sp)
                        Spacer(Modifier.height(10.dp))
                    }
                    items(trackLessons, key = { it.id }) { lesson ->
                        val done = LessonStore.isDone(lesson.id)
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(bottom = 10.dp)
                                .clip(RoundedCornerShape(20.dp))
                                .background(if (lesson.pro) CardNeutral else lesson.tint)
                                .clickable { navController.navigate("lesson_detail/${lesson.id}") }
                                .padding(14.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                        ) {
                            if (lesson.pro) {
                                Box(
                                    modifier = Modifier.size(40.dp).clip(CircleShape).background(Color(0xFFE6E3DC)),
                                    contentAlignment = Alignment.Center,
                                ) {
                                    Text("🔒", fontSize = 14.sp)
                                }
                            } else {
                                Image(painterResource(lesson.icon), contentDescription = null, modifier = Modifier.size(40.dp))
                            }
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    if (lesson.pro) "PREMIUM" else if (done) "DONE" else "${lesson.minutes} MIN",
                                    color = if (lesson.pro || done) TextSecondary else lesson.accent,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.SemiBold,
                                )
                                Text(
                                    lesson.title,
                                    color = if (lesson.pro) TextSecondary else Ink,
                                    fontSize = 15.5.sp,
                                    fontWeight = FontWeight.SemiBold,
                                )
                            }
                            if (done) {
                                Text("✓", color = Green, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                    item { Spacer(Modifier.height(8.dp)) }
                }
            }
        }
    }
}
