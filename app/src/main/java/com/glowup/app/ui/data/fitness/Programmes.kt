package com.glowup.app.ui.data.fitness

import kotlin.math.roundToInt
import java.time.LocalDate
import java.util.Random

/** A template exercise: [id] with an optional intermediate dose that overrides the exercise default. */
internal data class T(val id: String, val dose: Int? = null, val max: Boolean = false)

class Programme internal constructor(
    val id: String,
    val title: String,
    val tagline: String,
    val about: String,
    val level: Int,
    val free: Boolean,
    val focus: Muscle,
    val restDays: Set<Int>,
    private val templates: List<List<T>>,
    private val restSeconds: Int,
    /** Day numbers (1-based) with a fixed special workout, e.g. max-rep tests. */
    private val specials: Map<Int, List<T>> = emptyMap(),
    val honestNote: String? = null,
) {
    val days: Int get() = Programme.DAYS

    fun isRestDay(day: Int) = day in restDays

    /** Builds day [day] (1-based) for [level], applying the user's personal progression [bonus]. */
    fun dayPlan(day: Int, level: FitnessLevel, bonus: (String) -> Int = { 0 }): WorkoutPlan {
        val key = "p.$id.$day"
        if (isRestDay(day)) {
            return WorkoutPlan(key, "Day $day · Rest & recover", "Muscles grow on rest days. A short stretch helps.", emptyList(), restDay = true)
        }
        val week = (day - 1) / 7
        val trainingIndex = (1 until day).count { it !in restDays }
        val special = specials[day]
        val items = special ?: templates[trainingIndex % templates.size]
        val sets = if (special != null) 1 else listOf(2, 3, 3, 4, 4)[week.coerceAtMost(4)]
        val weekScale = 1.0 + 0.12 * week
        val blocks = items.map { t ->
            val exercise = Programmes.adapt(ExerciseLibrary.get(t.id), level)
            val base = (t.dose ?: exercise.baseDose) * level.doseScale * weekScale
            // Test days ask for your max against a fixed goal; everything else scales with you.
            val target = if (t.max) t.dose ?: 50 else Programmes.roundDose(exercise, base) + bonus(exercise.id) * exercise.step
            Block(exercise.id, sets, target.coerceAtLeast(Programmes.minDose(exercise)), if (t.max) 0 else Programmes.restFor(exercise, restSeconds), t.max)
        }
        val title = if (special != null) "Day $day · Test day" else "Day $day · ${Programmes.titleFor(blocks)}"
        return WorkoutPlan(key, title, "$sets ${if (sets == 1) "set" else "sets"} each · week ${week + 1}", blocks)
    }

    companion object {
        const val DAYS = 30
    }
}

object Programmes {
    private val everyFourth = setOf(4, 8, 12, 16, 20, 24, 28)

