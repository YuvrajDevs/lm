package com.glowup.app.ui.screens.fitness

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Air
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.outlined.WarningAmber
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
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
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import com.glowup.app.R
import com.glowup.app.ui.components.DetailHeader
import com.glowup.app.ui.components.goBack
import com.glowup.app.ui.data.ProGate
import com.glowup.app.ui.data.ProStore
import com.glowup.app.ui.data.fitness.Exercise
import com.glowup.app.ui.data.fitness.ExerciseLibrary
import com.glowup.app.ui.data.fitness.FitnessStore
import com.glowup.app.ui.data.fitness.Measure
import com.glowup.app.ui.data.fitness.Muscle
import com.glowup.app.ui.design.LineHeight
import com.glowup.app.ui.design.Shapes
import com.glowup.app.ui.design.Space
import com.glowup.app.ui.design.TextSize
import com.glowup.app.ui.theme.*

@Composable
fun ExerciseLibraryScreen(navController: NavHostController) {
    var muscle by remember { mutableStateOf<Muscle?>(null) }
    var query by remember { mutableStateOf("") }
    val list = ExerciseLibrary.all
        .filter { muscle == null || muscle in it.muscles }
        .filter { query.isBlank() || it.name.contains(query.trim(), ignoreCase = true) }
        .sortedWith(compareBy({ if (muscle != null && it.primary != muscle) 1 else 0 }, { it.level }, { it.name }))

    Box(Modifier.fillMaxSize().background(Surface).systemBarsPadding()) {
        Column(Modifier.fillMaxSize()) {
            DetailHeader(stringResource(R.string.exercise_exercises)) { navController.goBack() }
            OutlinedTextField(
                value = query,
                onValueChange = { query = it.take(30) },
                placeholder = { Text(stringResource(R.string.exercise_search_1_s_exercises, ExerciseLibrary.all.size)) },
                leadingIcon = { Icon(Icons.Filled.Search, contentDescription = null) },
                trailingIcon = if (query.isNotEmpty()) {
                    (
                        {
                            Icon(
                                Icons.Filled.Close,
                                contentDescription = stringResource(R.string.exercise_clear),
                                modifier = Modifier.clickable {
                                    query =
                                        ""
                                }
                            )
                        }
                        )
                } else {
                    null
                },
                singleLine = true,
                shape = Shapes.Pill,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = Ink,
                    unfocusedBorderColor = Color.Transparent,
                    unfocusedContainerColor = CardNeutral,
                    focusedContainerColor = CardNeutral
                ),
                modifier = Modifier.fillMaxWidth().padding(Space.Gutter, Space.L, Space.Gutter, 0.dp)
            )
            LazyRow(contentPadding = PaddingValues(Space.Gutter, Space.M), horizontalArrangement = Arrangement.spacedBy(Space.S)) {
                item { ChoicePill(stringResource(R.string.common_all), muscle == null) { muscle = null } }
                items(Muscle.entries) { m -> ChoicePill(m.label, muscle == m) { muscle = if (muscle == m) null else m } }
            }
            LazyColumn(
                Modifier.weight(1f),
                contentPadding = PaddingValues(Space.Gutter, 0.dp, Space.Gutter, Space.Gutter),
                verticalArrangement = Arrangement.spacedBy(Space.S)
            ) {
                items(list, key = { it.id }) { e -> ExerciseRow(e) { navController.navigate("exercise/${e.id}") } }
                if (list.isEmpty()) {
                    item {
                        Text(stringResource(R.string.exercise_no_exercises_match), color = TextSecondary, modifier = Modifier.padding(Space.S))
                    }
                }
            }
        }
    }
}

