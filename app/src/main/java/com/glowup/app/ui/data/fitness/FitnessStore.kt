package com.glowup.app.ui.data.fitness

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.glowup.app.ui.data.ProStore
import com.glowup.app.ui.data.ProfileStore
import com.glowup.app.ui.data.ProgressStore
import com.glowup.app.ui.data.Storage
import com.glowup.app.ui.data.objects
import com.glowup.app.ui.data.optStringOrNull
import com.glowup.app.ui.data.strings
import com.glowup.app.ui.data.toJsonArray
import org.json.JSONArray
import org.json.JSONObject
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.LocalTime
import java.time.temporal.ChronoUnit
import java.time.temporal.TemporalAdjusters
import kotlin.math.abs
import kotlin.math.min
import kotlin.math.roundToInt

data class SetLog(val exerciseId: String, val target: Int, val done: Int, val max: Boolean = false)

data class WorkoutLog(
    val id: Long,
    val date: LocalDate,
    val key: String,
    val title: String,
    val seconds: Int,
    val sets: List<SetLog>,
    /** 1 easy … 5 brutal. */
    val effort: Int,
) {
    /** Share of the targets hit, 0–1. Max-effort sets always count as complete. */
    val completion: Double
        get() {
            val scored = sets.filter { !it.max && it.target > 0 }
            if (scored.isEmpty()) return 1.0
            return scored.sumOf { min(it.done, it.target).toDouble() } / scored.sumOf { it.target.toDouble() }
        }
}

data class BodyEntry(val date: LocalDate, val weightKg: Double, val waistCm: Double?)

enum class Direction { UP, DOWN }

/** "Push-ups: +2 reps next time". */
data class ProgressionChange(val exerciseId: String, val direction: Direction, val newBonus: Int)

data class WorkoutResult(val points: Int, val personalBests: List<String>, val changes: List<ProgressionChange>)

enum class TrendTone { GOOD, NEUTRAL, CAUTION }

data class WeightTrend(val changeKg: Double, val weeks: Double, val weeklyPercent: Double, val message: String, val tone: TrendTone)

object FitnessStore {
    private const val KEY = "fitness"
    const val HITS_TO_LEVEL_UP = 2
    const val MISSES_TO_EASE_OFF = 2
    const val MIN_BONUS = -3

    var setUp by mutableStateOf(false)
        private set
    var goal by mutableStateOf<FitnessGoal?>(null)
        private set
    var level by mutableStateOf(FitnessLevel.BEGINNER)
        private set
    var days by mutableStateOf(setOf(DayOfWeek.MONDAY, DayOfWeek.WEDNESDAY, DayOfWeek.FRIDAY))
        private set
    var minutes by mutableStateOf(10)
        private set
    var reminder by mutableStateOf<LocalTime?>(null)
        private set
    var weighDay by mutableStateOf(DayOfWeek.MONDAY)
        private set
    var weighReminder by mutableStateOf(false)
        private set
    var heightCm by mutableStateOf<Int?>(null)
        private set
    var useLb by mutableStateOf(false)
        private set
    var targetKg by mutableStateOf<Double?>(null)
        private set
    var voiceOn by mutableStateOf(true)
        private set
    var activeProgramme by mutableStateOf<String?>(null)
        private set

    /** Programme id → day number → ISO date completed. */
    val progress = mutableStateMapOf<String, Map<Int, String>>()
    val logs = mutableStateListOf<WorkoutLog>()
    val bonus = mutableStateMapOf<String, Int>()
    private val hits = mutableMapOf<String, Int>()
    private val misses = mutableMapOf<String, Int>()
    val body = mutableStateMapOf<String, BodyEntry>()

    // ---------- Setup ----------

    fun completeSetup(
        goal: FitnessGoal,
        level: FitnessLevel,
        days: Set<DayOfWeek>,
        minutes: Int,
        reminder: LocalTime?,
        heightCm: Int?,
        useLb: Boolean,
        startWeightKg: Double?,
        waistCm: Double?,
        targetKg: Double?,
        today: LocalDate = LocalDate.now(),
    ) {
        // Under-18s never get a weight-loss goal or target.
        this.goal = if (ProfileStore.isMinor && !goal.minorSafe) FitnessGoal.GET_TONED else goal
        this.level = level
        this.days = days.ifEmpty { setOf(DayOfWeek.MONDAY, DayOfWeek.WEDNESDAY, DayOfWeek.FRIDAY) }
        this.minutes = minutes
        this.reminder = reminder
        this.heightCm = heightCm
        this.useLb = useLb
        this.targetKg = if (ProfileStore.isMinor) null else targetKg
        this.weighDay = this.days.minOrNull() ?: DayOfWeek.MONDAY
        this.weighReminder = weighReminder || startWeightKg != null
        startWeightKg?.let { body[today.toString()] = BodyEntry(today, it, waistCm) }
        setUp = true
        changed()
    }

