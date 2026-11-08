package com.glowup.app.ui.screens.fitness

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MonitorWeight
import androidx.compose.material.icons.outlined.DeleteOutline
import androidx.compose.material3.Icon
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import com.glowup.app.ui.components.ConsentSheet
import com.glowup.app.ui.components.DetailHeader
import com.glowup.app.ui.components.GlowUpDialog
import com.glowup.app.ui.components.GlowUpOptionRow
import com.glowup.app.ui.components.goBack
import com.glowup.app.ui.data.Consent
import com.glowup.app.ui.data.ConsentStore
import com.glowup.app.ui.data.ProfileStore
import com.glowup.app.ui.data.fitness.BodyEntry
import com.glowup.app.ui.data.fitness.FitnessStore
import com.glowup.app.ui.data.fitness.TrendTone
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
            DetailHeader("Weekly check-in") { navController.goBack() }
            Column(Modifier.weight(1f).verticalScroll(rememberScrollState()).padding(24.dp, 18.dp, 24.dp, 28.dp)) {
                Column(Modifier.fillMaxWidth().clip(RoundedCornerShape(28.dp)).background(PurpleTint).padding(20.dp)) {
                    Text(if (saved) "SAVED FOR TODAY" else "WEIGH IN", color = Purple, fontSize = 11.sp, fontWeight = FontWeight.Bold, letterSpacing = 0.4.sp)
                    Text("Same day, same time each week", fontSize = 18.sp, fontWeight = FontWeight.Bold)
                    Text("Morning, after the bathroom, before eating — that's the most consistent.", color = Color(0xFF5A4A8C), fontSize = 12.5.sp, lineHeight = 17.sp, modifier = Modifier.padding(top = 2.dp))
                    Spacer(Modifier.height(16.dp))
                    Row(Modifier.fillMaxWidth().clip(RoundedCornerShape(20.dp)).background(Color.White).padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                        Text("Weight", fontSize = 14.sp, fontWeight = FontWeight.SemiBold, modifier = Modifier.weight(1f))
                        NumberStepper(
                            "%.1f".format(if (lb) weight * 2.20462 else weight),
                            { weight = (weight - if (lb) 0.0454 * 2 else 0.1).coerceAtLeast(30.0); saved = false },
                            { weight += if (lb) 0.0454 * 2 else 0.1; saved = false },
                            unit = if (lb) "lb" else "kg",
                        )
                    }
                    Spacer(Modifier.height(8.dp))
                    Row(Modifier.fillMaxWidth().clip(RoundedCornerShape(20.dp)).background(Color.White).padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                        Column(Modifier.weight(1f)) {
                            Text("Waist", fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
                            Text("Optional · at the belly button", color = TextSecondary, fontSize = 11.5.sp)
                        }
                        NumberStepper(
                            waist?.let { "%.0f".format(it) } ?: "—",
                            { waist = ((waist ?: 81.0) - 1).coerceAtLeast(40.0); saved = false },
                            { waist = (waist ?: 79.0) + 1; saved = false },
                            unit = "cm",
                        )
                    }
                    Spacer(Modifier.height(14.dp))
                    Box(
                        Modifier.fillMaxWidth().height(52.dp).clip(RoundedCornerShape(999.dp)).background(if (saved) Color.White else Ink).clickable(enabled = !saved) {
                            FitnessStore.logBody(BodyEntry(today, weight, waist))
                            saved = true
                        },
                        contentAlignment = Alignment.Center,
                    ) { Text(if (saved) "Saved ✓" else "Save check-in", color = if (saved) Purple else Color.White, fontSize = 15.sp, fontWeight = FontWeight.SemiBold) }
                }

                val trend = FitnessStore.weightTrend()
                val entries = FitnessStore.bodyEntries()
                if (trend != null) {
                    Spacer(Modifier.height(14.dp))
                    val (bg, fg) = when (trend.tone) {
                        TrendTone.GOOD -> GreenTint to Color(0xFF1E7A4F)
                        TrendTone.CAUTION -> MaroonTint to Maroon
                        TrendTone.NEUTRAL -> CardNeutral to Ink
                    }
                    Column(Modifier.fillMaxWidth().clip(RoundedCornerShape(22.dp)).background(bg).padding(18.dp)) {
                        Row(verticalAlignment = Alignment.Bottom) {
                            Text(FitnessStore.displayChange(trend.changeKg), fontSize = 26.sp, fontWeight = FontWeight.Bold, color = fg)
                            Text("  over ${"%.0f".format(trend.weeks.coerceAtLeast(1.0))} weeks", color = TextSecondary, fontSize = 13.sp, modifier = Modifier.padding(bottom = 4.dp))
                        }
                        Text(trend.message, color = fg, fontSize = 13.5.sp, lineHeight = 19.sp, modifier = Modifier.padding(top = 4.dp))
                    }
                }
                if (entries.size >= 2) {
                    Spacer(Modifier.height(14.dp))
                    WeightChart(entries, FitnessStore.targetKg)
                }
                if (!minor) {
                    FitnessStore.waistToHeight()?.let { ratio ->
                        Spacer(Modifier.height(14.dp))
                        Column(Modifier.fillMaxWidth().clip(RoundedCornerShape(22.dp)).background(CardNeutral).padding(18.dp)) {
                            Text("Waist-to-height", color = TextSecondary, fontSize = 12.sp, fontWeight = FontWeight.Medium)
                            Text("%.2f".format(ratio), fontSize = 24.sp, fontWeight = FontWeight.Bold, color = if (ratio < 0.5) Green else Coral)
                            Text(
                                if (ratio < 0.5) "Under 0.5 — in the healthy range. Keep it there."
                                else "Above 0.5. Bringing your waist down is one of the best things you can do for health — and it sharpens your face too.",
                                fontSize = 12.5.sp, lineHeight = 18.sp, color = Ink.copy(alpha = 0.8f),
                            )
                        }
                    }
                }

                Spacer(Modifier.height(22.dp))
                Text("Reminder", fontSize = 17.sp, fontWeight = FontWeight.Bold)
                Spacer(Modifier.height(8.dp))
                Row(Modifier.fillMaxWidth().clip(RoundedCornerShape(20.dp)).background(CardNeutral).padding(16.dp, 10.dp), verticalAlignment = Alignment.CenterVertically) {
                    Column(Modifier.weight(1f).clickable { showDay = true }) {
                        Text("Weekly check-in reminder", fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
                        Text("${FitnessStore.weighDay.getDisplayName(TextStyle.FULL, Locale.getDefault())}s at 8:00 AM · tap to change", color = TextSecondary, fontSize = 12.sp)
                    }
                    Switch(
                        checked = FitnessStore.weighReminder,
                        onCheckedChange = { FitnessStore.updateWeighIn(FitnessStore.weighDay, it) },
                        colors = SwitchDefaults.colors(checkedTrackColor = Ink, checkedThumbColor = Color.White),
                    )
                }
                Spacer(Modifier.height(8.dp))
                Row(Modifier.fillMaxWidth().clip(RoundedCornerShape(20.dp)).background(CardNeutral).padding(16.dp, 10.dp), verticalAlignment = Alignment.CenterVertically) {
                    Text("Show weight in pounds", fontSize = 14.sp, fontWeight = FontWeight.SemiBold, modifier = Modifier.weight(1f))
                    Switch(checked = lb, onCheckedChange = { FitnessStore.updateUnits(it) }, colors = SwitchDefaults.colors(checkedTrackColor = Ink, checkedThumbColor = Color.White))
                }

                if (entries.isNotEmpty()) {
                    Spacer(Modifier.height(22.dp))
                    Text("History", fontSize = 17.sp, fontWeight = FontWeight.Bold)
                    Spacer(Modifier.height(8.dp))
                    entries.reversed().forEach { e ->
                        Row(Modifier.fillMaxWidth().padding(bottom = 8.dp).clip(RoundedCornerShape(18.dp)).background(CardNeutral).padding(16.dp, 10.dp), verticalAlignment = Alignment.CenterVertically) {
                            Column(Modifier.weight(1f)) {
                                Text(e.date.format(DateTimeFormatter.ofPattern("EEE d MMM yyyy", Locale.getDefault())), fontSize = 13.5.sp, fontWeight = FontWeight.SemiBold)
                                e.waistCm?.let { Text("Waist ${"%.0f".format(it)} cm", color = TextSecondary, fontSize = 12.sp) }
                            }
                            Text(FitnessStore.displayWeight(e.weightKg), fontSize = 14.sp, fontWeight = FontWeight.Bold)
                            Box(Modifier.padding(start = 6.dp).size(34.dp).clip(CircleShape).clickable { deleting = e }, contentAlignment = Alignment.Center) {
                                Icon(Icons.Outlined.DeleteOutline, contentDescription = "Delete", tint = TextSecondary, modifier = Modifier.size(18.dp))
                            }
                        }
                    }
                }
            }
        }
    }

    if (askConsent) {
        ConsentSheet(Consent.BODY, Icons.Filled.MonitorWeight, onAllow = { askConsent = false }, onDecline = { askConsent = false; navController.goBack() })
    }
    if (showDay) {
        GlowUpDialog(onDismissRequest = { showDay = false }, title = "Check-in day", primaryText = "Done", secondaryText = null, content = {
            Column { DayOfWeek.entries.forEach { d -> GlowUpOptionRow(d.getDisplayName(TextStyle.FULL, Locale.getDefault()), FitnessStore.weighDay == d) { FitnessStore.updateWeighIn(d, true) } } }
        })
    }
    deleting?.let { e ->
        GlowUpDialog(
            onDismissRequest = { deleting = null }, title = "Delete this check-in?",
            message = "${e.date} · ${FitnessStore.displayWeight(e.weightKg)}. This can't be undone.",
            primaryText = "Delete", primaryDestructive = true,
            onPrimary = { FitnessStore.removeBody(e.date); if (e.date == today) saved = false; deleting = null },
        )
    }
}

