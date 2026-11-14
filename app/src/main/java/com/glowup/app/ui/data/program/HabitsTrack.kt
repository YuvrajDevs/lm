package com.glowup.app.ui.data.program

internal val habitsTrack = listOf(
    lesson(
        "How habits actually work",
        6,
        hook = "You don't rise to the level of your goals — you fall to the level of your habits.",
        ideas = listOf(
            "Cue, routine, reward" to
                "Every habit has a trigger, an action and a payoff. To build one, make the cue obvious and the reward immediate; to break one, remove the cue.",
            "Start ridiculously small" to
                "'Read one page' or 'one push-up' sounds pointless, but it builds the identity of someone who shows up. You can always do more once started.",
            "Stack it" to "Attach a new habit to one you already do: 'After I pour my morning coffee, I'll write one line in my journal.'"
        ),
        drill = "Choose one tiny habit and write it as a stack: 'After I [existing habit], I will [tiny new habit].' Add it to your routines.",
        quiz = quiz(
            "Why start a new habit with a tiny version?",
            "Small habits are all you need",
            "It builds consistency and identity before intensity",
            "It's less tiring",
            answer = 1,
            why = "Showing up every day matters more early on than how much you do — tiny habits make showing up easy."
        )
    ),
    lesson(
        "Sleep that works",
        6,
        hook = "Sleep is the cheapest skin treatment, mood booster and focus upgrade there is.",
        ideas = listOf(
            "Fix your wake time" to
                "A consistent wake-up time — even on weekends — anchors your body clock more than a fixed bedtime. Sleepiness at night follows naturally.",
            "Morning light, dim evenings" to
                "10 minutes of daylight soon after waking sets your rhythm. In the evening, dim lights and screens an hour before bed.",
            "Cool, dark, quiet" to "A slightly cool room, blackout curtains or an eye mask, and no phone in bed. Your brain learns that the bed is for sleep."
        ),
        drill = "Set a fixed wake-up time for the next 7 days, weekends included, and get outside within 30 minutes of waking.",
        quiz = quiz(
            "What anchors your body clock most effectively?",
            "A strict bedtime",
            "A consistent wake-up time",
            "Sleeping in at weekends",
            answer = 1,
            why = "A fixed wake time and morning light set your circadian rhythm, making it easier to fall asleep at a regular time."
        )
    ),
    lesson(
        "Energy: water and food timing",
        5,
        hook = "A lot of 'tiredness' is really mild dehydration and blood-sugar crashes.",
        ideas = listOf(
            "Drink before you're thirsty" to
                "Start the day with a glass of water and keep a bottle visible. Thirst often shows up as fatigue, headaches or hunger.",
            "Protein at breakfast" to "Eggs, yoghurt, paneer or dal in the morning keep energy steadier than sugary cereals or pastries, which spike and crash.",
            "Watch the afternoon slump" to
                "Heavy, refined-carb lunches cause the 3 PM crash. A lighter lunch with protein and vegetables, plus a short walk, keeps you sharper."
        ),
        drill = "Today, drink a glass of water on waking and include a protein source at breakfast. Notice your energy at 3 PM.",
        quiz = quiz(
            "Which breakfast keeps energy most stable?",
            "Sweet cereal and juice",
            "Eggs or yoghurt with fruit",
            "Just coffee",
            answer = 1,
            why = "Protein and fibre digest slowly, avoiding the blood-sugar spike and crash of sugary breakfasts."
        )
    ),
    lesson(
        "Move more without a gym",
        5,
        hook = "The movement you do outside workouts often matters more than the workout itself.",
        ideas = listOf(
            "Walk daily" to
                "7,000–10,000 steps a day improves mood, energy, sleep and heart health. A 20-minute walk after meals also helps digestion and blood sugar.",
            "Break up sitting" to "Stand or walk for 2–3 minutes every hour. Long unbroken sitting hurts posture, energy and focus.",
            "Make it the default" to "Take stairs, walk to nearby places, take calls on foot. Small choices add thousands of steps without extra time."
        ),
        drill = "Take a 15–20 minute walk today, ideally after a meal, and take every call standing or walking.",
        quiz = quiz(
            "When is a short walk especially useful for energy and blood sugar?",
            "Right before bed",
            "After a meal",
            "Before breakfast only",
            answer = 1,
            why = "Walking after eating helps muscles use glucose, reducing the energy crash that follows meals."
        )
    ),
    lesson(
        "Design your environment",
        6,
        hook = "Willpower is unreliable. Your surroundings decide most of your behaviour for you.",
        ideas = listOf(
            "Make good habits visible" to
                "Book on the pillow, water bottle on the desk, running shoes by the door. Visible cues trigger action without needing to remember.",
            "Make bad habits harder" to
                "Phone charging outside the bedroom, snacks in a high cupboard, apps logged out. Adding 20 seconds of friction stops most automatic behaviour.",
            "One space, one purpose" to "If you can, keep your bed for sleep and a specific spot for focused work. Your brain links places with behaviours."
        ),
        drill = "Make one good habit more visible and one bad habit harder to do before you go to bed tonight.",
        quiz = quiz(
            "You want to stop scrolling in bed. What's the most effective change?",
            "Promise yourself to stop",
            "Charge your phone outside the bedroom",
            "Turn the brightness down",
            answer = 1,
            why = "Removing the cue and adding friction works far better than relying on willpower at the moment of temptation."
        )
    ),
    lesson(
        "Strength basics at home",
        7,
        hook = "Strength training improves posture, confidence, sleep and how clothes fit — and you can start with no equipment.",
        ideas = listOf(
            "Three movements" to
                "Push (push-ups or incline push-ups), squat (bodyweight squats) and hinge (glute bridges). These cover most of your major muscles.",
            "Form before reps" to "Slow and controlled: three seconds down, one second up. Ten good reps beat thirty rushed ones and prevent injury.",
            "Twice or three times a week" to
                "Three sets of each movement, 2–3 times a week, is enough to see real change within two months. Add reps or difficulty when it gets easy."
        ),
        drill = "Do 3 sets of 8–12 slow push-ups (incline if needed), squats and glute bridges today. Note your reps so you can beat them next time.",
        quiz = quiz(
            "Which three movements form a simple full-body home routine?",
            "Curls, crunches, calf raises",
            "Push-ups, squats, glute bridges",
            "Running, jumping, stretching",
            answer = 1,
            why = "Push, squat and hinge patterns train the chest, arms, legs, glutes and back together."
        )
    ),
    lesson(
        "Eat well without dieting",
        7,
        hook = "You don't need a diet — you need a plate that works most of the time.",
        ideas = listOf(
            "The plate method" to
                "Half the plate vegetables, a quarter protein, a quarter carbs. It's a simple guide that works with any cuisine, including home-cooked Indian meals.",
            "Protein at every meal" to "Dal, eggs, paneer, curd, chicken, fish, tofu. Protein keeps you full, supports muscle and helps skin and hair.",
            "80/20, not perfect" to "Eat well 80% of the time and enjoy the rest without guilt. Rigid rules usually end in a binge."
        ),
        drill = "Build your lunch or dinner today using the plate method: half vegetables, a quarter protein, a quarter carbs.",
        quiz = quiz(
            "In the plate method, what fills half the plate?",
            "Rice or roti",
            "Vegetables",
            "Protein",
            answer = 1,
            why = "Vegetables fill half the plate, adding fibre and nutrients with fewer calories, while protein and carbs share the other half."
        )
    ),
    lesson(
        "Take back control from your phone",
        7,
        hook = "The average person checks their phone over 100 times a day. Each check costs focus and calm.",
        ideas = listOf(
            "Notification diet" to
                "Turn off every notification that isn't from a real person or truly urgent. Most apps don't deserve the right to interrupt you.",
            "Grayscale and home screen" to "Moving social apps off the home screen — and trying grayscale mode — makes the phone far less compulsive.",
            "Phone-free anchors" to
                "No phone for the first 30 minutes after waking and the last 30 before sleep. These two windows change how your whole day feels."
        ),
        drill = "Turn off notifications for every app that isn't messages or calls, and move social apps off your home screen.",
        quiz = quiz(
            "What's the most effective first step to reduce phone use?",
            "Deleting the phone",
            "Cutting non-essential notifications",
            "Buying a new phone",
            answer = 1,
            why = "Notifications are the main trigger for checking. Removing most of them cuts compulsive checking dramatically."
        )
    ),
    lesson(
        "The weekly review",
        7,
        hook = "One honest hour a week keeps your goals from drifting into 'someday'.",
        ideas = listOf(
            "Look back" to "What went well? What didn't? What did I learn? Be honest, not harsh — the goal is adjustment, not judgement.",
            "Look ahead" to "Check your calendar and pick your top three priorities for the coming week. Everything else is secondary.",
            "Schedule the important" to "Put those three priorities into your calendar as blocks of time. If it isn't scheduled, it usually doesn't happen."
        ),
        drill = "Do a 20-minute weekly review today: three things that went well, one thing to change, and three priorities for next week, scheduled.",
        quiz = quiz(
            "What should a weekly review end with?",
            "A list of everything that went wrong",
            "Your top priorities scheduled into the week",
            "A new long-term goal",
            answer = 1,
            why = "Scheduling priorities turns reflection into action, which is the point of the review."
        )
    ),
    lesson(
        "Money basics for a better life",
        8,
        hook = "Money stress steals focus and confidence. A simple system gives both back.",
        ideas = listOf(
            "Know where it goes" to "Track every expense for one month — just notes on your phone. Most people are surprised where their money actually goes.",
            "50/30/20" to "Roughly 50% on needs, 30% on wants, 20% on savings and debt. Adjust to your situation, but always save something first.",
            "Automate saving" to "Set an automatic transfer to savings on payday. What you never see in your spending account, you don't miss."
        ),
        drill = "Start tracking every expense today, and set up (or plan) an automatic savings transfer for your next payday.",
        quiz = quiz(
            "Why automate savings on payday?",
            "To earn more interest",
            "So saving happens before spending, without willpower",
            "Banks require it",
            answer = 1,
            why = "Paying yourself first, automatically, removes the decision — and the temptation — entirely."
        )
    ),
    lesson(
        "Bounce back from slips",
        7,
        hook = "Everyone misses days. What separates people who change is what they do the day after.",
        ideas = listOf(
            "Never miss twice" to
                "Missing once is an accident; missing twice is the start of a new habit. Make the day after a slip non-negotiable, even if it's a tiny version.",
            "Lower the bar on hard days" to
                "On bad days, do the two-minute version — one page, one set, one minute of meditation. Keeping the chain alive matters more than the size.",
            "Drop the guilt" to "Guilt makes you avoid the thing you feel bad about. Treat a slip as data: what got in the way, and how can you plan around it?"
        ),
        drill = "Write your 'minimum version' for each of your habits — the two-minute version you'll do on bad days.",
        quiz = quiz(
            "You missed your workout yesterday. What's the best move today?",
            "Wait until Monday to restart",
            "Do at least a small version today",
            "Do double to make up for it",
            answer = 1,
            why = "Never missing twice keeps the habit alive. Doubling up often leads to burnout; waiting turns a slip into a stop."
        )
    ),
    lesson(
        "Become the person you're building",
        8,
        hook = "The deepest change happens when your habits become who you are, not what you're trying to do.",
        ideas = listOf(
            "Identity over outcomes" to "'I'm trying to run' becomes 'I'm a runner'. Every habit you complete is a vote for the person you're becoming.",
            "Review your 12 weeks" to
                "Look at how you speak, look, dress and think now compared with day one. Notice the evidence of change — it's easy to forget how far you've come.",
            "Choose your next chapter" to "Pick the two areas that changed you most and the one that needs more work. Build your next 12 weeks around them."
        ),
        drill = "Write three sentences starting with 'I am someone who…' based on habits you've actually built. Then choose your focus for the next 12 weeks.",
        quiz = quiz(
            "Why do identity-based habits last longer?",
            "They're easier",
            "Each action reinforces who you believe you are",
            "They need less effort",
            answer = 1,
            why = "When behaviour matches identity, keeping the habit feels natural — breaking it would feel out of character."
        )
    )
)
