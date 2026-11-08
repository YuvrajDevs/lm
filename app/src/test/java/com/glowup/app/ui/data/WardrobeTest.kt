package com.glowup.app.ui.data

import com.glowup.app.ui.data.style.Garment
import com.glowup.app.ui.data.style.OutfitEngine
import com.glowup.app.ui.data.style.Palette
import com.glowup.app.ui.data.style.StyleLibrary
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import java.time.LocalDate

class WardrobeTest {
    @Before
    fun setUp() {
        freshStores()
    }

    private fun basics() {
        WardrobeStore.add(Garment.TEE, Palette.White, "White tee")
        WardrobeStore.add(Garment.SHIRT, Palette.LightBlue, "Oxford shirt")
        WardrobeStore.add(Garment.TEE, Palette.Rust, "Rust tee")
        WardrobeStore.add(Garment.JEANS, Palette.DarkDenim, "Dark jeans")
        WardrobeStore.add(Garment.TROUSERS, Palette.Stone, "Stone chinos")
        WardrobeStore.add(Garment.SNEAKERS, Palette.White, "White sneakers")
        WardrobeStore.add(Garment.LOAFERS, Palette.Brown, "Brown loafers")
        WardrobeStore.add(Garment.BLAZER, Palette.Navy, "Navy blazer")
        WardrobeStore.add(Garment.TROUSERS, Palette.Charcoal, "Charcoal trousers", formality = 3)
        WardrobeStore.add(Garment.DERBIES, Palette.Black, "Black derbies")
    }

    @Test
    fun colourRulesFollowTheLookbook() {
        assertTrue(OutfitEngine.colourScore(listOf(Palette.White, Palette.Navy)) >= 80)
        assertTrue(OutfitEngine.colourScore(listOf(Palette.Rust, Palette.Stone)) > OutfitEngine.colourScore(listOf(Palette.Rust, Palette.Lavender)))
        assertTrue(OutfitEngine.colourScore(listOf(Palette.Olive, Palette.Rust)) > OutfitEngine.colourScore(listOf(Palette.Burgundy, Palette.Lavender)))
    }

    @Test
    fun suggestionsRespectFormalityAndVariety() {
        basics()
        val casual = OutfitEngine.suggest(WardrobeStore.items, 1)
        assertTrue(casual.isNotEmpty())
        assertTrue(casual.all { o -> o.items.all { kotlin.math.abs(it.formality - 1) <= 1 } })
        val wedding = OutfitEngine.suggest(WardrobeStore.items, 3)
        assertTrue(wedding.isNotEmpty())
        assertTrue("formal looks use smart shoes", wedding.none { it.shoes.garment == Garment.SNEAKERS })
        assertEquals(casual.size, casual.map { it.top.id to it.bottom.id }.toSet().size)
    }

    @Test
    fun makeThisLookFindsOwnedPiecesAndGaps() {
        basics()
        val look = StyleLibrary.outfits.first { o -> o.pieces.any { it.garment == Garment.LOAFERS } }
        val match = OutfitEngine.matchLook(look, WardrobeStore.items)
        assertEquals(look.pieces.size, match.size)
        assertNotNull(match.first { it.piece.garment == Garment.LOAFERS }.owned)
        // Nothing owned for, say, a watch.
        match.filter { it.piece.garment == Garment.WATCH }.forEach { assertNull(it.owned) }
    }

    @Test
    fun capsuleAndPacking() {
        basics()
        val capsule = OutfitEngine.capsule(WardrobeStore.items, 7)
        assertEquals(7, capsule.items.size)
        assertTrue(capsule.outfits > 0)
        val plan = OutfitEngine.pack(WardrobeStore.items, listOf("Everyday", "Everyday", "Dinner"))
        assertEquals(3, plan.days.size)
        assertTrue(plan.days.all { it.second != null })
        assertTrue("reuses pieces", plan.items.size < 3 * 4)
    }

    @Test
    fun wearLogCostPerWearAndPersistence() {
        val item = WardrobeStore.add(Garment.JACKET, Palette.Olive, "Field jacket", price = 3000.0)
        val d = LocalDate.of(2026, 3, 1)
        WardrobeStore.markWorn(listOf(item.id), d)
        WardrobeStore.markWorn(listOf(item.id), d)
        WardrobeStore.markWorn(listOf(item.id), d.plusDays(1))
        Stores.loadAll()
        val loaded = WardrobeStore.find(item.id)!!
        assertEquals(2, loaded.wearCount)
        assertEquals(1500.0, loaded.costPerWear!!, 0.01)
        assertEquals(listOf(loaded), WardrobeStore.wornOn(d))
    }

    @Test
    fun oldTextItemsAreUpgraded() {
        Storage.write("wardrobe", """[{"id":1,"category":"Top","title":"Navy oxford shirt","meta":"Cotton"},{"id":2,"category":"Shoes","title":"White sneakers","meta":""}]""")
        WardrobeStore.load()
        assertEquals(Garment.SHIRT, WardrobeStore.find(1)!!.garment)
        assertEquals(Palette.Navy, WardrobeStore.find(1)!!.colour)
        assertEquals(Garment.SNEAKERS, WardrobeStore.find(2)!!.garment)
    }

    @Test
    fun freeLimit() {
        repeat(WardrobeStore.FREE_MAX_ITEMS) { WardrobeStore.add(Garment.TEE, Palette.White, "Tee $it") }
        assertTrue(WardrobeStore.atFreeLimit)
        ProStore.updatePurchased(true)
        assertTrue(!WardrobeStore.atFreeLimit)
    }
}
