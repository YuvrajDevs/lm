package com.glowup.app.ui.data.glow

import androidx.compose.runtime.mutableStateListOf
import com.glowup.app.ui.data.ProfileStore
import com.glowup.app.ui.data.SleepLogStore
import com.glowup.app.ui.data.SleepMath
import com.glowup.app.ui.data.Storage
import com.glowup.app.ui.data.objects
import com.glowup.app.ui.data.optStringOrNull
import com.glowup.app.ui.data.strings
import com.glowup.app.ui.data.toJsonArray
import java.time.LocalDate
import java.time.temporal.ChronoUnit
import kotlin.math.abs
import org.json.JSONArray
import org.json.JSONObject

/** Things a user can actually change. Fixed traits (bone structure, eye spacing) are never areas. */
enum class FocusArea(val title: String, val tagline: String) {
    BODY("Body composition", "The biggest lever for a sharper face"),
    POSTURE("Posture", "Stand taller, look sharper from the side"),
    SKIN("Skin", "Clear, even, protected"),
    GROOMING("Grooming", "Hair, beard and the details"),
    SLEEP("Sleep", "Less puffiness, better mood and focus"),
    STYLE("Style", "Clothes that fit and work together"),
    JAW_NECK("Jaw & neck", "A cleaner side profile"),
    SYMMETRY("Balance habits", "Small daily habits"),
    CONFIDENCE("Presence", "How you carry yourself")
}

data class Question(val id: String, val text: String, val options: List<String>, val hint: String? = null)

object GlowQuestions {
    val goals = listOf("Face & jawline", "Skin", "Hair & grooming", "Body", "Posture", "Style", "Confidence", "Sleep")
    val minuteOptions = listOf(10, 20, 40)

    val all = listOf(
        Question("skinType", "What's your skin like by midday?", listOf("Oily all over", "Oily T-zone", "Dry or tight", "Normal", "Not sure")),
        Question("breakouts", "How often do you get spots?", listOf("Rarely", "Sometimes", "Often")),
        Question("skincare", "What's your skincare routine?", listOf("Nothing", "Just face wash", "Wash + moisturiser", "Wash, moisturiser & SPF")),
        Question("haircut", "How often do you get a haircut?", listOf("Every 3–5 weeks", "Every 6–8 weeks", "Every few months or less")),
        Question("facialHair", "Facial hair?", listOf("Clean shaven", "Stubble", "Beard", "Can't grow much")),
        Question("sleep", "How much do you usually sleep?", listOf("Under 6 hours", "6–7 hours", "7–8 hours", "8+ hours")),
        Question("exercise", "How many days a week do you exercise?", listOf("0", "1–2", "3–4", "5+")),
        Question(
            "screen",
            "Daily screen time (phone + laptop)?",
            listOf("Under 4 hours", "4–6 hours", "6–9 hours", "9+ hours"),
            hint = "Long hours at screens is the main cause of forward head posture."
        ),
        Question("style", "How confident are you in your clothes?", listOf("Not at all", "Somewhat", "Very"))
    )
}

data class AreaScore(val area: FocusArea, val room: Int, val reason: String) {
    /** 0 = a strength, 1 = some room to grow, 2 = big room to grow. */
    val roomLabel: String get() = when (room) {
        2 -> "Big room to grow"
        1 -> "Some room to grow"
        else -> "Strength"
    }
}

data class CheckResult(
    val date: LocalDate,
    val goals: Set<String>,
    val minutesPerDay: Int,
    val answers: Map<String, String>,
    val face: FaceMetrics?,
    val faceShape: String?,
    val neckAngle: Int?,
    val heightCm: Int?,
    val weightKg: Double?,
    val waistCm: Double?,
    val areas: List<AreaScore>
) {
    val focus: List<AreaScore> get() = areas.filter { it.room > 0 }.take(3)
    val strengths: List<AreaScore> get() = areas.filter { it.room == 0 }.take(2)
    val waistToHeight: Double? get() = if (waistCm != null && heightCm != null) waistCm / heightCm else null
    val bmi: Double? get() = if (weightKg != null && heightCm != null) weightKg / ((heightCm / 100.0) * (heightCm / 100.0)) else null
}

data class PlanItem(val title: String, val category: String, val weekly: Boolean, val area: FocusArea, val fromRoutine: String)

