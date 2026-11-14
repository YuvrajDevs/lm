package com.glowup.app.ui.screens.fitness

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.TrendingDown
import androidx.compose.material.icons.automirrored.filled.TrendingUp
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.glowup.app.R
import com.glowup.app.ui.data.ProGate
import com.glowup.app.ui.data.ProStore
import com.glowup.app.ui.data.fitness.Direction
import com.glowup.app.ui.data.fitness.ExerciseLibrary
import com.glowup.app.ui.data.fitness.FitnessStore
import com.glowup.app.ui.data.fitness.Measure
import com.glowup.app.ui.data.fitness.SetLog
import com.glowup.app.ui.data.fitness.WorkoutResult
import com.glowup.app.ui.design.LineHeight
import com.glowup.app.ui.design.Shapes
import com.glowup.app.ui.design.Space
import com.glowup.app.ui.design.TextSize
import com.glowup.app.ui.theme.*
import kotlin.math.roundToInt

@Composable
internal fun Summary(title: String, logged: List<SetLog>, seconds: Int, effort: Int, onEffort: (Int) -> Unit, onSave: () -> Unit, onDiscard: () -> Unit) {
    val scored = logged.filter { !it.max }
    val percent = if (scored.isEmpty()) 100 else (scored.sumOf { minOf(it.done, it.target) } * 100.0 / scored.sumOf { it.target }.coerceAtLeast(1)).roundToInt()
    val bests = logged.map { it.exerciseId }.distinct().filter { id ->
        val old = FitnessStore.bestFor(id)
        old != null && logged.filter { it.exerciseId == id }.maxOf { it.done } > old
    }
    Column(Modifier.fillMaxSize()) {
        Column(
            Modifier.weight(1f).verticalScroll(rememberScrollState()).padding(Space.Gutter, Space.Gutter, Space.Gutter, Space.L),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                stringResource(R.string.workout_player_scorecard),
                color = Color.White.copy(alpha = 0.55f),
                fontSize = TextSize.Caption,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.sp
            )
            Text(
                title,
                color = Color.White,
                fontSize = TextSize.TitleLarge,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(top = Space.Xs)
            )
            Spacer(Modifier.height(Space.L))
            Box(Modifier.size(150.dp), contentAlignment = Alignment.Center) {
                Canvas(Modifier.fillMaxSize()) {
                    val stroke = 14.dp.toPx()
                    val inset = stroke / 2
                    val color = when {
                        percent >= 90 -> Mint
                        percent >= 60 -> Sun
                        else -> Peach
                    }
                    drawArc(
                        Color.White.copy(alpha = 0.1f),
                        0f,
                        360f,
                        false,
                        Offset(inset, inset),
                        Size(size.width - stroke, size.height - stroke),
                        style = Stroke(stroke)
                    )
                    drawArc(
                        color,
                        -90f,
                        3.6f * percent.coerceIn(0, 100),
                        false,
                        Offset(inset, inset),
                        Size(size.width - stroke, size.height - stroke),
                        style = Stroke(stroke, cap = StrokeCap.Round)
                    )
                }
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("$percent%", color = Color.White, fontSize = TextSize.DisplayLarge, fontWeight = FontWeight.Bold)
                    Text(stringResource(R.string.workout_player_of_targets), color = Color.White.copy(alpha = 0.55f), fontSize = TextSize.Caption)
                }
            }
            Spacer(Modifier.height(Space.S))
            Text(
                when {
                    percent >= 95 -> stringResource(R.string.workout_player_every_target_hit_strong)
                    percent >= 75 -> stringResource(R.string.workout_player_solid_session_almost_everything_done)
                    percent >= 50 -> stringResource(R.string.workout_player_good_effort_the_reps_you)
                    else -> stringResource(R.string.workout_player_showing_up_is_the_win)
                },
                color = Color.White.copy(alpha = 0.75f),
                fontSize = TextSize.Body,
                textAlign = TextAlign.Center
            )
            Text(
                stringResource(R.string.workout_player_1_s_2_s_sets, formatElapsed(seconds), logged.size),
                color = Color.White.copy(alpha = 0.5f),
                fontSize = TextSize.BodySmall,
                modifier = Modifier.padding(top = Space.Xs)
            )

            Spacer(Modifier.height(Space.Xl))
            logged.groupBy { it.exerciseId }.forEach { (id, sets) ->
                val e = ExerciseLibrary.get(id)
                Row(Modifier.fillMaxWidth().padding(vertical = Space.S), verticalAlignment = Alignment.CenterVertically) {
                    ExerciseArt(e, 40.dp, 12.dp)
                    Spacer(Modifier.width(Space.M))
                    Column(Modifier.weight(1f)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(e.name, color = Color.White, fontSize = TextSize.Body, fontWeight = FontWeight.SemiBold)
                            if (id in bests) {
                                Spacer(Modifier.width(Space.S))
                                Icon(
                                    Icons.Filled.EmojiEvents,
                                    contentDescription = stringResource(R.string.workout_player_personal_best),
                                    tint = Sun,
                                    modifier = Modifier.size(15.dp)
                                )
                                Text(stringResource(R.string.workout_player_new_best), color = Sun, fontSize = TextSize.Micro, fontWeight = FontWeight.Bold)
                            }
                        }
                        Spacer(Modifier.height(Space.Xs))
                        Row(horizontalArrangement = Arrangement.spacedBy(Space.Xs)) {
                            sets.forEach { s ->
                                val ratio = if (s.max || s.target == 0) 1.0 else s.done.toDouble() / s.target
                                val c = when {
                                    ratio >= 1 -> Mint
                                    ratio >= 0.6 -> Sun
                                    else -> Peach
                                }
                                Text(
                                    if (s.max) "${s.done}" else "${s.done}/${s.target}",
                                    color = Ink,
                                    fontSize = TextSize.Caption,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.clip(Shapes.Pill).background(c).padding(Space.S, Space.Xs)
                                )
                            }
                        }
                    }
                }
            }

            Spacer(Modifier.height(Space.Xl))
            Text(stringResource(R.string.workout_player_how_hard_was_it), color = Color.White, fontSize = TextSize.BodyLarge, fontWeight = FontWeight.SemiBold)
            Spacer(Modifier.height(Space.S))
            Row(horizontalArrangement = Arrangement.spacedBy(Space.S)) {
                effortLabels.forEachIndexed { i, label ->
                    val on = effort == i + 1
                    Text(
                        label,
                        color = if (on) Ink else Color.White,
                        fontSize = TextSize.Caption,
                        fontWeight = FontWeight.SemiBold,
                        textAlign = TextAlign.Center,
                        maxLines = 1,
                        modifier = Modifier.weight(1f).clip(Shapes.Pill).background(if (on) Color.White else Color.White.copy(alpha = 0.1f)).clickable {
                            onEffort(
                                i + 1
                            )
                        }.padding(vertical = Space.S)
                    )
                }
            }
        }
        Column(Modifier.padding(Space.Gutter, Space.S, Space.Gutter, Space.Xl)) {
            WhitePill(stringResource(R.string.workout_player_save_workout), onClick = onSave)
            Text(
                stringResource(R.string.workout_player_discard),
                color = Color.White.copy(alpha = 0.6f),
                fontSize = TextSize.Body,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier.align(Alignment.CenterHorizontally).padding(top = Space.M).clickable(onClick = onDiscard)
            )
        }
    }
}

