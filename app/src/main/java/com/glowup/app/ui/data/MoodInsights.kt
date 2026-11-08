package com.glowup.app.ui.data

import com.glowup.app.ui.data.program.ProgramStore
import java.time.LocalDate
import kotlin.math.abs

/**
 * Something that may or may not have happened on a day. [observed] are the days we can tell
 * either way — e.g. sleep factors only count nights that were logged.
 */
data class MoodFactor(val id: String, val phrase: String, val present: Set<LocalDate>, val observed: Set<LocalDate>, val fromTag: Boolean)

/** "On days [phrase], your mood averages [withAvg] vs [withoutAvg]". */
data class MoodInsight(
    val factorId: String,
    val phrase: String,
    val withAvg: Double,
    val withoutAvg: Double,
    val withDays: Int,
    val withoutDays: Int,
    val fromTag: Boolean,
) {
    val difference: Double get() = withAvg - withoutAvg
    val lifts: Boolean get() = difference > 0
}

object MoodInsights {
    /** Each side of a comparison needs this many check-ins before we'll call it a pattern. */
    const val MIN_DAYS = 5
    /** On the 1–5 mood scale, smaller gaps are too likely to be noise to show. */
    const val MIN_DIFFERENCE = 0.5

    /** Pure comparison: every factor with enough days on both sides and a big enough gap, strongest first. */
    fun compute(scores: Map<LocalDate, Int>, factors: List<MoodFactor>): List<MoodInsight> =
        factors.mapNotNull { factor ->
            val days = scores.keys.filter { it in factor.observed }
            val with = days.filter { it in factor.present }.map { scores.getValue(it) }
            val without = days.filter { it !in factor.present }.map { scores.getValue(it) }
            if (with.size < MIN_DAYS || without.size < MIN_DAYS) return@mapNotNull null
            val insight = MoodInsight(factor.id, factor.phrase, with.average(), without.average(), with.size, without.size, factor.fromTag)
            insight.takeIf { abs(it.difference) >= MIN_DIFFERENCE }
        }.sortedByDescending { abs(it.difference) }

    /** Check-ins still needed before the first comparison is possible. */
    fun checkInsUntilInsights(): Int = (MIN_DAYS * 2 - MoodStore.checkInCount).coerceAtLeast(0)

    fun scores(): Map<LocalDate, Int> =
        MoodStore.moodLog.mapNotNull { (day, mood) -> MoodStore.scoreFor(mood)?.let { LocalDate.parse(day) to it } }.toMap()

    /** Factors from the user's tags plus everything the app already knows about each day. */
    fun factors(): List<MoodFactor> {
        val moodDays = scores().keys
        val taggedDays = MoodStore.tags.keys.mapTo(HashSet()) { LocalDate.parse(it) }
        val tagFactors = MoodStore.allTags.map { tag ->
            MoodFactor(
                id = "tag:$tag",
                phrase = "tagged “$tag”",
                present = MoodStore.tags.filterValues { tag in it }.keys.mapTo(HashSet()) { LocalDate.parse(it) },
                observed = taggedDays,
                fromTag = true,
            )
        }

        val auto = buildList {
            add(
                MoodFactor(
                    "focus30", "you focused 30+ min",
                    FocusStore.minutesByDay.filterValues { it >= 30 }.keys.mapTo(HashSet()) { LocalDate.parse(it) },
                    moodDays, false,
                ),
            )
            val daily = RoutineStore.items.filter { it.frequency == "DAILY" }
            if (daily.isNotEmpty()) {
                val routineDays = moodDays.filterTo(HashSet()) { day ->
                    daily.count { it.isDoneOn(day) } * 5 >= daily.size * 4 // 80% or more
                }
                add(MoodFactor("routines", "you did your routines", routineDays, moodDays, false))
            }
            val fitness = RoutineStore.items.filter { it.category == "Fitness" }
            if (fitness.isNotEmpty()) {
                add(MoodFactor("fitness", "you did a fitness routine", moodDays.filterTo(HashSet()) { day -> fitness.any { it.isDoneOn(day) } }, moodDays, false))
            }
            add(
                MoodFactor(
                    "learn", "you did a learning session",
                    ProgramStore.completions.values.mapTo(HashSet()) { LocalDate.parse(it) },
                    moodDays, false,
                ),
            )
            add(
                MoodFactor(
                    "workout", "you worked out",
                    com.glowup.app.ui.data.fitness.FitnessStore.workoutDates(),
                    moodDays, false,
                ),
            )
            val nights = SleepLogStore.entries.values
            val logged = nights.mapTo(HashSet()) { it.date }
            add(MoodFactor("sleep7", "you slept 7+ hours", nights.filter { it.minutes >= 420 }.mapTo(HashSet()) { it.date }, logged, false))
            add(MoodFactor("sleep6", "you slept under 6 hours", nights.filter { it.minutes < 360 }.mapTo(HashSet()) { it.date }, logged, false))
        }
        return auto + tagFactors
    }

    fun current(): List<MoodInsight> = compute(scores(), factors())

    /** Tags that show up most on your best days (score 5), most frequent first. */
    fun bestDayTags(limit: Int = 3): List<Pair<String, Int>> {
        val best = MoodStore.moodLog.filterValues { MoodStore.scoreFor(it) == 5 }.keys
        return best.flatMap { MoodStore.tags[it].orEmpty() }
            .groupingBy { it }.eachCount()
            .entries.sortedByDescending { it.value }
            .take(limit)
            .map { it.key to it.value }
    }

    fun averageScore(from: LocalDate, to: LocalDate): Double? =
        scores().filterKeys { !it.isBefore(from) && !it.isAfter(to) }.values.takeIf { it.isNotEmpty() }?.average()
}
