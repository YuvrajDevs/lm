package com.glowup.app.ui.screens.fitness

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.MonitorWeight
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material.icons.automirrored.outlined.MenuBook
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import com.glowup.app.ui.components.ProBadge
import com.glowup.app.ui.components.goBack
import com.glowup.app.ui.data.ProGate
import com.glowup.app.ui.data.ProStore
import com.glowup.app.ui.data.fitness.FitnessStore
import com.glowup.app.ui.data.fitness.Programme
import com.glowup.app.ui.data.fitness.Programmes
import com.glowup.app.ui.data.fitness.QuickFocus
import com.glowup.app.ui.data.fitness.QuickSessions
import com.glowup.app.ui.data.fitness.TrendTone
import com.glowup.app.ui.data.fitness.WorkoutPlan
import com.glowup.app.ui.theme.*
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.format.TextStyle
import java.time.temporal.TemporalAdjusters
import java.util.Locale
import kotlin.math.roundToInt
import com.glowup.app.ui.design.LineHeight
import com.glowup.app.ui.design.Shapes
import com.glowup.app.ui.design.Sizes
import com.glowup.app.ui.design.TextSize
import com.glowup.app.ui.design.SectionHeader
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.ui.text.style.TextAlign
import com.glowup.app.ui.data.fitness.ExerciseLibrary

