package com.glowup.app.ui.data.style

import androidx.compose.ui.graphics.Color
import com.glowup.app.ui.data.style.Garment.*
import com.glowup.app.ui.theme.BlueTint
import com.glowup.app.ui.theme.Coral
import com.glowup.app.ui.theme.CoralTint
import com.glowup.app.ui.theme.Green
import com.glowup.app.ui.theme.GreenTint
import com.glowup.app.ui.theme.Navy
import com.glowup.app.ui.theme.Purple
import com.glowup.app.ui.theme.PurpleTint
import com.glowup.app.ui.data.style.Palette as P

internal val oldMoney = StyleGuide(
    key = "old-money",
    title = "Old Money",
    tagline = "Quiet, tailored, no logos. Looks expensive without trying to.",
    blurb = "Quiet, tailored, no logos",
    tint = CoralTint, accent = Coral, heroText = Color(0xFF8A6A58),
    palette = listOf(P.Navy, P.Cream, P.Camel, P.Forest, P.Brown),
    goodFor = listOf("Office", "Smart dinners", "Family events", "Weekends away"),
    keyPieces = listOf(
        "Crisp white or light-blue Oxford shirt",
        "Tailored chinos or wool trousers",
        "Leather or suede loafers, well kept",
        "A cable-knit or merino sweater",
        "A navy blazer that fits the shoulders",
        "A brown leather belt and a simple watch",
    ),
    stylingTips = listOf(
        "Invest in fabric quality over quantity — wool, cotton, linen, cashmere",
        "Tuck your shirt in for a cleaner, longer silhouette",
        "Roll sleeves neatly twice rather than pushing them up",
        "Pair textures like knit and wool for depth without colour",
    ),
    keepInMind = listOf(
        "Skip visible logos and branding",
        "Avoid anything too shiny or synthetic",
        "Stick to muted, earthy colours",
        "Iron everything — crisp always beats trendy",
    ),
    pairings = listOf(
        Pairing("Navy blazer", listOf("Grey trousers", "White Oxford shirt", "Brown loafers"), "Black jeans and chunky trainers"),
        Pairing("Cream knit", listOf("Stone chinos", "Camel coat", "Suede loafers"), "Neon colours or sporty fabrics"),
        Pairing("Brown loafers", listOf("Chinos", "Linen trousers", "Grey flannel"), "Black suits"),
    ),
    outfits = outfits("old-money") {
        outfit(
            "The navy blazer", listOf("Office", "Interview", "Date"),
            why = "The most versatile tailored look there is: navy and grey are both neutral, and brown leather warms them up so it never feels corporate.",
            dressUp = "Add a knitted navy tie and swap loafers for brown derbies.",
            dressDown = "Lose the blazer and roll the sleeves for weekend lunch.",
            piece("White Oxford shirt", SHIRT, P.White), piece("Navy blazer", BLAZER, P.Navy),
            piece("Grey wool trousers", TROUSERS, P.Grey), piece("Penny loafers", LOAFERS, P.DarkBrown),
            piece("Brown leather belt", BELT, P.Brown), piece("Gold watch", WATCH, P.Gold),
        )
        outfit(
            "Weekend at the club", listOf("Weekend", "Date"),
            why = "Cream and stone are both soft, light neutrals — together they read relaxed and expensive. Suede loafers keep it off-duty.",
            dressUp = "Throw a navy blazer over the knit.",
            dressDown = "Swap loafers for white leather sneakers.",
            piece("Cable-knit sweater", KNIT, P.Cream), piece("Stone chinos", TROUSERS, P.Stone),
            piece("Suede loafers", LOAFERS, P.Brown), piece("Leather watch", WATCH, P.Brown),
        )
        outfit(
            "Linen summer", listOf("Weekend", "Travel", "Wedding"),
            why = "Linen breathes in heat and wrinkles in a way that looks relaxed, not messy. Ivory on beige stays light and cool.",
            dressUp = "Add a light beige blazer for a daytime wedding.",
            dressDown = "Roll the trousers once and switch to sandals.",
            piece("Linen shirt", SHIRT, P.Ivory), piece("Linen trousers", TROUSERS, P.Beige),
            piece("Tan loafers", LOAFERS, P.Tan), piece("Tortoiseshell sunglasses", SUNGLASSES, P.Brown),
        )
        outfit(
            "Quiet layers", listOf("Office", "Date"),
            why = "A forest knit polo under a camel coat is the old-money autumn uniform — rich colours, no pattern, all texture.",
            dressUp = "Swap the polo for a white shirt and add grey flannels.",
            dressDown = "Wear the polo with dark jeans and skip the coat.",
            piece("Knit polo", POLO, P.Forest), piece("Camel coat", JACKET, P.Camel),
            piece("Cream trousers", TROUSERS, P.Cream), piece("Suede boots", BOOTS, P.DarkBrown),
        )
        outfit(
            "Sunday lunch", listOf("Weekend", "College"),
            why = "A navy polo with khaki chinos is the simplest smart-casual pairing that exists, and white leather sneakers keep it young.",
            dressUp = "Swap sneakers for loafers and add a belt that matches.",
            dressDown = "Try tailored shorts in the same khaki for summer.",
            piece("Navy polo", POLO, P.Navy), piece("Khaki chinos", TROUSERS, P.Khaki),
            piece("White leather sneakers", SNEAKERS, P.White), piece("Brown belt", BELT, P.Brown), piece("Silver watch", WATCH, P.Silver),
        )
    },
)

