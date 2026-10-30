package com.glowup.app.ui.screens.looks

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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import com.glowup.app.R
import com.glowup.app.ui.components.DetailHeader
import com.glowup.app.ui.data.GroomingStore
import com.glowup.app.ui.theme.*

@Composable
fun LooksScreen(navController: NavHostController) {
    val lessons = GroomingStore.lessons
    val doneCount = lessons.count { GroomingStore.isDone(it.id) }
    val byTrack = lessons.groupBy { it.track }

    Box(modifier = Modifier.fillMaxSize().background(Surface).systemBarsPadding()) {
        Column(modifier = Modifier.fillMaxSize()) {
            DetailHeader("Looks & Grooming") { navController.popBackStack() }

            LazyColumn(
                modifier = Modifier.weight(1f),
                contentPadding = PaddingValues(24.dp, 16.dp, 24.dp, 24.dp),
            ) {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(28.dp))
                            .background(Ink)
                            .padding(20.dp, 18.dp, 86.dp, 18.dp),
                    ) {
                        Column {
                            Text("GLOW-UP GUIDES", color = Color.White.copy(alpha = 0.55f), fontSize = 10.5.sp, fontWeight = FontWeight.SemiBold)
                            Spacer(Modifier.height(4.dp))
                            Text("Skincare, hair, grooming & posture", color = Color.White, fontSize = 17.sp, fontWeight = FontWeight.Bold, lineHeight = 22.sp)
                            Spacer(Modifier.height(6.dp))
                            Text("$doneCount of ${lessons.size} guides read", color = Color.White.copy(alpha = 0.55f), fontSize = 12.5.sp)
                        }
                        Image(painterResource(R.drawable.ic_determined), contentDescription = null, modifier = Modifier.align(Alignment.BottomEnd).size(70.dp))
                    }
                    Spacer(Modifier.height(22.dp))
                }

                byTrack.forEach { (track, trackLessons) ->
                    item {
                        Text(track.uppercase(), color = TextSecondary, fontSize = 11.5.sp, fontWeight = FontWeight.SemiBold, letterSpacing = 0.4.sp)
                        Spacer(Modifier.height(10.dp))
                    }
                    items(trackLessons, key = { it.id }) { lesson ->
                        val done = GroomingStore.isDone(lesson.id)
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(bottom = 10.dp)
                                .clip(RoundedCornerShape(20.dp))
                                .background(if (lesson.pro) CardNeutral else lesson.tint)
                                .clickable { navController.navigate("grooming_detail/${lesson.id}") }
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
                                    if (lesson.pro) "PREMIUM" else if (done) "READ" else "${lesson.minutes} MIN",
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

                item { Spacer(Modifier.height(90.dp)) }
            }
        }
    }
}
