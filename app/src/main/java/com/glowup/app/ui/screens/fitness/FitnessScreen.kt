package com.glowup.app.ui.screens.fitness

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.outlined.MenuBook
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.MonitorWeight
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Tune
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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import com.glowup.app.R
import com.glowup.app.ui.components.ProBadge
import com.glowup.app.ui.components.goBack
import com.glowup.app.ui.data.ProGate
import com.glowup.app.ui.data.ProStore
import com.glowup.app.ui.data.fitness.ExerciseLibrary
import com.glowup.app.ui.data.fitness.FitnessStore
import com.glowup.app.ui.data.fitness.Programme
import com.glowup.app.ui.data.fitness.Programmes
import com.glowup.app.ui.data.fitness.QuickFocus
import com.glowup.app.ui.data.fitness.QuickSessions
import com.glowup.app.ui.data.fitness.TrendTone
import com.glowup.app.ui.data.fitness.WorkoutPlan
import com.glowup.app.ui.design.AppButton
import com.glowup.app.ui.design.ButtonVariant
import com.glowup.app.ui.design.LineHeight
import com.glowup.app.ui.design.SectionHeader
import com.glowup.app.ui.design.Shapes
import com.glowup.app.ui.design.Space
import com.glowup.app.ui.design.TextSize
import com.glowup.app.ui.theme.*
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.format.TextStyle
import java.time.temporal.TemporalAdjusters
import java.util.Locale
import kotlin.math.roundToInt