@Composable
private fun ExerciseRow(e: Exercise, onClick: () -> Unit) {
    Row(
        Modifier.fillMaxWidth().clip(Shapes.Card).background(CardNeutral).clickable(onClick = onClick).padding(Space.M),
        verticalAlignment = Alignment.CenterVertically
    ) {
        ExerciseArt(e, 54.dp, 14.dp)
        Spacer(Modifier.width(Space.M))
        Column(Modifier.weight(1f)) {
            Text(e.name, fontSize = TextSize.BodyLarge, fontWeight = FontWeight.SemiBold)
            Text(
                e.muscles.sortedBy {
                    if (it ==
                        e.primary
                    ) {
                        0
                    } else {
                        1
                    }
                }.joinToString(" · ") { it.label },
                color = TextSecondary,
                fontSize = TextSize.Caption,
                maxLines = 1
            )
            Spacer(Modifier.height(Space.Xs))
            Row(verticalAlignment = Alignment.CenterVertically) {
                LevelDots(e.level)
                Spacer(Modifier.width(Space.S))
                Text(
                    if (e.measure ==
                        Measure.REPS
                    ) {
                        stringResource(R.string.exercise_reps)
                    } else {
                        stringResource(R.string.exercise_timed)
                    },
                    color = TextSecondary,
                    fontSize = TextSize.Micro
                )
            }
        }
        FitnessStore.bestFor(e.id)?.let {
            Column(horizontalAlignment = Alignment.End) {
                Text("$it", fontSize = TextSize.Title, fontWeight = FontWeight.Bold, color = Green)
                Text(stringResource(R.string.exercise_best), color = TextSecondary, fontSize = TextSize.Micro)
            }
        }
    }
}

@Composable
fun ExerciseDetailScreen(navController: NavHostController, id: String) {
    val e = ExerciseLibrary.find(id) ?: return
    val history = FitnessStore.history(e.id)
    Box(Modifier.fillMaxSize().background(Surface).systemBarsPadding()) {
        Column(Modifier.fillMaxSize()) {
            DetailHeader(e.primary.label) { navController.goBack() }
            Column(Modifier.weight(1f).verticalScroll(rememberScrollState()).padding(Space.Gutter, Space.L, Space.Gutter, Space.Section)) {
                ExerciseArt(e, 240.dp, 28.dp, Modifier.fillMaxWidth())
                Spacer(Modifier.height(Space.L))
                Text(e.name, fontSize = TextSize.Display, fontWeight = FontWeight.Bold)
                Spacer(Modifier.height(Space.S))
                Row(horizontalArrangement = Arrangement.spacedBy(Space.S), verticalAlignment = Alignment.CenterVertically) {
                    e.muscles.sortedBy { if (it == e.primary) 0 else 1 }.forEach { MuscleChip(it) }
                    Spacer(Modifier.width(Space.Xs))
                    LevelDots(e.level)
                }
                Text(
                    listOf("Beginner", "Intermediate", "Advanced")[e.level - 1] + " · " +
                        (if (e.measure == Measure.REPS) stringResource(R.string.exercise_counted_in_reps) else stringResource(R.string.exercise_timed_2)) +
                        if (e.perSide) stringResource(R.string.exercise_each_side) else "",
                    color = TextSecondary,
                    fontSize = TextSize.BodySmall,
                    modifier = Modifier.padding(top = Space.S)
                )

                Section("How to do it")
                e.steps.forEachIndexed { i, step ->
                    Row(Modifier.padding(vertical = Space.Xs)) {
                        Box(Modifier.size(24.dp).clip(CircleShape).background(Ink), contentAlignment = Alignment.Center) {
                            Text("${i + 1}", color = Color.White, fontSize = TextSize.Caption, fontWeight = FontWeight.Bold)
                        }
                        Spacer(Modifier.width(Space.M))
                        Text(step, fontSize = TextSize.BodyLarge, lineHeight = LineHeight.Relaxed, modifier = Modifier.weight(1f))
                    }
                }

                Section("Common mistakes")
                e.mistakes.forEach { m ->
                    Row(Modifier.padding(vertical = Space.Xs), verticalAlignment = Alignment.Top) {
                        Icon(Icons.Filled.Close, contentDescription = null, tint = Coral, modifier = Modifier.padding(top = 2.dp).size(16.dp))
                        Spacer(Modifier.width(Space.S))
                        Text(m, fontSize = TextSize.Body, lineHeight = LineHeight.Body)
                    }
                }

                Spacer(Modifier.height(Space.L))
                Row(Modifier.fillMaxWidth().clip(Shapes.Medium).background(BlueTint).padding(Space.L, Space.M)) {
                    Icon(Icons.Filled.Air, contentDescription = null, tint = Navy, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(Space.S))
                    Text(e.cue, color = Navy, fontSize = TextSize.Body, lineHeight = LineHeight.Body)
                }
                e.safety?.let {
                    Spacer(Modifier.height(Space.S))
                    Row(Modifier.fillMaxWidth().clip(Shapes.Medium).background(MaroonTint).padding(Space.L, Space.M)) {
                        Icon(Icons.Outlined.WarningAmber, contentDescription = null, tint = Maroon, modifier = Modifier.size(18.dp))
                        Spacer(Modifier.width(Space.S))
                        Text(it, color = Maroon, fontSize = TextSize.Body, lineHeight = LineHeight.Body)
                    }
                }

                val variations = listOfNotNull(e.easier?.let { "Easier" to it }, e.harder?.let { "Harder" to it })
                if (variations.isNotEmpty()) {
                    Section("Variations")
                    variations.forEach { (label, vid) ->
                        val v = ExerciseLibrary.get(vid)
                        Row(
                            Modifier.fillMaxWidth().padding(bottom = Space.S).clip(Shapes.Medium).background(CardNeutral).clickable {
                                navController.navigate("exercise/$vid")
                            }.padding(Space.M),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            ExerciseArt(v, 44.dp, 12.dp)
                            Spacer(Modifier.width(Space.M))
                            Column(Modifier.weight(1f)) {
                                Text(label.uppercase(), color = TextSecondary, fontSize = TextSize.Micro, fontWeight = FontWeight.Bold, letterSpacing = 0.4.sp)
                                Text(v.name, fontSize = TextSize.BodyLarge, fontWeight = FontWeight.SemiBold)
                            }
                            Icon(Icons.AutoMirrored.Filled.ArrowForward, contentDescription = null, tint = TextSecondary, modifier = Modifier.size(18.dp))
                        }
                    }
                }

                Section("Your progress")
                if (history.isEmpty()) {
                    Text(stringResource(R.string.exercise_do_this_in_a_workout), color = TextSecondary, fontSize = TextSize.BodySmall)
                } else {
                    Row(verticalAlignment = Alignment.Bottom) {
                        Text("${history.maxOf { it.second }}", fontSize = TextSize.Display, fontWeight = FontWeight.Bold, color = Green)
                        Text(
                            if (e.measure ==
                                Measure.REPS
                            ) {
                                stringResource(R.string.exercise_reps_best)
                            } else {
                                stringResource(R.string.exercise_sec_best)
                            },
                            color = TextSecondary,
                            fontSize = TextSize.BodySmall,
                            modifier = Modifier.padding(bottom = Space.Xs)
                        )
                        Spacer(Modifier.weight(1f))
                        Text(
                            "${history.size} workout${if (history.size == 1) "" else "s"}",
                            color = TextSecondary,
                            fontSize = TextSize.Caption,
                            modifier = Modifier.padding(bottom = Space.Xs)
                        )
                    }
                    Spacer(Modifier.height(Space.S))
                    if (ProGate.progressionLocked) {
                        Row(
                            Modifier.fillMaxWidth().clip(Shapes.Medium).background(YellowTint).clickable {
                                ProStore.showPaywall("See your progress chart for every exercise")
                            }.padding(Space.L, Space.M),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(Icons.Filled.Lock, contentDescription = null, tint = Amber, modifier = Modifier.size(16.dp))
                            Spacer(Modifier.width(Space.S))
                            Text(stringResource(R.string.exercise_progress_charts_for_every_exercise), fontSize = TextSize.BodySmall, color = Amber)
                        }
                    } else {
                        HistoryChart(history.map { it.second }, e.primary.accent)
                    }
                }
            }
        }
    }
}

