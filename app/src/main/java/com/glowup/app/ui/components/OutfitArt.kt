package com.glowup.app.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.graphics.luminance
import com.glowup.app.ui.data.style.Garment
import com.glowup.app.ui.data.style.Outfit

/** A flat-lay illustration of an outfit: every piece drawn as a simple silhouette in its real colour. */
@Composable
fun OutfitArt(outfit: Outfit, modifier: Modifier = Modifier) {
    Canvas(modifier = modifier) {
        val w = size.width
        val h = size.height
        fun box(x0: Float, y0: Float, x1: Float, y1: Float) = Rect(x0 * w, y0 * h, x1 * w, y1 * h)

        val longTop = outfit.top?.garment == Garment.KURTA
        val topBox = box(0.05f, 0.05f, 0.53f, if (longTop) 0.66f else 0.52f)
        val bottom = outfit.bottom
        val bottomBox = if (bottom?.garment == Garment.SHORTS) box(0.58f, 0.08f, 0.94f, 0.54f) else box(0.58f, 0.06f, 0.94f, 0.95f)
        val shoesBox = if (longTop) box(0.10f, 0.80f, 0.50f, 0.96f) else box(0.06f, 0.70f, 0.52f, 0.94f)

        bottom?.let { drawBottom(it.garment, bottomBox, it.swatch.color) }

        val layer = outfit.layer
        val top = outfit.top
        if (layer != null && layer.garment == Garment.NEHRU) {
            top?.let { drawTop(it.garment, topBox, it.swatch.color) }
            drawVest(topBox, layer.swatch.color, longTop)
        } else if (layer != null) {
            drawLayer(layer.garment, topBox, layer.swatch.color, top?.swatch?.color ?: layer.swatch.color, outfit.accessories.firstOrNull { it.garment == Garment.TIE }?.swatch?.color)
        } else if (top != null) {
            drawTop(top.garment, topBox, top.swatch.color)
            outfit.accessories.firstOrNull { it.garment == Garment.TIE }?.let { drawTie(topBox, it.swatch.color) }
        }

        outfit.shoes?.let { drawShoes(it.garment, shoesBox, it.swatch.color) }

        val rowTop = if (longTop) 0.68f else 0.56f
        val rowBottom = if (longTop) 0.79f else 0.68f
        val slots = listOf(box(0.06f, rowTop, 0.20f, rowBottom), box(0.23f, rowTop, 0.37f, rowBottom), box(0.40f, rowTop, 0.54f, rowBottom))
        outfit.accessories.filter { it.garment != Garment.TIE }.take(3).forEachIndexed { index, piece ->
            drawAccessory(piece.garment, slots[index], piece.swatch.color)
        }
    }
}

/** One garment on its own, centred — used for wardrobe items without a photo. */
@Composable
fun GarmentArt(garment: Garment, color: Color, modifier: Modifier = Modifier) {
    Canvas(modifier = modifier) {
        val w = size.width
        val h = size.height
        val r = when (garment.slot) {
            com.glowup.app.ui.data.style.Slot.BOTTOM -> if (garment == Garment.SHORTS) Rect(w * 0.22f, h * 0.2f, w * 0.78f, h * 0.8f) else Rect(w * 0.25f, h * 0.02f, w * 0.75f, h * 0.98f)
            com.glowup.app.ui.data.style.Slot.SHOES -> Rect(w * 0.04f, h * 0.3f, w * 0.96f, h * 0.7f)
            com.glowup.app.ui.data.style.Slot.ACCESSORY -> Rect(w * 0.22f, h * 0.22f, w * 0.78f, h * 0.78f)
            else -> Rect(w * 0.02f, h * 0.06f, w * 0.98f, h * 0.94f)
        }
        when (garment.slot) {
            com.glowup.app.ui.data.style.Slot.TOP -> drawTop(garment, r, color)
            com.glowup.app.ui.data.style.Slot.LAYER -> if (garment == Garment.NEHRU) drawVest(r, color, false) else drawLayer(garment, r, color, Color(0xFFF8F7F2), null)
            com.glowup.app.ui.data.style.Slot.BOTTOM -> drawBottom(garment, r, color)
            com.glowup.app.ui.data.style.Slot.SHOES -> drawShoes(garment, r, color)
            com.glowup.app.ui.data.style.Slot.ACCESSORY -> if (garment == Garment.TIE) drawTie(Rect(w * 0.1f, h * 0.0f, w * 0.9f, h * 1.6f), color) else drawAccessory(garment, r, color)
        }
    }
}

