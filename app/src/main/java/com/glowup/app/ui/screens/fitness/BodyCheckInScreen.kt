package com.glowup.app.ui.screens.fitness

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MonitorWeight
import androidx.compose.material.icons.outlined.DeleteOutline
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import com.glowup.app.R
import com.glowup.app.ui.components.ConsentSheet
import com.glowup.app.ui.components.DetailHeader
import com.glowup.app.ui.components.ReMakeUDialog
import com.glowup.app.ui.components.ReMakeUOptionRow
import com.glowup.app.ui.components.goBack
import com.glowup.app.ui.data.Consent
import com.glowup.app.ui.data.ConsentStore
import com.glowup.app.ui.data.ProfileStore
import com.glowup.app.ui.data.fitness.BodyEntry
import com.glowup.app.ui.data.fitness.FitnessStore
import com.glowup.app.ui.data.fitness.TrendTone
import com.glowup.app.ui.design.AppButton
import com.glowup.app.ui.design.AppSwitch
import com.glowup.app.ui.design.ButtonVariant
import com.glowup.app.ui.design.LineHeight
import com.glowup.app.ui.design.Shapes
import com.glowup.app.ui.design.Space
import com.glowup.app.ui.design.TextSize
import com.glowup.app.ui.theme.*
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.format.TextStyle
import java.util.Locale

