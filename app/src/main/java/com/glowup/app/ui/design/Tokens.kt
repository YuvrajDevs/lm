package com.glowup.app.ui.design

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/**
 * Re:MakeU's type scale. Every Text uses one of these sizes so hierarchy stays consistent
 * across screens. Pair each with the matching [LineHeight] for multi-line text.
 */
object TextSize {
    val Micro = 11.sp // overlines, badges, chart labels
    val Caption = 12.sp // secondary metadata
    val BodySmall = 13.sp // supporting copy
    val Body = 14.sp // default body text
    val BodyLarge = 15.sp // row titles, buttons
    val Title = 17.sp // section titles, card titles
    val TitleLarge = 20.sp // hero card titles
    val Headline = 24.sp // screen headers
    val Display = 28.sp // step titles, big stats
    val DisplayLarge = 34.sp // hero numbers
    val Number = 48.sp // countdowns
    val NumberLarge = 56.sp
    val Jumbo = 72.sp
    val Countdown = 96.sp
}

object LineHeight {
    val Tight = 16.sp
    val Compact = 18.sp
    val Body = 20.sp
    val Relaxed = 22.sp
    val Title = 26.sp
    val Display = 32.sp
}

/** Corner radii. Cards use [Card]; hero blocks use [Hero]; buttons and chips use [Pill]. */
object Radius {
    val Small = 12.dp
    val Medium = 16.dp
    val Card = 20.dp
    val Large = 24.dp
    val Hero = 28.dp
}

object Shapes {
    val Small = RoundedCornerShape(Radius.Small)
    val Medium = RoundedCornerShape(Radius.Medium)
    val Card = RoundedCornerShape(Radius.Card)
    val Large = RoundedCornerShape(Radius.Large)
    val Hero = RoundedCornerShape(Radius.Hero)
    val Pill = RoundedCornerShape(percent = 50)
}

/**
 * Spacing on a 4dp grid; screen edges use [Gutter]. Every padding, gap and spacer uses one of
 * these — 0–3dp hairlines are the only literals allowed.
 */
object Space {
    val Xs = 4.dp
    val S = 8.dp
    val M = 12.dp
    val L = 16.dp
    val Xl = 20.dp
    val Gutter = 24.dp
    val Section = 28.dp
    val Xxl = 32.dp
    val Huge = 40.dp
}

object Sizes {
    val Button = 56.dp
    val ButtonSmall = 48.dp
    val IconButton = 48.dp

    /** Minimum touch target, per Material accessibility guidance. */
    val Touch = 48.dp
}
