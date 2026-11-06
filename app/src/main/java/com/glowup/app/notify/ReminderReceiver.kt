package com.glowup.app.notify

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.glowup.app.ui.data.ReminderStore
import com.glowup.app.ui.data.RoutineStore
import com.glowup.app.ui.data.SleepStore
import com.glowup.app.ui.data.formatClock
import java.time.LocalDate

/** Posts the notification for a fired alarm, then schedules the next occurrence. */
class ReminderReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val kind = intent.getStringExtra(AlarmScheduler.EXTRA_KIND) ?: return
        val id = intent.getIntExtra(AlarmScheduler.EXTRA_ID, 0)
        val code = AlarmScheduler.requestCode(kind, id)

        when (kind) {
            AlarmScheduler.KIND_REMINDER -> ReminderStore.items.firstOrNull { it.id == id && it.enabled }?.let {
                Notifications.post(context, code, Notifications.CHANNEL_REMINDERS, it.title, "Your Glow-Up reminder · ${it.schedule.label}", "notifications")
            }
            AlarmScheduler.KIND_ROUTINE -> RoutineStore.find(id)
                ?.takeIf { !it.isDoneOn(LocalDate.now()) }
                ?.let {
                    Notifications.post(context, code, Notifications.CHANNEL_REMINDERS, "Time for: ${it.title}", "Tap to check it off and keep your streak going.", "routine_detail/${it.id}")
                }
            AlarmScheduler.KIND_BEDTIME -> if (SleepStore.bedtimeAlarmOn) {
                Notifications.post(
                    context, code, Notifications.CHANNEL_ALARMS,
                    "It's ${formatClock(SleepStore.bedtime)} — time for bed",
                    "Screens off, lights low. A sleep sound can help you drift off.",
                    "sleep",
                )
            }
            AlarmScheduler.KIND_WAKE -> if (SleepStore.wakeAlarmOn) {
                Notifications.post(
                    context, code, Notifications.CHANNEL_ALARMS,
                    "Good morning — it's ${formatClock(SleepStore.wakeTime)}",
                    "Time to get up. Start with a 2-minute breathing session.",
                    "morning",
                )
            }
        }

        AlarmScheduler.syncAll(context)
    }
}

class BootReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        AlarmScheduler.syncAll(context)
    }
}
