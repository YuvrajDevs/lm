package com.glowup.app.ui.data

import org.junit.Assert.assertEquals
import org.junit.Test
import java.time.DayOfWeek
import java.time.LocalDateTime
import java.time.LocalTime

class ScheduleTest {
    private fun at(day: Int, hour: Int, minute: Int = 0) = LocalDateTime.of(2026, 11, day, hour, minute)

    @Test
    fun dailyFiresLaterTodayOrTomorrow() {
        val daily = Schedule.Daily(LocalTime.of(21, 30))
        assertEquals(at(4, 21, 30), daily.nextAfter(at(4, 9)))
        assertEquals(at(5, 21, 30), daily.nextAfter(at(4, 21, 30)))
        assertEquals(at(5, 21, 30), daily.nextAfter(at(4, 23)))
    }

    @Test
    fun weeklyFiresOnTheRightDay() {
        val weekly = Schedule.Weekly(DayOfWeek.SUNDAY, LocalTime.of(10, 0))
        // 4 Nov 2026 is a Wednesday; the next Sunday is the 8th.
        assertEquals(at(8, 10), weekly.nextAfter(at(4, 12)))
        assertEquals(at(15, 10), weekly.nextAfter(at(8, 10)))
        assertEquals(at(8, 10), weekly.nextAfter(at(8, 9, 59)))
    }

    @Test
    fun intervalStaysInsideWakingHours() {
        val everyTwo = Schedule.EveryHours(2)
        assertEquals(at(4, 8), everyTwo.nextAfter(at(4, 3)))
        assertEquals(at(4, 12), everyTwo.nextAfter(at(4, 10, 0)))
        assertEquals(at(4, 12), everyTwo.nextAfter(at(4, 11, 15)))
        assertEquals(at(4, 22), everyTwo.nextAfter(at(4, 21)))
        assertEquals(at(5, 8), everyTwo.nextAfter(at(4, 22)))

        val everyThree = Schedule.EveryHours(3)
        assertEquals(at(5, 8), everyThree.nextAfter(at(4, 20, 30)))
    }

    @Test
    fun encodeDecodeRoundTrips() {
        Schedule.presets.forEach { schedule ->
            assertEquals(schedule, Schedule.decode(schedule.encode()))
        }
        assertEquals(null, Schedule.decode("nonsense|1"))
    }

    @Test
    fun clockTextRoundTrips() {
        assertEquals(LocalTime.of(21, 30), parseClock("9:30 PM"))
        assertEquals(LocalTime.MIDNIGHT, parseClock("12:00 am"))
        assertEquals("7:00 AM", formatClock(LocalTime.of(7, 0)))
    }
}
