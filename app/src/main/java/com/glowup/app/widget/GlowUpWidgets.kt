package com.glowup.app.widget

import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.glance.GlanceId
import androidx.glance.GlanceModifier
import androidx.glance.Image
import androidx.glance.ImageProvider
import androidx.glance.action.ActionParameters
import androidx.glance.action.actionParametersOf
import androidx.glance.action.clickable
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.GlanceAppWidgetReceiver
import androidx.glance.appwidget.action.ActionCallback
import androidx.glance.appwidget.action.actionRunCallback
import androidx.glance.appwidget.lazy.LazyColumn
import androidx.glance.appwidget.lazy.items
import androidx.glance.appwidget.provideContent
import androidx.glance.background
import androidx.glance.layout.Alignment
import androidx.glance.layout.Box
import androidx.glance.layout.Column
import androidx.glance.layout.Row
import androidx.glance.layout.Spacer
import androidx.glance.layout.fillMaxSize
import androidx.glance.layout.fillMaxWidth
import androidx.glance.layout.height
import androidx.glance.layout.padding
import androidx.glance.layout.size
import androidx.glance.layout.width
import androidx.glance.text.FontWeight
import androidx.glance.text.Text
import androidx.glance.text.TextDecoration
import androidx.glance.text.TextStyle
import com.glowup.app.R
import com.glowup.app.ui.data.FocusStore
import com.glowup.app.ui.data.MoodStore
import com.glowup.app.ui.data.ProgressStore
import com.glowup.app.ui.data.RoutineStore
import com.glowup.app.ui.data.SleepLogStore
import com.glowup.app.ui.data.SleepMath
import com.glowup.app.ui.data.DebtLevel
import com.glowup.app.ui.data.formatClock
import com.glowup.app.ui.data.formatDuration
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId

// ---------- Today's routines: tick off without opening the app ----------

private val RoutineIdKey = ActionParameters.Key<Int>("routineId")

class ToggleRoutineAction : ActionCallback {
    override suspend fun onAction(context: Context, glanceId: GlanceId, parameters: ActionParameters) {
        val id = parameters[RoutineIdKey] ?: return
        withContext(Dispatchers.Main) {
            ProgressStore.applyFreezes()
            RoutineStore.toggleToday(id)
        }
        Widgets.updateNow(context)
    }
}

class TodayWidget : GlanceAppWidget() {
    override suspend fun provideGlance(context: Context, id: GlanceId) {
        provideContent { TodayContent(context) }
    }
}

@Composable
private fun TodayContent(context: Context) {
    val today = LocalDate.now()
    val routines = RoutineStore.items.toList()
    val done = routines.count { it.isDoneOn(today) }
    val streak = ProgressStore.streakDays(today)
    Column(
        modifier = GlanceModifier.fillMaxSize().background(ImageProvider(R.drawable.widget_bg_card)).padding(14.dp, 14.dp, 14.dp, 6.dp),
    ) {
        Row(
            modifier = GlanceModifier.fillMaxWidth().padding(start = 4.dp, end = 4.dp, bottom = 10.dp).clickable(openApp(context, "routines")),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(modifier = GlanceModifier.defaultWeight()) {
                Text("Today", style = style(17.sp, WidgetColors.ink, FontWeight.Bold))
                Text(
                    if (routines.isEmpty()) "No routines yet" else if (done == routines.size) "All done — nice work" else "$done of ${routines.size} done",
                    style = style(12.sp, WidgetColors.secondary),
                )
            }
            if (streak > 0) {
                Image(ImageProvider(R.drawable.ic_flame), contentDescription = null, modifier = GlanceModifier.size(16.dp))
                Spacer(GlanceModifier.width(3.dp))
                Text("$streak", style = style(15.sp, WidgetColors.ink, FontWeight.Bold))
            }
        }
        if (routines.isEmpty()) {
            Box(
                modifier = GlanceModifier.fillMaxWidth().background(ImageProvider(R.drawable.widget_row)).padding(14.dp).clickable(openApp(context, "add_routine")),
            ) {
                Text("Tap to add your first routine", style = style(13.sp, WidgetColors.ink, FontWeight.Medium))
            }
        } else {
            LazyColumn {
                items(routines, itemId = { it.id.toLong() }) { routine ->
                    val isDone = routine.isDoneOn(today)
                    Column {
                        Row(
                            modifier = GlanceModifier
                                .fillMaxWidth()
                                .background(ImageProvider(R.drawable.widget_row))
                                .padding(12.dp, 10.dp)
                                .clickable(actionRunCallback<ToggleRoutineAction>(actionParametersOf(RoutineIdKey to routine.id))),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Image(
                                ImageProvider(if (isDone) R.drawable.widget_check_on else R.drawable.widget_check_off),
                                contentDescription = if (isDone) "Done" else "Not done",
                                modifier = GlanceModifier.size(22.dp),
                            )
                            Spacer(GlanceModifier.width(10.dp))
                            Text(
                                routine.title,
                                maxLines = 1,
                                style = TextStyle(
                                    fontSize = 13.5.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = if (isDone) WidgetColors.secondary else WidgetColors.ink,
                                    textDecoration = if (isDone) TextDecoration.LineThrough else TextDecoration.None,
                                ),
                            )
                        }
                        Spacer(GlanceModifier.height(6.dp))
                    }
                }
            }
        }
    }
}

