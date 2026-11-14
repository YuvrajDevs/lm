package com.glowup.app.ui.data

import com.glowup.app.ui.data.fitness.BodyEntry
import com.glowup.app.ui.data.fitness.Direction
import com.glowup.app.ui.data.fitness.ExerciseLibrary
import com.glowup.app.ui.data.fitness.FitnessGoal
import com.glowup.app.ui.data.fitness.FitnessLevel
import com.glowup.app.ui.data.fitness.FitnessStore
import com.glowup.app.ui.data.fitness.Measure
import com.glowup.app.ui.data.fitness.Programmes
import com.glowup.app.ui.data.fitness.QuickFocus
import com.glowup.app.ui.data.fitness.QuickSessions
import com.glowup.app.ui.data.fitness.SetLog
import com.glowup.app.ui.data.fitness.TrendTone
import java.time.DayOfWeek
import java.time.LocalDate
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class FitnessTest {
    private val today = LocalDate.of(2026, 6, 1)

    @Before
    fun setUp() {
        freshStores()
    }

    private fun setupAdult(goal: FitnessGoal = FitnessGoal.LOSE_FAT) {
        ProfileStore.updateAgeBand("25–34")
        FitnessStore.completeSetup(goal, FitnessLevel.INTERMEDIATE, setOf(DayOfWeek.MONDAY, DayOfWeek.THURSDAY), 10, null, 178, false, 80.0, 88.0, 74.0, today)
    }

    @Test
    fun libraryIsCompleteAndConsistent() {
        assertTrue("Need a big library", ExerciseLibrary.all.size >= 70)
        assertEquals(ExerciseLibrary.all.size, ExerciseLibrary.all.map { it.id }.toSet().size)
        ExerciseLibrary.all.forEach { e ->
            assertTrue("${e.id} needs steps", e.steps.size >= 1 && e.mistakes.isNotEmpty() && e.cue.isNotBlank())
            e.easier?.let { assertNotNull("${e.id} easier $it", ExerciseLibrary.find(it)) }
            e.harder?.let { assertNotNull("${e.id} harder $it", ExerciseLibrary.find(it)) }
        }
    }

    @Test
    fun everyProgrammeDayBuildsAndGetsHarder() {
        Programmes.all.forEach { p ->
            FitnessLevel.entries.forEach { level ->
                (1..30).forEach { day ->
                    val plan = p.dayPlan(day, level)
                    if (p.isRestDay(day)) assertTrue(plan.restDay) else assertTrue("${p.id} day $day", plan.blocks.isNotEmpty())
                }
            }
        }
        val abs = Programmes.get("abs30")!!
        val week1 = abs.dayPlan(1, FitnessLevel.INTERMEDIATE)
        val week4 = abs.dayPlan(22, FitnessLevel.INTERMEDIATE)
        assertTrue(week4.blocks.first().sets > week1.blocks.first().sets)
        assertTrue(week4.estimatedSeconds > week1.estimatedSeconds)
    }

    @Test
    fun levelSwapsExercises() {
        val pushup = Programmes.get("pushup50")!!.dayPlan(2, FitnessLevel.BEGINNER)
        assertTrue(pushup.blocks.none { it.exerciseId == "pushup" })
        val burpee = ExerciseLibrary.get("burpee")
        assertEquals("step-back-burpee", Programmes.adapt(burpee, FitnessLevel.BEGINNER).id)
        assertEquals("pushup50 test day keeps its goal", 50, Programmes.get("pushup50")!!.dayPlan(30, FitnessLevel.BEGINNER).blocks.single().target)
    }

    @Test
    fun quickSessionsFitTheirLengthAndAreStableForADay() {
        QuickFocus.entries.forEach { focus ->
            QuickSessions.minuteOptions.forEach { m ->
                val plan = QuickSessions.build(focus, m, FitnessLevel.INTERMEDIATE, today.toEpochDay())
                assertTrue("$focus $m", plan.blocks.size >= 3)
                assertTrue("$focus $m too long: ${plan.minutes}", plan.minutes <= m * 1.6 + 2)
            }
        }
        assertEquals(
            QuickSessions.build(QuickFocus.ABS, 10, FitnessLevel.BEGINNER, 5).blocks,
            QuickSessions.build(QuickFocus.ABS, 10, FitnessLevel.BEGINNER, 5).blocks
        )
    }

    @Test
    fun scorecardDrivesProgression() {
        setupAdult()
        ProStore.updatePurchased(true)
        val plan = FitnessStore.planFor("q.UPPER.10", today)!!
        val block = plan.blocks.first()
        fun hitAll() = plan.blocks.flatMap { b -> List(b.sets) { SetLog(b.exerciseId, b.target, b.target) } }
        FitnessStore.saveWorkout(plan, hitAll(), 600, 3, 1, today)
        val result = FitnessStore.saveWorkout(plan, hitAll(), 600, 3, 2, today)
        assertTrue(result.changes.any { it.exerciseId == block.exerciseId && it.direction == Direction.UP })
        assertEquals(1, FitnessStore.bonusFor(block.exerciseId))
        val harder = FitnessStore.planFor("q.UPPER.10", today)!!.blocks.first { it.exerciseId == block.exerciseId }
        assertEquals(block.target + block.exercise.step, harder.target)

        // Two poor sessions ease it back off.
        val weak = plan.blocks.flatMap { b -> List(b.sets) { SetLog(b.exerciseId, b.target, b.target / 3) } }
        FitnessStore.saveWorkout(plan, weak, 600, 5, 3, today)
        FitnessStore.saveWorkout(plan, weak, 600, 5, 4, today)
        assertEquals(0, FitnessStore.bonusFor(block.exerciseId))
    }

    @Test
    fun progressionIsProOnlyButTracked() {
        setupAdult()
        val plan = FitnessStore.planFor("q.LOWER.10", today)!!
        val all = plan.blocks.flatMap { b -> List(b.sets) { SetLog(b.exerciseId, b.target, b.target) } }
        FitnessStore.saveWorkout(plan, all, 600, 3, 1, today)
        FitnessStore.saveWorkout(plan, all, 600, 3, 2, today)
        assertEquals(0, FitnessStore.bonusFor(plan.blocks.first().exerciseId))
        ProStore.updatePurchased(true)
        assertEquals(1, FitnessStore.bonusFor(plan.blocks.first().exerciseId))
    }

    @Test
    fun scorecardCompletionAndPersonalBests() {
        setupAdult()
        val plan = FitnessStore.planFor("q.UPPER.10", today)!!
        val id = plan.blocks.first().exerciseId
        val first = FitnessStore.saveWorkout(plan, listOf(SetLog(id, 10, 8), SetLog(id, 10, 7), SetLog(id, 10, 4)), 300, 4, 1, today)
        assertTrue(first.personalBests.isEmpty())
        assertEquals(19.0 / 30, FitnessStore.logs.last().completion, 0.001)
        val second = FitnessStore.saveWorkout(plan, listOf(SetLog(id, 10, 9)), 300, 3, 2, today)
        assertEquals(listOf(id), second.personalBests)
        assertEquals(9, FitnessStore.bestFor(id))
        assertTrue(ProgressStore.points >= 20)
    }

    @Test
    fun programmeDaysAdvanceAndPersist() {
        setupAdult()
        FitnessStore.startProgramme("first30")
        val plan = FitnessStore.todayPlan(today)
        assertEquals("p.first30.1", plan.key)
        FitnessStore.saveWorkout(plan, plan.blocks.map { SetLog(it.exerciseId, it.target, it.target) }, 500, 3, 1, today)
        assertEquals(2, FitnessStore.nextDay("first30"))
        Stores.loadAll()
        assertEquals("first30", FitnessStore.activeProgramme)
        assertEquals("p.first30.2", FitnessStore.todayPlan(today).key)
        assertEquals(1, FitnessStore.logs.size)
        FitnessStore.completeDay("first30", 2, today)
        FitnessStore.completeDay("first30", 3, today)
        assertTrue(FitnessStore.todayPlan(today).restDay) // day 4
    }

    @Test
    fun weightTrendIsGoalAwareAndSafe() {
        setupAdult(FitnessGoal.LOSE_FAT)
        FitnessStore.logBody(BodyEntry(today.plusDays(14), 77.0, null)) // -3 kg in 2 weeks ≈ 1.9%/week
        val fast = FitnessStore.weightTrend(today.plusDays(14))!!
        assertEquals(TrendTone.CAUTION, fast.tone)
        FitnessStore.logBody(BodyEntry(today.plusDays(14), 79.0, null))
        assertEquals(TrendTone.GOOD, FitnessStore.weightTrend(today.plusDays(14))!!.tone)
        assertEquals(88.0 / 178, FitnessStore.waistToHeight()!!, 0.001)
    }

    @Test
    fun minorsNeverGetWeightLossGoals() {
        ProfileStore.updateAgeBand("16–17")
        FitnessStore.completeSetup(FitnessGoal.LOSE_FAT, FitnessLevel.BEGINNER, setOf(DayOfWeek.MONDAY), 10, null, 170, false, 60.0, null, 55.0, today)
        assertEquals(FitnessGoal.GET_TONED, FitnessStore.goal)
        assertNull(FitnessStore.targetKg)
        FitnessStore.logBody(BodyEntry(today.plusDays(14), 57.0, null))
        assertEquals(TrendTone.NEUTRAL, FitnessStore.weightTrend(today.plusDays(14))!!.tone)
    }

    @Test
    fun levelTestAndFreeLimits() {
        assertEquals(FitnessLevel.BEGINNER, FitnessStore.levelFromTest(2, 20, 15))
        assertEquals(FitnessLevel.INTERMEDIATE, FitnessStore.levelFromTest(12, 45, 30))
        assertEquals(FitnessLevel.ADVANCED, FitnessStore.levelFromTest(30, 120, 45))
        assertFalse(ProGate.programmeLocked("first30"))
        assertTrue(ProGate.programmeLocked("abs30"))
        assertTrue(ProGate.quickLocked(15))
        assertFalse(ProGate.quickLocked(10))
        assertTrue(ExerciseLibrary.all.any { it.measure == Measure.SECONDS })
    }

    @Test
    fun weekStreakCountsPlannedWorkouts() {
        setupAdult()
        val monday = LocalDate.of(2026, 6, 1) // a Monday
        val plan = FitnessStore.planFor("q.ABS.5", monday)!!
        listOf(monday.minusWeeks(1), monday.minusWeeks(1).plusDays(3), monday, monday.plusDays(3)).forEachIndexed { i, d ->
            FitnessStore.saveWorkout(plan, emptyList(), 300, 3, i.toLong(), d)
        }
        assertEquals(2, FitnessStore.weekStreak(monday.plusDays(4)))
    }
}