data class GlowPlan(val routines: List<PlanItem>, val programmeId: String?, val dailyMinutes: Int)

object GlowScoring {
    /** How much each area can realistically change, used to rank areas with equal room. */
    private val changeability = mapOf(
        FocusArea.BODY to 5, FocusArea.POSTURE to 5, FocusArea.SKIN to 4, FocusArea.SLEEP to 4, FocusArea.GROOMING to 4,
        FocusArea.STYLE to 3, FocusArea.CONFIDENCE to 3, FocusArea.JAW_NECK to 2, FocusArea.SYMMETRY to 1
    )

    private val goalAreas = mapOf(
        "Face & jawline" to setOf(FocusArea.BODY, FocusArea.JAW_NECK, FocusArea.POSTURE),
        "Skin" to setOf(FocusArea.SKIN),
        "Hair & grooming" to setOf(FocusArea.GROOMING),
        "Body" to setOf(FocusArea.BODY),
        "Posture" to setOf(FocusArea.POSTURE),
        "Style" to setOf(FocusArea.STYLE),
        "Confidence" to setOf(FocusArea.CONFIDENCE),
        "Sleep" to setOf(FocusArea.SLEEP)
    )

    fun score(
        goals: Set<String>,
        answers: Map<String, String>,
        face: FaceMetrics?,
        neckAngle: Int?,
        heightCm: Int?,
        weightKg: Double?,
        waistCm: Double?,
        minor: Boolean,
        loggedSleepMinutes: Int? = null
    ): List<AreaScore> {
        val scores = mutableListOf<AreaScore>()
        val wanted = goals.flatMap { goalAreas[it].orEmpty() }.toSet()

        // Posture: measured if we have the side photo, otherwise inferred from screen time.
        when {
            neckAngle != null -> when (PostureAnalysis.level(neckAngle)) {
                PostureLevel.FORWARD ->
                    scores +=
                        AreaScore(
                            FocusArea.POSTURE,
                            2,
                            "Your head sits noticeably forward of your shoulders ($neckAngle°). This is very fixable — most people see a change in 4–6 weeks."
                        )
                PostureLevel.MILD ->
                    scores +=
                        AreaScore(FocusArea.POSTURE, 1, "Your head drifts a little forward ($neckAngle°). A few minutes of drills a day straightens this up.")
                PostureLevel.GOOD -> scores += AreaScore(FocusArea.POSTURE, 0, "Your ears sit nicely over your shoulders ($neckAngle°). Keep it up.")
            }
            answers["screen"] in setOf("6–9 hours", "9+ hours") ->
                scores += AreaScore(FocusArea.POSTURE, 1, "Long hours at screens pull the head forward. A short daily posture routine counters it.")
            FocusArea.POSTURE in wanted ->
                scores +=
                    AreaScore(FocusArea.POSTURE, 1, "You want to work on posture — a side-on scan next time will give you a number to track.")
        }

        // Body composition.
        val whtr = if (waistCm != null && heightCm != null) waistCm / heightCm else null
        val bmi = if (weightKg != null && heightCm != null) weightKg / ((heightCm / 100.0) * (heightCm / 100.0)) else null
        val inactive = answers["exercise"] == "0"
        when {
            minor -> {
                if (inactive) {
                    scores +=
                        AreaScore(FocusArea.BODY, 1, "Getting active 3 times a week builds strength and confidence. Short home workouts are a great start.")
                } else if (FocusArea.BODY in wanted) {
                    scores += AreaScore(FocusArea.BODY, 1, "Strength training a few times a week builds your frame as you grow.")
                }
            }
            whtr != null && whtr >= 0.55 ->
                scores +=
                    AreaScore(
                        FocusArea.BODY,
                        2,
                        "Your waist-to-height is ${"%.2f".format(
                            whtr
                        )}. Bringing it under 0.5 is the single biggest thing for a sharper face — and your health."
                    )
            whtr != null && whtr >= 0.5 ->
                scores +=
                    AreaScore(
                        FocusArea.BODY,
                        1,
                        "Your waist-to-height is ${"%.2f".format(whtr)}, just above the 0.5 line. A small drop would sharpen your jawline."
                    )
            bmi != null && bmi < 18.5 -> scores += AreaScore(FocusArea.BODY, 1, "You're on the lean side. Building muscle would fill out your frame and face.")
            bmi != null && bmi >= 30 ->
                scores +=
                    AreaScore(FocusArea.BODY, 2, "Lowering body fat is the biggest lever for face definition — and steady, safe loss works best.")
            bmi != null && bmi >= 25 -> scores += AreaScore(FocusArea.BODY, 1, "A little less body fat would make your jaw and cheekbones more defined.")
            inactive ->
                scores +=
                    AreaScore(FocusArea.BODY, 1, "You're not exercising yet. Three short sessions a week change how you look and feel within a month.")
            whtr != null || bmi != null -> scores += AreaScore(FocusArea.BODY, 0, "Your body composition is in a healthy range.")
            FocusArea.BODY in wanted -> scores += AreaScore(FocusArea.BODY, 1, "Training 3–4 times a week will tighten everything up.")
        }

        // Skin — routine only, never a diagnosis.
        val skinRoom = when (answers["skincare"]) {
            "Nothing" -> 2
            "Just face wash" -> 1
            "Wash + moisturiser" -> 1
            "Wash, moisturiser & SPF" -> if (answers["breakouts"] == "Often") 1 else 0
            else -> if (FocusArea.SKIN in wanted) 1 else -1
        }
        if (skinRoom >= 0) {
            scores += AreaScore(
                FocusArea.SKIN,
                skinRoom,
                when {
                    skinRoom == 0 -> "You've got the essentials covered — cleanser, moisturiser and sunscreen."
                    answers["breakouts"] == "Often" -> "Frequent spots: a simple, consistent routine helps, and a dermatologist can help a lot more if it doesn't settle."
                    answers["skincare"] == "Nothing" -> "Starting a 3-step routine is one of the quickest visible upgrades."
                    else -> "Adding daily sunscreen is the single best thing for long-term skin."
                }
            )
        }

        // Grooming.
        val groomRoom = when (answers["haircut"]) {
            "Every few months or less" -> 2
            "Every 6–8 weeks" -> 1
            "Every 3–5 weeks" -> 0
            else -> if (FocusArea.GROOMING in wanted) 1 else -1
        }
        if (groomRoom >= 0) {
            val shape = face?.shape
            scores += AreaScore(
                FocusArea.GROOMING,
                groomRoom,
                if (groomRoom == 0) {
                    "Regular cuts keep you looking sharp."
                } else {
                    "A cut chosen for your${shape?.let { " ${it.lowercase()}" } ?: ""} face shape, trimmed every few weeks, is the fastest visible upgrade."
                }
            )
        }

        // Sleep: use the sleep log when there's one, otherwise the answer.
        val sleepRoom = when {
            loggedSleepMinutes != null -> when {
                loggedSleepMinutes < 360 -> 2
                loggedSleepMinutes < 420 -> 1
                else -> 0
            }
            else -> when (answers["sleep"]) {
                "Under 6 hours" -> 2
                "6–7 hours" -> 1
                "7–8 hours", "8+ hours" -> 0
                else -> -1
            }
        }
        if (sleepRoom >= 0) {
            scores += AreaScore(
                FocusArea.SLEEP,
                sleepRoom,
                when (sleepRoom) {
                    2 -> "Under 6 hours shows up as puffiness, dark circles and lower mood. More sleep is a quick win."
                    1 -> "An extra 30–60 minutes of sleep reduces puffiness and helps everything else stick."
                    else -> "You're sleeping enough — great for skin, mood and recovery."
                }
            )
        }

        // Style.
        when (answers["style"]) {
            "Not at all" -> scores += AreaScore(FocusArea.STYLE, 2, "Clothes that fit your build change first impressions instantly.")
            "Somewhat" -> scores += AreaScore(FocusArea.STYLE, 1, "A few well-fitting basics and a simple colour palette would sharpen your look.")
            "Very" -> scores += AreaScore(FocusArea.STYLE, 0, "You're confident in your clothes.")
        }

        if (FocusArea.JAW_NECK in wanted && !minor) {
            scores += AreaScore(FocusArea.JAW_NECK, 1, "Neck training and a lean body define the jaw-to-neck line. We'll be honest about what doesn't work.")
        }
        if (FocusArea.CONFIDENCE in
            wanted
        ) {
            scores += AreaScore(FocusArea.CONFIDENCE, 1, "How you carry yourself changes how you're seen as much as your features do.")
        }

        face?.let {
            if (it.symmetry <
                88
            ) {
                scores +=
                    AreaScore(
                        FocusArea.SYMMETRY,
                        1,
                        "Every face is a little uneven. A few habits — chewing on both sides, sleeping on your back — may help with muscle balance."
                    )
            } else {
                scores += AreaScore(FocusArea.SYMMETRY, 0, "Your face is well balanced (${it.symmetry}/100 — most people land between 88 and 97).")
            }
        }

        return scores.sortedWith(
            compareByDescending<AreaScore> { it.room }
                .thenByDescending { if (it.area in wanted) 1 else 0 }
                .thenByDescending { changeability[it.area] ?: 0 }
        )
    }

    /** Routines and a programme for the top focus areas, trimmed to the time the user has. */
    fun plan(result: CheckResult, minor: Boolean): GlowPlan {
        val items = mutableListOf<PlanItem>()
        fun add(routineId: String) {
            val r = LooksRoutines.get(routineId) ?: return
            if (items.none { it.title == r.routineTitle }) items += PlanItem(r.routineTitle, r.routineCategory, r.weekly, r.area, r.id)
        }
        result.focus.forEach { area ->
            when (area.area) {
                FocusArea.POSTURE -> add("posture-fix")
                FocusArea.BODY -> add("fat-loss-face")
                FocusArea.SKIN -> add("skin-basics")
                FocusArea.GROOMING -> {
                    add("hair-face-shape")
                    add("brows-small-grooming")
                }
                FocusArea.SLEEP -> add("eyes-under-eye")
                FocusArea.STYLE -> add("style-basics")
                FocusArea.JAW_NECK -> add("jaw-neck")
                FocusArea.SYMMETRY -> add("symmetry-habits")
                FocusArea.CONFIDENCE -> add("presence")
            }
        }
        // Everyone benefits from the skin basics, so add them if there's time.
        if (result.minutesPerDay >= 20) add("skin-basics")
        val budget = when (result.minutesPerDay) {
            10 -> 3
            20 -> 4
            else -> 6
        }
        val chosen = items.take(budget)

        val focusAreas = result.focus.map { it.area }
        val programme = when {
            FocusArea.POSTURE in focusAreas && focusAreas.indexOf(FocusArea.POSTURE) == 0 -> "posture"
            FocusArea.BODY in focusAreas -> when {
                minor -> "first30"
                result.bmi?.let { it < 18.5 } == true -> "lean"
                else -> "fatburn"
            }
            FocusArea.POSTURE in focusAreas -> "posture"
            else -> null
        }
        return GlowPlan(chosen, programme, result.minutesPerDay)
    }

    /** What changed since the last check — only things that can change, worded kindly. */
    fun changes(previous: CheckResult, current: CheckResult): List<String> {
        val lines = mutableListOf<String>()
        if (previous.neckAngle != null && current.neckAngle != null) {
            val d = current.neckAngle - previous.neckAngle
            lines += when {
                d >= 3 -> "Posture improved by $d° — your head sits further back over your shoulders."
                d <= -3 -> "Posture slipped by ${-d}°. Worth a few extra minutes of chin tucks this month."
                else -> "Posture is steady (${current.neckAngle}°)."
            }
        }
        if (previous.waistCm != null && current.waistCm != null) {
            val d = current.waistCm - previous.waistCm
            if (abs(d) >= 1) lines += if (d < 0) "Waist down ${"%.0f".format(-d)} cm." else "Waist up ${"%.0f".format(d)} cm."
        }
        if (previous.weightKg != null && current.weightKg != null) {
            val d = current.weightKg - previous.weightKg
            if (abs(d) >= 0.5) lines += "Weight ${if (d < 0) "down" else "up"} ${"%.1f".format(abs(d))} kg."
        }
        val before = previous.areas.associateBy { it.area }
        current.areas.forEach { now ->
            val was = before[now.area] ?: return@forEach
            if (now.room < was.room) lines += "${now.area.title}: ${was.roomLabel.lowercase()} → ${now.roomLabel.lowercase()}."
        }
        if (lines.isEmpty()) lines += "Not much has changed yet — that's normal. Most changes show up after 6–8 weeks of consistency."
        return lines
    }
}

