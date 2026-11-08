package com.glowup.app.ui.data

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import org.json.JSONObject
import java.time.DayOfWeek
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.temporal.TemporalAdjusters

/** A focus block. Running when [endAtMillis] is set; paused when [remainingMillis] is set instead. */
data class ActiveFocus(
    val label: String,
    val minutes: Int,
    val endAtMillis: Long?,
    val remainingMillis: Long?,
    /** Block the user's chosen apps while this runs. */
    val blockApps: Boolean = false,
    /** Can't be paused or ended early except through the emergency exit. */
    val strict: Boolean = false,
) {
    val isRunning: Boolean get() = endAtMillis != null

    fun remaining(nowMillis: Long): Long = endAtMillis?.let { (it - nowMillis).coerceAtLeast(0) } ?: remainingMillis ?: 0
}

/**
 * Focus timer state and history. Timing is based on wall-clock end time rather than a ticking
 * counter, so a block keeps running while the app is in the background or the screen is off.
 */
object FocusStore {
    private const val KEY = "focus"
    const val MINUTES_PER_POINT = 5

    val labels = listOf("Deep work", "Study", "Reading", "Creative", "Admin")
    val durations = listOf(15, 25, 45, 60, 90)

    var active by mutableStateOf<ActiveFocus?>(null)
        private set
    var lastLabel by mutableStateOf("Deep work")
        private set
    var lastMinutes by mutableStateOf(25)
        private set

    /** Minutes of the most recently finished block until the user has seen its celebration. */
    var justFinished by mutableStateOf<Int?>(null)
        private set

    fun acknowledgeFinished() {
        justFinished = null
    }

    val minutesByDay = mutableStateMapOf<String, Int>()
    val sessionsByDay = mutableStateMapOf<String, Int>()

    fun start(minutes: Int, label: String, nowMillis: Long = System.currentTimeMillis(), blockApps: Boolean = false, strict: Boolean = false) {
        active = ActiveFocus(label, minutes, nowMillis + minutes * 60_000L, null, blockApps, strict && blockApps)
        lastLabel = label
        lastMinutes = minutes
        changed()
    }

    fun pause(nowMillis: Long = System.currentTimeMillis()) {
        val current = active ?: return
        if (!current.isRunning || current.strict) return
        active = current.copy(endAtMillis = null, remainingMillis = current.remaining(nowMillis))
        changed()
    }

    fun resume(nowMillis: Long = System.currentTimeMillis()) {
        val current = active ?: return
        if (current.isRunning) return
        active = current.copy(endAtMillis = nowMillis + (current.remainingMillis ?: 0), remainingMillis = null)
        changed()
    }

    fun cancel() {
        active = null
        changed()
    }

    /** Credits a running block whose time is up. Returns the minutes credited, or null if nothing finished. */
    fun finishIfDue(nowMillis: Long = System.currentTimeMillis(), zone: ZoneId = ZoneId.systemDefault()): Int? {
        val current = active ?: return null
        val endAt = current.endAtMillis ?: return null
        if (nowMillis < endAt) return null
        val day = Instant.ofEpochMilli(endAt).atZone(zone).toLocalDate()
        val key = day.toString()
        minutesByDay[key] = (minutesByDay[key] ?: 0) + current.minutes
        sessionsByDay[key] = (sessionsByDay[key] ?: 0) + 1
        ProgressStore.award(current.minutes / MINUTES_PER_POINT, day)
        active = null
        justFinished = current.minutes
        changed()
        return current.minutes
    }

    fun minutesOn(date: LocalDate) = minutesByDay[date.toString()] ?: 0

    fun streakDays(today: LocalDate = LocalDate.now()) =
        ProgressStore.streakFrom(minutesByDay.filterValues { it > 0 }.keys.mapTo(HashSet()) { LocalDate.parse(it) }, today)

    /** Minutes for each day Mon..Sun of the week containing [today]. */
    fun week(today: LocalDate = LocalDate.now()): List<Int> {
        val monday = today.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY))
        return List(7) { minutesOn(monday.plusDays(it.toLong())) }
    }

    val totalMinutes: Int get() = minutesByDay.values.sum()
    val totalSessions: Int get() = sessionsByDay.values.sum()

    private fun changed() {
        save()
        Storage.onScheduleChanged()
    }

    /** Clears past focus history; a block that's running now keeps going. */
    fun clearHistory() {
        minutesByDay.clear()
        sessionsByDay.clear()
        save()
    }

    fun load() {
        minutesByDay.clear()
        sessionsByDay.clear()
        val json = Storage.read(KEY)?.let(::JSONObject)
        json?.optJSONObject("minutes")?.let { m -> m.keys().forEach { minutesByDay[it] = m.getInt(it) } }
        json?.optJSONObject("sessions")?.let { s -> s.keys().forEach { sessionsByDay[it] = s.getInt(it) } }
        lastLabel = json?.optString("lastLabel")?.ifBlank { null } ?: "Deep work"
        lastMinutes = json?.optInt("lastMinutes", 25) ?: 25
        active = json?.optJSONObject("active")?.let {
            ActiveFocus(
                it.getString("label"),
                it.getInt("minutes"),
                if (it.isNull("endAt")) null else it.getLong("endAt"),
                if (it.isNull("remaining")) null else it.getLong("remaining"),
                it.optBoolean("block"),
                it.optBoolean("strict"),
            )
        }
    }

    private fun save() {
        val json = JSONObject()
            .put("minutes", JSONObject(minutesByDay.toMap()))
            .put("sessions", JSONObject(sessionsByDay.toMap()))
            .put("lastLabel", lastLabel)
            .put("lastMinutes", lastMinutes)
            .put(
                "active",
                active?.let {
                    JSONObject()
                        .put("label", it.label)
                        .put("minutes", it.minutes)
                        .put("endAt", it.endAtMillis ?: JSONObject.NULL)
                        .put("remaining", it.remainingMillis ?: JSONObject.NULL)
                        .put("block", it.blockApps)
                        .put("strict", it.strict)
                } ?: JSONObject.NULL,
            )
        Storage.write(KEY, json.toString())
    }
}
