package com.glowup.app.ui.data.fitness

import com.glowup.app.ui.data.fitness.Measure.REPS
import com.glowup.app.ui.data.fitness.Measure.SECONDS
import com.glowup.app.ui.data.fitness.Muscle.ARMS
import com.glowup.app.ui.data.fitness.Muscle.BACK
import com.glowup.app.ui.data.fitness.Muscle.CARDIO
import com.glowup.app.ui.data.fitness.Muscle.CHEST
import com.glowup.app.ui.data.fitness.Muscle.CORE
import com.glowup.app.ui.data.fitness.Muscle.GLUTES
import com.glowup.app.ui.data.fitness.Muscle.LEGS
import com.glowup.app.ui.data.fitness.Muscle.MOBILITY
import com.glowup.app.ui.data.fitness.Muscle.SHOULDERS

/** Every exercise needs only the floor, a wall, a chair or a sturdy table. */
object ExerciseLibrary {
    private fun ex(
        id: String,
        name: String,
        primary: Muscle,
        also: Set<Muscle>,
        measure: Measure,
        level: Int,
        steps: List<String>,
        mistakes: List<String>,
        cue: String,
        easier: String? = null,
        harder: String? = null,
        perSide: Boolean = false,
        safety: String? = null,
    ) = Exercise(id, name, primary, also, measure, level, perSide, steps, mistakes, cue, easier, harder, safety)

