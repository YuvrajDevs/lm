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
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import com.glowup.app.ui.components.DetailHeader
import com.glowup.app.ui.components.goBack
import com.glowup.app.ui.data.ProGate
import com.glowup.app.ui.data.ProStore
import com.glowup.app.ui.data.fitness.Exercise
import com.glowup.app.ui.data.fitness.ExerciseLibrary
import com.glowup.app.ui.data.fitness.FitnessStore
import com.glowup.app.ui.data.fitness.Measure
import com.glowup.app.ui.data.fitness.Muscle
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
            DetailHeader("Exercises") { navController.goBack() }
            OutlinedTextField(
                value = query,
                onValueChange = { query = it.take(30) },
                placeholder = { Text("Search ${ExerciseLibrary.all.size} exercises") },
                leadingIcon = { Icon(Icons.Filled.Search, contentDescription = null) },
                trailingIcon = if (query.isNotEmpty()) ({ Icon(Icons.Filled.Close, contentDescription = "Clear", modifier = Modifier.clickable { query = "" }) }) else null,
                singleLine = true,
                shape = RoundedCornerShape(999.dp),
                colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = Ink, unfocusedBorderColor = Color.Transparent, unfocusedContainerColor = CardNeutral, focusedContainerColor = CardNeutral),
                modifier = Modifier.fillMaxWidth().padding(24.dp, 16.dp, 24.dp, 0.dp),
            )
            LazyRow(contentPadding = PaddingValues(24.dp, 12.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                item { ChoicePill("All", muscle == null) { muscle = null } }
                items(Muscle.entries) { m -> ChoicePill(m.label, muscle == m) { muscle = if (muscle == m) null else m } }
            }
            LazyColumn(Modifier.weight(1f), contentPadding = PaddingValues(24.dp, 0.dp, 24.dp, 24.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                items(list, key = { it.id }) { e -> ExerciseRow(e) { navController.navigate("exercise/${e.id}") } }
                if (list.isEmpty()) item { Text("No exercises match.", color = TextSecondary, modifier = Modifier.padding(8.dp)) }
            }
        }
    }
}

