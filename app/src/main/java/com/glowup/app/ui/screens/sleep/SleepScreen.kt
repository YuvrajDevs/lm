package com.glowup.app.ui.screens.sleep

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.border
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
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
import com.glowup.app.ui.components.GlowUpOptionRow
import com.glowup.app.ui.theme.*

private data class SoundTile(val label: String, val tint: Color, val icon: Int)

private val sounds = listOf(
    SoundTile("Night Rain", BlueTint, R.drawable.ic_moon),
    SoundTile("Forest", GreenTint, R.drawable.ic_tree),
    SoundTile("White Noise", PurpleTint, R.drawable.ic_ocean),
    SoundTile("Ocean Waves", CoralTint, R.drawable.ic_ocean),
)

@Composable
fun SleepScreen(navController: NavHostController) {
    var selectedSound by remember { mutableStateOf("Night Rain") }
    var timerMinutes by remember { mutableStateOf(30) }
    var showTimerDialog by remember { mutableStateOf(false) }
    var isPlaying by remember { mutableStateOf(false) }

    Box(modifier = Modifier.fillMaxSize().background(Surface).systemBarsPadding()) {
        Column(modifier = Modifier.fillMaxSize()) {
            DetailHeader("Sleep") { navController.popBackStack() }

            LazyColumn(
                modifier = Modifier.weight(1f),
                contentPadding = PaddingValues(24.dp, 16.dp, 24.dp, 16.dp),
            ) {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(28.dp))
                            .background(PurpleTint)
                            .padding(20.dp, 18.dp, 86.dp, 18.dp),
                    ) {
                        Column(modifier = Modifier.fillMaxWidth(0.72f)) {
                            Text("YOU USUALLY SLEEP BY", color = Color(0xFF6B4FD1), fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
                            Spacer(Modifier.height(2.dp))
                            Text("11:00 PM", fontSize = 26.sp, fontWeight = FontWeight.Bold)
                            Spacer(Modifier.height(2.dp))
                            Text("About 8 hours before your alarm.", color = Color(0xFF5A4A8C), fontSize = 12.5.sp)
                        }
                        Image(painterResource(R.drawable.ic_sleep), contentDescription = null, modifier = Modifier.align(Alignment.BottomEnd).size(78.dp))
                    }
                    Spacer(Modifier.height(14.dp))
                }

                item {
                    Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        listOf("Bedtime alarm" to "11:00 PM", "Wake-up alarm" to "7:00 AM").forEach { (label, time) ->
                            Column(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(22.dp))
                                    .background(CardNeutral)
                                    .padding(16.dp, 14.dp),
                                verticalArrangement = Arrangement.spacedBy(6.dp),
                            ) {
                                Text(label, color = TextSecondary, fontSize = 11.5.sp, fontWeight = FontWeight.Medium)
                                Text(time, fontSize = 18.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                    Spacer(Modifier.height(22.dp))
                }

                item {
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                        Text("Sleep sounds", fontSize = 15.sp, fontWeight = FontWeight.SemiBold)
                        Text("Offline · Free", color = TextSecondary, fontSize = 12.sp, fontWeight = FontWeight.Medium)
                    }
                    Spacer(Modifier.height(12.dp))
                }

                items(sounds.chunked(2)) { pair ->
                    Row(horizontalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.fillMaxWidth()) {
                        pair.forEach { sound ->
                            val isSelected = selectedSound == sound.label
                            Column(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(22.dp))
                                    .background(sound.tint)
                                    .border(2.dp, if (isSelected) Ink else Color.Transparent, RoundedCornerShape(22.dp))
                                    .clickable { selectedSound = sound.label }
                                    .padding(16.dp),
                                verticalArrangement = Arrangement.spacedBy(16.dp),
                            ) {
                                Image(painterResource(sound.icon), contentDescription = null, modifier = Modifier.size(28.dp))
                                Text(sound.label, fontSize = 13.5.sp, fontWeight = FontWeight.SemiBold)
                            }
                        }
                    }
                    Spacer(Modifier.height(10.dp))
                }

                item {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(22.dp))
                            .background(CardNeutral)
                            .clickable { showTimerDialog = true }
                            .padding(18.dp, 16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Column {
                            Text("Sleep timer", fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
                            Text("Stops after $timerMinutes minutes", color = TextSecondary, fontSize = 12.sp)
                        }
                        Icon(Icons.Filled.ChevronRight, contentDescription = null, tint = TextSecondary, modifier = Modifier.size(16.dp))
                    }
                    Spacer(Modifier.height(12.dp))
                }

                item {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(22.dp))
                            .background(Yellow)
                            .clickable { navController.navigate("morning") }
                            .padding(18.dp, 16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Column {
                            Text("Morning routine", fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
                            Text("Meditation, breathing & stretching", color = Color(0xFF7A6600), fontSize = 12.sp)
                        }
                        Icon(Icons.Filled.ChevronRight, contentDescription = null, tint = Ink, modifier = Modifier.size(16.dp))
                    }
                }
            }

            Box(modifier = Modifier.padding(24.dp, 8.dp, 24.dp, 28.dp)) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(60.dp)
                        .clip(RoundedCornerShape(999.dp))
                        .background(Ink)
                        .clickable { isPlaying = !isPlaying },
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        if (isPlaying) "Stop $selectedSound" else "Start sleep sound",
                        color = Color.White,
                        fontSize = 15.5.sp,
                        fontWeight = FontWeight.SemiBold,
                    )
                }
            }
        }

        if (showTimerDialog) {
            GlowUpDialog(
                onDismissRequest = { showTimerDialog = false },
                title = "Sleep timer",
                primaryText = "Close",
                secondaryText = null,
                content = {
                    Column {
                        listOf(15, 30, 45, 60, 90).forEach { minutes ->
                            GlowUpOptionRow("$minutes minutes", selected = minutes == timerMinutes) { timerMinutes = minutes }
                        }
                    }
                },
            )
        }
    }
}
