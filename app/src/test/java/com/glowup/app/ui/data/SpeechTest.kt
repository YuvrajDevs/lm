package com.glowup.app.ui.data

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import java.time.LocalDate

class SpeechTest {
    @Before
    fun setUp() {
        freshStores()
    }

    @Test
    fun countsRealFillersNotNormalWords() {
        val f = SpeechAnalysis.fillers("Um so I like pizza and, like, you know, it's basically uh the best. I mean really.")
        val map = f.associate { it.word to it.count }
        assertEquals(1, map["um"])
        assertEquals(1, map["uh"])
        assertEquals(1, map["like"]) // "I like pizza" is a real verb
        assertEquals(1, map["you know"])
        assertEquals(1, map["basically"])
        assertEquals(1, map["i mean"])
        assertEquals(1, map["so"]) // sentence-start "so"
        assertEquals(2, SpeechAnalysis.repeats("I I think the the plan works"))
    }

    @Test
    fun paceAndPausesFromLevels() {
        // 60 s, 140 words, speaking the whole time except one 3-second pause.
        val words = List(140) { "word" }.joinToString(" ")
        val levels = (0 until 600).map { i -> LevelSample(i * 100L, if (i in 300..330) 0f else 8f + (i % 7)) }
        val r = SpeechAnalysis.analyse(words, 60, levels)
        assertEquals(1, r.longPauses)
        assertTrue(r.longestPauseSeconds >= 3.0)
        assertTrue("wpm ${r.wpm}", r.wpm in 135..150)
        assertTrue(r.variety > 0)
        assertTrue(r.wentWell().any { it.title == "Great pace" })
    }

    @Test
    fun feedbackFlagsProblems() {
        val text = List(60) { "um like basically" }.joinToString(" ")
        val r = SpeechAnalysis.analyse(text, 60, emptyList())
        assertTrue(r.fillersPerMinute > 3)
        assertTrue(r.workOn().any { it.title.startsWith("Swap fillers") })
        assertTrue(r.workOn().size <= 2)
    }

    @Test
    fun historyKeepsLatestAndPersists() {
        val r = SpeechAnalysis.analyse("hello there friend", 20, emptyList())
        repeat(SpeechStore.KEEP + 3) { SpeechStore.save("Topic $it", r, "hello", LocalDate.of(2026, 1, 1), it.toLong()) }
        assertEquals(SpeechStore.KEEP, SpeechStore.sessions.size)
        assertEquals("Topic ${SpeechStore.KEEP + 2}", SpeechStore.sessions.first().topic)
        Stores.loadAll()
        assertEquals(SpeechStore.KEEP, SpeechStore.sessions.size)
        assertEquals(r.wpm, SpeechStore.sessions.first().report.wpm)
    }

    @Test
    fun scenariosAreCompleteWithFreeOnesPerCategory() {
        assertTrue(Scenarios.all.size >= 30)
        assertEquals(Scenarios.all.size, Scenarios.all.map { it.id }.toSet().size)
        Scenarios.categories.forEach { c -> assertTrue(Scenarios.all.filter { it.category == c }.any(Scenarios::isFree)) }
    }
}
