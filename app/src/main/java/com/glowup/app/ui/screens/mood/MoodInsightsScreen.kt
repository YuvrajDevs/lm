package com.glowup.app.ui.screens.mood

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.TrendingDown
import androidx.compose.material.icons.automirrored.filled.TrendingUp
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import com.glowup.app.R
import com.glowup.app.ui.components.DetailHeader
import com.glowup.app.ui.components.ProBadge
import com.glowup.app.ui.components.goBack
import com.glowup.app.ui.data.MoodInsight
import com.glowup.app.ui.data.MoodInsights
import com.glowup.app.ui.data.MoodStore
import com.glowup.app.ui.data.ProGate
import com.glowup.app.ui.data.ProStore
import com.glowup.app.ui.data.moodOptions
import com.glowup.app.ui.design.LineHeight
import com.glowup.app.ui.design.SectionHeader
import com.glowup.app.ui.design.Shapes
import com.glowup.app.ui.design.Space
import com.glowup.app.ui.design.TextSize
import com.glowup.app.ui.theme.*
import java.time.LocalDate
import java.time.Month
import java.time.YearMonth
import java.time.format.TextStyle
import java.util.Locale
import kotlin.math.abs
import kotlin.math.roundToInt

/** 1 (low) → 5 (great). Shared by the pixels grid and its legend. */
val moodScoreColors = listOf(Maroon, Coral, Sun, Color(0xFF8FD6A8), Green)
private val moodScoreNames = listOf("Rough", "Low", "Okay", "Good", "Great")

@Composable
fun MoodInsightsScreen(navController: NavHostController) {
    val today = LocalDate.now()
    // Reading the stores here keeps the screen live as check-ins change.
    val logged = MoodStore.moodLog.size + MoodStore.tags.size
    val insights = remember(logged) { MoodInsights.current() }
    val bestTags = remember(logged) { MoodInsights.bestDayTags() }
    val todayMood = MoodStore.moodOn(today)

    Box(modifier = Modifier.fillMaxSize().background(Surface).systemBarsPadding()) {
        Column(modifier = Modifier.fillMaxSize()) {
            DetailHeader(stringResource(R.string.common_mood)) { navController.goBack() }

            Column(modifier = Modifier.weight(1f).verticalScroll(rememberScrollState()).padding(Space.Gutter, Space.L, Space.Gutter, Space.Section)) {
                MonthSummary(today, todayMood) { navController.navigate("mood_checkin") }

                Spacer(Modifier.height(Space.Gutter))
                SectionTitle("What moves your mood", "Compared across your own check-ins — nothing leaves your phone")
                Spacer(Modifier.height(Space.M))
                InsightsSection(insights)

                if (bestTags.isNotEmpty()) {
                    Spacer(Modifier.height(Space.Gutter))
                    SectionTitle("Your best days usually include", "From days you felt happy or grateful")
                    Spacer(Modifier.height(Space.M))
                    Row(horizontalArrangement = Arrangement.spacedBy(Space.S)) {
                        bestTags.forEach { (tag, count) ->
                            Column(
                                modifier = Modifier.weight(1f).clip(Shapes.Medium).background(GreenTint).padding(Space.M, Space.M)
                            ) {
                                Text(tag, fontSize = TextSize.Body, fontWeight = FontWeight.Bold, maxLines = 1)
                                Text("$count great day${if (count == 1) "" else "s"}", color = GreenDeep, fontSize = TextSize.Caption)
                            }
                        }
                        repeat(3 - bestTags.size) { Spacer(Modifier.weight(1f)) }
                    }
                }

                Spacer(Modifier.height(Space.Gutter))
                SectionTitle("${today.year} in pixels", "Every check-in, one square")
                Spacer(Modifier.height(Space.M))
                YearInPixels(today)

                Spacer(Modifier.height(Space.Gutter))
                SectionTitle("Last 30 days", "How often you felt each mood")
                Spacer(Modifier.height(Space.M))
                MoodBreakdown(today)
            }
        }
    }
}

@Composable
private fun SectionTitle(title: String, subtitle: String) {
    SectionHeader(title, subtitle = subtitle)
}

