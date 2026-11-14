package com.glowup.app.ui.screens.lesson

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
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
import com.glowup.app.ui.components.DetailHeader
import com.glowup.app.ui.components.ProLockedScreen
import com.glowup.app.ui.components.ReMakeUDialog
import com.glowup.app.ui.components.goBack
import com.glowup.app.ui.data.LessonStore
import com.glowup.app.ui.data.ProGate
import com.glowup.app.ui.design.AppButton
import com.glowup.app.ui.design.ButtonVariant
import com.glowup.app.ui.design.LineHeight
import com.glowup.app.ui.design.Shapes
import com.glowup.app.ui.design.Space
import com.glowup.app.ui.design.TextSize
import com.glowup.app.ui.theme.*
import java.net.URLEncoder

@Composable
fun LessonDetailScreen(navController: NavHostController, id: Int) {
    val lesson = LessonStore.find(id) ?: return
    if (ProGate.talkLessonLocked(id)) {
        ProLockedScreen(navController, lesson.title, "${lesson.title} is a Pro lesson")
        return
    }
    var showDone by remember { mutableStateOf(false) }
    val done = LessonStore.isDone(lesson.id)

    Box(modifier = Modifier.fillMaxSize().background(Surface).systemBarsPadding()) {
        Column(modifier = Modifier.fillMaxSize()) {
            DetailHeader(lesson.track) { navController.goBack() }

            Column(modifier = Modifier.weight(1f).verticalScroll(rememberScrollState()).padding(Space.Gutter, Space.L, Space.Gutter, Space.Gutter)) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(Shapes.Hero)
                        .background(lesson.tint)
                        .padding(Space.Xl)
                ) {
                    Row(horizontalArrangement = Arrangement.spacedBy(Space.S), verticalAlignment = Alignment.CenterVertically) {
                        Image(painterResource(lesson.icon), contentDescription = null, modifier = Modifier.size(34.dp))
                    }
                    Spacer(Modifier.height(Space.M))
                    Text(lesson.title, fontSize = TextSize.Headline, fontWeight = FontWeight.Bold, lineHeight = LineHeight.Display)
                    Spacer(Modifier.height(Space.S))
                    Text(lesson.tagline, color = TextSecondary, fontSize = TextSize.Body, lineHeight = LineHeight.Body)
                    Spacer(Modifier.height(Space.M))
                    Text(
                        stringResource(R.string.lesson_detail_1_s_min_2_s, lesson.minutes, lesson.points),
                        color = lesson.accent,
                        fontSize = TextSize.BodySmall,
                        fontWeight = FontWeight.SemiBold
                    )
                }

                Spacer(Modifier.height(Space.Xl))
                Text(stringResource(R.string.lesson_detail_what_to_know), fontSize = TextSize.Title, fontWeight = FontWeight.SemiBold)
                Spacer(Modifier.height(Space.S))

                lesson.keyPoints.forEachIndexed { index, point ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = Space.S)
                            .clip(Shapes.Medium)
                            .background(CardNeutral)
                            .padding(Space.L, Space.M),
                        horizontalArrangement = Arrangement.spacedBy(Space.M)
                    ) {
                        Box(
                            modifier = Modifier.size(24.dp).clip(CircleShape).background(lesson.tint),
                            contentAlignment = Alignment.Center
                        ) {
                            Text("${index + 1}", color = lesson.accent, fontSize = TextSize.Caption, fontWeight = FontWeight.Bold)
                        }
                        Text(point, fontSize = TextSize.Body, fontWeight = FontWeight.Medium, lineHeight = LineHeight.Body)
                    }
                }

                Spacer(Modifier.height(Space.M))

                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(Shapes.Large)
                        .background(Ink)
                        .padding(Space.Xl)
                ) {
                    Text(
                        stringResource(R.string.lesson_detail_practice_it),
                        color = Color.White.copy(alpha = 0.55f),
                        fontSize = TextSize.Micro,
                        fontWeight = FontWeight.SemiBold
                    )
                    Spacer(Modifier.height(Space.Xs))
                    Text(stringResource(R.string.lesson_detail_talk_for_1_minute_about), color = Color.White, fontSize = TextSize.Body)
                    Text(
                        "\"${lesson.practiceWord}\"",
                        color = Color.White,
                        fontSize = TextSize.Title,
                        fontWeight = FontWeight.Bold,
                        lineHeight = LineHeight.Relaxed
                    )
                    Spacer(Modifier.height(Space.M))
                    AppButton(stringResource(R.string.common_start_practice), variant = ButtonVariant.OnDarkPrimary) {
                        val encoded = URLEncoder.encode(lesson.practiceWord, "UTF-8")
                        navController.navigate("speaking_practice/$encoded")
                    }
                }

                Spacer(Modifier.height(Space.Xl))

                AppButton(
                    if (done) stringResource(R.string.lesson_detail_lesson_completed) else stringResource(R.string.lesson_detail_mark_as_done),
                    variant = if (done) ButtonVariant.Secondary else ButtonVariant.Primary,
                    enabled = !done
                ) {
                    LessonStore.markDone(lesson.id)
                    showDone = true
                }
            }
        }

        if (showDone) {
            val next = LessonStore.nextUpAfter(lesson.id)
            ReMakeUDialog(
                onDismissRequest = { showDone = false },
                title = "${lesson.title} · Done",
                message = if (next != null) "Nice work. Up next: ${next.title}." else "Nice work. You've completed every lesson in this track.",
                primaryText = "Nice",
                secondaryText = null
            )
        }
    }
}
