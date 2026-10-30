package com.glowup.app.ui.screens.speaking

import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import com.glowup.app.ui.theme.Coral
import com.glowup.app.ui.theme.Ink
import kotlin.random.Random

private val randomTopics = listOf(
    "Weekend", "A food you love", "Your favourite season", "A skill you want to learn",
    "The last trip you took", "A movie that stuck with you", "Your morning routine",
)

@Composable
fun SpeakingPracticeScreen(navController: NavHostController, topic: String? = null) {
    val displayTopic = remember { topic ?: randomTopics[Random.nextInt(randomTopics.size)] }
    val transition = rememberInfiniteTransition(label = "pulse")
    val scale by transition.animateFloat(
        initialValue = 1f,
        targetValue = 1.15f,
        animationSpec = infiniteRepeatable(tween(900), RepeatMode.Reverse),
        label = "scale",
    )
    val alpha by transition.animateFloat(
        initialValue = 0.5f,
        targetValue = 0.15f,
        animationSpec = infiniteRepeatable(tween(900), RepeatMode.Reverse),
        label = "alpha",
    )

    Box(modifier = Modifier.fillMaxSize().background(Ink).systemBarsPadding()) {
        Column(modifier = Modifier.fillMaxSize()) {
            Row(
                modifier = Modifier.fillMaxWidth().padding(24.dp, 24.dp, 24.dp, 0.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .clip(CircleShape)
                        .background(Color(0xFF1F1F22))
                        .clickable { navController.popBackStack() },
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = Color.White, modifier = Modifier.size(18.dp))
                }
                Text("0:38 left", color = Color.White.copy(alpha = 0.6f), fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
                Spacer(Modifier.size(48.dp))
            }

            Column(
                modifier = Modifier.weight(1f).fillMaxWidth().padding(horizontal = 24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center,
            ) {
                Text(
                    "TALK FOR 1 MINUTE ABOUT",
                    color = Color.White.copy(alpha = 0.45f),
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold,
                    letterSpacing = 0.5.sp,
                    textAlign = TextAlign.Center,
                )
                Spacer(Modifier.height(8.dp))
                Text(
                    "\"$displayTopic\"",
                    color = Color.White,
                    fontSize = if (displayTopic.length > 16) 24.sp else 34.sp,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center,
                )

                Spacer(Modifier.height(28.dp))

                Box(modifier = Modifier.size(160.dp), contentAlignment = Alignment.Center) {
                    Box(
                        modifier = Modifier
                            .size(160.dp)
                            .scale(scale)
                            .alpha(alpha)
                            .clip(CircleShape)
                            .background(Coral),
                    )
                    Box(
                        modifier = Modifier.size(110.dp).clip(CircleShape).background(Coral),
                        contentAlignment = Alignment.Center,
                    ) {
                        Icon(Icons.Filled.Mic, contentDescription = null, tint = Color.White, modifier = Modifier.size(36.dp))
                    }
                }

                Spacer(Modifier.height(28.dp))

                Text(
                    "Your voice is only yours. Nothing is saved or uploaded — it's gone the moment you leave this screen.",
                    color = Color.White.copy(alpha = 0.45f),
                    fontSize = 13.sp,
                    lineHeight = 19.sp,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.widthIn(max = 260.dp),
                )
            }

            Box(modifier = Modifier.padding(24.dp, 0.dp, 24.dp, 40.dp)) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(60.dp)
                        .clip(RoundedCornerShape(999.dp))
                        .background(Color.White)
                        .clickable { navController.popBackStack() },
                    contentAlignment = Alignment.Center,
                ) {
                    Text("Stop & listen back", color = Ink, fontSize = 15.5.sp, fontWeight = FontWeight.SemiBold)
                }
            }
        }
    }
}
