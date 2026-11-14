package com.glowup.app.ui.screens.looks

import com.glowup.app.ui.components.ProBadge
import com.glowup.app.ui.data.ProStore
import com.glowup.app.ui.data.ProGate
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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
import com.glowup.app.ui.components.goBack
import com.glowup.app.R
import com.glowup.app.ui.components.DetailHeader
import com.glowup.app.ui.data.GroomingStore
import com.glowup.app.ui.theme.*
import com.glowup.app.ui.design.LineHeight
import com.glowup.app.ui.design.Shapes
import com.glowup.app.ui.design.TextSize
import com.glowup.app.ui.data.ProfileStore
import com.glowup.app.ui.data.glow.GlowCheckStore

@Composable
fun LooksScreen(navController: NavHostController) {
    val lessons = GroomingStore.lessons
    val doneCount = lessons.count { GroomingStore.isDone(it.id) }
    val byTrack = lessons.groupBy { it.track }

    Box(modifier = Modifier.fillMaxSize().background(Surface).systemBarsPadding()) {
        Column(modifier = Modifier.fillMaxSize()) {
            DetailHeader("Looks & Grooming") { navController.goBack() }

            LazyColumn(
                modifier = Modifier.weight(1f),
                contentPadding = PaddingValues(24.dp, 16.dp, 24.dp, 24.dp),
            ) {
                item {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(Shapes.Hero)
                            .background(Ink)
                            .padding(20.dp, 18.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text("GROOMING GUIDES", color = Color.White.copy(alpha = 0.55f), fontSize = TextSize.Micro, fontWeight = FontWeight.SemiBold)
                            Spacer(Modifier.height(4.dp))
                            Text("Skincare, hair, grooming & posture", color = Color.White, fontSize = TextSize.Title, fontWeight = FontWeight.Bold, lineHeight = LineHeight.Relaxed)
                            Spacer(Modifier.height(6.dp))
                            Text("$doneCount of ${lessons.size} guides read", color = Color.White.copy(alpha = 0.55f), fontSize = TextSize.BodySmall)
                        }
                        Image(painterResource(R.drawable.ic_determined), contentDescription = null, modifier = Modifier.size(64.dp))
                    }
                    Spacer(Modifier.height(14.dp))
                    GlowToolkit(navController)
                    Spacer(Modifier.height(22.dp))
                }

                byTrack.forEach { (track, trackLessons) ->
                    item {
                        Text(track.uppercase(), color = TextSecondary, fontSize = TextSize.Caption, fontWeight = FontWeight.SemiBold, letterSpacing = 0.4.sp)
                        Spacer(Modifier.height(10.dp))
                    }
                    items(trackLessons, key = { it.id }) { lesson ->
                        val done = GroomingStore.isDone(lesson.id)
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(bottom = 10.dp)
                                .clip(Shapes.Card)
                                .background(lesson.tint)
                                .clickable {
                                    if (ProGate.groomingLessonLocked(lesson.id)) ProStore.showPaywall("${lesson.title} is a Pro guide")
                                    else navController.navigate("grooming_detail/${lesson.id}")
                                }
                                .padding(14.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                        ) {
                            Image(painterResource(lesson.icon), contentDescription = null, modifier = Modifier.size(40.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    if (done) "READ" else "${lesson.minutes} MIN",
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
                            if (ProGate.groomingLessonLocked(lesson.id)) {
                                ProBadge()
                            } else if (done) {
                                Text("✓", color = Green, fontSize = TextSize.Title, fontWeight = FontWeight.Bold)
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

/** The personal tools: the check, face-shape advice, routines and photos. */
@Composable
private fun GlowToolkit(navController: NavHostController) {
    val latest = GlowCheckStore.latest
    data class Tool(val title: String, val sub: String, val tint: Color, val route: String)
    val tools = listOf(
        Tool(if (latest == null) "Glow Check" else "Your map", if (latest == null) "3-min scan & plan" else latest.focus.firstOrNull()?.area?.title ?: "All good", Ink, if (latest == null) "glow_check" else "glow_results"),
        Tool("Face shape", ProfileStore.faceShape ?: "Cuts & beards", PurpleTint, "face_shape"),
        Tool("Routines", "With evidence labels", GreenTint, "glow_routines"),
        Tool("Progress photos", "Private & side-by-side", BlueTint, "progress_photos"),
    )
    tools.chunked(2).forEach { row ->
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.padding(bottom = 10.dp)) {
            row.forEach { t ->
                val dark = t.tint == Ink
                Column(Modifier.weight(1f).clip(Shapes.Card).background(t.tint).clickable { navController.navigate(t.route) }.padding(16.dp, 14.dp)) {
                    Text(t.title, color = if (dark) Color.White else Ink, fontSize = TextSize.BodyLarge, fontWeight = FontWeight.Bold)
                    Text(t.sub, color = if (dark) Color.White.copy(alpha = 0.6f) else TextSecondary, fontSize = TextSize.Caption, maxLines = 1)
                }
            }
        }
    }
}
