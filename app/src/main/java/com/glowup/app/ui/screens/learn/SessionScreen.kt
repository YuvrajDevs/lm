package com.glowup.app.ui.screens.learn

import com.glowup.app.ui.data.ProStore
import com.glowup.app.ui.data.ProGate
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
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import com.glowup.app.R
import com.glowup.app.ui.components.goBack
import com.glowup.app.ui.components.rememberNotificationPermission
import com.glowup.app.ui.data.TodoBucket
import com.glowup.app.ui.data.TodoStore
import com.glowup.app.ui.data.formatClock
import com.glowup.app.ui.data.program.Program
import com.glowup.app.ui.data.program.ProgramSession
import com.glowup.app.ui.data.program.ProgramStore
import com.glowup.app.ui.data.program.SessionState
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
        com.glowup.app.ui.components.ProLockedScreen(navController, "Day ${session.day}: ${session.title}", "Weeks 3–12 of the programme are part of Pro")
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
                modifier = Modifier.fillMaxWidth().padding(20.dp, 20.dp, 24.dp, 0.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Icon(
                    Icons.Filled.Close,
                    contentDescription = "Close",
                    tint = TextSecondary,
                    modifier = Modifier.size(40.dp).clip(CircleShape).clickable { navController.goBack() }.padding(8.dp),
                )
                Spacer(Modifier.width(8.dp))
                Row(modifier = Modifier.weight(1f), horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    repeat(steps.size - 1) { i ->
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .height(5.dp)
                                .clip(RoundedCornerShape(999.dp))
                                .background(if (i <= position) accent else ChipNeutral),
                        )
                    }
                }
            }
        }

        AnimatedContent(
            targetState = step,
            transitionSpec = { (slideInHorizontally { it / 4 } + fadeIn()) togetherWith fadeOut() },
            modifier = Modifier.weight(1f),
            label = "step",
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
                color = Ink,
                enabled = step != Step.Quiz || quizAnswered,
            ) { advance() }
        }
    }
}

@Composable
private fun IntroStep(session: ProgramSession, reviewing: Boolean) {
    Column(
        modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(28.dp, 40.dp, 28.dp, 16.dp),
    ) {
        Box(
            modifier = Modifier.size(84.dp).clip(RoundedCornerShape(26.dp)).background(session.area.tint),
            contentAlignment = Alignment.Center,
        ) {
            Image(painterResource(session.area.icon), contentDescription = null, modifier = Modifier.size(56.dp))
        }
        Spacer(Modifier.height(22.dp))
        Text(
            "DAY ${session.day} · WEEK ${session.week} · ${session.area.label.uppercase()}",
            color = session.area.accent,
            fontSize = 11.5.sp,
            fontWeight = FontWeight.SemiBold,
            letterSpacing = 0.4.sp,
        )
        Spacer(Modifier.height(8.dp))
        Text(session.title, fontSize = 30.sp, fontWeight = FontWeight.Bold, lineHeight = 36.sp)
        Spacer(Modifier.height(14.dp))
        Text(session.hook, color = TextSecondary, fontSize = 16.sp, lineHeight = 24.sp)
        Spacer(Modifier.height(24.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Chip("${session.minutes} min")
            Chip("3 ideas · quiz · drill")
            if (!reviewing) Chip("+${session.points} pts", session.area.tint, session.area.accent)
        }
        if (reviewing) {
            Spacer(Modifier.height(16.dp))
            Text("You finished this session — reviewing won't add points, but it locks the ideas in.", color = TextSecondary, fontSize = 13.sp, lineHeight = 19.sp)
        }
    }
}

@Composable
private fun IdeaStep(session: ProgramSession, number: Int) {
    val idea = session.ideas[number]
    Column(
        modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(28.dp, 40.dp, 28.dp, 16.dp),
    ) {
        Text(
            "KEY IDEA ${number + 1} OF ${session.ideas.size}",
            color = session.area.accent,
            fontSize = 11.5.sp,
            fontWeight = FontWeight.SemiBold,
            letterSpacing = 0.4.sp,
        )
        Spacer(Modifier.height(10.dp))
        Text(idea.heading, fontSize = 26.sp, fontWeight = FontWeight.Bold, lineHeight = 32.sp)
        Spacer(Modifier.height(18.dp))
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(24.dp))
                .background(session.area.tint)
                .padding(22.dp),
        ) {
            Text(idea.body, fontSize = 17.sp, lineHeight = 26.sp, fontWeight = FontWeight.Medium)
        }
    }
}

