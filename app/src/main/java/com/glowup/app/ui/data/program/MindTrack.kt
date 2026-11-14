package com.glowup.app.ui.data.program

internal val mindTrack = listOf(
    lesson(
        "Train your focus",
        6,
        hook = "Attention is the raw material of every skill you'll ever learn — and most of us leak it all day.",
        ideas = listOf(
            "Switching has a cost" to
                "Every time you jump between tasks, part of your attention stays stuck on the last one. That 'attention residue' makes both tasks slower and sloppier.",
            "Work in focused blocks" to
                "Pick one task, set a timer for 25–45 minutes, and do only that. A short break after each block keeps your focus fresh for the next one.",
            "Remove the temptation" to "Put your phone in another room or out of sight. Willpower loses to a buzzing phone; distance wins."
        ),
        drill = "Do one 25-minute focus session today with the Focus timer on a single task, phone out of reach.",
        quiz = quiz(
            "Why does constant task-switching slow you down?",
            "It wastes battery",
            "Attention stays partly stuck on the previous task",
            "It's only a problem for complex work",
            answer = 1,
            why = "Attention residue means your mind keeps processing the last task, so each switch reduces the quality of the next."
        )
    ),
    lesson(
        "Learn by testing yourself",
        6,
        hook = "Re-reading feels productive but barely works. Testing yourself is one of the most proven ways to remember.",
        ideas = listOf(
            "Retrieval beats review" to
                "Close the book and try to recall the key points. The effort of pulling information out of memory is what strengthens it.",
            "Use the blank page" to
                "After learning something, write everything you remember on a blank page, then check what you missed. The gaps show exactly what to study.",
            "Struggle is the signal" to "If recalling feels hard, that's good — difficult retrieval creates stronger memories than easy re-reading."
        ),
        drill = "After today's session, close the app and write the three key ideas from memory. Then check what you missed.",
        quiz = quiz(
            "Which study method creates the strongest memories?",
            "Re-reading notes",
            "Recalling information from memory before checking",
            "Highlighting key sentences",
            answer = 1,
            why = "Retrieval practice strengthens memory through effortful recall; re-reading and highlighting create a false sense of familiarity."
        )
    ),
    lesson(
        "Space it out",
        6,
        hook = "You forget most of what you learn within days — unless you review at the right moments.",
        ideas = listOf(
            "The forgetting curve" to "Memory drops fast after learning. Reviewing just before you'd forget resets the curve and makes each memory last longer.",
            "Review at growing gaps" to "Review after 1 day, 3 days, a week, then a month. Four short reviews beat one long cram session.",
            "Notes as questions" to "Write notes as questions with answers ('What's attention residue? → …'). They're ready-made for testing yourself later."
        ),
        drill = "Take three things you learned this week and write each as a question. Set reminders to answer them in 3 days and in a week.",
        quiz = quiz(
            "Which revision plan leads to better long-term memory?",
            "One 4-hour cram the night before",
            "Four short reviews spread across a month",
            "Re-reading every day for a week, then never again",
            answer = 1,
            why = "Spaced reviews interrupt forgetting at increasing intervals, which builds durable memory."
        )
    ),
    lesson(
        "Read actively",
        7,
        hook = "Reading 20 pages a day is about 30 books a year — but only if you remember what you read.",
        ideas = listOf(
            "Preview before reading" to
                "Skim the contents, headings and conclusion first. Knowing the shape of an argument makes the details easier to follow and remember.",
            "One line per chapter" to
                "After each chapter, write one sentence summarising it in your own words. It forces you to actually understand, not just read.",
            "Make reading the default" to "Keep a book or e-reader where you'd normally scroll — bed, commute, coffee. Ten pages a day adds up fast."
        ),
        drill = "Read 10 pages of any non-fiction book today and write a one-sentence summary afterwards.",
        quiz = quiz(
            "Why preview headings and the conclusion before reading?",
            "To skip the boring parts",
            "To understand the structure so details are easier to place",
            "To read faster",
            answer = 1,
            why = "A mental map of the argument gives new details somewhere to attach, improving comprehension and memory."
        )
    ),
    lesson(
        "Think from first principles",
        7,
        hook = "Most people reason by copying what others do. First-principles thinkers rebuild problems from the basics.",
        ideas = listOf(
            "Break it to the facts" to
                "Ask 'What do I know is definitely true here?' and strip away assumptions. 'Cars are expensive' becomes 'What do the materials and parts actually cost?'",
            "Question the 'always'" to "Whenever you hear 'that's just how it's done', ask why. Many rules are habits that outlived their reason.",
            "Rebuild from the ground up" to
                "With only the true basics, ask: 'If I designed this from scratch, what would I do?' This is where original solutions come from."
        ),
        drill = "Pick one 'that's just how it is' belief in your life (e.g. 'I can't save money'). List what's actually true and rebuild a solution from those facts.",
        quiz = quiz(
            "What's the first step of first-principles thinking?",
            "Copy what successful people do",
            "Identify what's definitely true and remove assumptions",
            "Brainstorm wild ideas",
            answer = 1,
            why = "First-principles reasoning starts by stripping a problem to its fundamental truths before rebuilding a solution."
        )
    ),
    lesson(
        "Spot your thinking traps",
        7,
        hook = "Your brain takes shortcuts that feel like logic. Knowing the common ones protects your decisions.",
        ideas = listOf(
            "Confirmation bias" to "We notice evidence that agrees with us and ignore what doesn't. Actively look for the best argument against your view.",
            "Sunk cost" to
                "Continuing something because of what you've already spent — time, money, effort — even when it's no longer worth it. Ask: 'Starting from today, would I choose this?'",
            "Availability bias" to
                "Judging how likely something is by how easily examples come to mind. Plane crashes feel common because they're memorable, not because they are."
        ),
        drill = "Find one decision you're making where sunk cost might be at play. Ask: 'Starting from today, would I still choose this?'",
        quiz = quiz(
            "You keep watching a bad series because you're already 5 episodes in. That's…",
            "Confirmation bias",
            "The sunk cost fallacy",
            "Availability bias",
            answer = 1,
            why = "Continuing because of past investment rather than future value is the sunk cost fallacy."
        )
    ),
    lesson(
        "Make better decisions",
        7,
        hook = "Good decisions aren't about predicting the future perfectly — they're about using a better process.",
        ideas = listOf(
            "Run a pre-mortem" to "Imagine it's a year later and the decision failed. Write down why. It surfaces risks optimism would have hidden.",
            "Use 10/10/10" to "How will I feel about this in 10 minutes, 10 months and 10 years? It separates short-term emotion from long-term value.",
            "Reversible vs irreversible" to "Decide reversible things quickly and learn from the result. Slow down only for decisions that are hard to undo."
        ),
        drill = "Take one decision you're facing this week and run a quick pre-mortem: list three reasons it could fail and how you'd prevent each.",
        quiz = quiz(
            "Which decisions deserve the most time and analysis?",
            "All decisions equally",
            "Those that are hard to reverse",
            "Small daily choices",
            answer = 1,
            why = "Irreversible decisions carry lasting consequences; reversible ones are better made quickly and adjusted."
        )
    ),
    lesson(
        "Write clearly",
        7,
        hook = "Clear writing is clear thinking made visible — and it's how people judge you in every message you send.",
        ideas = listOf(
            "Short sentences, plain words" to
                "Aim for sentences under 20 words. Use 'use' not 'utilise', 'help' not 'facilitate'. Simple language reads as confident.",
            "One message, one point" to "Put the main point or request in the first line. If a message has several asks, number them.",
            "Cut ruthlessly" to
                "Read it back and delete every word that doesn't change the meaning — 'just', 'really', 'I think that'. Most drafts improve by losing a third of their words."
        ),
        drill = "Rewrite one message or email you sent recently: main point first, short sentences, a third fewer words.",
        quiz = quiz(
            "Where should the main request go in a work message?",
            "At the end, after the context",
            "In the first line",
            "In a P.S.",
            answer = 1,
            why = "Putting the request first means the reader sees it immediately, even if they only skim."
        )
    ),
    lesson(
        "Solve problems systematically",
        8,
        hook = "Most problems aren't hard — they're poorly defined.",
        ideas = listOf(
            "Define the real problem" to
                "Write the problem as one specific sentence. 'Work is stressful' is vague; 'I keep missing deadlines because I start late' is solvable.",
            "Ask 'why' five times" to
                "Keep asking why until you reach a root cause. Missed deadlines → start late → unclear first step → tasks too big. Now you know what to fix.",
            "Test small, then scale" to "Try the smallest version of a solution for a week, check whether it worked, and adjust. Experiments beat grand plans."
        ),
        drill = "Pick one recurring problem in your life. Write it as one specific sentence and ask 'why' five times to find the root cause.",
        quiz = quiz(
            "What's the purpose of the '5 whys'?",
            "To blame someone",
            "To get from symptoms to the root cause",
            "To delay a decision",
            answer = 1,
            why = "Repeatedly asking why moves past surface symptoms to the cause you can actually fix."
        )
    ),
    lesson(
        "Use mental models",
        8,
        hook = "Smart people don't know more facts — they carry a few powerful ways of thinking into everything.",
        ideas = listOf(
            "Second-order thinking" to
                "Ask 'And then what?' Skipping sleep to work late gives more hours today (first order) but worse focus tomorrow (second order).",
            "Inversion" to
                "Instead of 'How do I succeed?', ask 'How would I guarantee failure?' — then avoid those things. It's often easier to see what to avoid.",
            "The 80/20 rule" to
                "Roughly 80% of results come from 20% of efforts. Find the few actions that drive most of your progress and protect time for them."
        ),
        drill = "Use inversion on one goal: list five ways you could guarantee failing at it. Pick the one you're most likely to do and plan to avoid it.",
        quiz = quiz(
            "Asking 'How could I make this fail?' to plan better is called…",
            "Second-order thinking",
            "Inversion",
            "The 80/20 rule",
            answer = 1,
            why = "Inversion approaches a problem backwards, exposing mistakes to avoid that forward planning misses."
        )
    ),
    lesson(
        "Grow your emotional intelligence",
        8,
        hook = "How well you understand and manage emotions predicts relationships and career success as much as IQ.",
        ideas = listOf(
            "Name it precisely" to
                "'Bad' is vague; 'disappointed', 'anxious' or 'overlooked' are useful. Precise labels calm the emotional brain and point to what you actually need.",
            "Pause before reacting" to "Strong emotions peak and fade within minutes. A 10-second pause and a breath stop you from saying what you'll regret.",
            "Read others' emotions" to "Notice tone, pace and posture, not just words. 'You seem a bit off today — everything okay?' shows people you see them."
        ),
        drill = "Three times today, stop and name exactly what you're feeling in one precise word. Notice what it tells you that you need.",
        quiz = quiz(
            "Why label emotions precisely rather than saying 'I feel bad'?",
            "It sounds smarter",
            "It calms the emotion and clarifies what you need",
            "It makes others take you seriously",
            answer = 1,
            why = "Specific labelling reduces emotional intensity and turns a vague feeling into a clear signal you can act on."
        )
    ),
    lesson(
        "Build a lifelong learning system",
        8,
        hook = "The people who keep growing aren't more talented — they have a system that keeps them learning.",
        ideas = listOf(
            "Capture ideas in one place" to
                "Keep a single notes app or notebook for everything worth remembering. Scattered notes get lost; one place compounds.",
            "Weekly learning review" to
                "Once a week, review what you captured, turn the best ideas into questions, and pick one to apply. Learning that isn't used fades.",
            "Teach to master" to "Explaining something to someone else — or writing a short post about it — reveals the gaps in your understanding fast."
        ),
        drill = "Choose your one capture place and set a weekly 15-minute learning review in your routines.",
        quiz = quiz(
            "Why does teaching something help you learn it?",
            "It saves time",
            "It exposes gaps in your understanding",
            "It's more fun",
            answer = 1,
            why = "Explaining forces you to organise the knowledge and reveals exactly where your understanding is weak."
        )
    )
)
