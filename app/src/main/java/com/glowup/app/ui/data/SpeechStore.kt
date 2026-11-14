package com.glowup.app.ui.data

import androidx.compose.runtime.mutableStateListOf
import java.time.LocalDate
import kotlin.math.roundToInt
import kotlin.math.sqrt
import org.json.JSONArray
import org.json.JSONObject

/** Loudness sample from the recogniser: [ms] since start, [db] its RMS level. */
data class LevelSample(val ms: Long, val db: Float)

data class FillerCount(val word: String, val count: Int)

data class SpeechReport(
    val words: Int,
    val seconds: Int,
    val wpm: Int,
    val fillers: List<FillerCount>,
    val longPauses: Int,
    val longestPauseSeconds: Double,
    /** 0–100: how much the volume moves while speaking. Low = monotone. */
    val variety: Int,
    val repeats: Int
) {
    val fillerTotal: Int get() = fillers.sumOf { it.count }
    val fillersPerMinute: Double get() = if (seconds == 0) 0.0 else fillerTotal * 60.0 / seconds

    data class Note(val title: String, val body: String)

    /** Up to three things that went well and two to work on, from the numbers. */
    fun wentWell(): List<Note> = buildList {
        if (wpm in 125..165) add(Note("Great pace", "$wpm words a minute — easy to follow and confident."))
        if (fillersPerMinute < 2) add(Note("Clean delivery", "Almost no filler words. That sounds sure of itself."))
        if (longPauses in 1..3 && wpm in 110..170) add(Note("Good use of pauses", "A few deliberate pauses help people take in what you said."))
        if (variety >= 55) add(Note("Expressive voice", "Your volume rises and falls naturally, which keeps people listening."))
        if (seconds >= 55) add(Note("Kept it going", "You held the floor for the whole minute."))
    }.take(3)

    fun workOn(): List<Note> = buildList {
        if (wpm > 170) add(Note("Slow down a little", "$wpm words a minute is fast. Aim for 130–160 — pause at full stops."))
        if (wpm in 1..109) add(Note("Pick up the pace", "$wpm words a minute can sound unsure. Try shorter sentences and keep going."))
        if (fillersPerMinute >= 3) {
            val top = fillers.maxByOrNull { it.count }
            val lead = if (top != null && top.count > 1 && fillers.count { it.count == top.count } == 1) "You said \"${top.word}\" the most. " else ""
            add(Note("Swap fillers for pauses", lead + "When you feel a filler coming, stop for a beat instead — a pause sounds confident."))
        }
        if (longPauses > 4) add(Note("Fewer long gaps", "$longPauses pauses over 2 seconds. Plan your next point while finishing the current one."))
        if (variety in 1..34) add(Note("Add some colour", "Your voice stayed quite flat. Lean on the important words and let your volume move."))
        if (repeats >= 3) add(Note("Watch the restarts", "You repeated words $repeats times (\"I I\", \"the the\"). Slow down at the start of sentences."))
        if (seconds < 30) add(Note("Speak for longer", "Try to keep going for the full time — even if you lose your thread, keep talking."))
    }.take(2)
}

data class SpeechSession(val id: Long, val date: LocalDate, val topic: String, val report: SpeechReport, val transcript: String)

object SpeechAnalysis {
    private val singleFillers = setOf("um", "umm", "uh", "uhh", "er", "erm", "ah", "hmm", "like", "basically", "literally", "actually", "so", "right")
    private val phraseFillers = listOf("you know", "i mean", "kind of", "sort of")

    fun words(text: String): List<String> = text.lowercase().replace(Regex("[^a-z' ]"), " ").split(" ").filter { it.isNotBlank() }

