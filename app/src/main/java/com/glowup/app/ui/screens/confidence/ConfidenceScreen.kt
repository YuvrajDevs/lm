package com.glowup.app.ui.screens.confidence

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.glowup.app.R
import com.glowup.app.ui.components.DetailHeader
import com.glowup.app.ui.components.goBack
import com.glowup.app.ui.data.ConfidenceStore
import com.glowup.app.ui.design.LineHeight
import com.glowup.app.ui.design.Shapes
import com.glowup.app.ui.design.Space
import com.glowup.app.ui.design.TextSize
import com.glowup.app.ui.theme.*

private data class ConfidenceCard(val title: String, val points: Int, val tint: Color = CardNeutral, val mascot: Int? = null)

private val cards = listOf(
    ConfidenceCard("Make eye contact today", 8),
    ConfidenceCard("Message a friend or family member", 8),
    ConfidenceCard("Compliment someone genuinely", 8),
    ConfidenceCard("Start one new conversation", 9, CoralTint, R.drawable.ic_talking),
    ConfidenceCard("Speak out loud for 2 minutes", 9, BlueTint, R.drawable.ic_talking),
    ConfidenceCard("Ask a question in a group setting", 9, GreenTint, R.drawable.ic_social),
    ConfidenceCard("Say no to something you don't want to do", 10, PurpleTint, R.drawable.ic_mind),
    ConfidenceCard("Sit or stand with open, relaxed posture today", 8),
    ConfidenceCard("Share an opinion before someone asks for it", 10, CoralTint, R.drawable.ic_talking),
    ConfidenceCard("Introduce yourself to someone new", 9, BlueTint, R.drawable.ic_talking),
    ConfidenceCard("Accept a compliment without deflecting it", 8),
    ConfidenceCard("Ask for help with something small", 8, GreenTint, R.drawable.ic_social)
)

@Composable
fun ConfidenceScreen(navController: NavHostController) {
    val doneCards = cards.filter { ConfidenceStore.isDone(it.title) }
    val doneCount = doneCards.size
    val points = doneCards.sumOf { it.points }

    Box(modifier = Modifier.fillMaxSize().background(Surface).systemBarsPadding()) {
        Column(modifier = Modifier.fillMaxSize()) {
            DetailHeader(stringResource(R.string.confidence_confidence_cards)) { navController.goBack() }

            Box(
                modifier = Modifier
                    .padding(Space.Gutter, Space.M, Space.Gutter, 0.dp)
                    .fillMaxWidth()
                    .clip(Shapes.Hero)
                    .background(GreenTint)
                    .padding(Space.Xl, Space.L, 80.dp, Space.L)
            ) {
                Column(modifier = Modifier.padding(end = 64.dp)) {
                    Text(stringResource(R.string.common_this_week), color = Color(0xFF1C8C4E), fontSize = TextSize.Caption, fontWeight = FontWeight.SemiBold)
                    Spacer(Modifier.height(2.dp))
                    Text(stringResource(R.string.confidence_1_s_of_2_s, doneCount, cards.size), fontSize = TextSize.TitleLarge, fontWeight = FontWeight.Bold)
                    Spacer(Modifier.height(2.dp))
                    Text(
                        stringResource(R.string.confidence_1_s_points_this_week, points),
                        color = Color(0xFF5C8C6E),
                        fontSize = TextSize.BodySmall,
                        fontWeight = FontWeight.Medium
                    )
                }
                Image(painterResource(R.drawable.ic_social), contentDescription = null, modifier = Modifier.align(Alignment.BottomEnd).size(64.dp))
            }

            Text(
                stringResource(R.string.confidence_points_are_just_for_you),
                color = TextSecondary,
                fontSize = TextSize.Caption,
                lineHeight = LineHeight.Compact,
                modifier = Modifier.padding(Space.Gutter, Space.M, Space.Gutter, 0.dp)
            )

            LazyColumn(
                modifier = Modifier.weight(1f),
                contentPadding = PaddingValues(Space.Gutter, Space.M, Space.Gutter, Space.Gutter),
                verticalArrangement = Arrangement.spacedBy(Space.S)
            ) {
                items(cards) { card ->
                    val isDone = card in doneCards
                    if (isDone) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(Shapes.Card)
                                .background(card.tint)
                                .clickable { ConfidenceStore.toggle(card.title, card.points) }
                                .padding(Space.L, Space.M)
                                .alpha(0.5f),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(Space.M)
                        ) {
                            Image(painterResource(R.drawable.ic_check), contentDescription = null, modifier = Modifier.size(26.dp))
                            Column {
                                Text(card.title, fontSize = TextSize.BodyLarge, fontWeight = FontWeight.SemiBold, textDecoration = TextDecoration.LineThrough)
                                Text(stringResource(R.string.common_1_s_points, card.points), color = TextSecondary, fontSize = TextSize.Caption)
                            }
                        }
                    } else {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(Shapes.Card)
                                .background(card.tint)
                                .clickable { ConfidenceStore.toggle(card.title, card.points) }
                                .padding(Space.L, Space.M, Space.L, Space.M)
                        ) {
                            Column(modifier = Modifier.padding(end = if (card.mascot != null) 62.dp else 0.dp)) {
                                Text(card.title, fontSize = TextSize.Body, fontWeight = FontWeight.SemiBold)
                                Text(stringResource(R.string.common_1_s_points, card.points), color = TextSecondary, fontSize = TextSize.Caption)
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