@Composable
private fun MonthSummary(today: LocalDate, todayMood: String?, onCheckIn: () -> Unit) {
    val month = YearMonth.from(today)
    val thisMonth = MoodInsights.averageScore(month.atDay(1), today)
    val lastMonth = month.minusMonths(1).let { MoodInsights.averageScore(it.atDay(1), it.atEndOfMonth()) }
    val count = MoodStore.moodLog.keys.count { YearMonth.from(LocalDate.parse(it)) == month }
    Column(modifier = Modifier.fillMaxWidth().clip(Shapes.Large).background(Ink).padding(Space.Xl, Space.L)) {
        Text(
            month.month.getDisplayName(TextStyle.FULL, Locale.getDefault()).uppercase() + " SO FAR",
            color = Color.White.copy(alpha = 0.55f),
            fontSize = TextSize.Micro,
            fontWeight = FontWeight.SemiBold,
            letterSpacing = 0.4.sp
        )
        Spacer(Modifier.height(Space.S))
        Row(verticalAlignment = Alignment.CenterVertically) {
            if (thisMonth != null) {
                val idx = (thisMonth.roundToInt() - 1).coerceIn(0, 4)
                Box(Modifier.size(14.dp).clip(CircleShape).background(moodScoreColors[idx]))
                Spacer(Modifier.width(Space.S))
                Text(
                    stringResource(R.string.mood_insights_mostly_1_s, moodScoreNames[idx].lowercase()),
                    color = Color.White,
                    fontSize = TextSize.TitleLarge,
                    fontWeight = FontWeight.Bold
                )
            } else {
                Text(stringResource(R.string.mood_insights_no_check_ins_yet), color = Color.White, fontSize = TextSize.TitleLarge, fontWeight = FontWeight.Bold)
            }
        }
        Spacer(Modifier.height(Space.Xs))
        val trend = if (thisMonth != null && lastMonth != null) {
            val delta = thisMonth - lastMonth
            when {
                delta >= 0.3 -> " · better than last month"
                delta <= -0.3 -> " · a little lower than last month"
                else -> " · steady with last month"
            }
        } else {
            ""
        }
        Text(
            (if (thisMonth != null) stringResource(R.string.mood_insights_average_1f_of_5).format(thisMonth) + " · " else "") +
                "$count check-in${if (count == 1) "" else "s"}$trend",
            color = Color.White.copy(alpha = 0.65f),
            fontSize = TextSize.BodySmall
        )
        Spacer(Modifier.height(Space.M))
        Row(
            modifier = Modifier.clip(Shapes.Pill).background(Color.White).clickable(onClick = onCheckIn).padding(Space.L, Space.S),
            verticalAlignment = Alignment.CenterVertically
        ) {
            todayMood?.let { MoodStore.iconFor(it) }?.let {
                Image(painterResource(it), contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(Modifier.width(Space.S))
            }
            Text(
                if (todayMood ==
                    null
                ) {
                    stringResource(R.string.mood_insights_check_in_for_today)
                } else {
                    stringResource(R.string.mood_insights_today_1_s_edit, todayMood)
                },
                color = Ink,
                fontSize = TextSize.BodySmall,
                fontWeight = FontWeight.SemiBold
            )
        }
    }
}

@Composable
private fun InsightsSection(insights: List<MoodInsight>) {
    val needed = MoodInsights.checkInsUntilInsights()
    when {
        needed > 0 -> {
            val have = MoodStore.checkInCount
            val total = have + needed
            Column(modifier = Modifier.fillMaxWidth().clip(Shapes.Card).background(PurpleTint).padding(Space.L, Space.L)) {
                Text(
                    "$needed more check-in${if (needed == 1) "" else "s"} to unlock your patterns",
                    fontSize = TextSize.BodyLarge,
                    fontWeight = FontWeight.Bold
                )
                Spacer(Modifier.height(Space.S))
                Text(
                    stringResource(R.string.mood_insights_re_makeu_compares_your_mood),
                    color = PurpleDeep,
                    fontSize = TextSize.BodySmall,
                    lineHeight = LineHeight.Compact
                )
                Spacer(Modifier.height(Space.M))
                Box(Modifier.fillMaxWidth().height(8.dp).clip(Shapes.Pill).background(Color.White)) {
                    Box(Modifier.fillMaxWidth(have / total.toFloat()).fillMaxHeight().clip(Shapes.Pill).background(Purple))
                }
                Text(
                    stringResource(R.string.mood_insights_1_s_of_2_s, have, total),
                    color = PurpleDeep,
                    fontSize = TextSize.Caption,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.padding(top = Space.S)
                )
            }
        }
        insights.isEmpty() -> {
            Column(modifier = Modifier.fillMaxWidth().clip(Shapes.Card).background(CardNeutral).padding(Space.L, Space.L)) {
                Text(stringResource(R.string.mood_insights_no_clear_patterns_yet), fontSize = TextSize.BodyLarge, fontWeight = FontWeight.Bold)
                Spacer(Modifier.height(Space.S))
                Text(
                    stringResource(R.string.mood_insights_nothing_makes_a_big_enough),
                    color = TextSecondary,
                    fontSize = TextSize.BodySmall,
                    lineHeight = LineHeight.Compact
                )
            }
        }
        else -> {
            val free = if (ProStore.isPro) insights.size else ProGate.FREE_MOOD_INSIGHTS
            Column(verticalArrangement = Arrangement.spacedBy(Space.S)) {
                insights.take(free).forEach { InsightCard(it) }
                val hidden = insights.size - free
                if (hidden > 0) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(Shapes.Card)
                            .background(Yellow)
                            .clickable { ProStore.showPaywall("See every pattern behind your mood") }
                            .padding(Space.L, Space.L),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Filled.Lock, contentDescription = null, tint = Ink, modifier = Modifier.size(18.dp))
                        Spacer(Modifier.width(Space.M))
                        Column(modifier = Modifier.weight(1f)) {
                            Text("$hidden more pattern${if (hidden == 1) "" else "s"} found", fontSize = TextSize.BodyLarge, fontWeight = FontWeight.Bold)
                            Text(stringResource(R.string.mood_insights_unlock_every_pattern_with_pro), color = Amber, fontSize = TextSize.BodySmall)
                        }
                        ProBadge()
                    }
                }
            }
        }
    }
}

