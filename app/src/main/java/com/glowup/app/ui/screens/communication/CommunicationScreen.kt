package com.glowup.app.ui.screens.communication

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import com.glowup.app.R
import com.glowup.app.ui.components.DetailHeader
import com.glowup.app.ui.components.GlowUpDialog
import com.glowup.app.ui.theme.*

@Composable
fun CommunicationScreen(navController: NavHostController) {
    var showLesson1Done by remember { mutableStateOf(false) }
    var showPremiumLocked by remember { mutableStateOf(false) }

    Box(modifier = Modifier.fillMaxSize().background(Surface).systemBarsPadding()) {
        Column(modifier = Modifier.fillMaxSize()) {
            DetailHeader("Communication") { navController.popBackStack() }

            Column(modifier = Modifier.weight(1f).padding(24.dp, 16.dp, 24.dp, 24.dp)) {

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

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                    Text("Your lessons", fontSize = 16.sp, fontWeight = FontWeight.SemiBold)
                    Text("1 of 3 done", color = TextSecondary, fontSize = 12.5.sp, fontWeight = FontWeight.Medium)
                }

                Spacer(Modifier.height(12.dp))

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(20.dp))
                        .background(GreenTint)
                        .clickable { showLesson1Done = true }
                        .padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    Image(painterResource(R.drawable.ic_check), contentDescription = null, modifier = Modifier.size(40.dp))
                    Column {
                        Text("LESSON 1 · DONE", color = Color(0xFF1C8C4E), fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
                        Text("Get less nervous", fontSize = 15.5.sp, fontWeight = FontWeight.SemiBold)
                    }
                }

                Spacer(Modifier.height(10.dp))

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(20.dp))
                        .background(BlueTint)
                        .clickable { navController.navigate("speaking_practice") }
                        .padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    Image(painterResource(R.drawable.ic_talking), contentDescription = null, modifier = Modifier.size(40.dp))
                    Column {
                        Text("LESSON 2 · UP NEXT", color = Navy, fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
                        Text("Stop mumbling", fontSize = 15.5.sp, fontWeight = FontWeight.SemiBold)
                    }
                }

                Spacer(Modifier.height(10.dp))

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(20.dp))
                        .background(CardNeutral)
                        .clickable { showPremiumLocked = true }
                        .padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    Box(
                        modifier = Modifier.size(40.dp).clip(CircleShape).background(Color(0xFFE6E3DC)),
                        contentAlignment = Alignment.Center,
                    ) {
                        Text("🔒", fontSize = 14.sp)
                    }
                    Column {
                        Text("LESSON 3 · PREMIUM", color = TextSecondary, fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
                        Text("Speak with confidence", color = TextSecondary, fontSize = 15.5.sp, fontWeight = FontWeight.SemiBold)
                    }
                }
            }
        }

        if (showLesson1Done) {
            GlowUpDialog(
                onDismissRequest = { showLesson1Done = false },
                title = "Lesson 1 · Done",
                message = "\"Get less nervous\" — completed. Up next: Stop mumbling.",
                primaryText = "Nice",
                secondaryText = null,
            )
        }

        if (showPremiumLocked) {
            GlowUpDialog(
                onDismissRequest = { showPremiumLocked = false },
                title = "Speak with confidence",
                message = "This lesson is part of Glow-Up PRO. Upgrade to unlock all premium lessons.",
                secondaryText = "Maybe later",
                primaryText = "Upgrade",
            )
        }
    }
}