internal val smartCasual = StyleGuide(
    key = "smart-casual",
    title = "Smart Casual",
    tagline = "One smart piece, one relaxed piece. The dress code most of life runs on.",
    blurb = "The everyday sweet spot",
    tint = BlueTint, accent = Navy, heroText = Color(0xFF3A5A8C),
    palette = listOf(P.Navy, P.White, P.Olive, P.Stone, P.Tan),
    goodFor = listOf("Office", "Dinners", "Dates", "Meetings"),
    keyPieces = listOf(
        "An Oxford shirt in white or light blue",
        "Dark jeans with no rips or fading",
        "Chinos in stone, navy or olive",
        "An unstructured blazer",
        "A merino crewneck",
        "Clean white sneakers or suede loafers",
    ),
    stylingTips = listOf(
        "Pair one smart piece with one casual piece — blazer with jeans, shirt with sneakers",
        "Tuck in shirts when you wear a blazer; untucked works without one",
        "Roll sleeves twice, just below the elbow",
        "Match your belt to your shoes",
    ),
    keepInMind = listOf(
        "No graphic tees under a blazer",
        "Avoid running shoes with tailoring",
        "Ironed beats expensive",
        "Fit matters more than brand",
    ),
    pairings = listOf(
        Pairing("Dark jeans", listOf("White tee", "Navy blazer", "Oxford shirt", "Brown boots"), "Ripped or faded washes at work"),
        Pairing("White sneakers", listOf("Chinos", "Dark jeans", "Tailored shorts"), "Suits at formal events"),
        Pairing("Olive overshirt", listOf("White or grey tee", "Dark denim", "Tan boots"), "Other shades of green"),
    ),
    outfits = outfits("smart-casual") {
        outfit(
            "Blazer and denim", listOf("Date", "Party", "Office"),
            why = "The blazer brings structure, the tee and sneakers keep it relaxed. It works almost anywhere that isn't black tie.",
            dressUp = "Swap the tee for a white Oxford shirt and the sneakers for loafers.",
            dressDown = "Drop the blazer for a bomber or overshirt.",
            piece("White tee", TEE, P.White), piece("Unstructured navy blazer", BLAZER, P.Navy),
            piece("Dark jeans", JEANS, P.DarkDenim), piece("White sneakers", SNEAKERS, P.White), piece("Silver watch", WATCH, P.Silver),
        )
        outfit(
            "Oxford and chinos", listOf("Office", "Interview", "Date"),
            why = "Light blue flatters almost every skin tone and stone chinos are the most versatile trouser colour you can own.",
            dressUp = "Add a navy blazer.",
            dressDown = "Leave the shirt untucked with white sneakers.",
            piece("Light-blue Oxford shirt", SHIRT, P.LightBlue), piece("Stone chinos", TROUSERS, P.Stone),
            piece("Suede loafers", LOAFERS, P.Tan), piece("Tan belt", BELT, P.Tan),
        )
        outfit(
            "Merino and olive", listOf("Office", "Weekend"),
            why = "Navy and olive is an underrated pairing — rich without being loud, and brown derbies tie them together.",
            dressUp = "Wear a white collar peeking out under the crewneck.",
            dressDown = "Switch derbies for clean sneakers.",
            piece("Merino crewneck", KNIT, P.Navy), piece("Olive chinos", TROUSERS, P.Olive),
            piece("Brown derbies", DERBIES, P.Brown), piece("Leather watch", WATCH, P.Brown),
        )
        outfit(
            "Overshirt layer", listOf("Weekend", "College", "Travel"),
            why = "An overshirt is the easiest layer in menswear: more polished than a hoodie, less formal than a blazer.",
            dressUp = "Swap the tee for a fine black knit.",
            dressDown = "Wear it open over a tee with joggers on travel days.",
            piece("Light-grey tee", TEE, P.LightGrey), piece("Olive overshirt", OVERSHIRT, P.Olive),
            piece("Dark jeans", JEANS, P.DarkDenim), piece("Brown boots", BOOTS, P.Brown),
        )
        outfit(
            "Dinner polo", listOf("Date", "Party"),
            why = "A black knit polo with charcoal trousers looks sharp at night without needing a jacket or tie.",
            dressUp = "Add a charcoal blazer.",
            dressDown = "Swap trousers for black jeans.",
            piece("Black knit polo", POLO, P.Black), piece("Charcoal trousers", TROUSERS, P.Charcoal),
            piece("Black loafers", LOAFERS, P.Black), piece("Silver watch", WATCH, P.Silver),
        )
    },
)

