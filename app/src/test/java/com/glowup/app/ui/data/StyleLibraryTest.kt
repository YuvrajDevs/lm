package com.glowup.app.ui.data

import com.glowup.app.ui.data.style.Garment
import com.glowup.app.ui.data.style.SavedLooksStore
import com.glowup.app.ui.data.style.Slot
import com.glowup.app.ui.data.style.StyleLibrary
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import java.time.LocalDate

class StyleLibraryTest {
    @Before
    fun setUp() {
        freshStores()
    }

    @Test
    fun everyOutfitIsAWearableLook() {
        assertEquals(8, StyleLibrary.guides.size)
        assertEquals(40, StyleLibrary.outfits.size)
        assertEquals(StyleLibrary.outfits.size, StyleLibrary.outfits.map { it.id }.toSet().size)
        StyleLibrary.outfits.forEach { outfit ->
            assertTrue("${outfit.id} has no top", outfit.top != null)
            assertTrue("${outfit.id} has no bottom", outfit.bottom != null)
            assertTrue("${outfit.id} has no shoes", outfit.shoes != null)
            assertTrue("${outfit.id} has two tops", outfit.pieces.count { it.garment.slot == Slot.TOP } == 1)
            assertTrue("${outfit.id} has a tie with no collar", outfit.accessories.none { it.garment == Garment.TIE } || outfit.top?.garment == Garment.SHIRT)
            assertTrue(outfit.occasions.all { it in StyleLibrary.occasions })
            listOf(outfit.why, outfit.dressUp, outfit.dressDown).forEach { assertTrue(it.isNotBlank()) }
        }
    }

    @Test
    fun everyGuideIsComplete() {
        StyleLibrary.guides.forEach { guide ->
            assertEquals(5, guide.outfits.size)
            assertEquals(5, guide.palette.size)
            assertEquals(3, guide.pairings.size)
            assertEquals(6, guide.keyPieces.size)
            assertTrue(guide.outfits.all { it.styleKey == guide.key })
        }
    }

    @Test
    fun everyOccasionHasChoices() {
        StyleLibrary.occasions.forEach { occasion ->
            assertTrue("$occasion has too few looks", StyleLibrary.forOccasion(occasion).size >= 3)
            val pick = StyleLibrary.pickOfTheDay(occasion, LocalDate.of(2026, 11, 6))
            assertTrue(pick in StyleLibrary.forOccasion(occasion).indices)
        }
    }

    @Test
    fun savedLooksSurviveRestart() {
        SavedLooksStore.toggle("old-money-1")
        SavedLooksStore.toggle("festive-3")
        Stores.loadAll()
        assertEquals(listOf("festive-3", "old-money-1"), SavedLooksStore.ids.toList())
        SavedLooksStore.toggle("festive-3")
        assertFalse(SavedLooksStore.isSaved("festive-3"))
    }
}