private fun outlineFor(color: Color) = if (color.luminance() > 0.5f) lerp(color, Color.Black, 0.28f) else lerp(color, Color.Black, 0.45f)

private fun detailFor(color: Color) = if (color.luminance() > 0.35f) lerp(color, Color.Black, 0.22f) else lerp(color, Color.White, 0.22f)

private fun shape(r: Rect, vararg pts: Pair<Float, Float>): Path = Path().apply {
    pts.forEachIndexed { i, (x, y) ->
        val px = r.left + x * r.width
        val py = r.top + y * r.height
        if (i == 0) moveTo(px, py) else lineTo(px, py)
    }
    close()
}

private fun Rect.at(x: Float, y: Float) = Offset(left + x * width, top + y * height)

private fun DrawScope.fillOutlined(path: Path, color: Color) {
    drawPath(path, color)
    drawPath(path, outlineFor(color), style = Stroke(width = 1.4f * density, cap = StrokeCap.Round))
}

private fun DrawScope.line(r: Rect, x0: Float, y0: Float, x1: Float, y1: Float, color: Color, width: Float = 1.2f) {
    drawLine(color, r.at(x0, y0), r.at(x1, y1), strokeWidth = width * density, cap = StrokeCap.Round)
}

private fun shortSleeve(r: Rect) = Path().apply {
    moveTo(r.at(0.36f, 0f).x, r.at(0.36f, 0f).y)
    quadraticTo(r.at(0.5f, 0.15f).x, r.at(0.5f, 0.15f).y, r.at(0.64f, 0f).x, r.at(0.64f, 0f).y)
    listOf(0.86f to 0.05f, 1f to 0.30f, 0.85f to 0.40f, 0.80f to 0.27f, 0.80f to 1f, 0.20f to 1f, 0.20f to 0.27f, 0.15f to 0.40f, 0f to 0.30f, 0.14f to 0.05f)
        .forEach { (x, y) -> r.at(x, y).let { lineTo(it.x, it.y) } }
    close()
}

private fun longSleeve(r: Rect, neckDepth: Float = 0.15f) = Path().apply {
    moveTo(r.at(0.36f, 0f).x, r.at(0.36f, 0f).y)
    quadraticTo(r.at(0.5f, neckDepth).x, r.at(0.5f, neckDepth).y, r.at(0.64f, 0f).x, r.at(0.64f, 0f).y)
    listOf(0.86f to 0.05f, 1f to 0.86f, 0.87f to 0.90f, 0.80f to 0.32f, 0.80f to 1f, 0.20f to 1f, 0.20f to 0.32f, 0.13f to 0.90f, 0f to 0.86f, 0.14f to 0.05f)
        .forEach { (x, y) -> r.at(x, y).let { lineTo(it.x, it.y) } }
    close()
}

