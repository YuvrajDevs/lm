package com.glowup.app.ui.screens.styling

import com.glowup.app.ui.components.ProBadge
import com.glowup.app.ui.data.ProStore
import com.glowup.app.ui.data.ProGate
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
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
import com.glowup.app.ui.components.ReMakeUBottomNav
import com.glowup.app.ui.components.NavTab
import com.glowup.app.ui.components.OutfitBoard
import com.glowup.app.ui.components.OutfitCard
import com.glowup.app.ui.components.SwatchDot
import com.glowup.app.ui.data.style.SavedLooksStore
import com.glowup.app.ui.data.style.StyleLibrary
import com.glowup.app.ui.theme.*
import com.glowup.app.ui.design.LineHeight
import com.glowup.app.ui.design.Shapes
import com.glowup.app.ui.design.TextSize
import com.glowup.app.ui.design.SectionHeader
import com.glowup.app.ui.data.WardrobeStore
import com.glowup.app.ui.data.style.Outfit
import com.glowup.app.ui.data.style.OutfitEngine

@Composable
fun StylingScreen(navController: NavHostController) {
    var occasion by rememberSaveable { mutableStateOf("Office") }
    var offset by remember(occasion) { mutableStateOf(0) }
    val all = StyleLibrary.forOccasion(occasion)
    val options = all.filterNot { ProGate.outfitLocked(it) }.ifEmpty { all }
    val proOnlyCount = all.count { ProGate.outfitLocked(it) }
    fun openOutfit(outfit: Outfit) {
        if (ProGate.outfitLocked(outfit)) ProStore.showPaywall("${outfit.name} is part of the ${StyleLibrary.guide(outfit.styleKey)?.title} Pro guide")
        else navController.navigate("outfit/${outfit.id}")
    }
    val pick = options[(StyleLibrary.pickOfTheDay(occasion) + offset) % options.size]
    val pickGuide = StyleLibrary.guide(pick.styleKey)!!
    val saved = SavedLooksStore.ids.mapNotNull { StyleLibrary.outfit(it) }

    Box(modifier = Modifier.fillMaxSize().background(Surface).systemBarsPadding()) {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(bottom = 120.dp),
        ) {
            item {
                Text("Styling", style = MaterialTheme.typography.headlineMedium, modifier = Modifier.padding(24.dp, 32.dp, 24.dp, 0.dp))
                Text(
                    if (ProStore.isPro) "${StyleLibrary.outfits.size} looks across ${StyleLibrary.guides.size} styles" else "${StyleLibrary.guides.count { !ProGate.guideLocked(it.key) }} free styles · ${StyleLibrary.guides.size} with Pro",
                    color = TextSecondary,
                    fontSize = TextSize.BodySmall,
                    modifier = Modifier.padding(horizontal = 24.dp),
                )
                Spacer(Modifier.height(18.dp))
                Text("What are you dressing for?", fontSize = TextSize.BodyLarge, fontWeight = FontWeight.SemiBold, modifier = Modifier.padding(horizontal = 24.dp))
                Spacer(Modifier.height(10.dp))
                LazyRow(contentPadding = PaddingValues(horizontal = 24.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(StyleLibrary.occasions) { option ->
                        val selected = option == occasion
                        Box(
                            modifier = Modifier
                                .clip(Shapes.Pill)
                                .background(if (selected) Ink else ChipNeutral)
                                .clickable { occasion = option }
                                .padding(18.dp, 10.dp),
                        ) {
                            Text(option, color = if (selected) Color.White else Ink, fontSize = TextSize.Body, fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Medium)
                        }
                    }
                }
                Spacer(Modifier.height(14.dp))
            }

            item {
                Column(
                    modifier = Modifier
                        .padding(horizontal = 24.dp)
                        .fillMaxWidth()
                        .clip(Shapes.Hero)
                        .background(CardNeutral)
                        .clickable { openOutfit(pick) }
                        .padding(12.dp),
                ) {
                    OutfitBoard(pick, height = 250.dp)
                    Column(modifier = Modifier.padding(8.dp, 14.dp, 8.dp, 4.dp)) {
                        Text(
                            "${if (offset == 0) "TODAY'S PICK" else "LOOK ${offset + 1} OF ${options.size}"} · ${occasion.uppercase()} · ${pickGuide.title.uppercase()}",
                            color = pickGuide.accent,
                            fontSize = TextSize.Micro,
                            fontWeight = FontWeight.SemiBold,
                            letterSpacing = 0.4.sp,
                        )
                        Spacer(Modifier.height(4.dp))
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(pick.name, fontSize = TextSize.TitleLarge, fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f, fill = false))
                            if (ProGate.outfitLocked(pick)) {
                                Spacer(Modifier.width(8.dp))
                                ProBadge()
                            }
                        }
                        Spacer(Modifier.height(4.dp))
                        Text(pick.pieces.joinToString(" · ") { it.name }, color = TextSecondary, fontSize = TextSize.BodySmall, lineHeight = LineHeight.Compact, maxLines = 2)
                        Spacer(Modifier.height(14.dp))
                        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .height(46.dp)
                                    .clip(Shapes.Pill)
                                    .background(Ink)
                                    .clickable { openOutfit(pick) },
                                contentAlignment = Alignment.Center,
                            ) {
                                Text(if (ProGate.outfitLocked(pick)) "Unlock with Pro" else "See the breakdown", color = Color.White, fontSize = TextSize.Body, fontWeight = FontWeight.SemiBold)
                            }
                            Row(
                                modifier = Modifier
                                    .height(46.dp)
                                    .clip(Shapes.Pill)
                                    .background(Surface)
                                    .clickable { offset = (offset + 1) % options.size }
                                    .padding(horizontal = 16.dp),
                                verticalAlignment = Alignment.CenterVertically,
                            ) {
                                Icon(Icons.Filled.Refresh, contentDescription = null, tint = Ink, modifier = Modifier.size(16.dp))
                                Spacer(Modifier.width(6.dp))
                                Text("Another", fontSize = TextSize.Body, fontWeight = FontWeight.SemiBold)
                            }
                        }
                        if (proOnlyCount > 0 && options.size < all.size) {
                            Text(
                                "+$proOnlyCount more ${occasion.lowercase()} looks with Pro",
                                color = Amber,
                                fontSize = TextSize.BodySmall,
                                fontWeight = FontWeight.SemiBold,
                                modifier = Modifier.padding(top = 12.dp).clickable { ProStore.showPaywall("Unlock every ${occasion.lowercase()} look") },
                            )
                        }
                    }
                }
                Spacer(Modifier.height(14.dp))
            }

            item {
                Row(
                    modifier = Modifier
                        .padding(horizontal = 24.dp)
                        .fillMaxWidth()
                        .clip(Shapes.Card)
                        .background(Ink)
                        .clickable {
                            navController.navigate("wardrobe")
                        }
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    Image(painterResource(R.drawable.ic_style), contentDescription = null, modifier = Modifier.size(36.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text("Outfits from your wardrobe", color = Color.White, fontSize = TextSize.Body, fontWeight = FontWeight.SemiBold)
                        Text(
                            if (WardrobeStore.items.isEmpty()) "Add your clothes — we'll build looks from what you own"
                            else "${OutfitEngine.countOutfits(WardrobeStore.items)} outfits from your ${WardrobeStore.items.size} pieces",
                            color = Color.White.copy(alpha = 0.55f), fontSize = TextSize.Caption,
                        )
                    }
                    Icon(Icons.Filled.ChevronRight, contentDescription = null, tint = Color.White.copy(alpha = 0.6f))
                }
            }

            if (saved.isNotEmpty()) {
                item {
                    SectionTitle("Saved looks", "${saved.size} saved")
                    LazyRow(contentPadding = PaddingValues(horizontal = 24.dp), horizontalArrangement = Arrangement.spacedBy(14.dp)) {
                        items(saved, key = { it.id }) { outfit ->
                            OutfitCard(outfit, Modifier.width(200.dp), showStyle = true) { openOutfit(outfit) }
                        }
                    }
                }
            }

            item { SectionTitle("Style guides", "Palettes, outfits and what goes with what") }

            items(StyleLibrary.guides.chunked(2)) { pair ->
                Row(
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.padding(24.dp, 0.dp, 24.dp, 12.dp).fillMaxWidth().height(IntrinsicSize.Min),
                ) {
                    pair.forEach { guide ->
                        Column(
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxHeight()
                                .clip(Shapes.Card)
                                .background(guide.tint)
                                .clickable {
                                    if (ProGate.guideLocked(guide.key)) ProStore.showPaywall("The ${guide.title} guide is part of Pro")
                                    else navController.navigate("style_guide_detail/${guide.key}")
                                }
                                .padding(16.dp),
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Row(horizontalArrangement = Arrangement.spacedBy((-6).dp), modifier = Modifier.weight(1f)) {
                                    guide.palette.forEach { SwatchDot(it.color, 22.dp) }
                                }
                                if (ProGate.guideLocked(guide.key)) ProBadge()
                            }
                            Spacer(Modifier.height(14.dp))
                            Text(guide.title, fontSize = TextSize.Title, fontWeight = FontWeight.Bold)
                            Spacer(Modifier.height(2.dp))
                            Text(guide.blurb, color = guide.heroText, fontSize = TextSize.Caption, lineHeight = LineHeight.Tight, minLines = 2)
                            Spacer(Modifier.height(10.dp))
                            Text("${guide.outfits.size} looks", color = guide.accent, fontSize = TextSize.Caption, fontWeight = FontWeight.SemiBold)
                        }
                    }
                }
            }

            item {
                Row(
                    modifier = Modifier
                        .padding(24.dp, 4.dp, 24.dp, 0.dp)
                        .fillMaxWidth()
                        .clip(Shapes.Card)
                        .background(CardNeutral)
                        .clickable { navController.navigate("wardrobe") }
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    Box(modifier = Modifier.size(44.dp).clip(CircleShape).background(Surface), contentAlignment = Alignment.Center) {
                        Image(painterResource(R.drawable.ic_closet), contentDescription = null, modifier = Modifier.size(22.dp))
                    }
                    Column(modifier = Modifier.weight(1f)) {
                        Text("My wardrobe", fontSize = TextSize.BodyLarge, fontWeight = FontWeight.SemiBold)
                        Text("Keep track of the clothes you own", color = TextSecondary, fontSize = TextSize.Caption)
                    }
                }
            }
        }

        Box(modifier = Modifier.align(Alignment.BottomCenter)) {
            ReMakeUBottomNav(navController, NavTab.STYLING)
        }
    }
}

@Composable
private fun SectionTitle(title: String, subtitle: String) {
    SectionHeader(title, Modifier.padding(24.dp, 26.dp, 24.dp, 12.dp), subtitle)
}
