package com.glowup.app.ui.screens.wardrobe

import android.graphics.Bitmap
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.PhotoCamera
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.outlined.Checkroom
import androidx.compose.material.icons.outlined.Collections
import androidx.compose.material.icons.outlined.DeleteOutline
import androidx.compose.material.icons.outlined.Inventory2
import androidx.compose.material.icons.outlined.Luggage
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
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
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.FileProvider
import androidx.navigation.NavHostController
import com.glowup.app.ui.components.ConsentSheet
import com.glowup.app.ui.components.DetailHeader
import com.glowup.app.ui.components.GlowUpDialog
import com.glowup.app.ui.components.goBack
import com.glowup.app.ui.data.Consent
import com.glowup.app.ui.data.ConsentStore
import com.glowup.app.ui.data.PhotoVault
import com.glowup.app.ui.data.ProStore
import com.glowup.app.ui.data.WardrobeItem
import com.glowup.app.ui.data.WardrobeStore
import com.glowup.app.ui.data.defaultFormality
import com.glowup.app.ui.data.garmentLabel
import com.glowup.app.ui.data.style.ClosetOutfit
import com.glowup.app.ui.data.style.Garment
import com.glowup.app.ui.data.style.OutfitEngine
import com.glowup.app.ui.data.style.Slot
import com.glowup.app.ui.data.style.Swatch
import com.glowup.app.ui.screens.fitness.ChoicePill
import com.glowup.app.ui.theme.*
import java.io.File
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale

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
            DetailHeader("My wardrobe") { navController.goBack() }
            Row(Modifier.padding(24.dp, 14.dp, 24.dp, 0.dp).fillMaxWidth().clip(RoundedCornerShape(999.dp)).background(ChipNeutral).padding(4.dp)) {
                Tab.entries.forEach { t ->
                    Box(
                        Modifier.weight(1f).clip(RoundedCornerShape(999.dp)).background(if (tab == t) Ink else Color.Transparent).clickable { tab = t }.padding(vertical = 10.dp),
                        contentAlignment = Alignment.Center,
                    ) { Text(t.label, color = if (tab == t) Color.White else Ink, fontSize = 14.sp, fontWeight = FontWeight.SemiBold) }
                }
            }
            when (tab) {
                Tab.CLOSET -> ClosetTab(navController, onAdd = { if (WardrobeStore.atFreeLimit) ProStore.showPaywall("Add more than ${WardrobeStore.FREE_MAX_ITEMS} items with Pro") else adding = true }, onOpen = { viewing = it })
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
            contentPadding = PaddingValues(24.dp, 14.dp, 24.dp, 110.dp),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            item(span = { GridItemSpan(2) }) {
                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    item { ChoicePill("All · ${WardrobeStore.items.size}", filter == null) { filter = null } }
                    items(Slot.entries) { s -> ChoicePill("${s.label} · ${WardrobeStore.items.count { it.slot == s }}", filter == s) { filter = if (filter == s) null else s } }
                }
            }
            if (WardrobeStore.items.isEmpty()) {
                item(span = { GridItemSpan(2) }) { EmptyCloset(onAdd) }
            } else {
                item(span = { GridItemSpan(2) }) { ToolsRow(navController) }
            }
            items(items, key = { it.id }) { item ->
                Column(Modifier.clip(RoundedCornerShape(20.dp)).background(CardNeutral).clickable { onOpen(item) }.padding(8.dp)) {
                    ItemImage(item, Modifier.fillMaxWidth().aspectRatio(1f))
                    Spacer(Modifier.height(8.dp))
                    Text(item.title, fontSize = 13.5.sp, fontWeight = FontWeight.SemiBold, maxLines = 1, modifier = Modifier.padding(horizontal = 4.dp))
                    Row(Modifier.padding(4.dp, 2.dp, 4.dp, 4.dp), verticalAlignment = Alignment.CenterVertically) {
                        SwatchDot(item.colour, 10.dp)
                        Spacer(Modifier.width(5.dp))
                        Text(
                            if (item.wearCount == 0) "Not worn yet" else "Worn ${item.wearCount}×",
                            color = TextSecondary, fontSize = 11.5.sp, maxLines = 1,
                        )
                    }
                }
            }
        }
        if (WardrobeStore.items.isNotEmpty()) Row(
            Modifier.align(Alignment.BottomCenter).padding(24.dp, 0.dp, 24.dp, 24.dp).fillMaxWidth().height(56.dp).clip(RoundedCornerShape(999.dp)).background(Ink).clickable(onClick = onAdd),
            horizontalArrangement = Arrangement.Center, verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(Icons.Filled.Add, contentDescription = null, tint = Color.White)
            Spacer(Modifier.width(6.dp))
            Text("Add clothes", color = Color.White, fontSize = 15.sp, fontWeight = FontWeight.SemiBold)
        }
    }
}

