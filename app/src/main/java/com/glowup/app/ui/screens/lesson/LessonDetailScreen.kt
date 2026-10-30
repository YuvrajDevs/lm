package com.glowup.app.ui.screens.lesson

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
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
import com.glowup.app.R
import com.glowup.app.ui.components.DetailHeader
import com.glowup.app.ui.components.GlowUpDialog
import com.glowup.app.ui.data.LessonStore
import com.glowup.app.ui.theme.*
import java.net.URLEncoder

@Composable
fun LessonDetailScreen(navController: NavHostController, id: Int) {
    val lesson = LessonStore.find(id) ?: return
    var showLocked by remember { mutableStateOf(false) }
    var showDone by remember { mutableStateOf(false) }
    val done = LessonStore.isDone(lesson.id)

    Box(modifier = Modifier.fillMaxSize().background(Surface).systemBarsPadding()) {
        Column(modifier = Modifier.fillMaxSize()) {
            DetailHeader(lesson.track) { navController.popBackStack() }

            Column(modifier = Modifier.weight(1f).verticalScroll(rememberScrollState()).padding(24.dp, 16.dp, 24.dp, 24.dp)) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(28.dp))
                        .background(lesson.tint)
                        .padding(22.dp),
                ) {
                    Row(horizontalArrangement = Arrangement.spacedBy(10.dp), verticalAlignment = Alignment.CenterVertically) {
                        Image(painterResource(lesson.icon), contentDescription = null, modifier = Modifier.size(34.dp))
                        if (lesson.pro) {
                            Box(modifier = Modifier.clip(RoundedCornerShape(999.dp)).background(Ink).padding(8.dp, 3.dp)) {
                                Text("PRO", color = Color.White, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                    Spacer(Modifier.height(14.dp))
                    Text(lesson.title, fontSize = 24.sp, fontWeight = FontWeight.Bold, lineHeight = 29.sp)
                    Spacer(Modifier.height(8.dp))
                    Text(lesson.tagline, color = TextSecondary, fontSize = 14.sp, lineHeight = 20.sp)
                    Spacer(Modifier.height(14.dp))
                    Text("${lesson.minutes} min · +${lesson.points} points", color = lesson.accent, fontSize = 12.5.sp, fontWeight = FontWeight.SemiBold)
                }

                Spacer(Modifier.height(22.dp))
                Text("What to know", fontSize = 16.sp, fontWeight = FontWeight.SemiBold)
                Spacer(Modifier.height(10.dp))

                lesson.keyPoints.forEachIndexed { index, point ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 8.dp)
                            .clip(RoundedCornerShape(18.dp))
                            .background(CardNeutral)
                            .padding(16.dp, 14.dp),
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                    ) {
                        Box(
                            modifier = Modifier.size(24.dp).clip(CircleShape).background(lesson.tint),
                            contentAlignment = Alignment.Center,
                        ) {
                            Text("${index + 1}", color = lesson.accent, fontSize = 11.5.sp, fontWeight = FontWeight.Bold)
                        }
                        Text(point, fontSize = 14.sp, fontWeight = FontWeight.Medium, lineHeight = 20.sp)
                    }
                }

                Spacer(Modifier.height(14.dp))

                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(24.dp))
                        .background(Ink)
                        .padding(20.dp),
                ) {
                    Text("PRACTICE IT", color = Color.White.copy(alpha = 0.55f), fontSize = 10.5.sp, fontWeight = FontWeight.SemiBold)
                    Spacer(Modifier.height(4.dp))
                    Text("Talk for 1 minute about:", color = Color.White, fontSize = 13.5.sp)
                    Text("\"${lesson.practiceWord}\"", color = Color.White, fontSize = 17.sp, fontWeight = FontWeight.Bold, lineHeight = 22.sp)
                    Spacer(Modifier.height(14.dp))
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp)
                            .clip(RoundedCornerShape(999.dp))
                            .background(Color.White)
                            .clickable {
                                if (lesson.pro) {
                                    showLocked = true
                                } else {
                                    val encoded = URLEncoder.encode(lesson.practiceWord, "UTF-8")
                                    navController.navigate("speaking_practice/$encoded")
                                }
                            },
                        contentAlignment = Alignment.Center,
                    ) {
                        Text("Start practice", color = Ink, fontSize = 14.5.sp, fontWeight = FontWeight.SemiBold)
                    }
                }

                Spacer(Modifier.height(20.dp))

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(58.dp)
                        .clip(RoundedCornerShape(999.dp))
                        .background(if (done) ChipNeutral else Ink)
                        .clickable(enabled = !done) {
                            LessonStore.markDone(lesson.id)
                            showDone = true
                        },
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        if (done) "Lesson completed" else "Mark as done",
                        color = if (done) Ink else Color.White,
                        fontSize = 14.5.sp,
                        fontWeight = FontWeight.SemiBold,
                    )
                }
            }
        }

        if (showLocked) {
            GlowUpDialog(
                onDismissRequest = { showLocked = false },
                title = lesson.title,
                message = "This lesson is part of Glow-Up PRO. Upgrade to unlock all premium lessons.",
                secondaryText = "Maybe later",
                primaryText = "Upgrade",
            )
        }

        if (showDone) {
            val next = LessonStore.nextUpAfter(lesson.id)
            GlowUpDialog(
                onDismissRequest = { showDone = false },
                title = "${lesson.title} · Done",
                message = if (next != null) "Nice work. Up next: ${next.title}." else "Nice work. You've completed every lesson in this track.",
                primaryText = "Nice",
                secondaryText = null,
            )
        }
    }
}
