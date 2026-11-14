package com.glowup.app.widget

import android.appwidget.AppWidgetManager
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.TextUnit
import androidx.glance.action.Action
import androidx.glance.appwidget.action.actionStartActivity
import androidx.glance.appwidget.updateAll
import androidx.glance.color.ColorProvider
import androidx.glance.text.FontWeight
import androidx.glance.text.TextStyle
import androidx.glance.unit.ColorProvider
import com.glowup.app.MainActivity
import com.glowup.app.notify.Notifications
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

/** Colours shared by every widget. Pairs are (light, dark) so widgets follow the launcher's theme. */
internal object WidgetColors {
    val ink = ColorProvider(day = Color(0xFF121212), night = Color(0xFFF5F5F5))
    val secondary = ColorProvider(day = Color(0xFF6B6B6B), night = Color(0xFFB0B0B0))
    val onDark = ColorProvider(Color.White)
    val onDarkMuted = ColorProvider(Color.White.copy(alpha = 0.6f))
    val navy = ColorProvider(day = Color(0xFF2C4E8E), night = Color(0xFFA9C4F5))
    val purple = ColorProvider(day = Color(0xFF6B4FD1), night = Color(0xFFC4B5FF))
    val green = ColorProvider(day = Color(0xFF1E7A4F), night = Color(0xFF7FD9A8))
    val coral = ColorProvider(day = Color(0xFFD9572B), night = Color(0xFFFF9C79))
    val amber = ColorProvider(day = Color(0xFF9A7800), night = Color(0xFFFFD84D))
    val onPill = ColorProvider(day = Color.White, night = Color(0xFF121212))
}

internal fun style(size: TextUnit, color: ColorProvider, weight: FontWeight = FontWeight.Normal) =
    TextStyle(fontSize = size, fontWeight = weight, color = color)

/** Opens the app on [route]. Each route gets its own data URI so launchers keep the intents apart. */
internal fun openApp(context: Context, route: String?): Action {
    val intent = Intent(context, MainActivity::class.java)
        .setData(Uri.parse("remakeu://widget/${route ?: "home"}"))
        .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP)
    route?.let { intent.putExtra(Notifications.EXTRA_ROUTE, it) }
    return actionStartActivity(intent)
}

/** Keeps every placed widget in step with the app's data. */
object Widgets {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)
    private var pending: Job? = null

    private val receivers = listOf(
        TodayWidgetReceiver::class.java,
        StreakWidgetReceiver::class.java,
        FocusWidgetReceiver::class.java,
        SleepWidgetReceiver::class.java
    )

    fun anyPlaced(context: Context): Boolean {
        val manager = AppWidgetManager.getInstance(context)
        return receivers.any { manager.getAppWidgetIds(ComponentName(context, it)).isNotEmpty() }
    }

    /** Coalesces bursts of saves (e.g. a check-off that also awards points) into one refresh. */
    fun requestUpdate(context: Context) {
        val app = context.applicationContext
        pending?.cancel()
        pending = scope.launch {
            delay(300)
            updateNow(app)
        }
    }

    suspend fun updateNow(context: Context) {
        if (!anyPlaced(context)) return
        runCatching {
            TodayWidget().updateAll(context)
            StreakWidget().updateAll(context)
            FocusWidget().updateAll(context)
            SleepWidget().updateAll(context)
        }
    }
}