    fun updateSchedule(days: Set<DayOfWeek>, minutes: Int, reminder: LocalTime?) {
        this.days = days.ifEmpty { this.days }
        this.minutes = minutes
        this.reminder = reminder
        changed()
    }

    fun updateLevel(value: FitnessLevel) {
        level = value
        save()
    }

    fun updateGoal(value: FitnessGoal) {
        goal = if (ProfileStore.isMinor && !value.minorSafe) FitnessGoal.GET_TONED else value
        if (ProfileStore.isMinor) targetKg = null
        save()
    }

    fun updateVoice(on: Boolean) {
        voiceOn = on
        save()
    }

    fun updateUnits(lb: Boolean) {
        useLb = lb
        save()
    }

    fun updateWeighIn(day: DayOfWeek, on: Boolean) {
        weighDay = day
        weighReminder = on
        changed()
    }

    fun updateHeight(cm: Int?) {
        heightCm = cm?.coerceIn(120, 230)
        save()
    }

    fun updateTarget(kg: Double?) {
        targetKg = if (ProfileStore.isMinor) null else kg
        save()
    }

    /** Level from the quick test: max push-ups, plank seconds and squats in a minute. */
    fun levelFromTest(pushups: Int, plankSeconds: Int, squatsPerMinute: Int): FitnessLevel {
        var score = 0
        score += when { pushups >= 20 -> 2; pushups >= 8 -> 1; else -> 0 }
        score += when { plankSeconds >= 90 -> 2; plankSeconds >= 40 -> 1; else -> 0 }
        score += when { squatsPerMinute >= 40 -> 2; squatsPerMinute >= 25 -> 1; else -> 0 }
        return when {
            score >= 5 -> FitnessLevel.ADVANCED
            score >= 2 -> FitnessLevel.INTERMEDIATE
            else -> FitnessLevel.BEGINNER
        }
    }

    // ---------- Programmes ----------

    fun startProgramme(id: String) {
        activeProgramme = id
        save()
    }

    fun stopProgramme() {
        activeProgramme = null
        save()
    }

    fun daysDone(programmeId: String): Map<Int, String> = progress[programmeId].orEmpty()

    /** First day not yet completed, or null when the programme is finished. */
    fun nextDay(programmeId: String): Int? = (1..Programme.DAYS).firstOrNull { it !in daysDone(programmeId) }

    fun completeDay(programmeId: String, day: Int, date: LocalDate = LocalDate.now()) {
        progress[programmeId] = daysDone(programmeId) + (day to date.toString())
        save()
    }

    /** Progression only applies with Pro, but it's tracked for everyone so it kicks in on upgrade. */
    fun bonusFor(exerciseId: String): Int = if (ProStore.isPro) bonus[exerciseId] ?: 0 else 0

    fun planFor(key: String, today: LocalDate = LocalDate.now()): WorkoutPlan? {
        val parts = key.split(".")
        return when (parts.getOrNull(0)) {
            "p" -> Programmes.get(parts.getOrNull(1) ?: return null)?.dayPlan(parts.getOrNull(2)?.toIntOrNull() ?: return null, level, ::bonusFor)
            "q" -> {
                val focus = runCatching { QuickFocus.valueOf(parts[1]) }.getOrNull() ?: return null
                QuickSessions.build(focus, parts.getOrNull(2)?.toIntOrNull() ?: return null, level, today.toEpochDay(), ::bonusFor)
            }
            else -> null
        }
    }