    val all: List<Exercise> = listOf(
        // ---------------- Chest ----------------
        ex(
            "wall-pushup", "Wall push-up", CHEST, setOf(ARMS, SHOULDERS), REPS, 1,
            listOf("Stand an arm's length from a wall, hands on it at shoulder height and a bit wider than shoulders.", "Keep your body in one straight line from head to heels.", "Bend your elbows to bring your chest towards the wall.", "Push back until your arms are straight."),
            listOf("Letting the hips sag or stick out", "Flaring elbows straight out to the sides", "Only moving the head towards the wall"),
            "Breathe in as you lower, out as you push away.",
            harder = "incline-pushup",
        ),
        ex(
            "incline-pushup", "Incline push-up", CHEST, setOf(ARMS, SHOULDERS), REPS, 1,
            listOf("Put your hands on a sturdy table, bench or kitchen counter, slightly wider than shoulders.", "Walk your feet back until your body is a straight plank.", "Lower your chest to the edge with elbows at about 45°.", "Press back up to straight arms."),
            listOf("Hips dropping towards the floor", "Using a surface that can slide or tip", "Half reps that stop well short of the edge"),
            "In on the way down, out as you press.",
            easier = "wall-pushup", harder = "knee-pushup",
        ),
        ex(
            "knee-pushup", "Knee push-up", CHEST, setOf(ARMS, SHOULDERS, CORE), REPS, 1,
            listOf("Start on hands and knees, hands slightly wider than shoulders.", "Walk your hands forward so there's a straight line from knees to head.", "Lower your chest towards the floor, elbows at 45°.", "Push the floor away until your arms are straight."),
            listOf("Bending at the hips instead of keeping a straight line", "Dropping the head before the chest", "Elbows flared out to 90°"),
            "Breathe in down, out up. Squeeze your glutes the whole time.",
            easier = "incline-pushup", harder = "pushup",
        ),
        ex(
            "pushup", "Push-up", CHEST, setOf(ARMS, SHOULDERS, CORE), REPS, 2,
            listOf("Hands just wider than shoulders, fingers spread, body in a straight line from head to heels.", "Brace your abs and squeeze your glutes.", "Lower until your chest is a fist's height from the floor, elbows at about 45°.", "Push back up to straight arms without letting the hips lead."),
            listOf("Sagging lower back", "Flaring elbows straight out", "Tiny half reps", "Craning the neck forward"),
            "In on the way down, out as you push. Slow down, don't speed up, when it gets hard.",
            easier = "knee-pushup", harder = "decline-pushup",
        ),
        ex(
            "wide-pushup", "Wide push-up", CHEST, setOf(SHOULDERS), REPS, 2,
            listOf("Set up like a normal push-up but with hands about 1.5× shoulder width.", "Keep a straight body line.", "Lower slowly until your chest nearly touches the floor.", "Press back up."),
            listOf("Hands so wide the shoulders hurt", "Hips sagging"),
            "Lower for two seconds, press for one.",
            easier = "knee-pushup", harder = "archer-pushup",
        ),
        ex(
            "diamond-pushup", "Diamond push-up", ARMS, setOf(CHEST, SHOULDERS), REPS, 3,
            listOf("Make a diamond shape with your thumbs and index fingers under your chest.", "Body straight, feet together or hip-width.", "Lower your chest to your hands, keeping elbows close to your sides.", "Push back up."),
            listOf("Elbows flaring out", "Hips piking up to make it easier"),
            "Breathe out hard as you press.",
            easier = "knee-pushup", harder = "archer-pushup",
            safety = "Skip if you feel wrist pain — use close-grip push-ups instead.",
        ),
        ex(
            "decline-pushup", "Decline push-up", CHEST, setOf(SHOULDERS, ARMS, CORE), REPS, 3,
            listOf("Put your feet on a chair or step, hands on the floor slightly wider than shoulders.", "Brace so your body forms a straight line.", "Lower your chest towards the floor.", "Press back up."),
            listOf("Hips sagging", "Feet on something that can slide"),
            "Out on the push, in on the way down.",
            easier = "pushup", harder = "archer-pushup",
        ),
        ex(
            "archer-pushup", "Archer push-up", CHEST, setOf(ARMS, SHOULDERS, CORE), REPS, 3,
            listOf("Start in a very wide push-up position.", "Lower towards one hand, keeping the other arm nearly straight.", "Push back to the middle.", "Alternate sides — each side counts once."),
            listOf("Bending both arms equally (that's just a wide push-up)", "Twisting the hips"),
            "Slow and controlled. Breathe out as you push.",
            easier = "wide-pushup", perSide = true,
        ),
        ex(
            "negative-pushup", "Slow negative push-up", CHEST, setOf(ARMS, CORE), REPS, 2,
            listOf("Start at the top of a push-up.", "Take a full 4 seconds to lower to the floor.", "Drop your knees and push back up any way you can.", "Reset at the top and repeat."),
            listOf("Dropping fast in the last few inches", "Letting the back sag"),
            "Count out loud — four, three, two, one.",
            easier = "knee-pushup", harder = "pushup",
        ),
        ex(
            "close-pushup", "Close-grip push-up", ARMS, setOf(CHEST), REPS, 2,
            listOf("Hands directly under your shoulders.", "Keep elbows tucked against your ribs.", "Lower until your chest is just above your hands.", "Press back up."),
            listOf("Elbows drifting out", "Shrugging the shoulders to the ears"),
            "Feel the back of your arms working. Out on the press.",
            easier = "knee-pushup", harder = "diamond-pushup",
        ),

        // ---------------- Back & posture ----------------
        ex(
            "superman", "Superman", BACK, setOf(GLUTES), REPS, 1,
            listOf("Lie face down with arms stretched out in front.", "Lift your arms, chest and legs a few inches off the floor.", "Hold for one second at the top.", "Lower slowly."),
            listOf("Cranking the neck up — keep looking at the floor", "Jerking up with momentum"),
            "Breathe out as you lift.",
            harder = "superman-hold",
        ),
        ex(
            "superman-hold", "Superman hold", BACK, setOf(GLUTES), SECONDS, 2,
            listOf("Lie face down, arms overhead.", "Lift arms, chest and legs off the floor.", "Hold still, squeezing your glutes and upper back."),
            listOf("Holding your breath", "Looking up at the wall"),
            "Slow, steady breaths through the nose.",
            easier = "superman",
        ),
        ex(
            "reverse-snow-angel", "Reverse snow angel", BACK, setOf(SHOULDERS), REPS, 1,
            listOf("Lie face down, arms by your sides, palms down.", "Lift your arms and chest slightly off the floor.", "Sweep your arms out and overhead in a wide arc, then back down.", "Keep them hovering the whole time."),
            listOf("Letting the arms touch the floor", "Rushing the arc"),
            "Breathe out as the arms travel overhead.",
        ),
        ex(
            "towel-row", "Doorframe towel row", BACK, setOf(ARMS), REPS, 1,
            listOf("Loop a strong towel around a sturdy door frame or pole and hold both ends.", "Lean back with straight arms, feet close to the frame.", "Pull your chest towards your hands, squeezing your shoulder blades.", "Lower back slowly."),
            listOf("Shrugging the shoulders", "Bending at the hips instead of staying straight"),
            "Out as you pull, in as you lower. Walk feet closer to make it harder.",
            harder = "table-row",
            safety = "Only use a frame or pole that can take your full weight.",
        ),
        ex(
            "table-row", "Table row", BACK, setOf(ARMS, CORE), REPS, 2,
            listOf("Lie under a sturdy table and grip its edge, hands shoulder-width.", "Keep your body straight from heels to head.", "Pull your chest up to the table edge.", "Lower under control."),
            listOf("Hips sagging", "Using a table that can tip over"),
            "Pull your elbows to your back pockets.",
            easier = "towel-row",
            safety = "Test the table first — it must not move or tip when you hang from it.",
        ),
        ex(
            "bird-dog", "Bird dog", CORE, setOf(BACK, GLUTES), REPS, 1,
            listOf("Start on hands and knees, hands under shoulders, knees under hips.", "Reach one arm forward and the opposite leg back until both are level with your body.", "Pause for two seconds without your hips tipping.", "Return and switch sides."),
            listOf("Arching the lower back", "Rotating the hips open"),
            "Imagine balancing a cup of tea on your lower back.",
            perSide = true,
        ),
        ex(
            "prone-y-raise", "Prone Y raise", BACK, setOf(SHOULDERS), REPS, 1,
            listOf("Lie face down, arms overhead in a Y shape, thumbs up.", "Lift your arms off the floor by squeezing between your shoulder blades.", "Hold one second, lower slowly."),
            listOf("Lifting with the neck", "Shrugging up to the ears"),
            "Breathe out as you lift. Great for rounded shoulders.",
        ),

        // ---------------- Shoulders ----------------
        ex(
            "pike-pushup", "Pike push-up", SHOULDERS, setOf(ARMS), REPS, 2,
            listOf("From a push-up position, walk your feet in so your hips are high — an upside-down V.", "Bend your elbows to bring the top of your head towards the floor between your hands.", "Press back up."),
            listOf("Letting the hips drop (turns it into a push-up)", "Elbows flaring wide"),
            "In on the way down, out as you press up.",
            easier = "knee-pushup", harder = "elevated-pike",
        ),
        ex(
            "elevated-pike", "Feet-up pike push-up", SHOULDERS, setOf(ARMS, CORE), REPS, 3,
            listOf("Put your feet on a chair, hands on the floor, hips high.", "Lower your head towards the floor.", "Press back up until your arms are straight."),
            listOf("Rushing the bottom position", "Chair sliding"),
            "Slow on the way down.",
            easier = "pike-pushup",
            safety = "Stop if you feel neck pain.",
        ),
        ex(
            "arm-circles", "Arm circles", SHOULDERS, setOf(MOBILITY), SECONDS, 1,
            listOf("Stand tall, arms out to your sides at shoulder height.", "Make small circles forward, gradually getting bigger.", "Halfway through, reverse direction."),
            listOf("Shrugging the shoulders", "Letting the arms drop below shoulder height"),
            "Easy breathing — this is a warm-up.",
        ),
        ex(
            "shoulder-taps", "Plank shoulder taps", CORE, setOf(SHOULDERS), REPS, 2,
            listOf("Hold a high plank, feet a bit wider than hips.", "Lift one hand and tap the opposite shoulder.", "Place it back down and switch.", "Keep the hips still — no rocking."),
            listOf("Hips swaying side to side", "Rushing"),
            "Steady breathing. Widen your feet to make it easier.",
            easier = "plank", perSide = true,
        ),
        ex(
            "bear-crawl", "Bear crawl", SHOULDERS, setOf(CORE, LEGS, CARDIO), SECONDS, 2,
            listOf("Start on hands and feet with knees hovering an inch above the floor.", "Move forward by stepping opposite hand and foot together.", "Keep your back flat and hips low.", "Crawl forward and back for the time."),
            listOf("Hips high in the air", "Knees touching the floor"),
            "Short, quick breaths. Small steps.",
        ),
        ex(
            "plank-up-down", "Plank up-down", ARMS, setOf(CORE, SHOULDERS), REPS, 2,
            listOf("Start in a forearm plank.", "Push up onto one hand, then the other, into a high plank.", "Lower back to your forearms one arm at a time.", "Alternate the leading arm."),
            listOf("Hips swaying", "Rushing so form breaks"),
            "Keep breathing — don't hold your breath.",
            easier = "plank",
        ),

        // ---------------- Arms ----------------
        ex(
            "chair-dip", "Chair dip", ARMS, setOf(SHOULDERS, CHEST), REPS, 1,
            listOf("Sit on the edge of a sturdy chair, hands gripping the edge beside your hips.", "Slide your hips forward off the seat, knees bent.", "Bend your elbows to lower your hips until your arms are at about 90°.", "Press back up."),
            listOf("Going too deep (shoulders hurt)", "Elbows flaring out", "Chair sliding backwards"),
            "Out as you press up. Straighten your legs to make it harder.",
            harder = "diamond-pushup",
            safety = "Put the chair against a wall. Skip if you have shoulder pain.",
        ),
        ex(
            "towel-curl", "Towel curl hold", ARMS, emptySet(), SECONDS, 1,
            listOf("Stand on the middle of a towel, holding both ends.", "Curl your hands up to 90° at the elbow.", "Pull up hard against your feet and hold."),
            listOf("Leaning back", "Holding your breath"),
            "Breathe steadily while pulling at about 80% effort.",
        ),

        // ---------------- Core ----------------
        ex(
            "crunch", "Crunch", CORE, emptySet(), REPS, 1,
            listOf("Lie on your back, knees bent, feet flat.", "Hands lightly by your temples or crossed on your chest.", "Curl your shoulders off the floor by squeezing your abs.", "Lower slowly."),
            listOf("Pulling on your neck", "Using momentum to swing up"),
            "Breathe out as you curl up.",
            harder = "toe-touch",
        ),
        ex(
            "bicycle-crunch", "Bicycle crunch", CORE, emptySet(), REPS, 2,
            listOf("Lie on your back, hands by your temples, legs lifted with knees bent.", "Bring one elbow towards the opposite knee while straightening the other leg.", "Switch sides in a slow pedalling motion.", "Each side counts as one."),
            listOf("Yanking the neck", "Going too fast to control"),
            "Breathe out on every twist.",
            easier = "crunch", perSide = true,
        ),
        ex(
            "reverse-crunch", "Reverse crunch", CORE, emptySet(), REPS, 1,
            listOf("Lie on your back, arms by your sides, knees bent at 90° in the air.", "Curl your hips off the floor, bringing knees towards your chest.", "Lower slowly until your feet nearly touch the floor."),
            listOf("Swinging the legs", "Letting the lower back arch on the way down"),
            "Out as you curl in.",
            harder = "leg-raise",
        ),
        ex(
            "leg-raise", "Lying leg raise", CORE, emptySet(), REPS, 2,
            listOf("Lie on your back, hands under your hips.", "Keep your legs straight and lift them up to vertical.", "Lower slowly until they hover just off the floor."),
            listOf("Lower back lifting off the floor", "Dropping the legs fast"),
            "Press your lower back down the whole time. Bend the knees if it pulls.",
            easier = "reverse-crunch", harder = "v-up",
        ),
        ex(
            "flutter-kicks", "Flutter kicks", CORE, emptySet(), SECONDS, 2,
            listOf("Lie on your back, hands under your hips, legs straight.", "Lift both legs a few inches off the floor.", "Kick them up and down in small, quick movements."),
            listOf("Lower back arching off the floor", "Big, slow kicks"),
            "Keep breathing steadily.",
            easier = "dead-bug",
        ),
        ex(
            "plank", "Forearm plank", CORE, setOf(SHOULDERS, GLUTES), SECONDS, 1,
            listOf("Forearms on the floor, elbows under shoulders.", "Step your feet back so your body is a straight line.", "Brace your abs like you're about to be poked, squeeze your glutes.", "Hold."),
            listOf("Hips sagging or piking up", "Looking forward instead of down", "Holding your breath"),
            "Slow breaths. If your back hurts, drop to your knees.",
            easier = "knee-plank", harder = "shoulder-taps",
        ),
        ex(
            "knee-plank", "Knee plank", CORE, setOf(SHOULDERS), SECONDS, 1,
            listOf("Forearms on the floor, knees down.", "Walk your knees back so there's a straight line from knees to head.", "Brace and hold."),
            listOf("Bum in the air"),
            "Easy, steady breaths.",
            harder = "plank",
        ),
        ex(
            "side-plank", "Side plank", CORE, setOf(SHOULDERS, GLUTES), SECONDS, 2,
            listOf("Lie on your side with your elbow under your shoulder.", "Stack your feet (or bend the bottom knee to make it easier).", "Lift your hips so your body is straight.", "Hold, then switch sides."),
            listOf("Hips dropping", "Rolling forward"),
            "Breathe into your ribs.",
            easier = "knee-plank", perSide = true,
        ),
        ex(
            "dead-bug", "Dead bug", CORE, emptySet(), REPS, 1,
            listOf("Lie on your back, arms up to the ceiling, knees bent at 90° above your hips.", "Slowly lower one arm overhead and the opposite leg towards the floor.", "Return and switch sides."),
            listOf("Lower back arching off the floor", "Moving fast"),
            "Breathe out as you extend. Back stays glued down.",
            harder = "hollow-hold", perSide = true,
        ),
        ex(
            "hollow-hold", "Hollow hold", CORE, emptySet(), SECONDS, 2,
            listOf("Lie on your back, arms overhead.", "Press your lower back into the floor.", "Lift your shoulders and straight legs off the floor into a banana shape.", "Hold."),
            listOf("Lower back lifting", "Legs too low too soon"),
            "Bend your knees or bring your arms down to make it easier.",
            easier = "dead-bug",
        ),
        ex(
            "mountain-climber", "Mountain climbers", CARDIO, setOf(CORE, SHOULDERS), SECONDS, 1,
            listOf("Start in a high plank.", "Drive one knee towards your chest, then switch legs quickly.", "Keep your hips level with your shoulders."),
            listOf("Bum high in the air", "Hands creeping forward"),
            "Quick, rhythmic breathing.",
        ),
        ex(
            "russian-twist", "Russian twist", CORE, emptySet(), REPS, 2,
            listOf("Sit with knees bent, lean back to about 45° with a straight back.", "Lift your feet off the floor if you can.", "Rotate your chest to one side, then the other. Each side counts as one."),
            listOf("Rounding the back", "Just moving the arms, not the torso"),
            "Breathe out on each twist.",
            perSide = true,
        ),
        ex(
            "heel-taps", "Heel taps", CORE, emptySet(), REPS, 1,
            listOf("Lie on your back, knees bent, feet flat.", "Lift your shoulders slightly off the floor.", "Reach side to side to tap each heel."),
            listOf("Pulling the neck forward"),
            "Short breaths out on each tap.",
            perSide = true,
        ),
        ex(
            "v-up", "V-up", CORE, emptySet(), REPS, 3,
            listOf("Lie flat with arms overhead and legs straight.", "Lift your legs and upper body at the same time, reaching hands to feet.", "Lower slowly back down."),
            listOf("Crashing back to the floor", "Bending at the knees to cheat"),
            "Out as you fold, in as you lower.",
            easier = "leg-raise",
        ),
        ex(
            "toe-touch", "Toe-touch crunch", CORE, emptySet(), REPS, 2,
            listOf("Lie on your back with legs straight up to the ceiling.", "Reach your hands towards your toes, lifting your shoulder blades.", "Lower slowly."),
            listOf("Swinging the arms", "Letting the legs fall"),
            "Breathe out as you reach.",
            easier = "crunch",
        ),
        ex(
            "plank-jacks", "Plank jacks", CARDIO, setOf(CORE), SECONDS, 2,
            listOf("Start in a high plank, feet together.", "Jump your feet wide, then back together, like a jumping jack.", "Keep your hips level."),
            listOf("Hips bouncing up and down"),
            "Quick, steady breaths.",
            easier = "mountain-climber",
        ),

        // ---------------- Glutes ----------------
        ex(
            "glute-bridge", "Glute bridge", GLUTES, setOf(LEGS, CORE), REPS, 1,
            listOf("Lie on your back, knees bent, feet flat and hip-width apart.", "Push through your heels to lift your hips until there's a straight line from knees to shoulders.", "Squeeze your glutes hard at the top for a second.", "Lower slowly."),
            listOf("Arching the lower back at the top", "Pushing through the toes"),
            "Breathe out as you lift.",
            harder = "single-leg-bridge",
        ),
        ex(
            "single-leg-bridge", "Single-leg glute bridge", GLUTES, setOf(LEGS, CORE), REPS, 2,
            listOf("Set up like a glute bridge, then lift one foot off the floor.", "Push through the planted heel to raise your hips.", "Keep your hips level, lower slowly, then switch sides."),
            listOf("Hips tilting to one side"),
            "Out as you lift.",
            easier = "glute-bridge", perSide = true,
        ),
        ex(
            "bridge-hold", "Glute bridge hold", GLUTES, setOf(CORE), SECONDS, 1,
            listOf("Lift into a glute bridge.", "Hold at the top, squeezing your glutes the whole time."),
            listOf("Letting the hips sink"),
            "Breathe steadily.",
        ),
        ex(
            "donkey-kick", "Donkey kicks", GLUTES, emptySet(), REPS, 1,
            listOf("Start on hands and knees.", "Keeping the knee bent at 90°, push one foot up towards the ceiling.", "Lower without touching the floor and repeat, then switch."),
            listOf("Arching the lower back", "Swinging with momentum"),
            "Out as you kick up.",
            perSide = true,
        ),
        ex(
            "fire-hydrant", "Fire hydrants", GLUTES, emptySet(), REPS, 1,
            listOf("Start on hands and knees.", "Lift one bent knee out to the side as high as you can without tipping.", "Lower and repeat, then switch sides."),
            listOf("Leaning the whole body away"),
            "Slow and controlled.",
            perSide = true,
        ),
        ex(
            "clamshell", "Clamshells", GLUTES, emptySet(), REPS, 1,
            listOf("Lie on your side, knees bent, feet together.", "Keeping your feet touching, open your top knee like a clamshell.", "Lower slowly. Switch sides after the set."),
            listOf("Rolling the hips backwards"),
            "Breathe out as the knee opens.",
            perSide = true,
        ),

        // ---------------- Legs ----------------
        ex(
            "chair-squat", "Chair squat", LEGS, setOf(GLUTES), REPS, 1,
            listOf("Stand in front of a chair, feet shoulder-width.", "Sit back and down until you lightly touch the seat.", "Stand straight back up, pushing through your heels."),
            listOf("Flopping onto the chair", "Knees caving in"),
            "In on the way down, out as you stand.",
            harder = "squat",
        ),
        ex(
            "squat", "Bodyweight squat", LEGS, setOf(GLUTES, CORE), REPS, 1,
            listOf("Feet shoulder-width, toes slightly out.", "Push your hips back and bend your knees as if sitting on a low chair.", "Go as low as you can with your chest up and heels down.", "Drive back up to standing."),
            listOf("Heels lifting", "Knees caving inwards", "Rounding the back"),
            "In on the way down, out as you stand.",
            easier = "chair-squat", harder = "jump-squat",
        ),
        ex(
            "sumo-squat", "Sumo squat", LEGS, setOf(GLUTES), REPS, 1,
            listOf("Take a wide stance with toes pointing out.", "Sit straight down, pushing your knees out over your toes.", "Stand back up, squeezing your glutes."),
            listOf("Knees collapsing in", "Leaning forward"),
            "Out on the way up.",
            harder = "squat-pulse",
        ),
        ex(
            "jump-squat", "Jump squat", LEGS, setOf(GLUTES, CARDIO), REPS, 3,
            listOf("Squat down to about parallel.", "Explode up into a jump.", "Land softly on the balls of your feet and sink straight into the next squat."),
            listOf("Landing stiff-legged", "Knees caving on landing"),
            "Breathe out as you jump.",
            easier = "squat",
            safety = "Skip on bad knees — do bodyweight squats instead.",
        ),
        ex(
            "squat-pulse", "Squat pulses", LEGS, setOf(GLUTES), SECONDS, 2,
            listOf("Lower into a squat.", "Stay low and pulse up and down a few inches.", "Don't stand up until the time is over."),
            listOf("Standing too tall between pulses"),
            "Short breaths. It's meant to burn.",
            easier = "wall-sit",
        ),
        ex(
            "wall-sit", "Wall sit", LEGS, setOf(GLUTES), SECONDS, 1,
            listOf("Lean your back against a wall.", "Slide down until your knees are bent at about 90°.", "Hold, with your weight in your heels."),
            listOf("Knees in front of the toes", "Hands pushing on the thighs"),
            "Breathe steadily — this one is mental.",
        ),
        ex(
            "reverse-lunge", "Reverse lunge", LEGS, setOf(GLUTES), REPS, 1,
            listOf("Stand tall, feet hip-width.", "Step one foot back and lower until both knees are at about 90°.", "Push through the front heel to step back to standing.", "Alternate legs."),
            listOf("Front knee caving in", "Leaning far forward"),
            "In as you step back, out as you stand.",
            harder = "bulgarian-split-squat", perSide = true,
        ),
        ex(
            "forward-lunge", "Forward lunge", LEGS, setOf(GLUTES), REPS, 2,
            listOf("Step forward with one leg and lower until both knees are at 90°.", "Push back off the front foot to return.", "Alternate legs."),
            listOf("Short steps that push the knee over the toes", "Wobbling hips"),
            "Out as you push back.",
            easier = "reverse-lunge", perSide = true,
        ),
        ex(
            "side-lunge", "Side lunge", LEGS, setOf(GLUTES, MOBILITY), REPS, 1,
            listOf("Take a wide step to one side.", "Bend that knee and sit your hips back, keeping the other leg straight.", "Push back to the middle and switch sides."),
            listOf("Knee caving in", "Heel lifting"),
            "Out as you push back to centre.",
            perSide = true,
        ),
        ex(
            "bulgarian-split-squat", "Bulgarian split squat", LEGS, setOf(GLUTES), REPS, 3,
            listOf("Stand a big step in front of a chair, top of your back foot on the seat.", "Lower straight down until your front thigh is about parallel.", "Drive up through the front heel. Finish one side, then the other."),
            listOf("Front foot too close to the chair", "Leaning sideways"),
            "In on the way down, out as you rise.",
            easier = "reverse-lunge", perSide = true,
        ),
        ex(
            "step-up", "Step-up", LEGS, setOf(GLUTES), REPS, 1,
            listOf("Face a sturdy step or low chair.", "Step up with one foot and drive through that heel to stand on top.", "Step down slowly. Do all reps on one side, then switch."),
            listOf("Pushing off the back foot", "Using something wobbly"),
            "Out on the way up.",
            perSide = true,
        ),
        ex(
            "calf-raise", "Calf raise", LEGS, emptySet(), REPS, 1,
            listOf("Stand tall, holding a wall for balance.", "Rise onto the balls of your feet as high as you can.", "Pause, then lower slowly."),
            listOf("Bouncing", "Rolling onto the outer edge of the foot"),
            "Slow on the way down.",
            harder = "single-calf-raise",
        ),
        ex(
            "single-calf-raise", "Single-leg calf raise", LEGS, emptySet(), REPS, 2,
            listOf("Stand on one foot, holding a wall.", "Rise as high as you can, then lower slowly.", "Switch after the set."),
            listOf("Bending the knee to cheat"),
            "Pause for a second at the top.",
            easier = "calf-raise", perSide = true,
        ),

        // ---------------- Cardio ----------------
        ex(
            "march", "Marching in place", CARDIO, setOf(LEGS), SECONDS, 1,
            listOf("Stand tall.", "March, lifting your knees to hip height and swinging your arms."),
            listOf("Slouching"),
            "Easy, steady breathing. Quiet for flats and late nights.",
            harder = "high-knees",
        ),
        ex(
            "jumping-jacks", "Jumping jacks", CARDIO, setOf(LEGS, SHOULDERS), SECONDS, 1,
            listOf("Stand with feet together, arms by your sides.", "Jump your feet out while raising your arms overhead.", "Jump back to the start. Keep a steady rhythm."),
            listOf("Landing flat-footed"),
            "Rhythmic breathing.",
            easier = "march",
        ),
        ex(
            "high-knees", "High knees", CARDIO, setOf(LEGS, CORE), SECONDS, 2,
            listOf("Run on the spot, driving your knees up to hip height.", "Pump your arms and stay on the balls of your feet."),
            listOf("Leaning back"),
            "Quick, short breaths.",
            easier = "march",
        ),
        ex(
            "butt-kicks", "Butt kicks", CARDIO, setOf(LEGS), SECONDS, 1,
            listOf("Jog on the spot, kicking your heels up towards your glutes."),
            listOf("Leaning forward"),
            "Light and quick.",
        ),
        ex(
            "burpee", "Burpee", CARDIO, setOf(CHEST, LEGS, CORE), REPS, 3,
            listOf("From standing, squat and put your hands on the floor.", "Jump your feet back into a plank and do a push-up.", "Jump your feet back in.", "Jump up with your arms overhead."),
            listOf("Sagging hips in the plank", "Landing heavily"),
            "Find a rhythm — breathe out on the jump.",
            easier = "step-back-burpee",
        ),
        ex(
            "step-back-burpee", "Step-back burpee", CARDIO, setOf(LEGS, CORE), REPS, 2,
            listOf("Squat and put your hands on the floor.", "Step one foot back, then the other, into a plank.", "Step them back in one at a time.", "Stand up and reach overhead."),
            listOf("Rushing so the plank sags"),
            "Steady breathing. No jumping needed.",
            harder = "burpee",
        ),
        ex(
            "skater-hops", "Skater hops", CARDIO, setOf(LEGS, GLUTES), SECONDS, 2,
            listOf("Leap sideways onto one foot, sweeping the other leg behind you.", "Land softly and leap back to the other side."),
            listOf("Landing on a straight knee"),
            "Breathe out on each landing.",
            easier = "side-lunge",
        ),
        ex(
            "shadow-boxing", "Shadow boxing", CARDIO, setOf(SHOULDERS, CORE), SECONDS, 1,
            listOf("Stand in a boxing stance, hands up by your chin.", "Throw quick jabs and crosses, rotating through your hips.", "Stay light on your feet."),
            listOf("Locking the elbows on punches"),
            "Breathe out sharply on each punch.",
        ),
        ex(
            "inchworm", "Inchworm", MOBILITY, setOf(CORE, SHOULDERS), REPS, 1,
            listOf("Stand tall, fold forward and put your hands on the floor.", "Walk your hands out into a plank.", "Walk them back to your feet and stand up."),
            listOf("Letting the hips sag in the plank"),
            "Bend your knees as much as you need.",
        ),

        // ---------------- Mobility & posture ----------------
        ex(
            "chin-tuck", "Chin tucks", MOBILITY, setOf(BACK), REPS, 1,
            listOf("Sit or stand tall, looking straight ahead.", "Glide your head straight back, making a double chin.", "Hold for 3 seconds, then relax."),
            listOf("Tilting the head down instead of gliding back"),
            "Breathe normally. The fix for forward head posture.",
        ),
        ex(
            "wall-angel", "Wall angels", MOBILITY, setOf(BACK, SHOULDERS), REPS, 1,
            listOf("Stand with your back, head and glutes against a wall.", "Raise your arms into a goalpost shape against the wall.", "Slide them up overhead and back down, keeping contact."),
            listOf("Lower back arching off the wall", "Wrists leaving the wall"),
            "Slow, controlled breathing.",
        ),
        ex(
            "cat-cow", "Cat-cow", MOBILITY, setOf(BACK, CORE), REPS, 1,
            listOf("Start on hands and knees.", "Breathe in as you drop your belly and lift your chest (cow).", "Breathe out as you round your back up to the ceiling (cat)."),
            listOf("Rushing"),
            "Move with your breath.",
        ),
        ex(
            "open-book", "Open book rotation", MOBILITY, setOf(BACK), REPS, 1,
            listOf("Lie on your side, knees bent at 90°, arms straight in front.", "Open your top arm across your body to the other side, following it with your eyes.", "Return slowly. Switch sides after the set."),
            listOf("Letting the knees come apart"),
            "Breathe out as you open.",
            perSide = true,
        ),
        ex(
            "hip-flexor-stretch", "Kneeling hip flexor stretch", MOBILITY, setOf(GLUTES), SECONDS, 1,
            listOf("Kneel on one knee, other foot flat in front.", "Squeeze the glute of the kneeling leg and gently shift your hips forward.", "Hold, then switch sides."),
            listOf("Arching the lower back"),
            "Long, slow breaths. Undoes hours of sitting.",
            perSide = true,
        ),
        ex(
            "childs-pose", "Child's pose", MOBILITY, setOf(BACK), SECONDS, 1,
            listOf("Kneel and sit back on your heels.", "Fold forward, reaching your arms out in front.", "Rest your forehead on the floor and relax."),
            listOf("Holding tension in the shoulders"),
            "Breathe deeply into your back.",
        ),
        ex(
            "hamstring-stretch", "Standing hamstring stretch", MOBILITY, setOf(LEGS), SECONDS, 1,
            listOf("Put one heel on a low step, leg straight.", "Hinge forward from the hips with a flat back until you feel a stretch.", "Hold, then switch."),
            listOf("Rounding the back to reach further"),
            "Relax into it with each breath out.",
            perSide = true,
        ),
        ex(
            "doorway-stretch", "Doorway chest stretch", MOBILITY, setOf(CHEST, SHOULDERS), SECONDS, 1,
            listOf("Place your forearms on either side of a doorway, elbows at shoulder height.", "Step one foot through and lean gently forward until you feel the stretch in your chest."),
            listOf("Shrugging", "Pushing into pain"),
            "Slow breaths. Opens up rounded shoulders.",
        ),
        ex(
            "worlds-greatest-stretch", "World's greatest stretch", MOBILITY, setOf(LEGS, BACK), REPS, 2,
            listOf("Step into a deep lunge, hands on the floor inside your front foot.", "Drop your inside elbow towards the floor.", "Rotate and reach that arm up to the ceiling.", "Return and switch sides."),
            listOf("Back knee sagging to the floor"),
            "Breathe out as you rotate.",
            perSide = true,
        ),
        ex(
            "trap-stretch", "Upper trap stretch", MOBILITY, setOf(SHOULDERS), SECONDS, 1,
            listOf("Sit tall and hold the side of the chair with one hand.", "Tilt your head away, ear towards the shoulder.", "Hold, then switch."),
            listOf("Pulling hard on the head"),
            "Gentle — let gravity do the work.",
            perSide = true,
        ),
        ex(
            "downward-dog", "Downward dog", MOBILITY, setOf(SHOULDERS, LEGS), SECONDS, 1,
            listOf("From hands and knees, lift your hips up and back into an upside-down V.", "Press your chest towards your thighs, heels towards the floor.", "Pedal your feet gently."),
            listOf("Rounding the back to get heels down"),
            "Long breaths in and out through the nose.",
        ),
        ex(
            "cobra", "Cobra stretch", MOBILITY, setOf(CORE, BACK), SECONDS, 1,
            listOf("Lie face down, hands under your shoulders.", "Gently press your chest up, keeping hips on the floor.", "Relax your shoulders down away from your ears."),
            listOf("Pushing up so far the lower back pinches"),
            "Breathe into your belly.",
        ),
        ex(
            "figure-4", "Figure-4 glute stretch", MOBILITY, setOf(GLUTES), SECONDS, 1,
            listOf("Lie on your back and cross one ankle over the opposite knee.", "Pull the bottom leg towards your chest.", "Hold, then switch."),
            listOf("Lifting your head off the floor"),
            "Relax and breathe slowly.",
            perSide = true,
        ),
    )

    private val byId = all.associateBy { it.id }

    fun get(id: String): Exercise = byId[id] ?: error("Unknown exercise $id")

    fun find(id: String): Exercise? = byId[id]

    fun forMuscle(muscle: Muscle) = all.filter { muscle in it.muscles }.sortedBy { if (it.primary == muscle) 0 else 1 }
}
