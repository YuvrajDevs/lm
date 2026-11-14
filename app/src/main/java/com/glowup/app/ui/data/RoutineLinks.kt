package com.glowup.app.ui.data

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue

/** Where in the app a routine is actually done, so tapping it can take you there. */
object RoutineLinks {
    /** Checked in order; the first keyword found in the title wins. */
    private val keywords = listOf(
        "skincare" to "looks", "skin" to "looks", "groom" to "looks", "shave" to "looks", "beard" to "looks", "hair" to "looks",
        "journal" to "quotes_notes", "gratitude" to "quotes_notes", "note" to "quotes_notes",
        "eye contact" to "communication", "speak" to "communication", "talk" to "communication", "conversation" to "communication",
        "confiden" to "confidence", "posture" to "confidence",
        "outfit" to "wardrobe", "wardrobe" to "wardrobe", "style" to "styling",
        "meditat" to "morning", "breath" to "morning", "stretch" to "morning",
        "focus" to "focus", "deep work" to "focus", "study" to "focus",
        "bedtime" to "sleep", "sleep" to "sleep_log",
        "mood" to "mood_checkin",
        "learn" to "learn", "lesson" to "learn",
        "workout" to "fitness", "exercise" to "fitness", "push-up" to "fitness", "pushup" to "fitness", "gym" to "fitness", "running" to "fitness",
        "jog" to "fitness", "walk" to "fitness",
        "progress photo" to "progress_photos",
        "to-do" to "todo", "todo" to "todo", "plan tomorrow" to "todo"
    )

    private val byCategory = mapOf(
        "Looks" to "looks",
        "Fitness" to "fitness",
        "Social" to "communication",
        "Mind" to "quotes_notes",
        "Style" to "wardrobe",
        "Sleep" to "sleep"
    )

    /** The screen to open for [routine], or null when it's something you do away from the app. */
    fun routeFor(routine: RoutineEntry): String? {
        val title = routine.title.lowercase()
        // "Make your bed" is a chore, not a sleep task.
        if ("make your bed" in title || "make the bed" in title) return null
        keywords.firstOrNull { (word, _) -> word in title }?.let { return it.second }
        return byCategory[routine.category]
    }
}

/** The routine the user left Home to complete, shown as a "Mark done" bar on the page they opened. */
object RoutineSession {
    var activeId by mutableStateOf<Int?>(null)
        private set

    fun start(id: Int) {
        activeId = id
    }

    fun clear() {
        activeId = null
    }
}
