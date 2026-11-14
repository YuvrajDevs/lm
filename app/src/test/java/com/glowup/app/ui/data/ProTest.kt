package com.glowup.app.ui.data

import com.glowup.app.ui.data.program.Program
import com.glowup.app.ui.data.style.StyleLibrary
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class ProTest {
    @Before
    fun setUp() {
        freshStores()
    }

    @Test
    fun freeUsersGetAMeaningfulSlice() {
        assertFalse(ProStore.isPro)
        assertEquals(14, Program.sessions.count { !ProGate.sessionLocked(it.index) })
        assertTrue(ProGate.sessionLocked(14))
        assertEquals(3, StyleLibrary.guides.count { !ProGate.guideLocked(it.key) })
        assertEquals(15, StyleLibrary.outfits.count { !ProGate.outfitLocked(it) })
        assertEquals(6, LessonStore.lessons.count { !ProGate.talkLessonLocked(it.id) })
        assertEquals(10, GroomingStore.lessons.count { !ProGate.groomingLessonLocked(it.id) })
        assertFalse(ProGate.soundLocked("Night Rain"))
        assertTrue(ProGate.soundLocked("Brown Noise"))
        assertFalse(ProGate.morningSessionLocked(0))
        assertTrue(ProGate.morningSessionLocked(3))
    }

    @Test
    fun everyOccasionHasAFreeLook() {
        val withoutFree = StyleLibrary.occasions.filter { occasion -> StyleLibrary.forOccasion(occasion).all { ProGate.outfitLocked(it) } }
        assertTrue("No free looks for $withoutFree", withoutFree.size <= 2)
    }

    @Test
    fun proUnlocksEverythingAndPersists() {
        ProStore.updatePurchased(true)
        assertTrue(Program.sessions.none { ProGate.sessionLocked(it.index) })
        assertTrue(StyleLibrary.outfits.none { ProGate.outfitLocked(it) })
        Stores.loadAll()
        assertTrue(ProStore.isPro)
        ProStore.updatePurchased(false)
        assertTrue(ProGate.sessionLocked(20))
    }

    @Test
    fun paywallOpensAndCloses() {
        ProStore.showPaywall("The Formal guide is part of Pro")
        assertEquals("The Formal guide is part of Pro", ProStore.paywallReason)
        ProStore.dismissPaywall()
        assertNull(ProStore.paywallReason)
    }
}
