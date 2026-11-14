package com.glowup.app.ui.screens.speaking

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Mic
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
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import com.glowup.app.R
import com.glowup.app.ui.components.DetailHeader
import com.glowup.app.ui.components.ProBadge
import com.glowup.app.ui.components.ReMakeUDialog
import com.glowup.app.ui.components.goBack
import com.glowup.app.ui.data.ProStore
import com.glowup.app.ui.data.Scenarios
import com.glowup.app.ui.data.SpeechSession
import com.glowup.app.ui.data.SpeechStore
import com.glowup.app.ui.design.EmptyState
import com.glowup.app.ui.design.LineHeight
import com.glowup.app.ui.design.Shapes
import com.glowup.app.ui.design.Space
import com.glowup.app.ui.design.TextSize
import com.glowup.app.ui.screens.fitness.ChoicePill
import com.glowup.app.ui.theme.*
import java.time.format.DateTimeFormatter
import java.util.Locale

@Composable
fun ScenariosScreen(navController: NavHostController) {
    var category by remember { mutableStateOf<String?>(null) }
    val list = Scenarios.all.filter { category == null || it.category == category }
    Column(Modifier.fillMaxSize().background(Surface).systemBarsPadding()) {
        DetailHeader(stringResource(R.string.speech_practice_scenarios)) { navController.goBack() }
        LazyColumn(contentPadding = PaddingValues(Space.Gutter, Space.M, Space.Gutter, Space.Section), verticalArrangement = Arrangement.spacedBy(Space.S)) {
            item {
                Text(stringResource(R.string.speech_real_moments_to_rehearse_a), color = TextSecondary, fontSize = TextSize.Body, lineHeight = LineHeight.Body)
                Spacer(Modifier.height(Space.M))
                LazyRow(horizontalArrangement = Arrangement.spacedBy(Space.S)) {
                    item { ChoicePill(stringResource(R.string.common_all), category == null) { category = null } }
                    items(Scenarios.categories) { c -> ChoicePill(c, category == c) { category = if (category == c) null else c } }
                }
            }
            items(list, key = { it.id }) { s ->
                val locked = !ProStore.isPro && !Scenarios.isFree(s)
                Row(
                    Modifier.fillMaxWidth().clip(Shapes.Card).background(CardNeutral).clickable {
                        if (locked) ProStore.showPaywall("Every practice scenario is part of Pro") else navController.navigate("scenario/${s.id}")
                    }.padding(Space.L, Space.M),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(Modifier.weight(1f)) {
                        Text(s.category.uppercase(), color = Purple, fontSize = TextSize.Micro, fontWeight = FontWeight.Bold, letterSpacing = 0.4.sp)
                        Text(s.title, fontSize = TextSize.BodyLarge, fontWeight = FontWeight.SemiBold)
                        Text(
                            stringResource(R.string.speech_1_ss_to_think_2, s.thinkSeconds, s.speakSeconds),
                            color = TextSecondary,
                            fontSize = TextSize.Caption
                        )
                    }
                    if (locked) ProBadge() else Icon(Icons.Filled.ChevronRight, contentDescription = null, tint = TextSecondary)
                }
            }
        }
    }
}

