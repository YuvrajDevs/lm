package com.glowup.app.ui.data

import com.glowup.app.ui.data.program.Program
import com.glowup.app.ui.data.program.ProgramArea
import com.glowup.app.ui.data.program.ProgramStore
import com.glowup.app.ui.data.program.SessionState
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import java.time.LocalDate
import java.time.LocalTime

class ProgramTest {
    // Monday 2 Nov 2026.
    private val monday = LocalDate.of(2026, 11, 2)

    @Before
    fun setUp() {
        freshStores()
    }

    private fun completeDays(count: Int, start: LocalDate = monday) {
        repeat(count) { day ->
            val next = ProgramStore.next!!
            assertEquals(next.points, ProgramStore.complete(next.index, start.plusDays(day.toLong())))
        }
    }

    @Test
    fun curriculumIsTwelveWeeksOfSevenAreas() {
        assertEquals(84, Program.sessions.size)
        ProgramArea.entries.forEach { area -> assertEquals(12, Program.sessions.count { it.area == area }) }
        Program.sessions.forEach { session ->
            assertEquals(ProgramArea.entries[session.index % 7], session.area)
            assertEquals(3, session.ideas.size)
            assertEquals(3, session.quiz.options.size)
            assertTrue(session.quiz.answer in session.quiz.options.indices)
            listOf(session.title, session.hook, session.drill, session.quiz.question, session.quiz.why).forEach { assertTrue(it.isNotBlank()) }
            session.ideas.forEach { assertTrue(it.heading.isNotBlank() && it.body.length > 40) }
        }
        assertEquals(84, Program.sessions.map { it.title }.toSet().size)
    }

    @Test
    fun laterWeeksAreWorthMore() {
        assertEquals(10, Program.sessions.first().points)
        assertEquals(32, Program.sessions.last().points)
        assertEquals("Foundations", Program.sessions[0].phase)
        assertEquals("Mastery", Program.sessions.last().phase)
    }

    @Test
    fun sessionsUnlockInOrderAtAnyPace() {
        assertEquals(SessionState.AVAILABLE, ProgramStore.state(0, monday))
        assertEquals(SessionState.LOCKED, ProgramStore.state(1, monday))

        assertEquals(10, ProgramStore.complete(0, monday))
        assertEquals(SessionState.DONE, ProgramStore.state(0, monday))
        assertEquals(SessionState.AVAILABLE, ProgramStore.state(1, monday))
        assertNull(ProgramStore.complete(0, monday))
        assertNull(ProgramStore.complete(5, monday))

        assertEquals(10, ProgramStore.complete(1, monday))
        assertEquals(10, ProgramStore.complete(2, monday))
        assertEquals(30, ProgressStore.points)
        assertEquals(1, ProgramStore.streakDays(monday))
    }

    @Test
    fun streakCountsConsecutiveLearningDays() {
        completeDays(3)
        assertEquals(3, ProgramStore.streakDays(monday.plusDays(2)))
        assertEquals(3, ProgramStore.streakDays(monday.plusDays(3)))
        assertEquals(0, ProgramStore.streakDays(monday.plusDays(4)))

        ProgramStore.complete(ProgramStore.next!!.index, monday.plusDays(5))
        assertEquals(1, ProgramStore.streakDays(monday.plusDays(5)))
        assertEquals(3, ProgramStore.bestStreak())
    }

    @Test
    fun areaStreaksAreMeasuredInWeeks() {
        completeDays(14)
        val secondSunday = monday.plusDays(13)
        ProgramArea.entries.forEach { area -> assertEquals(2, ProgramStore.areaStreakWeeks(area, secondSunday)) }
        assertEquals(2, ProgramStore.areaDone(ProgramArea.STYLE))
        assertEquals(7, ProgramStore.weekDone(2))
        assertEquals(0, ProgramStore.areaStreakWeeks(ProgramArea.MIND, secondSunday.plusWeeks(2)))
    }

    @Test
    fun finishingTheProgramme() {
        completeDays(84)
        assertTrue(ProgramStore.isFinished)
        assertNull(ProgramStore.next)
    }

    @Test
    fun progressAndReminderSurviveRestart() {
        completeDays(2)
        ProgramStore.setReminder(LocalTime.of(20, 0))
        Stores.loadAll()
        assertEquals(2, ProgramStore.completedCount)
        assertEquals(LocalTime.of(20, 0), ProgramStore.reminderTime)
        assertTrue(ProgramStore.reminderPrompted)
        assertEquals(SessionState.DONE, ProgramStore.state(1, monday.plusDays(1)))

        Stores.deleteAll()
        assertEquals(0, ProgramStore.completedCount)
        assertNull(ProgramStore.reminderTime)
        assertFalse(ProgramStore.reminderPrompted)
    }
}