class TodayWidgetReceiver : GlanceAppWidgetReceiver() {
    override val glanceAppWidget: GlanceAppWidget = TodayWidget()
}

// ---------- Streak & mood ----------

class StreakWidget : GlanceAppWidget() {
    override suspend fun provideGlance(context: Context, id: GlanceId) {
        provideContent { StreakContent(context) }
    }
}

@Composable
private fun StreakContent(context: Context) {
    val today = LocalDate.now()
    val streak = ProgressStore.streakDays(today)
    val freezes = ProgressStore.freezes
    val mood = MoodStore.moodOn(today)
    val activeToday = ProgressStore.isActive(today)
    Column(
        modifier = GlanceModifier.fillMaxSize().background(ImageProvider(R.drawable.widget_bg_ink)).padding(14.dp).clickable(openApp(context, "streak_detail")),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Image(ImageProvider(R.drawable.ic_flame), contentDescription = null, modifier = GlanceModifier.size(22.dp))
            Spacer(GlanceModifier.width(4.dp))
            Text("$streak", style = style(28.sp, WidgetColors.onDark, FontWeight.Bold))
        }
        Text("day streak", style = style(12.sp, WidgetColors.onDarkMuted))
        Spacer(GlanceModifier.height(4.dp))
        Row(verticalAlignment = Alignment.CenterVertically) {
            Image(ImageProvider(R.drawable.widget_snowflake), contentDescription = null, modifier = GlanceModifier.size(12.dp))
            Spacer(GlanceModifier.width(4.dp))
            Text("$freezes freeze${if (freezes == 1) "" else "s"}", style = style(11.sp, WidgetColors.onDarkMuted, FontWeight.Medium))
        }
        Spacer(GlanceModifier.defaultWeight())
        if (mood == null) {
            Box(
                modifier = GlanceModifier.fillMaxWidth().background(ImageProvider(R.drawable.widget_pill_white)).padding(10.dp, 8.dp).clickable(openApp(context, "mood_checkin")),
                contentAlignment = Alignment.Center,
            ) {
                Text(if (activeToday) "Log your mood" else "Check in today", style = style(12.sp, androidx.glance.unit.ColorProvider(androidx.compose.ui.graphics.Color(0xFF121212)), FontWeight.Bold))
            }
        } else {
            Row(
                modifier = GlanceModifier.fillMaxWidth().background(ImageProvider(R.drawable.widget_pill_glass)).padding(10.dp, 6.dp).clickable(openApp(context, "mood_insights")),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                MoodStore.iconFor(mood)?.let {
                    Image(ImageProvider(it), contentDescription = null, modifier = GlanceModifier.size(20.dp))
                    Spacer(GlanceModifier.width(6.dp))
                }
                Text(mood, style = style(12.sp, WidgetColors.onDark, FontWeight.Bold), maxLines = 1)
            }
        }
    }
}

class StreakWidgetReceiver : GlanceAppWidgetReceiver() {
    override val glanceAppWidget: GlanceAppWidget = StreakWidget()
}

// ---------- Focus ----------

class StartFocusAction : ActionCallback {
    override suspend fun onAction(context: Context, glanceId: GlanceId, parameters: ActionParameters) {
        withContext(Dispatchers.Main) {
            FocusStore.finishIfDue()
            if (FocusStore.active == null) FocusStore.start(FocusStore.lastMinutes, FocusStore.lastLabel)
        }
        Widgets.updateNow(context)
    }
}

class FocusWidget : GlanceAppWidget() {
    override suspend fun provideGlance(context: Context, id: GlanceId) {
        provideContent { FocusContent(context) }
    }
}

