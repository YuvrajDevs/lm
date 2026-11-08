package com.glowup.app.ui.data

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import java.time.LocalDate

class ProgressTest {
    private val today = LocalDate.of(2026, 11, 6)

    @Before
    fun setUp() {
        freshStores()
    }

    @Test
    fun streakCountsConsecutiveActiveDays() {
        val days = setOf(today, today.minusDays(1), today.minusDays(2), today.minusDays(4))
        assertEquals(3, ProgressStore.streakFrom(days, today))
    }

    @Test
    fun streakSurvivesUntilTodayIsLogged() {
        val days = setOf(today.minusDays(1), today.minusDays(2))
        assertEquals(2, ProgressStore.streakFrom(days, today))
        assertEquals(0, ProgressStore.streakFrom(setOf(today.minusDays(2)), today))
    }

    @Test
    fun awardAndRevokeTrackPointsAndActiveDays() {
        ProgressStore.award(10, today)
        ProgressStore.award(5, today.minusDays(1))
        assertEquals(15, ProgressStore.points)
        assertEquals(2, ProgressStore.streakDays(today))

        ProgressStore.revoke(10, today)
        assertFalse(ProgressStore.isActive(today))
        assertEquals(1, ProgressStore.streakDays(today))
        assertEquals(5, ProgressStore.points)
    }

    @Test
    fun levelsEveryHundredPoints() {
        assertEquals(1, ProgressStore.levelFor(0))
        assertEquals(1, ProgressStore.levelFor(99))
        assertEquals(2, ProgressStore.levelFor(100))
        assertEquals(4, ProgressStore.levelFor(350))
    }

    @Test
    fun lessonsAwardTheirPointsOnlyOnce() {
        val lesson = LessonStore.lessons.first()
        LessonStore.markDone(lesson.id)
        LessonStore.markDone(lesson.id)
        assertTrue(LessonStore.isDone(lesson.id))
        assertEquals(lesson.points, ProgressStore.points)
    }

    @Test
    fun moodCheckInAwardsOnFirstLogOfTheDay() {
        MoodStore.logToday("Calm", "slept well", today = today)
        MoodStore.logToday("Happy", "", today = today)
        assertEquals("Happy", MoodStore.moodOn(today))
        assertEquals(null, MoodStore.noteOn(today))
        assertEquals(MoodStore.POINTS_PER_CHECK_IN, ProgressStore.points)
    }

    @Test
    fun todoToggleAwardsAndRevokes() {
        val id = TodoStore.items.first().id
        TodoStore.toggle(id)
        assertEquals(TodoStore.POINTS_PER_TODO, ProgressStore.points)
        TodoStore.toggle(id)
        assertEquals(0, ProgressStore.points)
    }
}
