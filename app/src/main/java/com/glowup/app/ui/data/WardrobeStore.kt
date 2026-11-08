package com.glowup.app.ui.data

import androidx.compose.runtime.mutableStateListOf
import androidx.compose.ui.graphics.Color
import com.glowup.app.ui.data.style.Garment
import com.glowup.app.ui.data.style.Palette
import com.glowup.app.ui.data.style.Slot
import com.glowup.app.ui.data.style.Swatch
import com.glowup.app.ui.theme.BlueTint
import com.glowup.app.ui.theme.Coral
import com.glowup.app.ui.theme.CoralTint
import com.glowup.app.ui.theme.Green
import com.glowup.app.ui.theme.GreenTint
import com.glowup.app.ui.theme.Maroon
import com.glowup.app.ui.theme.MaroonTint
import com.glowup.app.ui.theme.Navy
import com.glowup.app.ui.theme.NavyTint
import org.json.JSONArray
import org.json.JSONObject
import java.time.LocalDate

/**
 * One piece of the user's own clothing. [photo] is a file name in the private photo vault.
 * [formality] is 1 casual · 2 smart casual · 3 formal.
 */
data class WardrobeItem(
    val id: Int,
    val garment: Garment,
    val colour: Swatch,
    val title: String,
    val formality: Int = defaultFormality(garment),
    val photo: String? = null,
    val price: Double? = null,
    val worn: List<LocalDate> = emptyList(),
    val addedOn: LocalDate = LocalDate.now(),
) {
    val slot: Slot get() = garment.slot
    val category: String get() = slot.label
    val meta: String get() = "${colour.name} · ${garmentLabel(garment)}"
    val tint: Color get() = WardrobeStore.styleFor(slot).first
    val accent: Color get() = WardrobeStore.styleFor(slot).second
    val wearCount: Int get() = worn.size
    val lastWorn: LocalDate? get() = worn.maxOrNull()
    val costPerWear: Double? get() = price?.let { it / wearCount.coerceAtLeast(1) }
}

fun garmentLabel(g: Garment): String = when (g) {
    Garment.TEE -> "T-shirt"
    Garment.NEHRU -> "Nehru jacket"
    Garment.BANDHGALA -> "Bandhgala"
    Garment.OVERSHIRT -> "Overshirt"
    Garment.DERBIES -> "Derby shoes"
    else -> g.name.lowercase().replaceFirstChar { it.uppercase() }
}

fun defaultFormality(g: Garment): Int = when (g) {
    Garment.TEE, Garment.HOODIE, Garment.TANK, Garment.OVERSHIRT, Garment.JEANS, Garment.SHORTS, Garment.JOGGERS,
    Garment.SNEAKERS, Garment.SANDALS, Garment.CAP -> 1
    Garment.BLAZER, Garment.NEHRU, Garment.BANDHGALA, Garment.DERBIES, Garment.TIE -> 3
    else -> 2
}

/** The user's own clothes. Starts empty — nothing here the user didn't add. */
object WardrobeStore {
    private const val KEY = "wardrobe"
    const val FREE_MAX_ITEMS = 15

    /** Garments offered when adding an item, grouped by slot. */
    val garmentsBySlot: Map<Slot, List<Garment>> = Garment.entries.groupBy { it.slot }

    val swatches: List<Swatch> = listOf(
        Palette.White, Palette.Ivory, Palette.Cream, Palette.Beige, Palette.Stone, Palette.Khaki, Palette.Camel, Palette.Tan,
        Palette.Brown, Palette.DarkBrown, Palette.LightGrey, Palette.Grey, Palette.Charcoal, Palette.Black, Palette.Navy,
        Palette.LightBlue, Palette.Sky, Palette.Denim, Palette.DarkDenim, Palette.Olive, Palette.Sage, Palette.Forest,
        Palette.Emerald, Palette.Rust, Palette.Mustard, Palette.Burgundy, Palette.Maroon, Palette.Blush, Palette.Lavender,
        Palette.Gold, Palette.Silver,
    )

    val items = mutableStateListOf<WardrobeItem>()

    fun styleFor(slot: Slot): Pair<Color, Color> = when (slot) {
        Slot.TOP -> BlueTint to Navy
        Slot.BOTTOM -> CoralTint to Coral
        Slot.SHOES -> GreenTint to Green
        Slot.LAYER -> NavyTint to Navy
        Slot.ACCESSORY -> MaroonTint to Maroon
    }

    fun find(id: Int) = items.firstOrNull { it.id == id }

    val atFreeLimit: Boolean get() = !ProStore.isPro && items.size >= FREE_MAX_ITEMS

