package com.glowup.app.ui.data

import java.time.LocalDate
import java.time.LocalTime
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Test

class SleepLogTest {
    private val today = LocalDate.of(2026, 5, 20)

    @Before
    fun setUp() {
        freshStores()
    }

    private fun night(daysAgo: Int, bed: String, wake: String, q: Int = 3) =
        SleepEntry(today.minusDays(daysAgo.toLong()), LocalTime.parse(bed), LocalTime.parse(wake), q)

    @Test
    fun durationCrossesMidnight() {
        assertEquals(450, night(0, "23:30", "07:00").minutes)
        assertEquals(360, night(0, "01:00", "07:00").minutes)
        assertEquals(24 * 60, night(0, "07:00", "07:00").minutes)
    }

    @Test
    fun debtNeedsThreeNightsAndWeightsTheRecentWeekFully() {
        val two = listOf(night(0, "00:00", "06:00"), night(1, "00:00", "06:00"))
        assertNull(SleepMath.debtMinutes(two, 480, today))
        // 3 recent nights 2h short = 6h; 2 older nights 2h short count half = 2h.
        val nights = two + night(2, "00:00", "06:00") + night(8, "00:00", "06:00") + night(9, "00:00", "06:00")
        assertEquals(8 * 60, SleepMath.debtMinutes(nights, 480, today))
        assertEquals(DebtLevel.HIGH, SleepMath.level(8 * 60))
    }

    @Test
    fun extraSleepPaysDebtBackButNeverBelowZero() {
        val nights = listOf(night(0, "22:00", "08:00"), night(1, "22:00", "08:00"), night(2, "00:00", "07:00"))
        assertEquals(0, SleepMath.debtMinutes(nights, 480, today))
    }

    @Test
    fun consistencyHandlesMidnightBedtimes() {
        val regular = listOf(night(0, "23:50", "07:00"), night(1, "00:10", "07:05"), night(2, "23:55", "06:55"), night(3, "00:05", "07:00"))
        assertEquals(100, SleepMath.consistency(regular))
        val messy = listOf(night(0, "21:00", "05:00"), night(1, "02:00", "10:00"), night(2, "22:00", "06:00"), night(3, "03:00", "11:00"))
        val score = SleepMath.consistency(messy)!!
        assertEquals("Irregular", SleepMath.consistencyLabel(score))
    }

    @Test
    fun loggingAwardsPointsOncePerNightAndPersists() {
        SleepLogStore.log(night(0, "23:00", "07:00", 4))
        SleepLogStore.log(night(0, "23:30", "07:00", 9))
        assertEquals(SleepLogStore.POINTS_PER_LOG, ProgressStore.points)
        assertEquals(5, SleepLogStore.on(today)!!.quality)
        SleepLogStore.updateNeed(450)
        Stores.loadAll()
        assertEquals(LocalTime.of(23, 30), SleepLogStore.on(today)!!.bed)
        assertEquals(450, SleepLogStore.needMinutes)
        SleepLogStore.remove(today)
        assertNull(SleepLogStore.on(today))
    }

    @Test
    fun formatsDurations() {
        assertEquals("7h 30m", formatDuration(450))
        assertEquals("45m", formatDuration(45))
        assertEquals("8h", formatDuration(480))
    }
}
