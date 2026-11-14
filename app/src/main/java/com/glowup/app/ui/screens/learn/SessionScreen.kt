package com.glowup.app.ui.screens.learn

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.Icon
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
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import com.glowup.app.R
import com.glowup.app.ui.components.ProLockedScreen
import com.glowup.app.ui.components.goBack
import com.glowup.app.ui.components.rememberNotificationPermission
import com.glowup.app.ui.data.ProGate
import com.glowup.app.ui.data.ProStore
import com.glowup.app.ui.data.TodoBucket
import com.glowup.app.ui.data.TodoStore
import com.glowup.app.ui.data.formatClock
import com.glowup.app.ui.data.program.Program
import com.glowup.app.ui.data.program.ProgramSession
import com.glowup.app.ui.data.program.ProgramStore
import com.glowup.app.ui.data.program.SessionState
import com.glowup.app.ui.design.AppButton
import com.glowup.app.ui.design.ButtonVariant
import com.glowup.app.ui.design.LineHeight
import com.glowup.app.ui.design.Shapes
import com.glowup.app.ui.design.Space
import com.glowup.app.ui.design.TextSize
import com.glowup.app.ui.theme.*
import java.time.LocalTime
import kotlin.random.Random

private sealed interface Step {
    data object Intro : Step
    data class Idea(val number: Int) : Step
    data object Quiz : Step
    data object Drill : Step
    data object Complete : Step
}

@Composable
fun SessionScreen(navController: NavHostController, index: Int) {
    val session = Program.sessions.getOrNull(index) ?: return
    val initialState = remember { ProgramStore.state(index) }
    if (ProGate.sessionLocked(index)) {
        ProLockedScreen(navController, "Day ${session.day}: ${session.title}", "Weeks 3–12 of the programme are part of Pro")
        return
    }
    if (initialState == SessionState.LOCKED) {
        LockedSession(navController, session)
        return
    }
    val reviewing = initialState == SessionState.DONE
    val steps = remember {
        buildList {
            add(Step.Intro)
            session.ideas.indices.forEach { add(Step.Idea(it)) }
            add(Step.Quiz)
            add(Step.Drill)
            add(Step.Complete)
        }
    }
    var position by remember { mutableStateOf(0) }
    var earned by remember { mutableStateOf<Int?>(null) }
    var quizAnswered by remember { mutableStateOf(false) }
    val step = steps[position]
    val accent = session.area.accent

    fun advance() {
        if (steps[position + 1] == Step.Complete && !reviewing) earned = ProgramStore.complete(index)
        position++
    }

    Column(modifier = Modifier.fillMaxSize().background(Surface).systemBarsPadding()) {
        if (step != Step.Complete) {
            Row(
                modifier = Modifier.fillMaxWidth().padding(Space.Xl, Space.Xl, Space.Gutter, 0.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    Icons.Filled.Close,
                    contentDescription = stringResource(R.string.common_close),
                    tint = TextSecondary,
                    modifier = Modifier.size(40.dp).clip(CircleShape).clickable { navController.goBack() }.padding(Space.S)
                )
                Spacer(Modifier.width(Space.S))
                Row(modifier = Modifier.weight(1f), horizontalArrangement = Arrangement.spacedBy(Space.Xs)) {
                    repeat(steps.size - 1) { i ->
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .height(5.dp)
                                .clip(Shapes.Pill)
                                .background(if (i <= position) accent else ChipNeutral)
                        )
                    }
                }
            }
        }

        AnimatedContent(
            targetState = step,
            transitionSpec = { (slideInHorizontally { it / 4 } + fadeIn()) togetherWith fadeOut() },
            modifier = Modifier.weight(1f),
            label = "step"
        ) { current ->
            when (current) {
                Step.Intro -> IntroStep(session, reviewing)
                is Step.Idea -> IdeaStep(session, current.number)
                Step.Quiz -> QuizStep(session) { quizAnswered = true }
                Step.Drill -> DrillStep(session)
                Step.Complete -> CompleteStep(navController, session, earned, reviewing)
            }
        }

        if (step != Step.Complete) {
            BottomButton(
                label = when (step) {
                    Step.Intro -> if (reviewing) "Review" else "Start"
                    Step.Drill -> if (reviewing) "Finish review" else "Complete day ${session.day}"
                    Step.Quiz -> "Continue"
                    else -> "Next"
                },
                enabled = step != Step.Quiz || quizAnswered
            ) { advance() }
        }
    }
}

