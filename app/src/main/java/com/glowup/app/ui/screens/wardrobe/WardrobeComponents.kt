package com.glowup.app.ui.screens.wardrobe

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.produceState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.glowup.app.ui.components.GarmentArt
import com.glowup.app.ui.data.PhotoVault
import com.glowup.app.ui.data.WardrobeItem
import com.glowup.app.ui.data.WardrobeStore
import com.glowup.app.ui.data.style.Swatch
import com.glowup.app.ui.theme.ChipNeutral
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.ByteArrayOutputStream
import java.io.File
import kotlin.math.max
import kotlin.math.sqrt

/** The item's photo if it has one, otherwise the same flat-lay drawing the lookbook uses. */
@Composable
fun ItemImage(item: WardrobeItem, modifier: Modifier, corner: Dp = 16.dp) {
    val file = item.photo?.let { name -> PhotoVault.list(PhotoVault.Album.WARDROBE).firstOrNull { it.name == name } }
    val bitmap by produceState<Bitmap?>(null, file) {
        value = file?.let { f -> withContext(Dispatchers.IO) { runCatching { BitmapFactory.decodeFile(f.absolutePath, BitmapFactory.Options().apply { inSampleSize = 4 }) }.getOrNull() } }
    }
    Box(modifier.clip(RoundedCornerShape(corner)).background(item.tint), contentAlignment = Alignment.Center) {
        val bmp = bitmap
        if (bmp != null) {
            Image(bmp.asImageBitmap(), contentDescription = item.title, contentScale = ContentScale.Crop, modifier = Modifier.fillMaxSize())
        } else {
            GarmentArt(item.garment, item.colour.color, Modifier.fillMaxSize(0.72f))
        }
    }
}

@Composable
fun SwatchDot(swatch: Swatch, size: Dp = 18.dp, selected: Boolean = false) {
    Box(
        Modifier.size(size).clip(CircleShape).background(swatch.color)
            .border(if (selected) 3.dp else 1.dp, if (selected) Color(0xFF121212) else Color.Black.copy(alpha = 0.12f), CircleShape),
    )
}

object PhotoTools {
    /** Loads a picked or captured image, downsized so wardrobe photos stay small on disk. */
    fun load(context: android.content.Context, uri: Uri, maxSide: Int = 1280): Bitmap? = runCatching {
        val bounds = BitmapFactory.Options().apply { inJustDecodeBounds = true }
        context.contentResolver.openInputStream(uri)?.use { BitmapFactory.decodeStream(it, null, bounds) }
        var sample = 1
        while (max(bounds.outWidth, bounds.outHeight) / sample > maxSide * 2) sample *= 2
        val bmp = context.contentResolver.openInputStream(uri)?.use { BitmapFactory.decodeStream(it, null, BitmapFactory.Options().apply { inSampleSize = sample }) }
            ?: return@runCatching null
        val scale = maxSide.toFloat() / max(bmp.width, bmp.height)
        if (scale < 1f) Bitmap.createScaledBitmap(bmp, (bmp.width * scale).toInt(), (bmp.height * scale).toInt(), true) else bmp
    }.getOrNull()

    fun jpeg(bitmap: Bitmap): ByteArray = ByteArrayOutputStream().also { bitmap.compress(Bitmap.CompressFormat.JPEG, 85, it) }.toByteArray()

    /**
     * The garment's main colour: looks at the middle of the photo (where the item usually is),
     * ignores pixels that match the border (the background), and snaps to the nearest named colour.
     */
    fun dominantSwatch(bitmap: Bitmap): Swatch {
        val small = Bitmap.createScaledBitmap(bitmap, 48, 48, true)
        val border = mutableListOf<Int>()
        for (i in 0 until 48) { border += small.getPixel(i, 0); border += small.getPixel(i, 47); border += small.getPixel(0, i); border += small.getPixel(47, i) }
        val bg = average(border)
        val counts = mutableMapOf<Swatch, Int>()
        for (y in 10 until 38) for (x in 10 until 38) {
            val p = small.getPixel(x, y)
            if (distance(p, bg) < 0.12) continue
            val s = nearest(p)
            counts[s] = (counts[s] ?: 0) + 1
        }
        return counts.maxByOrNull { it.value }?.key ?: nearest(small.getPixel(24, 24))
    }

    private fun average(pixels: List<Int>): Int {
        val r = pixels.sumOf { android.graphics.Color.red(it) } / pixels.size
        val g = pixels.sumOf { android.graphics.Color.green(it) } / pixels.size
        val b = pixels.sumOf { android.graphics.Color.blue(it) } / pixels.size
        return android.graphics.Color.rgb(r, g, b)
    }

    private fun distance(a: Int, b: Int): Double {
        val dr = (android.graphics.Color.red(a) - android.graphics.Color.red(b)) / 255.0
        val dg = (android.graphics.Color.green(a) - android.graphics.Color.green(b)) / 255.0
        val db = (android.graphics.Color.blue(a) - android.graphics.Color.blue(b)) / 255.0
        return sqrt(dr * dr + dg * dg + db * db)
    }

    fun nearest(pixel: Int): Swatch = WardrobeStore.swatches.minBy { s ->
        val c = s.color
        val dr = android.graphics.Color.red(pixel) / 255.0 - c.red
        val dg = android.graphics.Color.green(pixel) / 255.0 - c.green
        val db = android.graphics.Color.blue(pixel) / 255.0 - c.blue
        // Weighted for how eyes see colour.
        2 * dr * dr + 4 * dg * dg + 3 * db * db
    }
}