private fun DrawScope.drawTop(garment: Garment, r: Rect, color: Color) {
    val detail = detailFor(color)
    when (garment) {
        Garment.TEE -> fillOutlined(shortSleeve(r), color)
        Garment.TANK -> fillOutlined(shape(r, 0.30f to 0f, 0.40f to 0f, 0.5f to 0.18f, 0.60f to 0f, 0.70f to 0f, 0.80f to 0.30f, 0.80f to 1f, 0.20f to 1f, 0.20f to 0.30f), color)
        Garment.POLO -> {
            fillOutlined(shortSleeve(r), color)
            fillOutlined(shape(r, 0.36f to 0f, 0.46f to 0.12f, 0.5f to 0.05f), detail)
            fillOutlined(shape(r, 0.64f to 0f, 0.54f to 0.12f, 0.5f to 0.05f), detail)
            line(r, 0.5f, 0.08f, 0.5f, 0.30f, detail)
            drawCircle(detail, 1.6f * density, r.at(0.5f, 0.17f))
            drawCircle(detail, 1.6f * density, r.at(0.5f, 0.25f))
        }
        Garment.SHIRT -> {
            fillOutlined(longSleeve(r, 0.08f), color)
            fillOutlined(shape(r, 0.36f to 0f, 0.45f to 0.14f, 0.5f to 0.05f), lerp(color, Color.White, 0.15f))
            fillOutlined(shape(r, 0.64f to 0f, 0.55f to 0.14f, 0.5f to 0.05f), lerp(color, Color.White, 0.15f))
            line(r, 0.5f, 0.08f, 0.5f, 1f, detail)
            listOf(0.25f, 0.42f, 0.59f, 0.76f).forEach { drawCircle(detail, 1.5f * density, r.at(0.53f, it)) }
            line(r, 0.13f, 0.82f, 0.01f, 0.79f, detail)
            line(r, 0.87f, 0.82f, 0.99f, 0.79f, detail)
        }
        Garment.KNIT -> {
            fillOutlined(longSleeve(r, 0.12f), color)
            for (i in 0..8) line(r, 0.22f + i * 0.07f, 0.92f, 0.22f + i * 0.07f, 1f, detail, 1f)
            line(r, 0.20f, 0.91f, 0.80f, 0.91f, detail)
            line(r, 0.36f, 0.02f, 0.64f, 0.02f, detail, 1.6f)
        }
        Garment.HOODIE -> {
            fillOutlined(shape(r, 0.32f to 0.05f, 0.36f to -0.12f, 0.64f to -0.12f, 0.68f to 0.05f), lerp(color, Color.Black, 0.12f))
            fillOutlined(longSleeve(r, 0.12f), color)
            drawRoundRect(detail, r.at(0.32f, 0.60f), Size(0.36f * r.width, 0.22f * r.height), CornerRadius(4f * density), style = Stroke(1.2f * density))
            line(r, 0.45f, 0.10f, 0.44f, 0.36f, detail)
            line(r, 0.55f, 0.10f, 0.56f, 0.36f, detail)
        }
        Garment.KURTA -> {
            fillOutlined(longSleeve(r, 0.07f), color)
            line(r, 0.5f, 0.04f, 0.5f, 0.34f, detail)
            listOf(0.10f, 0.18f, 0.26f).forEach { drawCircle(detail, 1.5f * density, r.at(0.53f, it)) }
            line(r, 0.20f, 0.80f, 0.23f, 1f, detail)
            line(r, 0.80f, 0.80f, 0.77f, 1f, detail)
            line(r, 0.40f, 0.01f, 0.60f, 0.01f, detail, 1.6f)
        }
        else -> fillOutlined(shortSleeve(r), color)
    }
}

private fun DrawScope.drawLayer(garment: Garment, r: Rect, color: Color, inner: Color, tie: Color?) {
    val detail = detailFor(color)
    fillOutlined(longSleeve(r, 0.04f), color)
    when (garment) {
        Garment.BLAZER -> {
            fillOutlined(shape(r, 0.40f to 0f, 0.60f to 0f, 0.5f to 0.52f), inner)
            tie?.let { fillOutlined(shape(r, 0.47f to 0.06f, 0.53f to 0.06f, 0.55f to 0.40f, 0.5f to 0.50f, 0.45f to 0.40f), it) }
            line(r, 0.36f, 0.0f, 0.43f, 0.28f, detail, 1.4f)
            line(r, 0.43f, 0.28f, 0.5f, 0.54f, detail, 1.4f)
            line(r, 0.64f, 0.0f, 0.57f, 0.28f, detail, 1.4f)
            line(r, 0.57f, 0.28f, 0.5f, 0.54f, detail, 1.4f)
            drawCircle(detail, 2f * density, r.at(0.52f, 0.66f))
            line(r, 0.24f, 0.70f, 0.38f, 0.70f, detail)
            line(r, 0.62f, 0.70f, 0.76f, 0.70f, detail)
        }
        Garment.BANDHGALA -> {
            line(r, 0.5f, 0.06f, 0.5f, 1f, detail, 1.4f)
            listOf(0.18f, 0.34f, 0.50f, 0.66f, 0.82f).forEach { drawCircle(lerp(detail, Color(0xFFCBA24C), 0.6f), 2f * density, r.at(0.5f, it)) }
            line(r, 0.38f, 0.02f, 0.62f, 0.02f, detail, 2f)
        }
        else -> {
            fillOutlined(shape(r, 0.44f to 0.02f, 0.56f to 0.02f, 0.56f to 1f, 0.44f to 1f), inner)
            line(r, 0.20f, 0.30f, 0.38f, 0.30f, detail)
            line(r, 0.62f, 0.30f, 0.80f, 0.30f, detail)
            if (garment == Garment.JACKET) {
                line(r, 0.20f, 0.95f, 0.80f, 0.95f, detail, 1.6f)
            }
        }
    }
}

