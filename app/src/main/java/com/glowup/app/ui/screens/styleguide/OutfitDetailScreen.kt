package com.glowup.app.ui.screens.styleguide

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import com.glowup.app.ui.components.OutfitBoard
import com.glowup.app.ui.components.OutfitCard
import com.glowup.app.ui.components.SwatchDot
import com.glowup.app.ui.components.goBack
import com.glowup.app.ui.data.TodoBucket
import com.glowup.app.ui.data.TodoStore
import com.glowup.app.ui.data.style.SavedLooksStore
import com.glowup.app.ui.data.style.StyleLibrary
import com.glowup.app.ui.theme.*

@Composable
fun OutfitDetailScreen(navController: NavHostController, id: String) {
    val outfit = StyleLibrary.outfit(id) ?: return
    val guide = StyleLibrary.guide(outfit.styleKey) ?: return
    if (com.glowup.app.ui.data.ProGate.outfitLocked(outfit)) {
        com.glowup.app.ui.components.ProLockedScreen(navController, outfit.name, "This look is part of the ${guide.title} Pro guide")
        return
    }
    val saved = SavedLooksStore.isSaved(outfit.id)
    val todoTitle = "Wear: ${outfit.name}"
    val planned = TodoStore.items.any { it.title == todoTitle && !it.done }

    Column(modifier = Modifier.fillMaxSize().background(Surface).systemBarsPadding()) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(24.dp, 24.dp, 24.dp, 0.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            CircleButton(Icons.AutoMirrored.Filled.ArrowBack, "Back", Ink, Color.White) { navController.goBack() }
            CircleButton(
                if (saved) Icons.Filled.Favorite else Icons.Filled.FavoriteBorder,
                if (saved) "Remove from saved looks" else "Save look",
                ChipNeutral,
                if (saved) Coral else Ink,
            ) { SavedLooksStore.toggle(outfit.id) }
        }

        Column(modifier = Modifier.weight(1f).verticalScroll(rememberScrollState()).padding(top = 16.dp)) {
            OutfitBoard(outfit, Modifier.padding(horizontal = 24.dp), height = 330.dp)

            Column(modifier = Modifier.padding(24.dp, 18.dp, 24.dp, 0.dp)) {
                Text(
                    guide.title.uppercase(),
                    color = guide.accent,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.SemiBold,
                    letterSpacing = 0.5.sp,
                    modifier = Modifier.clickable { navController.navigate("style_guide_detail/${guide.key}") },
                )
                Text(outfit.name, fontSize = 28.sp, fontWeight = FontWeight.Bold, lineHeight = 33.sp)
                Spacer(Modifier.height(10.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    outfit.occasions.forEach { occasion ->
                        Box(modifier = Modifier.clip(RoundedCornerShape(999.dp)).background(ChipNeutral).padding(12.dp, 6.dp)) {
                            Text(occasion, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                        }
                    }
                }

                Spacer(Modifier.height(24.dp))
                Text("The pieces", fontSize = 18.sp, fontWeight = FontWeight.Bold)
                Spacer(Modifier.height(10.dp))
                outfit.pieces.forEach { piece ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 8.dp)
                            .clip(RoundedCornerShape(18.dp))
                            .background(CardNeutral)
                            .padding(14.dp, 12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(14.dp),
                    ) {
                        SwatchDot(piece.swatch.color, 34.dp)
                        Column(modifier = Modifier.weight(1f)) {
                            Text(piece.garment.slot.label.uppercase(), color = TextSecondary, fontSize = 10.5.sp, fontWeight = FontWeight.SemiBold, letterSpacing = 0.4.sp)
                            Text(piece.name, fontSize = 15.sp, fontWeight = FontWeight.SemiBold)
                        }
                        Text(piece.swatch.name, color = TextSecondary, fontSize = 12.sp)
                    }
                }

                Spacer(Modifier.height(16.dp))
                Column(
                    modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(22.dp)).background(guide.tint).padding(18.dp),
                ) {
                    Text("WHY IT WORKS", color = guide.accent, fontSize = 11.sp, fontWeight = FontWeight.SemiBold, letterSpacing = 0.5.sp)
                    Spacer(Modifier.height(6.dp))
                    Text(outfit.why, color = guide.heroText, fontSize = 15.sp, lineHeight = 22.sp, fontWeight = FontWeight.Medium)
                }

                Spacer(Modifier.height(10.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.height(IntrinsicSize.Min)) {
                    AdjustCard("Dress it up", outfit.dressUp, Icons.Filled.ArrowUpward, Modifier.weight(1f).fillMaxHeight())
                    AdjustCard("Dress it down", outfit.dressDown, Icons.Filled.ArrowDownward, Modifier.weight(1f).fillMaxHeight())
                }
            }

            val more = guide.outfits.filter { it.id != outfit.id }
            Text(
                "More ${guide.title} looks",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(24.dp, 26.dp, 24.dp, 12.dp),
            )
            LazyRow(contentPadding = PaddingValues(horizontal = 24.dp), horizontalArrangement = Arrangement.spacedBy(14.dp)) {
                items(more, key = { it.id }) { other ->
                    OutfitCard(other, Modifier.width(200.dp)) {
                        navController.navigate("outfit/${other.id}") { popUpTo("outfit/{id}") { inclusive = true } }
                    }
                }
            }
            Spacer(Modifier.height(20.dp))
        }

        Box(
            modifier = Modifier
                .padding(24.dp, 8.dp, 24.dp, 24.dp)
                .fillMaxWidth()
                .height(58.dp)
                .clip(RoundedCornerShape(999.dp))
                .background(if (planned) GreenTint else Ink)
                .clickable(enabled = !planned) { TodoStore.add(todoTitle, TodoBucket.TODAY, "${guide.title} · Outfit") },
            contentAlignment = Alignment.Center,
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                if (planned) Icon(Icons.Filled.Check, contentDescription = null, tint = Green, modifier = Modifier.size(18.dp))
                if (planned) Spacer(Modifier.width(6.dp))
                Text(
                    if (planned) "In today's plan" else "Wear it today",
                    color = if (planned) Green else Color.White,
                    fontSize = 15.5.sp,
                    fontWeight = FontWeight.SemiBold,
                )
            }
        }
    }
}

@Composable
private fun AdjustCard(title: String, body: String, icon: ImageVector, modifier: Modifier) {
    Column(modifier = modifier.clip(RoundedCornerShape(20.dp)).background(CardNeutral).padding(16.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(icon, contentDescription = null, tint = Ink, modifier = Modifier.size(15.dp))
            Spacer(Modifier.width(4.dp))
            Text(title, fontSize = 13.sp, fontWeight = FontWeight.Bold)
        }
        Spacer(Modifier.height(6.dp))
        Text(body, color = TextSecondary, fontSize = 13.sp, lineHeight = 18.sp)
    }
}

@Composable
private fun CircleButton(icon: ImageVector, description: String, background: Color, tint: Color, onClick: () -> Unit) {
    Box(
        modifier = Modifier.size(48.dp).clip(CircleShape).background(background).clickable(onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        Icon(icon, contentDescription = description, tint = tint, modifier = Modifier.size(20.dp))
    }
}
