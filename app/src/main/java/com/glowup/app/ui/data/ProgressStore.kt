package com.glowup.app.ui.data

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import org.json.JSONObject
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.temporal.TemporalAdjusters

/** Shown once after freezes quietly protect the streak over missed days. */
data class FreezeNotice(val daysCovered: Int, val streak: Int)

/**
 * Points earned per day. A day with points counts towards the streak; totals drive the level.
 *
 * Streak freezes: every 7th day of a streak earns one (banked up to [maxFreezes]). When whole days
 * are missed, banked freezes cover them automatically so the streak survives — a frozen day keeps
 * the streak alive but doesn't add to it. If the gap is longer than the freezes banked, the streak
 * ends and the freezes are kept for next time.
 */
object ProgressStore {
    private const val KEY = "progress"
    const val POINTS_PER_LEVEL = 100
    const val DAYS_PER_FREEZE = 7
    const val FREE_MAX_FREEZES = 2
    const val PRO_MAX_FREEZES = 3

    val dayPoints = mutableStateMapOf<String, Int>()

    var freezes by mutableStateOf(0)
        private set
    var frozenDays by mutableStateOf(emptySet<LocalDate>())
        private set
    private var lastFreezeEarned: LocalDate? = null

    var freezeNotice by mutableStateOf<FreezeNotice?>(null)
        private set

    /** True right after a freeze is earned, until the user has seen it. */
    var freezeJustEarned by mutableStateOf(false)
        private set

    val maxFreezes: Int get() = if (ProStore.isPro) PRO_MAX_FREEZES else FREE_MAX_FREEZES

    val points: Int get() = dayPoints.values.sum()
    val level: Int get() = levelFor(points)
    val pointsIntoLevel: Int get() = points % POINTS_PER_LEVEL

    private fun activeDates(): Set<LocalDate> = dayPoints.filterValues { it > 0 }.keys.mapTo(HashSet()) { LocalDate.parse(it) }

    fun streakDays(today: LocalDate = LocalDate.now()): Int = streakFrom(activeDates(), today, frozenDays)

    /** Days until the next freeze is earned, or null when the bank is full. */
    fun daysToNextFreeze(today: LocalDate = LocalDate.now()): Int? {
        if (freezes >= maxFreezes) return null
        // When today isn't done yet, today itself is one of the remaining days.
        return DAYS_PER_FREEZE - streakDays(today) % DAYS_PER_FREEZE
    }

    fun pointsThisWeek(today: LocalDate = LocalDate.now()): Int {
        val monday = today.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY))
        return dayPoints.entries.sumOf { (day, value) -> if (LocalDate.parse(day) in monday..today) value else 0 }
    }

    fun isActive(date: LocalDate) = (dayPoints[date.toString()] ?: 0) > 0

    fun isFrozen(date: LocalDate) = date in frozenDays

    fun award(amount: Int, on: LocalDate = LocalDate.now()) {
        val key = on.toString()
        dayPoints[key] = (dayPoints[key] ?: 0) + amount
        maybeEarnFreeze(on)
        save()
    }

    fun revoke(amount: Int, on: LocalDate = LocalDate.now()) {
        val key = on.toString()
        val remaining = (dayPoints[key] ?: 0) - amount
        if (remaining > 0) dayPoints[key] = remaining else dayPoints.remove(key)
        save()
    }

    private fun maybeEarnFreeze(on: LocalDate) {
        if (lastFreezeEarned == on || freezes >= maxFreezes) return
        val streak = streakDays(on)
        if (streak > 0 && streak % DAYS_PER_FREEZE == 0) {
            freezes++
            lastFreezeEarned = on
            freezeJustEarned = true
        }
    }

    /**
     * Covers fully missed days (before today) with banked freezes. Safe to call any number of
     * times — e.g. on launch, on resume and from widgets.
     */
    fun applyFreezes(today: LocalDate = LocalDate.now()) {
        val active = activeDates()
        val lastActive = active.filter { it < today }.maxOrNull() ?: return
        val missed = generateSequence(lastActive.plusDays(1)) { it.plusDays(1) }
            .takeWhile { it < today }
            .filter { it !in frozenDays }
            .toList()
        if (missed.isEmpty() || missed.size > freezes) return
        val streak = streakFrom(active, lastActive, frozenDays)
        if (streak == 0) return
        freezes -= missed.size
        frozenDays = frozenDays + missed
        freezeNotice = FreezeNotice(missed.size, streak)
        save()
    }

    fun dismissFreezeNotice() {
        freezeNotice = null
    }

    fun acknowledgeFreezeEarned() {
        freezeJustEarned = false
    }

    fun load() {
        dayPoints.clear()
        freezes = 0
        frozenDays = emptySet()
        lastFreezeEarned = null
        freezeNotice = null
        freezeJustEarned = false
        val json = Storage.read(KEY)?.let(::JSONObject) ?: return
        if (json.has("days")) {
            val days = json.getJSONObject("days")
            days.keys().forEach { day -> dayPoints[day] = days.getInt(day) }
            freezes = json.optInt("freezes", 0)
            frozenDays = json.optJSONArray("frozen")?.strings()?.mapTo(HashSet()) { LocalDate.parse(it) } ?: emptySet()
            lastFreezeEarned = json.optStringOrNull("lastEarned")?.let(LocalDate::parse)
        } else {
            // Before streak freezes, this key held only the day → points map.
            json.keys().forEach { day -> dayPoints[day] = json.getInt(day) }
        }
    }

    private fun save() {
        Storage.write(
            KEY,
            JSONObject()
                .put("days", JSONObject(dayPoints.toMap()))
                .put("freezes", freezes)
                .put("frozen", frozenDays.map { it.toString() }.sorted().toJsonArray())
                .put("lastEarned", lastFreezeEarned?.toString() ?: JSONObject.NULL)
                .toString(),
        )
    }

    fun levelFor(points: Int) = points / POINTS_PER_LEVEL + 1

    /**
     * Consecutive active days ending today, or ending yesterday if today has no activity yet.
     * Days in [bridged] (frozen days) keep the run going without adding to it.
     */
    fun streakFrom(activeDays: Set<LocalDate>, today: LocalDate, bridged: Set<LocalDate> = emptySet()): Int {
        var cursor = if (today in activeDays) today else today.minusDays(1)
        var count = 0
        while (cursor in activeDays || cursor in bridged) {
            if (cursor in activeDays) count++
            cursor = cursor.minusDays(1)
        }
        return count
    }
}