private fun DrawScope.drawVest(r: Rect, color: Color, overLongTop: Boolean) {
    val hem = if (overLongTop) 0.70f else 0.96f
    fillOutlined(shape(r, 0.27f to 0.02f, 0.40f to 0.02f, 0.5f to 0.10f, 0.60f to 0.02f, 0.73f to 0.02f, 0.80f to 0.24f, 0.80f to hem, 0.20f to hem, 0.20f to 0.24f), color)
    val detail = detailFor(color)
    line(r, 0.5f, 0.10f, 0.5f, hem, detail, 1.4f)
    listOf(0.22f, 0.36f, 0.50f, 0.64f).filter { it < hem - 0.04f }.forEach { drawCircle(lerp(detail, Color(0xFFCBA24C), 0.6f), 2f * density, r.at(0.53f, it)) }
    line(r, 0.38f, 0.01f, 0.62f, 0.01f, detail, 2f)
    line(r, 0.26f, 0.40f, 0.36f, 0.40f, detail)
}

private fun DrawScope.drawTie(r: Rect, color: Color) {
    fillOutlined(shape(r, 0.47f to 0.06f, 0.53f to 0.06f, 0.56f to 0.66f, 0.5f to 0.76f, 0.44f to 0.66f), color)
}

private fun DrawScope.drawBottom(garment: Garment, r: Rect, color: Color) {
    val detail = detailFor(color)
    when (garment) {
        Garment.SHORTS -> {
            fillOutlined(shape(r, 0.04f to 0.08f, 0.96f to 0.08f, 1f to 1f, 0.55f to 1f, 0.5f to 0.55f, 0.45f to 1f, 0f to 1f), color)
            fillOutlined(shape(r, 0.04f to 0f, 0.96f to 0f, 0.96f to 0.10f, 0.04f to 0.10f), color)
        }
        Garment.JOGGERS -> {
            fillOutlined(shape(r, 0.06f to 0.06f, 0.94f to 0.06f, 0.96f to 0.40f, 0.86f to 0.94f, 0.60f to 0.94f, 0.5f to 0.30f, 0.40f to 0.94f, 0.14f to 0.94f, 0.04f to 0.40f), color)
            fillOutlined(shape(r, 0.06f to 0f, 0.94f to 0f, 0.94f to 0.07f, 0.06f to 0.07f), detail)
            fillOutlined(shape(r, 0.14f to 0.94f, 0.40f to 0.94f, 0.40f to 1f, 0.14f to 1f), detail)
            fillOutlined(shape(r, 0.60f to 0.94f, 0.86f to 0.94f, 0.86f to 1f, 0.60f to 1f), detail)
            line(r, 0.46f, 0.07f, 0.44f, 0.18f, Color.White.copy(alpha = 0.8f))
            line(r, 0.54f, 0.07f, 0.56f, 0.18f, Color.White.copy(alpha = 0.8f))
        }
        else -> {
            fillOutlined(shape(r, 0.06f to 0.06f, 0.94f to 0.06f, 0.99f to 1f, 0.56f to 1f, 0.5f to 0.28f, 0.44f to 1f, 0.01f to 1f), color)
            fillOutlined(shape(r, 0.06f to 0f, 0.94f to 0f, 0.94f to 0.07f, 0.06f to 0.07f), color)
            if (garment == Garment.JEANS) {
                val stitch = Color(0xFFD9A55B).copy(alpha = 0.85f)
                line(r, 0.08f, 0.07f, 0.22f, 0.18f, stitch)
                line(r, 0.92f, 0.07f, 0.78f, 0.18f, stitch)
                line(r, 0.5f, 0.07f, 0.5f, 0.26f, stitch)
            } else {
                line(r, 0.24f, 0.12f, 0.22f, 0.98f, detail, 1f)
                line(r, 0.76f, 0.12f, 0.78f, 0.98f, detail, 1f)
            }
        }
    }
}

