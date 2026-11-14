package com.glowup.app.ui.screens.wardrobe

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.outlined.AutoAwesome
import androidx.compose.material.icons.outlined.Checkroom
import androidx.compose.material.icons.outlined.Insights
import androidx.compose.material.icons.outlined.Inventory2
import androidx.compose.material.icons.outlined.Luggage
import androidx.compose.material3.Icon
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.glowup.app.R
import com.glowup.app.ui.components.DetailHeader
import com.glowup.app.ui.components.ProBadge
import com.glowup.app.ui.components.goBack
import com.glowup.app.ui.data.ProStore
import com.glowup.app.ui.data.WardrobeItem
import com.glowup.app.ui.data.WardrobeStore
import com.glowup.app.ui.data.style.Slot
import com.glowup.app.ui.design.AppButton
import com.glowup.app.ui.design.ButtonVariant
import com.glowup.app.ui.design.LineHeight
import com.glowup.app.ui.design.SectionHeader
import com.glowup.app.ui.design.Shapes
import com.glowup.app.ui.design.Space
import com.glowup.app.ui.design.TextSize
import com.glowup.app.ui.screens.fitness.ChoicePill
import com.glowup.app.ui.theme.*

private enum class Tab(val label: String) { CLOSET("Closet"), OUTFITS("Outfits"), WORN("Worn") }

@Composable
fun WardrobeScreen(navController: NavHostController) {
    var tab by remember { mutableStateOf(Tab.CLOSET) }
    var adding by remember { mutableStateOf(false) }
    var viewing by remember { mutableStateOf<WardrobeItem?>(null) }

    if (adding) {
        AddItemFlow(onDone = { adding = false })
        return
    }

    Box(Modifier.fillMaxSize().background(Surface).systemBarsPadding()) {
        Column(Modifier.fillMaxSize()) {
            DetailHeader(stringResource(R.string.common_my_wardrobe)) { navController.goBack() }
            Row(Modifier.padding(Space.Gutter, Space.M, Space.Gutter, 0.dp).fillMaxWidth().clip(Shapes.Pill).background(ChipNeutral).padding(Space.Xs)) {
                Tab.entries.forEach { t ->
                    Box(
                        Modifier.weight(1f).clip(Shapes.Pill).background(
                            if (tab ==
                                t
                            ) {
                                Ink
                            } else {
                                Color.Transparent
                            }
                        ).clickable { tab = t }.padding(vertical = Space.S),
                        contentAlignment = Alignment.Center
                    ) { Text(t.label, color = if (tab == t) Color.White else Ink, fontSize = TextSize.Body, fontWeight = FontWeight.SemiBold) }
                }
            }
            when (tab) {
                Tab.CLOSET -> ClosetTab(navController, onAdd = {
                    if (WardrobeStore.atFreeLimit) {
                        ProStore.showPaywall("Add more than ${WardrobeStore.FREE_MAX_ITEMS} items with Pro")
                    } else {
                        adding =
                            true
                    }
                }, onOpen = { viewing = it })
                Tab.OUTFITS -> OutfitsTab(navController)
                Tab.WORN -> WornTab { viewing = it }
            }
        }
    }
    viewing?.let { item -> ItemSheet(WardrobeStore.find(item.id) ?: item) { viewing = null } }
}

// ---------------- Closet ----------------

@Composable
private fun ClosetTab(navController: NavHostController, onAdd: () -> Unit, onOpen: (WardrobeItem) -> Unit) {
    var filter by remember { mutableStateOf<Slot?>(null) }
    val items = WardrobeStore.items.filter { filter == null || it.slot == filter }.sortedBy { it.slot.ordinal }
    Box(Modifier.fillMaxSize()) {
        LazyVerticalGrid(
            columns = GridCells.Fixed(2),
            contentPadding = PaddingValues(Space.Gutter, Space.M, Space.Gutter, 110.dp),
            horizontalArrangement = Arrangement.spacedBy(Space.S),
            verticalArrangement = Arrangement.spacedBy(Space.S)
        ) {
            item(span = { GridItemSpan(2) }) {
                LazyRow(horizontalArrangement = Arrangement.spacedBy(Space.S)) {
                    item { ChoicePill(stringResource(R.string.wardrobe_all_1_s, WardrobeStore.items.size), filter == null) { filter = null } }
                    items(Slot.entries) { s ->
                        ChoicePill("${s.label} · ${WardrobeStore.items.count { it.slot == s }}", filter == s) {
                            filter =
                                if (filter == s) null else s
                        }
                    }
                }
            }
            if (WardrobeStore.items.isEmpty()) {
                item(span = { GridItemSpan(2) }) { EmptyCloset(onAdd) }
                item(span = { GridItemSpan(2) }) { ClosetUnlocks() }
            } else {
                item(span = { GridItemSpan(2) }) { ToolsRow(navController) }
            }
            items(items, key = { it.id }) { item ->
                Column(Modifier.clip(Shapes.Card).background(CardNeutral).clickable { onOpen(item) }.padding(Space.S)) {
                    ItemImage(item, Modifier.fillMaxWidth().aspectRatio(1f))
                    Spacer(Modifier.height(Space.S))
                    Text(
                        item.title,
                        fontSize = TextSize.Body,
                        fontWeight = FontWeight.SemiBold,
                        maxLines = 1,
                        modifier = Modifier.padding(horizontal = Space.Xs)
                    )
                    Row(Modifier.padding(Space.Xs, 2.dp, Space.Xs, Space.Xs), verticalAlignment = Alignment.CenterVertically) {
                        SwatchDot(item.colour, 10.dp)
                        Spacer(Modifier.width(Space.Xs))
                        Text(
                            if (item.wearCount == 0) stringResource(R.string.common_not_worn_yet) else stringResource(R.string.common_worn_1_s, item.wearCount),
                            color = TextSecondary,
                            fontSize = TextSize.Caption,
                            maxLines = 1
                        )
                    }
                }
            }
        }
        if (WardrobeStore.items.isNotEmpty()) {
            AppButton(
                stringResource(R.string.common_add_clothes),
                variant = ButtonVariant.Primary,
                modifier = Modifier.align(Alignment.BottomCenter).padding(Space.Gutter, 0.dp, Space.Gutter, Space.Gutter).fillMaxWidth(),
                icon = Icons.Filled.Add,
                onClick = onAdd
            )
        }
    }
}

