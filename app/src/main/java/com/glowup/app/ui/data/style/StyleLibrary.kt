package com.glowup.app.ui.data.style

import androidx.compose.runtime.mutableStateListOf
import com.glowup.app.ui.data.Storage
import com.glowup.app.ui.data.strings
import com.glowup.app.ui.data.toJsonArray
import org.json.JSONArray
import java.time.LocalDate

object StyleLibrary {
    val guides: List<StyleGuide> = listOf(oldMoney, smartCasual, minimal, streetwear, athleisure, formal, festive, jewellery)

    val outfits: List<Outfit> by lazy { guides.flatMap { it.outfits } }

    val occasions = listOf("Office", "College", "Date", "Weekend", "Wedding", "Interview", "Party", "Festive", "Travel", "Gym")

    fun guide(key: String) = guides.firstOrNull { it.key == key }

    fun outfit(id: String) = outfits.firstOrNull { it.id == id }

    fun forOccasion(occasion: String) = outfits.filter { occasion in it.occasions }

    /** A stable pick per day and occasion, so the "today" card doesn't change on every visit. */
    fun pickOfTheDay(occasion: String, today: LocalDate = LocalDate.now()): Int {
        val options = forOccasion(occasion)
        return if (options.isEmpty()) 0 else (today.dayOfYear + occasion.hashCode().mod(options.size)) % options.size
    }
}

object SavedLooksStore {
    private const val KEY = "saved_looks"

    val ids = mutableStateListOf<String>()

    fun isSaved(id: String) = id in ids

    fun toggle(id: String) {
        if (id in ids) ids.remove(id) else ids.add(0, id)
        Storage.write(KEY, ids.toJsonArray().toString())
    }

    fun clearAll() {
        Storage.remove(KEY)
        load()
    }

    fun load() {
        ids.clear()
        Storage.read(KEY)?.let { raw -> ids.addAll(JSONArray(raw).strings().filter { StyleLibrary.outfit(it) != null }) }
    }
}
