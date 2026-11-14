package com.glowup.app.ui.screens.sleep

import android.text.format.DateFormat
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.outlined.Bedtime
import androidx.compose.material.icons.outlined.WbSunny
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.TimePicker
import androidx.compose.material3.TimePickerDefaults
import androidx.compose.material3.rememberTimePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.glowup.app.R
import com.glowup.app.ui.components.ReMakeUDialog
import com.glowup.app.ui.data.SleepEntry
import com.glowup.app.ui.data.SleepLogStore
import com.glowup.app.ui.data.formatClock
import com.glowup.app.ui.data.formatDuration
import com.glowup.app.ui.design.AppButton
import com.glowup.app.ui.design.ButtonVariant
import com.glowup.app.ui.design.Shapes
import com.glowup.app.ui.design.Space
import com.glowup.app.ui.design.TextSize
import com.glowup.app.ui.screens.mood.moodScoreColors
import com.glowup.app.ui.theme.*
import java.time.LocalDate
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import java.util.Locale

@Composable
internal fun CheckInCard(
    date: LocalDate,
    today: LocalDate,
    bed: LocalTime,
    wake: LocalTime,
    quality: Int?,
    onBed: (LocalTime) -> Unit,
    onWake: (LocalTime) -> Unit,
    onPickBed: () -> Unit,
    onPickWake: () -> Unit,
    onQuality: (Int) -> Unit,
    onSave: () -> Unit,
    onCancel: (() -> Unit)?
) {
    val minutes = SleepEntry(date, bed, wake, 3).minutes
    val need = SleepLogStore.needMinutes
    Column(modifier = Modifier.fillMaxWidth().clip(Shapes.Hero).background(PurpleTint).padding(Space.Xl, Space.L)) {
        Text(
            if (date ==
                today
            ) {
                (
                    if (LocalTime.now().hour in
                        4..11
                    ) {
                        stringResource(R.string.sleep_log_good_morning)
                    } else {
                        stringResource(R.string.common_log_last_night)
                    }
                    )
            } else {
                stringResource(R.string.sleep_log_editing) +
                    date.format(DateTimeFormatter.ofPattern("EEE d MMM", Locale.getDefault())).uppercase()
            },
            color = PurpleText,
            fontSize = TextSize.Micro,
            fontWeight = FontWeight.SemiBold,
            letterSpacing = 0.4.sp
        )
        Text(
            if (date ==
                today
            ) {
                stringResource(R.string.sleep_log_how_did_you_sleep)
            } else {
                stringResource(R.string.sleep_log_edit_this_night)
            },
            fontSize = TextSize.TitleLarge,
            fontWeight = FontWeight.Bold
        )
        Spacer(Modifier.height(Space.M))
        Row(horizontalArrangement = Arrangement.spacedBy(Space.S)) {
            TimeTile("Went to bed", Icons.Outlined.Bedtime, bed, Modifier.weight(1f), onBed, onPickBed)
            TimeTile("Woke up", Icons.Outlined.WbSunny, wake, Modifier.weight(1f), onWake, onPickWake)
        }
        Spacer(Modifier.height(Space.M))
        val diff = minutes - need
        Text(
            buildString {
                append(formatDuration(minutes))
                append(" asleep · ")
                append(
                    when {
                        diff >= 0 ->
                            if (diff <
                                15
                            ) {
                                stringResource(R.string.sleep_log_right_on_your_need)
                            } else {
                                stringResource(R.string.sleep_log_1_s_over_your_need, formatDuration(diff))
                            }
                        else -> stringResource(R.string.sleep_log_1_s_short_of_your, formatDuration(-diff), formatDuration(need))
                    }
                )
            },
            color = PurpleDeep,
            fontSize = TextSize.BodySmall,
            fontWeight = FontWeight.SemiBold
        )
        if (minutes > 16 * 60) {
            Text(
                stringResource(R.string.sleep_log_that_s_over_16_hours),
                color = Maroon,
                fontSize = TextSize.Caption,
                modifier = Modifier.padding(top = Space.Xs)
            )
        }
        Spacer(Modifier.height(Space.L))
        Text(stringResource(R.string.sleep_log_how_rested_do_you_feel), fontSize = TextSize.Body, fontWeight = FontWeight.SemiBold)
        Spacer(Modifier.height(Space.S))
        Row(horizontalArrangement = Arrangement.spacedBy(Space.S)) {
            qualityLabels.forEachIndexed { i, label ->
                val value = i + 1
                val on = quality == value
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .clip(Shapes.Medium)
                        .background(if (on) Color.White else Color.White.copy(alpha = 0.55f))
                        .border(2.dp, if (on) Ink else Color.Transparent, Shapes.Medium)
                        .clickable { onQuality(value) }
                        .padding(vertical = Space.S),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Box(Modifier.size(14.dp).clip(CircleShape).background(moodScoreColors[i]))
                    Spacer(Modifier.height(Space.S))
                    Text(label, fontSize = TextSize.Caption, fontWeight = FontWeight.SemiBold, maxLines = 1)
                }
            }
        }
        Spacer(Modifier.height(Space.L))
        Row(horizontalArrangement = Arrangement.spacedBy(Space.S)) {
            onCancel?.let {
                AppButton(stringResource(R.string.common_cancel), variant = ButtonVariant.OnDarkPrimary, modifier = Modifier.weight(1f), onClick = it)
            }
            Box(
                Modifier
                    .weight(if (onCancel != null) 1.4f else 1f)
                    .height(52.dp)
                    .clip(Shapes.Pill)
                    .background(if (quality != null) Ink else Disabled)
                    .clickable(enabled = quality != null, onClick = onSave),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    when {
                        quality == null -> stringResource(R.string.sleep_log_pick_how_rested_you_feel)
                        SleepLogStore.on(date) == null -> stringResource(R.string.sleep_log_save_1_s_points, SleepLogStore.POINTS_PER_LOG)
                        else -> stringResource(R.string.sleep_log_save_changes)
                    },
                    color = Color.White,
                    fontSize = TextSize.BodyLarge,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }
    }
}

