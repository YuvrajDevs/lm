package com.glowup.app.ui.data.fitness

import androidx.compose.ui.graphics.Color
import com.glowup.app.ui.theme.BlueTint
import com.glowup.app.ui.theme.Coral
import com.glowup.app.ui.theme.CoralTint
import com.glowup.app.ui.theme.Green
import com.glowup.app.ui.theme.GreenTint
import com.glowup.app.ui.theme.Maroon
import com.glowup.app.ui.theme.MaroonTint
import com.glowup.app.ui.theme.Navy
import com.glowup.app.ui.theme.Purple
import com.glowup.app.ui.theme.PurpleTint
import com.glowup.app.ui.theme.Teal
import com.glowup.app.ui.theme.TealTint
import com.glowup.app.ui.theme.YellowTint

enum class Muscle(val label: String, val tint: Color, val accent: Color) {
    CHEST("Chest", CoralTint, Coral),
    BACK("Back", BlueTint, Navy),
    SHOULDERS("Shoulders", PurpleTint, Purple),
    ARMS("Arms", MaroonTint, Maroon),
    CORE("Core & abs", YellowTint, Color(0xFF8A6A00)),
    GLUTES("Glutes", TealTint, Teal),
    LEGS("Legs", GreenTint, Green),
    CARDIO("Cardio", CoralTint, Color(0xFFD9572B)),
    MOBILITY("Mobility", BlueTint, Color(0xFF4F8FE8))
}

enum class Measure { REPS, SECONDS }

/** 1 = beginner, 2 = intermediate, 3 = advanced. */
enum class FitnessLevel(val label: String, val doseScale: Double) {
    BEGINNER("Beginner", 0.8),
    INTERMEDIATE("Intermediate", 1.0),
    ADVANCED("Advanced", 1.25)
}

enum class FitnessGoal(val label: String, val blurb: String, val minorSafe: Boolean) {
    LOSE_FAT("Lose fat", "Burn more, keep muscle, see your jawline", false),
    BUILD_MUSCLE("Build muscle", "Get stronger and fill out your frame", true),
    GET_TONED("Get fit & toned", "A bit of everything — strength and conditioning", true),
    CONSISTENT("Just be consistent", "Short daily sessions that stick", true)
    ;

    /** Label shown to under-18s — no weight-loss framing. */
    val minorLabel: String get() = if (this == BUILD_MUSCLE) "Build strength" else label
}

data class Exercise(
    val id: String,
    val name: String,
    val primary: Muscle,
    val also: Set<Muscle>,
    val measure: Measure,
    val level: Int,
    /** Reps or seconds count for each side (lunges, side planks). */
    val perSide: Boolean,
    val steps: List<String>,
    val mistakes: List<String>,
    val cue: String,
    val easier: String? = null,
    val harder: String? = null,
    val safety: String? = null
) {
    val muscles: Set<Muscle> get() = also + primary

    /** Intermediate starting dose; scaled by level and week. */
    val baseDose: Int get() = when (measure) {
        Measure.REPS -> when (level) {
            1 -> 12
            2 -> 10
            else -> 8
        }
        Measure.SECONDS -> when (primary) {
            Muscle.CARDIO -> 30
            Muscle.MOBILITY -> 30
            else -> 30
        }
    }

    /** One step of auto-progression. */
    val step: Int get() = if (measure == Measure.REPS) (if (perSide) 1 else 2) else 5

    /** Rough seconds to perform one set at [dose], for time estimates. */
    fun workSeconds(dose: Int): Int = when (measure) {
        Measure.REPS -> dose * 3 * (if (perSide) 2 else 1)
        Measure.SECONDS -> dose * (if (perSide) 2 else 1)
    }

    fun doseLabel(dose: Int): String {
        val unit = if (measure == Measure.REPS) (if (dose == 1) "rep" else "reps") else "sec"
        return "$dose $unit" + if (perSide) " each side" else ""
    }
}

/** One exercise in a workout: [sets] × [target]. A [max] block asks for as many as you can. */
data class Block(val exerciseId: String, val sets: Int, val target: Int, val restSeconds: Int, val max: Boolean = false) {
    val exercise: Exercise get() = ExerciseLibrary.get(exerciseId)
}

data class WorkoutPlan(val key: String, val title: String, val subtitle: String, val blocks: List<Block>, val restDay: Boolean = false) {
    val estimatedSeconds: Int
        get() = blocks.sumOf { b -> b.sets * (b.exercise.workSeconds(b.target) + b.restSeconds) } + 30
    val minutes: Int get() = ((estimatedSeconds + 59) / 60).coerceAtLeast(1)
    val muscles: List<Muscle> get() = blocks.map { it.exercise.primary }.distinct()
}