@Composable
private fun IntroStep(session: ProgramSession, reviewing: Boolean) {
    Column(
        modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(Space.Section, Space.Huge, Space.Section, Space.L)
    ) {
        Box(
            modifier = Modifier.size(84.dp).clip(Shapes.Large).background(session.area.tint),
            contentAlignment = Alignment.Center
        ) {
            Image(painterResource(session.area.icon), contentDescription = null, modifier = Modifier.size(56.dp))
        }
        Spacer(Modifier.height(Space.Xl))
        Text(
            stringResource(R.string.session_day_1_s_week_2, session.day, session.week, session.area.label.uppercase()),
            color = session.area.accent,
            fontSize = TextSize.Caption,
            fontWeight = FontWeight.SemiBold,
            letterSpacing = 0.4.sp
        )
        Spacer(Modifier.height(Space.S))
        Text(session.title, fontSize = TextSize.DisplayLarge, fontWeight = FontWeight.Bold, lineHeight = LineHeight.Display)
        Spacer(Modifier.height(Space.M))
        Text(session.hook, color = TextSecondary, fontSize = TextSize.Title, lineHeight = LineHeight.Title)
        Spacer(Modifier.height(Space.Gutter))
        Row(horizontalArrangement = Arrangement.spacedBy(Space.S)) {
            Chip(stringResource(R.string.common_1_s_min, session.minutes))
            Chip(stringResource(R.string.session_3_ideas_quiz_drill))
            if (!reviewing) Chip(stringResource(R.string.session_1_s_pts, session.points), session.area.tint, session.area.accent)
        }
        if (reviewing) {
            Spacer(Modifier.height(Space.L))
            Text(
                stringResource(R.string.session_you_finished_this_session_reviewing),
                color = TextSecondary,
                fontSize = TextSize.BodySmall,
                lineHeight = LineHeight.Body
            )
        }
    }
}

@Composable
private fun IdeaStep(session: ProgramSession, number: Int) {
    val idea = session.ideas[number]
    Column(
        modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(Space.Section, Space.Huge, Space.Section, Space.L)
    ) {
        Text(
            stringResource(R.string.session_key_idea_1_s_of, number + 1, session.ideas.size),
            color = session.area.accent,
            fontSize = TextSize.Caption,
            fontWeight = FontWeight.SemiBold,
            letterSpacing = 0.4.sp
        )
        Spacer(Modifier.height(Space.S))
        Text(idea.heading, fontSize = TextSize.Display, fontWeight = FontWeight.Bold, lineHeight = LineHeight.Display)
        Spacer(Modifier.height(Space.L))
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(Shapes.Large)
                .background(session.area.tint)
                .padding(Space.Xl)
        ) {
            Text(idea.body, fontSize = TextSize.Title, lineHeight = LineHeight.Title, fontWeight = FontWeight.Medium)
        }
    }
}

