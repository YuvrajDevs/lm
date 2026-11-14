package com.glowup.app.ui.screens.fitness

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bedtime
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import com.glowup.app.R
import com.glowup.app.ui.components.DetailHeader
import com.glowup.app.ui.components.ProLockedScreen
import com.glowup.app.ui.components.goBack
import com.glowup.app.ui.data.ProGate
import com.glowup.app.ui.data.fitness.FitnessStore
import com.glowup.app.ui.data.fitness.Programmes
import com.glowup.app.ui.design.AppButton
import com.glowup.app.ui.design.ButtonVariant
import com.glowup.app.ui.design.LineHeight
import com.glowup.app.ui.design.Shapes
import com.glowup.app.ui.design.Space
import com.glowup.app.ui.design.TextSize
import com.glowup.app.ui.theme.*

@Composable
fun ProgrammeScreen(navController: NavHostController, id: String) {
    val programme = Programmes.get(id) ?: return
    if (ProGate.programmeLocked(id)) {
        ProLockedScreen(
            navController,
            programme.title,
            "${programme.title} is part of Pro — along with every 30-day programme, longer sessions and auto-progression."
        )
        return
    }
    val done = FitnessStore.daysDone(id)
    val next = FitnessStore.nextDay(id)
    val active = FitnessStore.activeProgramme == id
    val preview = programme.dayPlan(next ?: 1, FitnessStore.level, FitnessStore::bonusFor)

    Box(Modifier.fillMaxSize().background(Surface).systemBarsPadding()) {
        Column(Modifier.fillMaxSize()) {
            DetailHeader(programme.title) { navController.goBack() }
            Column(Modifier.weight(1f).verticalScroll(rememberScrollState()).padding(Space.Gutter, Space.L, Space.Gutter, Space.Gutter)) {
                Column(Modifier.fillMaxWidth().clip(Shapes.Hero).background(programme.focus.tint).padding(Space.Xl)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(Modifier.size(46.dp).clip(CircleShape).background(Color.White), contentAlignment = Alignment.Center) {
                            Icon(programme.focus.icon(), contentDescription = null, tint = programme.focus.accent)
                        }
                        Spacer(Modifier.width(Space.M))
                        Column {
                            Text(
                                programme.tagline.uppercase(),
                                color = programme.focus.accent,
                                fontSize = TextSize.Micro,
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 0.4.sp
                            )
                            Text(
                                stringResource(R.string.programme_30_days_1_s_workouts, 30 - programme.restDays.size),
                                fontSize = TextSize.BodySmall,
                                color = TextSecondary
                            )
                        }
                    }
                    Spacer(Modifier.height(Space.M))
                    Text(programme.about, fontSize = TextSize.Body, lineHeight = LineHeight.Body)
                    Spacer(Modifier.height(Space.M))
                    Box(Modifier.fillMaxWidth().height(8.dp).clip(Shapes.Pill).background(Color.White)) {
                        Box(Modifier.fillMaxWidth(done.size / 30f).fillMaxHeight().clip(Shapes.Pill).background(programme.focus.accent))
                    }
                    Text(
                        if (next ==
                            null
                        ) {
                            stringResource(R.string.programme_completed_all_30_days)
                        } else {
                            stringResource(R.string.programme_1_s_of_30_days, done.size)
                        },
                        color = programme.focus.accent,
                        fontSize = TextSize.Caption,
                        fontWeight = FontWeight.SemiBold,
                        modifier = Modifier.padding(top = Space.S)
                    )
                }

                programme.honestNote?.let { note ->
                    Spacer(Modifier.height(Space.M))
                    Row(Modifier.fillMaxWidth().clip(Shapes.Medium).background(CardNeutral).padding(Space.M, Space.M)) {
                        Icon(Icons.Outlined.Info, contentDescription = null, tint = Ink, modifier = Modifier.size(18.dp))
                        Spacer(Modifier.width(Space.S))
                        Text(note, fontSize = TextSize.BodySmall, lineHeight = LineHeight.Compact, color = Ink.copy(alpha = 0.8f))
                    }
                }

                Spacer(Modifier.height(Space.Xl))
                Text(stringResource(R.string.programme_your_30_days), fontSize = TextSize.Title, fontWeight = FontWeight.Bold)
                Text(
                    stringResource(R.string.programme_tap_any_finished_or_upcoming),
                    color = TextSecondary,
                    fontSize = TextSize.Caption,
                    modifier = Modifier.padding(top = 2.dp, bottom = Space.M)
                )
                (1..30).chunked(5).forEach { row ->
                    Row(Modifier.fillMaxWidth().padding(bottom = Space.S), horizontalArrangement = Arrangement.spacedBy(Space.S)) {
                        row.forEach { day ->
                            val isDone = day in done
                            val isNext = day == next
                            val rest = programme.isRestDay(day)
                            Box(
                                Modifier
                                    .weight(1f)
                                    .aspectRatio(1f)
                                    .clip(Shapes.Medium)
                                    .background(
                                        when {
                                            isDone -> programme.focus.accent
                                            isNext -> Ink
                                            rest -> CardNeutral
                                            else -> programme.focus.tint
                                        }
                                    )
                                    .then(if (isNext) Modifier.border(2.dp, Ink, Shapes.Medium) else Modifier)
                                    .clickable(enabled = !rest) { navController.navigate("workout/p.$id.$day") },
                                contentAlignment = Alignment.Center
                            ) {
                                when {
                                    isDone -> Icon(
                                        Icons.Filled.Check,
                                        contentDescription = stringResource(R.string.programme_day_1_s_done, day),
                                        tint = Color.White,
                                        modifier = Modifier.size(18.dp)
                                    )
                                    rest -> Icon(
                                        Icons.Filled.Bedtime,
                                        contentDescription = stringResource(R.string.programme_day_1_s_rest, day),
                                        tint = TextMuted,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    day == 30 -> Icon(
                                        Icons.Filled.EmojiEvents,
                                        contentDescription = stringResource(R.string.programme_day_30),
                                        tint = if (isNext) Color.White else programme.focus.accent,
                                        modifier = Modifier.size(18.dp)
                                    )
                                    else -> Text(
                                        "$day",
                                        color = if (isNext) Color.White else Ink,
                                        fontSize = TextSize.Body,
                                        fontWeight = FontWeight.Bold,
                                        textAlign = TextAlign.Center
                                    )
                                }
                            }
                        }
                    }
                }

                if (next != null && !preview.restDay) {
                    Spacer(Modifier.height(Space.M))
                    Text(stringResource(R.string.programme_up_next_1_s, preview.title), fontSize = TextSize.BodyLarge, fontWeight = FontWeight.Bold)
                    Spacer(Modifier.height(Space.S))
                    preview.blocks.forEach { b ->
                        Row(Modifier.fillMaxWidth().padding(vertical = Space.Xs), verticalAlignment = Alignment.CenterVertically) {
                            ExerciseArt(b.exercise, 40.dp, 12.dp)
                            Spacer(Modifier.width(Space.M))
                            Text(b.exercise.name, fontSize = TextSize.Body, fontWeight = FontWeight.SemiBold, modifier = Modifier.weight(1f))
                            Text(
                                if (b.max) stringResource(R.string.programme_max) else "${b.sets} × ${b.exercise.doseLabel(b.target)}",
                                color = TextSecondary,
                                fontSize = TextSize.BodySmall
                            )
                        }
                    }
                }
            }

            Column(Modifier.padding(Space.Gutter, Space.S, Space.Gutter, Space.Gutter)) {
                AppButton(
                    when {
                        next == null -> if (active) {
                            stringResource(
                                R.string.programme_programme_complete
                            )
                        } else {
                            stringResource(R.string.programme_make_this_my_programme)
                        }
                        !active -> if (done.isEmpty()) {
                            stringResource(
                                R.string.programme_start_programme
                            )
                        } else {
                            stringResource(R.string.programme_switch_to_this_resume_day, next)
                        }
                        preview.restDay -> stringResource(R.string.programme_today_is_a_rest_day)
                        else -> stringResource(R.string.programme_start_day_1_s, next)
                    },
                    variant = ButtonVariant.Primary
                ) {
                    if (!active) FitnessStore.startProgramme(id)
                    if (next != null) {
                        if (preview.restDay) navController.goBack() else navController.navigate("workout/${preview.key}")
                    }
                }
                if (active && next != null) {
                    Text(
                        stringResource(R.string.programme_stop_this_programme),
                        color = TextSecondary,
                        fontSize = TextSize.BodySmall,
                        fontWeight = FontWeight.SemiBold,
                        modifier = Modifier.align(Alignment.CenterHorizontally).padding(top = Space.M).clickable { FitnessStore.stopProgramme() }
                    )
                }
            }
        }
    }
}