@Composable
private fun WeightChart(entries: List<BodyEntry>, targetKg: Double?) {
    val pts = entries.takeLast(16)
    Column(Modifier.fillMaxWidth().clip(RoundedCornerShape(22.dp)).background(CardNeutral).padding(16.dp)) {
        Text("Weight", color = TextSecondary, fontSize = 12.sp, fontWeight = FontWeight.Medium)
        Spacer(Modifier.height(8.dp))
        Canvas(Modifier.fillMaxWidth().height(130.dp)) {
            val values = pts.map { it.weightKg }
            val lo = (values + listOfNotNull(targetKg)).min() - 0.5
            val hi = (values + listOfNotNull(targetKg)).max() + 0.5
            fun y(v: Double) = (size.height - ((v - lo) / (hi - lo) * size.height)).toFloat()
            val step = if (pts.size > 1) size.width / (pts.size - 1) else 0f
            targetKg?.let {
                drawLine(Green.copy(alpha = 0.6f), Offset(0f, y(it)), Offset(size.width, y(it)), 1.5.dp.toPx(), pathEffect = PathEffect.dashPathEffect(floatArrayOf(10f, 10f)))
            }
            val path = Path()
            values.forEachIndexed { i, v -> if (i == 0) path.moveTo(i * step, y(v)) else path.lineTo(i * step, y(v)) }
            drawPath(path, Purple, style = Stroke(3.dp.toPx()))
            values.forEachIndexed { i, v -> drawCircle(Purple, 4.dp.toPx(), Offset(i * step, y(v))) }
        }
        targetKg?.let { Text("Dashed line: your target ${FitnessStore.displayWeight(it)}", color = TextSecondary, fontSize = 11.5.sp, modifier = Modifier.padding(top = 6.dp)) }
    }
}
