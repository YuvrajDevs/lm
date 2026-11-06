package com.glowup.app.ui.components

import android.annotation.SuppressLint
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext

/** Resource name for real artwork that replaces an outfit illustration, e.g. `outfit_old_money_1`. */
fun outfitArtworkName(outfitId: String) = "outfit_" + outfitId.replace('-', '_')

/** Resource name for a style guide's cover image, e.g. `guide_old_money`. */
fun guideArtworkName(guideKey: String) = "guide_" + guideKey.replace('-', '_')

/** The drawable id for [name] if that artwork has been added to the app, otherwise null. */
@SuppressLint("DiscouragedApi")
@Composable
fun artworkOrNull(name: String): Int? {
    val context = LocalContext.current
    return remember(name) {
        context.resources.getIdentifier(name, "drawable", context.packageName).takeIf { it != 0 }
    }
}
