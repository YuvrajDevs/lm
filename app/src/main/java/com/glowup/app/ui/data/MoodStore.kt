package com.glowup.app.ui.data

import androidx.compose.runtime.mutableStateMapOf
import com.glowup.app.R
import org.json.JSONObject
import java.time.LocalDate

data class LoggedMood(val label: String, val icon: Int)

val moodOptions = listOf(
    LoggedMood("Happy", R.drawable.ic_mood_joy),
    LoggedMood("Calm", R.drawable.ic_mood_peaceful),
    LoggedMood("Grateful", R.drawable.ic_mood_grateful),
    LoggedMood("Anxious", R.drawable.ic_mood_anxious),
    LoggedMood("Tired", R.drawable.ic_mood_tired),
    LoggedMood("Confused", R.drawable.ic_mood_confused),
    LoggedMood("Sad", R.drawable.ic_mood_sad),
    LoggedMood("Angry", R.drawable.ic_mood_angry),
)

/** Mood log keyed by ISO date, shared by the streak detail screen and the check-in flow. */
object MoodStore {
    private const val KEY = "moods"
    const val POINTS_PER_CHECK_IN = 5

    val moodLog = mutableStateMapOf<String, String>()
    val notes = mutableStateMapOf<String, String>()

    fun moodOn(date: LocalDate) = moodLog[date.toString()]

    fun noteOn(date: LocalDate) = notes[date.toString()]

    fun iconFor(label: String) = moodOptions.firstOrNull { it.label == label }?.icon

    fun logToday(label: String, note: String = "", today: LocalDate = LocalDate.now()) {
        val key = today.toString()
        val firstToday = key !in moodLog
        moodLog[key] = label
        if (note.isNotBlank()) notes[key] = note.trim() else notes.remove(key)
        if (firstToday) ProgressStore.award(POINTS_PER_CHECK_IN, today)
        save()
    }

    fun load() {
        moodLog.clear()
        notes.clear()
        val json = Storage.read(KEY)?.let(::JSONObject) ?: return
        json.keys().forEach { day ->
            val entry = json.getJSONObject(day)
            moodLog[day] = entry.getString("mood")
            entry.optStringOrNull("note")?.let { notes[day] = it }
        }
    }

    private fun save() {
        val json = JSONObject()
        moodLog.forEach { (day, mood) ->
            json.put(day, JSONObject().put("mood", mood).put("note", notes[day] ?: JSONObject.NULL))
        }
        Storage.write(KEY, json.toString())
    }
}