internal val minimal = StyleGuide(
    key = "minimal",
    title = "Minimal",
    tagline = "Clean lines, neutral tones. Less, but better.",
    blurb = "Clean lines, neutral tones",
    tint = PurpleTint, accent = Purple, heroText = Color(0xFF5A4A8C),
    palette = listOf(P.Black, P.White, P.Grey, P.Charcoal, P.Stone),
    goodFor = listOf("Work", "Everyday", "Travel", "Dates"),
    keyPieces = listOf(
        "Solid-colour tees in black, white and grey",
        "Straight-leg trousers",
        "Clean white or black sneakers",
        "A structured crewneck sweater",
        "A single-colour overshirt or coach jacket",
        "One quiet accessory, max",
    ),
    stylingTips = listOf(
        "Stick to a tonal palette — black, white, grey, beige",
        "Let silhouette do the talking, not colour",
        "Invest in one genuinely great pair of trousers",
        "Steam or iron — wrinkles show more on plain fabric",
    ),
    keepInMind = listOf(
        "Avoid busy prints and logos",
        "Don't mix more than two neutral tones in one outfit",
        "Fit is everything when there's no pattern to distract",
        "Keep shoes clean — scuffs stand out more here",
    ),
    pairings = listOf(
        Pairing("Black tee", listOf("Black, grey or stone trousers", "Silver watch", "White sneakers"), "Colourful logos"),
        Pairing("Stone trousers", listOf("White, black or navy tops", "White sneakers"), "Busy patterns"),
        Pairing("White sneakers", listOf("Almost everything in this guide"), "Scuffs and grey laces"),
    ),
    outfits = outfits("minimal") {
        outfit(
            "All black, done right", listOf("Party", "Date", "Weekend"),
            why = "Head-to-toe black looks deliberate when every piece fits well and the blacks match. Silver is the only accent it needs.",
            dressUp = "Add a black blazer and swap to leather derbies.",
            dressDown = "Switch to black jeans.",
            piece("Black tee", TEE, P.Black), piece("Black trousers", TROUSERS, P.Black),
            piece("Black leather sneakers", SNEAKERS, P.Black), piece("Silver watch", WATCH, P.Silver),
        )
        outfit(
            "Tonal greys", listOf("Office", "Travel"),
            why = "Several shades of one colour is the minimalist's favourite trick — it looks considered and makes you look taller.",
            dressUp = "Add a charcoal overcoat.",
            dressDown = "Swap the knit for a grey hoodie.",
            piece("Grey knit", KNIT, P.Grey), piece("Charcoal trousers", TROUSERS, P.Charcoal),
            piece("White sneakers", SNEAKERS, P.White),
        )
        outfit(
            "White on stone", listOf("Weekend", "Travel", "College"),
            why = "White and stone are both light and warm. Easy in heat, and it photographs well.",
            dressUp = "Swap the tee for a white linen shirt.",
            dressDown = "Try stone shorts in summer.",
            piece("White tee", TEE, P.White), piece("Stone trousers", TROUSERS, P.Stone),
            piece("White sneakers", SNEAKERS, P.White), piece("Black sunglasses", SUNGLASSES, P.Black),
        )
        outfit(
            "Clean layers", listOf("College", "Weekend"),
            why = "A coach jacket adds shape over a plain tee without adding noise. Grey trousers soften the black.",
            dressUp = "Swap the jacket for a black overshirt.",
            dressDown = "Wear it with black joggers.",
            piece("Black tee", TEE, P.Black), piece("Charcoal coach jacket", JACKET, P.Charcoal),
            piece("Grey trousers", TROUSERS, P.Grey), piece("Black boots", BOOTS, P.Black),
        )
        outfit(
            "Minimal office", listOf("Office", "Interview"),
            why = "A crisp white shirt and black trousers is the cleanest office look there is. One silver watch is plenty.",
            dressUp = "Add a black blazer.",
            dressDown = "Swap derbies for black sneakers.",
            piece("White shirt", SHIRT, P.White), piece("Black trousers", TROUSERS, P.Black),
            piece("Black derbies", DERBIES, P.Black), piece("Silver watch", WATCH, P.Silver),
        )
    },
)