@Composable
private fun QuizStep(session: ProgramSession, onAnswered: () -> Unit) {
    val quiz = session.quiz
    val order = remember { quiz.options.indices.shuffled(Random(session.index)) }
    var picked by remember { mutableStateOf<Int?>(null) }

    Column(
        modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(28.dp, 40.dp, 28.dp, 16.dp),
    ) {
        Text("QUICK CHECK", color = session.area.accent, fontSize = 11.5.sp, fontWeight = FontWeight.SemiBold, letterSpacing = 0.4.sp)
        Spacer(Modifier.height(10.dp))
        Text(quiz.question, fontSize = 22.sp, fontWeight = FontWeight.Bold, lineHeight = 29.sp)
        Spacer(Modifier.height(20.dp))
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
                    .padding(bottom = 10.dp)
                    .clip(RoundedCornerShape(18.dp))
                    .background(background)
                    .border(2.dp, borderColor, RoundedCornerShape(18.dp))
                    .clickable(enabled = !revealed) {
                        picked = optionIndex
                        onAnswered()
                    }
                    .padding(18.dp, 16.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(quiz.options[optionIndex], fontSize = 15.sp, fontWeight = FontWeight.Medium, modifier = Modifier.weight(1f))
                if (revealed && isCorrect) Icon(Icons.Filled.Check, contentDescription = "Correct", tint = Green, modifier = Modifier.size(20.dp))
                if (revealed && isPicked && !isCorrect) Icon(Icons.Filled.Close, contentDescription = "Incorrect", tint = Maroon, modifier = Modifier.size(20.dp))
            }
        }
        picked?.let { choice ->
            Spacer(Modifier.height(6.dp))
            Text(
                if (choice == quiz.answer) "Spot on." else "Not quite.",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = if (choice == quiz.answer) Green else Maroon,
            )
            Spacer(Modifier.height(4.dp))
            Text(quiz.why, color = TextSecondary, fontSize = 14.5.sp, lineHeight = 21.sp)
        }
    }
}

@Composable
private fun DrillStep(session: ProgramSession) {
    val todoTitle = "Drill: ${session.title}"
    val added = TodoStore.items.any { it.title == todoTitle }
    Column(
        modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(28.dp, 40.dp, 28.dp, 16.dp),
    ) {
        Text("TODAY'S DRILL", color = session.area.accent, fontSize = 11.5.sp, fontWeight = FontWeight.SemiBold, letterSpacing = 0.4.sp)
        Spacer(Modifier.height(10.dp))
        Text("Put it into practice", fontSize = 26.sp, fontWeight = FontWeight.Bold)
        Spacer(Modifier.height(18.dp))
        Box(
            modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(24.dp)).background(Ink).padding(22.dp),
        ) {
            Text(session.drill, color = Color.White, fontSize = 17.sp, lineHeight = 26.sp, fontWeight = FontWeight.Medium)
        }
        Spacer(Modifier.height(14.dp))
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(18.dp))
                .background(if (added) GreenTint else CardNeutral)
                .clickable(enabled = !added) { TodoStore.add(todoTitle, TodoBucket.TODAY, "Day ${session.day} · ${session.area.label}") }
                .padding(16.dp, 14.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                if (added) "Added to today's plan" else "Add this drill to today's plan",
                fontSize = 14.5.sp,
                fontWeight = FontWeight.SemiBold,
                color = if (added) Green else Ink,
                modifier = Modifier.weight(1f),
            )
            Icon(if (added) Icons.Filled.Check else Icons.Filled.Add, contentDescription = null, tint = if (added) Green else Ink, modifier = Modifier.size(18.dp))
        }
        Spacer(Modifier.height(10.dp))
        Text("Knowing isn't the goal — doing it once today is what changes you.", color = TextSecondary, fontSize = 13.sp, lineHeight = 19.sp)
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
        modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(28.dp, 48.dp, 28.dp, 28.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Box(
            modifier = Modifier.size(150.dp).scale(flame.value).clip(CircleShape).background(YellowTint),
            contentAlignment = Alignment.Center,
        ) {
            Image(painterResource(if (reviewing) R.drawable.ic_check else R.drawable.ic_flame), contentDescription = null, modifier = Modifier.size(82.dp))
        }
        Spacer(Modifier.height(24.dp))
        Text(
            if (reviewing) "Review done" else "Day ${session.day} complete!",
            fontSize = 30.sp,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center,
        )
        Spacer(Modifier.height(8.dp))
        if (!reviewing) {
            Text(
                "${if (streak == 1) "1-day" else "$streak-day"} learning streak" + (earned?.let { " · +$it points" } ?: ""),
                color = Coral,
                fontSize = 16.sp,
                fontWeight = FontWeight.SemiBold,
                textAlign = TextAlign.Center,
            )
            Spacer(Modifier.height(4.dp))
            Text(
                "${session.area.label}: ${ProgramStore.areaDone(session.area)}/12 done" + if (areaStreak > 0) " · $areaStreak-week streak" else "",
                color = TextSecondary,
                fontSize = 14.sp,
                textAlign = TextAlign.Center,
            )
        }

        if (!reviewing && next != null) {
            Spacer(Modifier.height(26.dp))
            Column(
                modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(22.dp)).background(next.area.tint).padding(18.dp),
            ) {
                Text("UP NEXT · DAY ${next.day} · ${next.area.label.uppercase()}", color = next.area.accent, fontSize = 11.sp, fontWeight = FontWeight.SemiBold, letterSpacing = 0.4.sp)
                Spacer(Modifier.height(4.dp))
                Text(next.title, fontSize = 17.sp, fontWeight = FontWeight.Bold)
                Text(next.hook, color = TextSecondary, fontSize = 13.sp, lineHeight = 18.sp)
            }
        }

        if (!reviewing && !ProgramStore.reminderPrompted) {
            Spacer(Modifier.height(18.dp))
            Column(
                modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(22.dp)).background(CardNeutral).padding(18.dp),
            ) {
                Text("Want a nudge each day?", fontSize = 15.sp, fontWeight = FontWeight.SemiBold)
                Text("Only if you haven't done that day's session yet.", color = TextSecondary, fontSize = 12.5.sp)
                Spacer(Modifier.height(12.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    listOf(LocalTime.of(8, 0) to "8 AM", LocalTime.of(13, 0) to "1 PM", LocalTime.of(20, 0) to "8 PM").forEach { (time, label) ->
                        Chip(label, Ink, Color.White) {
                            ProgramStore.setReminder(time)
                            askPermission()
                        }
                    }
                }
                Text(
                    "No thanks",
                    color = TextSecondary,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.padding(top = 10.dp).clickable { ProgramStore.dismissReminderPrompt() },
                )
            }
        } else if (!reviewing && ProgramStore.reminderTime != null && earned != null) {
            Spacer(Modifier.height(14.dp))
            Text(
                "We'll nudge you at ${formatClock(ProgramStore.reminderTime!!)} if you haven't started.",
                color = TextSecondary,
                fontSize = 12.5.sp,
                textAlign = TextAlign.Center,
            )
        }

        Spacer(Modifier.height(26.dp))
        if (!reviewing && next != null) {
            if (ProGate.sessionLocked(next.index)) {
                BottomButton("Unlock weeks 3–12 with Pro", Ink, padding = false) {
                    ProStore.showPaywall("You've finished the free weeks — keep going with the full programme")
                }
            } else {
                BottomButton("Next session · Day ${next.day}", Ink, padding = false) {
                    navController.navigate("session/${next.index}") {
                        popUpTo("session/{index}") { inclusive = true }
                    }
                }
            }
            Text(
                "Done for now",
                color = TextSecondary,
                fontSize = 14.sp,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier.padding(top = 14.dp).clip(RoundedCornerShape(999.dp)).clickable { navController.goBack() }.padding(12.dp, 6.dp),
            )
        } else {
            BottomButton("Done", Ink, padding = false) { navController.goBack() }
        }
    }
}

