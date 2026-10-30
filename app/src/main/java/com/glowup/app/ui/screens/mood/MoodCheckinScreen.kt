package com.glowup.app.ui.screens.mood

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
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
import com.glowup.app.ui.data.MoodStore
import com.glowup.app.ui.data.moodOptions
import com.glowup.app.ui.theme.*

@Composable
fun MoodCheckinScreen(navController: NavHostController) {
    var selected by remember { mutableStateOf<String?>(null) }
    var note by remember { mutableStateOf("") }

    Box(modifier = Modifier.fillMaxSize().background(Surface).systemBarsPadding()) {
        Column(modifier = Modifier.fillMaxSize()) {
            Row(
                modifier = Modifier.fillMaxWidth().padding(24.dp, 12.dp, 24.dp, 0.dp),
                horizontalArrangement = Arrangement.End,
            ) {
                Text(
                    "Skip",
                    color = TextSecondary,
                    fontSize = 13.5.sp,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.clickable { navController.popBackStack() },
                )
            }

            Column(modifier = Modifier.padding(24.dp, 4.dp, 24.dp, 0.dp)) {
                Text("FRIDAY, 30 OCTOBER", color = TextSecondary, fontSize = 11.sp, fontWeight = FontWeight.SemiBold, letterSpacing = 0.4.sp)
                Spacer(Modifier.height(6.dp))
                Text(
                    "How are you",
                    fontSize = 27.sp,
                    fontWeight = FontWeight.Normal,
                    lineHeight = 33.sp,
                )
                Text(
                    "feeling today?",
                    fontSize = 27.sp,
                    fontWeight = FontWeight.Bold,
                    lineHeight = 33.sp,
                )
            }

            LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                modifier = Modifier.weight(1f),
                contentPadding = PaddingValues(24.dp, 12.dp, 24.dp, 0.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                items(moodOptions) { mood ->
                    val isSelected = selected == mood.label
                    Column(
                        modifier = Modifier
                            .clip(RoundedCornerShape(24.dp))
                            .background(if (isSelected) ChipNeutral else Color(0xFFFAF9F6))
                            .border(2.dp, if (isSelected) Ink else Color.Transparent, RoundedCornerShape(24.dp))
                            .clickable { selected = mood.label }
                            .padding(16.dp)
                            .fillMaxWidth(),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(10.dp),
                    ) {
                        Image(painterResource(mood.icon), contentDescription = null, modifier = Modifier.size(64.dp))
                        Text(mood.label, fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
                    }
                }
            }

            Box(
                modifier = Modifier
                    .padding(24.dp, 18.dp, 24.dp, 0.dp)
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(20.dp))
                    .background(CardNeutral)
                    .padding(16.dp, 4.dp),
            ) {
                OutlinedTextField(
                    value = note,
                    onValueChange = { note = it },
                    placeholder = { Text("Add a quick note (optional)", color = TextSecondary, fontSize = 13.5.sp) },
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Color.Transparent,
                        unfocusedBorderColor = Color.Transparent,
                        focusedContainerColor = Color.Transparent,
                        unfocusedContainerColor = Color.Transparent,
                    ),
                )
            }

            Box(modifier = Modifier.padding(24.dp, 16.dp, 24.dp, 28.dp)) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(60.dp)
                        .clip(RoundedCornerShape(999.dp))
                        .background(if (selected != null) Ink else Color(0xFFD8D5CE))
                        .clickable(enabled = selected != null) {
                            selected?.let { MoodStore.logToday(it) }
                            navController.popBackStack()
                        },
                    contentAlignment = Alignment.Center,
                ) {
                    Text("Continue", color = Color.White, fontSize = 15.5.sp, fontWeight = FontWeight.SemiBold)
                }
            }
        }
    }
}
