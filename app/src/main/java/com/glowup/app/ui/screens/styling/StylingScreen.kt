package com.glowup.app.ui.screens.styling

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Refresh
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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import com.glowup.app.R
import com.glowup.app.ui.components.NavTab
import com.glowup.app.ui.components.OutfitBoard
import com.glowup.app.ui.components.OutfitCard
import com.glowup.app.ui.components.ProBadge
import com.glowup.app.ui.components.ReMakeUBottomNav
import com.glowup.app.ui.components.SwatchDot
import com.glowup.app.ui.data.ProGate
import com.glowup.app.ui.data.ProStore
import com.glowup.app.ui.data.WardrobeStore
import com.glowup.app.ui.data.style.Outfit
import com.glowup.app.ui.data.style.OutfitEngine
import com.glowup.app.ui.data.style.SavedLooksStore
import com.glowup.app.ui.data.style.StyleLibrary
import com.glowup.app.ui.design.LineHeight
import com.glowup.app.ui.design.SectionHeader
import com.glowup.app.ui.design.Shapes
import com.glowup.app.ui.design.Space
import com.glowup.app.ui.design.TextSize
import com.glowup.app.ui.theme.*

@Composable
fun StylingScreen(navController: NavHostController) {
    var occasion by rememberSaveable { mutableStateOf("Office") }
    var offset by remember(occasion) { mutableStateOf(0) }
    val all = StyleLibrary.forOccasion(occasion)
    val options = all.filterNot { ProGate.outfitLocked(it) }.ifEmpty { all }
    val proOnlyCount = all.count { ProGate.outfitLocked(it) }
    fun openOutfit(outfit: Outfit) {
        if (ProGate.outfitLocked(outfit)) {
            ProStore.showPaywall("${outfit.name} is part of the ${StyleLibrary.guide(outfit.styleKey)?.title} Pro guide")
        } else {
            navController.navigate("outfit/${outfit.id}")
        }
    }
    val pick = options[(StyleLibrary.pickOfTheDay(occasion) + offset) % options.size]
    val pickGuide = StyleLibrary.guide(pick.styleKey)!!
    val saved = SavedLooksStore.ids.mapNotNull { StyleLibrary.outfit(it) }

    Box(modifier = Modifier.fillMaxSize().background(Surface).systemBarsPadding()) {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(bottom = 120.dp)
        ) {
            item {
                Text(
                    stringResource(R.string.styling_styling),
                    style = MaterialTheme.typography.headlineMedium,
                    modifier = Modifier.padding(Space.Gutter, Space.Xxl, Space.Gutter, 0.dp)
                )
                Text(
                    if (ProStore.isPro) {
                        stringResource(
                            R.string.styling_1_s_looks_across_2,
                            StyleLibrary.outfits.size,
                            StyleLibrary.guides.size
                        )
                    } else {
                        "${StyleLibrary.guides.count {
                            !ProGate.guideLocked(it.key)
                        }} free styles · ${StyleLibrary.guides.size} with Pro"
                    },
                    color = TextSecondary,
                    fontSize = TextSize.BodySmall,
                    modifier = Modifier.padding(horizontal = Space.Gutter)
                )
                Spacer(Modifier.height(Space.L))
                Text(
                    stringResource(R.string.styling_what_are_you_dressing_for),
                    fontSize = TextSize.BodyLarge,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.padding(horizontal = Space.Gutter)
                )
                Spacer(Modifier.height(Space.S))
                LazyRow(contentPadding = PaddingValues(horizontal = Space.Gutter), horizontalArrangement = Arrangement.spacedBy(Space.S)) {
                    items(StyleLibrary.occasions) { option ->
                        val selected = option == occasion
                        Box(
                            modifier = Modifier
                                .clip(Shapes.Pill)
                                .background(if (selected) Ink else ChipNeutral)
                                .clickable { occasion = option }
                                .padding(Space.L, Space.S)
                        ) {
                            Text(
                                option,
                                color = if (selected) Color.White else Ink,
                                fontSize = TextSize.Body,
                                fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Medium
                            )
                        }
                    }
                }
                Spacer(Modifier.height(Space.M))
            }

            item {
                Column(
                    modifier = Modifier
                        .padding(horizontal = Space.Gutter)
                        .fillMaxWidth()
                        .clip(Shapes.Hero)
                        .background(CardNeutral)
                        .clickable { openOutfit(pick) }
                        .padding(Space.M)
                ) {
                    OutfitBoard(pick, height = 250.dp)
                    Column(modifier = Modifier.padding(Space.S, Space.M, Space.S, Space.Xs)) {
                        Text(
                            "${if (offset == 0) "TODAY'S PICK" else "LOOK ${offset + 1} OF ${options.size}"} · ${occasion.uppercase()} · ${pickGuide.title.uppercase()}",
                            color = pickGuide.accent,
                            fontSize = TextSize.Micro,
                            fontWeight = FontWeight.SemiBold,
                            letterSpacing = 0.4.sp
                        )
                        Spacer(Modifier.height(Space.Xs))
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(pick.name, fontSize = TextSize.TitleLarge, fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f, fill = false))
                            if (ProGate.outfitLocked(pick)) {
                                Spacer(Modifier.width(Space.S))
                                ProBadge()
                            }
                        }
                        Spacer(Modifier.height(Space.Xs))
                        Text(
                            pick.pieces.joinToString(" · ") {
                                it.name
                            },
                            color = TextSecondary,
                            fontSize = TextSize.BodySmall,
                            lineHeight = LineHeight.Compact,
                            maxLines = 2
                        )
                        Spacer(Modifier.height(Space.M))
                        Row(horizontalArrangement = Arrangement.spacedBy(Space.S)) {
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .height(46.dp)
                                    .clip(Shapes.Pill)
                                    .background(Ink)
                                    .clickable { openOutfit(pick) },
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    if (ProGate.outfitLocked(
                                            pick
                                        )
                                    ) {
                                        stringResource(R.string.common_unlock_with_pro)
                                    } else {
                                        stringResource(R.string.styling_see_the_breakdown)
                                    },
                                    color = Color.White,
                                    fontSize = TextSize.Body,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }
                            Row(
                                modifier = Modifier
                                    .height(46.dp)
                                    .clip(Shapes.Pill)
                                    .background(Surface)
                                    .clickable { offset = (offset + 1) % options.size }
                                    .padding(horizontal = Space.L),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(Icons.Filled.Refresh, contentDescription = null, tint = Ink, modifier = Modifier.size(16.dp))
                                Spacer(Modifier.width(Space.S))
                                Text(stringResource(R.string.styling_another), fontSize = TextSize.Body, fontWeight = FontWeight.SemiBold)
                            }
                        }
                        if (proOnlyCount > 0 && options.size < all.size) {
                            Text(
                                stringResource(R.string.styling_1_s_more_2_s, proOnlyCount, occasion.lowercase()),
                                color = Amber,
                                fontSize = TextSize.BodySmall,
                                fontWeight = FontWeight.SemiBold,
                                modifier = Modifier.padding(top = Space.M).clickable { ProStore.showPaywall("Unlock every ${occasion.lowercase()} look") }
                            )
                        }
                    }
                }
                Spacer(Modifier.height(Space.M))
            }

            item {
                Row(
                    modifier = Modifier
                        .padding(horizontal = Space.Gutter)
                        .fillMaxWidth()
                        .clip(Shapes.Card)
                        .background(Ink)
                        .clickable {
                            navController.navigate("wardrobe")
                        }
                        .padding(Space.L),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(Space.M)
                ) {
                    Image(painterResource(R.drawable.ic_style), contentDescription = null, modifier = Modifier.size(36.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            stringResource(R.string.styling_outfits_from_your_wardrobe),
                            color = Color.White,
                            fontSize = TextSize.Body,
                            fontWeight = FontWeight.SemiBold
                        )
                        Text(
                            if (WardrobeStore.items.isEmpty()) {
                                stringResource(R.string.styling_add_your_clothes_we_ll)
                            } else {
                                stringResource(R.string.styling_1_s_outfits_from_your, OutfitEngine.countOutfits(WardrobeStore.items), WardrobeStore.items.size)
                            },
                            color = Color.White.copy(alpha = 0.55f),
                            fontSize = TextSize.Caption
                        )
                    }
                    Icon(Icons.Filled.ChevronRight, contentDescription = null, tint = Color.White.copy(alpha = 0.6f))
                }
            }

            if (saved.isNotEmpty()) {
                item {
                    SectionTitle("Saved looks", "${saved.size} saved")
                    LazyRow(contentPadding = PaddingValues(horizontal = Space.Gutter), horizontalArrangement = Arrangement.spacedBy(Space.M)) {
                        items(saved, key = { it.id }) { outfit ->
                            OutfitCard(outfit, Modifier.width(200.dp), showStyle = true) { openOutfit(outfit) }
                        }
                    }
                }
            }

            item { SectionTitle("Style guides", "Palettes, outfits and what goes with what") }

            items(StyleLibrary.guides.chunked(2)) { pair ->
                Row(
                    horizontalArrangement = Arrangement.spacedBy(Space.M),
                    modifier = Modifier.padding(Space.Gutter, 0.dp, Space.Gutter, Space.M).fillMaxWidth().height(IntrinsicSize.Min)
                ) {
                    pair.forEach { guide ->
                        Column(
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxHeight()
                                .clip(Shapes.Card)
                                .background(guide.tint)
                                .clickable {
                                    if (ProGate.guideLocked(guide.key)) {
                                        ProStore.showPaywall("The ${guide.title} guide is part of Pro")
                                    } else {
                                        navController.navigate("style_guide_detail/${guide.key}")
                                    }
                                }
                                .padding(Space.L)
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Row(horizontalArrangement = Arrangement.spacedBy((-6).dp), modifier = Modifier.weight(1f)) {
                                    guide.palette.forEach { SwatchDot(it.color, 22.dp) }
                                }
                                if (ProGate.guideLocked(guide.key)) ProBadge()
                            }
                            Spacer(Modifier.height(Space.M))
                            Text(guide.title, fontSize = TextSize.Title, fontWeight = FontWeight.Bold)
                            Spacer(Modifier.height(2.dp))
                            Text(guide.blurb, color = guide.heroText, fontSize = TextSize.Caption, lineHeight = LineHeight.Tight, minLines = 2)
                            Spacer(Modifier.height(Space.S))
                            Text(
                                stringResource(R.string.styling_1_s_looks, guide.outfits.size),
                                color = guide.accent,
                                fontSize = TextSize.Caption,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }
                }
            }

            item {
                Row(
                    modifier = Modifier
                        .padding(Space.Gutter, Space.Xs, Space.Gutter, 0.dp)
                        .fillMaxWidth()
                        .clip(Shapes.Card)
                        .background(CardNeutral)
                        .clickable { navController.navigate("wardrobe") }
                        .padding(Space.L),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(Space.M)
                ) {
                    Box(modifier = Modifier.size(44.dp).clip(CircleShape).background(Surface), contentAlignment = Alignment.Center) {
                        Image(painterResource(R.drawable.ic_closet), contentDescription = null, modifier = Modifier.size(22.dp))
                    }
                    Column(modifier = Modifier.weight(1f)) {
                        Text(stringResource(R.string.common_my_wardrobe), fontSize = TextSize.BodyLarge, fontWeight = FontWeight.SemiBold)
                        Text(stringResource(R.string.styling_keep_track_of_the_clothes), color = TextSecondary, fontSize = TextSize.Caption)
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
    SectionHeader(title, Modifier.padding(Space.Gutter, Space.Gutter, Space.Gutter, Space.M), subtitle)
}
