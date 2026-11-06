package com.glowup.app.ui.data.style

import androidx.compose.ui.graphics.Color
import com.glowup.app.ui.data.style.Garment.*
import com.glowup.app.ui.theme.Maroon
import com.glowup.app.ui.theme.MaroonTint
import com.glowup.app.ui.theme.Navy
import com.glowup.app.ui.theme.NavyTint
import com.glowup.app.ui.theme.Teal
import com.glowup.app.ui.theme.TealTint
import com.glowup.app.ui.theme.YellowTint
import com.glowup.app.ui.data.style.Palette as P

internal val athleisure = StyleGuide(
    key = "athleisure",
    title = "Athleisure",
    tagline = "Sport-inspired comfort that still looks put together.",
    blurb = "Sporty but put together",
    tint = TealTint, accent = Teal, heroText = Color(0xFF1C5C52),
    palette = listOf(P.Black, P.Grey, P.Navy, P.White, P.Sage),
    goodFor = listOf("Gym", "Errands", "Travel days", "Casual Fridays"),
    keyPieces = listOf(
        "Tapered joggers in black or grey",
        "A clean crewneck or zip hoodie",
        "Quality trainers, kept clean",
        "A technical zip jacket",
        "Moisture-wicking tees",
        "A cap or a minimal crossbody bag",
    ),
    stylingTips = listOf(
        "Pick one accent colour and repeat it through the outfit",
        "Choose tapered joggers over baggy sweats",
        "A clean white sneaker goes with everything here",
        "Matching tops and bottoms in the same tone look intentional",
    ),
    keepInMind = listOf(
        "Don't mix more than two bold logos",
        "Keep the fit tailored — baggy everywhere reads sloppy, not sporty",
        "Save true gym wear for the gym",
        "Wash and replace stretched or faded pieces",
    ),
    pairings = listOf(
        Pairing("Joggers", listOf("Crew tee", "Zip jacket", "Clean sneakers"), "Leather shoes"),
        Pairing("Zip jacket", listOf("Plain tee", "Joggers", "Dark jeans"), "Formal shirts"),
        Pairing("Running sneakers", listOf("All athletic pieces", "Jeans on travel days"), "Tailoring"),
    ),
    outfits = outfits("athleisure") {
        outfit(
            "Gym to coffee", listOf("Gym", "Weekend"),
            why = "Black and grey hide sweat and look clean afterwards. The cap covers post-workout hair.",
            dressUp = "Throw on a navy zip jacket.",
            dressDown = "It's already as easy as it gets.",
            piece("Performance tee", TEE, P.Black), piece("Grey joggers", JOGGERS, P.Grey),
            piece("Running sneakers", SNEAKERS, P.Grey), piece("Black cap", CAP, P.Black),
        )
        outfit(
            "Zip-up commute", listOf("Travel", "College"),
            why = "A navy zip jacket over white looks sharp, and everything is comfortable for long journeys.",
            dressUp = "Swap joggers for dark jeans.",
            dressDown = "Swap the jacket for a hoodie.",
            piece("White tee", TEE, P.White), piece("Navy zip jacket", JACKET, P.Navy),
            piece("Black joggers", JOGGERS, P.Black), piece("White sneakers", SNEAKERS, P.White), piece("Black sport watch", WATCH, P.Black),
        )
        outfit(
            "Elevated sweats", listOf("Weekend", "Travel"),
            why = "Light grey on charcoal is a soft tonal combination — it reads 'chosen', not 'just woke up'.",
            dressUp = "Add a black overshirt on top.",
            dressDown = "Swap sneakers for slides at home.",
            piece("Light-grey hoodie", HOODIE, P.LightGrey), piece("Charcoal joggers", JOGGERS, P.Charcoal),
            piece("White sneakers", SNEAKERS, P.White), piece("Black crossbody", BAG, P.Black),
        )
        outfit(
            "Hot-day training", listOf("Gym"),
            why = "A tank and shorts keep you cool. Sage is easy on the eye and doesn't show sweat as much as grey.",
            dressUp = "Throw on a tee and joggers after.",
            dressDown = "Already minimal.",
            piece("Sage tank", TANK, P.Sage), piece("Black shorts", SHORTS, P.Black),
            piece("Black trainers", SNEAKERS, P.Black), piece("White cap", CAP, P.White),
        )
        outfit(
            "Tennis club", listOf("Weekend", "Gym"),
            why = "White polo and navy shorts is the classic court look — sporty but polished enough for brunch after.",
            dressUp = "Swap shorts for stone chinos.",
            dressDown = "Change the polo for a white tee.",
            piece("White polo", POLO, P.White), piece("Navy shorts", SHORTS, P.Navy),
            piece("White sneakers", SNEAKERS, P.White), piece("Silver watch", WATCH, P.Silver), piece("Black sunglasses", SUNGLASSES, P.Black),
        )
    },
)

