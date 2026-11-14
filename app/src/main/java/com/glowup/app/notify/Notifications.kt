package com.glowup.app.notify

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.media.AudioAttributes
import android.media.RingtoneManager
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import com.glowup.app.MainActivity
import com.glowup.app.R

object Notifications {
    const val CHANNEL_REMINDERS = "reminders"
    const val CHANNEL_ALARMS = "alarms_v1"
    const val CHANNEL_PLAYBACK = "playback"
    const val EXTRA_ROUTE = "route"

    fun createChannels(context: Context) {
        val manager = context.getSystemService(NotificationManager::class.java)
        manager.createNotificationChannel(
            NotificationChannel(CHANNEL_REMINDERS, "Reminders", NotificationManager.IMPORTANCE_DEFAULT).apply {
                description = "Your routine and habit reminders"
            }
        )
        manager.createNotificationChannel(
            NotificationChannel(CHANNEL_ALARMS, "Sleep alarms", NotificationManager.IMPORTANCE_HIGH).apply {
                description = "Bedtime and wake-up alarms"
                setSound(
                    RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM),
                    AudioAttributes.Builder().setUsage(AudioAttributes.USAGE_ALARM).build()
                )
                enableVibration(true)
            }
        )
        manager.createNotificationChannel(
            NotificationChannel(CHANNEL_PLAYBACK, "Sleep sounds", NotificationManager.IMPORTANCE_LOW).apply {
                description = "Shown while a sleep sound is playing"
                setSound(null, null)
            }
        )
    }

    fun canPost(context: Context): Boolean = Build.VERSION.SDK_INT < 33 ||
        ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED

    fun openAppIntent(context: Context, route: String?, requestCode: Int): PendingIntent {
        val intent = Intent(context, MainActivity::class.java)
            .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP)
            .apply { route?.let { putExtra(EXTRA_ROUTE, it) } }
        return PendingIntent.getActivity(context, requestCode, intent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)
    }

    fun post(context: Context, id: Int, channel: String, title: String, text: String, route: String? = null) {
        if (!canPost(context)) return
        val notification = NotificationCompat.Builder(context, channel)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle(title)
            .setContentText(text)
            .setStyle(NotificationCompat.BigTextStyle().bigText(text))
            .setContentIntent(openAppIntent(context, route, id))
            .setAutoCancel(true)
            .setPriority(if (channel == CHANNEL_ALARMS) NotificationCompat.PRIORITY_HIGH else NotificationCompat.PRIORITY_DEFAULT)
            .setCategory(if (channel == CHANNEL_ALARMS) NotificationCompat.CATEGORY_ALARM else NotificationCompat.CATEGORY_REMINDER)
            .build()
        @Suppress("MissingPermission")
        NotificationManagerCompat.from(context).notify(id, notification)
    }
}
