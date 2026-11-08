package com.glowup.app.ui.data

import androidx.compose.runtime.mutableStateListOf
import org.json.JSONArray
import org.json.JSONObject
import java.time.LocalDate

data class UserNote(val id: Int, val body: String, val date: LocalDate, val pinned: Boolean = false)

/** The user's own notes and the quotes they've saved. */
object NotesStore {
    private const val NOTES_KEY = "notes"
    private const val LIKED_KEY = "liked_quotes"

    val notes = mutableStateListOf<UserNote>()
    val likedQuotes = mutableStateListOf<String>()

    fun add(body: String, today: LocalDate = LocalDate.now()) {
        val nextId = (notes.maxOfOrNull { it.id } ?: 0) + 1
        notes.add(0, UserNote(nextId, body.trim(), today))
        saveNotes()
    }

    fun remove(id: Int) {
        notes.removeAll { it.id == id }
        saveNotes()
    }

    fun togglePinned(id: Int) {
        val index = notes.indexOfFirst { it.id == id }
        if (index < 0) return
        notes[index] = notes[index].copy(pinned = !notes[index].pinned)
        saveNotes()
    }

    /** Pinned first, then newest first. */
    fun ordered(): List<UserNote> = notes.sortedWith(compareByDescending<UserNote> { it.pinned }.thenByDescending { it.date }.thenByDescending { it.id })

    fun toggleLiked(quote: String) {
        if (quote in likedQuotes) likedQuotes.remove(quote) else likedQuotes.add(0, quote)
        Storage.write(LIKED_KEY, likedQuotes.toJsonArray().toString())
    }

    fun clearAll() {
        Storage.remove(NOTES_KEY)
        Storage.remove(LIKED_KEY)
        load()
    }

    fun load() {
        notes.clear()
        likedQuotes.clear()
        Storage.read(NOTES_KEY)?.let { raw ->
            JSONArray(raw).objects().forEach {
                notes.add(UserNote(it.getInt("id"), it.getString("body"), LocalDate.parse(it.getString("date")), it.optBoolean("pinned")))
            }
        }
        Storage.read(LIKED_KEY)?.let { likedQuotes.addAll(JSONArray(it).strings()) }
    }

    private fun saveNotes() {
        val array = JSONArray()
        notes.forEach {
            array.put(JSONObject().put("id", it.id).put("body", it.body).put("date", it.date.toString()).put("pinned", it.pinned))
        }
        Storage.write(NOTES_KEY, array.toString())
    }
}