    val all: List<Programme> = listOf(
        Programme(
            id = "first30", title = "First 30 days", tagline = "Build the habit · full body",
            about = "Short, beginner-friendly sessions that cover everything: legs, push, core and a little cardio. By day 30 you'll do twice what you could on day 1.",
            level = 1, free = true, focus = Muscle.CHEST, restDays = everyFourth, restSeconds = 40,
            templates = listOf(
                listOf(T("squat"), T("knee-pushup", 8), T("glute-bridge"), T("plank", 20), T("march", 30)),
                listOf(T("reverse-lunge", 8), T("sumo-squat"), T("dead-bug", 8), T("bridge-hold", 20), T("calf-raise", 15)),
                listOf(T("incline-pushup"), T("reverse-snow-angel", 8), T("jumping-jacks", 30), T("bird-dog", 8), T("knee-plank", 20)),
            ),
        ),
        Programme(
            id = "abs30", title = "Abs in 30", tagline = "Core strength & definition",
            about = "Three rotating core circuits that hit the front, sides and deep stabilisers, getting harder every week.",
            level = 2, free = false, focus = Muscle.CORE, restDays = everyFourth, restSeconds = 20,
            templates = listOf(
                listOf(T("crunch", 15), T("bicycle-crunch", 10), T("leg-raise", 10), T("plank", 30), T("flutter-kicks", 20)),
                listOf(T("reverse-crunch", 12), T("russian-twist", 10), T("dead-bug", 8), T("side-plank", 20), T("mountain-climber", 25)),
                listOf(T("heel-taps", 12), T("toe-touch", 12), T("hollow-hold", 20), T("shoulder-taps", 8), T("plank", 35)),
            ),
            honestNote = "Visible abs depend on body fat, not just ab work. Pair this with Fat Burn and decent food for the best results.",
        ),
        Programme(
            id = "pushup50", title = "Push-up 0 → 50", tagline = "Chest, arms & shoulders",
            about = "A step-by-step ladder from your first clean push-up to 50 in a row. Test days on day 1, 15 and 30 show how far you've come.",
            level = 2, free = false, focus = Muscle.CHEST, restDays = setOf(3, 6, 9, 12, 14, 17, 20, 23, 26, 29), restSeconds = 60,
            templates = listOf(
                listOf(T("pushup", 6), T("pushup", 6), T("negative-pushup", 4), T("plank", 30)),
                listOf(T("wide-pushup", 6), T("close-pushup", 5), T("pushup", 6), T("shoulder-taps", 8)),
            ),
            specials = mapOf(1 to listOf(T("pushup", 50, max = true)), 15 to listOf(T("pushup", 50, max = true)), 30 to listOf(T("pushup", 50, max = true))),
        ),
        Programme(
            id = "fatburn", title = "Fat Burn HIIT", tagline = "Sweaty, short, no equipment",
            about = "High-intensity intervals that get your heart rate up and keep it there. Quiet low-impact swaps are offered for every jump.",
            level = 2, free = false, focus = Muscle.CARDIO, restDays = everyFourth, restSeconds = 20,
            templates = listOf(
                listOf(T("jumping-jacks", 30), T("squat", 15), T("mountain-climber", 30), T("step-back-burpee", 8), T("high-knees", 30)),
                listOf(T("skater-hops", 30), T("reverse-lunge", 10), T("butt-kicks", 30), T("shoulder-taps", 10), T("shadow-boxing", 40)),
                listOf(T("plank-jacks", 30), T("jump-squat", 10), T("bear-crawl", 25), T("burpee", 6), T("jumping-jacks", 40)),
            ),
            honestNote = "Exercise helps, but fat loss mostly comes from eating a little less than you burn. Keep protein high so you lose fat, not muscle.",
        ),
        Programme(
            id = "lean", title = "Lean muscle", tagline = "Push · legs · pull, no gym",
            about = "A proper strength split with your bodyweight: push day, leg day and pull & core day, with fewer, harder reps and longer rest.",
            level = 2, free = false, focus = Muscle.ARMS, restDays = everyFourth, restSeconds = 75,
            templates = listOf(
                listOf(T("pushup", 10), T("pike-pushup", 8), T("close-pushup", 8), T("chair-dip", 10), T("plank", 40)),
                listOf(T("squat", 15), T("bulgarian-split-squat", 8), T("single-leg-bridge", 10), T("wall-sit", 40), T("single-calf-raise", 12)),
                listOf(T("table-row", 8), T("superman-hold", 25), T("prone-y-raise", 10), T("bird-dog", 8), T("hollow-hold", 25)),
            ),
            honestNote = "Muscle needs enough protein and sleep — roughly 1.6 g of protein per kg of body weight a day is a good target for adults.",
        ),
        Programme(
            id = "posture", title = "Posture & mobility", tagline = "Undo the desk slump",
            about = "Ten minutes a day for forward head, rounded shoulders and tight hips. No rest days — it's gentle enough to do daily.",
            level = 1, free = false, focus = Muscle.MOBILITY, restDays = emptySet(), restSeconds = 10,
            templates = listOf(
                listOf(T("chin-tuck", 10), T("wall-angel", 10), T("doorway-stretch", 30), T("prone-y-raise", 10), T("hip-flexor-stretch", 30)),
                listOf(T("cat-cow", 10), T("open-book", 8), T("reverse-snow-angel", 10), T("childs-pose", 40), T("chin-tuck", 10)),
                listOf(T("worlds-greatest-stretch", 5), T("wall-angel", 12), T("cobra", 30), T("trap-stretch", 30), T("figure-4", 30)),
            ),
        ),
    )

    fun get(id: String) = all.firstOrNull { it.id == id }

    /** Swaps in an easier or harder version so the exercise fits the user. */
    fun adapt(exercise: Exercise, level: FitnessLevel): Exercise = when {
        level == FitnessLevel.BEGINNER && exercise.level >= 3 -> exercise.easier?.let(ExerciseLibrary::find) ?: exercise
        level == FitnessLevel.BEGINNER && exercise.id == "pushup" -> ExerciseLibrary.get("knee-pushup")
        level == FitnessLevel.ADVANCED && exercise.level == 1 -> exercise.harder?.let(ExerciseLibrary::find) ?: exercise
        else -> exercise
    }

    fun roundDose(exercise: Exercise, raw: Double): Int = when (exercise.measure) {
        Measure.REPS -> raw.roundToInt()
        Measure.SECONDS -> ((raw / 5).roundToInt() * 5)
    }

    fun minDose(exercise: Exercise) = if (exercise.measure == Measure.REPS) 3 else 10