object GlowCheckStore {
    private const val KEY = "glow_checks"
    const val RESCAN_DAYS = 28

    val results = mutableStateListOf<CheckResult>()

    /** Set when onboarding finishes so Home can open the check once. Not persisted. */
    var openAfterOnboarding = false

    val latest: CheckResult? get() = results.maxByOrNull { it.date }
    val previous: CheckResult? get() = results.sortedByDescending { it.date }.getOrNull(1)

    fun daysUntilRescan(today: LocalDate = LocalDate.now()): Int? =
        latest?.let { (RESCAN_DAYS - ChronoUnit.DAYS.between(it.date, today)).toInt().coerceAtLeast(0) }

    fun build(
        goals: Set<String>,
        minutes: Int,
        answers: Map<String, String>,
        face: FaceMetrics?,
        manualShape: String?,
        neckAngle: Int?,
        heightCm: Int?,
        weightKg: Double?,
        waistCm: Double?,
        today: LocalDate = LocalDate.now()
    ): CheckResult {
        val minor = ProfileStore.isMinor
        val logged = SleepLogStore.recent(today, 7).takeIf { it.size >= 3 }?.let(SleepMath::averageMinutes)
        val areas = GlowScoring.score(goals, answers, face, neckAngle, heightCm, if (minor) null else weightKg, if (minor) null else waistCm, minor, logged)
        return CheckResult(today, goals, minutes, answers, face, face?.shape ?: manualShape, neckAngle, heightCm, weightKg, waistCm, areas)
    }

