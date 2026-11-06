package com.glowup.app.ui.screens.styling

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Checkroom
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
import com.glowup.app.ui.components.GlowUpBottomNav
import com.glowup.app.ui.components.NavTab
import com.glowup.app.ui.theme.*

private data class StyleGuide(val title: String, val blurb: String, val tint: Color, val accent: Color)

private val styleGuides = listOf(
    StyleGuide("Old Money", "Quiet, tailored, no logos", CoralTint, Coral),
    StyleGuide("Streetwear", "Bold layers and sneakers", GreenTint, Green),
    StyleGuide("Minimal", "Clean lines, neutral tones", PurpleTint, Purple),
    StyleGuide("Athleisure", "Sporty but put together", GreenTint, Green),
    StyleGuide("Formal", "Sharp for big occasions", NavyTint, Navy),
    StyleGuide("Jewellery", "Small details, big impact", MaroonTint, Maroon),
)

private val outfitIdeasByOccasion = mapOf(
    "Office" to listOf(
        "Light linen shirt + chinos" to "Breathable and sharp for a warm day at work.",
        "Navy polo + tailored shorts" to "Smart-casual and cool for a hot afternoon.",
        "Black tee + light trousers" to "Simple, clean, and easy to layer with your watch.",
    ),
    "College" to listOf(
        "Graphic tee + relaxed denim" to "Easy and comfortable between classes.",
        "Oversized hoodie + joggers" to "Low effort, still put together.",
        "Plain tee + cargo pants" to "Casual with a bit of edge.",
    ),
    "Date" to listOf(
        "Navy shirt + beige chinos" to "Smart but relaxed — works for most first dates.",
        "Black button-up + dark denim" to "A little sharper without overdressing.",
        "Knit polo + tailored trousers" to "Understated and confident.",
    ),
    "Wedding" to listOf(
        "Pastel linen shirt + beige trousers" to "Festive but comfortable for a day event.",
        "Bandhgala with light embroidery" to "Traditional and sharp for the main ceremony.",
        "Kurta + churidar in muted tones" to "Classic, comfortable, and easy to move in.",
    ),
    "Interview" to listOf(
        "Light blue shirt + navy trousers" to "Professional without being a full suit.",
        "Half-sleeve sweater over a collared shirt" to "Polished and approachable.",
        "Charcoal blazer + plain tee" to "Sharp for a startup or creative interview.",
    ),
    "Gym" to listOf(
        "Moisture-wicking tee + shorts" to "Breathable for a heavy session.",
        "Compression tee + joggers" to "Good for lifting days.",
        "Tank top + track pants" to "Keeps you cool for cardio.",
    ),
)