@Composable
internal fun TimeTile(label: String, icon: ImageVector, time: LocalTime, modifier: Modifier, onChange: (LocalTime) -> Unit, onPick: () -> Unit) {
    Column(modifier = modifier.clip(Shapes.Card).background(Color.White).padding(Space.M, Space.M)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(icon, contentDescription = null, tint = Purple, modifier = Modifier.size(15.dp))
            Spacer(Modifier.width(Space.S))
            Text(label, color = TextSecondary, fontSize = TextSize.Caption, fontWeight = FontWeight.Medium)
        }
        Text(
            formatClock(time),
            fontSize = TextSize.TitleLarge,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(vertical = Space.Xs).clip(Shapes.Small).clickable(onClick = onPick)
        )
        Row(horizontalArrangement = Arrangement.spacedBy(Space.S)) {
            StepButton(Icons.Filled.Remove, "15 minutes earlier") { onChange(time.minusMinutes(15)) }
            StepButton(Icons.Filled.Add, "15 minutes later") { onChange(time.plusMinutes(15)) }
        }
    }
}

@Composable
internal fun StepButton(icon: ImageVector, description: String, onClick: () -> Unit) {
    Box(Modifier.size(34.dp).clip(CircleShape).background(ChipNeutral).clickable(onClick = onClick), contentAlignment = Alignment.Center) {
        Icon(icon, contentDescription = description, tint = Ink, modifier = Modifier.size(16.dp))
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun TimePickerDialog(title: String, initial: LocalTime, onConfirm: (LocalTime) -> Unit, onDismiss: () -> Unit) {
    val state = rememberTimePickerState(initial.hour, initial.minute, is24Hour = DateFormat.is24HourFormat(LocalContext.current))
    ReMakeUDialog(
        onDismissRequest = onDismiss,
        title = title,
        secondaryText = "Cancel",
        primaryText = "Set",
        onPrimary = { onConfirm(LocalTime.of(state.hour, state.minute)) },
        content = {
            Box(Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                TimePicker(
                    state = state,
                    colors = TimePickerDefaults.colors(
                        clockDialColor = ChipNeutral,
                        selectorColor = Ink,
                        timeSelectorSelectedContainerColor = Ink,
                        timeSelectorSelectedContentColor = Color.White,
                        timeSelectorUnselectedContainerColor = ChipNeutral,
                        periodSelectorSelectedContainerColor = Ink,
                        periodSelectorSelectedContentColor = Color.White
                    )
                )
            }
        }
    )
}
