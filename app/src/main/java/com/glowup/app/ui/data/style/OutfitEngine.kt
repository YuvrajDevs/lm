package com.glowup.app.ui.data.style

import com.glowup.app.ui.data.WardrobeItem
import kotlin.math.abs
import kotlin.math.sqrt

/** An outfit built from the user's own clothes. */
data class ClosetOutfit(val top: WardrobeItem, val bottom: WardrobeItem, val shoes: WardrobeItem, val layer: WardrobeItem?, val score: Int) {
    val items: List<WardrobeItem> get() = listOfNotNull(layer, top, bottom, shoes)
    val ids: Set<Int> get() = items.mapTo(HashSet()) { it.id }
}

/** One piece of a lookbook outfit, matched (or not) to something the user owns. */
data class LookMatch(val piece: Piece, val owned: WardrobeItem?, val exactColour: Boolean)

data class PackingPlan(val days: List<Pair<String, ClosetOutfit?>>, val items: List<WardrobeItem>)

data class CapsuleResult(val items: List<WardrobeItem>, val outfits: Int, val gaps: List<String>)

/**
 * Builds outfits from the user's wardrobe using the same rules as the lookbook: neutrals go with
 * everything, one accent colour at a time, and every piece within one step of formality.
 */
object OutfitEngine {
    private val neutrals = setOf(
        Palette.White, Palette.Ivory, Palette.Cream, Palette.Beige, Palette.Stone, Palette.Khaki, Palette.Camel, Palette.Tan,
        Palette.Brown, Palette.DarkBrown, Palette.LightGrey, Palette.Grey, Palette.Charcoal, Palette.Black, Palette.Navy,
        Palette.LightBlue, Palette.Denim, Palette.DarkDenim
    ).map { it.name }.toSet()

    /** Earthy accents that sit happily together (olive with rust, mustard with forest…). */
    private val earthy = setOf(Palette.Olive, Palette.Sage, Palette.Forest, Palette.Rust, Palette.Mustard).map { it.name }.toSet()

    /** Occasion → target formality (1 casual … 3 formal). */
    val occasions: List<Pair<String, Int>> = listOf(
        "Everyday" to 1, "Weekend" to 1, "Gym" to 1, "College" to 1, "Date" to 2, "Office" to 2, "Dinner" to 2, "Wedding" to 3, "Interview" to 3
    )

    fun isNeutral(s: Swatch) = s.name in neutrals

    /** 0–100: how well these colours work together. */
    fun colourScore(colours: List<Swatch>): Int {
        val accents = colours.filterNot(::isNeutral).map { it.name }.distinct()
        var score = when {
            accents.isEmpty() -> 80
            accents.size == 1 -> 95
            accents.size == 2 && accents.all { it in earthy } -> 78
            accents.size == 2 -> 40
            else -> 15
        }
        // A bit of contrast between top and bottom reads sharper than head-to-toe one shade.
        if (colours.size >= 2 && colours[0].name == colours[1].name) score -= 15
        // Black with navy or brown side by side is a classic clash.
        val names = colours.map { it.name }.toSet()
        if (Palette.Black.name in names && (Palette.Navy.name in names || Palette.Brown.name in names)) score -= 20
        return score.coerceIn(0, 100)
    }

    fun outfitScore(top: WardrobeItem, bottom: WardrobeItem, shoes: WardrobeItem, layer: WardrobeItem?, target: Int): Int? {
        val pieces = listOfNotNull(top, bottom, shoes, layer)
        if (pieces.any { abs(it.formality - target) > 1 }) return null
        // Garment rules that always look wrong.
        if (bottom.garment == Garment.SHORTS && layer?.garment in setOf(Garment.BLAZER, Garment.BANDHGALA, Garment.NEHRU)) return null
        if (target == 3 && (shoes.garment == Garment.SNEAKERS || shoes.garment == Garment.SANDALS) && layer != null) return null
        if (top.garment == Garment.KURTA && layer != null && layer.garment !in setOf(Garment.NEHRU, Garment.BANDHGALA)) return null
        val formalityFit = 100 - pieces.sumOf { abs(it.formality - target) } * 12
        val colours = listOfNotNull(layer?.colour, top.colour, bottom.colour, shoes.colour)
        // Prefer things you haven't worn lately so suggestions rotate.
        val freshness = pieces.sumOf { it.wearCount.coerceAtMost(10) }
        return (colourScore(listOf(top.colour, bottom.colour) + listOfNotNull(layer?.colour, shoes.colour)) * 6 + formalityFit * 4) / 10 - freshness +
            (if (colours.size >= 4) 2 else 0)
    }

    /** Best outfits for an occasion, all different, highest score first. */
    fun suggest(items: List<WardrobeItem>, target: Int, limit: Int = 6, avoid: Set<Int> = emptySet(), shuffleSeed: Int = 0): List<ClosetOutfit> {
        val tops = items.filter { it.slot == Slot.TOP }
        val bottoms = items.filter { it.slot == Slot.BOTTOM }
        val shoes = items.filter { it.slot == Slot.SHOES }
        val layers = listOf<WardrobeItem?>(null) + items.filter { it.slot == Slot.LAYER }
        val all = mutableListOf<ClosetOutfit>()
        for (t in tops) {
            for (b in bottoms) {
                for (s in shoes) {
                    for (l in layers) {
                        val score = outfitScore(t, b, s, l, target) ?: continue
                        // Formal occasions want a layer when one is available.
                        val bonus = if (target == 3 && l != null) 8 else 0
                        all += ClosetOutfit(t, b, s, l, score + bonus - ((t.id * 31 + b.id * 17 + shuffleSeed) % 3))
                    }
                }
            }
        }
        val result = mutableListOf<ClosetOutfit>()
        for (o in all.sortedByDescending { it.score }) {
            if (o.ids.any { it in avoid }) continue
            // Keep suggestions varied: no two share both the top and the bottom.
            if (result.any { it.top.id == o.top.id && it.bottom.id == o.bottom.id }) continue
            result += o
            if (result.size >= limit) break
        }
        return result
    }