@Composable
fun StylingScreen(navController: NavHostController) {
    var selectedOccasion by remember { mutableStateOf("Office") }
    var ideaIndex by remember { mutableStateOf(0) }
    val outfitIdeas = outfitIdeasByOccasion.getValue(selectedOccasion)

    Box(modifier = Modifier.fillMaxSize().background(Surface).systemBarsPadding()) {
        Column(modifier = Modifier.fillMaxSize()) {

            Text(
                "Styling",
                style = MaterialTheme.typography.headlineMedium,
                modifier = Modifier.padding(24.dp, 32.dp, 24.dp, 0.dp),
            )

            LazyColumn(
                modifier = Modifier.weight(1f),
                contentPadding = PaddingValues(horizontal = 24.dp, vertical = 18.dp),
            ) {
                item {
                    Text("What are you dressing for?", color = TextSecondary, fontSize = 13.sp, fontWeight = FontWeight.Medium)
                    Spacer(Modifier.height(14.dp))
                }

                item {
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        items(outfitIdeasByOccasion.keys.toList()) { chip ->
                            val selected = chip == selectedOccasion
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(999.dp))
                                    .background(if (selected) Ink else ChipNeutral)
                                    .clickable { selectedOccasion = chip; ideaIndex = 0 }
                                    .padding(18.dp, 10.dp),
                            ) {
                                Text(chip, color = if (selected) Color.White else Ink, fontSize = 13.5.sp, fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Medium)
                            }
                        }
                    }
                    Spacer(Modifier.height(14.dp))
                }

                item {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(28.dp))
                            .background(BlueTint)
                            .padding(20.dp, 18.dp, 14.dp, 18.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text("TODAY'S PICK FOR ${selectedOccasion.uppercase()}", color = Navy, fontSize = 10.5.sp, fontWeight = FontWeight.SemiBold)
                            Spacer(Modifier.height(4.dp))
                            Text(outfitIdeas[ideaIndex].first, fontSize = 17.sp, fontWeight = FontWeight.Bold)
                            Spacer(Modifier.height(4.dp))
                            Text(outfitIdeas[ideaIndex].second, color = Color(0xFF5A6B8C), fontSize = 12.5.sp)
                        }
                        Image(
                            painterResource(R.drawable.ic_outfit),
                            contentDescription = null,
                            modifier = Modifier.size(76.dp),
                        )
                    }
                    Spacer(Modifier.height(12.dp))
                }

                item {
                    Row(horizontalArrangement = Arrangement.spacedBy(10.dp), verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .height(48.dp)
                                .clip(RoundedCornerShape(999.dp))
                                .background(Ink)
                                .clickable { ideaIndex = (ideaIndex + 1) % outfitIdeas.size },
                            contentAlignment = Alignment.Center,
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                Icon(Icons.Filled.Checkroom, contentDescription = null, tint = Color.White, modifier = Modifier.size(18.dp))
                                Text("Show another fit", color = Color.White, fontSize = 13.5.sp, fontWeight = FontWeight.SemiBold)
                            }
                        }
                        Box(
                            modifier = Modifier
                                .size(48.dp)
                                .clip(CircleShape)
                                .background(ChipNeutral)
                                .clickable { navController.navigate("wardrobe") },
                            contentAlignment = Alignment.Center,
                        ) {
                            Image(painterResource(R.drawable.ic_closet), contentDescription = null, modifier = Modifier.size(20.dp))
                        }
                    }
                    Spacer(Modifier.height(10.dp))
                    Text(
                        "Idea ${ideaIndex + 1} of ${outfitIdeas.size} for ${selectedOccasion.lowercase()}",
                        color = TextMuted,
                        fontSize = 11.sp,
                        modifier = Modifier.fillMaxWidth(),
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                    )
                    Spacer(Modifier.height(14.dp))
                }

                item {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(22.dp))
                            .background(Ink)
                            .clickable { navController.navigate("ai_stylist") }
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                    ) {
                        Image(painterResource(R.drawable.ic_style), contentDescription = null, modifier = Modifier.size(36.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text("Ask your AI stylist", color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
                            Text("Tell it what you own — get real answers", color = Color.White.copy(alpha = 0.55f), fontSize = 11.5.sp)
                        }
                        Box(
                            modifier = Modifier.clip(RoundedCornerShape(999.dp)).background(Yellow).padding(8.dp, 4.dp),
                        ) {
                            Text("PRO", color = Ink, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                    Spacer(Modifier.height(22.dp))
                }

                item {
                    Text("Style guides", fontSize = 16.sp, fontWeight = FontWeight.SemiBold)
                    Spacer(Modifier.height(12.dp))
                }

                items(styleGuides.chunked(2)) { pair ->
                    Row(horizontalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.fillMaxWidth()) {
                        pair.forEach { guide ->
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(20.dp))
                                    .background(guide.tint)
                                    .clickable { navController.navigate("style_guide_detail/${guide.title}") }
                                    .padding(14.dp),
                            ) {
                                Column {
                                    Text(guide.title, fontSize = 15.sp, fontWeight = FontWeight.Bold, color = Ink)
                                    Spacer(Modifier.height(4.dp))
                                    Text(guide.blurb, fontSize = 12.sp, color = guide.accent, fontWeight = FontWeight.Medium, minLines = 2, lineHeight = 16.sp)
                                }
                            }
                        }
                    }
                    Spacer(Modifier.height(10.dp))
                }

                item { Spacer(Modifier.height(90.dp)) }
            }
        }

        Box(modifier = Modifier.align(Alignment.BottomCenter)) {
            GlowUpBottomNav(navController, NavTab.STYLING)
        }
    }
}
