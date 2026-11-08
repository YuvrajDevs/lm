package com.glowup.app.ui.screens.wardrobe

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import com.glowup.app.ui.components.DetailHeader
import com.glowup.app.ui.components.goBack
import com.glowup.app.ui.data.WardrobeStore
import com.glowup.app.ui.data.garmentLabel
import com.glowup.app.ui.data.style.Outfit
import com.glowup.app.ui.data.style.OutfitEngine
import com.glowup.app.ui.screens.fitness.ChoicePill
import com.glowup.app.ui.theme.*

@Composable
fun CapsuleScreen(navController: NavHostController) {
    var size by remember { mutableStateOf(10) }
    val items = WardrobeStore.items.toList()
    val result = remember(size, items) { OutfitEngine.capsule(items, size) }
    Column(Modifier.fillMaxSize().background(Surface).systemBarsPadding()) {
        DetailHeader("Capsule wardrobe") { navController.goBack() }
        LazyColumn(contentPadding = PaddingValues(24.dp, 16.dp, 24.dp, 28.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            item {
                Text("The fewest pieces that make the most outfits — perfect for a minimal wardrobe or a busy week.", color = TextSecondary, fontSize = 13.5.sp, lineHeight = 19.sp)
                Spacer(Modifier.height(12.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    listOf(7, 10, 15, 20).forEach { n -> ChoicePill("$n pieces", size == n, Modifier.weight(1f)) { size = n } }
                }
            }
            if (items.size < 5) {
                item { Text("Add at least 5 pieces to your closet to build a capsule.", fontSize = 14.sp, modifier = Modifier.padding(top = 8.dp)) }
            } else {
                item {
                    Column(Modifier.fillMaxWidth().clip(RoundedCornerShape(24.dp)).background(Ink).padding(20.dp)) {
                        Text("${result.items.size} pieces", color = Color.White.copy(alpha = 0.6f), fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                        Text("${result.outfits} outfits", color = Color.White, fontSize = 30.sp, fontWeight = FontWeight.Bold)
                        Text("from what you already own", color = Color.White.copy(alpha = 0.6f), fontSize = 13.sp)
                    }
                }
                item {
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        items(result.items, key = { it.id }) { item ->
                            Column(Modifier.width(96.dp)) {
                                ItemImage(item, Modifier.size(96.dp), 16.dp)
                                Text(item.title, fontSize = 11.5.sp, maxLines = 1, modifier = Modifier.padding(top = 4.dp))
                            }
                        }
                    }
                }
                if (result.gaps.isNotEmpty()) {
                    item { Text("To get more out of it", fontSize = 16.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(top = 8.dp)) }
                    items(result.gaps) { g ->
                        Row(Modifier.fillMaxWidth().clip(RoundedCornerShape(18.dp)).background(YellowTint).padding(14.dp, 12.dp)) {
                            Icon(Icons.Outlined.Lightbulb, contentDescription = null, tint = Color(0xFF8A6A00), modifier = Modifier.size(18.dp))
                            Spacer(Modifier.width(10.dp))
                            Text(g, fontSize = 13.5.sp, lineHeight = 19.sp)
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
        DetailHeader("Packing list") { navController.goBack() }
        LazyColumn(contentPadding = PaddingValues(24.dp, 16.dp, 24.dp, 28.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            item {
                Text("Add a day for each day of your trip and what you'll be doing. We'll plan an outfit for each and reuse pieces so your bag stays light.", color = TextSecondary, fontSize = 13.5.sp, lineHeight = 19.sp)
            }
            itemsIndexed(days) { i, occasion ->
                val outfit = plan.days.getOrNull(i)?.second
                Column(Modifier.fillMaxWidth().clip(RoundedCornerShape(22.dp)).background(CardNeutral).padding(14.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text("Day ${i + 1}", fontSize = 15.sp, fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f))
                        Box(Modifier.size(30.dp).clip(CircleShape).clickable { if (days.size > 1) days.removeAt(i) }, contentAlignment = Alignment.Center) {
                            Icon(Icons.Filled.Close, contentDescription = "Remove day", tint = TextSecondary, modifier = Modifier.size(16.dp))
                        }
                    }
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp), modifier = Modifier.padding(vertical = 8.dp)) {
                        items(OutfitEngine.occasions.map { it.first }) { o -> ChoicePill(o, occasion == o) { days[i] = o } }
                    }
                    if (outfit == null) Text("Nothing suitable in your closet for this yet.", color = TextSecondary, fontSize = 12.5.sp)
                    else Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) { outfit.items.forEach { ItemImage(it, Modifier.weight(1f).aspectRatio(0.85f), 12.dp) } }
                }
            }
            item {
                Row(
                    Modifier.fillMaxWidth().clip(RoundedCornerShape(999.dp)).background(ChipNeutral).clickable { if (days.size < 14) days.add("Everyday") }.padding(vertical = 13.dp),
                    horizontalArrangement = Arrangement.Center, verticalAlignment = Alignment.CenterVertically,
                ) {
                    Icon(Icons.Filled.Add, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(6.dp))
                    Text("Add a day", fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
                }
            }
            if (plan.items.isNotEmpty()) {
                item {
                    Text("Pack these ${plan.items.size} pieces", fontSize = 17.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(top = 10.dp))
                    Text("${days.size} outfits · tick them off as you pack", color = TextSecondary, fontSize = 12.sp)
                }
                items(plan.items, key = { "p${it.id}" }) { item ->
                    val on = packed[item.id] == true
                    Row(Modifier.fillMaxWidth().clip(RoundedCornerShape(16.dp)).background(if (on) GreenTint else CardNeutral).clickable { packed[item.id] = !on }.padding(10.dp), verticalAlignment = Alignment.CenterVertically) {
                        ItemImage(item, Modifier.size(40.dp), 10.dp)
                        Spacer(Modifier.width(12.dp))
                        Text(item.title, fontSize = 14.sp, fontWeight = FontWeight.Medium, textDecoration = if (on) TextDecoration.LineThrough else null, modifier = Modifier.weight(1f))
                        Box(Modifier.size(24.dp).clip(CircleShape).background(if (on) Green else Color.White), contentAlignment = Alignment.Center) {
                            if (on) Icon(Icons.Filled.Check, contentDescription = null, tint = Color.White, modifier = Modifier.size(15.dp))
                        }
                    }
                }
                item {
                    Text("Don't forget: underwear and socks × ${days.size + 1}, sleepwear, toiletries, charger.", color = TextSecondary, fontSize = 12.5.sp, lineHeight = 17.sp, modifier = Modifier.padding(top = 6.dp))
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
            Modifier.fillMaxWidth().clip(RoundedCornerShape(22.dp)).background(CardNeutral).clickable { navController.navigate("wardrobe") }.padding(16.dp, 14.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(Modifier.weight(1f)) {
                Text("Make this look from your wardrobe", fontSize = 14.5.sp, fontWeight = FontWeight.Bold)
                Text("Add your clothes and we'll show what you already own", color = TextSecondary, fontSize = 12.sp)
            }
            Text("›", fontSize = 22.sp)
        }
        return
    }
    val matches = remember(outfit, items) { OutfitEngine.matchLook(outfit, items) }
    val owned = matches.count { it.owned != null }
    Column(Modifier.fillMaxWidth().clip(RoundedCornerShape(24.dp)).background(GreenTint).padding(18.dp)) {
        Text("FROM YOUR WARDROBE", color = Color(0xFF1E7A4F), fontSize = 11.sp, fontWeight = FontWeight.Bold, letterSpacing = 0.4.sp)
        Text(
            when {
                owned == matches.size -> "You own this whole look"
                owned == 0 -> "You don't own pieces for this yet"
                else -> "You own $owned of ${matches.size} pieces"
            },
            fontSize = 17.sp, fontWeight = FontWeight.Bold,
        )
        Spacer(Modifier.height(10.dp))
        matches.forEach { m ->
            Row(Modifier.fillMaxWidth().padding(vertical = 4.dp), verticalAlignment = Alignment.CenterVertically) {
                val o = m.owned
                if (o != null) ItemImage(o, Modifier.size(40.dp), 10.dp)
                else Box(Modifier.size(40.dp).clip(RoundedCornerShape(10.dp)).background(Color.White), contentAlignment = Alignment.Center) { SwatchDot(m.piece.swatch, 16.dp) }
                Spacer(Modifier.width(12.dp))
                Column(Modifier.weight(1f)) {
                    Text(m.piece.name, fontSize = 13.5.sp, fontWeight = FontWeight.SemiBold)
                    Text(
                        when {
                            o == null -> "Look for: ${m.piece.swatch.name.lowercase()} ${garmentLabel(m.piece.garment).lowercase()}"
                            m.exactColour -> "Your ${o.title}"
                            else -> "Your ${o.title} works instead"
                        },
                        color = if (o == null) Maroon else Color(0xFF1E7A4F), fontSize = 12.sp,
                    )
                }
                if (o != null) Icon(Icons.Filled.Check, contentDescription = "Owned", tint = Green, modifier = Modifier.size(18.dp))
            }
        }
        if (owned > 0) {
            Spacer(Modifier.height(10.dp))
            Text(
                "Wear what I own today", color = Color.White, fontSize = 13.sp, fontWeight = FontWeight.Bold,
                modifier = Modifier.clip(RoundedCornerShape(999.dp)).background(Ink).clickable { WardrobeStore.markWorn(matches.mapNotNull { it.owned?.id }) }.padding(16.dp, 9.dp),
            )
        }
    }
}
