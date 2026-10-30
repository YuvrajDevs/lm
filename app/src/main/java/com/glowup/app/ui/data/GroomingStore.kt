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

data class GroomingLesson(
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
    val steps: List<String>,
)

/** Looks & grooming lesson library — skincare, hair, grooming, posture — shared by the Looks list and its detail screen. */
object GroomingStore {
    val lessons = listOf(
        GroomingLesson(
            1, "Skincare", "Build your AM routine", "Three steps, five minutes, every morning.", 3, 10,
            CoralTint, Coral, R.drawable.ic_looks,
            steps = listOf(
                "Cleanse with a gentle, pH-balanced face wash — no bar soap on your face.",
                "Moisturize while skin is still a little damp to lock in hydration.",
                "Finish with SPF 30+, even on cloudy days and indoors near windows.",
                "Keep it to 3 steps. More products isn't more progress at the start.",
            ),
        ),
        GroomingLesson(
            2, "Skincare", "Build your PM routine", "Undo the day in three honest steps.", 3, 10,
            CoralTint, Coral, R.drawable.ic_looks,
            steps = listOf(
                "Double cleanse if you wore sunscreen or makeup — oil cleanser, then your normal one.",
                "Add a treatment 2–3x a week (retinoid or exfoliant), not every single night at first.",
                "Moisturize to finish, even if your skin feels oily.",
                "Change your pillowcase weekly — it collects oil and bacteria.",
            ),
        ),
        GroomingLesson(
            3, "Skincare", "Deal with breakouts calmly", "Fewer products, more patience, better results.", 4, 12,
            CoralTint, Coral, R.drawable.ic_looks,
            steps = listOf(
                "Don't pick — it's the single biggest cause of lasting marks.",
                "Introduce one new active at a time so you know what's actually working.",
                "Benzoyl peroxide or salicylic acid are the proven first steps, not expensive serums.",
                "Give any routine 6–8 weeks before judging it — skin cycles are slow.",
            ),
        ),
        GroomingLesson(
            4, "Skincare", "Fix dark circles & puffiness", "Mostly about sleep and habits, not creams.", 3, 10,
            CoralTint, Coral, R.drawable.ic_looks,
            steps = listOf(
                "Consistent sleep timing matters more than total hours for puffiness.",
                "A cold spoon or chilled eye roller for 60 seconds reduces morning puffiness fast.",
                "Cut salty food the night before an important day — it's the biggest same-day cause.",
                "An eye cream with caffeine can help, but it's a finishing touch, not a fix.",
            ),
        ),
        GroomingLesson(
            5, "Hair", "Pick a cut for your face shape", "Work with your face, not against it.", 4, 12,
            BlueTint, Navy, R.drawable.ic_mind,
            steps = listOf(
                "Round faces usually suit height on top and shorter sides for contrast.",
                "Square faces can wear almost anything — soften sharp angles with texture if you want.",
                "Long faces benefit from fringe or volume on the sides, not more height on top.",
                "Bring 2–3 reference photos to your barber instead of describing it from memory.",
            ),
        ),
        GroomingLesson(
            6, "Hair", "Basic hair care habits", "Healthier hair without a 10-step routine.", 3, 10,
            BlueTint, Navy, R.drawable.ic_mind,
            steps = listOf(
                "Wash 2–4x a week for most hair types — daily washing can strip natural oils.",
                "Always use conditioner; it's what actually prevents breakage, not the shampoo.",
                "Let hair air-dry most of the time; heat styling daily damages faster than people expect.",
                "A monthly trim keeps the shape intact even while you're growing it out.",
            ),
        ),
        GroomingLesson(
            7, "Hair", "Style it like you mean it", "Five minutes of product and direction.", 3, 10,
            BlueTint, Navy, R.drawable.ic_mind, pro = true,
            steps = listOf(
                "Apply product to towel-dried, not soaking wet, hair for better hold.",
                "Match the product to your goal — clay for matte texture, pomade for shine and hold.",
                "Style with your fingers first before reaching for a comb — it looks less deliberate.",
                "A travel-size product in your bag saves you after humidity or a long day.",
            ),
        ),
        GroomingLesson(
            8, "Grooming", "Eyebrow grooming basics", "Cleaner without looking \"done.\"", 3, 10,
            GreenTint, Green, R.drawable.ic_determined,
            steps = listOf(
                "Brush brows upward first so you can see your natural shape before trimming anything.",
                "Only pluck the obvious strays between the brows and well below the natural arch.",
                "Trim long hairs with small scissors, not a razor, to avoid over-thinning.",
                "When in doubt, do less — brows grow back slower than you'd like.",
            ),
        ),
        GroomingLesson(
            9, "Grooming", "Beard care & shaping", "A clean line changes the whole face.", 4, 12,
            GreenTint, Green, R.drawable.ic_determined,
            steps = listOf(
                "Wash your beard with a dedicated beard wash — face cleanser can dry it out.",
                "Define your neckline just above the Adam's apple, not right under the jaw.",
                "Beard oil on damp (not wet) hair softens it and reduces itchiness as it grows.",
                "Trim every 1–2 weeks to keep the shape even as it grows out.",
            ),
        ),
        GroomingLesson(
            10, "Grooming", "Smell is part of the look", "Understated, not overwhelming.", 2, 8,
            GreenTint, Green, R.drawable.ic_determined,
            steps = listOf(
                "Apply fragrance to pulse points right after a shower, on slightly damp skin.",
                "2–3 sprays is plenty — if you can smell it all day, it's too much for others.",
                "Unscented deodorant lets a good fragrance actually do its job without clashing.",
                "Fresh breath and clean clothes matter more than any cologne.",
            ),
        ),
        GroomingLesson(
            11, "Posture & Smile", "Fix everyday slouching", "Small posture cues that compound fast.", 3, 10,
            PurpleTint, Purple, R.drawable.ic_reflective,
            steps = listOf(
                "Set a reminder to check your posture every hour until it becomes automatic.",
                "Imagine a string pulling the top of your head toward the ceiling while sitting.",
                "Strengthen your upper back, not just your core — weak backs are the main slouch cause.",
                "Adjust your screen to eye level; looking down all day is a posture killer.",
            ),
        ),
        GroomingLesson(
            12, "Posture & Smile", "Teeth & smile care", "A confident smile starts with basics.", 3, 10,
            PurpleTint, Purple, R.drawable.ic_reflective, pro = true,
            steps = listOf(
                "Brush twice a day for two minutes — most people underbrush on time, not effort.",
                "Floss at night; it's the step that actually prevents long-term issues.",
                "See a dentist twice a year even if nothing hurts — prevention is cheaper than fixes.",
                "Whitening strips work, but ask a dentist first if you have sensitive teeth.",
            ),
        ),
    )

    val completedIds = mutableStateListOf(1)

    fun find(id: Int) = lessons.firstOrNull { it.id == id }

    fun isDone(id: Int) = id in completedIds

    fun markDone(id: Int) {
        if (id !in completedIds) completedIds.add(id)
    }
}
