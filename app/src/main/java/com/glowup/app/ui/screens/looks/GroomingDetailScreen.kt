package com.glowup.app.ui.screens.looks

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import com.glowup.app.R
import com.glowup.app.ui.components.DetailHeader
import com.glowup.app.ui.components.ProLockedScreen
import com.glowup.app.ui.components.ReMakeUDialog
import com.glowup.app.ui.components.goBack
import com.glowup.app.ui.data.GroomingStore
import com.glowup.app.ui.data.ProGate
import com.glowup.app.ui.data.ProfileStore
import com.glowup.app.ui.data.RoutineStore
import com.glowup.app.ui.design.LineHeight
import com.glowup.app.ui.design.Shapes
import com.glowup.app.ui.design.Space
import com.glowup.app.ui.design.TextSize
import com.glowup.app.ui.theme.*

private const val FACE_SHAPE_LESSON_ID = 5

@Composable
fun GroomingDetailScreen(navController: NavHostController, id: Int) {
    val lesson = GroomingStore.find(id) ?: return
    if (ProGate.groomingLessonLocked(id)) {
        ProLockedScreen(navController, lesson.title, "${lesson.title} is a Pro guide")
        return
    }
    var showDone by remember { mutableStateOf(false) }
    var showAddedToRoutine by remember { mutableStateOf(false) }
    val done = GroomingStore.isDone(lesson.id)

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
                        stringResource(R.string.grooming_detail_1_s_min_read_2, lesson.minutes, lesson.points),
                        color = lesson.accent,
                        fontSize = TextSize.BodySmall,
                        fontWeight = FontWeight.SemiBold
                    )
                }

                Spacer(Modifier.height(Space.Xl))
                if (lesson.id == FACE_SHAPE_LESSON_ID) {
                    val shape = ProfileStore.faceShape
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(Shapes.Card)
                            .background(if (shape != null) Ink else CardNeutral)
                            .clickable(enabled = shape == null) { navController.navigate("settings") }
                            .padding(Space.L, Space.L)
                    ) {
                        if (shape != null) {
                            Text(
                                stringResource(R.string.grooming_detail_for_your_1_s_face, shape.uppercase()),
                                color = Color.White.copy(alpha = 0.6f),
                                fontSize = TextSize.Micro,
                                fontWeight = FontWeight.SemiBold,
                                letterSpacing = 0.4.sp
                            )
                            Spacer(Modifier.height(Space.S))
                            Text(
                                GroomingStore.faceShapeTips[shape].orEmpty(),
                                color = Color.White,
                                fontSize = TextSize.BodyLarge,
                                lineHeight = LineHeight.Relaxed,
                                fontWeight = FontWeight.Medium
                            )
                        } else {
                            Text(
                                stringResource(R.string.grooming_detail_get_advice_for_your_face),
                                fontSize = TextSize.BodyLarge,
                                fontWeight = FontWeight.SemiBold
                            )
                            Text(stringResource(R.string.grooming_detail_set_your_face_shape_in), color = TextSecondary, fontSize = TextSize.BodySmall)
                        }
                    }
                    Spacer(Modifier.height(Space.L))
                }
                Text(stringResource(R.string.common_routine_how_to_do_it), fontSize = TextSize.Title, fontWeight = FontWeight.SemiBold)
                Spacer(Modifier.height(Space.S))

                lesson.steps.forEachIndexed { index, point ->
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

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(Shapes.Card)
                        .background(Ink)
                        .clickable {
                            val routineCategory = if (lesson.track == "Posture & Smile") "Mind" else "Looks"
                            RoutineStore.add(routineCategory, lesson.title)
                            showAddedToRoutine = true
                        }
                        .padding(Space.L, Space.L),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(Space.M)
                ) {
                    Text("+", color = Color.White, fontSize = TextSize.TitleLarge, fontWeight = FontWeight.Bold)
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            stringResource(R.string.grooming_detail_turn_into_a_routine),
                            color = Color.White,
                            fontSize = TextSize.Body,
                            fontWeight = FontWeight.SemiBold
                        )
                        Text(
                            stringResource(R.string.grooming_detail_track_it_daily_from_your),
                            color = Color.White.copy(alpha = 0.55f),
                            fontSize = TextSize.Caption
                        )
                    }
                }
                Spacer(Modifier.height(Space.Xl))

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(58.dp)
                        .clip(Shapes.Pill)
                        .background(if (done) ChipNeutral else Ink)
                        .clickable(enabled = !done) {
                            GroomingStore.markDone(lesson.id)
                            showDone = true
                        },
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        if (done) stringResource(R.string.grooming_detail_marked_as_read) else stringResource(R.string.grooming_detail_mark_as_read),
                        color = if (done) Ink else Color.White,
                        fontSize = TextSize.BodyLarge,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
        }

        if (showDone) {
            ReMakeUDialog(
                onDismissRequest = { showDone = false },
                title = "${lesson.title} · Read",
                message = "Nice work. Keep building your ${lesson.track.lowercase()} habits.",
                primaryText = "Nice",
                secondaryText = null
            )
        }

        if (showAddedToRoutine) {
            ReMakeUDialog(
                onDismissRequest = { showAddedToRoutine = false },
                title = "Added to routines",
                message = "\"${lesson.title}\" is now in your Routines tab.",
                primaryText = "Nice",
                secondaryText = null
            )
        }
    }
}
