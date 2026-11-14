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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import com.glowup.app.ui.components.goBack
import com.glowup.app.ui.components.DetailHeader
import com.glowup.app.ui.components.ReMakeUDialog
import com.glowup.app.ui.data.GroomingStore
import com.glowup.app.ui.data.ProfileStore
import com.glowup.app.ui.data.RoutineStore
import com.glowup.app.ui.theme.*
import com.glowup.app.ui.design.LineHeight
import com.glowup.app.ui.design.Shapes
import com.glowup.app.ui.design.TextSize
import com.glowup.app.ui.components.ProLockedScreen
import com.glowup.app.ui.data.ProGate

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

            Column(modifier = Modifier.weight(1f).verticalScroll(rememberScrollState()).padding(24.dp, 16.dp, 24.dp, 24.dp)) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(Shapes.Hero)
                        .background(lesson.tint)
                        .padding(22.dp),
                ) {
                    Row(horizontalArrangement = Arrangement.spacedBy(10.dp), verticalAlignment = Alignment.CenterVertically) {
                        Image(painterResource(lesson.icon), contentDescription = null, modifier = Modifier.size(34.dp))
                    }
                    Spacer(Modifier.height(14.dp))
                    Text(lesson.title, fontSize = TextSize.Headline, fontWeight = FontWeight.Bold, lineHeight = LineHeight.Display)
                    Spacer(Modifier.height(8.dp))
                    Text(lesson.tagline, color = TextSecondary, fontSize = TextSize.Body, lineHeight = LineHeight.Body)
                    Spacer(Modifier.height(14.dp))
                    Text("${lesson.minutes} min read · +${lesson.points} points", color = lesson.accent, fontSize = TextSize.BodySmall, fontWeight = FontWeight.SemiBold)
                }

                Spacer(Modifier.height(22.dp))
                if (lesson.id == FACE_SHAPE_LESSON_ID) {
                    val shape = ProfileStore.faceShape
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(Shapes.Card)
                            .background(if (shape != null) Ink else CardNeutral)
                            .clickable(enabled = shape == null) { navController.navigate("settings") }
                            .padding(18.dp, 16.dp),
                    ) {
                        if (shape != null) {
                            Text("FOR YOUR ${shape.uppercase()} FACE", color = Color.White.copy(alpha = 0.6f), fontSize = TextSize.Micro, fontWeight = FontWeight.SemiBold, letterSpacing = 0.4.sp)
                            Spacer(Modifier.height(6.dp))
                            Text(GroomingStore.faceShapeTips[shape].orEmpty(), color = Color.White, fontSize = TextSize.BodyLarge, lineHeight = LineHeight.Relaxed, fontWeight = FontWeight.Medium)
                        } else {
                            Text("Get advice for your face shape", fontSize = TextSize.BodyLarge, fontWeight = FontWeight.SemiBold)
                            Text("Set your face shape in Settings → Profile", color = TextSecondary, fontSize = TextSize.BodySmall)
                        }
                    }
                    Spacer(Modifier.height(18.dp))
                }
                Text("How to do it", fontSize = TextSize.Title, fontWeight = FontWeight.SemiBold)
                Spacer(Modifier.height(10.dp))

                lesson.steps.forEachIndexed { index, point ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 8.dp)
                            .clip(Shapes.Medium)
                            .background(CardNeutral)
                            .padding(16.dp, 14.dp),
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                    ) {
                        Box(
                            modifier = Modifier.size(24.dp).clip(CircleShape).background(lesson.tint),
                            contentAlignment = Alignment.Center,
                        ) {
                            Text("${index + 1}", color = lesson.accent, fontSize = TextSize.Caption, fontWeight = FontWeight.Bold)
                        }
                        Text(point, fontSize = TextSize.Body, fontWeight = FontWeight.Medium, lineHeight = LineHeight.Body)
                    }
                }

                Spacer(Modifier.height(14.dp))

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
                        .padding(18.dp, 16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    Text("+", color = Color.White, fontSize = TextSize.TitleLarge, fontWeight = FontWeight.Bold)
                    Column(modifier = Modifier.weight(1f)) {
                        Text("Turn into a routine", color = Color.White, fontSize = TextSize.Body, fontWeight = FontWeight.SemiBold)
                        Text("Track it daily from your Routines tab", color = Color.White.copy(alpha = 0.55f), fontSize = TextSize.Caption)
                    }
                }
                Spacer(Modifier.height(20.dp))

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
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        if (done) "Marked as read" else "Mark as read",
                        color = if (done) Ink else Color.White,
                        fontSize = TextSize.BodyLarge,
                        fontWeight = FontWeight.SemiBold,
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
                secondaryText = null,
            )
        }

        if (showAddedToRoutine) {
            ReMakeUDialog(
                onDismissRequest = { showAddedToRoutine = false },
                title = "Added to routines",
                message = "\"${lesson.title}\" is now in your Routines tab.",
                primaryText = "Nice",
                secondaryText = null,
            )
        }
    }
}
