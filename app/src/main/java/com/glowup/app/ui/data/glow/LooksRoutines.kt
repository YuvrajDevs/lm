package com.glowup.app.ui.data.glow

enum class Evidence(val label: String, val blurb: String) {
    STRONG("Strong evidence", "Well supported by research."),
    MODERATE("Some evidence", "Reasonable support; results vary."),
    LOW("Evidence: low", "Popular online, but little proof it changes your face. Try it if you like — don't expect miracles."),
    ADVICE("Style advice", "Not a health claim — just what tends to look good."),
}

data class LooksRoutine(
    val id: String,
    val title: String,
    val area: FocusArea,
    val evidence: Evidence,
    val minutes: Int,
    val summary: String,
    val steps: List<String>,
    val safety: String? = null,
    /** Added to the user's routines with one tap. */
    val routineTitle: String,
    val routineCategory: String,
    val weekly: Boolean = false,
    /** Optional link deeper into the app, e.g. a fitness programme. */
    val route: String? = null,
    val routeLabel: String? = null,
)

/** What the big paid looksmaxxing courses cover — with honest evidence labels. */
object LooksRoutines {
    val all: List<LooksRoutine> = listOf(
        LooksRoutine(
            "posture-fix", "Posture fix", FocusArea.POSTURE, Evidence.MODERATE, 6,
            "Forward head and rounded shoulders make the jaw look softer and you look shorter. Five minutes a day of these drills makes a visible difference in weeks.",
            listOf(
                "Chin tucks × 10 — glide your head straight back, hold 3 seconds.",
                "Wall angels × 10 — back flat on a wall, slide arms up and down.",
                "Doorway chest stretch × 30 seconds.",
                "Prone Y raises × 10 — squeeze between your shoulder blades.",
                "Through the day: ears over shoulders, screen at eye level.",
            ),
            routineTitle = "Posture reset — chin tucks & wall angels", routineCategory = "Fitness",
            route = "programme/posture", routeLabel = "Open the 30-day Posture programme",
        ),
        LooksRoutine(
            "fat-loss-face", "Lean out for face definition", FocusArea.BODY, Evidence.STRONG, 20,
            "The most effective thing for a sharper jawline and cheekbones is lowering body fat. No face exercise comes close.",
            listOf(
                "Eat in a small calorie deficit — about 300–500 kcal a day below maintenance.",
                "Hit your protein: roughly 1.6 g per kg of body weight.",
                "Train 3–4 times a week so you lose fat, not muscle.",
                "Walk 8,000+ steps a day.",
                "Cut back on salt and alcohol before events — both make the face puffy.",
            ),
            safety = "Lose no more than about 1% of your body weight a week. Under 18? Focus on training and good food, not dieting.",
            routineTitle = "Workout — today's session", routineCategory = "Fitness",
            route = "fitness", routeLabel = "Open Fitness",
        ),
        LooksRoutine(
            "jaw-neck", "Jaw & neck training", FocusArea.JAW_NECK, Evidence.MODERATE, 4,
            "A stronger neck makes the jaw-to-neck line look sharper from the side. Neck training is real; jaw \"exercises\" mostly aren't.",
            listOf(
                "Neck curls × 15 — lie on a bed with your head off the edge, tuck chin to chest slowly.",
                "Neck extensions × 15 — face down, head off the edge, lift your head up slowly.",
                "Chin tucks × 10 — for posture and a cleaner side profile.",
                "Go slow and controlled — never jerk the neck.",
            ),
            safety = "Skip if you have neck pain. Jaw-trainer gadgets and hours of hard gum can strain the jaw joint (TMJ) — we don't recommend them.",
            routineTitle = "Neck curls & chin tucks", routineCategory = "Fitness",
        ),
        LooksRoutine(
            "tongue-posture", "Tongue posture (mewing)", FocusArea.JAW_NECK, Evidence.LOW, 1,
            "Resting your whole tongue on the roof of your mouth is a popular habit. There's no good evidence it reshapes an adult face — but nasal breathing and a closed mouth at rest are healthy habits anyway.",
            listOf(
                "Lips closed, teeth lightly together or just apart.",
                "Rest the whole tongue (including the back) gently against the roof of the mouth.",
                "Breathe through your nose.",
                "Check in a few times a day — don't force or push hard.",
            ),
            safety = "Never push hard with the tongue or clench. If you get jaw pain or headaches, stop.",
            routineTitle = "Tongue posture check-in", routineCategory = "Looks",
        ),
        LooksRoutine(
            "skin-basics", "Skin basics (AM / PM)", FocusArea.SKIN, Evidence.STRONG, 4,
            "Clear, even skin does more for how you look than almost anything else. Three products, twice a day, is enough for most people.",
            listOf(
                "Morning: gentle cleanser → moisturiser → sunscreen SPF 30+ (every day, even indoors near windows).",
                "Night: cleanser → moisturiser.",
                "Change your pillowcase twice a week.",
                "Don't pick spots — it causes marks that last for months.",
            ),
            safety = "Persistent acne, painful cysts or sudden changes? See a dermatologist — prescription treatments work far better than anything online.",
            routineTitle = "Skincare — morning & night", routineCategory = "Looks",
            route = "looks", routeLabel = "Open grooming lessons",
        ),
        LooksRoutine(
            "hair-face-shape", "Hair for your face shape", FocusArea.GROOMING, Evidence.ADVICE, 2,
            "The right cut for your face shape is the fastest visible upgrade there is. Getting it trimmed regularly keeps it looking intentional.",
            listOf(
                "Find your face shape (the Glow Check can estimate it).",
                "Pick one of the cuts recommended for your shape and save a photo to show the barber.",
                "Use the barber script — tell them exactly what you want.",
                "Book a trim every 3–5 weeks.",
                "Learn one styling routine: towel dry, a little product, shape with fingers.",
            ),
            routineTitle = "Book a haircut", routineCategory = "Life admin", weekly = true,
        ),
        LooksRoutine(
            "beard-shape", "Beard by face shape", FocusArea.GROOMING, Evidence.ADVICE, 5,
            "A shaped beard can add a jawline where there isn't one — or soften a very strong one.",
            listOf(
                "Neckline: imagine a curve from behind each ear to about one finger above the Adam's apple. Shave below it.",
                "Cheek line: keep it natural or a clean straight line — don't cut it too low.",
                "Trim the chin longer or shorter depending on your face shape.",
                "Moisturise the skin under the beard and use a little beard oil.",
            ),
            routineTitle = "Beard line-up", routineCategory = "Looks", weekly = true,
        ),
        LooksRoutine(
            "brows-small-grooming", "Brows & small details", FocusArea.GROOMING, Evidence.ADVICE, 5,
            "Small things people notice up close: brows, nose and ear hair, nails, lips.",
            listOf(
                "Brows: remove only the hairs between them and any obvious strays underneath. Don't thin the shape.",
                "Trim nose and ear hair weekly.",
                "Keep nails short and clean.",
                "Lip balm daily — cracked lips are very noticeable.",
            ),
            routineTitle = "Grooming check — brows, nails, nose & ears", routineCategory = "Looks", weekly = true,
        ),
        LooksRoutine(
            "teeth-breath", "Teeth & breath", FocusArea.GROOMING, Evidence.STRONG, 4,
            "A clean smile and fresh breath matter more than perfect teeth.",
            listOf(
                "Brush twice a day for 2 minutes with fluoride toothpaste.",
                "Floss or use interdental brushes every night.",
                "Scrape your tongue in the morning — it's the main cause of bad breath.",
                "See a dentist every 6–12 months.",
            ),
            safety = "Whitening: use dentist-approved products only. Avoid DIY charcoal or acid hacks — they damage enamel.",
            routineTitle = "Floss & tongue scrape", routineCategory = "Looks",
        ),
        LooksRoutine(
            "eyes-under-eye", "Eyes & under-eyes", FocusArea.SLEEP, Evidence.MODERATE, 3,
            "Dark circles and puffiness are mostly sleep, salt, alcohol and allergies.",
            listOf(
                "Aim for 7–9 hours of sleep at a consistent time.",
                "Cold compress or cold spoons for 2 minutes in the morning reduces puffiness.",
                "Cut salty food and alcohol the night before big days.",
                "20-20-20 rule on screens: every 20 minutes, look 20 feet away for 20 seconds.",
            ),
            routineTitle = "Wind down — screens off 30 min before bed", routineCategory = "Sleep",
            route = "sleep_log", routeLabel = "Open your sleep log",
        ),
        LooksRoutine(
            "body-grooming-fragrance", "Body grooming & fragrance", FocusArea.GROOMING, Evidence.ADVICE, 5,
            "Clean, trimmed and a light signature scent.",
            listOf(
                "Trim body hair with a guard rather than shaving to avoid irritation.",
                "Use deodorant or antiperspirant every morning.",
                "Fragrance: 2–3 sprays (neck and wrists), never more. Fresh scents for day, warmer ones at night.",
            ),
            routineTitle = "Body grooming", routineCategory = "Looks", weekly = true,
        ),
        LooksRoutine(
            "symmetry-habits", "Balance habits", FocusArea.SYMMETRY, Evidence.LOW, 1,
            "Small everyday habits that may help with uneven muscle use. Most faces are slightly asymmetrical — that's normal and usually invisible to others.",
            listOf(
                "Chew on both sides — notice if you always use one.",
                "Sleep on your back or alternate sides.",
                "Don't lean your face on one hand at your desk.",
                "Carry bags on alternate shoulders.",
            ),
            routineTitle = "Chew both sides · sleep on your back", routineCategory = "Looks",
        ),
        LooksRoutine(
            "presence", "Voice & presence", FocusArea.CONFIDENCE, Evidence.MODERATE, 5,
            "How you carry yourself changes how attractive you seem as much as your features do.",
            listOf(
                "Stand tall, shoulders back and down.",
                "Hold eye contact a beat longer than feels natural.",
                "Slow your speech down and pause before answering.",
                "Smile with your eyes, not just your mouth.",
            ),
            routineTitle = "2-minute eye contact practice", routineCategory = "Social",
            route = "communication", routeLabel = "Open Talk lessons",
        ),
        LooksRoutine(
            "style-basics", "Dress for your build", FocusArea.STYLE, Evidence.ADVICE, 5,
            "Fit is everything. A cheap shirt that fits beats an expensive one that doesn't.",
            listOf(
                "Shoulder seams should sit right at your shoulder bone.",
                "Trousers: a slight taper, no bunching at the ankle.",
                "Build outfits around 2–3 neutral colours.",
                "Lay out tomorrow's outfit the night before.",
            ),
            routineTitle = "Lay out tomorrow's outfit", routineCategory = "Style",
            route = "styling", routeLabel = "Open style guides",
        ),
    )

    fun get(id: String) = all.firstOrNull { it.id == id }

    fun forArea(area: FocusArea) = all.filter { it.area == area }
}