    fun fillers(text: String): List<FillerCount> {
        val w = words(text)
        val counts = mutableMapOf<String, Int>()
        val joined = " " + w.joinToString(" ") + " "
        phraseFillers.forEach { p -> Regex(" $p ").findAll(joined).count().takeIf { it > 0 }?.let { counts[p] = it } }
        w.forEachIndexed { i, word ->
            if (word !in singleFillers) return@forEachIndexed
            // "so" and "right" only count at the start of a thought, "like" not after "I/you/would" (real verb).
            val prev = w.getOrNull(i - 1)
            val counted = when (word) {
                "so", "right" -> prev == null || prev == word || prev in singleFillers
                "like" -> prev !in setOf("i", "you", "we", "they", "would", "really", "don't", "dont", "feel", "looks", "look", "sounds")
                else -> true
            }
            if (counted) {
                val key = when (word) {
                    "umm" -> "um"
                    "uhh" -> "uh"
                    "erm" -> "er"
                    else -> word
                }
                counts[key] = (counts[key] ?: 0) + 1
            }
        }
        return counts.map { FillerCount(it.key, it.value) }.sortedByDescending { it.count }
    }

    fun repeats(text: String): Int = words(text).zipWithNext().count { (a, b) -> a == b && a.length > 0 && a !in setOf("very", "really", "no", "bye") }

    fun analyse(transcript: String, seconds: Int, levels: List<LevelSample>): SpeechReport {
        val w = words(transcript)
        // Silence: well below your typical speaking level, measured against the room's background noise.
        val sorted = levels.map { it.db }.sorted()
        val floor = sorted.getOrNull(sorted.size / 20) ?: 0f
        val median = sorted.getOrNull(sorted.size / 2) ?: 0f
        val threshold = floor + (median - floor) * 0.3f
        var pauses = 0
        var longest = 0.0
        var silentMs = 0L
        var silentStart: Long? = null
        fun closeRun(endMs: Long) {
            val start = silentStart ?: return
            val len = (endMs - start) / 1000.0
            // Dips under 0.7 s are just gaps between words.
            if (len >= 0.7) silentMs += endMs - start
            if (len >= 2.0) pauses++
            longest = maxOf(longest, len)
            silentStart = null
        }
        levels.forEach { s ->
            if (s.db <= threshold) {
                if (silentStart == null) silentStart = s.ms
            } else {
                closeRun(s.ms)
            }
        }
        levels.lastOrNull()?.let { closeRun(it.ms) }
        val speaking = levels.filter { it.db > threshold }
        val speakingSeconds = (seconds - silentMs / 1000.0).coerceAtLeast(seconds * 0.4)
        val wpm = if (speakingSeconds <= 0) 0 else (w.size * 60 / speakingSeconds).roundToInt()
        val variety = if (speaking.size < 5) {
            0
        } else {
            val mean = speaking.map { it.db }.average()
            val sd = sqrt(speaking.sumOf { (it.db - mean) * (it.db - mean) } / speaking.size)
            (sd * 18).roundToInt().coerceIn(0, 100)
        }
        return SpeechReport(w.size, seconds, wpm, fillers(transcript), pauses, (longest * 10).roundToInt() / 10.0, variety, repeats(transcript))
    }
}

object SpeechStore {
    private const val KEY = "speech"
    const val KEEP = 30

    val sessions = mutableStateListOf<SpeechSession>()

    fun save(topic: String, report: SpeechReport, transcript: String, today: LocalDate = LocalDate.now(), now: Long = System.currentTimeMillis()) {
        sessions.add(0, SpeechSession(now, today, topic, report, transcript.take(4000)))
        while (sessions.size > KEEP) sessions.removeAt(sessions.lastIndex)
        persist()
    }

    fun delete(id: Long) {
        sessions.removeAll { it.id == id }
        persist()
    }

    fun clearAll() {
        sessions.clear()
        Storage.remove(KEY)
    }

    fun load() {
        sessions.clear()
        val raw = Storage.read(KEY) ?: return
        JSONArray(raw).objects().forEach { j ->
            val r = j.getJSONObject("r")
            val fillers = r.getJSONObject("f").let { f -> f.keys().asSequence().map { FillerCount(it, f.getInt(it)) }.sortedByDescending { it.count }.toList() }
            sessions.add(
                SpeechSession(
                    j.getLong("id"),
                    LocalDate.parse(j.getString("date")),
                    j.getString("topic"),
                    SpeechReport(r.getInt("w"), r.getInt("s"), r.getInt("wpm"), fillers, r.getInt("p"), r.getDouble("lp"), r.getInt("v"), r.getInt("rep")),
                    j.optString("t")
                )
            )
        }
    }

