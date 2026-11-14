package com.glowup.app.ui.data

import com.glowup.app.ui.data.fitness.FitnessStore
import com.glowup.app.ui.data.glow.GlowCheckStore
import com.glowup.app.ui.data.program.ProgramStore
import com.glowup.app.ui.data.style.SavedLooksStore

/**
 * Every kind of data Re:MakeU keeps, why, and how to delete it. Drives the Your data screen, so
 * anything new that gets saved must be added here — the catalogue test checks every storage key
 * is covered.
 */
data class DataCategory(
    val id: String,
    val title: String,
    val why: String,
    val keys: Set<String>,
    val summary: () -> String,
    /** Null when the data can't be deleted on its own (it's needed for the app to work). */
    val clear: (() -> Unit)?
)

object DataCatalog {
    private fun plural(n: Int, one: String, many: String = one + "s") = "$n ${if (n == 1) one else many}"

    val categories: List<DataCategory> = listOf(
        DataCategory(
            "profile",
            "Profile",
            "Your name, age range, face shape and focus areas personalise greetings, advice and what's shown first. Your age range keeps advice age-appropriate.",
            setOf("profile"),
            { listOfNotNull(ProfileStore.name.ifBlank { null }, ProfileStore.ageBand, ProfileStore.faceShape).joinToString(" · ").ifBlank { "Not filled in" } },
            null
        ),
        DataCategory(
            "progress",
            "Points & streak",
            "Which days you were active, so we can show your streak, level and streak freezes.",
            setOf("progress"),
            { plural(ProgressStore.dayPoints.size, "active day") + " · " + plural(ProgressStore.points, "point") },
            null
        ),
        DataCategory(
            "routines",
            "Routines & check-ins",
            "Your routines, the days you completed them and their reminder times.",
            setOf("routines"),
            { plural(RoutineStore.items.size, "routine") + " · " + plural(RoutineStore.items.sumOf { it.doneDates.size }, "check-in") },
            { RoutineStore.clearAll() }
        ),
        DataCategory(
            "mood",
            "Mood journal",
            "Your daily moods, tags and notes — used for your calendar, Year in Pixels and mood patterns.",
            setOf("moods", "mood_tags"),
            { plural(MoodStore.checkInCount, "check-in") + " · " + plural(MoodStore.notes.size, "note") },
            { MoodStore.clearAll() }
        ),
        DataCategory(
            "sleep_log",
            "Sleep log",
            "The bed and wake times you log, to work out sleep debt and how regular your sleep is.",
            setOf("sleep_log"),
            { plural(SleepLogStore.entries.size, "night") },
            { SleepLogStore.clearAll() }
        ),
        DataCategory(
            "glow",
            "Glow Check results",
            "Your answers and the numbers measured during each check (face shape, a symmetry score, neck angle) — never a photo. Used to build your plan and show change over time.",
            setOf("glow_checks"),
            { plural(GlowCheckStore.results.size, "check") },
            { GlowCheckStore.clearAll() }
        ),
        DataCategory(
            "fitness",
            "Workouts & body",
            "Your fitness goal and schedule, every workout and set you log, and the weight and waist you enter — to plan sessions, adjust difficulty and show your progress.",
            setOf("fitness"),
            {
                val f = FitnessStore
                plural(f.logs.size, "workout") + " · " + plural(f.body.size, "weigh-in")
            },
            { FitnessStore.clearAll() }
        ),
        DataCategory(
            "blocker",
            "App blocker",
            "The apps you chose to block or pause, your schedules, and daily counts of blocks and pauses for the last 30 days. Never what you do inside apps.",
            setOf("blocker"),
            {
                plural(BlockStore.focusApps.size + BlockStore.pauseApps.size + BlockStore.scheduleApps.size, "app") + " · " +
                    plural(BlockStore.schedules.size, "schedule")
            },
            { BlockStore.clearAll() }
        ),
        DataCategory(
            "speech",
            "Speaking practice",
            "Your last ${SpeechStore.KEEP} practice reports — pace, filler words, pauses — and their transcripts, so you can see yourself improve. Never any audio.",
            setOf("speech"),
            { plural(SpeechStore.sessions.size, "session") },
            { SpeechStore.clearAll() }
        ),
        DataCategory(
            "focus",
            "Focus history",
            "Minutes focused each day, for your focus streak and weekly chart.",
            setOf("focus"),
            { plural(FocusStore.totalSessions, "session") + " · ${FocusStore.totalMinutes} min" },
            { FocusStore.clearHistory() }
        ),
        DataCategory(
            "todos",
            "To-dos",
            "Your to-do lists.",
            setOf("todos"),
            { plural(TodoStore.items.size, "item") },
            { TodoStore.clearAll() }
        ),
        DataCategory(
            "notes",
            "Notes & saved quotes",
            "Notes you write and quotes you like.",
            setOf("notes", "liked_quotes"),
            { plural(NotesStore.notes.size, "note") + " · " + plural(NotesStore.likedQuotes.size, "quote") },
            { NotesStore.clearAll() }
        ),
        DataCategory(
            "wardrobe",
            "Wardrobe & saved looks",
            "Clothes you've added (with their photos, if you took any), what you wore when, and looks you've saved — to build outfits from what you own.",
            setOf("wardrobe", "saved_looks"),
            { plural(WardrobeStore.items.size, "item") + " · " + plural(SavedLooksStore.ids.size, "saved look") },
            {
                WardrobeStore.clearAll()
                SavedLooksStore.clearAll()
            }
        ),
        DataCategory(
            "learning",
            "Learning progress",
            "Which programme sessions, lessons and guides you've finished, so you pick up where you left off.",
            setOf("program", "lessons_done", "grooming_done", "confidence"),
            {
                plural(ProgramStore.completedCount, "session") + " · " +
                    plural(LessonStore.completedIds.size + GroomingStore.completedIds.size, "lesson")
            },
            {
                ProgramStore.clearAll()
                LessonStore.clearAll()
                GroomingStore.clearAll()
                ConfidenceStore.clearAll()
            }
        ),
        DataCategory(
            "reminders",
            "Reminders & alarms",
            "Reminder and alarm times, so notifications arrive when you asked.",
            setOf("reminders", "sleep"),
            { plural(ReminderStore.items.count { it.enabled }, "reminder") + " on" },
            {
                ReminderStore.clearAll()
                SleepStore.resetAlarms()
            }
        ),
        DataCategory(
            "consents",
            "Permissions you've agreed to",
            "A record of which features you allowed to use the microphone or camera, and when.",
            setOf("consents"),
            { plural(ConsentStore.granted.size, "permission") },
            null
        )
    )

    /** Keys the app writes that aren't personal data (purchase cache, internal flags). */
    val systemKeys = setOf("pro")

    val coveredKeys: Set<String> get() = categories.flatMap { it.keys }.toSet() + systemKeys
}
