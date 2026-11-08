package com.glowup.app.ui.data

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import org.json.JSONArray
import org.json.JSONObject
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime

/** A recurring window when chosen apps are blocked, e.g. weekdays 9:00–17:00. */
data class BlockSchedule(val id: Int, val label: String, val days: Set<DayOfWeek>, val start: LocalTime, val end: LocalTime, val enabled: Boolean = true) {
    /** Handles windows that run past midnight, like 23:00–07:00. */
    fun isActive(at: LocalDateTime): Boolean {
        if (!enabled) return false
        val t = at.toLocalTime()
        return if (start <= end) at.dayOfWeek in days && t >= start && t < end
        else (at.dayOfWeek in days && t >= start) || (at.minusDays(1).dayOfWeek in days && t < end)
    }
}

enum class BlockReason { FOCUS, SCHEDULE }

/** What should happen when [packageName] comes to the foreground. */
sealed interface Intervention {
    data class Block(val reason: BlockReason, val untilLabel: String) : Intervention
    data object Pause : Intervention
    data object None : Intervention
}

data class DayStats(val blocks: Int = 0, val pauses: Int = 0, val resisted: Int = 0)

/**
 * The app blocker's rules. Only package names and daily counts are ever stored — never what you
 * did inside an app or a usage history.
 */
object BlockStore {
    private const val KEY = "blocker"
    const val FREE_MAX_APPS = 3
    /** Rough time a resisted app open would otherwise have cost, for the "time saved" estimate. */
    const val MINUTES_PER_RESISTED_OPEN = 7

    val focusApps = mutableStateListOf<String>()
    val pauseApps = mutableStateListOf<String>()
    val schedules = mutableStateListOf<BlockSchedule>()
    var scheduleApps = mutableStateListOf<String>()
        private set
    val stats = mutableStateMapOf<String, DayStats>()
    /** Package → per-day open counts for paused apps (only today's is kept). */
    val opensToday = mutableStateMapOf<String, Int>()
    private var opensDay: String = LocalDate.now().toString()

    /** Package → epoch millis until which the pause screen is skipped ("open for 5 minutes"). */
    private val grace = mutableMapOf<String, Long>()

    var pauseSeconds by mutableStateOf(5)
        private set

    fun decide(packageName: String, now: LocalDateTime, nowMillis: Long, focus: ActiveFocus?): Intervention {
        if (focus != null && focus.isRunning && focus.blockApps && packageName in focusApps) {
            return Intervention.Block(BlockReason.FOCUS, "until your focus block ends")
        }
        schedules.firstOrNull { it.isActive(now) && packageName in scheduleApps }?.let {
            return Intervention.Block(BlockReason.SCHEDULE, "${it.label} · until ${formatClock(it.end)}")
        }
        if (packageName in pauseApps && (grace[packageName] ?: 0L) < nowMillis) return Intervention.Pause
        return Intervention.None
    }

    /** Whether the background watcher needs to run at all. */
    fun needsWatcher(focus: ActiveFocus?): Boolean =
        (focus?.isRunning == true && focus.blockApps && focusApps.isNotEmpty()) ||
            (schedules.any { it.enabled } && scheduleApps.isNotEmpty()) ||
            pauseApps.isNotEmpty()

    fun allow(packageName: String, minutes: Int, nowMillis: Long = System.currentTimeMillis()) {
        grace[packageName] = nowMillis + minutes * 60_000L
    }

    fun graceEndsAt(packageName: String): Long? = grace[packageName]

    // ---- Stats ----

    private fun bump(today: LocalDate, change: (DayStats) -> DayStats) {
        val key = today.toString()
        stats[key] = change(stats[key] ?: DayStats())
        save()
    }

    fun recordBlock(today: LocalDate = LocalDate.now()) = bump(today) { it.copy(blocks = it.blocks + 1) }

    fun recordPause(packageName: String, today: LocalDate = LocalDate.now()) {
        if (opensDay != today.toString()) {
            opensToday.clear()
            opensDay = today.toString()
        }
        opensToday[packageName] = (opensToday[packageName] ?: 0) + 1
        bump(today) { it.copy(pauses = it.pauses + 1) }
    }

    fun recordResisted(today: LocalDate = LocalDate.now()) = bump(today) { it.copy(resisted = it.resisted + 1) }

    fun weekTotals(today: LocalDate = LocalDate.now()): DayStats =
        (0..6).map { stats[today.minusDays(it.toLong()).toString()] ?: DayStats() }
            .fold(DayStats()) { a, b -> DayStats(a.blocks + b.blocks, a.pauses + b.pauses, a.resisted + b.resisted) }

