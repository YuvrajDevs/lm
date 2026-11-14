package com.glowup.app.ui.data.program

internal val groomingTrack = listOf(
    lesson(
        "Know your skin type",
        5,
        hook = "Most skincare mistakes come from using products made for a different skin type.",
        ideas = listOf(
            "Do the blot test" to
                "Wash your face, wait an hour with nothing on it, then press a tissue to your forehead, nose and cheeks. Oil everywhere = oily; only the T-zone = combination; tight and flaky = dry.",
            "Sensitive is a trait, not a type" to
                "If products often sting, redden or itch, treat your skin as sensitive on top of its type: fragrance-free, fewer products, patch-test anything new.",
            "Three steps is enough" to
                "Cleanser, moisturiser, sunscreen. Pick each for your type — gel textures for oily, creams for dry. Adding ten products before nailing three usually backfires."
        ),
        drill = "Do the blot test today and write down your skin type. Check whether your current cleanser and moisturiser actually suit it.",
        quiz = quiz(
            "After the blot test, oil shows only on your forehead and nose. Your type is most likely…",
            "Oily",
            "Combination",
            "Dry",
            answer = 1,
            why = "Oil limited to the T-zone with normal or dry cheeks is the classic sign of combination skin."
        )
    ),
    lesson(
        "Sunscreen, done properly",
        5,
        hook = "Sunscreen is the single most effective anti-ageing and even-skin-tone product there is — if you use enough.",
        ideas = listOf(
            "Use two finger-lengths" to
                "Squeeze sunscreen along your index and middle finger — that's about the right amount for face and neck. Most people use a third of that and get a fraction of the protection.",
            "Every day, every weather" to
                "UVA rays that cause ageing and dark spots pass through clouds and windows. Make SPF 30+ the last step of your morning routine, not a beach-only product.",
            "Reapply when you're outside" to
                "If you're outdoors for a long time, reapply every two hours. For office days, morning application is usually enough."
        ),
        drill = "Tomorrow morning, measure out two finger-lengths of sunscreen and apply it to face, neck and ears. Notice how much more that is than usual.",
        quiz = quiz(
            "Why wear sunscreen on a cloudy day indoors near a window?",
            "UVB burns through glass",
            "UVA, which causes ageing and spots, passes through clouds and glass",
            "It isn't needed indoors",
            answer = 1,
            why = "UVA penetrates clouds and window glass and drives ageing and pigmentation, even without a visible burn."
        )
    ),
    lesson(
        "Hair and scalp basics",
        5,
        hook = "Good hair starts with a healthy scalp, not with styling products.",
        ideas = listOf(
            "Wash to your scalp's needs" to
                "Oily scalps may need washing daily or every other day; dry or curly hair often does better 2–3 times a week. Shampoo the scalp, not the lengths.",
            "Condition the ends" to
                "Conditioner goes on the mid-lengths and ends, where hair is oldest and driest. Keeping it off the scalp avoids flat, greasy roots.",
            "Go easy on heat" to
                "Towel-dry gently by squeezing, not rubbing. If you blow-dry, use medium heat and keep the dryer moving — high heat is the fastest way to frizz and breakage."
        ),
        drill = "On your next wash, shampoo only the scalp and condition only the ends. Pat dry instead of rubbing.",
        quiz = quiz(
            "Where should conditioner mainly go?",
            "The scalp",
            "Mid-lengths and ends",
            "Evenly from root to tip",
            answer = 1,
            why = "The ends are the oldest and driest part of the hair. Conditioning the scalp tends to weigh roots down and make them greasy."
        )
    ),
    lesson(
        "The details people notice",
        5,
        hook = "People rarely notice great skin — but they always notice dirty nails, cracked lips and stray hairs.",
        ideas = listOf(
            "Nails: short, clean, even" to
                "Trim weekly, file the edges smooth and push back cuticles gently after a shower. Clean nails matter more than any manicure.",
            "Hands and lips" to
                "A hand cream after washing and a lip balm in your pocket prevent cracked, dry skin — especially in cold or air-conditioned environments.",
            "The two-minute stray hair check" to "Once a week, check ears, nose and between the eyebrows in good light. A small trimmer handles it in minutes."
        ),
        drill = "Do a full details check today: nails, cuticles, lips, ears, nose and brow line. Fix anything that needs it.",
        quiz = quiz(
            "How often should nails usually be trimmed to stay neat?",
            "Monthly",
            "About weekly",
            "Only when they break",
            answer = 1,
            why = "Nails grow about 3 mm a month; a weekly trim keeps them consistently short and clean-looking."
        )
    ),
    lesson(
        "Get the right haircut",
        6,
        hook = "The best haircut isn't the trendiest one — it's the one that suits your face shape and your hair.",
        ideas = listOf(
            "Balance your face shape" to
                "Round faces suit height on top and shorter sides; long faces suit fuller sides and less height; square faces suit almost anything. The goal is to balance, not exaggerate.",
            "Work with your hair type" to
                "Fine hair looks fuller shorter and textured; thick hair needs thinning to avoid bulk; curly hair should be cut by someone who cuts curls dry.",
            "Bring photos, speak in specifics" to
                "Show 2–3 reference photos and say what you like about each. Use guard numbers ('a 2 on the sides') rather than 'short' — everyone's 'short' is different."
        ),
        drill = "Save three reference photos of haircuts on people with a similar face shape and hair type to yours. Note what you like in each.",
        quiz = quiz(
            "Someone with a round face wants their face to look longer. Which cut helps most?",
            "Fuller sides, flat top",
            "Short sides with height on top",
            "A heavy straight fringe",
            answer = 1,
            why = "Height on top with tighter sides adds vertical length and contrast, balancing a rounder face."
        )
    ),
    lesson(
        "Shape your brows and facial hair",
        6,
        hook = "Brows and facial hair frame your whole face — small tidy-ups make a big difference.",
        ideas = listOf(
            "Tidy, don't reshape" to
                "For brows, remove only the obvious strays between and under them. Brush them up and trim any long hairs. Over-plucking takes months to grow back.",
            "Pick a beard length you can maintain" to
                "Stubble, short beard or full — each looks good when the edges are clean. A messy neckline is what makes facial hair look unkempt.",
            "The neckline rule" to "Set the beard line about two fingers above your Adam's apple, curving up towards the ears. Shave everything below it."
        ),
        drill = "Tidy your brows (strays only) and, if you have facial hair, clean up the neckline and cheek line today.",
        quiz = quiz(
            "Where should a beard neckline usually sit?",
            "Along the jawline",
            "About two fingers above the Adam's apple",
            "At the collar",
            answer = 1,
            why = "Two fingers above the Adam's apple keeps the beard looking full from the front while removing neck fuzz."
        )
    ),
    lesson(
        "Fresh breath and a better smile",
        5,
        hook = "A confident smile and fresh breath affect first impressions more than almost anything you wear.",
        ideas = listOf(
            "Two minutes, twice a day" to
                "Most people brush for about 45 seconds. Use a timer or an electric brush, angle the bristles at the gumline and go gently — hard brushing damages gums.",
            "Clean between teeth and your tongue" to
                "Floss or use interdental brushes once a day, and brush or scrape your tongue — it's where most bad-breath bacteria live.",
            "Water beats mints" to
                "A dry mouth causes bad breath. Drinking water through the day does more than sugary mints, which feed the bacteria you're trying to beat."
        ),
        drill = "Tonight, brush for a full two minutes with a timer, floss, and clean your tongue.",
        quiz = quiz(
            "Where do most bad-breath bacteria live?",
            "On the front teeth",
            "On the tongue",
            "In the throat",
            answer = 1,
            why = "The rough surface of the tongue traps bacteria and debris — cleaning it is one of the fastest fixes for bad breath."
        )
    ),
    lesson(
        "Wear fragrance well",
        6,
        hook = "The right fragrance, worn lightly, is remembered. Too much is remembered for the wrong reasons.",
        ideas = listOf(
            "Two to three sprays" to
                "Apply to pulse points — neck and wrists — or spray once onto your chest under your shirt. If people can smell you from across the room, it's too much.",
            "Match it to the setting" to
                "Fresh, citrus or light woody scents for daytime and work; warmer, spicier or sweeter scents for evenings and colder weather.",
            "Don't rub your wrists" to
                "Rubbing crushes the top notes and makes the scent fade faster. Spray, let it dry, done. Moisturised skin holds fragrance longer."
        ),
        drill = "Check how you're applying fragrance: no more than three sprays, pulse points, no rubbing. If you don't own one, sample two at a store on your next visit.",
        quiz = quiz(
            "Why shouldn't you rub your wrists together after spraying?",
            "It stains clothes",
            "It breaks down the top notes and shortens the scent",
            "It makes it too strong",
            answer = 1,
            why = "Friction warms and breaks the lighter top notes, so the fragrance develops less fully and fades sooner."
        )
    ),
    lesson(
        "Introduce active ingredients safely",
        7,
        hook = "Actives like retinoids and acids can transform skin — or wreck your barrier if you rush them.",
        ideas = listOf(
            "Know what each does" to
                "Retinoids: texture, fine lines, breakouts. Niacinamide: oil, redness, uneven tone. AHA/BHA acids: dullness and clogged pores (BHA for oily, AHA for dry).",
            "One new active at a time" to
                "Introduce one product, 2–3 nights a week, for at least 4 weeks before adding another. Otherwise you can't tell what's helping or what's irritating.",
            "Protect the barrier" to
                "Stinging, peeling or redness means pull back. Actives go at night, moisturiser after, sunscreen every morning — retinoids and acids increase sun sensitivity."
        ),
        drill = "If you want to try an active, pick just one based on your main concern and plan a 4-week schedule: 2–3 nights a week, moisturiser after, SPF every morning.",
        quiz = quiz(
            "You want to start a retinoid and an exfoliating acid. What's the safest plan?",
            "Start both nightly",
            "Start one, 2–3 nights a week, add the other weeks later",
            "Alternate them every morning",
            answer = 1,
            why = "One active at a time, slowly, lets your skin adjust and lets you see what each one does. Both at once is the classic path to irritation."
        )
    ),
    lesson(
        "Handle breakouts and dark spots",
        7,
        hook = "Clear skin comes from patience and consistency far more than from any miracle product.",
        ideas = listOf(
            "Hands off" to
                "Picking and squeezing pushes inflammation deeper and turns a 5-day spot into a 2-month dark mark. A pimple patch protects it while it heals.",
            "Give a routine 8–12 weeks" to
                "Skin cells take about a month to turn over, and treatments take two to three cycles to show results. Switching every two weeks means nothing ever gets a chance to work.",
            "Know when to see a professional" to
                "Painful, deep or scarring acne, or anything not improving after three months of a consistent routine, is a reason to see a dermatologist — prescription treatments work far better."
        ),
        drill = "Take a clear, well-lit photo of your skin today. Commit to your current routine unchanged for the next 8 weeks and set a reminder to compare.",
        quiz = quiz(
            "How long should you usually give a new skincare routine before judging it?",
            "One week",
            "8–12 weeks",
            "As long as it feels nice",
            answer = 1,
            why = "Visible change needs two to three skin cell cycles. Judging after a week or two leads to constant product-switching."
        )
    ),
    lesson(
        "Body care that people notice",
        6,
        hook = "Grooming doesn't stop at the neck — body odour, rough skin and razor bumps undo a great outfit.",
        ideas = listOf(
            "Antiperspirant at night" to
                "Antiperspirant works best on dry skin before bed, so it can plug sweat glands overnight. Deodorant (scent only) can go on in the morning.",
            "Shave with the grain" to
                "Shave after a warm shower, use a sharp blade and shaving gel, and go in the direction hair grows. Against the grain is closer but causes most ingrown hairs.",
            "Exfoliate and moisturise the body" to
                "A gentle body scrub or exfoliating wash twice a week and lotion after showering clears rough patches and body breakouts on the back and chest."
        ),
        drill = "Tonight, apply antiperspirant before bed instead of in the morning. On your next shave, go with the grain only.",
        quiz = quiz(
            "When does antiperspirant work best?",
            "Right after a morning shower",
            "On dry skin before bed",
            "Just before exercise",
            answer = 1,
            why = "At night, sweat glands are less active, so the active ingredient can block them overnight and keep working the next day."
        )
    ),
    lesson(
        "Your grooming system",
        7,
        hook = "The people who always look put together don't try harder — they've turned grooming into a schedule.",
        ideas = listOf(
            "Daily, weekly, monthly" to
                "Daily: skin AM/PM, teeth, deodorant. Weekly: nails, brows, exfoliation, beard edges. Monthly: haircut check, replace razor blades, clean brushes.",
            "Stack it onto existing habits" to
                "Attach weekly tasks to fixed moments — nails every Sunday shower, brows every Sunday evening — so they happen without remembering.",
            "Audit every quarter" to
                "Every three months, take a photo in the same light, check what's improved and drop products you don't use. Simple routines you actually follow beat perfect ones you don't."
        ),
        drill = "Write your grooming calendar: daily, weekly and monthly tasks, each attached to a specific day or habit. Add the weekly ones to your routines.",
        quiz = quiz(
            "What's the main reason to attach grooming tasks to fixed moments?",
            "It saves money",
            "It removes the need to remember them",
            "It makes products last longer",
            answer = 1,
            why = "Habits attached to existing routines run on autopilot — that's what makes people look consistently put together."
        )
    )
)
