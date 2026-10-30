package com.glowup.app.ui.screens.morning

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Air
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.SelfImprovement
import androidx.compose.material.icons.filled.SportsGymnastics
import androidx.compose.material3.Icon
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import com.glowup.app.ui.theme.*

private data class MorningActivity(val title: String, val meta: String, val tint: Color, val iconTint: Color, val icon: ImageVector)

private val activities = listOf(
    MorningActivity("Guided meditation", "5 min · Audio", PurpleTint, Purple, Icons.Filled.SelfImprovement),
    MorningActivity("Breathing exercise", "2 min · Guided", BlueTint, Navy, Icons.Filled.Air),
    MorningActivity("Stretching", "4 min · Animation", GreenTint, Green, Icons.Filled.SportsGymnastics),
    MorningActivity("Simple yoga flow", "6 min · Video", CoralTint, Coral, Icons.Filled.SelfImprovement),
)

@Composable
fun MorningScreen(navController: NavHostController) {
    var playingActivity by remember { mutableStateOf<String?>(null) }

    Box(modifier = Modifier.fillMaxSize().background(Surface).systemBarsPadding()) {
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
                        .background(Ink)
                        .clickable { navController.popBackStack() },
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = Color.White, modifier = Modifier.size(18.dp))
                }
                Text("Good morning", fontSize = 18.sp, fontWeight = FontWeight.Bold)
                Spacer(Modifier.size(48.dp))
            }

            Text(
                "A few minutes to start the day right — pick what fits.",
                color = TextSecondary,
                fontSize = 13.5.sp,
                lineHeight = 19.sp,
                modifier = Modifier.padding(24.dp, 14.dp, 24.dp, 0.dp),
            )

            LazyColumn(
                modifier = Modifier.weight(1f),
                contentPadding = PaddingValues(24.dp, 18.dp, 24.dp, 24.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                items(activities) { activity ->
                    val isPlaying = playingActivity == activity.title
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(22.dp))
                            .background(activity.tint)
                            .clickable { playingActivity = if (isPlaying) null else activity.title }
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(14.dp),
                    ) {
                        Box(
                            modifier = Modifier.size(48.dp).clip(CircleShape).background(Color.White),
                            contentAlignment = Alignment.Center,
                        ) {
                            Icon(activity.icon, contentDescription = null, tint = activity.iconTint, modifier = Modifier.size(22.dp))
                        }
                        Column(modifier = Modifier.weight(1f)) {
                            Text(activity.title, fontSize = 15.sp, fontWeight = FontWeight.SemiBold)
                            Text(if (isPlaying) "Playing..." else activity.meta, color = if (isPlaying) activity.iconTint else TextSecondary, fontSize = 12.sp)
                        }
                        Box(
                            modifier = Modifier.size(36.dp).clip(CircleShape).background(Ink),
                            contentAlignment = Alignment.Center,
                        ) {
                            Icon(
                                if (isPlaying) Icons.Filled.Pause else Icons.Filled.PlayArrow,
                                contentDescription = if (isPlaying) "Pause" else "Play",
                                tint = Color.White,
                                modifier = Modifier.size(18.dp),
                            )
                        }
                    }
                }

                item {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 6.dp)
                            .clip(RoundedCornerShape(20.dp))
                            .background(CardNeutral)
                            .padding(16.dp, 14.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Text("Attach any of these to a routine card", color = TextSecondary, fontSize = 13.sp, modifier = Modifier.weight(1f))
                        Text(
                            "Add →",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.SemiBold,
                            modifier = Modifier.clickable { navController.navigate("add_routine") },
                        )
                    }
                }
            }
        }
    }
}
