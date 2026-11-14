package com.glowup.app.ui.screens.fitness

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.DirectionsRun
import androidx.compose.material.icons.filled.AccessibilityNew
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.SelfImprovement
import androidx.compose.material.icons.filled.SportsGymnastics
import androidx.compose.material.icons.filled.SportsMartialArts
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.glowup.app.ui.components.artworkOrNull
import com.glowup.app.ui.data.fitness.Exercise
import com.glowup.app.ui.data.fitness.Muscle
import com.glowup.app.ui.design.Chip
import com.glowup.app.ui.design.Shapes
import com.glowup.app.ui.design.Space
import com.glowup.app.ui.design.TextSize
import com.glowup.app.ui.theme.ChipNeutral
import com.glowup.app.ui.theme.Ink
import com.glowup.app.ui.theme.TextMuted
import com.glowup.app.ui.theme.TextSecondary
import kotlinx.coroutines.launch

/** Resource name for a real exercise illustration, e.g. `exercise_glute_bridge`. */
fun exerciseArtworkName(id: String) = "exercise_" + id.replace('-', '_')

fun Muscle.icon(): ImageVector = when (this) {
    Muscle.CHEST, Muscle.ARMS, Muscle.SHOULDERS -> Icons.Filled.FitnessCenter
    Muscle.BACK -> Icons.Filled.AccessibilityNew
    Muscle.CORE -> Icons.Filled.SportsGymnastics
    Muscle.GLUTES, Muscle.LEGS -> Icons.Filled.SportsMartialArts
    Muscle.CARDIO -> Icons.AutoMirrored.Filled.DirectionsRun
    Muscle.MOBILITY -> Icons.Filled.SelfImprovement
}

/**
 * The exercise picture: real artwork when it's been added, otherwise a clean tinted tile with the
 * muscle-group icon so every card still looks finished.
 */
@Composable
fun ExerciseArt(exercise: Exercise, size: Dp, corner: Dp = 18.dp, modifier: Modifier = Modifier) {
    val art = artworkOrNull(exerciseArtworkName(exercise.id))
    Box(
        modifier = modifier.size(size).clip(RoundedCornerShape(corner)).background(exercise.primary.tint),
        contentAlignment = Alignment.Center
    ) {
        if (art != null) {
            Image(painterResource(art), contentDescription = exercise.name, contentScale = ContentScale.Crop, modifier = Modifier.fillMaxSize())
        } else {
            Icon(exercise.primary.icon(), contentDescription = null, tint = exercise.primary.accent, modifier = Modifier.size(size * 0.45f))
        }
    }
}

@Composable
fun MuscleChip(muscle: Muscle, onDark: Boolean = false) {
    Text(
        muscle.label,
        color = if (onDark) Color.White else muscle.accent,
        fontSize = TextSize.Micro,
        fontWeight = FontWeight.SemiBold,
        modifier = Modifier
            .clip(Shapes.Pill)
            .background(if (onDark) Color.White.copy(alpha = 0.14f) else muscle.tint)
            .padding(Space.S, Space.Xs),
        maxLines = 1
    )
}

@Composable
fun LevelDots(level: Int, color: Color = Ink) {
    Row(horizontalArrangement = Arrangement.spacedBy(Space.Xs), verticalAlignment = Alignment.CenterVertically) {
        repeat(3) { i ->
            Box(Modifier.size(6.dp).clip(CircleShape).background(if (i < level) color else TextMuted.copy(alpha = 0.5f)))
        }
    }
}

/** Big −/value/+ control used for reps, seconds and body measurements. */
@Composable
fun NumberStepper(
    value: String,
    onMinus: () -> Unit,
    onPlus: () -> Unit,
    modifier: Modifier = Modifier,
    unit: String? = null,
    onDark: Boolean = false,
    big: Boolean = false
) {
    val buttonBg = if (onDark) Color.White.copy(alpha = 0.14f) else ChipNeutral
    val tint = if (onDark) Color.White else Ink
    Row(modifier = modifier, verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.Center) {
        RepeatButton(Icons.Filled.Remove, "Less", buttonBg, tint, if (big) 56.dp else 44.dp, onMinus)
        Column(Modifier.widthIn(min = if (big) 120.dp else 90.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            Text(value, color = tint, fontSize = if (big) 56.sp else 28.sp, fontWeight = FontWeight.Bold, lineHeight = if (big) 60.sp else 32.sp)
            unit?.let { Text(it, color = if (onDark) Color.White.copy(alpha = 0.6f) else TextSecondary, fontSize = TextSize.Caption) }
        }
        RepeatButton(Icons.Filled.Add, "More", buttonBg, tint, if (big) 56.dp else 44.dp, onPlus)
    }
}

/** Pill used for single-choice options across the fitness screens. */
@Composable
fun ChoicePill(label: String, selected: Boolean, modifier: Modifier = Modifier, trailing: (@Composable () -> Unit)? = null, onClick: () -> Unit) =
    Chip(label, selected, modifier, trailing, onClick)

/** Tap to step once; press and hold to keep stepping, faster the longer you hold. */
@Composable
private fun RepeatButton(icon: ImageVector, description: String, bg: Color, tint: Color, size: Dp, onStep: () -> Unit) {
    val step by rememberUpdatedState(onStep)
    Box(
        Modifier.size(size).clip(CircleShape).background(bg).pointerInput(Unit) {
            kotlinx.coroutines.coroutineScope {
                detectTapGestures(onPress = {
                    step()
                    val job = launch {
                        kotlinx.coroutines.delay(400)
                        var interval = 120L
                        while (true) {
                            step()
                            kotlinx.coroutines.delay(interval)
                            interval = (interval * 0.9).toLong().coerceAtLeast(35L)
                        }
                    }
                    tryAwaitRelease()
                    job.cancel()
                })
            }
        },
        contentAlignment = Alignment.Center
    ) {
        Icon(icon, contentDescription = description, tint = tint)
    }
}
