package com.glowup.app.ui.screens.communication

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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import com.glowup.app.R
import com.glowup.app.ui.components.DetailHeader
import com.glowup.app.ui.components.ProBadge
import com.glowup.app.ui.components.goBack
import com.glowup.app.ui.data.LessonStore
import com.glowup.app.ui.data.ProGate
import com.glowup.app.ui.data.ProStore
import com.glowup.app.ui.design.AppButton
import com.glowup.app.ui.design.ButtonVariant
import com.glowup.app.ui.design.LineHeight
import com.glowup.app.ui.design.Shapes
import com.glowup.app.ui.design.Space
import com.glowup.app.ui.design.TextSize
import com.glowup.app.ui.theme.*

@Composable
fun CommunicationScreen(navController: NavHostController) {
    val lessons = LessonStore.lessons
    val doneCount = lessons.count { LessonStore.isDone(it.id) }
    val byTrack = lessons.groupBy { it.track }

    Box(modifier = Modifier.fillMaxSize().background(Surface).systemBarsPadding()) {
        Column(modifier = Modifier.fillMaxSize()) {
            DetailHeader(stringResource(R.string.communication_communication)) { navController.goBack() }

            LazyColumn(
                modifier = Modifier.weight(1f),
                contentPadding = PaddingValues(Space.Gutter, Space.L, Space.Gutter, Space.Gutter)
            ) {
                item {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(Shapes.Hero)
                            .background(Ink)
                            .padding(Space.L)
                    ) {
                        Text(
                            stringResource(R.string.communication_speaking_practice),
                            color = Color.White.copy(alpha = 0.55f),
                            fontSize = TextSize.Micro,
                            fontWeight = FontWeight.SemiBold
                        )
                        Spacer(Modifier.height(Space.Xs))
                        Text(
                            stringResource(R.string.communication_talk_for_1_minute_get),
                            color = Color.White,
                            fontSize = TextSize.Title,
                            fontWeight = FontWeight.Bold,
                            lineHeight = LineHeight.Relaxed
                        )
                        Spacer(Modifier.height(Space.M))
                        AppButton(stringResource(R.string.common_start_practice), variant = ButtonVariant.OnDarkPrimary) {
                            navController.navigate("speaking_practice")
                        }
                        Spacer(Modifier.height(Space.S))
                        Row(horizontalArrangement = Arrangement.spacedBy(Space.S)) {
                            Text(
                                stringResource(R.string.communication_30_scenarios),
                                color = Color.White,
                                fontSize = TextSize.BodySmall,
                                fontWeight = FontWeight.SemiBold,
                                textAlign = TextAlign.Center,
                                modifier = Modifier.weight(1f).clip(Shapes.Pill).background(Color.White.copy(alpha = 0.12f)).clickable {
                                    navController.navigate("scenarios")
                                }.padding(vertical = Space.M)
                            )
                            Text(
                                stringResource(R.string.communication_your_progress),
                                color = Color.White,
                                fontSize = TextSize.BodySmall,
                                fontWeight = FontWeight.SemiBold,
                                textAlign = TextAlign.Center,
                                modifier = Modifier.weight(1f).clip(Shapes.Pill).background(Color.White.copy(alpha = 0.12f)).clickable {
                                    navController.navigate("speech_history")
                                }.padding(vertical = Space.M)
                            )
                        }
                    }
                    Spacer(Modifier.height(Space.Xl))
                }

                item {
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                        Text(stringResource(R.string.communication_your_lessons), fontSize = TextSize.Title, fontWeight = FontWeight.SemiBold)
                        Text(
                            stringResource(R.string.common_1_s_of_2_s, doneCount, lessons.size),
                            color = TextSecondary,
                            fontSize = TextSize.BodySmall,
                            fontWeight = FontWeight.Medium
                        )
                    }
                    Spacer(Modifier.height(Space.M))
                }

                byTrack.forEach { (track, trackLessons) ->
                    item {
                        Text(track.uppercase(), color = TextSecondary, fontSize = TextSize.Caption, fontWeight = FontWeight.SemiBold, letterSpacing = 0.4.sp)
                        Spacer(Modifier.height(Space.S))
                    }
                    items(trackLessons, key = { it.id }) { lesson ->
                        val done = LessonStore.isDone(lesson.id)
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(bottom = Space.S)
                                .clip(Shapes.Card)
                                .background(lesson.tint)
                                .clickable {
                                    if (ProGate.talkLessonLocked(lesson.id)) {
                                        ProStore.showPaywall("${lesson.title} is a Pro lesson")
                                    } else {
                                        navController.navigate("lesson_detail/${lesson.id}")
                                    }
                                }
                                .padding(Space.M),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(Space.M)
                        ) {
                            Image(painterResource(lesson.icon), contentDescription = null, modifier = Modifier.size(40.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    if (done) stringResource(R.string.communication_done) else stringResource(R.string.common_1_s_min_2, lesson.minutes),
                                    color = if (done) TextSecondary else lesson.accent,
                                    fontSize = TextSize.Micro,
                                    fontWeight = FontWeight.SemiBold
                                )
                                Text(
                                    lesson.title,
                                    color = Ink,
                                    fontSize = TextSize.BodyLarge,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }
                            if (ProGate.talkLessonLocked(lesson.id)) {
                                ProBadge()
                            } else if (done) {
                                Text("✓", color = Green, fontSize = TextSize.Title, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                    item { Spacer(Modifier.height(Space.S)) }
                }
            }
        }
    }
}