@Composable
private fun EmptyCloset(onAdd: () -> Unit) {
    Column(Modifier.fillMaxWidth().clip(RoundedCornerShape(26.dp)).background(Ink).padding(22.dp)) {
        Icon(Icons.Outlined.Checkroom, contentDescription = null, tint = Color.White, modifier = Modifier.size(30.dp))
        Spacer(Modifier.height(10.dp))
        Text("Build your digital closet", color = Color.White, fontSize = 20.sp, fontWeight = FontWeight.Bold)
        Text(
            "Snap or pick a photo of each piece. We'll spot the colour, then build outfits from what you actually own, plan trips and show what you never wear.",
            color = Color.White.copy(alpha = 0.7f), fontSize = 13.sp, lineHeight = 18.sp, modifier = Modifier.padding(top = 4.dp),
        )
        Spacer(Modifier.height(10.dp))
        Text("Tip: start with 3 tops, 2 bottoms and 2 pairs of shoes — that's already 12 outfits.", color = Color(0xFF7FD9A8), fontSize = 12.5.sp, lineHeight = 17.sp)
        Spacer(Modifier.height(14.dp))
        Box(Modifier.fillMaxWidth().height(50.dp).clip(RoundedCornerShape(999.dp)).background(Color.White).clickable(onClick = onAdd), contentAlignment = Alignment.Center) {
            Text("Add your first piece", color = Ink, fontSize = 14.5.sp, fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
private fun ToolsRow(navController: NavHostController) {
    Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
        Tool(Icons.Outlined.Inventory2, "Capsule", "Fewest pieces, most outfits", PurpleTint, Modifier.weight(1f)) {
            if (ProStore.isPro) navController.navigate("capsule") else ProStore.showPaywall("The capsule builder is part of Pro")
        }
        Tool(Icons.Outlined.Luggage, "Packing", "Outfits for every day of a trip", BlueTint, Modifier.weight(1f)) {
            if (ProStore.isPro) navController.navigate("packing") else ProStore.showPaywall("Packing lists are part of Pro")
        }
    }
}

@Composable
private fun Tool(icon: androidx.compose.ui.graphics.vector.ImageVector, title: String, sub: String, tint: Color, modifier: Modifier, onClick: () -> Unit) {
    Column(modifier.clip(RoundedCornerShape(20.dp)).background(tint).clickable(onClick = onClick).padding(14.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(icon, contentDescription = null, tint = Ink, modifier = Modifier.size(20.dp))
            Spacer(Modifier.weight(1f))
            if (!ProStore.isPro) com.glowup.app.ui.components.ProBadge()
        }
        Spacer(Modifier.height(8.dp))
        Text(title, fontSize = 14.5.sp, fontWeight = FontWeight.Bold)
        Text(sub, color = TextSecondary, fontSize = 11.5.sp, lineHeight = 15.sp)
    }
}

// ---------------- Outfits ----------------

@Composable
private fun OutfitsTab(navController: NavHostController) {
    var occasion by remember { mutableStateOf(OutfitEngine.occasions.first()) }
    var seed by remember { mutableIntStateOf(0) }
    val items = WardrobeStore.items.toList()
    val outfits = remember(occasion, seed, items) { OutfitEngine.suggest(items, occasion.second, limit = 6, shuffleSeed = seed) }
    val today = LocalDate.now()
    LazyColumn(contentPadding = PaddingValues(24.dp, 14.dp, 24.dp, 28.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        item {
            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                items(OutfitEngine.occasions) { o -> ChoicePill(o.first, occasion == o) { occasion = o } }
            }
        }
        if (outfits.isEmpty()) {
            item {
                val missing = listOf(Slot.TOP, Slot.BOTTOM, Slot.SHOES).filter { s -> items.none { it.slot == s } }
                Column(Modifier.fillMaxWidth().clip(RoundedCornerShape(22.dp)).background(CardNeutral).padding(18.dp)) {
                    Text(if (missing.isNotEmpty()) "Add a few more pieces" else "Nothing fits ${occasion.first.lowercase()} yet", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                    Text(
                        if (missing.isNotEmpty()) "Outfits need at least one ${missing.joinToString(", ") { it.label.lowercase() }}."
                        else "None of your pieces are dressy or casual enough for this. Try another occasion or add something suitable.",
                        color = TextSecondary, fontSize = 13.sp, lineHeight = 18.sp, modifier = Modifier.padding(top = 4.dp),
                    )
                    Spacer(Modifier.height(10.dp))
                    Text("Or browse the lookbook for ideas →", color = Purple, fontSize = 13.sp, fontWeight = FontWeight.SemiBold, modifier = Modifier.clickable { navController.navigate("styling") })
                }
            }
        } else {
            items(outfits, key = { it.ids.sorted().joinToString() }) { o ->
                val wornToday = o.items.all { today in it.worn }
                Column(Modifier.fillMaxWidth().clip(RoundedCornerShape(24.dp)).background(CardNeutral).padding(12.dp)) {
                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        o.items.forEach { ItemImage(it, Modifier.weight(1f).aspectRatio(0.8f), 14.dp) }
                    }
                    Spacer(Modifier.height(10.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Column(Modifier.weight(1f)) {
                            Text(o.items.joinToString(" · ") { it.title }, fontSize = 13.sp, fontWeight = FontWeight.SemiBold, maxLines = 2, lineHeight = 17.sp)
                            Text(whyItWorks(o), color = TextSecondary, fontSize = 11.5.sp, lineHeight = 15.sp, modifier = Modifier.padding(top = 2.dp))
                        }
                        Spacer(Modifier.width(8.dp))
                        Text(
                            if (wornToday) "Worn today ✓" else "Wear today",
                            color = if (wornToday) Green else Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold,
                            modifier = Modifier.clip(RoundedCornerShape(999.dp)).background(if (wornToday) GreenTint else Ink)
                                .clickable(enabled = !wornToday) { WardrobeStore.markWorn(o.ids, today) }.padding(12.dp, 8.dp),
                        )
                    }
                }
            }
            item {
                Row(Modifier.fillMaxWidth().clip(RoundedCornerShape(999.dp)).background(ChipNeutral).clickable { seed++ }.padding(vertical = 13.dp), horizontalArrangement = Arrangement.Center, verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Filled.Refresh, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(6.dp))
                    Text("Shuffle", fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
                }
            }
        }
    }
}

private fun whyItWorks(o: ClosetOutfit): String {
    val accents = o.items.map { it.colour }.filterNot(OutfitEngine::isNeutral).distinctBy { it.name }
    return when {
        accents.isEmpty() -> "All neutrals — clean and easy to get right"
        accents.size == 1 -> "Neutrals with one ${accents[0].name.lowercase()} accent"
        else -> "Earthy tones that sit well together"
    }
}

// ---------------- Worn ----------------

@Composable
private fun WornTab(onOpen: (WardrobeItem) -> Unit) {
    val today = LocalDate.now()
    val items = WardrobeStore.items.toList()
    val days = (0..13).map { today.minusDays(it.toLong()) }
    val sleeping = items.filter { (it.lastWorn ?: it.addedOn).isBefore(today.minusDays(60)) }
    LazyColumn(contentPadding = PaddingValues(24.dp, 14.dp, 24.dp, 28.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
        item {
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                StatTile("Pieces", "${items.size}", Modifier.weight(1f))
                StatTile("Never worn", "${items.count { it.wearCount == 0 }}", Modifier.weight(1f))
                StatTile("Outfits possible", "${OutfitEngine.countOutfits(items)}", Modifier.weight(1f))
            }
        }
        val priced = items.filter { it.price != null }
        if (priced.isNotEmpty()) {
            item {
                Text("Cost per wear", fontSize = 16.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(top = 8.dp))
                Text("The more you wear something, the better value it becomes", color = TextSecondary, fontSize = 12.sp)
            }
            items(priced.sortedByDescending { it.costPerWear }.take(5), key = { "cpw${it.id}" }) { item ->
                Row(Modifier.fillMaxWidth().clip(RoundedCornerShape(18.dp)).background(CardNeutral).clickable { onOpen(item) }.padding(10.dp), verticalAlignment = Alignment.CenterVertically) {
                    ItemImage(item, Modifier.size(44.dp), 12.dp)
                    Spacer(Modifier.width(12.dp))
                    Column(Modifier.weight(1f)) {
                        Text(item.title, fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
                        Text("Worn ${item.wearCount}×", color = TextSecondary, fontSize = 12.sp)
                    }
                    Text("%.0f / wear".format(item.costPerWear), fontSize = 13.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
        if (sleeping.isNotEmpty()) {
            item {
                Text("Not worn in 60+ days", fontSize = 16.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(top = 8.dp))
                Text("Wear it this week, or think about passing it on", color = TextSecondary, fontSize = 12.sp)
            }
            item {
                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(sleeping, key = { "s${it.id}" }) { ItemImage(it, Modifier.size(76.dp).clickable { onOpen(it) }, 14.dp) }
                }
            }
        }
        item { Text("Last 2 weeks", fontSize = 16.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(top = 8.dp)) }
        items(days, key = { it.toString() }) { d ->
            val worn = WardrobeStore.wornOn(d)
            Row(Modifier.fillMaxWidth().clip(RoundedCornerShape(18.dp)).background(if (d == today) PurpleTint else CardNeutral).padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                Column(Modifier.width(64.dp)) {
                    Text(d.format(DateTimeFormatter.ofPattern("EEE", Locale.getDefault())), fontSize = 13.sp, fontWeight = FontWeight.Bold)
                    Text(d.format(DateTimeFormatter.ofPattern("d MMM", Locale.getDefault())), color = TextSecondary, fontSize = 11.5.sp)
                }
                if (worn.isEmpty()) Text(if (d == today) "Pick an outfit in the Outfits tab" else "—", color = TextSecondary, fontSize = 12.5.sp)
                else Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) { worn.take(5).forEach { ItemImage(it, Modifier.size(40.dp).clickable { onOpen(it) }, 10.dp) } }
            }
        }
    }
}

@Composable
private fun StatTile(label: String, value: String, modifier: Modifier) {
    Column(modifier.clip(RoundedCornerShape(18.dp)).background(CardNeutral).padding(12.dp)) {
        Text(value, fontSize = 22.sp, fontWeight = FontWeight.Bold)
        Text(label, color = TextSecondary, fontSize = 11.5.sp, lineHeight = 14.sp)
    }
}

// ---------------- Item detail ----------------

@Composable
private fun ItemSheet(item: WardrobeItem, onClose: () -> Unit) {
    var confirmDelete by remember { mutableStateOf(false) }
    val today = LocalDate.now()
    GlowUpDialog(
        onDismissRequest = onClose,
        title = item.title,
        primaryText = if (today in item.worn) "Worn today ✓" else "Wore it today",
        primaryEnabled = today !in item.worn,
        onPrimary = { WardrobeStore.markWorn(listOf(item.id), today); onClose() },
        secondaryText = "Close",
        content = {
            ItemImage(item, Modifier.fillMaxWidth().aspectRatio(1.2f), 20.dp)
            Spacer(Modifier.height(12.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                SwatchDot(item.colour, 14.dp)
                Spacer(Modifier.width(6.dp))
                Text("${item.colour.name} · ${garmentLabel(item.garment)} · " + listOf("Casual", "Smart casual", "Formal")[item.formality - 1], fontSize = 13.sp, color = TextSecondary)
            }
            Text(
                (if (item.wearCount == 0) "Not worn yet" else "Worn ${item.wearCount}× · last on ${item.lastWorn!!.format(DateTimeFormatter.ofPattern("d MMM", Locale.getDefault()))}") +
                    (item.costPerWear?.let { " · %.0f per wear".format(it) } ?: ""),
                fontSize = 13.sp, modifier = Modifier.padding(top = 4.dp),
            )
            Text(
                "Delete item", color = Maroon, fontSize = 13.sp, fontWeight = FontWeight.SemiBold,
                modifier = Modifier.padding(top = 12.dp).clickable { confirmDelete = true },
            )
        },
    )
    if (confirmDelete) {
        GlowUpDialog(
            onDismissRequest = { confirmDelete = false }, title = "Delete ${item.title}?", message = "Its photo and wear history are removed from this phone.",
            primaryText = "Delete", primaryDestructive = true, onPrimary = { WardrobeStore.remove(item.id); confirmDelete = false; onClose() },
        )
    }
}

// ---------------- Add item ----------------

@OptIn(androidx.compose.foundation.layout.ExperimentalLayoutApi::class)
@Composable
private fun AddItemFlow(onDone: () -> Unit) {
    val context = LocalContext.current
    var pendingSource by remember { mutableStateOf<String?>(null) }
    var photo by remember { mutableStateOf<Bitmap?>(null) }
    var slot by remember { mutableStateOf(Slot.TOP) }
    var garment by remember { mutableStateOf(Garment.TEE) }
    var colour by remember { mutableStateOf<Swatch?>(null) }
    var autoColour by remember { mutableStateOf(false) }
    var title by remember { mutableStateOf("") }
    var formality by remember { mutableStateOf(defaultFormality(Garment.TEE)) }
    var price by remember { mutableStateOf("") }
    val cameraFile = remember { File(File(context.cacheDir, "captures").apply { mkdirs() }, "wardrobe_capture.jpg") }
    val cameraUri = remember { FileProvider.getUriForFile(context, "${context.packageName}.files", cameraFile) }

    fun use(uri: android.net.Uri) {
        PhotoTools.load(context, uri)?.let { bmp ->
            photo = bmp
            colour = PhotoTools.dominantSwatch(bmp)
            autoColour = true
        }
        cameraFile.delete()
    }
    val takePicture = rememberLauncherForActivityResult(ActivityResultContracts.TakePicture()) { ok -> if (ok) use(cameraUri) }
    val pickPhoto = rememberLauncherForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri -> uri?.let(::use) }

    androidx.activity.compose.BackHandler(onBack = onDone)
    Column(Modifier.fillMaxSize().background(Surface).systemBarsPadding()) {
        DetailHeader("Add clothes", onDone)
        Column(Modifier.weight(1f).verticalScroll(rememberScrollState()).padding(24.dp, 16.dp, 24.dp, 16.dp)) {
            val bmp = photo
            if (bmp != null) {
                Box {
                    Image(bmp.asImageBitmap(), contentDescription = null, contentScale = ContentScale.Crop, modifier = Modifier.fillMaxWidth().aspectRatio(1.1f).clip(RoundedCornerShape(24.dp)))
                    Text(
                        "Change", color = Color.White, fontSize = 13.sp, fontWeight = FontWeight.SemiBold,
                        modifier = Modifier.align(Alignment.BottomEnd).padding(12.dp).clip(RoundedCornerShape(999.dp)).background(Color.Black.copy(alpha = 0.55f)).clickable { photo = null }.padding(14.dp, 8.dp),
                    )
                }
            } else {
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    PhotoButton(Icons.Filled.PhotoCamera, "Take photo", Modifier.weight(1f)) {
                        if (ConsentStore.has(Consent.WARDROBE_PHOTOS)) takePicture.launch(cameraUri) else pendingSource = "camera"
                    }
                    PhotoButton(Icons.Outlined.Collections, "From gallery", Modifier.weight(1f)) {
                        if (ConsentStore.has(Consent.WARDROBE_PHOTOS)) pickPhoto.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)) else pendingSource = "gallery"
                    }
                }
                Text(
                    "Lay it flat or hang it against a plain wall in good light. Photos are optional and stay inside the app.",
                    color = TextSecondary, fontSize = 12.5.sp, lineHeight = 17.sp, modifier = Modifier.padding(top = 8.dp),
                )
            }

            Spacer(Modifier.height(20.dp))
            Text("What is it?", fontSize = 15.sp, fontWeight = FontWeight.SemiBold)
            Spacer(Modifier.height(8.dp))
            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                items(Slot.entries) { s -> ChoicePill(s.label, slot == s) {
                    slot = s
                    garment = WardrobeStore.garmentsBySlot[s]!!.first()
                    formality = defaultFormality(garment)
                } }
            }
            Spacer(Modifier.height(8.dp))
            FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                WardrobeStore.garmentsBySlot[slot]!!.forEach { g ->
                    ChoicePill(garmentLabel(g), garment == g) { garment = g; formality = defaultFormality(g) }
                }
            }

            Spacer(Modifier.height(20.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("Colour", fontSize = 15.sp, fontWeight = FontWeight.SemiBold)
                if (autoColour && colour != null) Text("  · picked from your photo", color = Green, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
            }
            Spacer(Modifier.height(10.dp))
            FlowRow(horizontalArrangement = Arrangement.spacedBy(10.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                WardrobeStore.swatches.forEach { s -> Box(Modifier.clickable { colour = s; autoColour = false }) { SwatchDot(s, 32.dp, colour == s) } }
            }
            colour?.let { Text(it.name, fontSize = 13.sp, fontWeight = FontWeight.SemiBold, modifier = Modifier.padding(top = 8.dp)) }

            Spacer(Modifier.height(20.dp))
            Text("How dressy?", fontSize = 15.sp, fontWeight = FontWeight.SemiBold)
            Spacer(Modifier.height(8.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                listOf("Casual", "Smart", "Formal").forEachIndexed { i, l -> ChoicePill(l, formality == i + 1, Modifier.weight(1f)) { formality = i + 1 } }
            }

            Spacer(Modifier.height(20.dp))
            OutlinedTextField(
                value = title, onValueChange = { title = it.take(40) }, singleLine = true, label = { Text("Name (optional)") },
                placeholder = { Text("${colour?.name ?: "Navy"} ${garmentLabel(garment).lowercase()}") },
                colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = Ink, unfocusedBorderColor = TextMuted), modifier = Modifier.fillMaxWidth(),
            )
            Spacer(Modifier.height(8.dp))
            OutlinedTextField(
                value = price, onValueChange = { price = it.filter { c -> c.isDigit() || c == '.' }.take(8) }, singleLine = true, label = { Text("Price (optional, for cost per wear)") },
                keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(keyboardType = androidx.compose.ui.text.input.KeyboardType.Decimal),
                colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = Ink, unfocusedBorderColor = TextMuted), modifier = Modifier.fillMaxWidth(),
            )
        }
        Box(Modifier.padding(24.dp, 8.dp, 24.dp, 24.dp)) {
            val ready = colour != null
            Box(
                Modifier.fillMaxWidth().height(56.dp).clip(RoundedCornerShape(999.dp)).background(if (ready) Ink else Color(0xFFD8D5CE)).clickable(enabled = ready) {
                    val saved = photo?.let { PhotoVault.save(PhotoVault.Album.WARDROBE, PhotoTools.jpeg(it), tag = "item") }
                    WardrobeStore.add(garment, colour!!, title, formality, saved?.name, price.toDoubleOrNull())
                    onDone()
                },
                contentAlignment = Alignment.Center,
            ) { Text(if (ready) "Add to closet" else "Pick a colour", color = Color.White, fontSize = 15.sp, fontWeight = FontWeight.SemiBold) }
        }
    }
    pendingSource?.let { source ->
        ConsentSheet(
            Consent.WARDROBE_PHOTOS, Icons.Filled.PhotoCamera,
            onAllow = {
                pendingSource = null
                if (source == "camera") takePicture.launch(cameraUri) else pickPhoto.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
            },
            onDecline = { pendingSource = null },
        )
    }
}

@Composable
private fun PhotoButton(icon: androidx.compose.ui.graphics.vector.ImageVector, label: String, modifier: Modifier, onClick: () -> Unit) {
    Column(
        modifier.height(120.dp).clip(RoundedCornerShape(22.dp)).background(CardNeutral).border(1.5.dp, TextMuted.copy(alpha = 0.5f), RoundedCornerShape(22.dp)).clickable(onClick = onClick),
        horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center,
    ) {
        Icon(icon, contentDescription = null, tint = Ink, modifier = Modifier.size(26.dp))
        Spacer(Modifier.height(8.dp))
        Text(label, fontSize = 13.5.sp, fontWeight = FontWeight.SemiBold, textAlign = TextAlign.Center)
    }
}
