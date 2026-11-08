package com.glowup.app.ui.components

import android.content.Context
import android.content.Intent
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.FileProvider
import androidx.navigation.NavHostController
import com.glowup.app.audio.SleepSoundService
import com.glowup.app.billing.Billing
import com.glowup.app.ui.data.Storage
import com.glowup.app.ui.data.Stores
import java.io.File
import java.time.LocalDate

/** Shares everything Glow-Up has saved as one readable JSON file. */
fun exportMyData(context: Context) {
    val dir = File(context.cacheDir, "exports").apply { mkdirs() }
    dir.listFiles()?.forEach { it.delete() } // never leave older exports lying around
    val file = File(dir, "glowup-data-${LocalDate.now()}.json")
    file.writeText(Storage.exportJson())
    val uri = FileProvider.getUriForFile(context, "${context.packageName}.files", file)
    val share = Intent(Intent.ACTION_SEND)
        .setType("application/json")
        .putExtra(Intent.EXTRA_STREAM, uri)
        .putExtra(Intent.EXTRA_SUBJECT, "My Glow-Up data")
        .addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
    context.startActivity(Intent.createChooser(share, "Export my data"))
}

@Composable
fun DeleteEverythingDialog(navController: NavHostController, onDismiss: () -> Unit) {
    val context = LocalContext.current
    GlowUpDialog(
        onDismissRequest = onDismiss,
        title = "Delete all your data?",
        message = "This permanently deletes your profile, routines, moods, sleep log, notes, wardrobe, photos, reminders and progress from this phone. This can't be undone.",
        secondaryText = "Cancel",
        primaryText = "Delete",
        primaryDestructive = true,
        onPrimary = {
            onDismiss()
            SleepSoundService.stop(context)
            File(context.cacheDir, "exports").deleteRecursively()
            Stores.deleteAll()
            Billing.refreshPurchases()
            navController.navigate("onboarding") { popUpTo(navController.graph.id) { inclusive = true } }
        },
    )
}