internal val formal = StyleGuide(
    key = "formal",
    title = "Formal",
    tagline = "Sharp, deliberate, built for the room that matters.",
    blurb = "Sharp for big occasions",
    tint = NavyTint, accent = Navy, heroText = Color(0xFF2C4E8E),
    palette = listOf(P.Navy, P.Charcoal, P.White, P.LightBlue, P.Burgundy),
    goodFor = listOf("Weddings", "Interviews", "Business meetings", "Formal dinners"),
    keyPieces = listOf(
        "A well-fitted suit in navy or charcoal",
        "Crisp dress shirts in white and light blue",
        "Black Oxfords or derbies, polished",
        "Two or three ties in solid colours",
        "A leather belt matching your shoes",
        "A slim dress watch",
    ),
    stylingTips = listOf(
        "A slightly shorter trouser break looks sharper",
        "Show about 1 cm of shirt cuff below the jacket sleeve",
        "Match sock colour to your trousers, not your shoes",
        "The tie tip should just touch your belt buckle",
    ),
    keepInMind = listOf(
        "Get the fit tailored — off-the-rack rarely sits right",
        "Don't mix more than two patterns at once",
        "Undo the bottom jacket button, always",
        "Keep accessories minimal — one statement piece max",
    ),
    pairings = listOf(
        Pairing("Navy suit", listOf("White or light-blue shirt", "Burgundy or navy tie", "Brown or black shoes"), "Black shirts"),
        Pairing("Charcoal suit", listOf("White shirt", "Black shoes", "Navy tie"), "Brown shoes at evening events"),
        Pairing("White dress shirt", listOf("Every suit colour", "Any solid tie"), "A visible vest underneath"),
    ),
    outfits = outfits("formal") {
        outfit(
            "Navy suit classic", listOf("Wedding", "Interview", "Office"),
            why = "Navy is the most flattering suit colour for most people. A burgundy tie adds warmth without being loud.",
            dressUp = "Add a white pocket square.",
            dressDown = "Drop the tie and open the top button.",
            piece("White dress shirt", SHIRT, P.White), piece("Navy suit jacket", BLAZER, P.Navy),
            piece("Navy suit trousers", TROUSERS, P.Navy), piece("Dark-brown derbies", DERBIES, P.DarkBrown),
            piece("Burgundy tie", TIE, P.Burgundy), piece("Silver watch", WATCH, P.Silver),
        )
        outfit(
            "Charcoal interview", listOf("Interview", "Office"),
            why = "Charcoal reads serious and competent. Light blue softens it so you look approachable, not severe.",
            dressUp = "Switch to a white shirt for evening events.",
            dressDown = "Wear the jacket alone with dark jeans.",
            piece("Light-blue shirt", SHIRT, P.LightBlue), piece("Charcoal suit jacket", BLAZER, P.Charcoal),
            piece("Charcoal trousers", TROUSERS, P.Charcoal), piece("Black derbies", DERBIES, P.Black), piece("Navy tie", TIE, P.Navy),
        )
        outfit(
            "Evening black", listOf("Party", "Wedding"),
            why = "A black suit with a black tie is sleek for evening events — let the fit do all the work.",
            dressUp = "Swap the tie for a black bow tie.",
            dressDown = "Lose the tie and wear a black knit underneath.",
            piece("White dress shirt", SHIRT, P.White), piece("Black suit jacket", BLAZER, P.Black),
            piece("Black trousers", TROUSERS, P.Black), piece("Black loafers", LOAFERS, P.Black),
            piece("Black tie", TIE, P.Black), piece("Silver watch", WATCH, P.Silver),
        )
        outfit(
            "The odd jacket", listOf("Office", "Date", "Wedding"),
            why = "A navy jacket with grey trousers is less stiff than a matching suit, and wears far more often.",
            dressUp = "Add a navy knitted tie.",
            dressDown = "Swap the shirt for a fine grey knit.",
            piece("White shirt", SHIRT, P.White), piece("Navy blazer", BLAZER, P.Navy),
            piece("Grey trousers", TROUSERS, P.Grey), piece("Brown loafers", LOAFERS, P.Brown), piece("Knitted navy tie", TIE, P.Navy),
        )
        outfit(
            "Summer wedding", listOf("Wedding"),
            why = "Light, warm colours suit daytime weddings in heat. Tan loafers keep it relaxed but correct.",
            dressUp = "Add a light-blue tie.",
            dressDown = "Remove the jacket for the reception.",
            piece("Light-blue shirt", SHIRT, P.LightBlue), piece("Stone linen jacket", BLAZER, P.Stone),
            piece("Light-grey trousers", TROUSERS, P.LightGrey), piece("Tan loafers", LOAFERS, P.Tan), piece("Brown sunglasses", SUNGLASSES, P.Brown),
        )
    },
)

