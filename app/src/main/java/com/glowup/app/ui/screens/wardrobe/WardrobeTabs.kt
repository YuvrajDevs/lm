package com.glowup.app.ui.screens.wardrobe

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.glowup.app.R
import com.glowup.app.ui.data.WardrobeItem
import com.glowup.app.ui.data.WardrobeStore
import com.glowup.app.ui.data.style.ClosetOutfit
import com.glowup.app.ui.data.style.OutfitEngine
import com.glowup.app.ui.data.style.Slot
import com.glowup.app.ui.design.LineHeight
import com.glowup.app.ui.design.Shapes
import com.glowup.app.ui.design.Space
import com.glowup.app.ui.design.TextSize
import com.glowup.app.ui.screens.fitness.ChoicePill
import com.glowup.app.ui.theme.*
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale

@Composable
internal fun OutfitsTab(navController: NavHostController) {
    var occasion by remember { mutableStateOf(OutfitEngine.occasions.first()) }
    var seed by remember { mutableIntStateOf(0) }
    val items = WardrobeStore.items.toList()
    val outfits = remember(occasion, seed, items) { OutfitEngine.suggest(items, occasion.second, limit = 6, shuffleSeed = seed) }
    val today = LocalDate.now()
    LazyColumn(contentPadding = PaddingValues(Space.Gutter, Space.M, Space.Gutter, Space.Section), verticalArrangement = Arrangement.spacedBy(Space.M)) {
        item {
            LazyRow(horizontalArrangement = Arrangement.spacedBy(Space.S)) {
                items(OutfitEngine.occasions) { o -> ChoicePill(o.first, occasion == o) { occasion = o } }
            }
        }
        if (outfits.isEmpty()) {
            item {
                val missing = listOf(Slot.TOP, Slot.BOTTOM, Slot.SHOES).filter { s -> items.none { it.slot == s } }
                Column(Modifier.fillMaxWidth().clip(Shapes.Card).background(CardNeutral).padding(Space.L)) {
                    Text(
                        if (missing.isNotEmpty()) {
                            stringResource(
                                R.string.wardrobe_add_a_few_more_pieces
                            )
                        } else {
                            stringResource(R.string.wardrobe_nothing_fits_1_s_yet, occasion.first.lowercase())
                        },
                        fontSize = TextSize.Title,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        if (missing.isNotEmpty()) {
                            "Outfits need at least one ${missing.joinToString(", ") { it.label.lowercase() }}."
                        } else {
                            "None of your pieces are dressy or casual enough for this. Try another occasion or add something suitable."
                        },
                        color = TextSecondary,
                        fontSize = TextSize.BodySmall,
                        lineHeight = LineHeight.Compact,
                        modifier = Modifier.padding(top = Space.Xs)
                    )
                    Spacer(Modifier.height(Space.S))
                    Text(
                        stringResource(R.string.wardrobe_or_browse_the_lookbook_for),
                        color = Purple,
                        fontSize = TextSize.BodySmall,
                        fontWeight = FontWeight.SemiBold,
                        modifier = Modifier.clickable {
                            navController.navigate("styling")
                        }
                    )
                }
            }
        } else {
            items(outfits, key = { it.ids.sorted().joinToString() }) { o ->
                val wornToday = o.items.all { today in it.worn }
                Column(Modifier.fillMaxWidth().clip(Shapes.Large).background(CardNeutral).padding(Space.M)) {
                    Row(horizontalArrangement = Arrangement.spacedBy(Space.S)) {
                        o.items.forEach { ItemImage(it, Modifier.weight(1f).aspectRatio(0.8f), 14.dp) }
                    }
                    Spacer(Modifier.height(Space.S))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Column(Modifier.weight(1f)) {
                            Text(
                                o.items.joinToString(" · ") {
                                    it.title
                                },
                                fontSize = TextSize.BodySmall,
                                fontWeight = FontWeight.SemiBold,
                                maxLines = 2,
                                lineHeight = LineHeight.Compact
                            )
                            Text(
                                whyItWorks(o),
                                color = TextSecondary,
                                fontSize = TextSize.Caption,
                                lineHeight = LineHeight.Tight,
                                modifier = Modifier.padding(top = 2.dp)
                            )
                        }
                        Spacer(Modifier.width(Space.S))
                        Text(
                            if (wornToday) stringResource(R.string.wardrobe_worn_today) else stringResource(R.string.wardrobe_wear_today),
                            color = if (wornToday) Green else Color.White,
                            fontSize = TextSize.Caption,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.clip(Shapes.Pill).background(if (wornToday) GreenTint else Ink)
                                .clickable(enabled = !wornToday) { WardrobeStore.markWorn(o.ids, today) }.padding(Space.M, Space.S)
                        )
                    }
                }
            }
            item {
                Row(
                    Modifier.fillMaxWidth().clip(Shapes.Pill).background(ChipNeutral).clickable {
                        seed++
                    }.padding(vertical = Space.M),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Filled.Refresh, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(Space.S))
                    Text(stringResource(R.string.wardrobe_shuffle), fontSize = TextSize.Body, fontWeight = FontWeight.SemiBold)
                }
            }
        }
    }
}