    fun minutesSaved(s: DayStats) = (s.blocks + s.resisted) * MINUTES_PER_RESISTED_OPEN

    // ---- Editing ----

    fun appLimitReached(list: List<String>) = !ProStore.isPro && list.size >= FREE_MAX_APPS

    fun toggleFocusApp(pkg: String): Boolean = toggle(focusApps, pkg)
    fun togglePauseApp(pkg: String): Boolean = toggle(pauseApps, pkg)
    fun toggleScheduleApp(pkg: String): Boolean = toggle(scheduleApps, pkg)

    /** Returns false when adding would go over the free limit. */
    private fun toggle(list: MutableList<String>, pkg: String): Boolean {
        if (pkg in list) list.remove(pkg) else {
            if (appLimitReached(list)) return false
            list.add(pkg)
        }
        changed()
        return true
    }

    fun addSchedule(label: String, days: Set<DayOfWeek>, start: LocalTime, end: LocalTime) {
        val id = (schedules.maxOfOrNull { it.id } ?: 0) + 1
        schedules.add(BlockSchedule(id, label.ifBlank { "Blocked time" }, days, start, end))
        changed()
    }

    fun setScheduleEnabled(id: Int, enabled: Boolean) {
        val i = schedules.indexOfFirst { it.id == id }
        if (i >= 0) schedules[i] = schedules[i].copy(enabled = enabled)
        changed()
    }

    fun removeSchedule(id: Int) {
        schedules.removeAll { it.id == id }
        changed()
    }

    fun updatePauseSeconds(seconds: Int) {
        pauseSeconds = seconds.coerceIn(3, 15)
        save()
    }

    fun clearAll() {
        Storage.remove(KEY)
        load()
        Storage.onScheduleChanged()
    }

    private fun changed() {
        save()
        Storage.onScheduleChanged()
    }

    fun load() {
        focusApps.clear(); pauseApps.clear(); schedules.clear(); scheduleApps.clear(); stats.clear(); opensToday.clear(); grace.clear()
        val json = Storage.read(KEY)?.let(::JSONObject)
        json?.optJSONArray("focus")?.strings()?.let(focusApps::addAll)
        json?.optJSONArray("pause")?.strings()?.let(pauseApps::addAll)
        json?.optJSONArray("scheduleApps")?.strings()?.let(scheduleApps::addAll)
        pauseSeconds = json?.optInt("pauseSeconds", 5) ?: 5
        json?.optJSONArray("schedules")?.objects()?.forEach { s ->
            schedules.add(
                BlockSchedule(
                    s.getInt("id"), s.getString("label"),
                    s.getJSONArray("days").strings().mapNotNull { runCatching { DayOfWeek.valueOf(it) }.getOrNull() }.toSet(),
                    LocalTime.parse(s.getString("start")), LocalTime.parse(s.getString("end")), s.optBoolean("enabled", true),
                ),
            )
        }
        json?.optJSONObject("stats")?.let { st ->
            st.keys().forEach { d ->
                val o = st.getJSONObject(d)
                stats[d] = DayStats(o.optInt("b"), o.optInt("p"), o.optInt("r"))
            }
        }
        opensDay = json?.optString("opensDay")?.ifBlank { null } ?: LocalDate.now().toString()
        if (opensDay == LocalDate.now().toString()) {
            json?.optJSONObject("opens")?.let { o -> o.keys().forEach { opensToday[it] = o.getInt(it) } }
        }
    }

    private fun save() {
        val json = JSONObject()
            .put("focus", focusApps.toJsonArray())
            .put("pause", pauseApps.toJsonArray())
            .put("scheduleApps", scheduleApps.toJsonArray())
            .put("pauseSeconds", pauseSeconds)
            .put(
                "schedules",
                JSONArray().also { arr ->
                    schedules.forEach {
                        arr.put(
                            JSONObject().put("id", it.id).put("label", it.label).put("days", it.days.map { d -> d.name }.toJsonArray())
                                .put("start", it.start.toString()).put("end", it.end.toString()).put("enabled", it.enabled),
                        )
                    }
                },
            )
            // Only the last 30 days of counts are kept.
            .put("stats", JSONObject().also { st -> stats.filterKeys { runCatching { LocalDate.parse(it) }.getOrNull()?.isAfter(LocalDate.now().minusDays(30)) == true }.forEach { (d, s) -> st.put(d, JSONObject().put("b", s.blocks).put("p", s.pauses).put("r", s.resisted)) } })
            .put("opensDay", opensDay)
            .put("opens", JSONObject(opensToday.toMap()))
        Storage.write(KEY, json.toString())
    }
}
