package com.glowup.app.ui.data

import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateMapOf
import com.glowup.app.R
import org.json.JSONArray
import org.json.JSONObject
import java.time.LocalDate

/** [score] places each mood on a 1 (low) – 5 (great) scale so days can be compared. */
data class LoggedMood(val label: String, val icon: Int, val score: Int)

val moodOptions = listOf(
    LoggedMood("Happy", R.drawable.ic_mood_joy, 5),
    LoggedMood("Calm", R.drawable.ic_mood_peaceful, 4),
    LoggedMood("Grateful", R.drawable.ic_mood_grateful, 5),
    LoggedMood("Anxious", R.drawable.ic_mood_anxious, 2),
    LoggedMood("Tired", R.drawable.ic_mood_tired, 2),
    LoggedMood("Confused", R.drawable.ic_mood_confused, 3),
    LoggedMood("Sad", R.drawable.ic_mood_sad, 1),
    LoggedMood("Angry", R.drawable.ic_mood_angry, 1),
)

/** Mood log keyed by ISO date, shared by the streak detail screen and the check-in flow. */
object MoodStore {
    private const val KEY = "moods"
    private const val CUSTOM_TAGS_KEY = "mood_tags"
    const val POINTS_PER_CHECK_IN = 5
    const val MAX_CUSTOM_TAGS = 12

    val defaultTags = listOf(
        "Slept well", "Exercise", "Friends", "Family", "Date", "Work stress", "Study",
        "Outdoors", "Junk food", "Ate well", "Alone time", "Social media", "Alcohol", "Sick",
    )

    val moodLog = mutableStateMapOf<String, String>()
    val notes = mutableStateMapOf<String, String>()

    /** Tags chosen on each day. A day is present here (possibly empty) once tags were offered. */
    val tags = mutableStateMapOf<String, List<String>>()
    val customTags = mutableStateListOf<String>()

    val allTags: List<String> get() = defaultTags + customTags

    fun moodOn(date: LocalDate) = moodLog[date.toString()]

    fun noteOn(date: LocalDate) = notes[date.toString()]

    fun tagsOn(date: LocalDate): List<String> = tags[date.toString()].orEmpty()

    fun iconFor(label: String) = moodOptions.firstOrNull { it.label == label }?.icon

    fun scoreFor(label: String) = moodOptions.firstOrNull { it.label == label }?.score

    fun scoreOn(date: LocalDate) = moodOn(date)?.let(::scoreFor)

    val checkInCount: Int get() = moodLog.size

    fun logToday(label: String, note: String = "", dayTags: List<String> = emptyList(), today: LocalDate = LocalDate.now()) {
        val key = today.toString()
        val firstToday = key !in moodLog
        moodLog[key] = label
        if (note.isNotBlank()) notes[key] = note.trim() else notes.remove(key)
        tags[key] = dayTags.distinct()
        if (firstToday) ProgressStore.award(POINTS_PER_CHECK_IN, today)
        save()
    }

    /** Adds a user tag. Returns the tag as stored, or null if it's blank, a duplicate or over the limit. */
    fun addCustomTag(raw: String): String? {
        val tag = raw.trim().replace(Regex("\\s+"), " ").take(24)
        if (tag.isBlank() || allTags.any { it.equals(tag, ignoreCase = true) } || customTags.size >= MAX_CUSTOM_TAGS) return null
        customTags.add(tag)
        saveCustomTags()
        return tag
    }

    fun removeCustomTag(tag: String) {
        customTags.remove(tag)
        saveCustomTags()
    }

    fun load() {
        moodLog.clear()
        notes.clear()
        tags.clear()
        customTags.clear()
        Storage.read(CUSTOM_TAGS_KEY)?.let { customTags.addAll(JSONArray(it).strings()) }
        val json = Storage.read(KEY)?.let(::JSONObject) ?: return
        json.keys().forEach { day ->
            val entry = json.getJSONObject(day)
            moodLog[day] = entry.getString("mood")
            entry.optStringOrNull("note")?.let { notes[day] = it }
            entry.optJSONArray("tags")?.let { tags[day] = it.strings() }
        }
    }

    /** Deletes check-ins, notes and tags but keeps the points already earned. */
    fun clearAll() {
        moodLog.clear()
        notes.clear()
        tags.clear()
        customTags.clear()
        Storage.remove(KEY)
        Storage.remove(CUSTOM_TAGS_KEY)
    }

    private fun save() {
        val json = JSONObject()
        moodLog.forEach { (day, mood) ->
            val entry = JSONObject().put("mood", mood).put("note", notes[day] ?: JSONObject.NULL)
            tags[day]?.let { entry.put("tags", it.toJsonArray()) }
            json.put(day, entry)
        }
        Storage.write(KEY, json.toString())
    }

    private fun saveCustomTags() {
        Storage.write(CUSTOM_TAGS_KEY, customTags.toJsonArray().toString())
    }
}