@Composable
private fun EmptyCloset(onAdd: () -> Unit) {
    Column(Modifier.fillMaxWidth().clip(Shapes.Large).background(Ink).padding(Space.Xl)) {
        Icon(Icons.Outlined.Checkroom, contentDescription = null, tint = Color.White, modifier = Modifier.size(30.dp))
        Spacer(Modifier.height(Space.S))
        Text(stringResource(R.string.wardrobe_build_your_digital_closet), color = Color.White, fontSize = TextSize.TitleLarge, fontWeight = FontWeight.Bold)
        Text(
            stringResource(R.string.wardrobe_snap_or_pick_a_photo),
            color = Color.White.copy(alpha = 0.7f),
            fontSize = TextSize.BodySmall,
            lineHeight = LineHeight.Compact,
            modifier = Modifier.padding(top = Space.Xs)
        )
        Spacer(Modifier.height(Space.S))
        Text(stringResource(R.string.wardrobe_tip_start_with_3_tops), color = Mint, fontSize = TextSize.BodySmall, lineHeight = LineHeight.Compact)
        Spacer(Modifier.height(Space.M))
        AppButton(stringResource(R.string.wardrobe_add_your_first_piece), variant = ButtonVariant.OnDarkPrimary, onClick = onAdd)
    }
}

@Composable
private fun ToolsRow(navController: NavHostController) {
    Row(horizontalArrangement = Arrangement.spacedBy(Space.S)) {
        Tool(Icons.Outlined.Inventory2, "Capsule", "Fewest pieces, most outfits", PurpleTint, Modifier.weight(1f)) {
            if (ProStore.isPro) navController.navigate("capsule") else ProStore.showPaywall("The capsule builder is part of Pro")
        }
        Tool(Icons.Outlined.Luggage, "Packing", "Outfits for every day of a trip", BlueTint, Modifier.weight(1f)) {
            if (ProStore.isPro) navController.navigate("packing") else ProStore.showPaywall("Packing lists are part of Pro")
        }
    }
}

@Composable
private fun Tool(icon: ImageVector, title: String, sub: String, tint: Color, modifier: Modifier, onClick: () -> Unit) {
    Column(modifier.clip(Shapes.Card).background(tint).clickable(onClick = onClick).padding(Space.M)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(icon, contentDescription = null, tint = Ink, modifier = Modifier.size(20.dp))
            Spacer(Modifier.weight(1f))
            if (!ProStore.isPro) ProBadge()
        }
        Spacer(Modifier.height(Space.S))
        Text(title, fontSize = TextSize.BodyLarge, fontWeight = FontWeight.Bold)
        Text(sub, color = TextSecondary, fontSize = TextSize.Caption, lineHeight = LineHeight.Tight)
    }
}

// ---------------- Outfits ----------------

// ---------------- Worn ----------------

// ---------------- Item detail ----------------

// ---------------- Add item ----------------

/** What the closet does once it has clothes in it, so the empty state shows the payoff. */
@Composable
private fun ClosetUnlocks() {
    Column(Modifier.fillMaxWidth().padding(top = Space.S)) {
        SectionHeader(stringResource(R.string.wardrobe_what_you_ll_unlock), subtitle = "Everything works from your own clothes")
        Spacer(Modifier.height(Space.M))
        UnlockRow(Icons.Outlined.AutoAwesome, PurpleTint, PurpleDeep, "Outfits", "Combinations built from pieces you own, matched by colour")
        UnlockRow(Icons.Outlined.Luggage, BlueTint, Navy, "Capsule & packing", "The fewest pieces for the most outfits, and trip lists")
        UnlockRow(Icons.Outlined.Insights, GreenTint, GreenDeep, "Wear tracking", "See what you reach for, and what never leaves the hanger")
    }
}

@Composable
private fun UnlockRow(icon: ImageVector, tint: Color, accent: Color, title: String, body: String) {
    Row(Modifier.fillMaxWidth().padding(vertical = Space.S), verticalAlignment = Alignment.CenterVertically) {
        Box(Modifier.size(44.dp).clip(Shapes.Medium).background(tint), contentAlignment = Alignment.Center) {
            Icon(icon, contentDescription = null, tint = accent, modifier = Modifier.size(22.dp))
        }
        Spacer(Modifier.width(Space.M))
        Column(Modifier.weight(1f)) {
            Text(title, fontSize = TextSize.BodyLarge, fontWeight = FontWeight.SemiBold)
            Text(body, color = TextSecondary, fontSize = TextSize.BodySmall, lineHeight = LineHeight.Compact)
        }
    }
}
