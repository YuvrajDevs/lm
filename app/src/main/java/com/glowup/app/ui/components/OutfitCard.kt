package com.glowup.app.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.glowup.app.ui.data.style.Outfit
import com.glowup.app.ui.data.style.SavedLooksStore
import com.glowup.app.ui.data.style.StyleLibrary
import com.glowup.app.ui.theme.*
import com.glowup.app.ui.design.Shapes
import com.glowup.app.ui.design.TextSize
import androidx.compose.foundation.Image
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource

/** The board an outfit is laid out on: a soft tint of its style's colour. */
@Composable
fun OutfitBoard(outfit: Outfit, modifier: Modifier = Modifier, height: Dp = 210.dp) {
    val guide = StyleLibrary.guide(outfit.styleKey)
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(height)
            .clip(Shapes.Card)
            .background(guide?.tint ?: CardNeutral),
    ) {
        val artwork = artworkOrNull(outfitArtworkName(outfit.id))
        if (artwork != null) {
            Image(
                painterResource(artwork),
                contentDescription = outfit.name,
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize(),
            )
        } else {
            OutfitArt(outfit, Modifier.fillMaxSize().padding(14.dp))
        }
        if (SavedLooksStore.isSaved(outfit.id)) {
            Box(
                modifier = Modifier.align(Alignment.TopEnd).padding(10.dp).size(28.dp).clip(CircleShape).background(Color.White),
                contentAlignment = Alignment.Center,
            ) {
                Icon(Icons.Filled.Favorite, contentDescription = "Saved", tint = Coral, modifier = Modifier.size(15.dp))
            }
        }
    }
}

/** A compact card for carousels: the flat lay, the look's name and its colours. */
@Composable
fun OutfitCard(outfit: Outfit, modifier: Modifier = Modifier, showStyle: Boolean = false, onClick: () -> Unit) {
    val guide = StyleLibrary.guide(outfit.styleKey)
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(22.dp, 22.dp, 10.dp, 10.dp))
            .background(Surface)
            .clickable(onClick = onClick)
            .padding(bottom = 6.dp),
    ) {
        OutfitBoard(outfit, height = 200.dp)
        Spacer(Modifier.height(10.dp))
        if (showStyle && guide != null) {
            Text(guide.title.uppercase(), color = guide.accent, fontSize = TextSize.Micro, fontWeight = FontWeight.SemiBold, letterSpacing = 0.4.sp)
        }
        Text(outfit.name, fontSize = TextSize.BodyLarge, fontWeight = FontWeight.Bold, maxLines = 1, overflow = TextOverflow.Ellipsis)
        Spacer(Modifier.height(6.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(4.dp), verticalAlignment = Alignment.CenterVertically) {
            outfit.pieces.take(5).forEach { piece -> SwatchDot(piece.swatch.color, 14.dp) }
            Spacer(Modifier.width(4.dp))
            Text("${outfit.pieces.size} pieces", color = TextSecondary, fontSize = TextSize.Caption)
        }
    }
}

@Composable
fun SwatchDot(color: Color, size: Dp) {
    Box(
        modifier = Modifier
            .size(size)
            .clip(CircleShape)
            .background(color)
            .border(1.dp, Color.Black.copy(alpha = 0.12f), CircleShape),
    )
}
