package com.glowup.app.ui.screens.sleep

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.DeleteOutline
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.glowup.app.R
import com.glowup.app.ui.data.DebtLevel
import com.glowup.app.ui.data.SleepEntry
import com.glowup.app.ui.data.SleepLogStore
import com.glowup.app.ui.data.SleepMath
import com.glowup.app.ui.data.formatClock
import com.glowup.app.ui.data.formatDuration
import com.glowup.app.ui.design.LineHeight
import com.glowup.app.ui.design.Shapes
import com.glowup.app.ui.design.Space
import com.glowup.app.ui.design.TextSize
import com.glowup.app.ui.screens.mood.moodScoreColors
import com.glowup.app.ui.theme.*
import java.time.LocalDate
import java.time.format.TextStyle
import java.util.Locale

@Composable
internal fun LastNightCard(entry: SleepEntry, onEdit: () -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth().clip(Shapes.Hero).background(PurpleTint).clickable(onClick = onEdit).padding(Space.Xl, Space.L),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                stringResource(R.string.common_log_last_night),
                color = PurpleText,
                fontSize = TextSize.Micro,
                fontWeight = FontWeight.SemiBold,
                letterSpacing = 0.4.sp
            )
            Text(formatDuration(entry.minutes), fontSize = TextSize.Display, fontWeight = FontWeight.Bold)
            Text(
                stringResource(R.string.sleep_log_1_s_2_s_felt, formatClock(entry.bed), formatClock(entry.wake), qualityLabels[entry.quality - 1].lowercase()),
                color = PurpleDeep,
                fontSize = TextSize.BodySmall
            )
        }
        Text(
            stringResource(R.string.sleep_log_edit),
            fontSize = TextSize.BodySmall,
            fontWeight = FontWeight.SemiBold,
            modifier = Modifier.clip(Shapes.Pill).background(Color.White).padding(Space.M, Space.S)
        )
    }
}

@Composable
internal fun DebtCard(today: LocalDate) {
    val debt = SleepLogStore.debtMinutes(today)
    val nights = SleepLogStore.recent(today).size
    if (debt == null) {
        Column(modifier = Modifier.fillMaxWidth().clip(Shapes.Card).background(CardNeutral).padding(Space.L, Space.L)) {
            Text(stringResource(R.string.sleep_log_sleep_debt), fontSize = TextSize.BodyLarge, fontWeight = FontWeight.Bold)
            Spacer(Modifier.height(Space.Xs))
            val more = SleepMath.MIN_NIGHTS_FOR_DEBT - nights
            Text(
                "Log $more more night${if (more == 1) "" else "s"} to see how much sleep you owe yourself. It takes 10 seconds each morning.",
                color = TextSecondary,
                fontSize = TextSize.BodySmall,
                lineHeight = LineHeight.Compact
            )
        }
        return
    }
    val level = SleepMath.level(debt)
    val (bg, accent) = when (level) {
        DebtLevel.LOW -> GreenTint to Green
        DebtLevel.MODERATE -> YellowTint to Color(0xFFB08A00)
        DebtLevel.HIGH -> CoralTint to Coral
    }
    Column(modifier = Modifier.fillMaxWidth().clip(Shapes.Card).background(bg).padding(Space.L, Space.L)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    stringResource(R.string.sleep_log_sleep_debt_2),
                    color = accent,
                    fontSize = TextSize.Micro,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 0.4.sp
                )
                Text(
                    if (debt ==
                        0
                    ) {
                        stringResource(R.string.sleep_log_none)
                    } else {
                        formatDuration(debt)
                    },
                    fontSize = TextSize.Display,
                    fontWeight = FontWeight.Bold
                )
            }
            Text(
                level.label,
                color = Color.White,
                fontSize = TextSize.Caption,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.clip(Shapes.Pill).background(accent).padding(Space.M, Space.S)
            )
        }
        Spacer(Modifier.height(Space.S))
        Text(
            when (level) {
                DebtLevel.LOW -> stringResource(R.string.sleep_log_you_re_well_rested_keep)
                DebtLevel.MODERATE -> stringResource(R.string.sleep_log_pay_it_back_gently_go)
                DebtLevel.HIGH -> stringResource(R.string.sleep_log_you_re_running_on_empty)
            },
            color = Ink.copy(alpha = 0.8f),
            fontSize = TextSize.BodySmall,
            lineHeight = LineHeight.Compact
        )
        Spacer(Modifier.height(Space.S))
        Text(
            "Estimated from $nights logged night${if (nights == 1) "" else "s"} in the last 2 weeks. Not a medical measurement.",
            color = TextSecondary,
            fontSize = TextSize.Micro
        )
    }
}