    fun save(result: CheckResult) {
        results.removeAll { it.date == result.date }
        results.add(result)
        result.faceShape?.let(ProfileStore::updateFaceShape)
        persist()
    }

    fun clearAll() {
        results.clear()
        Storage.remove(KEY)
    }

    fun load() {
        results.clear()
        val raw = Storage.read(KEY) ?: return
        JSONArray(raw).objects().forEach { j ->
            val face = j.optJSONObject("face")?.let {
                FaceMetrics(it.getString("shape"), it.getDouble("lw"), it.getDouble("fc"), it.getDouble("jc"), it.getInt("sym"), it.getDouble("lower"))
            }
            val answers = j.getJSONObject("answers").let { a -> a.keys().asSequence().associateWith { a.getString(it) } }
            val areas = j.getJSONArray("areas").objects().mapNotNull { a ->
                runCatching { AreaScore(FocusArea.valueOf(a.getString("area")), a.getInt("room"), a.getString("reason")) }.getOrNull()
            }
            fun dbl(k: String) = if (j.has(k) && !j.isNull(k)) j.getDouble(k) else null
            results.add(
                CheckResult(
                    LocalDate.parse(j.getString("date")), j.getJSONArray("goals").strings().toSet(), j.getInt("minutes"), answers, face,
                    j.optStringOrNull("shape"), if (j.isNull("neck")) null else j.getInt("neck"),
                    if (j.isNull("height")) null else j.getInt("height"), dbl("weight"), dbl("waist"), areas
                )
            )
        }
    }