@Composable
fun BodyCheckInScreen(navController: NavHostController) {
    val today = LocalDate.now()
    val latest = FitnessStore.latestBody()
    val todayEntry = FitnessStore.body[today.toString()]
    var weight by remember { mutableStateOf(todayEntry?.weightKg ?: latest?.weightKg ?: 70.0) }
    var waist by remember { mutableStateOf(todayEntry?.waistCm ?: latest?.waistCm) }
    var saved by remember { mutableStateOf(todayEntry != null) }
    var askConsent by remember { mutableStateOf(!ConsentStore.has(Consent.BODY)) }
    var showDay by remember { mutableStateOf(false) }
    var deleting by remember { mutableStateOf<BodyEntry?>(null) }
    val lb = FitnessStore.useLb
    val minor = ProfileStore.isMinor

    Box(Modifier.fillMaxSize().background(Surface).systemBarsPadding()) {
        Column(Modifier.fillMaxSize()) {
            DetailHeader(stringResource(R.string.body_check_in_weekly_check_in)) { navController.goBack() }
            Column(Modifier.weight(1f).verticalScroll(rememberScrollState()).padding(Space.Gutter, Space.L, Space.Gutter, Space.Section)) {
                Column(Modifier.fillMaxWidth().clip(Shapes.Hero).background(PurpleTint).padding(Space.Xl)) {
                    Text(
                        if (saved) stringResource(R.string.body_check_in_saved_for_today) else stringResource(R.string.body_check_in_weigh_in),
                        color = Purple,
                        fontSize = TextSize.Micro,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 0.4.sp
                    )
                    Text(stringResource(R.string.body_check_in_same_day_same_time_each), fontSize = TextSize.Title, fontWeight = FontWeight.Bold)
                    Text(
                        stringResource(R.string.body_check_in_morning_after_the_bathroom_before),
                        color = PurpleDeep,
                        fontSize = TextSize.BodySmall,
                        lineHeight = LineHeight.Compact,
                        modifier = Modifier.padding(top = 2.dp)
                    )
                    Spacer(Modifier.height(Space.L))
                    Row(Modifier.fillMaxWidth().clip(Shapes.Card).background(Color.White).padding(Space.M), verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            stringResource(R.string.body_check_in_weight),
                            fontSize = TextSize.Body,
                            fontWeight = FontWeight.SemiBold,
                            modifier = Modifier.weight(1f)
                        )
                        NumberStepper(
                            "%.1f".format(if (lb) weight * 2.20462 else weight),
                            {
                                weight = (weight - if (lb) 0.0454 * 2 else 0.1).coerceAtLeast(30.0)
                                saved = false
                            },
                            {
                                weight += if (lb) 0.0454 * 2 else 0.1
                                saved = false
                            },
                            unit = if (lb) "lb" else "kg"
                        )
                    }
                    Spacer(Modifier.height(Space.S))
                    Row(Modifier.fillMaxWidth().clip(Shapes.Card).background(Color.White).padding(Space.M), verticalAlignment = Alignment.CenterVertically) {
                        Column(Modifier.weight(1f)) {
                            Text(stringResource(R.string.body_check_in_waist), fontSize = TextSize.Body, fontWeight = FontWeight.SemiBold)
                            Text(stringResource(R.string.body_check_in_optional_at_the_belly_button), color = TextSecondary, fontSize = TextSize.Caption)
                        }
                        NumberStepper(
                            waist?.let { "%.0f".format(it) } ?: "—",
                            {
                                waist = ((waist ?: 81.0) - 1).coerceAtLeast(40.0)
                                saved = false
                            },
                            {
                                waist = (waist ?: 79.0) + 1
                                saved = false
                            },
                            unit = "cm"
                        )
                    }
                    Spacer(Modifier.height(Space.M))
                    AppButton(
                        if (saved) stringResource(R.string.body_check_in_saved) else stringResource(R.string.common_save_check_in),
                        variant = if (saved) ButtonVariant.OnDarkPrimary else ButtonVariant.Primary,
                        enabled = !saved
                    ) {
                        FitnessStore.logBody(BodyEntry(today, weight, waist))
                        saved = true
                    }
                }

                val trend = FitnessStore.weightTrend()
                val entries = FitnessStore.bodyEntries()
                if (trend != null) {
                    Spacer(Modifier.height(Space.M))
                    val (bg, fg) = when (trend.tone) {
                        TrendTone.GOOD -> GreenTint to GreenDeep
                        TrendTone.CAUTION -> MaroonTint to Maroon
                        TrendTone.NEUTRAL -> CardNeutral to Ink
                    }
                    Column(Modifier.fillMaxWidth().clip(Shapes.Card).background(bg).padding(Space.L)) {
                        Row(verticalAlignment = Alignment.Bottom) {
                            Text(FitnessStore.displayChange(trend.changeKg), fontSize = TextSize.Display, fontWeight = FontWeight.Bold, color = fg)
                            Text(
                                "  over ${"%.0f".format(trend.weeks.coerceAtLeast(1.0))} weeks",
                                color = TextSecondary,
                                fontSize = TextSize.BodySmall,
                                modifier = Modifier.padding(bottom = Space.Xs)
                            )
                        }
                        Text(trend.message, color = fg, fontSize = TextSize.Body, lineHeight = LineHeight.Body, modifier = Modifier.padding(top = Space.Xs))
                    }
                }
                if (entries.size >= 2) {
                    Spacer(Modifier.height(Space.M))
                    WeightChart(entries, FitnessStore.targetKg)
                }
                if (!minor) {
                    FitnessStore.waistToHeight()?.let { ratio ->
                        Spacer(Modifier.height(Space.M))
                        Column(Modifier.fillMaxWidth().clip(Shapes.Card).background(CardNeutral).padding(Space.L)) {
                            Text(
                                stringResource(R.string.body_check_in_waist_to_height),
                                color = TextSecondary,
                                fontSize = TextSize.Caption,
                                fontWeight = FontWeight.Medium
                            )
                            Text("%.2f".format(ratio), fontSize = TextSize.Headline, fontWeight = FontWeight.Bold, color = if (ratio < 0.5) Green else Coral)
                            Text(
                                if (ratio < 0.5) {
                                    stringResource(R.string.body_check_in_under_0_5_in_the)
                                } else {
                                    stringResource(R.string.body_check_in_above_0_5_bringing_your)
                                },
                                fontSize = TextSize.BodySmall,
                                lineHeight = LineHeight.Compact,
                                color = Ink.copy(alpha = 0.8f)
                            )
                        }
                    }
                }

                Spacer(Modifier.height(Space.Xl))
                Text(stringResource(R.string.common_reminder), fontSize = TextSize.Title, fontWeight = FontWeight.Bold)
                Spacer(Modifier.height(Space.S))
                Row(
                    Modifier.fillMaxWidth().clip(Shapes.Card).background(CardNeutral).padding(Space.L, Space.S),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(Modifier.weight(1f).clickable { showDay = true }) {
                        Text(stringResource(R.string.body_check_in_weekly_check_in_reminder), fontSize = TextSize.Body, fontWeight = FontWeight.SemiBold)
                        Text(
                            stringResource(R.string.body_check_in_1_ss_at_8_00, FitnessStore.weighDay.getDisplayName(TextStyle.FULL, Locale.getDefault())),
                            color = TextSecondary,
                            fontSize = TextSize.Caption
                        )
                    }
                    AppSwitch(FitnessStore.weighReminder, stringResource(R.string.body_check_in_weekly_check_in_reminder)) {
                        FitnessStore.updateWeighIn(FitnessStore.weighDay, it)
                    }
                }
                Spacer(Modifier.height(Space.S))
                Row(
                    Modifier.fillMaxWidth().clip(Shapes.Card).background(CardNeutral).padding(Space.L, Space.S),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        stringResource(R.string.body_check_in_show_weight_in_pounds),
                        fontSize = TextSize.Body,
                        fontWeight = FontWeight.SemiBold,
                        modifier = Modifier.weight(1f)
                    )
                    AppSwitch(lb, stringResource(R.string.body_check_in_show_weight_in_pounds)) { FitnessStore.updateUnits(it) }
                }

                if (entries.isNotEmpty()) {
                    Spacer(Modifier.height(Space.Xl))
                    Text(stringResource(R.string.common_history), fontSize = TextSize.Title, fontWeight = FontWeight.Bold)
                    Spacer(Modifier.height(Space.S))
                    entries.reversed().forEach { e ->
                        Row(
                            Modifier.fillMaxWidth().padding(bottom = Space.S).clip(Shapes.Medium).background(CardNeutral).padding(Space.L, Space.S),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(Modifier.weight(1f)) {
                                Text(
                                    e.date.format(DateTimeFormatter.ofPattern("EEE d MMM yyyy", Locale.getDefault())),
                                    fontSize = TextSize.Body,
                                    fontWeight = FontWeight.SemiBold
                                )
                                e.waistCm?.let { Text("Waist ${"%.0f".format(it)} cm", color = TextSecondary, fontSize = TextSize.Caption) }
                            }
                            Text(FitnessStore.displayWeight(e.weightKg), fontSize = TextSize.Body, fontWeight = FontWeight.Bold)
                            Box(
                                Modifier.padding(start = Space.S).size(34.dp).clip(CircleShape).clickable {
                                    deleting = e
                                },
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    Icons.Outlined.DeleteOutline,
                                    contentDescription = stringResource(R.string.common_delete),
                                    tint = TextSecondary,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    if (askConsent) {
        ConsentSheet(Consent.BODY, Icons.Filled.MonitorWeight, onAllow = { askConsent = false }, onDecline = {
            askConsent = false
            navController.goBack()
        })
    }
    if (showDay) {
        ReMakeUDialog(onDismissRequest = { showDay = false }, title = "Check-in day", primaryText = "Done", secondaryText = null, content = {
            Column {
                DayOfWeek.entries.forEach { d ->
                    ReMakeUOptionRow(d.getDisplayName(TextStyle.FULL, Locale.getDefault()), FitnessStore.weighDay == d) { FitnessStore.updateWeighIn(d, true) }
                }
            }
        })
    }
    deleting?.let { e ->
        ReMakeUDialog(
            onDismissRequest = { deleting = null },
            title = "Delete this check-in?",
            message = "${e.date} · ${FitnessStore.displayWeight(e.weightKg)}. This can't be undone.",
            primaryText = "Delete",
            primaryDestructive = true,
            onPrimary = {
                FitnessStore.removeBody(e.date)
                if (e.date == today) saved = false
                deleting = null
            }
        )
    }
}

@Composable
private fun WeightChart(entries: List<BodyEntry>, targetKg: Double?) {
    val pts = entries.takeLast(16)
    Column(Modifier.fillMaxWidth().clip(Shapes.Card).background(CardNeutral).padding(Space.L)) {
        Text(stringResource(R.string.body_check_in_weight), color = TextSecondary, fontSize = TextSize.Caption, fontWeight = FontWeight.Medium)
        Spacer(Modifier.height(Space.S))
        Canvas(Modifier.fillMaxWidth().height(130.dp)) {
            val values = pts.map { it.weightKg }
            val lo = (values + listOfNotNull(targetKg)).min() - 0.5
            val hi = (values + listOfNotNull(targetKg)).max() + 0.5
            fun y(v: Double) = (size.height - ((v - lo) / (hi - lo) * size.height)).toFloat()
            val step = if (pts.size > 1) size.width / (pts.size - 1) else 0f
            targetKg?.let {
                drawLine(
                    Green.copy(alpha = 0.6f),
                    Offset(0f, y(it)),
                    Offset(size.width, y(it)),
                    1.5.dp.toPx(),
                    pathEffect = PathEffect.dashPathEffect(floatArrayOf(10f, 10f))
                )
            }
            val path = Path()
            values.forEachIndexed { i, v -> if (i == 0) path.moveTo(i * step, y(v)) else path.lineTo(i * step, y(v)) }
            drawPath(path, Purple, style = Stroke(3.dp.toPx()))
            values.forEachIndexed { i, v -> drawCircle(Purple, 4.dp.toPx(), Offset(i * step, y(v))) }
        }
        targetKg?.let {
            Text(
                stringResource(R.string.body_check_in_dashed_line_your_target_1, FitnessStore.displayWeight(it)),
                color = TextSecondary,
                fontSize = TextSize.Caption,
                modifier = Modifier.padding(top = Space.S)
            )
        }
    }
}
