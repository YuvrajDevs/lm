package com.glowup.app.ui.screens.styleguide

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import com.glowup.app.R
import com.glowup.app.ui.components.OutfitBoard
import com.glowup.app.ui.components.OutfitCard
import com.glowup.app.ui.components.ProLockedScreen
import com.glowup.app.ui.components.SwatchDot
import com.glowup.app.ui.components.goBack
import com.glowup.app.ui.data.ProGate
import com.glowup.app.ui.data.TodoBucket
import com.glowup.app.ui.data.TodoStore
import com.glowup.app.ui.data.style.SavedLooksStore
import com.glowup.app.ui.data.style.StyleLibrary
import com.glowup.app.ui.design.LineHeight
import com.glowup.app.ui.design.Shapes
import com.glowup.app.ui.design.Space
import com.glowup.app.ui.design.TextSize
import com.glowup.app.ui.screens.wardrobe.MakeThisLook
import com.glowup.app.ui.theme.*

@Composable
fun OutfitDetailScreen(navController: NavHostController, id: String) {
    val outfit = StyleLibrary.outfit(id) ?: return
    val guide = StyleLibrary.guide(outfit.styleKey) ?: return
    if (ProGate.outfitLocked(outfit)) {
        ProLockedScreen(navController, outfit.name, "This look is part of the ${guide.title} Pro guide")
        return
    }
    val saved = SavedLooksStore.isSaved(outfit.id)
    val todoTitle = "Wear: ${outfit.name}"
    val planned = TodoStore.items.any { it.title == todoTitle && !it.done }

    Column(modifier = Modifier.fillMaxSize().background(Surface).systemBarsPadding()) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(Space.Gutter, Space.Gutter, Space.Gutter, 0.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            CircleButton(Icons.AutoMirrored.Filled.ArrowBack, "Back", Ink, Color.White) { navController.goBack() }
            CircleButton(
                if (saved) Icons.Filled.Favorite else Icons.Filled.FavoriteBorder,
                if (saved) "Remove from saved looks" else "Save look",
                ChipNeutral,
                if (saved) Coral else Ink
            ) { SavedLooksStore.toggle(outfit.id) }
        }

        Column(modifier = Modifier.weight(1f).verticalScroll(rememberScrollState()).padding(top = Space.L)) {
            OutfitBoard(outfit, Modifier.padding(horizontal = Space.Gutter), height = 330.dp)

            Column(modifier = Modifier.padding(Space.Gutter, Space.L, Space.Gutter, 0.dp)) {
                Text(
                    guide.title.uppercase(),
                    color = guide.accent,
                    fontSize = TextSize.Micro,
                    fontWeight = FontWeight.SemiBold,
                    letterSpacing = 0.5.sp,
                    modifier = Modifier.clickable { navController.navigate("style_guide_detail/${guide.key}") }
                )
                Text(outfit.name, fontSize = TextSize.Display, fontWeight = FontWeight.Bold, lineHeight = LineHeight.Display)
                Spacer(Modifier.height(Space.S))
                Row(horizontalArrangement = Arrangement.spacedBy(Space.S)) {
                    outfit.occasions.forEach { occasion ->
                        Box(modifier = Modifier.clip(Shapes.Pill).background(ChipNeutral).padding(Space.M, Space.S)) {
                            Text(occasion, fontSize = TextSize.Caption, fontWeight = FontWeight.SemiBold)
                        }
                    }
                }

                Spacer(Modifier.height(Space.L))
                MakeThisLook(outfit, navController)

                Spacer(Modifier.height(Space.Gutter))
                Text(stringResource(R.string.outfit_detail_the_pieces), fontSize = TextSize.Title, fontWeight = FontWeight.Bold)
                Spacer(Modifier.height(Space.S))
                outfit.pieces.forEach { piece ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = Space.S)
                            .clip(Shapes.Medium)
                            .background(CardNeutral)
                            .padding(Space.M, Space.M),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(Space.M)
                    ) {
                        SwatchDot(piece.swatch.color, 34.dp)
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                piece.garment.slot.label.uppercase(),
                                color = TextSecondary,
                                fontSize = TextSize.Micro,
                                fontWeight = FontWeight.SemiBold,
                                letterSpacing = 0.4.sp
                            )
                            Text(piece.name, fontSize = TextSize.BodyLarge, fontWeight = FontWeight.SemiBold)
                        }
                        Text(piece.swatch.name, color = TextSecondary, fontSize = TextSize.Caption)
                    }
                }

                Spacer(Modifier.height(Space.L))
                Column(
                    modifier = Modifier.fillMaxWidth().clip(Shapes.Card).background(guide.tint).padding(Space.L)
                ) {
                    Text(
                        stringResource(R.string.outfit_detail_why_it_works),
                        color = guide.accent,
                        fontSize = TextSize.Micro,
                        fontWeight = FontWeight.SemiBold,
                        letterSpacing = 0.5.sp
                    )
                    Spacer(Modifier.height(Space.S))
                    Text(outfit.why, color = guide.heroText, fontSize = TextSize.BodyLarge, lineHeight = LineHeight.Relaxed, fontWeight = FontWeight.Medium)
                }

                Spacer(Modifier.height(Space.S))
                Row(horizontalArrangement = Arrangement.spacedBy(Space.S), modifier = Modifier.height(IntrinsicSize.Min)) {
                    AdjustCard("Dress it up", outfit.dressUp, Icons.Filled.ArrowUpward, Modifier.weight(1f).fillMaxHeight())
                    AdjustCard("Dress it down", outfit.dressDown, Icons.Filled.ArrowDownward, Modifier.weight(1f).fillMaxHeight())
                }
            }

            val more = guide.outfits.filter { it.id != outfit.id }
            Text(
                stringResource(R.string.outfit_detail_more_1_s_looks, guide.title),
                fontSize = TextSize.Title,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(Space.Gutter, Space.Gutter, Space.Gutter, Space.M)
            )
            LazyRow(contentPadding = PaddingValues(horizontal = Space.Gutter), horizontalArrangement = Arrangement.spacedBy(Space.M)) {
                items(more, key = { it.id }) { other ->
                    OutfitCard(other, Modifier.width(200.dp)) {
                        navController.navigate("outfit/${other.id}") { popUpTo("outfit/{id}") { inclusive = true } }
                    }
                }
            }
            Spacer(Modifier.height(Space.Xl))
        }

        Box(
            modifier = Modifier
                .padding(Space.Gutter, Space.S, Space.Gutter, Space.Gutter)
                .fillMaxWidth()
                .height(58.dp)
                .clip(Shapes.Pill)
                .background(if (planned) GreenTint else Ink)
                .clickable(enabled = !planned) { TodoStore.add(todoTitle, TodoBucket.TODAY, "${guide.title} · Outfit") },
            contentAlignment = Alignment.Center
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                if (planned) Icon(Icons.Filled.Check, contentDescription = null, tint = Green, modifier = Modifier.size(18.dp))
                if (planned) Spacer(Modifier.width(Space.S))
                Text(
                    if (planned) stringResource(R.string.outfit_detail_in_today_s_plan) else stringResource(R.string.outfit_detail_wear_it_today),
                    color = if (planned) Green else Color.White,
                    fontSize = TextSize.BodyLarge,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }
    }
}

@Composable
private fun AdjustCard(title: String, body: String, icon: ImageVector, modifier: Modifier) {
    Column(modifier = modifier.clip(Shapes.Card).background(CardNeutral).padding(Space.L)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(icon, contentDescription = null, tint = Ink, modifier = Modifier.size(15.dp))
            Spacer(Modifier.width(Space.Xs))
            Text(title, fontSize = TextSize.BodySmall, fontWeight = FontWeight.Bold)
        }
        Spacer(Modifier.height(Space.S))
        Text(body, color = TextSecondary, fontSize = TextSize.BodySmall, lineHeight = LineHeight.Compact)
    }
}

@Composable
private fun CircleButton(icon: ImageVector, description: String, background: Color, tint: Color, onClick: () -> Unit) {
    Box(
        modifier = Modifier.size(48.dp).clip(CircleShape).background(background).clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Icon(icon, contentDescription = description, tint = tint, modifier = Modifier.size(20.dp))
    }
}