private fun DrawScope.drawShoes(garment: Garment, r: Rect, color: Color) {
    val left = Rect(r.left, r.top + r.height * 0.18f, r.left + r.width * 0.50f, r.bottom)
    val right = Rect(r.left + r.width * 0.50f, r.top, r.right, r.bottom - r.height * 0.18f)
    listOf(left, right).forEach { drawShoe(garment, it, color) }
}

private fun DrawScope.drawShoe(garment: Garment, r: Rect, color: Color) {
    val detail = detailFor(color)
    when (garment) {
        Garment.SNEAKERS -> {
            val upper = Path().apply {
                r.at(0.03f, 0.82f).let { moveTo(it.x, it.y) }
                r.at(0.03f, 0.40f).let { lineTo(it.x, it.y) }
                r.at(0.12f, 0.30f).let { c -> r.at(0.24f, 0.36f).let { quadraticTo(c.x, c.y, it.x, it.y) } }
                r.at(0.34f, 0.28f).let { lineTo(it.x, it.y) }
                r.at(0.84f, 0.50f).let { lineTo(it.x, it.y) }
                r.at(1.0f, 0.58f).let { c -> r.at(0.99f, 0.82f).let { quadraticTo(c.x, c.y, it.x, it.y) } }
                close()
            }
            fillOutlined(upper, color)
            val sole = Color(0xFFF5F3EC)
            drawRoundRect(sole, r.at(0f, 0.78f), Size(r.width, r.height * 0.20f), CornerRadius(r.height * 0.10f))
            drawRoundRect(outlineFor(sole), r.at(0f, 0.78f), Size(r.width, r.height * 0.20f), CornerRadius(r.height * 0.10f), style = Stroke(1.2f * density))
            line(r, 0.32f, 0.40f, 0.40f, 0.52f, detail)
            line(r, 0.42f, 0.44f, 0.50f, 0.56f, detail)
        }
        Garment.BOOTS -> {
            fillOutlined(shape(r, 0.10f to 0.86f, 0.10f to 0.0f, 0.46f to 0.0f, 0.50f to 0.46f, 0.86f to 0.58f, 0.99f to 0.74f, 0.99f to 0.86f), color)
            fillOutlined(shape(r, 0.08f to 0.84f, 1f to 0.84f, 1f to 1f, 0.08f to 1f), lerp(color, Color.Black, 0.55f))
        }
        Garment.SANDALS -> {
            fillOutlined(shape(r, 0f to 0.82f, 1f to 0.82f, 1f to 1f, 0f to 1f), lerp(color, Color.Black, 0.25f))
            fillOutlined(shape(r, 0.40f to 0.82f, 0.50f to 0.44f, 0.86f to 0.58f, 0.90f to 0.82f), color)
        }
        else -> {
            val upper = Path().apply {
                r.at(0.03f, 0.86f).let { moveTo(it.x, it.y) }
                r.at(0.03f, 0.54f).let { lineTo(it.x, it.y) }
                r.at(0.30f, 0.46f).let { c -> r.at(0.56f, 0.54f).let { quadraticTo(c.x, c.y, it.x, it.y) } }
                r.at(0.86f, 0.60f).let { lineTo(it.x, it.y) }
                r.at(1.0f, 0.66f).let { c -> r.at(0.98f, 0.86f).let { quadraticTo(c.x, c.y, it.x, it.y) } }
                close()
            }
            fillOutlined(upper, color)
            val sole = lerp(color, Color.Black, 0.5f)
            drawRoundRect(sole, r.at(0.01f, 0.84f), Size(r.width * 0.98f, r.height * 0.13f), CornerRadius(r.height * 0.06f))
            if (garment == Garment.DERBIES) {
                drawCircle(detail, 1.4f * density, r.at(0.48f, 0.58f))
                drawCircle(detail, 1.4f * density, r.at(0.56f, 0.60f))
            } else {
                line(r, 0.44f, 0.56f, 0.70f, 0.64f, detail, 1.6f)
            }
        }
    }
}

