package com.glowup.app.ui.data

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import java.time.LocalDate

class MoodInsightsTest {
    private val start = LocalDate.of(2026, 4, 1)

    @Before
    fun setUp() {
        freshStores()
    }

    private fun day(i: Int) = start.plusDays(i.toLong())

    @Test
    fun needsFiveDaysOnEachSide() {
        val scores = (0 until 9).associate { day(it) to if (it < 5) 5 else 2 }
        val factor = MoodFactor("x", "x", (0 until 5).mapTo(HashSet(), ::day), scores.keys, false)
        assertTrue(MoodInsights.compute(scores, listOf(factor)).isEmpty())
        val more = scores + (day(9) to 2)
        val result = MoodInsights.compute(more, listOf(factor.copy(observed = more.keys)))
        assertEquals(1, result.size)
        assertEquals(5.0, result[0].withAvg, 0.001)
        assertEquals(2.0, result[0].withoutAvg, 0.001)
        assertTrue(result[0].lifts)
    }

    @Test
    fun smallDifferencesAreIgnoredAndStrongestComesFirst() {
        val scores = (0 until 10).associate { day(it) to if (it % 2 == 0) 4 else 3 }
        val all = scores.keys
        val evens = (0 until 10 step 2).mapTo(HashSet(), ::day)
        val firstHalf = (0 until 5).mapTo(HashSet(), ::day)
        val strong = MoodFactor("strong", "strong", evens, all, false)
        val weak = MoodFactor("weak", "weak", firstHalf, all, false)
        val result = MoodInsights.compute(scores, listOf(weak, strong))
        assertEquals(listOf("strong"), result.map { it.factorId })
    }

    @Test
    fun onlyObservedDaysCount() {
        // Sleep is only known on logged nights: unlogged days must not count as "didn't sleep 7h".
        val scores = (0 until 20).associate { day(it) to 3 }
        val observed = (0 until 8).mapTo(HashSet(), ::day)
        val factor = MoodFactor("s", "s", (0 until 4).mapTo(HashSet(), ::day), observed, false)
        assertTrue(MoodInsights.compute(scores, listOf(factor)).isEmpty())
    }

    @Test
    fun tagsFromCheckInsBecomeInsights() {
        repeat(10) { i ->
            val tags = if (i < 5) listOf("Exercise") else listOf("Work stress")
            MoodStore.logToday(if (i < 5) "Happy" else "Tired", dayTags = tags, today = day(i))
        }
        val insights = MoodInsights.current()
        val exercise = insights.first { it.factorId == "tag:Exercise" }
        assertEquals(5.0, exercise.withAvg, 0.001)
        assertEquals(2.0, exercise.withoutAvg, 0.001)
        assertTrue(insights.any { it.factorId == "tag:Work stress" && !it.lifts })
        assertEquals(listOf("Exercise" to 5), MoodInsights.bestDayTags())
    }

    @Test
    fun customTagsAreValidatedAndPersist() {
        assertEquals("Gaming", MoodStore.addCustomTag("  Gaming "))
        assertEquals(null, MoodStore.addCustomTag("gaming"))
        assertEquals(null, MoodStore.addCustomTag("exercise"))
        assertEquals(null, MoodStore.addCustomTag("   "))
        MoodStore.logToday("Calm", "ok", listOf("Gaming"), day(0))
        Stores.loadAll()
        assertEquals(listOf("Gaming"), MoodStore.customTags.toList())
        assertEquals(listOf("Gaming"), MoodStore.tagsOn(day(0)))
        assertEquals("ok", MoodStore.noteOn(day(0)))
    }

    @Test
    fun checkInsUntilInsightsCountsDown() {
        assertEquals(10, MoodInsights.checkInsUntilInsights())
        repeat(4) { MoodStore.logToday("Calm", today = day(it)) }
        assertEquals(6, MoodInsights.checkInsUntilInsights())
    }
}
