package com.glowup.app.ui.data

import java.time.DayOfWeek
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class BlockerTest {
    private val insta = "com.instagram.android"
    private val monday9 = LocalDateTime.of(2026, 6, 1, 9, 30) // a Monday

    @Before
    fun setUp() {
        freshStores()
    }

    @Test
    fun focusBlocksOnlyChosenAppsWhileRunningWithBlockingOn() {
        BlockStore.toggleFocusApp(insta)
        val running = ActiveFocus("Study", 25, 10_000L, null, blockApps = true)
        assertTrue(BlockStore.decide(insta, monday9, 0, running) is Intervention.Block)
        assertEquals(Intervention.None, BlockStore.decide("com.other", monday9, 0, running))
        assertEquals(Intervention.None, BlockStore.decide(insta, monday9, 0, running.copy(blockApps = false)))
        assertEquals(Intervention.None, BlockStore.decide(insta, monday9, 0, running.copy(endAtMillis = null, remainingMillis = 5)))
        assertTrue(BlockStore.needsWatcher(running))
        assertFalse(BlockStore.needsWatcher(null))
    }

    @Test
    fun schedulesHandleDaysAndMidnight() {
        val work = BlockSchedule(1, "Work", setOf(DayOfWeek.MONDAY), LocalTime.of(9, 0), LocalTime.of(17, 0))
        assertTrue(work.isActive(monday9))
        assertFalse(work.isActive(monday9.plusDays(1)))
        assertFalse(work.isActive(monday9.withHour(17)))
        val night = BlockSchedule(2, "Night", setOf(DayOfWeek.MONDAY), LocalTime.of(23, 0), LocalTime.of(7, 0))
        assertTrue(night.isActive(LocalDateTime.of(2026, 6, 1, 23, 30)))
        assertTrue("runs into Tuesday morning", night.isActive(LocalDateTime.of(2026, 6, 2, 6, 0)))
        assertFalse(night.isActive(LocalDateTime.of(2026, 6, 1, 6, 0)))

        BlockStore.toggleScheduleApp(insta)
        BlockStore.addSchedule("Work", setOf(DayOfWeek.MONDAY), LocalTime.of(9, 0), LocalTime.of(17, 0))
        val d = BlockStore.decide(insta, monday9, 0, null)
        assertTrue(d is Intervention.Block && d.reason == BlockReason.SCHEDULE)
    }

    @Test
    fun pauseRespectsGracePeriod() {
        BlockStore.togglePauseApp(insta)
        assertEquals(Intervention.Pause, BlockStore.decide(insta, monday9, 1_000, null))
        BlockStore.allow(insta, 5, nowMillis = 1_000)
        assertEquals(Intervention.None, BlockStore.decide(insta, monday9, 1_000 + 60_000, null))
        assertEquals(Intervention.Pause, BlockStore.decide(insta, monday9, 1_000 + 6 * 60_000, null))
    }

    @Test
    fun freeLimitAndStatsPersist() {
        listOf("a", "b", "c").forEach { assertTrue(BlockStore.togglePauseApp(it)) }
        assertFalse(BlockStore.togglePauseApp("d"))
        ProStore.updatePurchased(true)
        assertTrue(BlockStore.togglePauseApp("d"))
        val today = LocalDate.of(2026, 6, 1)
        BlockStore.recordPause(insta, today)
        BlockStore.recordResisted(today)
        BlockStore.recordBlock(today)
        val week = BlockStore.weekTotals(today)
        assertEquals(DayStats(1, 1, 1), week)
        assertEquals(2 * BlockStore.MINUTES_PER_RESISTED_OPEN, BlockStore.minutesSaved(week))
        Stores.loadAll()
        assertEquals(4, BlockStore.pauseApps.size)
    }

    @Test
    fun strictFocusCannotBePausedAndPersists() {
        FocusStore.start(25, "Deep work", 0, blockApps = true, strict = true)
        FocusStore.pause(1_000)
        assertTrue(FocusStore.active!!.isRunning)
        Stores.loadAll()
        assertTrue(FocusStore.active!!.strict && FocusStore.active!!.blockApps)
        // Strict needs blocking on.
        FocusStore.cancel()
        FocusStore.start(25, "Deep work", 0, blockApps = false, strict = true)
        assertFalse(FocusStore.active!!.strict)
    }
}
