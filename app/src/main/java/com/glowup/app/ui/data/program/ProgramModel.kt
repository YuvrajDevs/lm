package com.glowup.app.ui.data.program

import androidx.compose.ui.graphics.Color
import com.glowup.app.R
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

/** The seven areas of the programme, in the order they appear within each week. */
enum class ProgramArea(val label: String, val tagline: String, val tint: Color, val accent: Color, val icon: Int) {
    COMMUNICATION("Communication", "Speak so people listen", BlueTint, Navy, R.drawable.ic_speech),
    GROOMING("Grooming", "Look after the details", CoralTint, Coral, R.drawable.ic_looks),
    STYLE("Style", "Dress with intent", PurpleTint, Purple, R.drawable.ic_style),
    CONFIDENCE("Confidence", "Presence and body language", GreenTint, Green, R.drawable.ic_determined),
    MIND("Mind", "Think, learn and decide better", YellowTint, Color(0xFF8A6A00), R.drawable.ic_mind),
    SOCIAL("Social", "Connect and keep people close", TealTint, Teal, R.drawable.ic_social),
    HABITS("Habits & body", "Energy, discipline, systems", MaroonTint, Maroon, R.drawable.ic_lifeadmin),
}

data class Quiz(val question: String, val options: List<String>, val answer: Int, val why: String)

data class KeyIdea(val heading: String, val body: String)

data class ProgramSession(
    val index: Int,
    val area: ProgramArea,
    val title: String,
    val minutes: Int,
    val hook: String,
    val ideas: List<KeyIdea>,
    val drill: String,
    val quiz: Quiz,
) {
    val week: Int get() = index / Program.DAYS_PER_WEEK + 1
    val day: Int get() = index + 1
    val dayOfWeek: Int get() = index % Program.DAYS_PER_WEEK + 1
    val phase: String get() = Program.phaseFor(week)

    /** Later sessions are worth more, so progress keeps feeling earned. */
    val points: Int get() = 10 + (week - 1) * 2
}

/** One authored session before it's placed in the programme. */
data class TrackSession(
    val title: String,
    val minutes: Int,
    val hook: String,
    val ideas: List<KeyIdea>,
    val drill: String,
    val quiz: Quiz,
)

internal fun lesson(
    title: String,
    minutes: Int,
    hook: String,
    ideas: List<Pair<String, String>>,
    drill: String,
    quiz: Quiz,
) = TrackSession(title, minutes, hook, ideas.map { (heading, body) -> KeyIdea(heading, body) }, drill, quiz)

internal fun quiz(question: String, vararg options: String, answer: Int, why: String) =
    Quiz(question, options.toList(), answer, why)

object Program {
    const val WEEKS = 12
    const val DAYS_PER_WEEK = 7
    const val TOTAL = WEEKS * DAYS_PER_WEEK

    private val tracks: Map<ProgramArea, List<TrackSession>> = mapOf(
        ProgramArea.COMMUNICATION to communicationTrack,
        ProgramArea.GROOMING to groomingTrack,
        ProgramArea.STYLE to styleTrack,
        ProgramArea.CONFIDENCE to confidenceTrack,
        ProgramArea.MIND to mindTrack,
        ProgramArea.SOCIAL to socialTrack,
        ProgramArea.HABITS to habitsTrack,
    )

    val sessions: List<ProgramSession> by lazy {
        (0 until TOTAL).map { index ->
            val area = ProgramArea.entries[index % DAYS_PER_WEEK]
            val authored = tracks.getValue(area)[index / DAYS_PER_WEEK]
            ProgramSession(index, area, authored.title, authored.minutes, authored.hook, authored.ideas, authored.drill, authored.quiz)
        }
    }

    fun phaseFor(week: Int) = when (week) {
        in 1..4 -> "Foundations"
        in 5..8 -> "Building"
        else -> "Mastery"
    }

    fun sessionsInWeek(week: Int) = sessions.subList((week - 1) * DAYS_PER_WEEK, week * DAYS_PER_WEEK)
}
