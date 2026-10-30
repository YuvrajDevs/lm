package com.glowup.app.ui.screens.styleguide

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.foundation.rememberScrollState
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
    val goodFor: List<String>,
    val keyPieces: List<String>,
    val stylingTips: List<String>,
    val keepInMind: List<String>,
)

private val styleGuideContents = mapOf(
    "Old Money" to StyleGuideContent(
        "Old Money",
        "Quiet, tailored, no logos. Looks expensive without trying to.",
        CoralTint, Color(0xFF8A6A58), Coral,
        listOf("Office", "Smart dinners", "Family events", "Golf or club days"),
        listOf(
            "Crisp white or light-blue shirt",
            "Tailored chinos or wool trousers",
            "Leather loafers, well kept",
            "A simple knit sweater or blazer",
            "A leather belt in brown or black",
            "A structured overcoat for layering",
        ),
        listOf(
            "Invest in fabric quality over quantity",
            "Tuck your shirt in for a cleaner silhouette",
            "Roll sleeves neatly rather than pushing them up",
            "Pair textures like knit and wool for depth",
        ),
        listOf(
            "Skip visible logos and branding",
            "Avoid anything too shiny or synthetic",
            "Stick to muted, earthy colours",
            "Iron everything — crisp always beats trendy",
        ),
    ),
    "Streetwear" to StyleGuideContent(
        "Streetwear",
        "Bold silhouettes, graphic layers. Confidence through contrast.",
        GreenTint, Color(0xFF2B6B4C), Green,
        listOf("College", "Hanging out", "Concerts", "Casual weekends"),
        listOf(
            "Oversized graphic tee or hoodie",
            "Relaxed cargo pants or denim",
            "Statement sneakers",
            "A cap or beanie to finish",
            "A bomber or varsity jacket",
            "Crossbody bag or fanny pack",
        ),
        listOf(
            "Let one bold sneaker colour anchor the fit",
            "Cuff your jeans to show off your shoes",
            "Layer a plain tee under an open shirt",
            "Mix high and low — one hype piece, rest basics",
        ),
        listOf(
            "Don't over-layer in hot weather",
            "Keep one statement piece, not five",
            "Avoid clashing too many graphics at once",
            "Fit still matters — oversized isn't the same as shapeless",
        ),
    ),
    "Minimal" to StyleGuideContent(
        "Minimal",
        "Clean lines, neutral tones. Less, but better.",
        PurpleTint, Color(0xFF5A4A8C), Purple,
        listOf("Work", "Everyday", "Travel", "Dates"),
        listOf(
            "Solid-colour tee or shirt",
            "Straight-leg trousers",
            "Clean white or black sneakers",
            "One quiet accessory, max",
            "A structured crewneck sweater",
            "A single-colour overshirt",
        ),
        listOf(
            "Stick to a tonal palette — black, white, grey, beige",
            "Let silhouette do the talking, not colour",
            "Invest in one genuinely great pair of trousers",
            "Steam or iron — wrinkles show more on plain fabric",
        ),
        listOf(
            "Avoid busy prints and logos",
            "Don't mix more than two neutral tones",
            "Fit is everything when there's no pattern to distract",
            "Keep shoes clean — scuffs stand out more here",
        ),
    ),
    "Athleisure" to StyleGuideContent(
        "Athleisure",
        "Sport-inspired comfort that still looks put together.",
        GreenTint, Color(0xFF2B6B4C), Green,
        listOf("Gym", "Errands", "Travel days", "Casual Fridays"),
        listOf(
            "Fitted joggers or track pants",
            "A clean crewneck or zip hoodie",
            "Quality trainers, kept clean",
            "A cap or a minimal crossbody bag",
            "A technical zip jacket",
            "A moisture-wicking base layer",
        ),
        listOf(
            "Pick one accent colour and repeat it through the fit",
            "Swap baggy sweats for tapered joggers",
            "A clean white sneaker goes with everything",
            "Layer a bomber over a hoodie for texture",
        ),
        listOf(
            "Don't mix more than two bold logos",
            "Keep the fit tailored — baggy everywhere reads sloppy, not sporty",
            "Match your sneakers to your jacket, not just your pants",
            "Save true gym wear for the gym",
        ),
    ),
    "Formal" to StyleGuideContent(
        "Formal",
        "Sharp, deliberate, built for the room that matters.",
        NavyTint, Color(0xFF2C4E8E), Navy,
        listOf("Weddings", "Interviews", "Business meetings", "Formal dinners"),
        listOf(
            "A well-fitted suit in navy or charcoal",
            "Crisp dress shirt, pressed",
            "Leather Oxfords or derbies",
            "A tie or pocket square that matches, not clashes",
            "A quality leather belt matching your shoes",
            "Cufflinks for a sharper finish",
        ),
        listOf(
            "A slightly shorter trouser break looks sharper",
            "Keep your shirt collar one size snug, not loose",
            "Match sock colour to your trousers, not your shoes",
            "Steam your suit before every wear",
        ),
        listOf(
            "Get the fit tailored — off-the-rack rarely sits right",
            "Don't mix more than two patterns at once",
            "Match your belt and shoe colour",
            "Keep accessories minimal — one statement piece max",
        ),
    ),
    "Jewellery" to StyleGuideContent(
        "Jewellery",
        "Small details, big difference. Wear metal with intention.",
        MaroonTint, Color(0xFF7A2E3D), Maroon,
        listOf("Date nights", "Nights out", "Casual days", "Smart-casual events"),
        listOf(
            "A simple chain or pendant",
            "One ring, not a full set",
            "Matched metal tones",
            "A watch that fits the occasion",
            "A subtle bracelet for balance",
            "Stud earrings, if you wear them",
        ),
        listOf(
            "Let one piece be the hero, keep the rest simple",
            "A chain works under or over a collar depending on formality",
            "Clean your jewellery regularly — tarnish kills the look",
            "Size matters — rings and chains should fit, not dangle",
        ),
        listOf(
            "Don't mix gold and silver at once",
            "Skip anything that jingles when you walk",
            "Less is more — one standout piece beats five small ones",
            "Match metal tone to your watch and belt buckle",
        ),
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
                        Spacer(Modifier.height(14.dp))
                        Row(
                            modifier = Modifier.horizontalScroll(rememberScrollState()),
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                        ) {
                            content.goodFor.forEach { occasion ->
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(999.dp))
                                        .background(Color.White.copy(alpha = 0.55f))
                                        .padding(12.dp, 7.dp),
                                ) {
                                    Text(occasion, color = content.accent, fontSize = 11.5.sp, fontWeight = FontWeight.SemiBold)
                                }
                            }
                        }
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
                    Text("Styling tips", fontSize = 16.sp, fontWeight = FontWeight.SemiBold)
                    Spacer(Modifier.height(10.dp))
                }

                items(content.stylingTips) { tip ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 8.dp)
                            .clip(RoundedCornerShape(16.dp))
                            .background(content.heroTint)
                            .padding(16.dp, 13.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                    ) {
                        Text("✦", color = content.accent, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                        Text(tip, color = content.heroTextTint, fontSize = 14.sp, fontWeight = FontWeight.Medium)
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