    /** What to do today: the next programme day, or a quick session matched to the goal. */
    fun todayPlan(today: LocalDate = LocalDate.now()): WorkoutPlan {
        activeProgramme?.let { id ->
            val programme = Programmes.get(id)
            val day = nextDay(id)
            if (programme != null && day != null) return programme.dayPlan(day, level, ::bonusFor)
        }
        val mins = if (ProStore.isPro) minutes else minutes.coerceAtMost(QuickSessions.FREE_MAX_MINUTES)
        return QuickSessions.build(QuickSessions.forGoal(goal, today), mins, level, today.toEpochDay(), ::bonusFor)
    }

    fun isTrainingDay(date: LocalDate) = date.dayOfWeek in days

    // ---------- Logging ----------

    fun bestFor(exerciseId: String): Int? = logs.flatMap { it.sets }.filter { it.exerciseId == exerciseId }.maxOfOrNull { it.done }

    /** Best set per workout over time, oldest first. */
    fun history(exerciseId: String): List<Pair<LocalDate, Int>> =
        logs.sortedBy { it.id }.mapNotNull { log ->
            log.sets.filter { it.exerciseId == exerciseId }.maxOfOrNull { it.done }?.let { log.date to it }
        }

    fun saveWorkout(plan: WorkoutPlan, sets: List<SetLog>, seconds: Int, effort: Int, now: Long = System.currentTimeMillis(), date: LocalDate = LocalDate.now()): WorkoutResult {
        val previousBests = sets.map { it.exerciseId }.distinct().associateWith { bestFor(it) }
        val log = WorkoutLog(now, date, plan.key, plan.title, seconds, sets, effort.coerceIn(1, 5))
        logs.add(log)

        val bests = previousBests.filter { (id, old) ->
            val newBest = sets.filter { it.exerciseId == id }.maxOf { it.done }
            old != null && newBest > old
        }.keys.toList()

        val changes = mutableListOf<ProgressionChange>()
        sets.filter { !it.max }.groupBy { it.exerciseId }.forEach { (id, exerciseSets) ->
            val allHit = exerciseSets.all { it.done >= it.target }
            val rate = exerciseSets.sumOf { min(it.done, it.target) }.toDouble() / exerciseSets.sumOf { it.target }.coerceAtLeast(1)
            when {
                allHit -> {
                    misses[id] = 0
                    val h = (hits[id] ?: 0) + 1
                    if (h >= HITS_TO_LEVEL_UP) {
                        hits[id] = 0
                        bonus[id] = (bonus[id] ?: 0) + 1
                        changes += ProgressionChange(id, Direction.UP, bonus[id]!!)
                    } else hits[id] = h
                }
                rate < 0.6 -> {
                    hits[id] = 0
                    val m = (misses[id] ?: 0) + 1
                    if (m >= MISSES_TO_EASE_OFF) {
                        misses[id] = 0
                        val eased = ((bonus[id] ?: 0) - 1).coerceAtLeast(MIN_BONUS)
                        if (eased != (bonus[id] ?: 0)) {
                            bonus[id] = eased
                            changes += ProgressionChange(id, Direction.DOWN, eased)
                        }
                    } else misses[id] = m
                }
                else -> hits[id] = 0
            }
        }

        val dayParts = plan.key.split(".")
        if (dayParts.size == 3 && dayParts[0] == "p") dayParts[2].toIntOrNull()?.let { completeDay(dayParts[1], it, date) }

        val points = 10 + seconds / 300
        ProgressStore.award(points, date)
        save()
        return WorkoutResult(points, bests, changes)
    }

    fun deleteLog(id: Long) {
        logs.removeAll { it.id == id }
        save()
    }

    fun workoutDates(): Set<LocalDate> = logs.mapTo(HashSet()) { it.date }

    fun doneOn(date: LocalDate) = logs.any { it.date == date }