@Composable
private fun QuizStep(session: ProgramSession, onAnswered: () -> Unit) {
    val quiz = session.quiz
    val order = remember { quiz.options.indices.shuffled(Random(session.index)) }
    var picked by remember { mutableStateOf<Int?>(null) }

    Column(
        modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(Space.Section, Space.Huge, Space.Section, Space.L)
    ) {
        Text(
            stringResource(R.string.session_quick_check),
            color = session.area.accent,
            fontSize = TextSize.Caption,
            fontWeight = FontWeight.SemiBold,
            letterSpacing = 0.4.sp
        )
        Spacer(Modifier.height(Space.S))
        Text(quiz.question, fontSize = TextSize.TitleLarge, fontWeight = FontWeight.Bold, lineHeight = LineHeight.Display)
        Spacer(Modifier.height(Space.Xl))
        order.forEach { optionIndex ->
            val isCorrect = optionIndex == quiz.answer
            val isPicked = optionIndex == picked
            val revealed = picked != null
            val background = when {
                revealed && isCorrect -> GreenTint
                revealed && isPicked -> MaroonTint
                else -> CardNeutral
            }
            val borderColor = when {
                revealed && isCorrect -> Green
                revealed && isPicked -> Maroon
                else -> Color.Transparent
            }
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = Space.S)
                    .clip(Shapes.Medium)
                    .background(background)
                    .border(2.dp, borderColor, Shapes.Medium)
                    .clickable(enabled = !revealed) {
                        picked = optionIndex
                        onAnswered()
                    }
                    .padding(Space.L, Space.L),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(quiz.options[optionIndex], fontSize = TextSize.BodyLarge, fontWeight = FontWeight.Medium, modifier = Modifier.weight(1f))
                if (revealed &&
                    isCorrect
                ) {
                    Icon(Icons.Filled.Check, contentDescription = stringResource(R.string.session_correct), tint = Green, modifier = Modifier.size(20.dp))
                }
                if (revealed &&
                    isPicked &&
                    !isCorrect
                ) {
                    Icon(
                        Icons.Filled.Close,
                        contentDescription = stringResource(R.string.session_incorrect),
                        tint = Maroon,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }
        picked?.let { choice ->
            Spacer(Modifier.height(Space.S))
            Text(
                if (choice == quiz.answer) stringResource(R.string.session_spot_on) else stringResource(R.string.session_not_quite),
                fontSize = TextSize.Title,
                fontWeight = FontWeight.Bold,
                color = if (choice == quiz.answer) Green else Maroon
            )
            Spacer(Modifier.height(Space.Xs))
            Text(quiz.why, color = TextSecondary, fontSize = TextSize.BodyLarge, lineHeight = LineHeight.Relaxed)
        }
    }
}

@Composable
private fun DrillStep(session: ProgramSession) {
    val todoTitle = "Drill: ${session.title}"
    val added = TodoStore.items.any { it.title == todoTitle }
    Column(
        modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(Space.Section, Space.Huge, Space.Section, Space.L)
    ) {
        Text(
            stringResource(R.string.session_today_s_drill),
            color = session.area.accent,
            fontSize = TextSize.Caption,
            fontWeight = FontWeight.SemiBold,
            letterSpacing = 0.4.sp
        )
        Spacer(Modifier.height(Space.S))
        Text(stringResource(R.string.session_put_it_into_practice), fontSize = TextSize.Display, fontWeight = FontWeight.Bold)
        Spacer(Modifier.height(Space.L))
        Box(
            modifier = Modifier.fillMaxWidth().clip(Shapes.Large).background(Ink).padding(Space.Xl)
        ) {
            Text(session.drill, color = Color.White, fontSize = TextSize.Title, lineHeight = LineHeight.Title, fontWeight = FontWeight.Medium)
        }
        Spacer(Modifier.height(Space.M))
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(Shapes.Medium)
                .background(if (added) GreenTint else CardNeutral)
                .clickable(enabled = !added) { TodoStore.add(todoTitle, TodoBucket.TODAY, "Day ${session.day} · ${session.area.label}") }
                .padding(Space.L, Space.M),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                if (added) stringResource(R.string.common_added_to_today_s_plan) else stringResource(R.string.session_add_this_drill_to_today),
                fontSize = TextSize.BodyLarge,
                fontWeight = FontWeight.SemiBold,
                color = if (added) Green else Ink,
                modifier = Modifier.weight(1f)
            )
            Icon(
                if (added) Icons.Filled.Check else Icons.Filled.Add,
                contentDescription = null,
                tint = if (added) Green else Ink,
                modifier = Modifier.size(18.dp)
            )
        }
        Spacer(Modifier.height(Space.S))
        Text(stringResource(R.string.session_knowing_isn_t_the_goal), color = TextSecondary, fontSize = TextSize.BodySmall, lineHeight = LineHeight.Body)
    }
}