@Composable
private fun Section(title: String) {
    Spacer(Modifier.height(Space.Xl))
    Text(title, fontSize = TextSize.Title, fontWeight = FontWeight.Bold)
    Spacer(Modifier.height(Space.S))
}

/** Simple line of best sets, oldest to newest. */
@Composable
fun HistoryChart(values: List<Int>, color: Color) {
    val points = values.takeLast(20)
    Box(Modifier.fillMaxWidth().clip(Shapes.Card).background(CardNeutral).padding(Space.L)) {
        Canvas(Modifier.fillMaxWidth().height(110.dp)) {
            if (points.size == 1) {
                drawCircle(color, 5.dp.toPx(), Offset(size.width / 2, size.height / 2))
                return@Canvas
            }
            val min = points.min().toFloat()
            val max = points.max().toFloat().coerceAtLeast(min + 1)
            val step = size.width / (points.size - 1)
            val path = Path()
            points.forEachIndexed { i, v ->
                val x = i * step
                val y = size.height - (v - min) / (max - min) * (size.height * 0.85f) - size.height * 0.075f
                if (i == 0) path.moveTo(x, y) else path.lineTo(x, y)
                drawCircle(color, 4.dp.toPx(), Offset(x, y))
            }
            drawPath(path, color, style = Stroke(3.dp.toPx()))
        }
    }
}