@Composable
fun FitnessScreen(navController: NavHostController) {
    val today = LocalDate.now()
    Box(modifier = Modifier.fillMaxSize().background(Surface).systemBarsPadding()) {
        Column(modifier = Modifier.fillMaxSize()) {
            Row(modifier = Modifier.fillMaxWidth().padding(24.dp, 32.dp, 24.dp, 0.dp), verticalAlignment = Alignment.CenterVertically) {
                Box(Modifier.size(48.dp).clip(CircleShape).background(Ink).clickable { navController.goBack() }, contentAlignment = Alignment.Center) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = Color.White, modifier = Modifier.size(18.dp))
                }
                Text("Fitness", fontSize = TextSize.TitleLarge, fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f), textAlign = TextAlign.Center)
                if (FitnessStore.setUp) {
                    Box(Modifier.size(48.dp).clip(CircleShape).background(ChipNeutral).clickable { navController.navigate("fitness_setup") }, contentAlignment = Alignment.Center) {
                        Icon(Icons.Filled.Tune, contentDescription = "Edit plan", tint = Ink, modifier = Modifier.size(20.dp))
                    }
                } else Spacer(Modifier.size(48.dp))
            }

            Column(modifier = Modifier.weight(1f).verticalScroll(rememberScrollState()).padding(top = 18.dp, bottom = 32.dp)) {
                Column(Modifier.padding(horizontal = 24.dp)) {
                    if (!FitnessStore.setUp) {
                        SetupHero { navController.navigate("fitness_setup") }
                    } else {
                        TodayCard(navController, today)
                        Spacer(Modifier.height(12.dp))
                        WeekStrip(today)
                        Spacer(Modifier.height(12.dp))
                        StatsRow(today)
                        Spacer(Modifier.height(12.dp))
                        BodyCard(navController)
                    }
                }

                SectionTitle("30-day programmes", "Pick one and we'll line up every day for you")
                LazyRow(contentPadding = PaddingValues(horizontal = 24.dp), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    items(Programmes.all, key = { it.id }) { ProgrammeCard(it) { navController.navigate("programme/${it.id}") } }
                }

                SectionTitle("Quick sessions", "No plan needed — pick a focus and a length")
                QuickPicker(navController)

                Column(Modifier.padding(horizontal = 24.dp)) {
                    Spacer(Modifier.height(18.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth().clip(Shapes.Card).background(CardNeutral).clickable { navController.navigate("exercises") }.padding(18.dp, 16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Box(Modifier.size(42.dp).clip(CircleShape).background(Color.White), contentAlignment = Alignment.Center) {
                            Icon(Icons.AutoMirrored.Outlined.MenuBook, contentDescription = null, tint = Ink, modifier = Modifier.size(20.dp))
                        }
                        Spacer(Modifier.width(14.dp))
                        Column(Modifier.weight(1f)) {
                            Text("Exercise library", fontSize = TextSize.BodyLarge, fontWeight = FontWeight.Bold)
                            Text("${ExerciseLibrary.all.size} exercises · how-to, mistakes, easier & harder versions", color = TextSecondary, fontSize = TextSize.Caption, lineHeight = LineHeight.Tight)
                        }
                        Icon(Icons.Filled.ChevronRight, contentDescription = null, tint = TextSecondary)
                    }

                    if (FitnessStore.logs.isNotEmpty()) {
                        Spacer(Modifier.height(24.dp))
                        Text("Recent workouts", fontSize = TextSize.Title, fontWeight = FontWeight.Bold)
                        Spacer(Modifier.height(10.dp))
                        FitnessStore.logs.sortedByDescending { it.id }.take(5).forEach { log ->
                            Row(
                                modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp).clip(Shapes.Medium).background(CardNeutral).padding(16.dp, 12.dp),
                                verticalAlignment = Alignment.CenterVertically,
                            ) {
                                Column(Modifier.weight(1f)) {
                                    Text(log.title, fontSize = TextSize.Body, fontWeight = FontWeight.SemiBold, maxLines = 1)
                                    Text(
                                        log.date.format(DateTimeFormatter.ofPattern("EEE d MMM", Locale.getDefault())) + " · ${(log.seconds + 59) / 60} min",
                                        color = TextSecondary, fontSize = TextSize.Caption,
                                    )
                                }
                                CompletionBadge((log.completion * 100).roundToInt())
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun CompletionBadge(percent: Int) {
    val (bg, fg) = when {
        percent >= 90 -> GreenTint to Green
        percent >= 60 -> YellowTint to Amber
        else -> CoralTint to Coral
    }
    Text("$percent%", color = fg, fontSize = TextSize.BodySmall, fontWeight = FontWeight.Bold, modifier = Modifier.clip(Shapes.Pill).background(bg).padding(10.dp, 5.dp))
}

@Composable
private fun SectionTitle(title: String, subtitle: String) {
    SectionHeader(title, Modifier.padding(24.dp, 26.dp, 24.dp, 12.dp), subtitle)
}

@Composable
private fun SetupHero(onSetup: () -> Unit) {
    Column(Modifier.fillMaxWidth().clip(Shapes.Hero).background(Ink).padding(22.dp, 22.dp)) {
        Text("GET IN SHAPE AT HOME", color = Color.White.copy(alpha = 0.55f), fontSize = TextSize.Micro, fontWeight = FontWeight.SemiBold, letterSpacing = 0.5.sp)
        Spacer(Modifier.height(6.dp))
        Text("A plan that fits your goal, level and week", color = Color.White, fontSize = TextSize.TitleLarge, fontWeight = FontWeight.Bold, lineHeight = LineHeight.Title)
        Spacer(Modifier.height(14.dp))
        listOf(
            "${ExerciseLibrary.all.size} exercises with step-by-step cards",
            "30-day programmes, 5 to 30 minutes a day",
            "Score every set — targets adjust as you get stronger",
            "Weekly weigh-ins with a trend, not daily noise",
        ).forEach {
            Row(Modifier.padding(vertical = 3.dp), verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Filled.Check, contentDescription = null, tint = Mint, modifier = Modifier.size(16.dp))
                Spacer(Modifier.width(8.dp))
                Text(it, color = Color.White.copy(alpha = 0.85f), fontSize = TextSize.BodySmall)
            }
        }
        Spacer(Modifier.height(18.dp))
        Box(Modifier.fillMaxWidth().height(Sizes.Button).clip(Shapes.Pill).background(Color.White).clickable(onClick = onSetup), contentAlignment = Alignment.Center) {
            Text("Build my plan · 1 minute", color = Ink, fontSize = TextSize.BodyLarge, fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
private fun TodayCard(navController: NavHostController, today: LocalDate) {
    val plan = FitnessStore.todayPlan(today)
    val programme = FitnessStore.activeProgramme?.let(Programmes::get)
    val done = FitnessStore.doneOn(today)
    val trainingDay = FitnessStore.isTrainingDay(today) || programme != null
    val tint = programme?.focus?.tint ?: GreenTint
    val accent = programme?.focus?.accent ?: Green
    Column(Modifier.fillMaxWidth().clip(Shapes.Hero).background(tint).padding(20.dp, 18.dp)) {
        Text(
            (if (done) "DONE TODAY · " else if (trainingDay) "TODAY · " else "OPTIONAL TODAY · ") + (programme?.title?.uppercase() ?: "QUICK SESSION"),
            color = accent, fontSize = TextSize.Micro, fontWeight = FontWeight.Bold, letterSpacing = 0.4.sp,
        )
        Spacer(Modifier.height(4.dp))
        Text(plan.title, fontSize = TextSize.TitleLarge, fontWeight = FontWeight.Bold, lineHeight = LineHeight.Title)
        Text(
            if (plan.restDay) plan.subtitle else "${plan.minutes} min · ${plan.blocks.size} exercises · ${plan.subtitle}",
            color = TextSecondary, fontSize = TextSize.BodySmall, modifier = Modifier.padding(top = 2.dp),
        )
        if (!plan.restDay) {
            Spacer(Modifier.height(10.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) { plan.muscles.take(3).forEach { MuscleChip(it) } }
        }
        Spacer(Modifier.height(14.dp))
        Row(verticalAlignment = Alignment.CenterVertically) {
            if (plan.restDay) {
                Pill("Rest day done", Ink, Color.White) {
                    val day = plan.key.split(".").last().toInt()
                    FitnessStore.completeDay(programme!!.id, day, today)
                }
                Spacer(Modifier.width(8.dp))
                Pill("5-min stretch", Color.White, Ink) { navController.navigate("workout/${QuickSessions.key(QuickFocus.MOBILITY, 5)}") }
            } else {
                Row(
                    modifier = Modifier.clip(Shapes.Pill).background(Ink).clickable { navController.navigate("workout/${plan.key}") }.padding(18.dp, 11.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Icon(Icons.Filled.PlayArrow, contentDescription = null, tint = Color.White, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(6.dp))
                    Text(if (done && programme != null) "Start day ${plan.key.split(".").last()} early" else if (done) "Train again" else "Start workout", color = Color.White, fontSize = TextSize.Body, fontWeight = FontWeight.SemiBold)
                }
                if (programme != null) {
                    Spacer(Modifier.width(10.dp))
                    val doneDays = FitnessStore.daysDone(programme.id).size
                    Text("$doneDays of ${programme.days} days", color = accent, fontSize = TextSize.BodySmall, fontWeight = FontWeight.SemiBold)
                }
            }
        }
    }
}

@Composable
private fun Pill(text: String, bg: Color, fg: Color, onClick: () -> Unit) {
    Text(text, color = fg, fontSize = TextSize.BodySmall, fontWeight = FontWeight.SemiBold, modifier = Modifier.clip(Shapes.Pill).background(bg).clickable(onClick = onClick).padding(16.dp, 10.dp))
}

@Composable
private fun WeekStrip(today: LocalDate) {
    val monday = today.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY))
    val done = FitnessStore.workoutDates()
    Row(Modifier.fillMaxWidth().clip(Shapes.Card).background(CardNeutral).padding(12.dp, 12.dp), horizontalArrangement = Arrangement.SpaceBetween) {
        (0..6).forEach { i ->
            val date = monday.plusDays(i.toLong())
            val isDone = date in done
            val planned = date.dayOfWeek in FitnessStore.days
            Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.weight(1f)) {
                Text(date.dayOfWeek.getDisplayName(TextStyle.NARROW, Locale.getDefault()), color = if (date == today) Ink else TextSecondary, fontSize = TextSize.Micro, fontWeight = if (date == today) FontWeight.Bold else FontWeight.Medium)
                Spacer(Modifier.height(6.dp))
                Box(
                    Modifier
                        .size(30.dp)
                        .clip(CircleShape)
                        .background(if (isDone) Green else Color.White)
                        .then(if (planned && !isDone) Modifier.border(2.dp, if (date == today) Ink else TextMuted, CircleShape) else Modifier),
                    contentAlignment = Alignment.Center,
                ) {
                    if (isDone) Icon(Icons.Filled.Check, contentDescription = "Done", tint = Color.White, modifier = Modifier.size(16.dp))
                }
            }
        }
    }
}

@Composable
private fun StatsRow(today: LocalDate) {
    val week = FitnessStore.thisWeek(today).map { it.date }.distinct().size
    Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
        Stat("This week", "$week/${FitnessStore.days.size}", "workouts", Modifier.weight(1f))
        Stat("Week streak", "${FitnessStore.weekStreak(today)}", "on target", Modifier.weight(1f))
        Stat("All time", "${FitnessStore.logs.size}", "workouts", Modifier.weight(1f))
    }
}

@Composable
private fun Stat(label: String, value: String, sub: String, modifier: Modifier) {
    Column(modifier.clip(Shapes.Card).background(CardNeutral).padding(14.dp, 12.dp)) {
        Text(label, color = TextSecondary, fontSize = TextSize.Micro, fontWeight = FontWeight.Medium)
        Text(value, fontSize = TextSize.TitleLarge, fontWeight = FontWeight.Bold)
        Text(sub, color = TextSecondary, fontSize = TextSize.Micro)
    }
}

@Composable
private fun BodyCard(navController: NavHostController) {
    val latest = FitnessStore.latestBody()
    val trend = FitnessStore.weightTrend()
    val due = FitnessStore.weighInDue()
    Row(
        modifier = Modifier.fillMaxWidth().clip(Shapes.Card).background(PurpleTint).clickable { navController.navigate("body_checkin") }.padding(18.dp, 16.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(Modifier.size(42.dp).clip(CircleShape).background(Color.White), contentAlignment = Alignment.Center) {
            Icon(Icons.Filled.MonitorWeight, contentDescription = null, tint = Purple, modifier = Modifier.size(20.dp))
        }
        Spacer(Modifier.width(14.dp))
        Column(Modifier.weight(1f)) {
            Text(
                when {
                    latest == null -> "Track your body"
                    trend != null -> FitnessStore.displayWeight(latest.weightKg) + " · " + FitnessStore.displayChange(trend.changeKg) + " in ${trend.weeks.roundToInt().coerceAtLeast(1)} wk"
                    else -> FitnessStore.displayWeight(latest.weightKg)
                },
                fontSize = TextSize.BodyLarge, fontWeight = FontWeight.Bold,
            )
            Text(
                when {
                    latest == null -> "A weekly weigh-in shows if your plan is working"
                    due -> "Weekly check-in is due"
                    trend != null -> trend.message
                    else -> "Weigh in again next week to see your trend"
                },
                color = if (trend?.tone == TrendTone.CAUTION) Maroon else PurpleDeep, fontSize = TextSize.Caption, lineHeight = LineHeight.Tight, maxLines = 2,
            )
        }
        if (due) Text("Due", color = Color.White, fontSize = TextSize.Micro, fontWeight = FontWeight.Bold, modifier = Modifier.clip(Shapes.Pill).background(Purple).padding(9.dp, 4.dp))
        else Icon(Icons.Filled.ChevronRight, contentDescription = null, tint = Purple)
    }
}

@Composable
private fun ProgrammeCard(programme: Programme, onClick: () -> Unit) {
    val done = FitnessStore.daysDone(programme.id).size
    val active = FitnessStore.activeProgramme == programme.id
    Column(
        modifier = Modifier.width(210.dp).clip(Shapes.Large).background(programme.focus.tint)
            .then(if (active) Modifier.border(2.dp, Ink, Shapes.Large) else Modifier)
            .clickable(onClick = onClick).padding(16.dp),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(Modifier.size(38.dp).clip(CircleShape).background(Color.White), contentAlignment = Alignment.Center) {
                Icon(programme.focus.icon(), contentDescription = null, tint = programme.focus.accent, modifier = Modifier.size(20.dp))
            }
            Spacer(Modifier.weight(1f))
            if (ProGate.programmeLocked(programme.id)) ProBadge()
            else if (active) Text("ACTIVE", color = programme.focus.accent, fontSize = TextSize.Micro, fontWeight = FontWeight.Bold, letterSpacing = 0.5.sp)
        }
        Spacer(Modifier.height(12.dp))
        Text(programme.title, fontSize = TextSize.Title, fontWeight = FontWeight.Bold)
        Text(programme.tagline, color = TextSecondary, fontSize = TextSize.Caption, maxLines = 1)
        Spacer(Modifier.height(12.dp))
        Box(Modifier.fillMaxWidth().height(6.dp).clip(Shapes.Pill).background(Color.White)) {
            Box(Modifier.fillMaxWidth(done / programme.days.toFloat()).fillMaxHeight().clip(Shapes.Pill).background(programme.focus.accent))
        }
        Text(if (done == 0) "30 days" else "Day $done of ${programme.days}", color = programme.focus.accent, fontSize = TextSize.Caption, fontWeight = FontWeight.SemiBold, modifier = Modifier.padding(top = 6.dp))
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun QuickPicker(navController: NavHostController) {
    var focus by remember { mutableStateOf(QuickSessions.forGoal(FitnessStore.goal, LocalDate.now())) }
    var minutes by remember { mutableStateOf(FitnessStore.minutes.coerceAtMost(if (ProStore.isPro) 30 else 10)) }
    LazyRow(contentPadding = PaddingValues(horizontal = 24.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        items(QuickFocus.entries) { f -> ChoicePill(f.label, f == focus) { focus = f } }
    }
    Spacer(Modifier.height(8.dp))
    LazyRow(contentPadding = PaddingValues(horizontal = 24.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        items(QuickSessions.minuteOptions) { m ->
            ChoicePill("$m min", m == minutes, trailing = if (ProGate.quickLocked(m)) ({ Text("PRO", color = Amber, fontSize = TextSize.Micro, fontWeight = FontWeight.Bold) }) else null) {
                if (ProGate.quickLocked(m)) ProStore.showPaywall("Longer sessions are part of Pro") else minutes = m
            }
        }
    }
    Spacer(Modifier.height(12.dp))
    val plan: WorkoutPlan = remember(focus, minutes, FitnessStore.level) { FitnessStore.planFor(QuickSessions.key(focus, minutes))!! }
    Row(
        modifier = Modifier.padding(horizontal = 24.dp).fillMaxWidth().clip(Shapes.Card).background(Ink).clickable { navController.navigate("workout/${plan.key}") }.padding(18.dp, 14.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(Modifier.weight(1f)) {
            Text(plan.title, color = Color.White, fontSize = TextSize.BodyLarge, fontWeight = FontWeight.Bold)
            Text(plan.blocks.joinToString(" · ") { it.exercise.name }, color = Color.White.copy(alpha = 0.6f), fontSize = TextSize.Caption, maxLines = 1)
        }
        Box(Modifier.size(40.dp).clip(CircleShape).background(Color.White), contentAlignment = Alignment.Center) {
            Icon(Icons.Filled.PlayArrow, contentDescription = "Start", tint = Ink)
        }
    }
}
