package com.glowup.app.ui.data.program

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.glowup.app.ui.data.ProgressStore
import com.glowup.app.ui.data.Storage
import com.glowup.app.ui.data.optStringOrNull
import org.json.JSONObject
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.LocalTime
import java.time.temporal.TemporalAdjusters

enum class SessionState { DONE, AVAILABLE, TOMORROW, LOCKED }

/**
 * Progress through the 12-week programme. Sessions unlock in order, one new session per day,
 * so the programme builds a daily habit; finished sessions stay open for review.
 */
object ProgramStore {
    private const val KEY = "program"

    /** Session index → ISO date it was completed. Always the contiguous range 0 until size. */
    val completions = mutableStateMapOf<Int, String>()

    var reminderTime by mutableStateOf<LocalTime?>(null)
        private set
    var reminderPrompted by mutableStateOf(false)
        private set

    val completedCount: Int get() = completions.size
    val isFinished: Boolean get() = completedCount >= Program.TOTAL

    /** The next session to do, or null once the whole programme is finished. */
    val next: ProgramSession? get() = Program.sessions.getOrNull(completedCount)

    fun doneToday(today: LocalDate = LocalDate.now()) = completions.values.any { it == today.toString() }

    fun state(index: Int, today: LocalDate = LocalDate.now()): SessionState = when {
        index in completions -> SessionState.DONE
        index == completedCount -> if (doneToday(today)) SessionState.TOMORROW else SessionState.AVAILABLE
        else -> SessionState.LOCKED
    }

    /** Returns the points earned, or null if this session isn't the one available today. */
    fun complete(index: Int, today: LocalDate = LocalDate.now()): Int? {
        if (state(index, today) != SessionState.AVAILABLE) return null
        val session = Program.sessions[index]
        completions[index] = today.toString()
        ProgressStore.award(session.points, today)
        save()
        Storage.onScheduleChanged()
        return session.points
    }

    private fun completionDates(): Set<LocalDate> = completions.values.mapTo(HashSet()) { LocalDate.parse(it) }

    fun streakDays(today: LocalDate = LocalDate.now()) = ProgressStore.streakFrom(completionDates(), today)

    fun bestStreak(): Int {
        val days = completionDates().sorted()
        var best = 0
        var run = 0
        var previous: LocalDate? = null
        days.forEach { day ->
            run = if (previous != null && previous!!.plusDays(1) == day) run + 1 else 1
            best = maxOf(best, run)
            previous = day
        }
        return best
    }

    /** Consecutive calendar weeks (ending this week, or last week if this week has none yet) with a session in [area]. */
    fun areaStreakWeeks(area: ProgramArea, today: LocalDate = LocalDate.now()): Int {
        val weeks = completions.entries
            .filter { (index, _) -> Program.sessions[index].area == area }
            .mapTo(HashSet()) { (_, date) -> mondayOf(LocalDate.parse(date)) }
        return weekStreak(weeks, mondayOf(today))
    }

    fun areaDone(area: ProgramArea) = completions.keys.count { Program.sessions[it].area == area }

    fun weekDone(week: Int) = Program.sessionsInWeek(week).count { it.index in completions }

    fun completionDate(index: Int): LocalDate? = completions[index]?.let(LocalDate::parse)

    fun setReminder(time: LocalTime?) {
        reminderTime = time
        reminderPrompted = true
        save()
        Storage.onScheduleChanged()
    }

    fun dismissReminderPrompt() {
        reminderPrompted = true
        save()
    }

    fun load() {
        completions.clear()
        val json = Storage.read(KEY)?.let(::JSONObject)
        json?.optJSONObject("done")?.let { done -> done.keys().forEach { completions[it.toInt()] = done.getString(it) } }
        reminderTime = json?.optStringOrNull("reminder")?.let(LocalTime::parse)
        reminderPrompted = json?.optBoolean("prompted") ?: false
    }

    private fun save() {
        val done = JSONObject()
        completions.forEach { (index, date) -> done.put(index.toString(), date) }
        Storage.write(
            KEY,
            JSONObject()
                .put("done", done)
                .put("reminder", reminderTime?.toString() ?: JSONObject.NULL)
                .put("prompted", reminderPrompted)
                .toString(),
        )
    }

    fun mondayOf(date: LocalDate): LocalDate = date.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY))

    fun weekStreak(mondays: Set<LocalDate>, thisMonday: LocalDate): Int {
        var cursor = if (thisMonday in mondays) thisMonday else thisMonday.minusWeeks(1)
        var count = 0
        while (cursor in mondays) {
            count++
            cursor = cursor.minusWeeks(1)
        }
        return count
    }
}