    private fun persist() {
        val arr = JSONArray()
        sessions.forEach { s ->
            val r = s.report
            arr.put(
                JSONObject().put("id", s.id).put("date", s.date.toString()).put("topic", s.topic).put("t", s.transcript)
                    .put(
                        "r",
                        JSONObject().put("w", r.words).put("s", r.seconds).put("wpm", r.wpm).put("p", r.longPauses).put("lp", r.longestPauseSeconds)
                            .put("v", r.variety).put("rep", r.repeats).put("f", JSONObject(r.fillers.associate { it.word to it.count }))
                    )
            )
        }
        Storage.write(KEY, arr.toString())
    }
}

data class Scenario(val id: String, val category: String, val title: String, val prompt: String, val tip: String, val thinkSeconds: Int, val speakSeconds: Int)

/** Real situations to practise, with a short think time first — like the moment itself. */
object Scenarios {
    val all: List<Scenario> = listOf(
        Scenario(
            "intro-30",
            "Introductions",
            "Introduce yourself",
            "You've just joined a new team. Introduce yourself in 30 seconds.",
            "Name, what you do, one interesting detail, a question back.",
            10,
            30
        ),
        Scenario(
            "intro-party",
            "Introductions",
            "Party small talk",
            "Someone at a party asks, \"So how do you know the host?\" Keep it going.",
            "Answer, add a short story, then ask them something.",
            5,
            45
        ),
        Scenario(
            "intro-elevator",
            "Introductions",
            "Elevator pitch",
            "Pitch yourself or your idea before the lift doors open.",
            "Problem → what you do → why it matters → ask.",
            15,
            45
        ),
        Scenario(
            "job-about",
            "Job interview",
            "Tell me about yourself",
            "The classic opener. You have one minute.",
            "Present → past → future. End with why this role.",
            20,
            60
        ),
        Scenario(
            "job-strength",
            "Job interview",
            "Your biggest strength",
            "\"What's your biggest strength?\"",
            "Name it, prove it with a short story, link it to the job.",
            15,
            60
        ),
        Scenario(
            "job-weakness",
            "Job interview",
            "Your weakness",
            "\"What's a weakness you're working on?\"",
            "A real one, what you're doing about it, progress so far.",
            20,
            60
        ),
        Scenario(
            "job-conflict",
            "Job interview",
            "A conflict at work",
            "\"Tell me about a time you disagreed with someone.\"",
            "Situation, what you did, the result. Stay generous to the other person.",
            20,
            75
        ),
        Scenario(
            "job-why",
            "Job interview",
            "Why should we hire you?",
            "Close the interview with a strong answer.",
            "Three reasons, each tied to what they need.",
            15,
            45
        ),
        Scenario(
            "date-first",
            "Dating",
            "First-date opener",
            "You've just sat down. Get the conversation flowing.",
            "Comment on the place, ask an open question, share something light.",
            5,
            45
        ),
        Scenario(
            "date-passion",
            "Dating",
            "Something you love",
            "\"What are you into at the moment?\" Make it interesting.",
            "Show energy, give a detail they can ask about.",
            10,
            60
        ),
        Scenario(
            "date-story",
            "Dating",
            "Tell a funny story",
            "Share a short story that makes them laugh.",
            "Set the scene in one line, build up, land the ending.",
            15,
            60
        ),
        Scenario(
            "story-trip",
            "Storytelling",
            "Your best trip",
            "Describe the best trip you've taken so someone wants to go.",
            "Use the senses — what you saw, heard, ate.",
            15,
            60
        ),
        Scenario(
            "story-lesson",
            "Storytelling",
            "A lesson you learned",
            "Tell a story about something that changed how you think.",
            "Before, the moment, after.",
            20,
            75
        ),
        Scenario(
            "story-fail",
            "Storytelling",
            "A time you failed",
            "Share a failure and what it taught you.",
            "Be honest, keep it short, end on the lesson.",
            20,
            60
        ),
        Scenario(
            "opinion-agree",
            "Opinions",
            "Disagree politely",
            "A friend says social media is all bad. Give your view respectfully.",
            "Acknowledge their point, add yours, give an example.",
            15,
            60
        ),
        Scenario(
            "opinion-defend",
            "Opinions",
            "Defend a choice",
            "Explain a decision you made that others questioned.",
            "Your reasoning, what you considered, what you'd do again.",
            15,
            60
        ),
        Scenario(
            "opinion-recommend",
            "Opinions",
            "Recommend something",
            "Convince a friend to read a book or watch a show you love.",
            "Hook, why it's good, who it's for.",
            10,
            45
        ),
        Scenario(
            "work-update",
            "Work & study",
            "Give a quick update",
            "Your manager asks, \"Where are we on this?\"",
            "Status, blockers, next step. Under 40 seconds.",
            10,
            40
        ),
        Scenario(
            "work-explain",
            "Work & study",
            "Explain something tricky",
            "Explain how something works (the internet, a recipe, your job) to a 10-year-old.",
            "One analogy, three steps, no jargon.",
            20,
            60
        ),
        Scenario(
            "work-ask",
            "Work & study",
            "Ask for something",
            "Ask your boss for a day off or a raise.",
            "What you want, why it's fair, make it easy to say yes.",
            20,
            45
        ),
        Scenario(
            "work-feedback",
            "Work & study",
            "Give feedback",
            "Tell a teammate their presentation ran too long — kindly.",
            "Something good, the issue, a specific suggestion.",
            15,
            45
        ),
        Scenario(
            "work-present",
            "Work & study",
            "Open a presentation",
            "Give the first 45 seconds of a talk on any topic.",
            "Start with a question or surprising fact. No \"um, so…\".",
            20,
            45
        ),
        Scenario(
            "phone-call",
            "Everyday",
            "Make a phone call",
            "Call a restaurant to book a table for six on Friday with a special request.",
            "Who you are, what you need, confirm the details.",
            10,
            40
        ),
        Scenario(
            "everyday-complaint",
            "Everyday",
            "Return an item",
            "Your new headphones broke after a week. Ask for a refund.",
            "Calm, clear, what you'd like them to do.",
            10,
            45
        ),
        Scenario(
            "everyday-directions",
            "Everyday",
            "Give directions",
            "A tourist asks how to get to the nearest station.",
            "Landmarks, left/right, check they've got it.",
            10,
            30
        ),
        Scenario(
            "social-toast",
            "Social",
            "Give a toast",
            "Raise a glass at a friend's birthday dinner.",
            "A short memory, what you admire, the toast.",
            20,
            60
        ),
        Scenario(
            "social-comfort",
            "Social",
            "Comfort a friend",
            "A friend just failed an exam. Say something helpful.",
            "Listen-first tone: acknowledge, empathise, offer help.",
            10,
            45
        ),
        Scenario(
            "social-group",
            "Social",
            "Join a group conversation",
            "You walk up to friends talking about weekend plans. Jump in.",
            "React to what's being said, then add your piece.",
            5,
            30
        ),
        Scenario(
            "random-object",
            "Quick fire",
            "Sell this object",
            "Pick up the nearest object and sell it to me.",
            "Benefits, not features. End with a call to action.",
            10,
            45
        ),
        Scenario(
            "random-hottake",
            "Quick fire",
            "Hot take",
            "Argue that breakfast is the most overrated meal — or the opposite.",
            "Pick a side and commit. Three reasons.",
            10,
            45
        )
    )

    val categories: List<String> get() = all.map { it.category }.distinct()

    fun get(id: String) = all.firstOrNull { it.id == id }

    /** Free users get the first two scenarios in each category. */
    fun isFree(s: Scenario) = all.filter { it.category == s.category }.indexOf(s) < 2
}