@Composable
private fun FocusContent(context: Context) {
    FocusStore.finishIfDue()
    val active = FocusStore.active
    val minutesToday = FocusStore.minutesOn(LocalDate.now())
    Column(
        modifier = GlanceModifier.fillMaxSize().background(ImageProvider(R.drawable.widget_bg_blue)).padding(14.dp).clickable(openApp(context, "focus")),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Image(ImageProvider(R.drawable.ic_goal), contentDescription = null, modifier = GlanceModifier.size(18.dp))
            Spacer(GlanceModifier.width(6.dp))
            Text("Focus", style = style(13.sp, WidgetColors.navy, FontWeight.Bold))
        }
        Spacer(GlanceModifier.height(6.dp))
        when {
            active == null -> {
                Text("$minutesToday min", style = style(24.sp, WidgetColors.ink, FontWeight.Bold))
                Text("focused today", style = style(12.sp, WidgetColors.secondary))
            }
            active.isRunning -> {
                val endsAt = Instant.ofEpochMilli(active.endAtMillis!!).atZone(ZoneId.systemDefault()).toLocalTime()
                Text("Focusing", style = style(22.sp, WidgetColors.ink, FontWeight.Bold))
                Text("${active.label} · until ${formatClock(endsAt)}", style = style(12.sp, WidgetColors.secondary), maxLines = 2)
            }
            else -> {
                Text("Paused", style = style(22.sp, WidgetColors.ink, FontWeight.Bold))
                Text("${active.label} · ${active.remaining(System.currentTimeMillis()) / 60_000 + 1} min left", style = style(12.sp, WidgetColors.secondary), maxLines = 2)
            }
        }
        Spacer(GlanceModifier.defaultWeight())
        Box(
            modifier = GlanceModifier
                .fillMaxWidth()
                .background(ImageProvider(R.drawable.widget_pill_ink))
                .padding(10.dp, 8.dp)
                .clickable(if (active == null) actionRunCallback<StartFocusAction>() else openApp(context, "focus")),
            contentAlignment = Alignment.Center,
        ) {
            Text(
                if (active == null) "Start ${FocusStore.lastMinutes} min" else "Open timer",
                style = style(12.sp, WidgetColors.onPill, FontWeight.Bold),
            )
        }
    }
}

class FocusWidgetReceiver : GlanceAppWidgetReceiver() {
    override val glanceAppWidget: GlanceAppWidget = FocusWidget()
}

// ---------- Sleep ----------

class SleepWidget : GlanceAppWidget() {
    override suspend fun provideGlance(context: Context, id: GlanceId) {
        provideContent { SleepContent(context) }
    }
}

@Composable
private fun SleepContent(context: Context) {
    val today = LocalDate.now()
    val debt = SleepLogStore.debtMinutes(today)
    val lastNight = SleepLogStore.on(today)
    Column(
        modifier = GlanceModifier.fillMaxSize().background(ImageProvider(R.drawable.widget_bg_purple)).padding(14.dp).clickable(openApp(context, "sleep_log")),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Image(ImageProvider(R.drawable.ic_moon), contentDescription = null, modifier = GlanceModifier.size(18.dp))
            Spacer(GlanceModifier.width(6.dp))
            Text("Sleep debt", style = style(13.sp, WidgetColors.purple, FontWeight.Bold))
        }
        Spacer(GlanceModifier.height(6.dp))
        if (debt == null) {
            Text("—", style = style(24.sp, WidgetColors.ink, FontWeight.Bold))
            Text("Log 3 nights to see it", style = style(12.sp, WidgetColors.secondary), maxLines = 2)
        } else {
            val level = SleepMath.level(debt)
            Text(if (debt == 0) "None" else formatDuration(debt), style = style(24.sp, WidgetColors.ink, FontWeight.Bold))
            Text(
                level.label,
                style = style(
                    12.sp,
                    when (level) {
                        DebtLevel.LOW -> WidgetColors.green
                        DebtLevel.MODERATE -> WidgetColors.amber
                        DebtLevel.HIGH -> WidgetColors.coral
                    },
                    FontWeight.Bold,
                ),
            )
        }
        Spacer(GlanceModifier.defaultWeight())
        if (lastNight == null) {
            Box(
                modifier = GlanceModifier.fillMaxWidth().background(ImageProvider(R.drawable.widget_pill_ink)).padding(10.dp, 8.dp),
                contentAlignment = Alignment.Center,
            ) {
                Text("Log last night", style = style(12.sp, WidgetColors.onPill, FontWeight.Bold))
            }
        } else {
            Text("Last night ${formatDuration(lastNight.minutes)}", style = style(12.sp, WidgetColors.ink, FontWeight.Medium), maxLines = 1)
        }
    }
}

class SleepWidgetReceiver : GlanceAppWidgetReceiver() {
    override val glanceAppWidget: GlanceAppWidget = SleepWidget()
}
