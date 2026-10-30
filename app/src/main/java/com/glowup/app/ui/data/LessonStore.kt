package com.glowup.app.ui.data

import androidx.compose.runtime.mutableStateListOf
import androidx.compose.ui.graphics.Color
import com.glowup.app.R
import com.glowup.app.ui.theme.BlueTint
import com.glowup.app.ui.theme.Coral
import com.glowup.app.ui.theme.CoralTint
import com.glowup.app.ui.theme.Green
import com.glowup.app.ui.theme.GreenTint
import com.glowup.app.ui.theme.Navy
import com.glowup.app.ui.theme.Purple
import com.glowup.app.ui.theme.PurpleTint

data class Lesson(
    val id: Int,
    val track: String,
    val title: String,
    val tagline: String,
    val minutes: Int,
    val points: Int,
    val tint: Color,
    val accent: Color,
    val icon: Int,
    val pro: Boolean = false,
    val keyPoints: List<String>,
    val practiceWord: String,
)

/** Lesson library and completion state, shared by the Communication list and lesson detail screen. */
object LessonStore {
    val lessons = listOf(
        Lesson(
            1, "Confidence", "Get less nervous", "Calm your body before you even open your mouth.", 3, 10,
            GreenTint, Green, R.drawable.ic_determined,
            keyPoints = listOf(
                "Breathe out for longer than you breathe in — it tells your nervous system you're safe.",
                "Nerves and excitement feel the same in your body. Relabel \"I'm nervous\" as \"I'm ready.\"",
                "Pick one friendly face in the room and talk to them first.",
                "Shaky hands and a dry mouth are normal — nobody can see your heartbeat.",
            ),
            practiceWord = "A time I was nervous and it went fine anyway",
        ),
        Lesson(
            2, "Clarity", "Stop mumbling", "Slow down and finish your words — people will lean in, not ask you to repeat.", 3, 10,
            BlueTint, Navy, R.drawable.ic_talking,
            keyPoints = listOf(
                "Slow down by 20%. Most mumbling is just talking faster than your mouth can form words.",
                "Finish the ends of your sentences — that's where clarity usually drops off.",
                "Open your mouth a little wider than feels natural; it's the easiest clarity fix there is.",
                "Record yourself for 10 seconds and listen back once. You'll hear it immediately.",
            ),
            practiceWord = "What I did this morning",
        ),
        Lesson(
            3, "Confidence", "Speak with confidence", "Own the pause instead of rushing through it.", 4, 12,
            CoralTint, Coral, R.drawable.ic_speech, pro = true,
            keyPoints = listOf(
                "A pause feels like forever to you and like nothing to everyone listening — use it.",
                "Drop the uptalk. End statements on a flat or falling tone, not a question mark.",
                "Stand or sit like you already belong in the conversation — posture changes your voice.",
                "Confidence is a delivery skill, not a personality trait. It's trainable.",
            ),
            practiceWord = "Something I'm proud of",
        ),
        Lesson(
            4, "Clarity", "Cut the filler words", "\"Um\", \"like\", and \"you know\" are costing you authority.", 3, 10,
            PurpleTint, Purple, R.drawable.ic_mind,
            keyPoints = listOf(
                "Replace filler words with a silent pause — it feels longer to you than it sounds to others.",
                "Notice your top filler word this week before trying to fix all of them at once.",
                "Filler words spike when you're thinking ahead. Slow down and finish the current sentence first.",
                "A clean pause reads as confidence; a filler word reads as hesitation.",
            ),
            practiceWord = "My ideal weekend",
        ),
        Lesson(
            5, "Connection", "Start a conversation", "Openers that don't feel forced, even with strangers.", 3, 10,
            GreenTint, Green, R.drawable.ic_social,
            keyPoints = listOf(
                "Comment on something shared in the moment — the room, the event, the wait — before asking questions.",
                "Skip \"How are you?\" — try \"What's been the best part of your week?\" instead.",
                "You don't need a clever line. \"Hey, I don't think we've met\" works almost every time.",
                "The goal of an opener is just to get a reply, not to impress anyone.",
            ),
            practiceWord = "How I'd start a conversation with a stranger",
        ),
        Lesson(
            6, "Connection", "Keep it going", "Turn small talk into an actual conversation.", 4, 12,
            BlueTint, Navy, R.drawable.ic_talking,
            keyPoints = listOf(
                "Ask a follow-up before you ask a new question — it shows you were actually listening.",
                "Share something small about yourself after they answer. Conversation is a trade, not an interview.",
                "Use \"what\" and \"how\" questions over \"why\" — they feel less like an interrogation.",
                "Comfortable silence is fine. Not every gap needs to be filled instantly.",
            ),
            practiceWord = "A conversation I enjoyed recently",
        ),
        Lesson(
            7, "Connection", "Active listening", "Make people feel properly heard, not just heard out.", 3, 10,
            PurpleTint, Purple, R.drawable.ic_reflective,
            keyPoints = listOf(
                "Put your phone face-down and actually look at the person talking.",
                "Reflect back what you heard in one short sentence before replying with your own point.",
                "Resist planning your response while they're still talking — you'll miss half of it.",
                "Nod and give small verbal cues (\"right\", \"yeah\") instead of staying silent.",
            ),
            practiceWord = "What a good listener does differently",
        ),
        Lesson(
            8, "Conflict & Work", "Disagree without a fight", "Push back on an idea without attacking the person.", 4, 12,
            CoralTint, Coral, R.drawable.ic_determined, pro = true,
            keyPoints = listOf(
                "Open with the part you agree with before you introduce the disagreement.",
                "Say \"I see it differently\" instead of \"you're wrong\" — same point, no defensiveness.",
                "Separate the idea from the person. You're allowed to challenge one without attacking the other.",
                "Ask \"help me understand why\" before assuming bad intent.",
            ),
            practiceWord = "A disagreement I handled well",
        ),
        Lesson(
            9, "Conflict & Work", "Speak up in meetings", "Get your point in without waiting for the perfect gap.", 4, 12,
            BlueTint, Navy, R.drawable.ic_mind, pro = true,
            keyPoints = listOf(
                "Prepare one sentence in advance — the hardest part is usually just starting.",
                "Use a bridge phrase like \"Building on that...\" to enter the conversation smoothly.",
                "Say your point in the first sentence, then explain — don't bury the headline.",
                "If you miss your window, message it right after. Being heard matters more than timing.",
            ),
            practiceWord = "A point I wish I'd made in a meeting",
        ),
    )

    val completedIds = mutableStateListOf(1)

    fun find(id: Int) = lessons.firstOrNull { it.id == id }

    fun isDone(id: Int) = id in completedIds

    fun markDone(id: Int) {
        if (id !in completedIds) completedIds.add(id)
    }

    fun nextUpAfter(id: Int) = lessons.firstOrNull { it.id > id && it.id !in completedIds }
}
