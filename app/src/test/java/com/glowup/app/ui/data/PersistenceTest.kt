package com.glowup.app.ui.data

import java.time.DayOfWeek
import java.time.LocalDate
import java.time.LocalTime
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

/** Every store must come back exactly as it was after a simulated app restart. */
class PersistenceTest {
    private val today = LocalDate.now()

    @Before
    fun setUp() {
        freshStores()
    }

    private fun restart() = Stores.loadAll()

    @Test
    fun profileSurvivesRestart() {
        ProfileStore.completeOnboarding("  Sam Rivera ", listOf("Looks", "Sleep"), "Round")
        restart()
        assertTrue(ProfileStore.onboarded)
        assertEquals("Sam Rivera", ProfileStore.name)
        assertEquals("Sam", ProfileStore.firstName)
        assertEquals(listOf("Looks", "Sleep"), ProfileStore.focusAreas.toList())
        assertEquals("Round", ProfileStore.faceShape)
    }

    @Test
    fun routinesTodosAndProgressSurviveRestart() {
        RoutineStore.add("Fitness", "Morning walk", reminderTime = LocalTime.of(7, 0))
        val walk = RoutineStore.items.first { it.title == "Morning walk" }
        RoutineStore.toggleToday(walk.id, today)
        TodoStore.add("Buy SPF", TodoBucket.WEEK, "")
        TodoStore.toggle(TodoStore.items.first { it.title == "Buy SPF" }.id)
        val pointsBefore = ProgressStore.points

        restart()

        val restored = RoutineStore.items.first { it.title == "Morning walk" }
        assertEquals("Fitness", restored.category)
        assertEquals(LocalTime.of(7, 0), restored.reminderTime)
        assertTrue(restored.isDoneOn(today))
        val todo = TodoStore.items.first { it.title == "Buy SPF" }
        assertTrue(todo.done)
        assertEquals(TodoBucket.WEEK, todo.bucket)
        assertEquals(pointsBefore, ProgressStore.points)
        assertEquals(1, ProgressStore.streakDays(today))
    }

    @Test
    fun deletionsSurviveRestart() {
        val removed = RoutineStore.items.first().id
        RoutineStore.remove(removed)
        TodoStore.clearDone()
        restart()
        assertEquals(null, RoutineStore.find(removed))
    }

    @Test
    fun userContentSurvivesRestart() {
        WardrobeStore.add(com.glowup.app.ui.data.style.Garment.JACKET, com.glowup.app.ui.data.style.Palette.Denim, "Denim jacket")
        NotesStore.add("Speak first in Monday's meeting")
        NotesStore.togglePinned(NotesStore.notes.first().id)
        NotesStore.toggleLiked(quoteLibrary.first().text)
        MoodStore.logToday("Grateful", "Good day", today = today)
        GroomingStore.markDone(2)

        restart()

        assertEquals("Denim jacket", WardrobeStore.items.single().title)
        assertTrue(NotesStore.notes.single().pinned)
        assertEquals(listOf(quoteLibrary.first().text), NotesStore.likedQuotes.toList())
        assertEquals("Grateful", MoodStore.moodOn(today))
        assertEquals("Good day", MoodStore.noteOn(today))
        assertTrue(GroomingStore.isDone(2))
    }

    @Test
    fun remindersAndSleepSettingsSurviveRestart() {
        ReminderStore.add("Stretch", Schedule.Weekly(DayOfWeek.SATURDAY, LocalTime.of(11, 0)))
        val builtIn = ReminderStore.builtIn.first()
        ReminderStore.setEnabled(builtIn.id, true)
        SleepStore.updateBedtime(LocalTime.of(22, 30))
        SleepStore.updateWakeTime(null)
        SleepStore.selectSound("Ocean Waves")
        SleepStore.selectTimer(60)

        restart()

        val stretch = ReminderStore.custom.single()
        assertEquals(Schedule.Weekly(DayOfWeek.SATURDAY, LocalTime.of(11, 0)), stretch.schedule)
        assertTrue(ReminderStore.builtIn.first { it.id == builtIn.id }.enabled)
        assertEquals(LocalTime.of(22, 30), SleepStore.bedtime)
        assertTrue(SleepStore.bedtimeAlarmOn)
        assertFalse(SleepStore.wakeAlarmOn)
        assertEquals("Ocean Waves", SleepStore.sound)
        assertEquals(60, SleepStore.timerMinutes)
    }

    @Test
    fun deleteAllReturnsToFirstLaunch() {
        ProfileStore.completeOnboarding("Sam", listOf("Looks"), null)
        WardrobeStore.add(com.glowup.app.ui.data.style.Garment.TEE, com.glowup.app.ui.data.style.Palette.White, "Tee")
        ProgressStore.award(40)
        SleepStore.updateWakeTime(LocalTime.of(6, 30))
        SleepStore.selectSound("Deep Space")

        Stores.deleteAll()

        assertFalse(ProfileStore.onboarded)
        assertTrue(WardrobeStore.items.isEmpty())
        assertEquals(0, ProgressStore.points)
        assertTrue(RoutineStore.items.isNotEmpty())
        assertFalse(SleepStore.wakeAlarmOn)
        assertEquals("Night Rain", SleepStore.sound)
    }

    @Test
    fun freshInstallSchedulesNothing() {
        assertTrue(RoutineStore.items.all { it.reminderTime == null })
        assertTrue(ReminderStore.items.none { it.enabled })
        assertFalse(SleepStore.bedtimeAlarmOn || SleepStore.wakeAlarmOn)
    }

    @Test
    fun exportContainsEveryStore() {
        ProfileStore.completeOnboarding("Sam", listOf("Looks"), null)
        val export = org.json.JSONObject(Storage.exportJson())
        listOf("profile", "routines", "todos", "reminders").forEach { assertTrue("missing $it", export.has(it)) }
    }
}
