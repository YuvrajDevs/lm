package com.glowup.app.ui.screens.morning

data class SessionStep(val title: String, val detail: String, val seconds: Int, val breath: Breath? = null)

enum class Breath { IN, HOLD, OUT, REST }

data class MorningSession(val title: String, val meta: String, val steps: List<SessionStep>) {
    val totalSeconds: Int get() = steps.sumOf { it.seconds }
}

private fun breathingCycles(count: Int) = List(count) {
    listOf(
        SessionStep("Breathe in", "Slowly through your nose", 4, Breath.IN),
        SessionStep("Hold", "Stay relaxed", 4, Breath.HOLD),
        SessionStep("Breathe out", "Long and slow through your mouth", 6, Breath.OUT),
        SessionStep("Rest", "Let the next breath come naturally", 2, Breath.REST)
    )
}.flatten()

val morningSessions = listOf(
    MorningSession(
        "Guided meditation",
        "5 min · Guided",
        listOf(
            SessionStep("Settle in", "Sit comfortably. Rest your hands and close your eyes if that feels okay.", 30),
            SessionStep("Three slow breaths", "In through the nose, out through the mouth. Let each one be a little slower.", 30),
            SessionStep("Feel the ground", "Notice where your body touches the chair or floor. Let it hold your weight.", 40),
            SessionStep("Follow the breath", "Breathe normally. Just notice each breath coming in and going out.", 50),
            SessionStep("Wandering is fine", "When your mind drifts, that's normal. Gently come back to the breath.", 50),
            SessionStep("Body scan", "Move your attention from head to feet. Soften your jaw, shoulders and hands.", 50),
            SessionStep("Look forward", "Think of one thing you're looking forward to today.", 30),
            SessionStep("Come back", "Take one deeper breath. Open your eyes when you're ready.", 20)
        )
    ),
    MorningSession("Breathing exercise", "2 min · Guided", breathingCycles(8)),
    MorningSession(
        "Stretching",
        "4 min · Guided",
        listOf(
            SessionStep("Neck tilts", "Ear towards shoulder, hold, then switch sides. Keep shoulders down.", 30),
            SessionStep("Shoulder rolls", "Roll both shoulders slowly backwards, then forwards.", 30),
            SessionStep("Chest opener", "Clasp hands behind your back and gently lift. Open the chest.", 30),
            SessionStep("Side stretch", "Reach one arm overhead and lean to the side. Switch halfway.", 30),
            SessionStep("Forward fold", "Soft knees, let your upper body hang. Nod your head yes and no.", 30),
            SessionStep("Hip flexor lunge", "Step one foot forward, sink your hips. Switch legs halfway.", 30),
            SessionStep("Hamstring reach", "One heel forward, hinge at the hips with a long back. Switch halfway.", 30),
            SessionStep("Calf raises", "Rise onto your toes and lower slowly. Steady, controlled reps.", 30)
        )
    ),
    MorningSession(
        "Simple yoga flow",
        "6 min · Guided",
        listOf(
            SessionStep("Mountain pose", "Stand tall, feet hip-width, arms by your sides. Breathe into your belly.", 60),
            SessionStep("Cat–cow", "On hands and knees, arch and round your back with each breath.", 60),
            SessionStep("Downward dog", "Lift your hips up and back. Pedal your heels to loosen the calves.", 60),
            SessionStep("Low lunge", "Step one foot forward and lift your chest. Switch sides halfway.", 60),
            SessionStep("Child's pose", "Knees wide, sit back on your heels, arms long. Rest your forehead down.", 60),
            SessionStep("Seated twist", "Sit tall, twist gently to one side, then the other. Breathe slowly.", 60)
        )
    )
)