    fun restFor(exercise: Exercise, programmeRest: Int) = when (exercise.primary) {
        Muscle.MOBILITY -> 10
        else -> programmeRest
    }

    fun titleFor(blocks: List<Block>): String {
        val muscles = blocks.map { it.exercise.primary }
        val top = muscles.groupingBy { it }.eachCount().maxByOrNull { it.value }?.key
        return when {
            muscles.count { it == Muscle.CARDIO } >= 3 -> "HIIT"
            muscles.count { it == Muscle.MOBILITY } >= 3 -> "Mobility"
            muscles.count { it in setOf(Muscle.LEGS, Muscle.GLUTES) } >= 3 -> "Legs & glutes"
            muscles.count { it == Muscle.CORE } >= 3 -> "Core"
            muscles.count { it in setOf(Muscle.CHEST, Muscle.ARMS, Muscle.SHOULDERS) } >= 3 -> "Push"
            muscles.count { it == Muscle.BACK } >= 2 -> "Pull & core"
            else -> top?.let { "Full body" } ?: "Workout"
        }
    }
}

enum class QuickFocus(val label: String, val pool: List<String>, val restSeconds: Int) {
    FULL_BODY("Full body", listOf("squat", "pushup", "glute-bridge", "reverse-lunge", "plank", "jumping-jacks", "superman", "mountain-climber", "shoulder-taps", "side-lunge"), 25),
    ABS("Abs & core", listOf("crunch", "bicycle-crunch", "leg-raise", "plank", "russian-twist", "dead-bug", "flutter-kicks", "side-plank", "heel-taps", "mountain-climber"), 15),
    UPPER("Upper body", listOf("pushup", "pike-pushup", "chair-dip", "wide-pushup", "superman", "prone-y-raise", "plank-up-down", "close-pushup", "reverse-snow-angel"), 30),
    LOWER("Legs & glutes", listOf("squat", "reverse-lunge", "glute-bridge", "wall-sit", "sumo-squat", "calf-raise", "side-lunge", "donkey-kick", "step-up", "single-leg-bridge"), 30),
    FAT_BURN("Fat burn", listOf("jumping-jacks", "high-knees", "mountain-climber", "step-back-burpee", "skater-hops", "squat", "butt-kicks", "plank-jacks", "shadow-boxing"), 15),
    MOBILITY("Mobility & posture", listOf("cat-cow", "chin-tuck", "wall-angel", "hip-flexor-stretch", "open-book", "doorway-stretch", "childs-pose", "downward-dog", "cobra", "figure-4"), 10),
}

object QuickSessions {
    val minuteOptions = listOf(5, 10, 15, 20, 30)
    const val FREE_MAX_MINUTES = 10

    fun key(focus: QuickFocus, minutes: Int) = "q.${focus.name}.$minutes"

    /**
     * A session close to [minutes] long. The exercise mix changes daily ([seed] is usually the day)
     * but stays the same within a day, so the preview matches what you play.
     */
    fun build(focus: QuickFocus, minutes: Int, level: FitnessLevel, seed: Long, bonus: (String) -> Int = { 0 }): WorkoutPlan {
        val rounds = when {
            minutes <= 5 -> 1
            minutes <= 10 -> 2
            minutes <= 20 -> 3
            else -> 4
        }
        val shuffled = focus.pool.shuffled(Random(seed * 31 + focus.ordinal))
        val target = minutes * 60
        val blocks = mutableListOf<Block>()
        for (id in shuffled) {
            val exercise = Programmes.adapt(ExerciseLibrary.get(id), level)
            if (blocks.any { it.exerciseId == exercise.id }) continue
            val dose = (Programmes.roundDose(exercise, exercise.baseDose * level.doseScale) + bonus(exercise.id) * exercise.step)
                .coerceAtLeast(Programmes.minDose(exercise))
            val block = Block(exercise.id, rounds, dose, Programmes.restFor(exercise, focus.restSeconds))
            val withBlock = WorkoutPlan("", "", "", blocks + block).estimatedSeconds
            if (blocks.size >= 3 && withBlock > target * 1.1) break
            blocks += block
        }
        return WorkoutPlan(
            key(focus, minutes),
            "${focus.label} · $minutes min",
            "$rounds ${if (rounds == 1) "round" else "rounds"} · ${blocks.size} exercises",
            blocks,
        )
    }

    fun forGoal(goal: FitnessGoal?, date: LocalDate): QuickFocus = when (goal) {
        FitnessGoal.LOSE_FAT -> QuickFocus.FAT_BURN
        FitnessGoal.BUILD_MUSCLE -> if (date.toEpochDay() % 2 == 0L) QuickFocus.UPPER else QuickFocus.LOWER
        else -> QuickFocus.FULL_BODY
    }
}