internal val festive = StyleGuide(
    key = "festive",
    title = "Festive & Ethnic",
    tagline = "Rich colour and craft for weddings, festivals and family occasions.",
    blurb = "Kurtas, Nehru jackets, mojaris",
    tint = YellowTint, accent = Color(0xFF8A6A00), heroText = Color(0xFF7A5C00),
    palette = listOf(P.Ivory, P.Maroon, P.Emerald, P.Gold, P.Navy),
    goodFor = listOf("Weddings", "Festivals", "Pujas", "Sangeet"),
    keyPieces = listOf(
        "A straight-cut kurta in ivory or a jewel tone",
        "Churidar or straight-cut pants",
        "A Nehru jacket",
        "Mojaris or juttis",
        "A bandhgala for formal events",
        "A simple stole",
    ),
    stylingTips = listOf(
        "Let one piece be rich — embroidered jacket or kurta — and keep the rest plain",
        "Silk and brocade for evenings, cotton and linen for daytime",
        "Kurtas look best ending between mid-thigh and the knee",
        "Mojaris or juttis, never sports shoes",
    ),
    keepInMind = listOf(
        "Avoid head-to-toe heavy embroidery",
        "Sleeves should end at the wrist bone",
        "Steam the kurta — creases show on light fabrics",
        "Keep jewellery to one or two pieces",
    ),
    pairings = listOf(
        Pairing("Ivory kurta", listOf("Maroon, navy or emerald Nehru jacket", "Tan mojaris"), "Sports shoes"),
        Pairing("Nehru jacket", listOf("Kurtas", "A white shirt with trousers"), "Prints on prints"),
        Pairing("Mojaris", listOf("Kurta sets", "Linen trousers"), "Suits"),
    ),
    outfits = outfits("festive") {
        outfit(
            "Ivory classic", listOf("Festive", "Wedding"),
            why = "An all-ivory kurta set is timeless and works at almost any function. A gold watch is the only accent it needs.",
            dressUp = "Add a maroon Nehru jacket.",
            dressDown = "Swap mojaris for tan sandals at a daytime puja.",
            piece("Ivory kurta", KURTA, P.Ivory), piece("Ivory churidar", TROUSERS, P.Ivory),
            piece("Tan mojaris", LOAFERS, P.Tan), piece("Gold watch", WATCH, P.Gold),
        )
        outfit(
            "Nehru jacket evening", listOf("Wedding", "Festive", "Party"),
            why = "A maroon Nehru jacket over ivory is rich and festive without being loud — perfect for evening functions.",
            dressUp = "Pick a silk or brocade jacket.",
            dressDown = "Wear the jacket over a white shirt with trousers.",
            piece("Ivory kurta", KURTA, P.Ivory), piece("Maroon Nehru jacket", NEHRU, P.Maroon),
            piece("Cream pants", TROUSERS, P.Cream), piece("Brown mojaris", LOAFERS, P.Brown),
        )
        outfit(
            "Emerald sangeet", listOf("Party", "Festive"),
            why = "Emerald is a jewel tone that suits most skin tones and stands out in photos. Gold details complete it.",
            dressUp = "Add a gold-toned stole.",
            dressDown = "Choose a cotton kurta for daytime.",
            piece("Emerald kurta", KURTA, P.Emerald), piece("Ivory pants", TROUSERS, P.Ivory),
            piece("Gold mojaris", LOAFERS, P.Gold), piece("Gold bracelet", BRACELET, P.Gold),
        )
        outfit(
            "Navy bandhgala", listOf("Wedding"),
            why = "A navy bandhgala is the formal Indian equivalent of a suit — sharp, regal and right for the main ceremony.",
            dressUp = "Add a pocket square and a brooch.",
            dressDown = "Wear the jacket open over a kurta.",
            piece("Ivory kurta", KURTA, P.Ivory), piece("Navy bandhgala", BANDHGALA, P.Navy),
            piece("Cream trousers", TROUSERS, P.Cream), piece("Brown mojaris", LOAFERS, P.DarkBrown),
        )
        outfit(
            "Puja morning", listOf("Festive", "Weekend"),
            why = "Pastel cotton is cool and fresh for daytime rituals — festive, but not heavy.",
            dressUp = "Add an ivory Nehru jacket.",
            dressDown = "Wear the kurta with jeans for a casual festival day.",
            piece("Blush cotton kurta", KURTA, P.Blush), piece("White pants", TROUSERS, P.White),
            piece("Tan sandals", SANDALS, P.Tan),
        )
    },
)

