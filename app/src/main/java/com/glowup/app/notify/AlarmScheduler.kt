package com.glowup.app.notify

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import com.glowup.app.ui.data.ReminderStore
import com.glowup.app.ui.data.RoutineStore
import com.glowup.app.ui.data.Schedule
import com.glowup.app.ui.data.SleepStore
import java.time.LocalDateTime
import java.time.ZoneId

/** Keeps AlarmManager in sync with every enabled reminder, routine reminder and sleep alarm. */
object AlarmScheduler {
    const val KIND_REMINDER = "reminder"
    const val KIND_ROUTINE = "routine"
    const val KIND_BEDTIME = "bedtime"
    const val KIND_WAKE = "wake"
    const val EXTRA_KIND = "kind"
    const val EXTRA_ID = "id"

    private const val PREFS = "glowup_alarms"
    private const val KEY_CODES = "codes"
    private const val BEDTIME_CODE = 9001
    private const val WAKE_CODE = 9002

    fun requestCode(kind: String, id: Int) = when (kind) {
        KIND_REMINDER -> 1000 + id
        KIND_ROUTINE -> 5000 + id
        KIND_BEDTIME -> BEDTIME_CODE
        else -> WAKE_CODE
    }

    fun canScheduleExact(context: Context): Boolean {
        val manager = context.getSystemService(AlarmManager::class.java)
        return Build.VERSION.SDK_INT < 31 || manager.canScheduleExactAlarms()
    }

    fun syncAll(context: Context, now: LocalDateTime = LocalDateTime.now()) {
        val manager = context.getSystemService(AlarmManager::class.java)
        val prefs = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)

        prefs.getStringSet(KEY_CODES, emptySet())!!.forEach { code ->
            val (kind, id) = code.split(":").let { it[0] to it[1].toInt() }
            pendingIntent(context, kind, id, PendingIntent.FLAG_NO_CREATE)?.let {
                manager.cancel(it)
                it.cancel()
            }
        }

        val scheduled = mutableSetOf<String>()
        fun schedule(kind: String, id: Int, schedule: Schedule, alarmClock: Boolean = false) {
            val trigger = schedule.nextAfter(now).atZone(ZoneId.systemDefault()).toInstant().toEpochMilli()
            val intent = pendingIntent(context, kind, id, PendingIntent.FLAG_UPDATE_CURRENT)!!
            when {
                alarmClock && canScheduleExact(context) ->
                    manager.setAlarmClock(AlarmManager.AlarmClockInfo(trigger, Notifications.openAppIntent(context, "sleep", WAKE_CODE)), intent)
                canScheduleExact(context) -> manager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, trigger, intent)
                else -> manager.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, trigger, intent)
            }
            scheduled += "$kind:$id"
        }

        ReminderStore.items.filter { it.enabled }.forEach { schedule(KIND_REMINDER, it.id, it.schedule) }
        RoutineStore.items.forEach { routine ->
            routine.reminderTime?.let { schedule(KIND_ROUTINE, routine.id, Schedule.Daily(it)) }
        }
        if (SleepStore.bedtimeAlarmOn) schedule(KIND_BEDTIME, 0, Schedule.Daily(SleepStore.bedtime))
        if (SleepStore.wakeAlarmOn) schedule(KIND_WAKE, 0, Schedule.Daily(SleepStore.wakeTime), alarmClock = true)

        prefs.edit().putStringSet(KEY_CODES, scheduled).apply()
    }

    private fun pendingIntent(context: Context, kind: String, id: Int, flag: Int): PendingIntent? {
        val intent = Intent(context, ReminderReceiver::class.java)
            .setAction("com.glowup.app.ALARM.$kind")
            .putExtra(EXTRA_KIND, kind)
            .putExtra(EXTRA_ID, id)
        return PendingIntent.getBroadcast(context, requestCode(kind, id), intent, flag or PendingIntent.FLAG_IMMUTABLE)
    }
}