    fun thisWeek(today: LocalDate = LocalDate.now()): List<WorkoutLog> {
        val monday = today.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY))
        return logs.filter { !it.date.isBefore(monday) && !it.date.isAfter(today) }
    }

    /** Weeks in a row (ending this week or last) where you hit your planned number of workouts. */
    fun weekStreak(today: LocalDate = LocalDate.now()): Int {
        val target = days.size.coerceAtLeast(1)
        var monday = today.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY))
        val countIn = { m: LocalDate -> logs.map { it.date }.distinct().count { !it.isBefore(m) && it.isBefore(m.plusWeeks(1)) } }
        if (countIn(monday) < target) monday = monday.minusWeeks(1)
        var streak = 0
        while (countIn(monday) >= target) {
            streak++
            monday = monday.minusWeeks(1)
        }
        return streak
    }

    // ---------- Body ----------

    fun logBody(entry: BodyEntry) {
        body[entry.date.toString()] = entry.copy(weightKg = (entry.weightKg * 10).roundToInt() / 10.0)
        save()
    }

    fun removeBody(date: LocalDate) {
        body.remove(date.toString())
        save()
    }

    fun bodyEntries(): List<BodyEntry> = body.values.sortedBy { it.date }

    fun latestBody(): BodyEntry? = bodyEntries().lastOrNull()

    fun weighInDue(today: LocalDate = LocalDate.now()): Boolean {
        val last = latestBody() ?: return true
        return ChronoUnit.DAYS.between(last.date, today) >= 6
    }

    /** Change over roughly the last four weeks, with goal-aware, safety-first wording. */
    fun weightTrend(today: LocalDate = LocalDate.now()): WeightTrend? {
        val entries = bodyEntries().filter { !it.date.isAfter(today) }
        if (entries.size < 2) return null
        val latest = entries.last()
        val baseline = entries.lastOrNull { ChronoUnit.DAYS.between(it.date, latest.date) >= 28 } ?: entries.first()
        val days = ChronoUnit.DAYS.between(baseline.date, latest.date).coerceAtLeast(1)
        val weeks = days / 7.0
        val change = latest.weightKg - baseline.weightKg
        val weekly = if (weeks < 0.5) 0.0 else change / baseline.weightKg / weeks * 100
        if (ProfileStore.isMinor) {
            return WeightTrend(change, weeks, weekly, "Weight naturally changes as you grow. Focus on getting stronger and sleeping well — that's what counts.", TrendTone.NEUTRAL)
        }
        val (message, tone) = when (goal) {
            FitnessGoal.LOSE_FAT -> when {
                weekly < -1.0 -> "You're losing faster than about 1% a week. Eat a bit more and keep protein high so you lose fat, not muscle." to TrendTone.CAUTION
                weekly <= -0.25 -> "Right on track — steady fat loss you can keep up." to TrendTone.GOOD
                weekly < 0.25 -> "Holding steady. Small changes work best: one less snack, a 20-minute walk." to TrendTone.NEUTRAL
                else -> "Up a little. Water and salt swing weight by 1–2 kg day to day, so watch the trend over weeks." to TrendTone.NEUTRAL
            }
            FitnessGoal.BUILD_MUSCLE -> when {
                weekly > 0.5 -> "Gaining quickly — some of that may be fat. Ease off the extra food a little." to TrendTone.CAUTION
                weekly >= 0.1 -> "Right on track for lean muscle gain." to TrendTone.GOOD
                else -> "Not gaining yet. Add a protein-rich snack a day and keep training hard." to TrendTone.NEUTRAL
            }
            else -> (if (abs(weekly) < 0.25) "Nice and steady." else "Weight is moving — check it matches what you want.") to TrendTone.NEUTRAL
        }
        return WeightTrend(change, weeks, weekly, message, tone)
    }

    fun waistToHeight(): Double? {
        val waist = bodyEntries().lastOrNull { it.waistCm != null }?.waistCm ?: return null
        val height = heightCm ?: return null
        return waist / height
    }

    fun clearAll() {
        Storage.remove(KEY)
        load()
        Storage.onScheduleChanged()
    }

    // ---------- Units ----------

    fun displayWeight(kg: Double): String =
        if (useLb) "%.1f lb".format(kg * 2.20462) else "%.1f kg".format(kg)

    fun displayChange(kg: Double): String {
        val value = if (useLb) kg * 2.20462 else kg
        return (if (value >= 0) "+" else "−") + "%.1f".format(kotlin.math.abs(value)) + if (useLb) " lb" else " kg"
    }

    fun toKg(value: Double): Double = if (useLb) value / 2.20462 else value

    // ---------- Persistence ----------

    private fun changed() {
        save()
        Storage.onScheduleChanged()
    }

    fun load() {
        progress.clear(); logs.clear(); bonus.clear(); hits.clear(); misses.clear(); body.clear()
        val json = Storage.read(KEY)?.let(::JSONObject)
        setUp = json?.optBoolean("setUp") ?: false
        goal = json?.optStringOrNull("goal")?.let { runCatching { FitnessGoal.valueOf(it) }.getOrNull() }
        level = json?.optStringOrNull("level")?.let { runCatching { FitnessLevel.valueOf(it) }.getOrNull() } ?: FitnessLevel.BEGINNER
        days = json?.optJSONArray("days")?.strings()?.mapNotNull { runCatching { DayOfWeek.valueOf(it) }.getOrNull() }?.toSet()
            ?: setOf(DayOfWeek.MONDAY, DayOfWeek.WEDNESDAY, DayOfWeek.FRIDAY)
        minutes = json?.optInt("minutes", 10) ?: 10
        reminder = json?.optStringOrNull("reminder")?.let(LocalTime::parse)
        weighDay = json?.optStringOrNull("weighDay")?.let { runCatching { DayOfWeek.valueOf(it) }.getOrNull() } ?: DayOfWeek.MONDAY
        weighReminder = json?.optBoolean("weighReminder") ?: false
        heightCm = json?.takeIf { it.has("height") && !it.isNull("height") }?.getInt("height")
        useLb = json?.optBoolean("lb") ?: false
        targetKg = json?.takeIf { it.has("target") && !it.isNull("target") }?.getDouble("target")
        voiceOn = json?.optBoolean("voice", true) ?: true
        activeProgramme = json?.optStringOrNull("active")
        json?.optJSONObject("progress")?.let { p ->
            p.keys().forEach { id ->
                val days = p.getJSONObject(id)
                progress[id] = days.keys().asSequence().associate { it.toInt() to days.getString(it) }
            }
        }
        json?.optJSONArray("logs")?.objects()?.forEach { l ->
            logs.add(
                WorkoutLog(
                    l.getLong("id"), LocalDate.parse(l.getString("date")), l.getString("key"), l.getString("title"),
                    l.getInt("seconds"),
                    l.getJSONArray("sets").objects().map { SetLog(it.getString("e"), it.getInt("t"), it.getInt("d"), it.optBoolean("m")) },
                    l.getInt("effort"),
                ),
            )
        }
        fun ints(name: String, into: MutableMap<String, Int>) = json?.optJSONObject(name)?.let { o -> o.keys().forEach { into[it] = o.getInt(it) } }
        ints("bonus", bonus); ints("hits", hits); ints("misses", misses)
        json?.optJSONObject("body")?.let { b ->
            b.keys().forEach { day ->
                val e = b.getJSONObject(day)
                body[day] = BodyEntry(LocalDate.parse(day), e.getDouble("kg"), if (e.isNull("waist")) null else e.getDouble("waist"))
            }
        }
    }

    private fun save() {
        val json = JSONObject()
            .put("setUp", setUp)
            .put("goal", goal?.name ?: JSONObject.NULL)
            .put("level", level.name)
            .put("days", days.map { it.name }.toJsonArray())
            .put("minutes", minutes)
            .put("reminder", reminder?.toString() ?: JSONObject.NULL)
            .put("weighDay", weighDay.name)
            .put("weighReminder", weighReminder)
            .put("height", heightCm ?: JSONObject.NULL)
            .put("lb", useLb)
            .put("target", targetKg ?: JSONObject.NULL)
            .put("voice", voiceOn)
            .put("active", activeProgramme ?: JSONObject.NULL)
            .put("progress", JSONObject().also { p -> progress.forEach { (id, d) -> p.put(id, JSONObject(d.mapKeys { it.key.toString() })) } })
            .put(
                "logs",
                JSONArray().also { arr ->
                    logs.forEach { l ->
                        arr.put(
                            JSONObject().put("id", l.id).put("date", l.date.toString()).put("key", l.key).put("title", l.title)
                                .put("seconds", l.seconds).put("effort", l.effort)
                                .put("sets", JSONArray().also { s -> l.sets.forEach { s.put(JSONObject().put("e", it.exerciseId).put("t", it.target).put("d", it.done).put("m", it.max)) } }),
                        )
                    }
                },
            )
            .put("bonus", JSONObject(bonus.toMap()))
            .put("hits", JSONObject(hits.toMap()))
            .put("misses", JSONObject(misses.toMap()))
            .put("body", JSONObject().also { b -> body.forEach { (d, e) -> b.put(d, JSONObject().put("kg", e.weightKg).put("waist", e.waistCm ?: JSONObject.NULL)) } })
        Storage.write(KEY, json.toString())
    }
}