@Composable
fun FitnessScreen(navController: NavHostController) {
    val today = LocalDate.now()
    Box(modifier = Modifier.fillMaxSize().background(Surface).systemBarsPadding()) {
        Column(modifier = Modifier.fillMaxSize()) {
            Row(modifier = Modifier.fillMaxWidth().padding(Space.Gutter, Space.Xxl, Space.Gutter, 0.dp), verticalAlignment = Alignment.CenterVertically) {
                Box(Modifier.size(48.dp).clip(CircleShape).background(Ink).clickable { navController.goBack() }, contentAlignment = Alignment.Center) {
                    Icon(
                        Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = stringResource(R.string.common_back),
                        tint = Color.White,
                        modifier = Modifier.size(18.dp)
                    )
                }
                Text(
                    stringResource(R.string.fitness_fitness),
                    fontSize = TextSize.TitleLarge,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.weight(1f),
                    textAlign = TextAlign.Center
                )
                if (FitnessStore.setUp) {
                    Box(
                        Modifier.size(48.dp).clip(CircleShape).background(ChipNeutral).clickable {
                            navController.navigate("fitness_setup")
                        },
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Filled.Tune, contentDescription = stringResource(R.string.fitness_edit_plan), tint = Ink, modifier = Modifier.size(20.dp))
                    }
                } else {
                    Spacer(Modifier.size(48.dp))
                }
            }

            Column(modifier = Modifier.weight(1f).verticalScroll(rememberScrollState()).padding(top = Space.L, bottom = Space.Xxl)) {
                Column(Modifier.padding(horizontal = Space.Gutter)) {
                    if (!FitnessStore.setUp) {
                        SetupHero { navController.navigate("fitness_setup") }
                    } else {
                        TodayCard(navController, today)
                        Spacer(Modifier.height(Space.M))
                        WeekStrip(today)
                        Spacer(Modifier.height(Space.M))
                        StatsRow(today)
                        Spacer(Modifier.height(Space.M))
                        BodyCard(navController)
                    }
                }

                SectionTitle("30-day programmes", "Pick one and we'll line up every day for you")
                LazyRow(contentPadding = PaddingValues(horizontal = Space.Gutter), horizontalArrangement = Arrangement.spacedBy(Space.S)) {
                    items(Programmes.all, key = { it.id }) { ProgrammeCard(it) { navController.navigate("programme/${it.id}") } }
                }

                SectionTitle("Quick sessions", "No plan needed — pick a focus and a length")
                QuickPicker(navController)

                Column(Modifier.padding(horizontal = Space.Gutter)) {
                    Spacer(Modifier.height(Space.L))
                    Row(
                        modifier = Modifier.fillMaxWidth().clip(Shapes.Card).background(CardNeutral).clickable {
                            navController.navigate("exercises")
                        }.padding(Space.L, Space.L),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(Modifier.size(42.dp).clip(CircleShape).background(Color.White), contentAlignment = Alignment.Center) {
                            Icon(Icons.AutoMirrored.Outlined.MenuBook, contentDescription = null, tint = Ink, modifier = Modifier.size(20.dp))
                        }
                        Spacer(Modifier.width(Space.M))
                        Column(Modifier.weight(1f)) {
                            Text(stringResource(R.string.fitness_exercise_library), fontSize = TextSize.BodyLarge, fontWeight = FontWeight.Bold)
                            Text(
                                stringResource(R.string.fitness_1_s_exercises_how_to, ExerciseLibrary.all.size),
                                color = TextSecondary,
                                fontSize = TextSize.Caption,
                                lineHeight = LineHeight.Tight
                            )
                        }
                        Icon(Icons.Filled.ChevronRight, contentDescription = null, tint = TextSecondary)
                    }

                    if (FitnessStore.logs.isNotEmpty()) {
                        Spacer(Modifier.height(Space.Gutter))
                        Text(stringResource(R.string.fitness_recent_workouts), fontSize = TextSize.Title, fontWeight = FontWeight.Bold)
                        Spacer(Modifier.height(Space.S))
                        FitnessStore.logs.sortedByDescending { it.id }.take(5).forEach { log ->
                            Row(
                                modifier = Modifier.fillMaxWidth().padding(
                                    bottom = Space.S
                                ).clip(Shapes.Medium).background(CardNeutral).padding(Space.L, Space.M),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(Modifier.weight(1f)) {
                                    Text(log.title, fontSize = TextSize.Body, fontWeight = FontWeight.SemiBold, maxLines = 1)
                                    Text(
                                        log.date.format(DateTimeFormatter.ofPattern("EEE d MMM", Locale.getDefault())) + " · ${(log.seconds + 59) / 60} min",
                                        color = TextSecondary,
                                        fontSize = TextSize.Caption
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
    Text(
        "$percent%",
        color = fg,
        fontSize = TextSize.BodySmall,
        fontWeight = FontWeight.Bold,
        modifier = Modifier.clip(Shapes.Pill).background(bg).padding(Space.S, Space.Xs)
    )
}

@Composable
private fun SectionTitle(title: String, subtitle: String) {
    SectionHeader(title, Modifier.padding(Space.Gutter, Space.Gutter, Space.Gutter, Space.M), subtitle)
}

@Composable
private fun SetupHero(onSetup: () -> Unit) {
    Column(Modifier.fillMaxWidth().clip(Shapes.Hero).background(Ink).padding(Space.Xl, Space.Xl)) {
        Text(
            stringResource(R.string.fitness_get_in_shape_at_home),
            color = Color.White.copy(alpha = 0.55f),
            fontSize = TextSize.Micro,
            fontWeight = FontWeight.SemiBold,
            letterSpacing = 0.5.sp
        )
        Spacer(Modifier.height(Space.S))
        Text(
            stringResource(R.string.fitness_a_plan_that_fits_your),
            color = Color.White,
            fontSize = TextSize.TitleLarge,
            fontWeight = FontWeight.Bold,
            lineHeight = LineHeight.Title
        )
        Spacer(Modifier.height(Space.M))
        listOf(
            "${ExerciseLibrary.all.size} exercises with step-by-step cards",
            "30-day programmes, 5 to 30 minutes a day",
            "Score every set — targets adjust as you get stronger",
            "Weekly weigh-ins with a trend, not daily noise"
        ).forEach {
            Row(Modifier.padding(vertical = Space.Xs), verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Filled.Check, contentDescription = null, tint = Mint, modifier = Modifier.size(16.dp))
                Spacer(Modifier.width(Space.S))
                Text(it, color = Color.White.copy(alpha = 0.85f), fontSize = TextSize.BodySmall)
            }
        }
        Spacer(Modifier.height(Space.L))
        AppButton(stringResource(R.string.fitness_build_my_plan_1_minute), variant = ButtonVariant.OnDarkPrimary, onClick = onSetup)
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
    Column(Modifier.fillMaxWidth().clip(Shapes.Hero).background(tint).padding(Space.Xl, Space.L)) {
        Text(
            (
                if (done) {
                    stringResource(R.string.fitness_done_today)
                } else if (trainingDay) {
                    stringResource(R.string.fitness_today)
                } else {
                    stringResource(R.string.fitness_optional_today)
                }
                ) +
                (programme?.title?.uppercase() ?: stringResource(R.string.fitness_quick_session)),
            color = accent,
            fontSize = TextSize.Micro,
            fontWeight = FontWeight.Bold,
            letterSpacing = 0.4.sp
        )
        Spacer(Modifier.height(Space.Xs))
        Text(plan.title, fontSize = TextSize.TitleLarge, fontWeight = FontWeight.Bold, lineHeight = LineHeight.Title)
        Text(
            if (plan.restDay) plan.subtitle else stringResource(R.string.fitness_1_s_min_2_s, plan.minutes, plan.blocks.size, plan.subtitle),
            color = TextSecondary,
            fontSize = TextSize.BodySmall,
            modifier = Modifier.padding(top = 2.dp)
        )
        if (!plan.restDay) {
            Spacer(Modifier.height(Space.S))
            Row(horizontalArrangement = Arrangement.spacedBy(Space.S)) { plan.muscles.take(3).forEach { MuscleChip(it) } }
        }
        Spacer(Modifier.height(Space.M))
        Row(verticalAlignment = Alignment.CenterVertically) {
            if (plan.restDay) {
                Pill("Rest day done", Ink, Color.White) {
                    val day = plan.key.split(".").last().toInt()
                    FitnessStore.completeDay(programme!!.id, day, today)
                }
                Spacer(Modifier.width(Space.S))
                Pill("5-min stretch", Color.White, Ink) { navController.navigate("workout/${QuickSessions.key(QuickFocus.MOBILITY, 5)}") }
            } else {
                Row(
                    modifier = Modifier.clip(Shapes.Pill).background(Ink).clickable { navController.navigate("workout/${plan.key}") }.padding(Space.L, Space.M),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Filled.PlayArrow, contentDescription = null, tint = Color.White, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(Space.S))
                    Text(
                        if (done &&
                            programme != null
                        ) {
                            "Start day ${plan.key.split(".").last()} early"
                        } else if (done) {
                            "Train again"
                        } else {
                            "Start workout"
                        },
                        color = Color.White,
                        fontSize = TextSize.Body,
                        fontWeight = FontWeight.SemiBold
                    )
                }
                if (programme != null) {
                    Spacer(Modifier.width(Space.S))
                    val doneDays = FitnessStore.daysDone(programme.id).size
                    Text(
                        stringResource(R.string.fitness_1_s_of_2_s, doneDays, programme.days),
                        color = accent,
                        fontSize = TextSize.BodySmall,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
        }
    }
}

@Composable
private fun Pill(text: String, bg: Color, fg: Color, onClick: () -> Unit) {
    Text(
        text,
        color = fg,
        fontSize = TextSize.BodySmall,
        fontWeight = FontWeight.SemiBold,
        modifier = Modifier.clip(Shapes.Pill).background(bg).clickable(onClick = onClick).padding(Space.L, Space.S)
    )
}

@Composable
private fun WeekStrip(today: LocalDate) {
    val monday = today.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY))
    val done = FitnessStore.workoutDates()
    Row(Modifier.fillMaxWidth().clip(Shapes.Card).background(CardNeutral).padding(Space.M, Space.M), horizontalArrangement = Arrangement.SpaceBetween) {
        (0..6).forEach { i ->
            val date = monday.plusDays(i.toLong())
            val isDone = date in done
            val planned = date.dayOfWeek in FitnessStore.days
            Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.weight(1f)) {
                Text(
                    date.dayOfWeek.getDisplayName(TextStyle.NARROW, Locale.getDefault()),
                    color = if (date ==
                        today
                    ) {
                        Ink
                    } else {
                        TextSecondary
                    },
                    fontSize = TextSize.Micro,
                    fontWeight = if (date == today) FontWeight.Bold else FontWeight.Medium
                )
                Spacer(Modifier.height(Space.S))
                Box(
                    Modifier
                        .size(30.dp)
                        .clip(CircleShape)
                        .background(if (isDone) Green else Color.White)
                        .then(if (planned && !isDone) Modifier.border(2.dp, if (date == today) Ink else TextMuted, CircleShape) else Modifier),
                    contentAlignment = Alignment.Center
                ) {
                    if (isDone) {
                        Icon(
                            Icons.Filled.Check,
                            contentDescription = stringResource(R.string.common_done),
                            tint = Color.White,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun StatsRow(today: LocalDate) {
    val week = FitnessStore.thisWeek(today).map { it.date }.distinct().size
    Row(horizontalArrangement = Arrangement.spacedBy(Space.S)) {
        Stat("This week", "$week/${FitnessStore.days.size}", "workouts", Modifier.weight(1f))
        Stat("Week streak", "${FitnessStore.weekStreak(today)}", "on target", Modifier.weight(1f))
        Stat("All time", "${FitnessStore.logs.size}", "workouts", Modifier.weight(1f))
    }
}

@Composable
private fun Stat(label: String, value: String, sub: String, modifier: Modifier) {
    Column(modifier.clip(Shapes.Card).background(CardNeutral).padding(Space.M, Space.M)) {
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
        modifier = Modifier.fillMaxWidth().clip(Shapes.Card).background(PurpleTint).clickable {
            navController.navigate("body_checkin")
        }.padding(Space.L, Space.L),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(Modifier.size(42.dp).clip(CircleShape).background(Color.White), contentAlignment = Alignment.Center) {
            Icon(Icons.Filled.MonitorWeight, contentDescription = null, tint = Purple, modifier = Modifier.size(20.dp))
        }
        Spacer(Modifier.width(Space.M))
        Column(Modifier.weight(1f)) {
            Text(
                when {
                    latest == null -> stringResource(R.string.fitness_track_your_body)
                    trend != null -> FitnessStore.displayWeight(latest.weightKg) + " · " + FitnessStore.displayChange(trend.changeKg) +
                        " in ${trend.weeks.roundToInt().coerceAtLeast(1)} wk"
                    else -> FitnessStore.displayWeight(latest.weightKg)
                },
                fontSize = TextSize.BodyLarge,
                fontWeight = FontWeight.Bold
            )
            Text(
                when {
                    latest == null -> stringResource(R.string.fitness_a_weekly_weigh_in_shows)
                    due -> stringResource(R.string.fitness_weekly_check_in_is_due)
                    trend != null -> trend.message
                    else -> stringResource(R.string.fitness_weigh_in_again_next_week)
                },
                color = if (trend?.tone == TrendTone.CAUTION) Maroon else PurpleDeep,
                fontSize = TextSize.Caption,
                lineHeight = LineHeight.Tight,
                maxLines = 2
            )
        }
        if (due) {
            Text(
                stringResource(R.string.fitness_due),
                color = Color.White,
                fontSize = TextSize.Micro,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.clip(Shapes.Pill).background(Purple).padding(Space.S, Space.Xs)
            )
        } else {
            Icon(Icons.Filled.ChevronRight, contentDescription = null, tint = Purple)
        }
    }
}

@Composable
private fun ProgrammeCard(programme: Programme, onClick: () -> Unit) {
    val done = FitnessStore.daysDone(programme.id).size
    val active = FitnessStore.activeProgramme == programme.id
    Column(
        modifier = Modifier.width(210.dp).clip(Shapes.Large).background(programme.focus.tint)
            .then(if (active) Modifier.border(2.dp, Ink, Shapes.Large) else Modifier)
            .clickable(onClick = onClick).padding(Space.L)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(Modifier.size(38.dp).clip(CircleShape).background(Color.White), contentAlignment = Alignment.Center) {
                Icon(programme.focus.icon(), contentDescription = null, tint = programme.focus.accent, modifier = Modifier.size(20.dp))
            }
            Spacer(Modifier.weight(1f))
            if (ProGate.programmeLocked(programme.id)) {
                ProBadge()
            } else if (active) {
                Text(
                    stringResource(R.string.fitness_active),
                    color = programme.focus.accent,
                    fontSize = TextSize.Micro,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 0.5.sp
                )
            }
        }
        Spacer(Modifier.height(Space.M))
        Text(programme.title, fontSize = TextSize.Title, fontWeight = FontWeight.Bold)
        Text(programme.tagline, color = TextSecondary, fontSize = TextSize.Caption, maxLines = 1)
        Spacer(Modifier.height(Space.M))
        Box(Modifier.fillMaxWidth().height(6.dp).clip(Shapes.Pill).background(Color.White)) {
            Box(Modifier.fillMaxWidth(done / programme.days.toFloat()).fillMaxHeight().clip(Shapes.Pill).background(programme.focus.accent))
        }
        Text(
            if (done ==
                0
            ) {
                stringResource(R.string.fitness_30_days)
            } else {
                stringResource(R.string.fitness_day_1_s_of_2, done, programme.days)
            },
            color = programme.focus.accent,
            fontSize = TextSize.Caption,
            fontWeight = FontWeight.SemiBold,
            modifier = Modifier.padding(top = Space.S)
        )
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun QuickPicker(navController: NavHostController) {
    var focus by remember { mutableStateOf(QuickSessions.forGoal(FitnessStore.goal, LocalDate.now())) }
    var minutes by remember { mutableStateOf(FitnessStore.minutes.coerceAtMost(if (ProStore.isPro) 30 else 10)) }
    LazyRow(contentPadding = PaddingValues(horizontal = Space.Gutter), horizontalArrangement = Arrangement.spacedBy(Space.S)) {
        items(QuickFocus.entries) { f -> ChoicePill(f.label, f == focus) { focus = f } }
    }
    Spacer(Modifier.height(Space.S))
    LazyRow(contentPadding = PaddingValues(horizontal = Space.Gutter), horizontalArrangement = Arrangement.spacedBy(Space.S)) {
        items(QuickSessions.minuteOptions) { m ->
            ChoicePill(
                stringResource(R.string.common_1_s_min, m),
                m == minutes,
                trailing = if (ProGate.quickLocked(m)) {
                    (
                        {
                            Text(stringResource(R.string.common_pro), color = Amber, fontSize = TextSize.Micro, fontWeight = FontWeight.Bold)
                        }
                        )
                } else {
                    null
                }
            ) {
                if (ProGate.quickLocked(m)) ProStore.showPaywall("Longer sessions are part of Pro") else minutes = m
            }
        }
    }
    Spacer(Modifier.height(Space.M))
    val plan: WorkoutPlan = remember(focus, minutes, FitnessStore.level) { FitnessStore.planFor(QuickSessions.key(focus, minutes))!! }
    Row(
        modifier = Modifier.padding(horizontal = Space.Gutter).fillMaxWidth().clip(Shapes.Card).background(Ink).clickable {
            navController.navigate("workout/${plan.key}")
        }.padding(Space.L, Space.M),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(Modifier.weight(1f)) {
            Text(plan.title, color = Color.White, fontSize = TextSize.BodyLarge, fontWeight = FontWeight.Bold)
            Text(plan.blocks.joinToString(" · ") { it.exercise.name }, color = Color.White.copy(alpha = 0.6f), fontSize = TextSize.Caption, maxLines = 1)
        }
        Box(Modifier.size(40.dp).clip(CircleShape).background(Color.White), contentAlignment = Alignment.Center) {
            Icon(Icons.Filled.PlayArrow, contentDescription = stringResource(R.string.common_start), tint = Ink)
        }
    }
}