    fun countOutfits(items: List<WardrobeItem>): Int {
        var n = 0
        val tops = items.filter { it.slot == Slot.TOP }
        val bottoms = items.filter { it.slot == Slot.BOTTOM }
        val shoes = items.filter { it.slot == Slot.SHOES }
        for (t in tops) {
            for (b in bottoms) {
                for (s in shoes) {
                    if ((1..3).any { target -> outfitScore(t, b, s, null, target)?.let { it >= 60 } == true }) n++
                }
            }
        }
        return n
    }

    // ---------- "Make this look from my wardrobe" ----------

    private fun distance(a: Swatch, b: Swatch): Double {
        val dr = a.color.red - b.color.red
        val dg = a.color.green - b.color.green
        val db = a.color.blue - b.color.blue
        return sqrt((dr * dr + dg * dg + db * db).toDouble())
    }

    /** Garments close enough to stand in for each other. */
    private val standIns = mapOf(
        Garment.TEE to setOf(Garment.TANK, Garment.POLO),
        Garment.POLO to setOf(Garment.TEE),
        Garment.SHIRT to setOf(Garment.POLO),
        Garment.KNIT to setOf(Garment.HOODIE),
        Garment.JACKET to setOf(Garment.OVERSHIRT),
        Garment.OVERSHIRT to setOf(Garment.JACKET),
        Garment.TROUSERS to setOf(Garment.JEANS),
        Garment.LOAFERS to setOf(Garment.DERBIES),
        Garment.DERBIES to setOf(Garment.LOAFERS),
        Garment.BOOTS to setOf(Garment.DERBIES)
    )

    fun matchLook(outfit: Outfit, items: List<WardrobeItem>): List<LookMatch> {
        val used = mutableSetOf<Int>()
        return outfit.pieces.map { piece ->
            val sameGarment = items.filter { it.id !in used && it.garment == piece.garment }
            val similar = items.filter { it.id !in used && it.garment in standIns[piece.garment].orEmpty() }
            val best = (sameGarment.ifEmpty { similar })
                .minByOrNull { distance(it.colour, piece.swatch) }
                ?.takeIf {
                    distance(it.colour, piece.swatch) < 0.42 || (isNeutral(it.colour) && isNeutral(piece.swatch) && piece.garment.slot != Slot.ACCESSORY)
                }
            best?.let { used += it.id }
            LookMatch(piece, best, best != null && best.garment == piece.garment && distance(best.colour, piece.swatch) < 0.15)
        }
    }

    // ---------- Capsule ----------

    /** Greedily picks [size] items that make the most outfits together. */
    fun capsule(items: List<WardrobeItem>, size: Int): CapsuleResult {
        val pool = items.filter { it.slot != Slot.ACCESSORY }
        val chosen = mutableListOf<WardrobeItem>()
        // Seed with the most versatile piece of each core slot.
        listOf(Slot.TOP, Slot.BOTTOM, Slot.SHOES).forEach { slot ->
            pool.filter { it.slot == slot }.maxByOrNull { if (isNeutral(it.colour)) 2 else 1 }?.let { chosen += it }
        }
        while (chosen.size < size) {
            val next = pool.filter { it !in chosen }.maxByOrNull { countOutfits(chosen + it) * 10 + (if (isNeutral(it.colour)) 1 else 0) } ?: break
            chosen += next
        }
        val gaps = buildList {
            val slots = chosen.groupBy { it.slot }
            if ((slots[Slot.TOP]?.size ?: 0) < 3) add("More tops — a white and a navy tee or shirt go with almost everything")
            if ((slots[Slot.BOTTOM]?.size ?: 0) < 2) add("A second pair of trousers in a neutral (stone, navy or charcoal)")
            if ((slots[Slot.SHOES]?.size ?: 0) < 2) add("A second pair of shoes — clean white sneakers or brown loafers")
            if (slots[Slot.LAYER].isNullOrEmpty()) add("One layer, like a navy overshirt or blazer, multiplies your outfits")
        }
        return CapsuleResult(chosen.take(size), countOutfits(chosen.take(size)), gaps)
    }

    // ---------- Packing ----------

    /** An outfit per day, reusing bottoms and shoes where possible so the bag stays small. */
    fun pack(items: List<WardrobeItem>, days: List<String>): PackingPlan {
        val plans = mutableListOf<Pair<String, ClosetOutfit?>>()
        val usedTops = mutableSetOf<Int>()
        days.forEach { occasion ->
            val target = occasions.firstOrNull { it.first == occasion }?.second ?: 1
            val packed = plans.mapNotNull { it.second }.flatMap { it.items }.map { it.id }.toSet()
            val options = suggest(items, target, limit = 30, avoid = usedTops)
            // Prefer outfits that reuse what's already in the bag.
            val pick = options.maxByOrNull { o -> o.score + o.items.count { it.id in packed } * 25 }
            pick?.let { usedTops += it.top.id }
            plans += occasion to pick
        }
        val bag = plans.mapNotNull { it.second }.flatMap { it.items }.distinctBy { it.id }.sortedBy { it.slot.ordinal }
        return PackingPlan(plans, bag)
    }
}
