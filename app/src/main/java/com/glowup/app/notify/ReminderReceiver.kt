package com.glowup.app.notify

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.glowup.app.ui.data.FocusStore
import com.glowup.app.ui.data.ReminderStore
import com.glowup.app.ui.data.program.ProgramStore
import com.glowup.app.ui.data.RoutineStore
import com.glowup.app.ui.data.SleepStore
import com.glowup.app.ui.data.formatClock
import java.time.LocalDate
import kotlinx.coroutines.launch

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
                    "Tap to log how you slept in one tap, then start your morning routine.",
                    "sleep_log",
                )
            }
            AlarmScheduler.KIND_FOCUS -> FocusStore.finishIfDue()?.let { minutes ->
                val streak = FocusStore.streakDays()
                Notifications.post(
                    context, code, Notifications.CHANNEL_REMINDERS,
                    "Focus complete — $minutes minutes",
                    "+${minutes / FocusStore.MINUTES_PER_POINT} points · ${if (streak == 1) "1-day" else "$streak-day"} focus streak. Take a short break.",
                    "focus",
                )
            }
            AlarmScheduler.KIND_LEARN -> {
                val next = ProgramStore.next
                if (next != null && !ProgramStore.doneToday()) {
                    val streak = ProgramStore.streakDays()
                    Notifications.post(
                        context, code, Notifications.CHANNEL_REMINDERS,
                        if (streak > 0) "Keep your $streak-day streak alive" else "Day ${next.day} is ready",
                        "${next.area.label}: ${next.title} · ${next.minutes} min",
                        "session/${next.index}",
                    )
                }
            }
        }

        if (kind == AlarmScheduler.KIND_WORKOUT) {
            val fitness = com.glowup.app.ui.data.fitness.FitnessStore
            if (!fitness.doneOn(LocalDate.now())) {
                val plan = fitness.todayPlan()
                Notifications.post(
                    context, code, Notifications.CHANNEL_REMINDERS,
                    if (plan.restDay) "Rest day — nice work this week" else "Time to train: ${plan.title}",
                    if (plan.restDay) "A 5-minute stretch keeps you loose." else "About ${plan.minutes} min · tap to start",
                    "fitness",
                )
            }
        }
        if (kind == AlarmScheduler.KIND_WEIGH) {
            if (com.glowup.app.ui.data.fitness.FitnessStore.weighInDue()) {
                Notifications.post(context, code, Notifications.CHANNEL_REMINDERS, "Weekly check-in", "Weigh in before breakfast for the most accurate trend.", "body_checkin")
            }
        }
        if (kind == AlarmScheduler.KIND_MIDNIGHT) {
            com.glowup.app.ui.data.ProgressStore.applyFreezes()
            val pending = goAsync()
            kotlinx.coroutines.CoroutineScope(kotlinx.coroutines.Dispatchers.Default).launch {
                try {
                    com.glowup.app.widget.Widgets.updateNow(context)
                } finally {
                    pending.finish()
                }
            }
        }

        AlarmScheduler.syncAll(context)
    }
}

class BootReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        AlarmScheduler.syncAll(context)
        com.glowup.app.blocker.Blocker.sync(context)
    }
}