@Composable
internal fun StatsRow(today: LocalDate) {
    val week = SleepLogStore.recent(today, 7)
    val average = SleepMath.averageMinutes(week)
    val consistency = SleepLogStore.consistency(today)
    Row(horizontalArrangement = Arrangement.spacedBy(Space.S)) {
        StatTile(
            stringResource(R.string.sleep_log_7_night_average),
            average?.let(::formatDuration) ?: "—",
            if (average == null) "Log a night to start" else "${week.size} night${if (week.size == 1) "" else "s"} logged",
            Modifier.weight(1f)
        )
        StatTile(
            stringResource(R.string.sleep_log_consistency),
            consistency?.let { "$it" } ?: "—",
            consistency?.let(SleepMath::consistencyLabel) ?: "Needs ${SleepMath.MIN_NIGHTS_FOR_CONSISTENCY} nights",
            Modifier.weight(1f)
        )
    }
}

@Composable
internal fun StatTile(label: String, value: String, sub: String, modifier: Modifier) {
    Column(modifier = modifier.clip(Shapes.Card).background(CardNeutral).padding(Space.L, Space.M)) {
        Text(label, color = TextSecondary, fontSize = TextSize.Caption, fontWeight = FontWeight.Medium)
        Text(value, fontSize = TextSize.TitleLarge, fontWeight = FontWeight.Bold, modifier = Modifier.padding(vertical = 2.dp))
        Text(sub, color = TextSecondary, fontSize = TextSize.Caption)
    }
}

@Composable
internal fun NightsChart(today: LocalDate) {
    val need = SleepLogStore.needMinutes
    val days = (13 downTo 0).map { today.minusDays(it.toLong()) }
    val maxMinutes = maxOf(need + 120, days.maxOfOrNull { SleepLogStore.on(it)?.minutes ?: 0 } ?: 0)
    Column(modifier = Modifier.fillMaxWidth().clip(Shapes.Card).background(CardNeutral).padding(Space.M, Space.L)) {
        Canvas(modifier = Modifier.fillMaxWidth().height(130.dp)) {
            val gap = 5.dp.toPx()
            val barW = (size.width - gap * 13) / 14
            days.forEachIndexed { i, day ->
                val x = i * (barW + gap)
                val entry = SleepLogStore.on(day)
                if (entry == null) {
                    drawCircle(Color(0xFFDAD7D0), radius = 2.5.dp.toPx(), center = Offset(x + barW / 2, size.height - 3.dp.toPx()))
                } else {
                    val h = (entry.minutes / maxMinutes.toFloat()) * size.height
                    val color = when {
                        entry.minutes >= need -> Green
                        entry.minutes >= need - 60 -> Color(0xFFFFC94D)
                        else -> Coral
                    }
                    drawRoundRect(color, Offset(x, size.height - h), Size(barW, h), CornerRadius(4.dp.toPx()))
                }
            }
            val needY = size.height - need / maxMinutes.toFloat() * size.height
            drawLine(
                Ink.copy(alpha = 0.45f),
                Offset(0f, needY),
                Offset(size.width, needY),
                strokeWidth = 1.5.dp.toPx(),
                pathEffect = PathEffect.dashPathEffect(floatArrayOf(8f, 8f))
            )
        }
        Spacer(Modifier.height(Space.S))
        Row {
            days.forEach { day ->
                Text(
                    day.dayOfWeek.getDisplayName(TextStyle.NARROW, Locale.getDefault()),
                    color = if (day == today) Ink else TextSecondary,
                    fontSize = TextSize.Micro,
                    fontWeight = if (day == today) FontWeight.Bold else FontWeight.Medium,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

@Composable
internal fun HistoryRow(entry: SleepEntry, onClick: () -> Unit, onDelete: () -> Unit) {
    val today = LocalDate.now()
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = Space.S)
            .clip(Shapes.Medium)
            .background(CardNeutral)
            .clickable(onClick = onClick)
            .padding(Space.L, Space.M),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(Modifier.size(10.dp).clip(CircleShape).background(moodScoreColors[entry.quality - 1]))
        Spacer(Modifier.width(Space.M))
        Column(modifier = Modifier.weight(1f)) {
            Text(dayLabel(entry.date, today), fontSize = TextSize.Body, fontWeight = FontWeight.SemiBold)
            Text(
                "${formatClock(entry.bed)} – ${formatClock(entry.wake)} · ${qualityLabels[entry.quality - 1]}",
                color = TextSecondary,
                fontSize = TextSize.Caption
            )
        }
        Text(formatDuration(entry.minutes), fontSize = TextSize.Body, fontWeight = FontWeight.Bold)
        Box(Modifier.padding(start = Space.S).size(34.dp).clip(CircleShape).clickable(onClick = onDelete), contentAlignment = Alignment.Center) {
            Icon(
                Icons.Outlined.DeleteOutline,
                contentDescription = stringResource(R.string.sleep_log_delete_night),
                tint = TextSecondary,
                modifier = Modifier.size(18.dp)
            )
        }
    }
}
