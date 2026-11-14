package com.glowup.app.ui.screens.morning

import android.media.AudioManager
import android.media.ToneGenerator
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
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
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import com.glowup.app.ui.components.goBack
import com.glowup.app.ui.components.DetailHeader
import com.glowup.app.ui.data.ProgressStore
import com.glowup.app.ui.theme.*
import kotlinx.coroutines.delay
import com.glowup.app.ui.design.LineHeight
import com.glowup.app.ui.design.Shapes
import com.glowup.app.ui.design.TextSize
import com.glowup.app.ui.components.ProLockedScreen
import com.glowup.app.ui.data.ProGate

private const val COMPLETION_POINTS = 5

@Composable
fun MorningSessionScreen(navController: NavHostController, index: Int) {
    val session = morningSessions.getOrNull(index) ?: return
    if (ProGate.morningSessionLocked(index)) {
        ProLockedScreen(navController, session.title, "${session.title} is a Pro session")
        return
    }
    var stepIndex by remember { mutableStateOf(0) }
    var remaining by remember { mutableStateOf(session.steps.first().seconds) }
    var paused by remember { mutableStateOf(false) }
    var finished by remember { mutableStateOf(false) }
    val step = session.steps[stepIndex]
    val elapsedBefore = session.steps.take(stepIndex).sumOf { it.seconds }
    val progress = (elapsedBefore + step.seconds - remaining).toFloat() / session.totalSeconds

    val view = LocalView.current
    val chime = remember { runCatching { ToneGenerator(AudioManager.STREAM_MUSIC, 45) }.getOrNull() }
    DisposableEffect(Unit) {
        view.keepScreenOn = true
        onDispose {
            view.keepScreenOn = false
            chime?.release()
        }
    }

    fun advance() {
        if (stepIndex < session.steps.lastIndex) {
            stepIndex++
            remaining = session.steps[stepIndex].seconds
            if (session.steps[stepIndex].breath == null) chime?.startTone(ToneGenerator.TONE_PROP_ACK, 120)
        } else if (!finished) {
            finished = true
            chime?.startTone(ToneGenerator.TONE_PROP_PROMPT, 300)
            ProgressStore.award(COMPLETION_POINTS)
        }
    }

    LaunchedEffect(stepIndex, paused, finished) {
        while (!paused && !finished) {
            delay(1000)
            if (remaining > 1) remaining-- else advance()
        }
    }

    val breathScale by animateFloatAsState(
        targetValue = when (step.breath) {
            Breath.IN, Breath.HOLD -> 1f
            Breath.OUT, Breath.REST, null -> 0.55f
        },
        animationSpec = tween(durationMillis = if (step.breath == null) 300 else step.seconds * 1000, easing = LinearEasing),
        label = "breath",
    )

    Box(modifier = Modifier.fillMaxSize().background(Surface).systemBarsPadding()) {
        Column(modifier = Modifier.fillMaxSize()) {
            DetailHeader(session.title) { navController.goBack() }

            Box(
                modifier = Modifier
                    .padding(24.dp, 12.dp, 24.dp, 0.dp)
                    .fillMaxWidth()
                    .height(6.dp)
                    .clip(Shapes.Pill)
                    .background(ChipNeutral),
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth(if (finished) 1f else progress.coerceIn(0f, 1f))
                        .fillMaxHeight()
                        .clip(Shapes.Pill)
                        .background(Ink),
                )
            }

            Column(
                modifier = Modifier.weight(1f).fillMaxWidth().padding(horizontal = 28.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center,
            ) {
                if (finished) {
                    Text("Session complete", fontSize = TextSize.Display, fontWeight = FontWeight.Bold, textAlign = TextAlign.Center)
                    Spacer(Modifier.height(10.dp))
                    Text("+$COMPLETION_POINTS points. Nice way to start the day.", color = TextSecondary, fontSize = TextSize.BodyLarge, textAlign = TextAlign.Center)
                } else {
                    if (step.breath != null) {
                        Box(modifier = Modifier.size(220.dp), contentAlignment = Alignment.Center) {
                            Box(
                                modifier = Modifier
                                    .size(220.dp)
                                    .scale(breathScale)
                                    .clip(CircleShape)
                                    .background(BlueTint),
                            )
                            Text(remaining.toString(), fontSize = TextSize.Number, fontWeight = FontWeight.Bold, color = Navy)
                        }
                        Spacer(Modifier.height(28.dp))
                    } else {
                        Text(
                            "STEP ${stepIndex + 1} OF ${session.steps.size}",
                            color = TextSecondary,
                            fontSize = TextSize.Caption,
                            fontWeight = FontWeight.SemiBold,
                            letterSpacing = 0.5.sp,
                        )
                        Spacer(Modifier.height(10.dp))
                    }
                    Text(step.title, fontSize = TextSize.Display, fontWeight = FontWeight.Bold, textAlign = TextAlign.Center)
                    Spacer(Modifier.height(10.dp))
                    Text(step.detail, color = TextSecondary, fontSize = TextSize.BodyLarge, lineHeight = LineHeight.Relaxed, textAlign = TextAlign.Center)
                    if (step.breath == null) {
                        Spacer(Modifier.height(28.dp))
                        Text(
                            "%d:%02d".format(remaining / 60, remaining % 60),
                            fontSize = TextSize.Number,
                            fontWeight = FontWeight.Bold,
                        )
                    }
                }
            }

            Row(
                modifier = Modifier.padding(24.dp, 8.dp, 24.dp, 28.dp),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                if (finished) {
                    PillButton("Done", Ink, Color.White, Modifier.weight(1f)) { navController.goBack() }
                } else {
                    PillButton(if (paused) "Resume" else "Pause", Ink, Color.White, Modifier.weight(1f)) { paused = !paused }
                    PillButton("Skip", ChipNeutral, Ink, Modifier.weight(1f)) { advance() }
                }
            }
        }
    }
}

@Composable
private fun PillButton(label: String, background: Color, content: Color, modifier: Modifier, onClick: () -> Unit) {
    Box(
        modifier = modifier
            .height(58.dp)
            .clip(Shapes.Pill)
            .background(background)
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        Text(label, color = content, fontSize = TextSize.BodyLarge, fontWeight = FontWeight.SemiBold)
    }
}