internal val jewellery = StyleGuide(
    key = "jewellery",
    title = "Jewellery",
    tagline = "Small details, big difference. Wear metal with intention.",
    blurb = "Small details, big impact",
    tint = MaroonTint, accent = Maroon, heroText = Color(0xFF7A2E3D),
    palette = listOf(P.Gold, P.Silver, P.Black, P.Brown, P.White),
    goodFor = listOf("Date nights", "Nights out", "Casual days", "Smart-casual events"),
    keyPieces = listOf(
        "A simple chain or pendant",
        "One ring, not a full set",
        "Matched metal tones",
        "A watch that fits the occasion",
        "A subtle bracelet for balance",
        "Stud earrings, if you wear them",
    ),
    stylingTips = listOf(
        "Let one piece be the hero, keep the rest simple",
        "Gold flatters warm skin and warm colours; silver suits cool tones",
        "A chain sits outside a tee and inside an open shirt",
        "Clean your jewellery regularly — tarnish kills the look",
    ),
    keepInMind = listOf(
        "Don't mix gold and silver at once",
        "Skip anything that jingles when you walk",
        "Less is more — one standout piece beats five small ones",
        "Match metal tone to your watch and belt buckle",
    ),
    pairings = listOf(
        Pairing("Gold", listOf("Navy", "Cream", "Brown", "Olive"), "Mixing with silver"),
        Pairing("Silver", listOf("Black", "Grey", "White", "Blue"), "Warm brown leathers"),
        Pairing("Leather bracelet", listOf("Denim", "Tees", "Casual shirts"), "Formal suits"),
    ),
    outfits = outfits("jewellery") {
        outfit(
            "One chain, one watch", listOf("Date", "Weekend"),
            why = "Silver on black and white is crisp — a fine chain and a matching watch are all the detail you need.",
            dressUp = "Add a black overshirt.",
            dressDown = "Lose the watch for a leather bracelet.",
            piece("White tee", TEE, P.White), piece("Black jeans", JEANS, P.Black),
            piece("White sneakers", SNEAKERS, P.White), piece("Silver chain", CHAIN, P.Silver), piece("Silver watch", WATCH, P.Silver),
        )
        outfit(
            "Gold on navy", listOf("Date", "Party"),
            why = "Gold glows against navy and stone. A single ring and a gold watch feel warm and grown-up.",
            dressUp = "Add a camel coat.",
            dressDown = "Swap loafers for white sneakers.",
            piece("Navy knit", KNIT, P.Navy), piece("Stone trousers", TROUSERS, P.Stone),
            piece("Brown loafers", LOAFERS, P.Brown), piece("Gold watch", WATCH, P.Gold), piece("Gold ring", RING, P.Gold),
        )
        outfit(
            "All-black dinner", listOf("Party", "Date"),
            why = "Silver pops against black. A statement ring and bracelet make a simple outfit feel like an evening look.",
            dressUp = "Add a black blazer.",
            dressDown = "Swap the shirt for a black tee.",
            piece("Black shirt", SHIRT, P.Black), piece("Black trousers", TROUSERS, P.Black),
            piece("Black loafers", LOAFERS, P.Black), piece("Silver ring", RING, P.Silver), piece("Silver bracelet", BRACELET, P.Silver),
        )
        outfit(
            "Festive gold", listOf("Festive", "Wedding"),
            why = "Gold accents are made for ivory and festive occasions — keep them matched and limited to two or three.",
            dressUp = "Add a maroon Nehru jacket.",
            dressDown = "Keep just the watch for daytime.",
            piece("Ivory kurta", KURTA, P.Ivory), piece("Ivory pants", TROUSERS, P.Ivory),
            piece("Tan mojaris", LOAFERS, P.Tan), piece("Gold bracelet", BRACELET, P.Gold), piece("Gold ring", RING, P.Gold),
        )
        outfit(
            "Weekend leather", listOf("Weekend", "Travel"),
            why = "A leather bracelet and sunglasses add personality to a plain tee and jeans without trying too hard.",
            dressUp = "Add a silver watch.",
            dressDown = "Already relaxed.",
            piece("Sage tee", TEE, P.Sage), piece("Mid-wash jeans", JEANS, P.Denim),
            piece("White sneakers", SNEAKERS, P.White), piece("Leather bracelet", BRACELET, P.Brown), piece("Black sunglasses", SUNGLASSES, P.Black),
        )
    },
)