@Composable
internal fun Saved(result: WorkoutResult, logged: List<SetLog>, seconds: Int, onDone: () -> Unit) {
    Column(Modifier.fillMaxSize()) {
        Column(
            Modifier.weight(1f).verticalScroll(rememberScrollState()).padding(Space.Gutter, Space.Huge, Space.Gutter, Space.L),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(Modifier.size(84.dp).clip(CircleShape).background(Mint), contentAlignment = Alignment.Center) {
                Icon(Icons.Filled.EmojiEvents, contentDescription = null, tint = Ink, modifier = Modifier.size(42.dp))
            }
            Spacer(Modifier.height(Space.L))
            Text(stringResource(R.string.workout_player_workout_saved), color = Color.White, fontSize = TextSize.Headline, fontWeight = FontWeight.Bold)
            Text(
                stringResource(R.string.workout_player_1_s_points_2_s, result.points, formatElapsed(seconds)),
                color = Color.White.copy(alpha = 0.6f),
                fontSize = TextSize.Body,
                modifier = Modifier.padding(top = Space.Xs)
            )

            if (result.personalBests.isNotEmpty()) {
                Spacer(Modifier.height(Space.Xl))
                Column(Modifier.fillMaxWidth().clip(Shapes.Card).background(Sun.copy(alpha = 0.15f)).padding(Space.L)) {
                    Text(stringResource(R.string.workout_player_new_personal_bests), color = Sun, fontSize = TextSize.BodySmall, fontWeight = FontWeight.Bold)
                    result.personalBests.forEach { id ->
                        Text(
                            "${ExerciseLibrary.get(id).name} · ${logged.filter {
                                it.exerciseId == id
                            }.maxOf { it.done }}",
                            color = Color.White,
                            fontSize = TextSize.Body,
                            modifier = Modifier.padding(top = Space.Xs)
                        )
                    }
                }
            }

            Spacer(Modifier.height(Space.M))
            Column(Modifier.fillMaxWidth().clip(Shapes.Card).background(Color.White.copy(alpha = 0.08f)).padding(Space.L)) {
                Text(stringResource(R.string.workout_player_next_time), color = Color.White, fontSize = TextSize.BodySmall, fontWeight = FontWeight.Bold)
                if (ProGate.progressionLocked) {
                    Row(
                        Modifier.padding(top = Space.S).clickable {
                            ProStore.showPaywall("Let Re:MakeU raise your targets as you get stronger")
                        },
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Filled.Lock, contentDescription = null, tint = Sun, modifier = Modifier.size(14.dp))
                        Spacer(Modifier.width(Space.S))
                        Text(
                            stringResource(R.string.workout_player_with_pro_targets_go_up),
                            color = Color.White.copy(alpha = 0.7f),
                            fontSize = TextSize.BodySmall,
                            lineHeight = LineHeight.Compact
                        )
                    }
                } else if (result.changes.isEmpty()) {
                    Text(
                        stringResource(R.string.workout_player_same_targets_hit_every_set),
                        color = Color.White.copy(alpha = 0.7f),
                        fontSize = TextSize.BodySmall,
                        lineHeight = LineHeight.Compact,
                        modifier = Modifier.padding(top = Space.S)
                    )
                } else {
                    result.changes.forEach { c ->
                        val e = ExerciseLibrary.get(c.exerciseId)
                        Row(Modifier.padding(top = Space.S), verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                if (c.direction == Direction.UP) Icons.AutoMirrored.Filled.TrendingUp else Icons.AutoMirrored.Filled.TrendingDown,
                                contentDescription = null,
                                tint = if (c.direction == Direction.UP) Mint else Sun,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(Modifier.width(Space.S))
                            val unit = if (e.measure == Measure.REPS) "reps" else "sec"
                            Text(
                                "${e.name}: ${if (c.direction == Direction.UP) "+" else "−"}${e.step} $unit",
                                color = Color.White,
                                fontSize = TextSize.Body
                            )
                        }
                    }
                }
            }
        }
        Box(Modifier.padding(Space.Gutter, Space.S, Space.Gutter, Space.Xl)) { WhitePill(stringResource(R.string.common_done), onClick = onDone) }
    }
}
