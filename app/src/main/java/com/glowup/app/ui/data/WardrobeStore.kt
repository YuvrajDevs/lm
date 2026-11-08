package com.glowup.app.ui.data

import androidx.compose.runtime.mutableStateListOf
import androidx.compose.ui.graphics.Color
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

data class WardrobeItem(val id: Int, val category: String, val title: String, val meta: String) {
    val tint: Color get() = WardrobeStore.styleFor(category).first
    val accent: Color get() = WardrobeStore.styleFor(category).second
}

/** The user's own clothes. Starts empty — nothing here the user didn't add. */
object WardrobeStore {
    private const val KEY = "wardrobe"

    val categories = listOf("Top", "Bottom", "Shoes", "Outerwear", "Accessory")

    val items = mutableStateListOf<WardrobeItem>()

    fun styleFor(category: String): Pair<Color, Color> = when (category) {
        "Top" -> BlueTint to Navy
        "Bottom" -> CoralTint to Coral
        "Shoes" -> GreenTint to Green
        "Outerwear" -> NavyTint to Navy
        else -> MaroonTint to Maroon
    }

    fun add(category: String, title: String, meta: String) {
        val nextId = (items.maxOfOrNull { it.id } ?: 0) + 1
        items.add(WardrobeItem(nextId, category, title.trim(), meta.trim()))
        save()
    }

    fun remove(id: Int) {
        items.removeAll { it.id == id }
        save()
    }

    fun clearAll() {
        Storage.remove(KEY)
        load()
    }

    fun load() {
        items.clear()
        Storage.read(KEY)?.let { raw ->
            JSONArray(raw).objects().forEach {
                items.add(WardrobeItem(it.getInt("id"), it.getString("category"), it.getString("title"), it.getString("meta")))
            }
        }
    }

    private fun save() {
        val array = JSONArray()
        items.forEach {
            array.put(JSONObject().put("id", it.id).put("category", it.category).put("title", it.title).put("meta", it.meta))
        }
        Storage.write(KEY, array.toString())
    }
}