internal fun whyItWorks(o: ClosetOutfit): String {
    val accents = o.items.map { it.colour }.filterNot(OutfitEngine::isNeutral).distinctBy { it.name }
    return when {
        accents.isEmpty() -> "All neutrals — clean and easy to get right"
        accents.size == 1 -> "Neutrals with one ${accents[0].name.lowercase()} accent"
        else -> "Earthy tones that sit well together"
    }
}

@Composable
internal fun WornTab(onOpen: (WardrobeItem) -> Unit) {
    val today = LocalDate.now()
    val items = WardrobeStore.items.toList()
    val days = (0..13).map { today.minusDays(it.toLong()) }
    val sleeping = items.filter { (it.lastWorn ?: it.addedOn).isBefore(today.minusDays(60)) }
    LazyColumn(contentPadding = PaddingValues(Space.Gutter, Space.M, Space.Gutter, Space.Section), verticalArrangement = Arrangement.spacedBy(Space.S)) {
        item {
            Row(horizontalArrangement = Arrangement.spacedBy(Space.S)) {
                StatTile(stringResource(R.string.wardrobe_pieces), "${items.size}", Modifier.weight(1f))
                StatTile(stringResource(R.string.wardrobe_never_worn), "${items.count { it.wearCount == 0 }}", Modifier.weight(1f))
                StatTile(stringResource(R.string.wardrobe_outfits_possible), "${OutfitEngine.countOutfits(items)}", Modifier.weight(1f))
            }
        }
        val priced = items.filter { it.price != null }
        if (priced.isNotEmpty()) {
            item {
                Text(
                    stringResource(R.string.wardrobe_cost_per_wear),
                    fontSize = TextSize.Title,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(top = Space.S)
                )
                Text(stringResource(R.string.wardrobe_the_more_you_wear_something), color = TextSecondary, fontSize = TextSize.Caption)
            }
            items(priced.sortedByDescending { it.costPerWear }.take(5), key = { "cpw${it.id}" }) { item ->
                Row(
                    Modifier.fillMaxWidth().clip(Shapes.Medium).background(CardNeutral).clickable {
                        onOpen(item)
                    }.padding(Space.S),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    ItemImage(item, Modifier.size(44.dp), 12.dp)
                    Spacer(Modifier.width(Space.M))
                    Column(Modifier.weight(1f)) {
                        Text(item.title, fontSize = TextSize.Body, fontWeight = FontWeight.SemiBold)
                        Text(stringResource(R.string.common_worn_1_s, item.wearCount), color = TextSecondary, fontSize = TextSize.Caption)
                    }
                    Text(stringResource(R.string.wardrobe_0f_wear).format(item.costPerWear), fontSize = TextSize.BodySmall, fontWeight = FontWeight.Bold)
                }
            }
        }
        if (sleeping.isNotEmpty()) {
            item {
                Text(
                    stringResource(R.string.wardrobe_not_worn_in_60_days),
                    fontSize = TextSize.Title,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(top = Space.S)
                )
                Text(stringResource(R.string.wardrobe_wear_it_this_week_or), color = TextSecondary, fontSize = TextSize.Caption)
            }
            item {
                LazyRow(horizontalArrangement = Arrangement.spacedBy(Space.S)) {
                    items(sleeping, key = { "s${it.id}" }) { ItemImage(it, Modifier.size(76.dp).clickable { onOpen(it) }, 14.dp) }
                }
            }
        }
        item {
            Text(
                stringResource(R.string.wardrobe_last_2_weeks),
                fontSize = TextSize.Title,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(top = Space.S)
            )
        }
        items(days, key = { it.toString() }) { d ->
            val worn = WardrobeStore.wornOn(d)
            Row(
                Modifier.fillMaxWidth().clip(Shapes.Medium).background(
                    if (d ==
                        today
                    ) {
                        PurpleTint
                    } else {
                        CardNeutral
                    }
                ).padding(Space.M),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(Modifier.width(64.dp)) {
                    Text(d.format(DateTimeFormatter.ofPattern("EEE", Locale.getDefault())), fontSize = TextSize.BodySmall, fontWeight = FontWeight.Bold)
                    Text(d.format(DateTimeFormatter.ofPattern("d MMM", Locale.getDefault())), color = TextSecondary, fontSize = TextSize.Caption)
                }
                if (worn.isEmpty()) {
                    Text(
                        if (d ==
                            today
                        ) {
                            stringResource(R.string.wardrobe_pick_an_outfit_in_the)
                        } else {
                            "—"
                        },
                        color = TextSecondary,
                        fontSize = TextSize.BodySmall
                    )
                } else {
                    Row(horizontalArrangement = Arrangement.spacedBy(Space.S)) {
                        worn.take(5).forEach { ItemImage(it, Modifier.size(40.dp).clickable { onOpen(it) }, 10.dp) }
                    }
                }
            }
        }
    }
}

@Composable
internal fun StatTile(label: String, value: String, modifier: Modifier) {
    Column(modifier.clip(Shapes.Medium).background(CardNeutral).padding(Space.M)) {
        Text(value, fontSize = TextSize.TitleLarge, fontWeight = FontWeight.Bold)
        Text(label, color = TextSecondary, fontSize = TextSize.Caption, lineHeight = LineHeight.Tight)
    }
}
