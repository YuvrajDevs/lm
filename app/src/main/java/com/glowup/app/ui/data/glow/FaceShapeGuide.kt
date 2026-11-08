package com.glowup.app.ui.data.glow

data class StyleTip(val name: String, val why: String)

data class ShapeGuide(
    val shape: String,
    val summary: String,
    val haircuts: List<StyleTip>,
    val barberScript: String,
    val avoid: List<String>,
    val beards: List<StyleTip>,
    val glasses: String,
)

/** Haircut, beard and glasses advice for each face shape. Shape is a guide, not a rule. */
object FaceShapeGuide {
    val all: List<ShapeGuide> = listOf(
        ShapeGuide(
            "Oval",
            "Balanced proportions — slightly longer than wide, with a jaw a little narrower than your forehead. Almost everything suits you, so choose for your hair type and lifestyle.",
            listOf(
                StyleTip("Textured crop", "Short, messy texture on top with a low fade. Low effort and sharp."),
                StyleTip("Classic side part", "Tapered sides and 5–8 cm on top. Smart enough for weddings and interviews."),
                StyleTip("Medium flow", "Longer, swept-back hair to the ears if you have thick or wavy hair."),
            ),
            "\"Low taper on the sides, keep about 5 cm on top so I can push it to the side, and add some texture.\"",
            listOf("A heavy fringe that covers your forehead and hides your proportions"),
            listOf(
                StyleTip("Short boxed beard", "Clean cheek and neck lines keep things sharp."),
                StyleTip("Heavy stubble", "3–5 mm all over — the easiest upgrade for most oval faces."),
            ),
            "Most frames work. Try rectangular or classic wayfarer shapes that are as wide as your face.",
        ),
        ShapeGuide(
            "Round",
            "Width and length are similar, with soft cheeks and a rounded jaw. The goal is to add height and angles so your face reads longer and sharper.",
            listOf(
                StyleTip("Quiff or pompadour", "Volume on top with short sides adds length."),
                StyleTip("Faux hawk", "A subtle ridge of height through the middle creates angles."),
                StyleTip("Angular fringe", "A textured fringe cut at an angle breaks up the roundness."),
            ),
            "\"Skin or low fade on the sides, keep length on top — around 7 cm — so I can style it up with some height.\"",
            listOf("Round bowl cuts and blunt fringes", "Long, full sides that add width", "Buzz cuts with no fade (they emphasise roundness)"),
            listOf(
                StyleTip("Short beard, longer at the chin", "Adds length and defines a jawline."),
                StyleTip("Goatee or extended goatee", "Draws the eye down and lengthens the face."),
            ),
            "Angular, rectangular or square frames add structure. Avoid small round frames.",
        ),
        ShapeGuide(
            "Square",
            "A strong, wide jaw with a forehead and cheekbones of similar width. Your jaw is your best feature — keep the hair clean and structured.",
            listOf(
                StyleTip("Crew cut", "Short and masculine; shows off the jaw."),
                StyleTip("Side part with taper", "Classic and sharp for formal looks."),
                StyleTip("Textured slick back", "Medium length on top, swept back, softens the angles a little."),
            ),
            "\"Short taper on the sides, about 4 cm on top, side part please.\"",
            listOf("Very boxy, flat-top cuts that make the head look square too"),
            listOf(
                StyleTip("Light stubble", "Lets the jawline do the work."),
                StyleTip("Circle beard", "A rounded moustache and chin beard softens very strong angles."),
            ),
            "Round or oval frames soften strong angles. Thin metal frames look great.",
        ),
        ShapeGuide(
            "Heart",
            "A wider forehead and cheekbones narrowing to a pointed chin. Balance the top and add some width lower down.",
            listOf(
                StyleTip("Medium-length side swept", "Covers some forehead width."),
                StyleTip("Textured fringe", "A soft fringe makes the forehead look narrower."),
                StyleTip("Longer, tousled top with fuller sides", "Adds width around the ears to balance the chin."),
            ),
            "\"Leave a bit more length on the sides — scissor cut, not a skin fade — and keep a soft fringe I can push to the side.\"",
            listOf("Very short sides with tall volume on top", "Slicked-back styles that expose the whole forehead"),
            listOf(
                StyleTip("Full beard or chin strap", "Adds width at the jaw to balance the forehead."),
                StyleTip("Short boxed beard", "Fills out a narrow chin."),
            ),
            "Bottom-heavy or rimless frames, and oval shapes. Avoid heavy top-line frames.",
        ),
        ShapeGuide(
            "Long",
            "Noticeably longer than wide, often with a straight cheek line. Aim to add width and avoid extra height.",
            listOf(
                StyleTip("Fringe or brushed-forward crop", "Shortens the face visually."),
                StyleTip("Side part with fuller sides", "Medium length on the sides adds width."),
                StyleTip("Messy medium length", "Width around the ears balances length."),
            ),
            "\"Not too short on the sides — number 3 or scissors — and keep the top medium, brushed forward. No extra height.\"",
            listOf("Tall quiffs and pompadours", "Very short sides with a skin fade"),
            listOf(
                StyleTip("Full beard kept short at the chin", "Adds width to the sides of the face."),
                StyleTip("Mutton-chop style sides", "Fuller cheeks, short chin — widens the face."),
            ),
            "Deep, oversized or bold frames break up the length. Avoid narrow frames.",
        ),
        ShapeGuide(
            "Diamond",
            "Wide cheekbones with a narrower forehead and jaw. Soften the cheekbones and add width at the forehead and chin.",
            listOf(
                StyleTip("Textured fringe", "Adds width at the forehead."),
                StyleTip("Fuller sides, longer top", "Balances standout cheekbones."),
                StyleTip("Messy medium length", "Covers the temples and softens angles."),
            ),
            "\"Keep the sides a bit fuller — no skin fade — and leave a textured fringe on top.\"",
            listOf("Very short sides that make the cheekbones look wider"),
            listOf(
                StyleTip("Full beard", "Adds width to a narrower jaw."),
                StyleTip("Chin-focused beard", "Balances the cheekbones."),
            ),
            "Oval or rimless frames, or frames with a strong browline.",
        ),
    )

    fun forShape(shape: String?): ShapeGuide? = all.firstOrNull { it.shape.equals(shape, ignoreCase = true) }

    /** Extra beard nuance from proportions — a fixed trait, only used for advice. */
    fun beardNote(lowerFaceRatio: Double?): String? = when {
        lowerFaceRatio == null -> null
        lowerFaceRatio > 1.35 -> "Your lower face is on the longer side, so keep beard length short at the chin and a little fuller at the sides."
        lowerFaceRatio < 1.0 -> "Your lower face is on the shorter side, so a bit of length at the chin (goatee or short boxed beard) adds nice balance."
        else -> null
    }
}
