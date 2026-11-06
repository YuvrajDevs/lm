package com.glowup.app.ui.data.program

internal val communicationTrack = listOf(
    lesson(
        "Slow down to be heard", 5,
        hook = "Most people who 'aren't listened to' are simply talking faster than listeners can follow.",
        ideas = listOf(
            "Aim for about 140 words a minute" to "Nervous speakers drift towards 180+. At 140, people follow without effort — it feels calm and deliberate rather than slow.",
            "Pause at every full stop" to "A one-second pause after each sentence gives the listener time to absorb it and makes you sound in control. Silence reads as confidence, not hesitation.",
            "Slow down most on the important part" to "Numbers, names and your main point deserve the slowest delivery. Speeding up on the key line is the most common way good ideas get lost.",
        ),
        drill = "Read one paragraph of anything out loud, timing yourself. Then read it again 20% slower with a full pause at every full stop. Notice which version sounds more sure of itself.",
        quiz = quiz(
            "You're explaining a deadline in a meeting. Where should you slow down most?",
            "At the start, to warm up", "On the date and what's needed", "Evenly throughout",
            answer = 1,
            why = "Slow down on the information people must remember. The surrounding sentences can flow at a normal pace.",
        ),
    ),
    lesson(
        "Finish every word", 5,
        hook = "Mumbling isn't a voice problem — it's dropped word endings and a mouth that barely opens.",
        ideas = listOf(
            "Land the last syllable" to "Clarity usually dies at the end of words and sentences: 'gonna', 'didn'', trailing off. Consciously finish the final consonant of the last word in each sentence.",
            "Open your mouth wider" to "Speaking through half-closed teeth muffles everything. Exaggerate your jaw movement slightly — it will feel big to you and sound normal to everyone else.",
            "Project to the back of the room" to "Volume should match the furthest person who needs to hear you, not the nearest. Breathe from your belly so volume doesn't turn into strain.",
        ),
        drill = "Say this three times, slowly, hitting every ending: 'I'd like to take that test tomorrow at twelve.' Then record a 20-second voice note and listen once for trailing endings.",
        quiz = quiz(
            "What's the quickest fix for sounding mumbled?",
            "Speak louder only", "Open the mouth more and finish word endings", "Use simpler words",
            answer = 1,
            why = "Volume helps, but most mumbling comes from a closed jaw and swallowed endings. Fix those and you'll be understood even at a normal volume.",
        ),
    ),
    lesson(
        "Replace filler with pauses", 6,
        hook = "'Um', 'like' and 'you know' quietly tell people you're unsure — even when you aren't.",
        ideas = listOf(
            "Fillers are thinking noise" to "They appear when your brain is finding the next words and you're afraid of silence. The fix isn't to think faster — it's to be comfortable not making a sound.",
            "Find your top filler first" to "Everyone has one or two favourites. Spotting just one and catching it for a week works far better than trying to remove all of them at once.",
            "Close your mouth to stop the 'um'" to "You can't say 'um' with your lips closed. When you feel one coming, close your mouth, breathe, then start the next sentence.",
        ),
        drill = "Record yourself describing your day for 60 seconds. Count your fillers. Do it again, closing your mouth every time you feel one coming. Compare the counts.",
        quiz = quiz(
            "What do listeners usually hear when you pause silently instead of saying 'um'?",
            "Hesitation", "Thoughtfulness and control", "That you've forgotten what to say",
            answer = 1,
            why = "A short silent pause feels long to the speaker but sounds deliberate to the listener. Fillers are what make speakers sound unsure.",
        ),
    ),
    lesson(
        "Lead with the answer", 6,
        hook = "When someone asks a question, they want the answer first and the backstory second — if at all.",
        ideas = listOf(
            "Bottom line up front" to "Start with your conclusion in one sentence: 'Yes, we can ship Friday.' Then give the reasons. People can stop listening whenever they have enough.",
            "Build answers in threes" to "Answer, then up to three reasons or details, then stop. 'Yes — because A, B and C.' Three is enough to feel complete without rambling.",
            "End when you've answered" to "Rambling usually comes from not knowing how to finish. Repeat your answer in a few words ('So — Friday works') and stop talking.",
        ),
        drill = "The next three questions anyone asks you today, answer in your first sentence before explaining anything. Notice how people react.",
        quiz = quiz(
            "Your manager asks 'How's the project going?' Which reply leads with the answer?",
            "'So on Monday we started looking at…'", "'On track for Friday — two things left: testing and the final review.'", "'It's been a bit of a week, honestly…'",
            answer = 1,
            why = "It states the status first, then gives exactly the detail needed. The others make the listener wait for the point.",
        ),
    ),
    lesson(
        "Tell a story people remember", 7,
        hook = "Facts inform. Stories are what people repeat to others the next day.",
        ideas = listOf(
            "Setup, tension, payoff" to "Every good short story has a normal situation, something going wrong or at stake, and how it turned out. If there's no tension, it's an update, not a story.",
            "Start in the middle" to "Skip the long build-up. 'I was standing outside the interview room, and I realised I'd brought the wrong CV' is a far better opening than how you got there.",
            "Give one vivid detail" to "A single specific detail — the coffee stain, the exact words someone said — makes a story feel real. Ten general details make it feel long.",
        ),
        drill = "Pick something small that went wrong this month. Tell it out loud in under 60 seconds using setup–tension–payoff, starting in the middle.",
        quiz = quiz(
            "What turns an update into a story?",
            "Adding more detail", "Something at stake or going wrong", "Telling it in the past tense",
            answer = 1,
            why = "Tension is what makes people care what happens next. Without it, extra detail just makes the update longer.",
        ),
    ),
    lesson(
        "Ask questions that open people up", 6,
        hook = "The best conversationalists talk less — they ask the questions that make others want to talk.",
        ideas = listOf(
            "Use 'what' and 'how'" to "'Did you enjoy it?' gets a yes. 'What was the best part?' gets a story. Open questions invite people to go deeper.",
            "Follow the energy" to "Notice what someone lights up about and ask about that, not your next prepared question. 'Wait — you climb? How did you get into that?'",
            "Ask the second question" to "Most people ask one question and then talk about themselves. Asking a follow-up to the answer is what makes someone feel genuinely interesting.",
        ),
        drill = "In one conversation today, ask at least two follow-up questions on the same topic before sharing anything about yourself.",
        quiz = quiz(
            "Which question is most likely to start a real conversation?",
            "'Was the trip good?'", "'What surprised you most on the trip?'", "'Did you go anywhere nice?'",
            answer = 1,
            why = "It's open, specific, and asks for a story rather than a yes/no — it gives the other person something to run with.",
        ),
    ),
    lesson(
        "Listen so people feel heard", 7,
        hook = "Being a good listener isn't silence — it's showing people you actually understood them.",
        ideas = listOf(
            "Reflect before you respond" to "Summarise what you heard in one line before giving your view: 'So the real problem is the deadline, not the work itself?' It proves you listened and catches misunderstandings.",
            "Name the feeling" to "'That sounds really frustrating' does more for someone than any advice. People need to feel understood before they're open to solutions.",
            "Ask before advising" to "'Do you want ideas, or do you just need to vent?' saves you from fixing things nobody asked you to fix — a very common source of friction.",
        ),
        drill = "In your next real conversation, reflect back what you heard once before replying, and name one feeling you notice.",
        quiz = quiz(
            "A friend is upset about work. What usually helps most first?",
            "Telling them what you'd do", "Reflecting what they said and naming the feeling", "Changing the subject to cheer them up",
            answer = 1,
            why = "Feeling understood comes before being ready for advice. Jumping straight to solutions often makes people feel unheard.",
        ),
    ),
    lesson(
        "Explain anything simply", 7,
        hook = "If you can explain something to a 12-year-old, you understand it — and so will everyone else.",
        ideas = listOf(
            "Start with the one-line version" to "Before details, say what it is in plain words: 'A mortgage is a loan to buy a home that you pay back over years.' The details then have somewhere to hang.",
            "Use an analogy" to "Connect the new thing to something familiar: 'An API is like a waiter — you ask for something, it brings it from the kitchen.' Analogies make ideas stick.",
            "Check, don't assume" to "Pause and ask 'Does that make sense so far?' or better, 'What part should I go deeper on?' Then layer in detail only where it's needed.",
        ),
        drill = "Explain something you know well from your work or studies in 3 sentences using one analogy. Try it on a friend or out loud to yourself.",
        quiz = quiz(
            "What's the best way to begin explaining a complex topic?",
            "With its history", "With a one-line plain-language summary", "With the most technical detail, to show expertise",
            answer = 1,
            why = "A simple frame first lets the listener place every later detail. Starting with history or jargon loses them early.",
        ),
    ),
    lesson(
        "Persuade without pushing", 8,
        hook = "People rarely change their minds because they were told to — they change when they see their own problem solved.",
        ideas = listOf(
            "Problem, then idea, then benefit" to "Start with the problem they already feel, then your idea, then what's in it for them. Leading with your idea makes people defend their current position.",
            "Use their words and goals" to "Frame your point around what they care about: 'You said speed matters most — this cuts the review time in half.' People trust arguments built on their own priorities.",
            "Acknowledge the downside" to "Naming the main weakness yourself ('It'll cost a week up front') makes you more credible, not less. It shows you've thought it through.",
        ),
        drill = "Think of something you want someone to agree to this week. Write it as: their problem → your idea → their benefit → the honest downside. Then have the conversation.",
        quiz = quiz(
            "Why mention the main downside of your own proposal?",
            "It's polite", "It increases trust and credibility", "It lowers expectations",
            answer = 1,
            why = "When you raise the weakness first, people stop hunting for it and trust the rest of what you say.",
        ),
    ),
    lesson(
        "Have the difficult conversation", 8,
        hook = "Avoiding a hard conversation rarely makes the problem smaller — it just makes the conversation harder later.",
        ideas = listOf(
            "Use Situation–Behaviour–Impact" to "'In yesterday's meeting (situation), you interrupted me twice (behaviour), and I lost my train of thought (impact).' Specific and factual — no labels like 'you're rude'.",
            "Get curious before concluding" to "Follow with a real question: 'Was something going on?' You may be missing context, and curiosity lowers defensiveness.",
            "Agree on one next step" to "End with something concrete: 'Could we each take turns finishing our point?' A hard conversation without a next step usually repeats.",
        ),
        drill = "Think of one small thing that's been bothering you with someone. Write the SBI sentence and one curious question. Have the conversation, or rehearse it out loud.",
        quiz = quiz(
            "Which opening is most likely to keep someone from getting defensive?",
            "'You're always so dismissive.'", "'In today's call, when my idea was cut off, I felt unheard.'", "'We need to talk about your attitude.'",
            answer = 1,
            why = "It describes a specific moment and its effect, not a judgement of their character, so there's nothing to defend against.",
        ),
    ),
    lesson(
        "Present to a room", 8,
        hook = "Great presenters aren't fearless — they're prepared in the specific ways that matter.",
        ideas = listOf(
            "Open with a hook, not your name" to "Start with a question, a surprising number or a short story. 'Most people forget 90% of a talk within a week' earns attention in a way 'Hi, I'm…' doesn't.",
            "Three points, signposted" to "Tell them the structure ('Three things: cost, timing, risk'), then use clear transitions ('Second — timing'). Signposts keep people with you.",
            "Rehearse the first minute word for word" to "Nerves peak at the start. Knowing your first 60 seconds cold gets you through the hardest part; after that, speak from points, not a script.",
        ),
        drill = "Prepare a 2-minute talk on any topic you know: a hook, three signposted points, a one-line close. Rehearse the first minute out loud three times.",
        quiz = quiz(
            "Why memorise only the opening of a talk?",
            "Openings are the most important content", "Nerves are highest at the start; after that you can speak from notes", "Audiences only remember the beginning",
            answer = 1,
            why = "A solid opening carries you through the adrenaline spike. Fully scripted talks sound read; key points keep the rest natural.",
        ),
    ),
    lesson(
        "Speak well on the spot", 8,
        hook = "Being put on the spot is where communication skill really shows — and there's a framework for it.",
        ideas = listOf(
            "Use PREP" to "Point, Reason, Example, Point. 'I'd pick option B. It's cheaper long-term. Last year a similar switch saved us 20%. So — B.' It works for almost any unexpected question.",
            "Buy two seconds honestly" to "'Good question — let me think for a second' is completely acceptable and far better than rushing into a ramble.",
            "Answer the question you were asked" to "Under pressure we drift to what we're comfortable talking about. Repeat the question in your head first, then answer exactly that.",
        ),
        drill = "Ask a friend (or use random topics) for three surprise questions. Answer each in under 45 seconds using PREP. Record at least one and listen back.",
        quiz = quiz(
            "What does PREP stand for?",
            "Plan, Rehearse, Explain, Pause", "Point, Reason, Example, Point", "Problem, Result, Evidence, Proof",
            answer = 1,
            why = "Point–Reason–Example–Point gives any spontaneous answer a clear beginning and end, so you don't ramble.",
        ),
    ),
)
