package com.glowup.app.ui.components

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.ContextCompat
import com.glowup.app.notify.Notifications

fun hasPermission(context: Context, permission: String) =
    ContextCompat.checkSelfPermission(context, permission) == PackageManager.PERMISSION_GRANTED

/** Returns a function that asks for notification permission if needed, then reports whether it's granted. */
@Composable
fun rememberNotificationPermission(onResult: (Boolean) -> Unit = {}): () -> Unit {
    val context = LocalContext.current
    val callback by rememberUpdatedState(onResult)
    val launcher = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { callback(it) }
    return remember(launcher) {
        {
            if (Notifications.canPost(context) || Build.VERSION.SDK_INT < 33) callback(true)
            else launcher.launch(Manifest.permission.POST_NOTIFICATIONS)
        }
    }
}

/** Returns a function that asks for microphone permission if needed, then reports whether it's granted. */
@Composable
fun rememberMicPermission(onResult: (Boolean) -> Unit): () -> Unit {
    val context = LocalContext.current
    val callback by rememberUpdatedState(onResult)
    val launcher = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { callback(it) }
    return remember(launcher) {
        {
            if (hasPermission(context, Manifest.permission.RECORD_AUDIO)) callback(true)
            else launcher.launch(Manifest.permission.RECORD_AUDIO)
        }
    }
}