@Composable
fun SpeechHistoryScreen(navController: NavHostController) {
    var open by remember { mutableStateOf<SpeechSession?>(null) }
    val sessions = SpeechStore.sessions.toList()
    Column(Modifier.fillMaxSize().background(Surface).systemBarsPadding()) {
        DetailHeader(stringResource(R.string.speech_your_speaking)) { navController.goBack() }
        LazyColumn(contentPadding = PaddingValues(Space.Gutter, Space.M, Space.Gutter, Space.Section), verticalArrangement = Arrangement.spacedBy(Space.S)) {
            if (sessions.isEmpty()) {
                item {
                    EmptyState(
                        icon = Icons.Filled.Mic,
                        title = stringResource(R.string.speech_history_empty_title),
                        body = stringResource(R.string.speech_practise_in_analyse_mode_and),
                        tint = CoralTint,
                        actionLabel = stringResource(R.string.speech_history_empty_action)
                    ) { navController.navigate("speaking_practice") }
                }
            } else {
                item {
                    val chrono = sessions.reversed()
                    TrendCard("Pace", "${sessions.first().report.wpm} wpm", chrono.map { it.report.wpm.toFloat() }, 130f to 160f, "Target 130–160")
                    Spacer(Modifier.height(Space.S))
                    TrendCard(
                        "Fillers per minute",
                        "%.1f".format(sessions.first().report.fillersPerMinute),
                        chrono.map {
                            it.report.fillersPerMinute.toFloat()
                        },
                        0f to 2f,
                        "Aim for under 2"
                    )
                }
                item {
                    Text(
                        stringResource(R.string.speech_sessions),
                        fontSize = TextSize.Title,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(top = Space.S)
                    )
                }
                items(sessions, key = { it.id }) { s ->
                    Row(
                        Modifier.fillMaxWidth().clip(Shapes.Medium).background(CardNeutral).clickable {
                            open = s
                        }.padding(Space.M, Space.M),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(Modifier.weight(1f)) {
                            Text(s.topic, fontSize = TextSize.Body, fontWeight = FontWeight.SemiBold, maxLines = 1)
                            Text(
                                s.date.format(DateTimeFormatter.ofPattern("d MMM", Locale.getDefault())) + " · ${s.report.seconds}s",
                                color = TextSecondary,
                                fontSize = TextSize.Caption
                            )
                        }
                        Text(
                            stringResource(R.string.speech_1_s_wpm_2_s, s.report.wpm, s.report.fillerTotal),
                            fontSize = TextSize.BodySmall,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
            }
        }
    }
    open?.let { s ->
        ReMakeUDialog(
            onDismissRequest = { open = null },
            title = s.topic,
            primaryText = "Close",
            secondaryText = "Delete",
            secondaryDestructive = true,
            onSecondary = {
                SpeechStore.delete(s.id)
                open = null
            },
            content = {
                Text(
                    stringResource(R.string.speech_1_s_wpm_2_s_2, s.report.wpm, s.report.fillerTotal, s.report.longPauses),
                    fontSize = TextSize.BodySmall,
                    fontWeight = FontWeight.SemiBold
                )
                Spacer(Modifier.height(Space.S))
                Text(s.transcript, color = TextSecondary, fontSize = TextSize.BodySmall, lineHeight = LineHeight.Body, maxLines = 12)
            }
        )
    }
}

@Composable
private fun TrendCard(title: String, latest: String, values: List<Float>, band: Pair<Float, Float>, caption: String) {
    Column(Modifier.fillMaxWidth().clip(Shapes.Card).background(CardNeutral).padding(Space.L)) {
        Row(verticalAlignment = Alignment.Bottom) {
            Text(title, color = TextSecondary, fontSize = TextSize.BodySmall, fontWeight = FontWeight.Medium, modifier = Modifier.weight(1f))
            Text(latest, fontSize = TextSize.TitleLarge, fontWeight = FontWeight.Bold)
        }
        Spacer(Modifier.height(Space.S))
        Canvas(Modifier.fillMaxWidth().height(80.dp)) {
            val all = values + listOf(band.first, band.second)
            val lo = all.min() - 1
            val hi = all.max() + 1
            fun y(v: Float) = size.height - (v - lo) / (hi - lo) * size.height
            drawRect(Green.copy(alpha = 0.12f), Offset(0f, y(band.second)), Size(size.width, y(band.first) - y(band.second)))
            if (values.size == 1) {
                drawCircle(Purple, 5.dp.toPx(), Offset(size.width / 2, y(values[0])))
                return@Canvas
            }
            val step = size.width / (values.size - 1)
            val path = Path()
            values.forEachIndexed { i, v -> if (i == 0) path.moveTo(0f, y(v)) else path.lineTo(i * step, y(v)) }
            drawPath(path, Purple, style = Stroke(3.dp.toPx()))
            values.forEachIndexed { i, v -> drawCircle(Purple, 3.5.dp.toPx(), Offset(i * step, y(v))) }
        }
        Text(
            stringResource(R.string.speech_1_s_green_band_is, caption),
            color = TextSecondary,
            fontSize = TextSize.Caption,
            modifier = Modifier.padding(top = Space.S)
        )
    }
}