@Composable
private fun CompleteStep(navController: NavHostController, session: ProgramSession, earned: Int?, reviewing: Boolean) {
    val flame = remember { Animatable(0.3f) }
    LaunchedEffect(Unit) { flame.animateTo(1f, spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessLow)) }
    val streak = ProgramStore.streakDays()
    val areaStreak = ProgramStore.areaStreakWeeks(session.area)
    val next = ProgramStore.next
    val askPermission = rememberNotificationPermission()

    Column(
        modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(Space.Section, 48.dp, Space.Section, Space.Section),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier.size(150.dp).scale(flame.value).clip(CircleShape).background(YellowTint),
            contentAlignment = Alignment.Center
        ) {
            Image(painterResource(if (reviewing) R.drawable.ic_check else R.drawable.ic_flame), contentDescription = null, modifier = Modifier.size(82.dp))
        }
        Spacer(Modifier.height(Space.Gutter))
        Text(
            if (reviewing) stringResource(R.string.session_review_done) else stringResource(R.string.session_day_1_s_complete, session.day),
            fontSize = TextSize.DisplayLarge,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center
        )
        Spacer(Modifier.height(Space.S))
        if (!reviewing) {
            Text(
                "${if (streak == 1) "1-day" else "$streak-day"} learning streak" + (earned?.let { " · +$it points" } ?: ""),
                color = Coral,
                fontSize = TextSize.Title,
                fontWeight = FontWeight.SemiBold,
                textAlign = TextAlign.Center
            )
            Spacer(Modifier.height(Space.Xs))
            Text(
                stringResource(R.string.session_1_s_2_s_12, session.area.label, ProgramStore.areaDone(session.area)) +
                    if (areaStreak > 0) stringResource(R.string.session_1_s_week_streak, areaStreak) else "",
                color = TextSecondary,
                fontSize = TextSize.Body,
                textAlign = TextAlign.Center
            )
        }

        if (!reviewing && next != null) {
            Spacer(Modifier.height(Space.Gutter))
            Column(
                modifier = Modifier.fillMaxWidth().clip(Shapes.Card).background(next.area.tint).padding(Space.L)
            ) {
                Text(
                    stringResource(R.string.session_up_next_day_1_s, next.day, next.area.label.uppercase()),
                    color = next.area.accent,
                    fontSize = TextSize.Micro,
                    fontWeight = FontWeight.SemiBold,
                    letterSpacing = 0.4.sp
                )
                Spacer(Modifier.height(Space.Xs))
                Text(next.title, fontSize = TextSize.Title, fontWeight = FontWeight.Bold)
                Text(next.hook, color = TextSecondary, fontSize = TextSize.BodySmall, lineHeight = LineHeight.Compact)
            }
        }

        if (!reviewing && !ProgramStore.reminderPrompted) {
            Spacer(Modifier.height(Space.L))
            Column(
                modifier = Modifier.fillMaxWidth().clip(Shapes.Card).background(CardNeutral).padding(Space.L)
            ) {
                Text(stringResource(R.string.session_want_a_nudge_each_day), fontSize = TextSize.BodyLarge, fontWeight = FontWeight.SemiBold)
                Text(stringResource(R.string.session_only_if_you_haven_t), color = TextSecondary, fontSize = TextSize.BodySmall)
                Spacer(Modifier.height(Space.M))
                Row(horizontalArrangement = Arrangement.spacedBy(Space.S)) {
                    listOf(LocalTime.of(8, 0) to "8 AM", LocalTime.of(13, 0) to "1 PM", LocalTime.of(20, 0) to "8 PM").forEach { (time, label) ->
                        Chip(label, Ink, Color.White) {
                            ProgramStore.setReminder(time)
                            askPermission()
                        }
                    }
                }
                Text(
                    stringResource(R.string.session_no_thanks),
                    color = TextSecondary,
                    fontSize = TextSize.BodySmall,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.padding(top = Space.S).clickable { ProgramStore.dismissReminderPrompt() }
                )
            }
        } else if (!reviewing && ProgramStore.reminderTime != null && earned != null) {
            Spacer(Modifier.height(Space.M))
            Text(
                stringResource(R.string.session_we_ll_nudge_you_at, formatClock(ProgramStore.reminderTime!!)),
                color = TextSecondary,
                fontSize = TextSize.BodySmall,
                textAlign = TextAlign.Center
            )
        }

        Spacer(Modifier.height(Space.Gutter))
        if (!reviewing && next != null) {
            if (ProGate.sessionLocked(next.index)) {
                BottomButton("Unlock weeks 3–12 with Pro", padding = false) {
                    ProStore.showPaywall("You've finished the free weeks — keep going with the full programme")
                }
            } else {
                BottomButton("Next session · Day ${next.day}", padding = false) {
                    navController.navigate("session/${next.index}") {
                        popUpTo("session/{index}") { inclusive = true }
                    }
                }
            }
            Text(
                stringResource(R.string.session_done_for_now),
                color = TextSecondary,
                fontSize = TextSize.Body,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier.padding(top = Space.M).clip(Shapes.Pill).clickable { navController.goBack() }.padding(Space.M, Space.S)
            )
        } else {
            BottomButton("Done", padding = false) { navController.goBack() }
        }
    }
}

@Composable
private fun LockedSession(navController: NavHostController, session: ProgramSession) {
    Column(
        modifier = Modifier.fillMaxSize().background(Surface).systemBarsPadding().padding(Space.Section),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(Icons.Filled.Lock, contentDescription = null, tint = TextMuted, modifier = Modifier.size(40.dp))
        Spacer(Modifier.height(Space.M))
        Text(
            stringResource(R.string.session_day_1_s_isn_t, session.day),
            fontSize = TextSize.TitleLarge,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center
        )
        Spacer(Modifier.height(Space.S))
        Text(
            stringResource(R.string.session_finish_day_1_s_first, session.day - 1),
            color = TextSecondary,
            fontSize = TextSize.Body,
            textAlign = TextAlign.Center
        )
        Spacer(Modifier.height(Space.Gutter))
        BottomButton("Back", padding = false) { navController.goBack() }
    }
}

@Composable
private fun Chip(label: String, background: Color = ChipNeutral, content: Color = Ink, onClick: (() -> Unit)? = null) {
    Box(
        modifier = Modifier
            .clip(Shapes.Pill)
            .background(background)
            .let { if (onClick != null) it.clickable(onClick = onClick) else it }
            .padding(Space.M, Space.S)
    ) {
        Text(label, color = content, fontSize = TextSize.BodySmall, fontWeight = FontWeight.SemiBold)
    }
}

@Composable
private fun BottomButton(label: String, enabled: Boolean = true, padding: Boolean = true, onClick: () -> Unit) {
    AppButton(
        label,
        ButtonVariant.Primary,
        Modifier.then(if (padding) Modifier.padding(Space.Gutter, Space.S, Space.Gutter, Space.Section) else Modifier).fillMaxWidth(),
        enabled = enabled,
        onClick = onClick
    )
}
