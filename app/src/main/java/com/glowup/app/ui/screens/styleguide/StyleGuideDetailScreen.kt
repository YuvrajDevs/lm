package com.glowup.app.ui.screens.styleguide

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
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
import com.glowup.app.ui.theme.*

private data class StyleGuideContent(
    val title: String,
    val tagline: String,
    val heroTint: Color,
    val heroTextTint: Color,
    val accent: Color,
    val keyPieces: List<String>,
    val keepInMind: List<String>,
)

private val styleGuideContents = mapOf(
    "Old Money" to StyleGuideContent(
        "Old Money",
        "Quiet, tailored, no logos. Looks expensive without trying to.",
        CoralTint, Color(0xFF8A6A58), Coral,
        listOf("Crisp white or light-blue shirt", "Tailored chinos or wool trousers", "Leather loafers, well kept", "A simple knit sweater or blazer"),
        listOf("Skip visible logos and branding", "Avoid anything too shiny or synthetic"),
    ),
    "Streetwear" to StyleGuideContent(
        "Streetwear",
        "Bold silhouettes, graphic layers. Confidence through contrast.",
        GreenTint, Color(0xFF2B6B4C), Green,
        listOf("Oversized graphic tee or hoodie", "Relaxed cargo pants or denim", "Statement sneakers", "A cap or beanie to finish"),
        listOf("Don't over-layer in hot weather", "Keep one statement piece, not five"),
    ),
    "Minimal" to StyleGuideContent(
        "Minimal",
        "Clean lines, neutral tones. Less, but better.",
        PurpleTint, Color(0xFF5A4A8C), Purple,
        listOf("Solid-colour tee or shirt", "Straight-leg trousers", "Clean white or black sneakers", "One quiet accessory, max"),
        listOf("Avoid busy prints and logos", "Don't mix more than two neutral tones"),
    ),
    "Athleisure" to StyleGuideContent(
        "Athleisure",
        "Sport-inspired comfort that still looks put together.",
        GreenTint, Color(0xFF2B6B4C), Green,
        listOf("Fitted joggers or track pants", "A clean crewneck or zip hoodie", "Quality trainers, kept clean", "A cap or a minimal crossbody bag"),
        listOf("Don't mix more than two bold logos", "Keep the fit tailored — baggy everywhere reads sloppy, not sporty"),
    ),
    "Formal" to StyleGuideContent(
        "Formal",
        "Sharp, deliberate, built for the room that matters.",
        NavyTint, Color(0xFF2C4E8E), Navy,
        listOf("A well-fitted suit in navy or charcoal", "Crisp dress shirt, pressed", "Leather Oxfords or derbies", "A tie or pocket square that matches, not clashes"),
        listOf("Get the fit tailored — off-the-rack rarely sits right", "Don't mix more than two patterns at once"),
    ),
    "Jewellery" to StyleGuideContent(
        "Jewellery",
        "Small details, big difference. Wear metal with intention.",
        MaroonTint, Color(0xFF7A2E3D), Maroon,
        listOf("A simple chain or pendant", "One ring, not a full set", "Matched metal tones", "A watch that fits the occasion"),
        listOf("Don't mix gold and silver at once", "Skip anything that jingles when you walk"),
    ),
)

@Composable
fun StyleGuideDetailScreen(navController: NavHostController, title: String) {
    val content = styleGuideContents[title] ?: styleGuideContents.getValue("Old Money")

    Box(modifier = Modifier.fillMaxSize().background(Surface).systemBarsPadding()) {
        Column(modifier = Modifier.fillMaxSize()) {
            Row(
                modifier = Modifier.fillMaxWidth().padding(24.dp, 24.dp, 24.dp, 0.dp),
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
            }

            LazyColumn(
                modifier = Modifier.weight(1f),
                contentPadding = PaddingValues(24.dp, 8.dp, 24.dp, 24.dp),
            ) {
                item {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(28.dp))
                            .background(content.heroTint)
                            .padding(24.dp),
                    ) {
                        Text("STYLE GUIDE", color = content.accent, fontSize = 11.sp, fontWeight = FontWeight.SemiBold, letterSpacing = 0.4.sp)
                        Spacer(Modifier.height(8.dp))
                        Text(content.title, fontSize = 28.sp, fontWeight = FontWeight.Bold)
                        Spacer(Modifier.height(10.dp))
                        Text(content.tagline, color = content.heroTextTint, fontSize = 13.5.sp, lineHeight = 19.sp)
                    }
                    Spacer(Modifier.height(22.dp))
                }

                item {
                    Text("Key pieces", fontSize = 16.sp, fontWeight = FontWeight.SemiBold)
                    Spacer(Modifier.height(10.dp))
                }

                items(content.keyPieces) { piece ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 8.dp)
                            .clip(RoundedCornerShape(16.dp))
                            .background(CardNeutral)
                            .padding(16.dp, 13.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                    ) {
                        Box(
                            modifier = Modifier.size(22.dp).clip(CircleShape).background(Ink),
                            contentAlignment = Alignment.Center,
                        ) {
                            Icon(Icons.Filled.Check, contentDescription = null, tint = Color.White, modifier = Modifier.size(12.dp))
                        }
                        Text(piece, fontSize = 14.sp, fontWeight = FontWeight.Medium)
                    }
                }

                item {
                    Spacer(Modifier.height(12.dp))
                    Text("Keep in mind", fontSize = 16.sp, fontWeight = FontWeight.SemiBold)
                    Spacer(Modifier.height(10.dp))
                }

                items(content.keepInMind) { note ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 8.dp)
                            .clip(RoundedCornerShape(16.dp))
                            .background(MaroonTint)
                            .padding(16.dp, 13.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                    ) {
                        Text("—", color = Maroon, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                        Text(note, color = Color(0xFF7A2E3D), fontSize = 14.sp, fontWeight = FontWeight.Medium)
                    }
                }

                item {
                    Spacer(Modifier.height(10.dp))
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(20.dp))
                            .background(PurpleTint)
                            .clickable { navController.navigate("ai_stylist") }
                            .padding(18.dp, 16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                    ) {
                        Image(painterResource(R.drawable.ic_area_style), contentDescription = null, modifier = Modifier.size(32.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text("Not sure where to start?", fontSize = 13.5.sp, fontWeight = FontWeight.SemiBold)
                            Text("Ask your AI stylist to build an outfit", color = Purple, fontSize = 11.5.sp)
                        }
                    }
                }
            }
        }
    }
}