@Composable
private fun ExerciseRow(e: Exercise, onClick: () -> Unit) {
    Row(Modifier.fillMaxWidth().clip(RoundedCornerShape(20.dp)).background(CardNeutral).clickable(onClick = onClick).padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
        ExerciseArt(e, 54.dp, 14.dp)
        Spacer(Modifier.width(14.dp))
        Column(Modifier.weight(1f)) {
            Text(e.name, fontSize = 15.sp, fontWeight = FontWeight.SemiBold)
            Text(e.muscles.sortedBy { if (it == e.primary) 0 else 1 }.joinToString(" · ") { it.label }, color = TextSecondary, fontSize = 12.sp, maxLines = 1)
            Spacer(Modifier.height(4.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                LevelDots(e.level)
                Spacer(Modifier.width(8.dp))
                Text(if (e.measure == Measure.REPS) "Reps" else "Timed", color = TextSecondary, fontSize = 11.sp)
            }
        }
        FitnessStore.bestFor(e.id)?.let {
            Column(horizontalAlignment = Alignment.End) {
                Text("$it", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Green)
                Text("best", color = TextSecondary, fontSize = 10.sp)
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
            Column(Modifier.weight(1f).verticalScroll(rememberScrollState()).padding(24.dp, 18.dp, 24.dp, 28.dp)) {
                ExerciseArt(e, 240.dp, 28.dp, Modifier.fillMaxWidth())
                Spacer(Modifier.height(16.dp))
                Text(e.name, fontSize = 26.sp, fontWeight = FontWeight.Bold)
                Spacer(Modifier.height(8.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalAlignment = Alignment.CenterVertically) {
                    e.muscles.sortedBy { if (it == e.primary) 0 else 1 }.forEach { MuscleChip(it) }
                    Spacer(Modifier.width(4.dp))
                    LevelDots(e.level)
                }
                Text(
                    listOf("Beginner", "Intermediate", "Advanced")[e.level - 1] + " · " + (if (e.measure == Measure.REPS) "counted in reps" else "timed") + if (e.perSide) " · each side" else "",
                    color = TextSecondary, fontSize = 12.5.sp, modifier = Modifier.padding(top = 6.dp),
                )

                Section("How to do it")
                e.steps.forEachIndexed { i, step ->
                    Row(Modifier.padding(vertical = 5.dp)) {
                        Box(Modifier.size(24.dp).clip(CircleShape).background(Ink), contentAlignment = Alignment.Center) {
                            Text("${i + 1}", color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }
                        Spacer(Modifier.width(12.dp))
                        Text(step, fontSize = 14.5.sp, lineHeight = 21.sp, modifier = Modifier.weight(1f))
                    }
                }

                Section("Common mistakes")
                e.mistakes.forEach { m ->
                    Row(Modifier.padding(vertical = 4.dp), verticalAlignment = Alignment.Top) {
                        Icon(Icons.Filled.Close, contentDescription = null, tint = Coral, modifier = Modifier.padding(top = 2.dp).size(16.dp))
                        Spacer(Modifier.width(10.dp))
                        Text(m, fontSize = 14.sp, lineHeight = 20.sp)
                    }
                }

                Spacer(Modifier.height(16.dp))
                Row(Modifier.fillMaxWidth().clip(RoundedCornerShape(18.dp)).background(BlueTint).padding(16.dp, 14.dp)) {
                    Icon(Icons.Filled.Air, contentDescription = null, tint = Navy, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(10.dp))
                    Text(e.cue, color = Navy, fontSize = 13.5.sp, lineHeight = 19.sp)
                }
                e.safety?.let {
                    Spacer(Modifier.height(10.dp))
                    Row(Modifier.fillMaxWidth().clip(RoundedCornerShape(18.dp)).background(MaroonTint).padding(16.dp, 14.dp)) {
                        Icon(Icons.Outlined.WarningAmber, contentDescription = null, tint = Maroon, modifier = Modifier.size(18.dp))
                        Spacer(Modifier.width(10.dp))
                        Text(it, color = Maroon, fontSize = 13.5.sp, lineHeight = 19.sp)
                    }
                }

                val variations = listOfNotNull(e.easier?.let { "Easier" to it }, e.harder?.let { "Harder" to it })
                if (variations.isNotEmpty()) {
                    Section("Variations")
                    variations.forEach { (label, vid) ->
                        val v = ExerciseLibrary.get(vid)
                        Row(
                            Modifier.fillMaxWidth().padding(bottom = 8.dp).clip(RoundedCornerShape(18.dp)).background(CardNeutral).clickable { navController.navigate("exercise/$vid") }.padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            ExerciseArt(v, 44.dp, 12.dp)
                            Spacer(Modifier.width(12.dp))
                            Column(Modifier.weight(1f)) {
                                Text(label.uppercase(), color = TextSecondary, fontSize = 10.5.sp, fontWeight = FontWeight.Bold, letterSpacing = 0.4.sp)
                                Text(v.name, fontSize = 14.5.sp, fontWeight = FontWeight.SemiBold)
                            }
                            Icon(Icons.AutoMirrored.Filled.ArrowForward, contentDescription = null, tint = TextSecondary, modifier = Modifier.size(18.dp))
                        }
                    }
                }

                Section("Your progress")
                if (history.isEmpty()) {
                    Text("Do this in a workout and your best sets will chart here.", color = TextSecondary, fontSize = 13.sp)
                } else {
                    Row(verticalAlignment = Alignment.Bottom) {
                        Text("${history.maxOf { it.second }}", fontSize = 28.sp, fontWeight = FontWeight.Bold, color = Green)
                        Text(if (e.measure == Measure.REPS) " reps best" else " sec best", color = TextSecondary, fontSize = 13.sp, modifier = Modifier.padding(bottom = 5.dp))
                        Spacer(Modifier.weight(1f))
                        Text("${history.size} workout${if (history.size == 1) "" else "s"}", color = TextSecondary, fontSize = 12.sp, modifier = Modifier.padding(bottom = 5.dp))
                    }
                    Spacer(Modifier.height(10.dp))
                    if (ProGate.progressionLocked) {
                        Row(
                            Modifier.fillMaxWidth().clip(RoundedCornerShape(18.dp)).background(YellowTint).clickable { ProStore.showPaywall("See your progress chart for every exercise") }.padding(16.dp, 14.dp),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Icon(Icons.Filled.Lock, contentDescription = null, tint = Color(0xFF8A6A00), modifier = Modifier.size(16.dp))
                            Spacer(Modifier.width(10.dp))
                            Text("Progress charts for every exercise come with Pro", fontSize = 13.sp, color = Color(0xFF7A6600))
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
    Spacer(Modifier.height(22.dp))
    Text(title, fontSize = 17.sp, fontWeight = FontWeight.Bold)
    Spacer(Modifier.height(8.dp))
}

/** Simple line of best sets, oldest to newest. */
@Composable
fun HistoryChart(values: List<Int>, color: Color) {
    val points = values.takeLast(20)
    Box(Modifier.fillMaxWidth().clip(RoundedCornerShape(20.dp)).background(CardNeutral).padding(16.dp)) {
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
