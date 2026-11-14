package com.glowup.app.ui.screens.styleguide

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
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
import com.glowup.app.ui.components.OutfitCard
import com.glowup.app.ui.components.SwatchDot
import com.glowup.app.ui.components.goBack
import com.glowup.app.ui.data.style.StyleGuide
import com.glowup.app.ui.data.style.StyleLibrary
import com.glowup.app.ui.theme.*
import com.glowup.app.ui.design.LineHeight
import com.glowup.app.ui.design.Shapes
import com.glowup.app.ui.design.TextSize
import com.glowup.app.ui.design.SectionHeader
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import com.glowup.app.ui.components.ProLockedScreen
import com.glowup.app.ui.components.artworkOrNull
import com.glowup.app.ui.components.guideArtworkName
import com.glowup.app.ui.data.ProGate

@Composable
fun StyleGuideDetailScreen(navController: NavHostController, key: String) {
    val guide = StyleLibrary.guide(key) ?: StyleLibrary.guides.first()
    if (ProGate.guideLocked(guide.key)) {
        ProLockedScreen(navController, guide.title, "The ${guide.title} guide and its ${guide.outfits.size} looks are part of Pro")
        return
    }

    Column(modifier = Modifier.fillMaxSize().background(Surface).systemBarsPadding()) {
        DetailHeader("Style guide") { navController.goBack() }

        LazyColumn(
            modifier = Modifier.weight(1f),
            contentPadding = PaddingValues(top = 16.dp, bottom = 32.dp),
        ) {
            item { Hero(guide) }

            item {
                SectionTitle("The looks", "${guide.outfits.size} outfits · tap one for the full breakdown")
                LazyRow(contentPadding = PaddingValues(horizontal = 24.dp), horizontalArrangement = Arrangement.spacedBy(14.dp)) {
                    items(guide.outfits, key = { it.id }) { outfit ->
                        OutfitCard(outfit, Modifier.width(232.dp)) { navController.navigate("outfit/${outfit.id}") }
                    }
                }
            }

            item { SectionTitle("What goes with what", "Build outfits from what you already own") }
            items(guide.pairings) { pairing ->
                Column(
                    modifier = Modifier
                        .padding(24.dp, 0.dp, 24.dp, 10.dp)
                        .fillMaxWidth()
                        .clip(Shapes.Card)
                        .background(CardNeutral)
                        .padding(16.dp),
                ) {
                    Text(pairing.item, fontSize = TextSize.BodyLarge, fontWeight = FontWeight.Bold)
                    Spacer(Modifier.height(10.dp))
                    Row(verticalAlignment = Alignment.Top) {
                        Icon(Icons.Filled.Check, contentDescription = null, tint = Green, modifier = Modifier.size(16.dp).padding(top = 2.dp))
                        Spacer(Modifier.width(8.dp))
                        Text(pairing.goesWith.joinToString(" · "), fontSize = TextSize.Body, lineHeight = LineHeight.Body, fontWeight = FontWeight.Medium)
                    }
                    Spacer(Modifier.height(6.dp))
                    Row(verticalAlignment = Alignment.Top) {
                        Icon(Icons.Filled.Close, contentDescription = null, tint = Maroon, modifier = Modifier.size(16.dp).padding(top = 2.dp))
                        Spacer(Modifier.width(8.dp))
                        Text(pairing.avoid, color = Maroon, fontSize = TextSize.BodySmall, lineHeight = LineHeight.Compact)
                    }
                }
            }

            item { SectionTitle("The essentials", "Pieces this style is built on") }
            items(guide.keyPieces) { piece -> Bullet(piece, Icons.Filled.Check, Ink, CardNeutral, Ink) }

            item { SectionTitle("Styling tips", null) }
            items(guide.stylingTips) { tip -> Bullet(tip, null, guide.accent, guide.tint, guide.heroText) }

            item { SectionTitle("Keep in mind", null) }
            items(guide.keepInMind) { note -> Bullet(note, Icons.Filled.Close, Maroon, MaroonTint, Color(0xFF7A2E3D)) }

            item {
                Spacer(Modifier.height(14.dp))
                Row(
                    modifier = Modifier
                        .padding(horizontal = 24.dp)
                        .fillMaxWidth()
                        .clip(Shapes.Card)
                        .background(Ink)
                        .clickable { navController.navigate("wardrobe") }
                        .padding(18.dp, 16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    Image(painterResource(R.drawable.ic_closet), contentDescription = null, modifier = Modifier.size(30.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text("Check your wardrobe", color = Color.White, fontSize = TextSize.Body, fontWeight = FontWeight.SemiBold)
                        Text("See which of these essentials you already own", color = Color.White.copy(alpha = 0.6f), fontSize = TextSize.Caption)
                    }
                }
            }
        }
    }
}

@Composable
private fun Hero(guide: StyleGuide) {
    Column(
        modifier = Modifier
            .padding(horizontal = 24.dp)
            .fillMaxWidth()
            .clip(Shapes.Hero)
            .background(guide.tint)
            .padding(22.dp),
    ) {
        artworkOrNull(guideArtworkName(guide.key))?.let { cover ->
            Image(
                painterResource(cover),
                contentDescription = guide.title,
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxWidth().height(200.dp).clip(Shapes.Card),
            )
            Spacer(Modifier.height(18.dp))
        }
        Text("STYLE GUIDE · ${guide.outfits.size} LOOKS", color = guide.accent, fontSize = TextSize.Micro, fontWeight = FontWeight.SemiBold, letterSpacing = 0.5.sp)
        Spacer(Modifier.height(6.dp))
        Text(guide.title, fontSize = TextSize.DisplayLarge, fontWeight = FontWeight.Bold, lineHeight = LineHeight.Display)
        Spacer(Modifier.height(8.dp))
        Text(guide.tagline, color = guide.heroText, fontSize = TextSize.Body, lineHeight = LineHeight.Body)
        Spacer(Modifier.height(18.dp))
        Text("THE PALETTE", color = guide.accent, fontSize = TextSize.Micro, fontWeight = FontWeight.SemiBold, letterSpacing = 0.5.sp)
        Spacer(Modifier.height(10.dp))
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            guide.palette.forEach { swatch ->
                Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.width(56.dp)) {
                    SwatchDot(swatch.color, 34.dp)
                    Spacer(Modifier.height(4.dp))
                    Text(swatch.name, fontSize = TextSize.Micro, color = guide.heroText, textAlign = TextAlign.Center, lineHeight = LineHeight.Tight, maxLines = 2)
                }
            }
        }
        Spacer(Modifier.height(16.dp))
        Text("WEAR IT FOR", color = guide.accent, fontSize = TextSize.Micro, fontWeight = FontWeight.SemiBold, letterSpacing = 0.5.sp)
        Spacer(Modifier.height(8.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            guide.goodFor.take(3).forEach { occasion ->
                Box(modifier = Modifier.clip(Shapes.Pill).background(Color.White.copy(alpha = 0.7f)).padding(10.dp, 6.dp)) {
                    Text(occasion, color = guide.accent, fontSize = TextSize.Caption, fontWeight = FontWeight.SemiBold, maxLines = 1)
                }
            }
        }
    }
}

@Composable
private fun SectionTitle(title: String, subtitle: String?) {
    SectionHeader(title, Modifier.padding(24.dp, 26.dp, 24.dp, 12.dp), subtitle)
}

@Composable
private fun Bullet(text: String, icon: ImageVector?, iconTint: Color, background: Color, textColor: Color) {
    Row(
        modifier = Modifier
            .padding(24.dp, 0.dp, 24.dp, 8.dp)
            .fillMaxWidth()
            .clip(Shapes.Medium)
            .background(background)
            .padding(16.dp, 13.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        if (icon != null) {
            Box(modifier = Modifier.size(22.dp).clip(CircleShape).background(iconTint), contentAlignment = Alignment.Center) {
                Icon(icon, contentDescription = null, tint = Color.White, modifier = Modifier.size(12.dp))
            }
        } else {
            Text("✦", color = iconTint, fontSize = TextSize.Body, fontWeight = FontWeight.Bold)
        }
        Text(text, color = textColor, fontSize = TextSize.Body, fontWeight = FontWeight.Medium, lineHeight = LineHeight.Body)
    }
}
