package com.glowup.app.ui.screens.styleguide

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
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

@Composable
fun StyleGuideDetailScreen(navController: NavHostController, key: String) {
    val guide = StyleLibrary.guide(key) ?: StyleLibrary.guides.first()
    if (com.glowup.app.ui.data.ProGate.guideLocked(guide.key)) {
        com.glowup.app.ui.components.ProLockedScreen(navController, guide.title, "The ${guide.title} guide and its ${guide.outfits.size} looks are part of Pro")
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
                        .clip(RoundedCornerShape(20.dp))
                        .background(CardNeutral)
                        .padding(16.dp),
                ) {
                    Text(pairing.item, fontSize = 15.sp, fontWeight = FontWeight.Bold)
                    Spacer(Modifier.height(10.dp))
                    Row(verticalAlignment = Alignment.Top) {
                        Icon(Icons.Filled.Check, contentDescription = null, tint = Green, modifier = Modifier.size(16.dp).padding(top = 2.dp))
                        Spacer(Modifier.width(8.dp))
                        Text(pairing.goesWith.joinToString(" · "), fontSize = 13.5.sp, lineHeight = 19.sp, fontWeight = FontWeight.Medium)
                    }
                    Spacer(Modifier.height(6.dp))
                    Row(verticalAlignment = Alignment.Top) {
                        Icon(Icons.Filled.Close, contentDescription = null, tint = Maroon, modifier = Modifier.size(16.dp).padding(top = 2.dp))
                        Spacer(Modifier.width(8.dp))
                        Text(pairing.avoid, color = Maroon, fontSize = 13.sp, lineHeight = 18.sp)
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
                        .clip(RoundedCornerShape(20.dp))
                        .background(Ink)
                        .clickable { navController.navigate("wardrobe") }
                        .padding(18.dp, 16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    Image(painterResource(R.drawable.ic_closet), contentDescription = null, modifier = Modifier.size(30.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text("Check your wardrobe", color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
                        Text("See which of these essentials you already own", color = Color.White.copy(alpha = 0.6f), fontSize = 12.sp)
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
            .clip(RoundedCornerShape(28.dp))
            .background(guide.tint)
            .padding(22.dp),
    ) {
        com.glowup.app.ui.components.artworkOrNull(com.glowup.app.ui.components.guideArtworkName(guide.key))?.let { cover ->
            Image(
                painterResource(cover),
                contentDescription = guide.title,
                contentScale = androidx.compose.ui.layout.ContentScale.Crop,
                modifier = Modifier.fillMaxWidth().height(200.dp).clip(RoundedCornerShape(20.dp)),
            )
            Spacer(Modifier.height(18.dp))
        }
        Text("STYLE GUIDE · ${guide.outfits.size} LOOKS", color = guide.accent, fontSize = 11.sp, fontWeight = FontWeight.SemiBold, letterSpacing = 0.5.sp)
        Spacer(Modifier.height(6.dp))
        Text(guide.title, fontSize = 30.sp, fontWeight = FontWeight.Bold, lineHeight = 34.sp)
        Spacer(Modifier.height(8.dp))
        Text(guide.tagline, color = guide.heroText, fontSize = 14.sp, lineHeight = 20.sp)
        Spacer(Modifier.height(18.dp))
        Text("THE PALETTE", color = guide.accent, fontSize = 10.5.sp, fontWeight = FontWeight.SemiBold, letterSpacing = 0.5.sp)
        Spacer(Modifier.height(10.dp))
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            guide.palette.forEach { swatch ->
                Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.width(56.dp)) {
                    SwatchDot(swatch.color, 34.dp)
                    Spacer(Modifier.height(4.dp))
                    Text(swatch.name, fontSize = 10.5.sp, color = guide.heroText, textAlign = TextAlign.Center, lineHeight = 13.sp, maxLines = 2)
                }
            }
        }
        Spacer(Modifier.height(16.dp))
        Text("WEAR IT FOR", color = guide.accent, fontSize = 10.5.sp, fontWeight = FontWeight.SemiBold, letterSpacing = 0.5.sp)
        Spacer(Modifier.height(8.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            guide.goodFor.take(3).forEach { occasion ->
                Box(modifier = Modifier.clip(RoundedCornerShape(999.dp)).background(Color.White.copy(alpha = 0.7f)).padding(10.dp, 6.dp)) {
                    Text(occasion, color = guide.accent, fontSize = 11.5.sp, fontWeight = FontWeight.SemiBold, maxLines = 1)
                }
            }
        }
    }
}

@Composable
private fun SectionTitle(title: String, subtitle: String?) {
    Column(modifier = Modifier.padding(24.dp, 26.dp, 24.dp, 12.dp)) {
        Text(title, fontSize = 18.sp, fontWeight = FontWeight.Bold)
        subtitle?.let { Text(it, color = TextSecondary, fontSize = 12.5.sp) }
    }
}

@Composable
private fun Bullet(text: String, icon: androidx.compose.ui.graphics.vector.ImageVector?, iconTint: Color, background: Color, textColor: Color) {
    Row(
        modifier = Modifier
            .padding(24.dp, 0.dp, 24.dp, 8.dp)
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
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
            Text("✦", color = iconTint, fontSize = 14.sp, fontWeight = FontWeight.Bold)
        }
        Text(text, color = textColor, fontSize = 14.sp, fontWeight = FontWeight.Medium, lineHeight = 20.sp)
    }
}
