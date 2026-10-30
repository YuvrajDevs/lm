package com.glowup.app.ui.data

import androidx.compose.runtime.mutableStateMapOf
import com.glowup.app.R
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

/** Per-day mood log, keyed by day-of-month for the current month, shared by the streak detail screen and the check-in flow. */
object MoodStore {
    var streakDays = 7
    val points = 128
    val level = 3

    val moodLog = mutableStateMapOf<Int, String>().apply {
        val today = LocalDate.now().dayOfMonth
        val pattern = listOf("Happy", "Calm", "Grateful", "Tired", "Happy", "Calm")
        pattern.forEachIndexed { index, label ->
            val day = today - pattern.size + index
            if (day in 1..31) put(day, label)
        }
    }

    fun iconFor(label: String) = moodOptions.firstOrNull { it.label == label }?.icon

    fun logToday(label: String) {
        val today = LocalDate.now().dayOfMonth
        val alreadyLogged = moodLog.containsKey(today)
        moodLog[today] = label
        if (!alreadyLogged) streakDays += 1
    }
}