@Composable
private fun InsightCard(insight: MoodInsight) {
    val accent = if (insight.lifts) Green else Coral
    val tint = if (insight.lifts) GreenTint else CoralTint
    Column(modifier = Modifier.fillMaxWidth().clip(Shapes.Card).background(tint).padding(Space.L, Space.L)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(Modifier.size(30.dp).clip(CircleShape).background(Color.White), contentAlignment = Alignment.Center) {
                Icon(
                    if (insight.lifts) Icons.AutoMirrored.Filled.TrendingUp else Icons.AutoMirrored.Filled.TrendingDown,
                    contentDescription = null,
                    tint = accent,
                    modifier = Modifier.size(17.dp)
                )
            }
            Spacer(Modifier.width(Space.S))
            Text(
                if (insight.lifts) stringResource(R.string.mood_insights_lifts_your_mood) else stringResource(R.string.mood_insights_weighs_on_your_mood),
                color = accent,
                fontSize = TextSize.Micro,
                fontWeight = FontWeight.Bold,
                letterSpacing = 0.4.sp
            )
        }
        Spacer(Modifier.height(Space.S))
        Text(
            "On days ${insight.phrase}, your mood is ${"%.1f".format(abs(insight.difference))} points ${if (insight.lifts) "higher" else "lower"}.",
            fontSize = TextSize.BodyLarge,
            fontWeight = FontWeight.SemiBold,
            lineHeight = LineHeight.Relaxed
        )
        Spacer(Modifier.height(Space.M))
        CompareBar("With", insight.withAvg, accent)
        Spacer(Modifier.height(Space.S))
        CompareBar("Without", insight.withoutAvg, TextMuted)
        Spacer(Modifier.height(Space.S))
        Text(
            stringResource(R.string.mood_insights_based_on_1_s_days, insight.withDays, insight.withoutDays),
            color = TextSecondary,
            fontSize = TextSize.Caption
        )
    }
}

@Composable
private fun CompareBar(label: String, value: Double, color: Color) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Text(label, color = TextSecondary, fontSize = TextSize.Caption, fontWeight = FontWeight.Medium, modifier = Modifier.width(52.dp))
        Box(Modifier.weight(1f).height(10.dp).clip(Shapes.Pill).background(Color.White)) {
            Box(Modifier.fillMaxWidth((value / 5.0).toFloat().coerceIn(0.04f, 1f)).fillMaxHeight().clip(Shapes.Pill).background(color))
        }
        Text("%.1f".format(value), fontSize = TextSize.Caption, fontWeight = FontWeight.Bold, textAlign = TextAlign.End, modifier = Modifier.width(34.dp))
    }
}

