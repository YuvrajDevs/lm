package com.glowup.app.ui.data

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import org.json.JSONObject
import java.time.Duration
import java.time.LocalDate
import java.time.LocalTime
import kotlin.math.roundToInt
import kotlin.math.sqrt

/** One night, filed under the morning the user woke up. [quality] is 1 (awful) – 5 (great). */
data class SleepEntry(val date: LocalDate, val bed: LocalTime, val wake: LocalTime, val quality: Int) {
    val minutes: Int
        get() = Duration.between(bed, wake).let { if (it.isNegative || it.isZero) it.plusDays(1) else it }.toMinutes().toInt()
}

enum class DebtLevel(val label: String) { LOW("Low"), MODERATE("Moderate"), HIGH("High") }

object SleepLogStore {
    private const val KEY = "sleep_log"
    const val POINTS_PER_LOG = 5
    const val DEFAULT_NEED_MINUTES = 8 * 60
    val needOptions = (12..20).map { it * 30 } // 6h – 10h in half hours

    val entries = mutableStateMapOf<String, SleepEntry>()

    var needMinutes by mutableStateOf(DEFAULT_NEED_MINUTES)
        private set

    fun on(date: LocalDate) = entries[date.toString()]

    fun recent(today: LocalDate = LocalDate.now(), nights: Int = 14): List<SleepEntry> =
        entries.values.filter { !it.date.isAfter(today) && it.date.isAfter(today.minusDays(nights.toLong())) }.sortedBy { it.date }

    fun log(entry: SleepEntry) {
        val key = entry.date.toString()
        val first = key !in entries
        entries[key] = entry.copy(quality = entry.quality.coerceIn(1, 5))
        if (first) ProgressStore.award(POINTS_PER_LOG, entry.date)
        save()
    }

    fun remove(date: LocalDate) {
        entries.remove(date.toString())
        save()
    }

    fun updateNeed(minutes: Int) {
        needMinutes = minutes.coerceIn(needOptions.first(), needOptions.last())
        save()
    }

    fun debtMinutes(today: LocalDate = LocalDate.now()) = SleepMath.debtMinutes(recent(today), needMinutes, today)

    fun consistency(today: LocalDate = LocalDate.now()) = SleepMath.consistency(recent(today))

    fun clearAll() {
        entries.clear()
        needMinutes = DEFAULT_NEED_MINUTES
        Storage.remove(KEY)
    }

    fun load() {
        entries.clear()
        val json = Storage.read(KEY)?.let(::JSONObject)
        needMinutes = json?.optInt("need", DEFAULT_NEED_MINUTES) ?: DEFAULT_NEED_MINUTES
        json?.optJSONObject("nights")?.let { nights ->
            nights.keys().forEach { day ->
                val n = nights.getJSONObject(day)
                entries[day] = SleepEntry(LocalDate.parse(day), LocalTime.parse(n.getString("bed")), LocalTime.parse(n.getString("wake")), n.getInt("q"))
            }
        }
    }

    private fun save() {
        val nights = JSONObject()
        entries.forEach { (day, e) ->
            nights.put(day, JSONObject().put("bed", e.bed.toString()).put("wake", e.wake.toString()).put("q", e.quality))
        }
        Storage.write(KEY, JSONObject().put("need", needMinutes).put("nights", nights).toString())
    }
}

object SleepMath {
    const val MIN_NIGHTS_FOR_DEBT = 3
    const val MIN_NIGHTS_FOR_CONSISTENCY = 4

    /**
     * Sleep debt over the last 14 nights: how far short of your sleep need you've been, with the
     * most recent week counting fully and the week before counting half. Extra sleep pays debt back.
     * Null until there are enough logged nights to say anything useful.
     */
    fun debtMinutes(nights: List<SleepEntry>, needMinutes: Int, today: LocalDate): Int? {
        if (nights.size < MIN_NIGHTS_FOR_DEBT) return null
        val total = nights.sumOf { night ->
            val age = Duration.between(night.date.atStartOfDay(), today.atStartOfDay()).toDays()
            val weight = if (age < 7) 1.0 else 0.5
            weight * (needMinutes - night.minutes)
        }
        return total.coerceAtLeast(0.0).roundToInt()
    }

    fun level(debtMinutes: Int): DebtLevel = when {
        debtMinutes < 120 -> DebtLevel.LOW
        debtMinutes < 300 -> DebtLevel.MODERATE
        else -> DebtLevel.HIGH
    }

    /**
     * 0–100: how regular bed and wake times are. Up to 10 minutes of typical variation is perfect;
     * every extra minute costs 1.5 points, so ±1 hour of drift lands around "Irregular".
     */
    fun consistency(nights: List<SleepEntry>): Int? {
        if (nights.size < MIN_NIGHTS_FOR_CONSISTENCY) return null
        // Measure bedtimes from noon so 23:30 and 00:30 are an hour apart, not 23 hours.
        val beds = nights.map { ((it.bed.toSecondOfDay() / 60) - 720 + 1440) % 1440 }
        val wakes = nights.map { it.wake.toSecondOfDay() / 60 }
        val spread = (stdDev(beds) + stdDev(wakes)) / 2
        return (100 - (spread - 10).coerceAtLeast(0.0) * 1.5).coerceIn(0.0, 100.0).roundToInt()
    }

    fun consistencyLabel(score: Int) = when {
        score >= 85 -> "Very regular"
        score >= 65 -> "Fairly regular"
        else -> "Irregular"
    }

    fun averageMinutes(nights: List<SleepEntry>): Int? = nights.takeIf { it.isNotEmpty() }?.map { it.minutes }?.average()?.roundToInt()

    private fun stdDev(values: List<Int>): Double {
        val mean = values.average()
        return sqrt(values.sumOf { (it - mean) * (it - mean) } / values.size)
    }
}

/** "7h 30m", "45m", "8h". */
fun formatDuration(minutes: Int): String {
    val h = minutes / 60
    val m = minutes % 60
    return when {
        h == 0 -> "${m}m"
        m == 0 -> "${h}h"
        else -> "${h}h ${m}m"
    }
}
