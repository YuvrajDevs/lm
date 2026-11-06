package com.glowup.app.ui.data.style

import androidx.compose.ui.graphics.Color

data class Swatch(val name: String, val color: Color)

/** Named colours used across the lookbook, so every piece shows a real, consistent colour. */
object Palette {
    val White = Swatch("White", Color(0xFFF8F7F2))
    val Ivory = Swatch("Ivory", Color(0xFFF1EBDC))
    val Cream = Swatch("Cream", Color(0xFFE9DFC9))
    val Stone = Swatch("Stone", Color(0xFFBDB4A3))
    val Beige = Swatch("Beige", Color(0xFFD6C3A0))
    val Khaki = Swatch("Khaki", Color(0xFFB09D72))
    val Camel = Swatch("Camel", Color(0xFFBE9461))
    val Tan = Swatch("Tan", Color(0xFFAC7448))
    val Brown = Swatch("Brown", Color(0xFF6B4630))
    val DarkBrown = Swatch("Dark brown", Color(0xFF45301F))
    val Rust = Swatch("Rust", Color(0xFFA14E2C))
    val Mustard = Swatch("Mustard", Color(0xFFC79A34))
    val Olive = Swatch("Olive", Color(0xFF666B41))
    val Sage = Swatch("Sage", Color(0xFFA5B39C))
    val Forest = Swatch("Forest green", Color(0xFF2F4B3B))
    val LightBlue = Swatch("Light blue", Color(0xFFC3D5EB))
    val Sky = Swatch("Sky blue", Color(0xFF8DB2D9))
    val Denim = Swatch("Mid denim", Color(0xFF46638A))
    val DarkDenim = Swatch("Dark denim", Color(0xFF263751))
    val Navy = Swatch("Navy", Color(0xFF1E2A45))
    val Grey = Swatch("Grey", Color(0xFF9C9C9E))
    val LightGrey = Swatch("Light grey", Color(0xFFCFCFD1))
    val Charcoal = Swatch("Charcoal", Color(0xFF3A3B3F))
    val Black = Swatch("Black", Color(0xFF1B1B1D))
    val Burgundy = Swatch("Burgundy", Color(0xFF6C1F2E))
    val Blush = Swatch("Blush", Color(0xFFE6C5C1))
    val Lavender = Swatch("Lavender", Color(0xFFC9C0E2))
    val Maroon = Swatch("Maroon", Color(0xFF7A2232))
    val Emerald = Swatch("Emerald", Color(0xFF1F6B50))
    val Gold = Swatch("Gold", Color(0xFFCBA24C))
    val Silver = Swatch("Silver", Color(0xFFC2C6CB))
}

/** The silhouette used to draw a piece in an outfit's flat lay. */
enum class Garment(val slot: Slot) {
    TEE(Slot.TOP), POLO(Slot.TOP), SHIRT(Slot.TOP), KNIT(Slot.TOP), HOODIE(Slot.TOP), KURTA(Slot.TOP), TANK(Slot.TOP),
    BLAZER(Slot.LAYER), JACKET(Slot.LAYER), OVERSHIRT(Slot.LAYER), NEHRU(Slot.LAYER), BANDHGALA(Slot.LAYER),
    TROUSERS(Slot.BOTTOM), JEANS(Slot.BOTTOM), SHORTS(Slot.BOTTOM), JOGGERS(Slot.BOTTOM),
    SNEAKERS(Slot.SHOES), LOAFERS(Slot.SHOES), BOOTS(Slot.SHOES), DERBIES(Slot.SHOES), SANDALS(Slot.SHOES),
    WATCH(Slot.ACCESSORY), BELT(Slot.ACCESSORY), CAP(Slot.ACCESSORY), BAG(Slot.ACCESSORY), CHAIN(Slot.ACCESSORY),
    SUNGLASSES(Slot.ACCESSORY), TIE(Slot.ACCESSORY), RING(Slot.ACCESSORY), BRACELET(Slot.ACCESSORY),
}

enum class Slot(val label: String) { TOP("Top"), LAYER("Layer"), BOTTOM("Bottom"), SHOES("Shoes"), ACCESSORY("Accessory") }

data class Piece(val name: String, val garment: Garment, val swatch: Swatch)

data class Outfit(
    val id: String,
    val name: String,
    val styleKey: String,
    val pieces: List<Piece>,
    val occasions: List<String>,
    val why: String,
    val dressUp: String,
    val dressDown: String,
) {
    val top: Piece? get() = pieces.firstOrNull { it.garment.slot == Slot.TOP }
    val layer: Piece? get() = pieces.firstOrNull { it.garment.slot == Slot.LAYER }
    val bottom: Piece? get() = pieces.firstOrNull { it.garment.slot == Slot.BOTTOM }
    val shoes: Piece? get() = pieces.firstOrNull { it.garment.slot == Slot.SHOES }
    val accessories: List<Piece> get() = pieces.filter { it.garment.slot == Slot.ACCESSORY }
}

data class Pairing(val item: String, val goesWith: List<String>, val avoid: String)

data class StyleGuide(
    val key: String,
    val title: String,
    val tagline: String,
    val blurb: String,
    val tint: Color,
    val accent: Color,
    val heroText: Color,
    val palette: List<Swatch>,
    val goodFor: List<String>,
    val keyPieces: List<String>,
    val stylingTips: List<String>,
    val keepInMind: List<String>,
    val pairings: List<Pairing>,
    val outfits: List<Outfit>,
)

internal fun piece(name: String, garment: Garment, swatch: Swatch) = Piece(name, garment, swatch)

internal class OutfitBuilder(private val styleKey: String) {
    private var count = 0
    val outfits = mutableListOf<Outfit>()

    fun outfit(name: String, occasions: List<String>, why: String, dressUp: String, dressDown: String, vararg pieces: Piece) {
        count++
        outfits += Outfit("$styleKey-$count", name, styleKey, pieces.toList(), occasions, why, dressUp, dressDown)
    }
}

internal fun outfits(styleKey: String, build: OutfitBuilder.() -> Unit) = OutfitBuilder(styleKey).apply(build).outfits.toList()
