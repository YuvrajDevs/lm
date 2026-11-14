package com.glowup.app.ui.screens.wardrobe

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.selection.toggleable
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.outlined.Lightbulb
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import com.glowup.app.R
import com.glowup.app.ui.components.DetailHeader
import com.glowup.app.ui.components.goBack
import com.glowup.app.ui.data.WardrobeStore
import com.glowup.app.ui.data.garmentLabel
import com.glowup.app.ui.data.style.Outfit
import com.glowup.app.ui.data.style.OutfitEngine
import com.glowup.app.ui.design.LineHeight
import com.glowup.app.ui.design.Shapes
import com.glowup.app.ui.design.Space
import com.glowup.app.ui.design.TextSize
import com.glowup.app.ui.screens.fitness.ChoicePill
import com.glowup.app.ui.theme.*

@Composable
fun CapsuleScreen(navController: NavHostController) {
    var size by remember { mutableStateOf(10) }
    val items = WardrobeStore.items.toList()
    val result = remember(size, items) { OutfitEngine.capsule(items, size) }
    Column(Modifier.fillMaxSize().background(Surface).systemBarsPadding()) {
        DetailHeader(stringResource(R.string.wardrobe_tools_capsule_wardrobe)) { navController.goBack() }
        LazyColumn(contentPadding = PaddingValues(Space.Gutter, Space.L, Space.Gutter, Space.Section), verticalArrangement = Arrangement.spacedBy(Space.S)) {
            item {
                Text(
                    stringResource(R.string.wardrobe_tools_the_fewest_pieces_that_make),
                    color = TextSecondary,
                    fontSize = TextSize.Body,
                    lineHeight = LineHeight.Body
                )
                Spacer(Modifier.height(Space.M))
                Row(horizontalArrangement = Arrangement.spacedBy(Space.S)) {
                    listOf(7, 10, 15, 20).forEach { n ->
                        ChoicePill(stringResource(R.string.common_outfit_card_1_s_pieces, n), size == n, Modifier.weight(1f)) {
                            size =
                                n
                        }
                    }
                }
            }
            if (items.size < 5) {
                item {
                    Text(stringResource(R.string.wardrobe_tools_add_at_least_5_pieces), fontSize = TextSize.Body, modifier = Modifier.padding(top = Space.S))
                }
            } else {
                item {
                    Column(Modifier.fillMaxWidth().clip(Shapes.Large).background(Ink).padding(Space.Xl)) {
                        Text(
                            stringResource(R.string.common_outfit_card_1_s_pieces, result.items.size),
                            color = Color.White.copy(alpha = 0.6f),
                            fontSize = TextSize.Caption,
                            fontWeight = FontWeight.SemiBold
                        )
                        Text(
                            stringResource(R.string.wardrobe_tools_1_s_outfits, result.outfits),
                            color = Color.White,
                            fontSize = TextSize.DisplayLarge,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            stringResource(R.string.wardrobe_tools_from_what_you_already_own),
                            color = Color.White.copy(alpha = 0.6f),
                            fontSize = TextSize.BodySmall
                        )
                    }
                }
                item {
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(Space.S)) {
                        items(result.items, key = { it.id }) { item ->
                            Column(Modifier.width(96.dp)) {
                                ItemImage(item, Modifier.size(96.dp), 16.dp)
                                Text(item.title, fontSize = TextSize.Caption, maxLines = 1, modifier = Modifier.padding(top = Space.Xs))
                            }
                        }
                    }
                }
                if (result.gaps.isNotEmpty()) {
                    item {
                        Text(
                            stringResource(R.string.wardrobe_tools_to_get_more_out_of),
                            fontSize = TextSize.Title,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(top = Space.S)
                        )
                    }
                    items(result.gaps) { g ->
                        Row(Modifier.fillMaxWidth().clip(Shapes.Medium).background(YellowTint).padding(Space.M, Space.M)) {
                            Icon(Icons.Outlined.Lightbulb, contentDescription = null, tint = Amber, modifier = Modifier.size(18.dp))
                            Spacer(Modifier.width(Space.S))
                            Text(g, fontSize = TextSize.Body, lineHeight = LineHeight.Body)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun PackingScreen(navController: NavHostController) {
    val days = remember { mutableStateListOf("Everyday", "Everyday", "Dinner") }
    val packed = remember { mutableStateMapOf<Int, Boolean>() }
    val items = WardrobeStore.items.toList()
    val plan = remember(days.toList(), items) { OutfitEngine.pack(items, days.toList()) }
    Column(Modifier.fillMaxSize().background(Surface).systemBarsPadding()) {
        DetailHeader(stringResource(R.string.wardrobe_tools_packing_list)) { navController.goBack() }
        LazyColumn(contentPadding = PaddingValues(Space.Gutter, Space.L, Space.Gutter, Space.Section), verticalArrangement = Arrangement.spacedBy(Space.S)) {
            item {
                Text(stringResource(R.string.wardrobe_tools_add_a_day_for_each), color = TextSecondary, fontSize = TextSize.Body, lineHeight = LineHeight.Body)
            }
            itemsIndexed(days) { i, occasion ->
                val outfit = plan.days.getOrNull(i)?.second
                Column(Modifier.fillMaxWidth().clip(Shapes.Card).background(CardNeutral).padding(Space.M)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            stringResource(R.string.wardrobe_tools_day_1_s, i + 1),
                            fontSize = TextSize.BodyLarge,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.weight(1f)
                        )
                        Box(Modifier.size(30.dp).clip(CircleShape).clickable { if (days.size > 1) days.removeAt(i) }, contentAlignment = Alignment.Center) {
                            Icon(
                                Icons.Filled.Close,
                                contentDescription = stringResource(R.string.wardrobe_tools_remove_day),
                                tint = TextSecondary,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(Space.S), modifier = Modifier.padding(vertical = Space.S)) {
                        items(OutfitEngine.occasions.map { it.first }) { o -> ChoicePill(o, occasion == o) { days[i] = o } }
                    }
                    if (outfit ==
                        null
                    ) {
                        Text(stringResource(R.string.wardrobe_tools_nothing_suitable_in_your_closet), color = TextSecondary, fontSize = TextSize.BodySmall)
                    } else {
                        Row(horizontalArrangement = Arrangement.spacedBy(Space.S)) {
                            outfit.items.forEach { ItemImage(it, Modifier.weight(1f).aspectRatio(0.85f), 12.dp) }
                        }
                    }
                }
            }
            item {
                Row(
                    Modifier.fillMaxWidth().clip(Shapes.Pill).background(ChipNeutral).clickable {
                        if (days.size <
                            14
                        ) {
                            days.add("Everyday")
                        }
                    }.padding(vertical = Space.M),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Filled.Add, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(Space.S))
                    Text(stringResource(R.string.wardrobe_tools_add_a_day), fontSize = TextSize.Body, fontWeight = FontWeight.SemiBold)
                }
            }
            if (plan.items.isNotEmpty()) {
                item {
                    Text(
                        stringResource(R.string.wardrobe_tools_pack_these_1_s_pieces, plan.items.size),
                        fontSize = TextSize.Title,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(top = Space.S)
                    )
                    Text(stringResource(R.string.wardrobe_tools_1_s_outfits_tick_them, days.size), color = TextSecondary, fontSize = TextSize.Caption)
                }
                items(plan.items, key = { "p${it.id}" }) { item ->
                    val on = packed[item.id] == true
                    Row(
                        Modifier.fillMaxWidth().clip(
                            Shapes.Medium
                        ).background(if (on) GreenTint else CardNeutral).toggleable(value = on, role = Role.Checkbox) {
                            packed[item.id] =
                                it
                        }.padding(Space.S),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        ItemImage(item, Modifier.size(40.dp), 10.dp)
                        Spacer(Modifier.width(Space.M))
                        Text(
                            item.title,
                            fontSize = TextSize.Body,
                            fontWeight = FontWeight.Medium,
                            textDecoration = if (on) TextDecoration.LineThrough else null,
                            modifier = Modifier.weight(1f)
                        )
                        Box(Modifier.size(24.dp).clip(CircleShape).background(if (on) Green else Color.White), contentAlignment = Alignment.Center) {
                            if (on) Icon(Icons.Filled.Check, contentDescription = null, tint = Color.White, modifier = Modifier.size(15.dp))
                        }
                    }
                }
                item {
                    Text(
                        stringResource(R.string.wardrobe_tools_don_t_forget_underwear_and, days.size + 1),
                        color = TextSecondary,
                        fontSize = TextSize.BodySmall,
                        lineHeight = LineHeight.Compact,
                        modifier = Modifier.padding(top = Space.S)
                    )
                }
            }
        }
    }
}

/** On a lookbook outfit: which pieces you already own, and what you'd need. */
@Composable
fun MakeThisLook(outfit: Outfit, navController: NavHostController) {
    val items = WardrobeStore.items.toList()
    if (items.isEmpty()) {
        Row(
            Modifier.fillMaxWidth().clip(Shapes.Card).background(CardNeutral).clickable { navController.navigate("wardrobe") }.padding(Space.L, Space.M),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(Modifier.weight(1f)) {
                Text(stringResource(R.string.wardrobe_tools_make_this_look_from_your), fontSize = TextSize.BodyLarge, fontWeight = FontWeight.Bold)
                Text(stringResource(R.string.wardrobe_tools_add_your_clothes_and_we), color = TextSecondary, fontSize = TextSize.Caption)
            }
            Text("›", fontSize = TextSize.TitleLarge)
        }
        return
    }
    val matches = remember(outfit, items) { OutfitEngine.matchLook(outfit, items) }
    val owned = matches.count { it.owned != null }
    Column(Modifier.fillMaxWidth().clip(Shapes.Large).background(GreenTint).padding(Space.L)) {
        Text(
            stringResource(R.string.wardrobe_tools_from_your_wardrobe),
            color = GreenDeep,
            fontSize = TextSize.Micro,
            fontWeight = FontWeight.Bold,
            letterSpacing = 0.4.sp
        )
        Text(
            when {
                owned == matches.size -> stringResource(R.string.wardrobe_tools_you_own_this_whole_look)
                owned == 0 -> stringResource(R.string.wardrobe_tools_you_don_t_own_pieces)
                else -> stringResource(R.string.wardrobe_tools_you_own_1_s_of, owned, matches.size)
            },
            fontSize = TextSize.Title,
            fontWeight = FontWeight.Bold
        )
        Spacer(Modifier.height(Space.S))
        matches.forEach { m ->
            Row(Modifier.fillMaxWidth().padding(vertical = Space.Xs), verticalAlignment = Alignment.CenterVertically) {
                val o = m.owned
                if (o != null) {
                    ItemImage(o, Modifier.size(40.dp), 10.dp)
                } else {
                    Box(Modifier.size(40.dp).clip(Shapes.Small).background(Color.White), contentAlignment = Alignment.Center) {
                        SwatchDot(m.piece.swatch, 16.dp)
                    }
                }
                Spacer(Modifier.width(Space.M))
                Column(Modifier.weight(1f)) {
                    Text(m.piece.name, fontSize = TextSize.Body, fontWeight = FontWeight.SemiBold)
                    Text(
                        when {
                            o == null -> stringResource(
                                R.string.wardrobe_tools_look_for_1_s_2,
                                m.piece.swatch.name.lowercase(),
                                garmentLabel(m.piece.garment).lowercase()
                            )
                            m.exactColour -> stringResource(R.string.wardrobe_tools_your_1_s, o.title)
                            else -> stringResource(R.string.wardrobe_tools_your_1_s_works_instead, o.title)
                        },
                        color = if (o == null) Maroon else GreenDeep,
                        fontSize = TextSize.Caption
                    )
                }
                if (o !=
                    null
                ) {
                    Icon(
                        Icons.Filled.Check,
                        contentDescription = stringResource(R.string.wardrobe_tools_owned),
                        tint = Green,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        }
        if (owned > 0) {
            Spacer(Modifier.height(Space.S))
            Text(
                stringResource(R.string.wardrobe_tools_wear_what_i_own_today),
                color = Color.White,
                fontSize = TextSize.BodySmall,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.clip(Shapes.Pill).background(Ink).clickable {
                    WardrobeStore.markWorn(matches.mapNotNull { it.owned?.id })
                }.padding(Space.L, Space.S)
            )
        }
    }
}
