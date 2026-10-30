package com.glowup.app.ui.screens.confidence

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import com.glowup.app.R
import com.glowup.app.ui.components.DetailHeader
import com.glowup.app.ui.theme.*

private data class ConfidenceCard(val title: String, val points: String, val done: Boolean, val tint: Color = CardNeutral, val mascot: Int? = null)

private val cards = listOf(
    ConfidenceCard("Make eye contact today", "+8 points", true),
    ConfidenceCard("Message a friend or family member", "+8 points", true),
    ConfidenceCard("Compliment someone genuinely", "+8 points", true),
    ConfidenceCard("Start one new conversation", "+9 points", false, CoralTint, R.drawable.ic_talking),
    ConfidenceCard("Speak out loud for 2 minutes", "+9 points", false, BlueTint, R.drawable.ic_talking),
)

@Composable
fun ConfidenceScreen(navController: NavHostController) {
    var doneTitles by remember { mutableStateOf(cards.filter { it.done }.map { it.title }.toSet()) }
    val doneCount = doneTitles.size
    val points = doneCount * 8 + 2

    Box(modifier = Modifier.fillMaxSize().background(Surface).systemBarsPadding()) {
        Column(modifier = Modifier.fillMaxSize()) {
            DetailHeader("Confidence cards") { navController.popBackStack() }

            Box(
                modifier = Modifier
                    .padding(24.dp, 14.dp, 24.dp, 0.dp)
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(28.dp))
                    .background(GreenTint)
                    .padding(20.dp, 18.dp, 80.dp, 18.dp),
            ) {
                Column {
                    Text("THIS WEEK", color = Color(0xFF1C8C4E), fontSize = 11.5.sp, fontWeight = FontWeight.SemiBold)
                    Spacer(Modifier.height(2.dp))
                    Text("$doneCount of ${cards.size} cards done", fontSize = 22.sp, fontWeight = FontWeight.Bold)
                    Spacer(Modifier.height(2.dp))
                    Text("$points points · Level 2", color = Color(0xFF5C8C6E), fontSize = 12.5.sp, fontWeight = FontWeight.Medium)
                }
                Image(painterResource(R.drawable.ic_social), contentDescription = null, modifier = Modifier.align(Alignment.BottomEnd).size(64.dp))
            }

            Text(
                "Points are just for you — never compared with anyone else.",
                color = TextSecondary,
                fontSize = 12.sp,
                lineHeight = 17.sp,
                modifier = Modifier.padding(24.dp, 12.dp, 24.dp, 0.dp),
            )

            LazyColumn(
                modifier = Modifier.weight(1f),
                contentPadding = PaddingValues(24.dp, 14.dp, 24.dp, 24.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                items(cards) { card ->
                    val isDone = card.title in doneTitles
                    if (isDone) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(22.dp))
                                .background(card.tint)
                                .clickable { doneTitles = doneTitles - card.title }
                                .padding(16.dp, 14.dp)
                                .alpha(0.5f),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(14.dp),
                        ) {
                            Image(painterResource(R.drawable.ic_check), contentDescription = null, modifier = Modifier.size(26.dp))
                            Column {
                                Text(card.title, fontSize = 14.5.sp, fontWeight = FontWeight.SemiBold, textDecoration = TextDecoration.LineThrough)
                                Text(card.points, color = TextSecondary, fontSize = 12.sp)
                            }
                        }
                    } else {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(22.dp))
                                .background(card.tint)
                                .clickable { doneTitles = doneTitles + card.title }
                                .padding(16.dp, 14.dp, 78.dp, 14.dp),
                        ) {
                            Column {
                                Text(card.title, fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
                                Text(card.points, color = TextSecondary, fontSize = 11.5.sp)
                            }
                            card.mascot?.let {
                                Image(painterResource(it), contentDescription = null, modifier = Modifier.align(Alignment.CenterEnd).size(52.dp))
                            }
                        }
                    }
                }
            }
        }
    }
}
