package com.glowup.app.ui.data

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import java.time.LocalDate

class StreakFreezeTest {
    private val start = LocalDate.of(2026, 3, 1)

    @Before
    fun setUp() {
        freshStores()
    }

    private fun activeFor(days: Int, from: LocalDate = start) {
        repeat(days) { ProgressStore.award(5, from.plusDays(it.toLong())) }
    }

    @Test
    fun earnsOneFreezePerSevenDaysUpToTheLimit() {
        activeFor(6)
        assertEquals(0, ProgressStore.freezes)
        assertEquals(1, ProgressStore.daysToNextFreeze(start.plusDays(5)))
        activeFor(1, start.plusDays(6))
        assertEquals(1, ProgressStore.freezes)
        // Extra points on the same day don't earn another.
        ProgressStore.award(5, start.plusDays(6))
        assertEquals(1, ProgressStore.freezes)
        activeFor(21, start.plusDays(7))
        assertEquals(ProgressStore.FREE_MAX_FREEZES, ProgressStore.freezes)
        assertNull(ProgressStore.daysToNextFreeze(start.plusDays(27)))
    }

    @Test
    fun freezeCoversAMissedDayAndKeepsTheStreak() {
        activeFor(7) // Mar 1–7 → 1 freeze
        val missed = start.plusDays(7) // Mar 8 missed
        val today = start.plusDays(8)
        ProgressStore.applyFreezes(today)
        assertEquals(0, ProgressStore.freezes)
        assertTrue(ProgressStore.isFrozen(missed))
        assertEquals(FreezeNotice(1, 7), ProgressStore.freezeNotice)
        // Streak still 7 this morning, and grows when today is done.
        assertEquals(7, ProgressStore.streakDays(today))
        ProgressStore.award(5, today)
        assertEquals(8, ProgressStore.streakDays(today))
    }

    @Test
    fun applyingTwiceDoesNothingMore() {
        activeFor(14) // 2 freezes
        val today = start.plusDays(15)
        ProgressStore.applyFreezes(today)
        ProgressStore.dismissFreezeNotice()
        ProgressStore.applyFreezes(today)
        assertEquals(1, ProgressStore.freezes)
        assertNull(ProgressStore.freezeNotice)
    }

    @Test
    fun gapLongerThanBankedFreezesEndsTheStreakAndKeepsFreezes() {
        activeFor(7) // 1 freeze
        val today = start.plusDays(9) // Mar 8 and 9 missed → 2 days, only 1 freeze
        ProgressStore.applyFreezes(today)
        assertEquals(1, ProgressStore.freezes)
        assertEquals(0, ProgressStore.streakDays(today))
        assertNull(ProgressStore.freezeNotice)
    }

    @Test
    fun todayIsNeverFrozen() {
        activeFor(7)
        ProgressStore.applyFreezes(start.plusDays(7)) // today = Mar 8, nothing missed yet
        assertEquals(1, ProgressStore.freezes)
        assertTrue(ProgressStore.frozenDays.isEmpty())
    }

    @Test
    fun freezesSurviveRestartAndOldFormatStillLoads() {
        activeFor(7)
        ProgressStore.applyFreezes(start.plusDays(8))
        Stores.loadAll()
        assertTrue(ProgressStore.isFrozen(start.plusDays(7)))
        assertEquals(7, ProgressStore.streakDays(start.plusDays(8)))

        Storage.write("progress", """{"2026-03-01":10,"2026-03-02":5}""")
        ProgressStore.load()
        assertEquals(15, ProgressStore.points)
        assertEquals(0, ProgressStore.freezes)
    }

    @Test
    fun proBanksMoreFreezes() {
        ProStore.updatePurchased(true)
        activeFor(28)
        assertEquals(ProgressStore.PRO_MAX_FREEZES, ProgressStore.freezes)
        ProStore.updatePurchased(false)
        assertNotNull(ProgressStore.freezes)
    }
}
