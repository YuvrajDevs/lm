package com.glowup.app.ui.data

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Test
import java.time.LocalDate
import java.time.ZoneOffset

class FocusTest {
    private val zone = ZoneOffset.UTC
    private val day = LocalDate.of(2026, 11, 6)
    private val nine = day.atTime(9, 0).toInstant(zone).toEpochMilli()
    private val minute = 60_000L

    @Before
    fun setUp() {
        freshStores()
    }

    @Test
    fun creditsOnlyWhenTheBlockEnds() {
        FocusStore.start(25, "Study", nine)
        assertNull(FocusStore.finishIfDue(nine + 24 * minute, zone))
        assertEquals(25, FocusStore.finishIfDue(nine + 25 * minute, zone))
        assertNull(FocusStore.active)
        assertEquals(25, FocusStore.justFinished)
        FocusStore.acknowledgeFinished()
        assertNull(FocusStore.justFinished)
        assertEquals(25, FocusStore.minutesOn(day))
        assertEquals(5, ProgressStore.points)
        assertEquals(1, FocusStore.streakDays(day))
        assertEquals(1, FocusStore.totalSessions)
    }

    @Test
    fun pauseKeepsRemainingTime() {
        FocusStore.start(25, "Study", nine)
        FocusStore.pause(nine + 10 * minute)
        assertFalse(FocusStore.active!!.isRunning)
        assertNull(FocusStore.finishIfDue(nine + 60 * minute, zone))
        assertEquals(15 * minute, FocusStore.active!!.remaining(nine + 60 * minute))

        FocusStore.resume(nine + 60 * minute)
        assertNull(FocusStore.finishIfDue(nine + 74 * minute, zone))
        assertEquals(25, FocusStore.finishIfDue(nine + 75 * minute, zone))
    }

    @Test
    fun endingEarlyEarnsNothing() {
        FocusStore.start(45, "Deep work", nine)
        FocusStore.cancel()
        assertEquals(0, FocusStore.minutesOn(day))
        assertEquals(0, ProgressStore.points)
    }

    @Test
    fun runningBlockSurvivesRestartAndWeekTotals() {
        FocusStore.start(60, "Reading", nine)
        Stores.loadAll()
        assertEquals("Reading", FocusStore.active!!.label)
        assertEquals(60, FocusStore.finishIfDue(nine + 61 * minute, zone))

        // 6 Nov 2026 is a Friday: index 4 of the Mon..Sun week.
        assertEquals(listOf(0, 0, 0, 0, 60, 0, 0), FocusStore.week(day))
    }

    @Test
    fun focusStreakSpansDays() {
        listOf(0L, 1L, 2L).forEach { offset ->
            val start = nine + offset * 24 * 60 * minute
            FocusStore.start(15, "Study", start)
            FocusStore.finishIfDue(start + 15 * minute, zone)
        }
        assertEquals(3, FocusStore.streakDays(day.plusDays(2)))
        assertEquals(45, FocusStore.totalMinutes)
    }
}
