package com.glowup.app.blocker

import android.app.AppOpsManager
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.app.usage.UsageEvents
import android.app.usage.UsageStatsManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.ServiceInfo
import android.graphics.Bitmap
import android.os.Build
import android.os.Handler
import android.os.IBinder
import android.os.Looper
import android.os.PowerManager
import android.os.Process
import android.provider.Settings
import androidx.core.app.NotificationCompat
import androidx.core.app.ServiceCompat
import androidx.core.content.ContextCompat
import androidx.core.graphics.drawable.toBitmap
import com.glowup.app.R
import com.glowup.app.notify.Notifications
import com.glowup.app.ui.data.BlockStore
import com.glowup.app.ui.data.FocusStore
import com.glowup.app.ui.data.Intervention
import com.glowup.app.ui.data.formatClock
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneId

data class InstalledApp(val packageName: String, val label: String)

object Blocker {
    const val CHANNEL = "blocker"

    fun hasUsageAccess(context: Context): Boolean {
        val ops = context.getSystemService(AppOpsManager::class.java)
        val mode = if (Build.VERSION.SDK_INT >= 29) ops.unsafeCheckOpNoThrow(AppOpsManager.OPSTR_GET_USAGE_STATS, Process.myUid(), context.packageName)
        else @Suppress("DEPRECATION") ops.checkOpNoThrow(AppOpsManager.OPSTR_GET_USAGE_STATS, Process.myUid(), context.packageName)
        return mode == AppOpsManager.MODE_ALLOWED
    }

    fun canOverlay(context: Context) = Settings.canDrawOverlays(context)

    fun ready(context: Context) = hasUsageAccess(context) && canOverlay(context)

    /** Starts or stops the watcher so it only runs while there's something to block or pause. */
    fun sync(context: Context) {
        val app = context.applicationContext
        val needed = BlockStore.needsWatcher(FocusStore.active) && ready(app)
        val intent = Intent(app, BlockerService::class.java)
        if (needed) runCatching { ContextCompat.startForegroundService(app, intent) }
        else if (BlockerService.running) app.stopService(intent)
    }

    /** Launchable apps the user can choose from (no special "all packages" permission needed). */
    fun launchableApps(context: Context): List<InstalledApp> {
        val pm = context.packageManager
        val intent = Intent(Intent.ACTION_MAIN).addCategory(Intent.CATEGORY_LAUNCHER)
        return pm.queryIntentActivities(intent, 0)
            .map { InstalledApp(it.activityInfo.packageName, it.loadLabel(pm).toString()) }
            .filter { it.packageName != context.packageName }
            .distinctBy { it.packageName }
            .sortedBy { it.label.lowercase() }
    }

    fun label(context: Context, pkg: String): String =
        runCatching { context.packageManager.getApplicationLabel(context.packageManager.getApplicationInfo(pkg, 0)).toString() }.getOrDefault(pkg)

    fun icon(context: Context, pkg: String): Bitmap? =
        runCatching { context.packageManager.getApplicationIcon(pkg).toBitmap(96, 96) }.getOrNull()

    fun goHome(context: Context) {
        context.startActivity(Intent(Intent.ACTION_MAIN).addCategory(Intent.CATEGORY_HOME).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK))
    }
}

/**
 * Watches which app is in front — only the package name — and shows the block or pause screen
 * when it matches the user's lists. Sleeps while the screen is off and stops itself when nothing
 * needs watching.
 */
class BlockerService : Service() {
    companion object {
        @Volatile var running = false
            private set
        private const val NOTIFICATION_ID = 7301
        private const val TICK_MS = 900L
    }

    private val handler = Handler(Looper.getMainLooper())
    private var lastQuery = 0L
    private var foreground: String? = null
    private var handled: String? = null
    private var homePackage: String? = null