private fun DrawScope.drawAccessory(garment: Garment, r: Rect, color: Color) {
    val outline = outlineFor(color)
    val c = r.center
    val s = minOf(r.width, r.height)
    when (garment) {
        Garment.WATCH -> {
            drawRoundRect(lerp(color, Color.Black, 0.35f), Offset(c.x - s * 0.14f, r.top), Size(s * 0.28f, r.height), CornerRadius(3f * density))
            drawCircle(color, s * 0.30f, c)
            drawCircle(Color(0xFFF7F5EE), s * 0.22f, c)
            drawCircle(outline, s * 0.30f, c, style = Stroke(1.2f * density))
            drawLine(Color.Black, c, Offset(c.x, c.y - s * 0.14f), 1.2f * density)
            drawLine(Color.Black, c, Offset(c.x + s * 0.10f, c.y), 1.2f * density)
        }
        Garment.BELT -> {
            drawRoundRect(color, Offset(r.left, c.y - s * 0.12f), Size(r.width, s * 0.24f), CornerRadius(3f * density))
            drawRoundRect(Color(0xFFC9CDD2), Offset(c.x - s * 0.18f, c.y - s * 0.20f), Size(s * 0.36f, s * 0.40f), CornerRadius(2f * density), style = Stroke(2f * density))
        }
        Garment.CAP -> {
            val dome = Path().apply {
                moveTo(r.left + r.width * 0.12f, c.y + s * 0.15f)
                cubicTo(r.left + r.width * 0.12f, r.top, r.right - r.width * 0.22f, r.top, r.right - r.width * 0.22f, c.y + s * 0.15f)
                close()
            }
            fillOutlined(dome, color)
            drawRoundRect(lerp(color, Color.Black, 0.2f), Offset(r.right - r.width * 0.32f, c.y + s * 0.08f), Size(r.width * 0.32f, s * 0.12f), CornerRadius(4f * density))
        }
        Garment.BAG -> {
            drawArc(outline, 180f, 180f, false, Offset(c.x - s * 0.22f, r.top), Size(s * 0.44f, s * 0.44f), style = Stroke(2f * density))
            drawRoundRect(color, Offset(r.left + r.width * 0.12f, c.y - s * 0.12f), Size(r.width * 0.76f, s * 0.58f), CornerRadius(4f * density))
            drawRoundRect(outline, Offset(r.left + r.width * 0.12f, c.y - s * 0.12f), Size(r.width * 0.76f, s * 0.58f), CornerRadius(4f * density), style = Stroke(1.2f * density))
        }
        Garment.CHAIN -> {
            drawArc(color, 20f, 140f, false, Offset(c.x - s * 0.40f, r.top - s * 0.30f), Size(s * 0.80f, s * 0.95f), style = Stroke(2.4f * density))
            drawCircle(color, s * 0.10f, Offset(c.x, r.top + s * 0.66f))
        }
        Garment.SUNGLASSES -> {
            drawCircle(color, s * 0.20f, Offset(c.x - s * 0.24f, c.y))
            drawCircle(color, s * 0.20f, Offset(c.x + s * 0.24f, c.y))
            drawLine(color, Offset(c.x - s * 0.06f, c.y - s * 0.04f), Offset(c.x + s * 0.06f, c.y - s * 0.04f), 2f * density)
        }
        Garment.RING -> drawCircle(color, s * 0.26f, c, style = Stroke(3.2f * density))
        Garment.BRACELET -> drawOval(color, Offset(r.left + r.width * 0.10f, c.y - s * 0.22f), Size(r.width * 0.80f, s * 0.44f), style = Stroke(3f * density))
        else -> Unit
    }
}
