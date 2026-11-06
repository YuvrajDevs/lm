package com.glowup.app.ui.data

import androidx.compose.runtime.mutableStateMapOf
import org.json.JSONObject
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.temporal.TemporalAdjusters

/** Points earned per day. A day with points counts towards the streak; totals drive the level. */
object ProgressStore {
    private const val KEY = "progress"
    const val POINTS_PER_LEVEL = 100

    val dayPoints = mutableStateMapOf<String, Int>()

    val points: Int get() = dayPoints.values.sum()
    val level: Int get() = levelFor(points)
    val pointsIntoLevel: Int get() = points % POINTS_PER_LEVEL

    fun streakDays(today: LocalDate = LocalDate.now()): Int =
        streakFrom(dayPoints.filterValues { it > 0 }.keys.mapTo(HashSet()) { LocalDate.parse(it) }, today)

    fun pointsThisWeek(today: LocalDate = LocalDate.now()): Int {
        val monday = today.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY))
        return dayPoints.entries.sumOf { (day, value) -> if (LocalDate.parse(day) in monday..today) value else 0 }
    }

    fun isActive(date: LocalDate) = (dayPoints[date.toString()] ?: 0) > 0

    fun award(amount: Int, on: LocalDate = LocalDate.now()) {
        val key = on.toString()
        dayPoints[key] = (dayPoints[key] ?: 0) + amount
        save()
    }

    fun revoke(amount: Int, on: LocalDate = LocalDate.now()) {
        val key = on.toString()
        val remaining = (dayPoints[key] ?: 0) - amount
        if (remaining > 0) dayPoints[key] = remaining else dayPoints.remove(key)
        save()
    }

    fun load() {
        dayPoints.clear()
        val json = Storage.read(KEY)?.let(::JSONObject) ?: return
        json.keys().forEach { day -> dayPoints[day] = json.getInt(day) }
    }

    private fun save() {
        Storage.write(KEY, JSONObject(dayPoints.toMap()).toString())
    }

    fun levelFor(points: Int) = points / POINTS_PER_LEVEL + 1

    /** Consecutive active days ending today, or ending yesterday if today has no activity yet. */
    fun streakFrom(activeDays: Set<LocalDate>, today: LocalDate): Int {
        var cursor = if (today in activeDays) today else today.minusDays(1)
        var count = 0
        while (cursor in activeDays) {
            count++
            cursor = cursor.minusDays(1)
        }
        return count
    }
}