    fun add(garment: Garment, colour: Swatch, title: String, formality: Int = defaultFormality(garment), photo: String? = null, price: Double? = null): WardrobeItem {
        val nextId = (items.maxOfOrNull { it.id } ?: 0) + 1
        val item = WardrobeItem(nextId, garment, colour, title.trim().ifBlank { "${colour.name} ${garmentLabel(garment).lowercase()}" }, formality.coerceIn(1, 3), photo, price)
        items.add(item)
        save()
        return item
    }

    fun update(item: WardrobeItem) {
        val i = items.indexOfFirst { it.id == item.id }
        if (i >= 0) items[i] = item
        save()
    }

    fun remove(id: Int) {
        val item = find(id)
        items.removeAll { it.id == id }
        save()
        item?.photo?.let { name -> PhotoVault.list(PhotoVault.Album.WARDROBE).firstOrNull { it.name == name }?.let(PhotoVault::delete) }
    }

    /** Logs that these items were worn on [date] (once per day per item). */
    fun markWorn(ids: Collection<Int>, date: LocalDate = LocalDate.now()) {
        ids.forEach { id ->
            val i = items.indexOfFirst { it.id == id }
            if (i >= 0 && date !in items[i].worn) items[i] = items[i].copy(worn = items[i].worn + date)
        }
        save()
    }

    fun unmarkWorn(id: Int, date: LocalDate) {
        val i = items.indexOfFirst { it.id == id }
        if (i >= 0) items[i] = items[i].copy(worn = items[i].worn - date)
        save()
    }

    fun wornOn(date: LocalDate) = items.filter { date in it.worn }

    fun clearAll() {
        Storage.remove(KEY)
        load()
        PhotoVault.deleteAlbum(PhotoVault.Album.WARDROBE)
    }

    fun load() {
        items.clear()
        val raw = Storage.read(KEY) ?: return
        JSONArray(raw).objects().forEach { j ->
            if (j.has("garment")) {
                val garment = runCatching { Garment.valueOf(j.getString("garment")) }.getOrNull() ?: return@forEach
                items.add(
                    WardrobeItem(
                        j.getInt("id"), garment, swatchNamed(j.getString("colour")), j.getString("title"), j.optInt("formality", defaultFormality(garment)),
                        j.optStringOrNull("photo"), if (j.isNull("price") || !j.has("price")) null else j.getDouble("price"),
                        j.optJSONArray("worn")?.strings()?.map(LocalDate::parse).orEmpty(),
                        j.optStringOrNull("added")?.let(LocalDate::parse) ?: LocalDate.now(),
                    ),
                )
            } else {
                // Before photos: free-text items with a category. Guess the garment and colour.
                val title = j.getString("title")
                val garment = legacyGarment(j.getString("category"), title)
                items.add(WardrobeItem(j.getInt("id"), garment, guessColour("$title ${j.optString("meta")}"), title))
            }
        }
    }

    private fun save() {
        val array = JSONArray()
        items.forEach {
            array.put(
                JSONObject().put("id", it.id).put("garment", it.garment.name).put("colour", it.colour.name).put("title", it.title)
                    .put("formality", it.formality).put("photo", it.photo ?: JSONObject.NULL).put("price", it.price ?: JSONObject.NULL)
                    .put("worn", it.worn.map(LocalDate::toString).toJsonArray()).put("added", it.addedOn.toString()),
            )
        }
        Storage.write(KEY, array.toString())
    }

    fun swatchNamed(name: String): Swatch = swatches.firstOrNull { it.name.equals(name, ignoreCase = true) } ?: Palette.Grey

    fun guessColour(text: String): Swatch {
        val t = text.lowercase()
        return swatches.sortedByDescending { it.name.length }.firstOrNull { it.name.lowercase() in t }
            ?: when {
                "blue" in t -> Palette.Sky
                "green" in t -> Palette.Olive
                "red" in t -> Palette.Burgundy
                "brown" in t -> Palette.Brown
                else -> Palette.Grey
            }
    }

    private fun legacyGarment(category: String, title: String): Garment {
        val t = title.lowercase()
        Garment.entries.firstOrNull { garmentLabel(it).lowercase() in t || it.name.lowercase() in t }?.let { return it }
        return when {
            "shirt" in t && "t-shirt" !in t -> Garment.SHIRT
            "sweater" in t || "jumper" in t -> Garment.KNIT
            "chino" in t -> Garment.TROUSERS
            else -> when (category) {
                "Bottom" -> Garment.TROUSERS
                "Shoes" -> Garment.SNEAKERS
                "Outerwear" -> Garment.JACKET
                "Accessory" -> Garment.WATCH
                else -> Garment.TEE
            }
        }
    }
}