@Composable
private fun LockedSession(navController: NavHostController, session: ProgramSession) {
    Column(
        modifier = Modifier.fillMaxSize().background(Surface).systemBarsPadding().padding(28.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Icon(Icons.Filled.Lock, contentDescription = null, tint = TextMuted, modifier = Modifier.size(40.dp))
        Spacer(Modifier.height(12.dp))
        Text("Day ${session.day} isn't unlocked yet", fontSize = 22.sp, fontWeight = FontWeight.Bold, textAlign = TextAlign.Center)
        Spacer(Modifier.height(8.dp))
        Text(
            "Finish day ${session.day - 1} first — sessions build on each other.",
            color = TextSecondary,
            fontSize = 14.sp,
            textAlign = TextAlign.Center,
        )
        Spacer(Modifier.height(24.dp))
        BottomButton("Back", Ink, padding = false) { navController.goBack() }
    }
}

@Composable
private fun Chip(label: String, background: Color = ChipNeutral, content: Color = Ink, onClick: (() -> Unit)? = null) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(999.dp))
            .background(background)
            .let { if (onClick != null) it.clickable(onClick = onClick) else it }
            .padding(12.dp, 8.dp),
    ) {
        Text(label, color = content, fontSize = 12.5.sp, fontWeight = FontWeight.SemiBold)
    }
}

@Composable
private fun BottomButton(label: String, color: Color, enabled: Boolean = true, padding: Boolean = true, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .let { if (padding) it.padding(24.dp, 8.dp, 24.dp, 28.dp) else it }
            .fillMaxWidth()
            .height(58.dp)
            .clip(RoundedCornerShape(999.dp))
            .background(if (enabled) color else Color(0xFFD8D5CE))
            .clickable(enabled = enabled, onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        Text(label, color = Color.White, fontSize = 15.5.sp, fontWeight = FontWeight.SemiBold)
    }
}