    private fun persist() {
        val arr = JSONArray()
        results.forEach { r ->
            arr.put(
                JSONObject()
                    .put("date", r.date.toString())
                    .put("goals", r.goals.toList().toJsonArray())
                    .put("minutes", r.minutesPerDay)
                    .put("answers", JSONObject(r.answers))
                    .put(
                        "face",
                        r.face?.let {
                            JSONObject().put(
                                "shape",
                                it.shape
                            ).put(
                                "lw",
                                it.lengthToWidth
                            ).put("fc", it.foreheadToCheek).put("jc", it.jawToCheek).put("sym", it.symmetry).put("lower", it.lowerFaceRatio)
                        }
                            ?: JSONObject.NULL
                    )
                    .put("shape", r.faceShape ?: JSONObject.NULL)
                    .put("neck", r.neckAngle ?: JSONObject.NULL)
                    .put("height", r.heightCm ?: JSONObject.NULL)
                    .put("weight", r.weightKg ?: JSONObject.NULL)
                    .put("waist", r.waistCm ?: JSONObject.NULL)
                    .put(
                        "areas",
                        JSONArray().also { a ->
                            r.areas.forEach { a.put(JSONObject().put("area", it.area.name).put("room", it.room).put("reason", it.reason)) }
                        }
                    )
            )
        }
        Storage.write(KEY, arr.toString())
    }
}
