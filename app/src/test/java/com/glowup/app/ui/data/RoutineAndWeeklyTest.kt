package com.glowup.app.ui.data

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import java.time.LocalDate

class RoutineAndWeeklyTest {
    // 6 Nov 2026 is a Friday.
    private val friday = LocalDate.of(2026, 11, 6)

    @Before
    fun setUp() {
        freshStores()
    }

    @Test
    fun seedRoutinesStartWithNoHistory() {
        assertTrue(RoutineStore.items.isNotEmpty())
        assertTrue(RoutineStore.items.all { it.doneDates.isEmpty() && it.streakDays(friday) == 0 })
    }

    @Test
    fun checkingInBuildsStreakWeekAndPoints() {
        val id = RoutineStore.items.first().id
        RoutineStore.toggleToday(id, friday.minusDays(1))
        RoutineStore.toggleToday(id, friday)
        val routine = RoutineStore.find(id)!!

        assertEquals(2, routine.streakDays(friday))
        assertEquals(listOf(false, false, false, true, true, false, false), routine.week(friday))
        assertEquals(2 * RoutineStore.POINTS_PER_CHECK_IN, ProgressStore.points)

        RoutineStore.toggleToday(id, friday)
        assertFalse(RoutineStore.find(id)!!.isDoneOn(friday))
        assertEquals(RoutineStore.POINTS_PER_CHECK_IN, ProgressStore.points)
    }

    @Test
    fun duplicateRoutineTitlesAreRejected() {
        assertTrue(RoutineStore.add("Fitness", "20 push-ups"))
        assertFalse(RoutineStore.add("Fitness", "20 Push-Ups"))
    }

    @Test
    fun confidenceCardsResetEachWeek() {
        ConfidenceStore.toggle("Start one new conversation", 9, friday)
        assertTrue(ConfidenceStore.isDone("Start one new conversation", friday))
        assertFalse(ConfidenceStore.isDone("Start one new conversation", friday.plusDays(3)))

        ConfidenceStore.toggle("Make eye contact today", 8, friday.plusDays(3))
        assertFalse(ConfidenceStore.doneTitles.contains("Start one new conversation"))
        assertEquals(17, ProgressStore.points)
    }
}
