package com.glowup.app.notify

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import com.glowup.app.ui.data.FocusStore
import com.glowup.app.ui.data.ReminderStore
import com.glowup.app.ui.data.program.ProgramStore
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
    const val KIND_FOCUS = "focus"
    const val KIND_LEARN = "learn"
    const val KIND_MIDNIGHT = "midnight"
    const val KIND_WORKOUT = "workout"
    const val KIND_WEIGH = "weigh"
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
        KIND_FOCUS -> 9003
        KIND_LEARN -> 9004
        KIND_MIDNIGHT -> 9005
        KIND_WORKOUT -> 9100 + id
        KIND_WEIGH -> 9110
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
        fun scheduleAt(kind: String, id: Int, trigger: Long, alarmClock: Boolean = false) {
            val intent = pendingIntent(context, kind, id, PendingIntent.FLAG_UPDATE_CURRENT)!!
            when {
                alarmClock && canScheduleExact(context) ->
                    manager.setAlarmClock(AlarmManager.AlarmClockInfo(trigger, Notifications.openAppIntent(context, "sleep", WAKE_CODE)), intent)
                canScheduleExact(context) -> manager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, trigger, intent)
                else -> manager.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, trigger, intent)
            }
            scheduled += "$kind:$id"
        }
        fun schedule(kind: String, id: Int, schedule: Schedule, alarmClock: Boolean = false) =
            scheduleAt(kind, id, schedule.nextAfter(now).atZone(ZoneId.systemDefault()).toInstant().toEpochMilli(), alarmClock)

        ReminderStore.items.filter { it.enabled }.forEach { schedule(KIND_REMINDER, it.id, it.schedule) }
        RoutineStore.items.forEach { routine ->
            routine.reminderTime?.let { schedule(KIND_ROUTINE, routine.id, Schedule.Daily(it)) }
        }
        if (SleepStore.bedtimeAlarmOn) schedule(KIND_BEDTIME, 0, Schedule.Daily(SleepStore.bedtime))
        if (SleepStore.wakeAlarmOn) schedule(KIND_WAKE, 0, Schedule.Daily(SleepStore.wakeTime), alarmClock = true)
        FocusStore.active?.endAtMillis?.let { scheduleAt(KIND_FOCUS, 0, it) }
        val learnTime = ProgramStore.reminderTime
        if (learnTime != null && !ProgramStore.isFinished) schedule(KIND_LEARN, 0, Schedule.Daily(learnTime))
        val fitness = com.glowup.app.ui.data.fitness.FitnessStore
        if (fitness.setUp) {
            fitness.reminder?.let { time -> fitness.days.forEach { day -> schedule(KIND_WORKOUT, day.value, Schedule.Weekly(day, time)) } }
            if (fitness.weighReminder) schedule(KIND_WEIGH, 0, Schedule.Weekly(fitness.weighDay, java.time.LocalTime.of(8, 0)))
        }
        // Home-screen widgets show "today", so refresh them just after midnight.
        if (com.glowup.app.widget.Widgets.anyPlaced(context)) {
            schedule(KIND_MIDNIGHT, 0, Schedule.Daily(java.time.LocalTime.of(0, 1)))
        }

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