@Composable
private fun YearInPixels(today: LocalDate) {
    val year = today.year
    Column(modifier = Modifier.fillMaxWidth().clip(Shapes.Card).background(CardNeutral).padding(Space.M, Space.L)) {
        Row {
            Column(modifier = Modifier.padding(end = Space.S)) {
                Month.entries.forEach { month ->
                    Box(Modifier.height(11.dp).padding(bottom = 0.dp), contentAlignment = Alignment.CenterStart) {
                        Text(
                            month.getDisplayName(TextStyle.NARROW, Locale.getDefault()),
                            color = TextSecondary,
                            fontSize = 8.5.sp,
                            fontWeight = FontWeight.SemiBold,
                            lineHeight = LineHeight.Tight
                        )
                    }
                }
            }
            Canvas(modifier = Modifier.weight(1f).height(11.dp * 12)) {
                val gap = 2.dp.toPx()
                val cell = ((size.width - gap * 30) / 31f).coerceAtMost(11.dp.toPx() - gap)
                val rowH = 11.dp.toPx()
                val radius = CornerRadius(2.dp.toPx())
                Month.entries.forEachIndexed { row, month ->
                    val ym = YearMonth.of(year, month)
                    for (day in 1..31) {
                        val x = (day - 1) * (cell + gap)
                        val y = row * rowH + (rowH - cell) / 2
                        val color = if (day > ym.lengthOfMonth()) {
                            Color.Transparent
                        } else {
                            val date = ym.atDay(day)
                            val score = MoodStore.scoreOn(date)
                            when {
                                score != null -> moodScoreColors[score - 1]
                                date.isAfter(today) -> Color(0xFFEFEDE8)
                                else -> Color(0xFFE3E0D9)
                            }
                        }
                        drawRoundRect(color, Offset(x, y), Size(cell, cell), radius)
                    }
                }
            }
        }
        Spacer(Modifier.height(Space.M))
        Row(horizontalArrangement = Arrangement.spacedBy(Space.S), verticalAlignment = Alignment.CenterVertically) {
            moodScoreColors.forEachIndexed { i, color ->
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(Modifier.size(9.dp).clip(RoundedCornerShape(2.dp)).background(color))
                    Spacer(Modifier.width(Space.Xs))
                    Text(moodScoreNames[i], color = TextSecondary, fontSize = TextSize.Micro, fontWeight = FontWeight.Medium)
                }
            }
        }
    }
}

@Composable
private fun MoodBreakdown(today: LocalDate) {
    val from = today.minusDays(29)
    val counts = MoodStore.moodLog.filterKeys { !LocalDate.parse(it).isBefore(from) && !LocalDate.parse(it).isAfter(today) }
        .values.groupingBy { it }.eachCount()
    val max = counts.values.maxOrNull() ?: 0
    if (max == 0) {
        Text(stringResource(R.string.mood_insights_check_in_a_few_times), color = TextSecondary, fontSize = TextSize.BodySmall)
        return
    }
    Column(verticalArrangement = Arrangement.spacedBy(Space.S)) {
        moodOptions.sortedByDescending { counts[it.label] ?: 0 }.filter { (counts[it.label] ?: 0) > 0 }.forEach { mood ->
            val n = counts[mood.label] ?: 0
            Row(verticalAlignment = Alignment.CenterVertically) {
                Image(painterResource(mood.icon), contentDescription = null, modifier = Modifier.size(26.dp))
                Spacer(Modifier.width(Space.S))
                Text(mood.label, fontSize = TextSize.BodySmall, fontWeight = FontWeight.SemiBold, modifier = Modifier.width(72.dp))
                Box(Modifier.weight(1f).height(12.dp).clip(Shapes.Pill).background(ChipNeutral)) {
                    Box(Modifier.fillMaxWidth(n / max.toFloat()).fillMaxHeight().clip(Shapes.Pill).background(moodScoreColors[mood.score - 1]))
                }
                Text("$n", fontSize = TextSize.BodySmall, fontWeight = FontWeight.Bold, textAlign = TextAlign.End, modifier = Modifier.width(28.dp))
            }
        }
    }
}