internal val streetwear = StyleGuide(
    key = "streetwear",
    title = "Streetwear",
    tagline = "Bold silhouettes, graphic layers. Confidence through contrast.",
    blurb = "Bold layers and sneakers",
    tint = GreenTint, accent = Green, heroText = Color(0xFF2B6B4C),
    palette = listOf(P.Black, P.Olive, P.White, P.Grey, P.Rust),
    goodFor = listOf("College", "Hanging out", "Concerts", "Weekends"),
    keyPieces = listOf(
        "An oversized hoodie or graphic tee",
        "Relaxed cargo pants or straight denim",
        "Statement sneakers",
        "A bomber or varsity jacket",
        "A cap or beanie",
        "A crossbody bag",
    ),
    stylingTips = listOf(
        "Let one bold sneaker colour anchor the fit",
        "Balance oversized tops with straight or tapered bottoms",
        "Layer a plain tee under an open shirt or jacket",
        "Mix high and low — one hype piece, the rest basics",
    ),
    keepInMind = listOf(
        "Don't over-layer in hot weather",
        "Keep one statement piece, not five",
        "Avoid clashing graphics",
        "Oversized isn't the same as shapeless — shoulders should still sit right",
    ),
    pairings = listOf(
        Pairing("Oversized hoodie", listOf("Cargos", "Tapered joggers", "Chunky sneakers"), "Baggy trousers that pool at the ankle"),
        Pairing("Cargo pants", listOf("Plain or graphic tees", "Hoodies", "White sneakers"), "Formal shirts and loafers"),
        Pairing("Cap", listOf("Hoodies", "Tees", "Bombers"), "Blazers"),
    ),
    outfits = outfits("streetwear") {
        outfit(
            "Hoodie and cargos", listOf("College", "Weekend"),
            why = "Grey, olive and black is a foolproof street palette. The oversized hoodie works because the cargos keep a clean line.",
            dressUp = "Swap the hoodie for an olive bomber over a white tee.",
            dressDown = "Change cargos for black joggers.",
            piece("Oversized hoodie", HOODIE, P.Grey), piece("Cargo pants", TROUSERS, P.Olive),
            piece("White sneakers", SNEAKERS, P.White), piece("Black cap", CAP, P.Black),
        )
        outfit(
            "Graphic tee and denim", listOf("College", "Party"),
            why = "One graphic is your statement; mid-wash denim and white sneakers stay quiet around it.",
            dressUp = "Layer a black bomber on top.",
            dressDown = "Swap jeans for black shorts.",
            piece("Black graphic tee", TEE, P.Black), piece("Relaxed jeans", JEANS, P.Denim),
            piece("White sneakers", SNEAKERS, P.White), piece("Silver chain", CHAIN, P.Silver),
        )
        outfit(
            "Bomber season", listOf("Weekend", "Party", "Travel"),
            why = "An olive bomber is the most wearable street jacket — military colour, clean shape, goes with black and white.",
            dressUp = "Swap the tee for a black knit.",
            dressDown = "Wear it with grey joggers.",
            piece("White tee", TEE, P.White), piece("Olive bomber", JACKET, P.Olive),
            piece("Black jeans", JEANS, P.Black), piece("Black sneakers", SNEAKERS, P.Black),
        )
        outfit(
            "Varsity energy", listOf("College", "Weekend"),
            why = "A varsity jacket brings one strong colour; everything else stays plain so it doesn't look like a costume.",
            dressUp = "Wear it over a white Oxford shirt.",
            dressDown = "Pair with grey joggers and a cap.",
            piece("White tee", TEE, P.White), piece("Burgundy varsity jacket", JACKET, P.Burgundy),
            piece("Dark jeans", JEANS, P.DarkDenim), piece("White sneakers", SNEAKERS, P.White), piece("Burgundy cap", CAP, P.Burgundy),
        )
        outfit(
            "Summer shorts fit", listOf("Weekend", "Travel"),
            why = "A rust tee warms up black shorts, and a crossbody keeps your hands free for travel days.",
            dressUp = "Swap shorts for black cargos in the evening.",
            dressDown = "Add slides instead of sneakers at the beach.",
            piece("Rust tee", TEE, P.Rust), piece("Black shorts", SHORTS, P.Black),
            piece("White sneakers", SNEAKERS, P.White), piece("Black sunglasses", SUNGLASSES, P.Black), piece("Crossbody bag", BAG, P.Black),
        )
    },
)