    private val screenReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            handler.removeCallbacks(tick)
            if (intent.action == Intent.ACTION_SCREEN_ON) handler.post(tick)
        }
    }

    private val tick = object : Runnable {
        override fun run() {
            if (!BlockStore.needsWatcher(FocusStore.active) || !Blocker.ready(this@BlockerService)) {
                stopSelf()
                return
            }
            FocusStore.finishIfDue()
            check()
            handler.postDelayed(this, TICK_MS)
        }
    }

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onCreate() {
        super.onCreate()
        running = true
        homePackage = packageManager.resolveActivity(Intent(Intent.ACTION_MAIN).addCategory(Intent.CATEGORY_HOME), 0)?.activityInfo?.packageName
        val manager = getSystemService(NotificationManager::class.java)
        manager.createNotificationChannel(NotificationChannel(Blocker.CHANNEL, "App blocker", NotificationManager.IMPORTANCE_MIN).apply {
            description = "Shown while Glow-Up is blocking or pausing apps"
            setShowBadge(false)
        })
        registerReceiver(screenReceiver, IntentFilter().apply { addAction(Intent.ACTION_SCREEN_ON); addAction(Intent.ACTION_SCREEN_OFF) })
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val type = if (Build.VERSION.SDK_INT >= 34) ServiceInfo.FOREGROUND_SERVICE_TYPE_SPECIAL_USE else 0
        runCatching { ServiceCompat.startForeground(this, NOTIFICATION_ID, notification(), type) }.onFailure { stopSelf(); return START_NOT_STICKY }
        lastQuery = System.currentTimeMillis() - 10_000
        handler.removeCallbacks(tick)
        if (getSystemService(PowerManager::class.java).isInteractive) handler.post(tick)
        return START_STICKY
    }

    override fun onDestroy() {
        running = false
        handler.removeCallbacks(tick)
        runCatching { unregisterReceiver(screenReceiver) }
        super.onDestroy()
    }

    private fun notification() = NotificationCompat.Builder(this, Blocker.CHANNEL)
        .setSmallIcon(R.drawable.ic_notification)
        .setContentTitle(
            FocusStore.active?.takeIf { it.blockApps && it.isRunning }?.endAtMillis?.let {
                "Focusing until " + formatClock(Instant.ofEpochMilli(it).atZone(ZoneId.systemDefault()).toLocalTime())
            } ?: "Mindful mode is on",
        )
        .setContentText("Glow-Up will step in when you open an app on your list.")
        .setOngoing(true)
        .setSilent(true)
        .setContentIntent(Notifications.openAppIntent(this, "blocker", NOTIFICATION_ID))
        .build()

    private fun check() {
        val usage = getSystemService(UsageStatsManager::class.java)
        val now = System.currentTimeMillis()
        val events = usage.queryEvents(lastQuery, now)
        lastQuery = now - 500
        val e = UsageEvents.Event()
        while (events.hasNextEvent()) {
            events.getNextEvent(e)
            if (e.eventType == UsageEvents.Event.ACTIVITY_RESUMED) foreground = e.packageName
        }
        val pkg = foreground ?: return
        if (pkg == packageName || pkg == homePackage) {
            handled = null
            return
        }
        if (pkg == handled) return
        when (val d = BlockStore.decide(pkg, LocalDateTime.now(), now, FocusStore.active)) {
            is Intervention.Block -> {
                handled = pkg
                BlockStore.recordBlock()
                show(pkg, "block", d.untilLabel)
            }
            Intervention.Pause -> {
                handled = pkg
                BlockStore.recordPause(pkg)
                show(pkg, "pause", null)
            }
            Intervention.None -> handled = null
        }
    }

    private fun show(pkg: String, mode: String, detail: String?) {
        val intent = Intent(this, InterventionActivity::class.java)
            .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NO_ANIMATION)
            .putExtra(InterventionActivity.EXTRA_PACKAGE, pkg)
            .putExtra(InterventionActivity.EXTRA_MODE, mode)
            .putExtra(InterventionActivity.EXTRA_DETAIL, detail)
        runCatching { startActivity(intent) }
    }
}
