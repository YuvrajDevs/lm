package com.glowup.app.ui.screens.sleep

import com.glowup.app.ui.components.ProBadge
import com.glowup.app.ui.data.ProStore
import com.glowup.app.ui.data.ProGate
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.border
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Air
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Forest
import androidx.compose.material.icons.filled.Grain
import androidx.compose.material.icons.filled.GraphicEq
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material.icons.filled.NightsStay
import androidx.compose.material.icons.filled.WaterDrop
import androidx.compose.material.icons.filled.Waves
import androidx.compose.ui.graphics.vector.ImageVector
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
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.glowup.app.ui.components.goBack
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.SystemClock
import android.provider.Settings
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.compose.LifecycleResumeEffect
import com.glowup.app.R
import com.glowup.app.audio.SleepSoundService
import com.glowup.app.notify.AlarmScheduler
import com.glowup.app.ui.components.DetailHeader
import com.glowup.app.ui.components.ReMakeUDialog
import com.glowup.app.ui.components.ReMakeUOptionRow
import com.glowup.app.ui.components.rememberNotificationPermission
import com.glowup.app.ui.data.SleepStore
import com.glowup.app.ui.data.formatClock
import com.glowup.app.ui.theme.*
import kotlinx.coroutines.delay
import java.time.Duration
import java.time.LocalTime
import com.glowup.app.ui.design.Shapes
import com.glowup.app.ui.design.TextSize
import com.glowup.app.ui.data.SleepLogStore
import com.glowup.app.ui.data.SleepMath
import com.glowup.app.ui.data.formatDuration
import java.time.LocalDate

private data class SoundTile(val label: String, val tint: Color, val accent: Color, val icon: ImageVector)

private val sounds = listOf(
    SoundTile("Night Rain", BlueTint, Navy, Icons.Filled.WaterDrop),
    SoundTile("Forest", GreenTint, Green, Icons.Filled.Forest),
    SoundTile("White Noise", PurpleTint, Purple, Icons.Filled.GraphicEq),
    SoundTile("Ocean Waves", CoralTint, Coral, Icons.Filled.Waves),
    SoundTile("Crackling Fire", MaroonTint, Maroon, Icons.Filled.LocalFireDepartment),
    SoundTile("Deep Space", NavyTint, Navy, Icons.Filled.NightsStay),
    SoundTile("Soft Wind", GreenTint, Green, Icons.Filled.Air),
    SoundTile("Brown Noise", PurpleTint, Purple, Icons.Filled.Grain),
)

private val bedtimeOptions = listOf(LocalTime.of(21, 30), LocalTime.of(22, 0), LocalTime.of(22, 30), LocalTime.of(23, 0), LocalTime.of(23, 30), LocalTime.MIDNIGHT, LocalTime.of(0, 30))
private val wakeOptions = listOf(LocalTime.of(5, 0), LocalTime.of(5, 30), LocalTime.of(6, 0), LocalTime.of(6, 30), LocalTime.of(7, 0), LocalTime.of(7, 30), LocalTime.of(8, 0), LocalTime.of(8, 30), LocalTime.of(9, 0))

@Composable
fun SleepScreen(navController: NavHostController) {
    val context = LocalContext.current
    val selectedSound = if (ProGate.soundLocked(SleepStore.sound)) "Night Rain" else SleepStore.sound
    val timerMinutes = SleepStore.timerMinutes
    val playingSound by SleepSoundService.playing
    val endsAt by SleepSoundService.endsAtElapsed
    var showTimerDialog by remember { mutableStateOf(false) }
    var showBedtimeDialog by remember { mutableStateOf(false) }
    var showWakeDialog by remember { mutableStateOf(false) }
    var exactAllowed by remember { mutableStateOf(AlarmScheduler.canScheduleExact(context)) }
    val askNotifications = rememberNotificationPermission()
    LifecycleResumeEffect(Unit) {
        exactAllowed = AlarmScheduler.canScheduleExact(context)
        if (exactAllowed) AlarmScheduler.syncAll(context)
        onPauseOrDispose { }
    }
    var minutesLeft by remember { mutableStateOf(0L) }
    LaunchedEffect(playingSound, endsAt) {
        while (playingSound != null) {
            minutesLeft = ((endsAt - SystemClock.elapsedRealtime()) / 60_000L + 1).coerceAtLeast(0)
            delay(5_000)
        }
    }
    val sleepHours = Duration.between(SleepStore.bedtime, SleepStore.wakeTime).let { if (it.isNegative) it.plusDays(1) else it }
    val sleepMinutesPart = sleepHours.toMinutes() % 60
    val sleepLabel = "${sleepHours.toHours()}h${if (sleepMinutesPart > 0) " ${sleepMinutesPart}m" else ""}"

    Box(modifier = Modifier.fillMaxSize().background(Surface).systemBarsPadding()) {
        Column(modifier = Modifier.fillMaxSize()) {
            DetailHeader("Sleep") { navController.goBack() }

            LazyColumn(
                modifier = Modifier.weight(1f),
                contentPadding = PaddingValues(24.dp, 16.dp, 24.dp, 16.dp),
            ) {
                item {
                    SleepLogSummary { navController.navigate("sleep_log") }
                    Spacer(Modifier.height(12.dp))
                }

                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(Shapes.Hero)
                            .background(PurpleTint)
                            .padding(20.dp, 18.dp, 86.dp, 18.dp),
                    ) {
                        Column(modifier = Modifier.fillMaxWidth(0.72f)) {
                            Text("YOUR SLEEP WINDOW", color = PurpleText, fontSize = TextSize.Micro, fontWeight = FontWeight.SemiBold)
                            Spacer(Modifier.height(2.dp))
                            Text(formatClock(SleepStore.bedtime), fontSize = TextSize.Display, fontWeight = FontWeight.Bold)
                            Spacer(Modifier.height(2.dp))
                            Text("Up at ${formatClock(SleepStore.wakeTime)} · $sleepLabel of sleep", color = PurpleDeep, fontSize = TextSize.BodySmall)
                        }
                        Image(painterResource(R.drawable.ic_sleep), contentDescription = null, modifier = Modifier.align(Alignment.BottomEnd).size(78.dp))
                    }
                    Spacer(Modifier.height(14.dp))
                }

                item {
                    Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        listOf(
                            Triple("Bedtime alarm", if (SleepStore.bedtimeAlarmOn) formatClock(SleepStore.bedtime) else "Off") { showBedtimeDialog = true },
                            Triple("Wake-up alarm", if (SleepStore.wakeAlarmOn) formatClock(SleepStore.wakeTime) else "Off") { showWakeDialog = true },
                        ).forEach { (label, time, onClick) ->
                            Column(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(Shapes.Card)
                                    .background(CardNeutral)
                                    .clickable { onClick() }
                                    .padding(16.dp, 14.dp),
                                verticalArrangement = Arrangement.spacedBy(6.dp),
                            ) {
                                Text(label, color = TextSecondary, fontSize = TextSize.Caption, fontWeight = FontWeight.Medium)
                                Text(time, fontSize = TextSize.Title, fontWeight = FontWeight.Bold, color = if (time == "Off") TextMuted else Ink)
                            }
                        }
                    }
                    if ((SleepStore.wakeAlarmOn || SleepStore.bedtimeAlarmOn) && !exactAllowed && Build.VERSION.SDK_INT >= 31) {
                        Spacer(Modifier.height(10.dp))
                        Text(
                            "Alarms may arrive a few minutes late. Tap to allow exact alarms.",
                            color = Maroon,
                            fontSize = TextSize.Caption,
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(Shapes.Small)
                                .background(MaroonTint)
                                .clickable {
                                    context.startActivity(
                                        Intent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM, Uri.parse("package:${context.packageName}")),
                                    )
                                }
                                .padding(14.dp, 10.dp),
                        )
                    }
                    Spacer(Modifier.height(22.dp))
                }

                item {
                    Text("Sleep sounds", fontSize = TextSize.BodyLarge, fontWeight = FontWeight.SemiBold)
                    Text("Works offline · keeps playing with the screen off", color = TextSecondary, fontSize = TextSize.Caption, fontWeight = FontWeight.Medium)
                    Spacer(Modifier.height(12.dp))
                }

                items(sounds.chunked(2)) { pair ->
                    Row(horizontalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.fillMaxWidth()) {
                        pair.forEach { sound ->
                            val isSelected = selectedSound == sound.label
                            Column(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(Shapes.Card)
                                    .background(sound.tint)
                                    .border(2.dp, if (isSelected) Ink else Color.Transparent, Shapes.Card)
                                    .clickable {
                                        if (ProGate.soundLocked(sound.label)) {
                                            ProStore.showPaywall("${sound.label} is a Pro sleep sound")
                                        } else {
                                            SleepStore.selectSound(sound.label)
                                            if (playingSound != null) SleepSoundService.play(context, sound.label, timerMinutes)
                                        }
                                    }
                                    .padding(16.dp),
                                verticalArrangement = Arrangement.spacedBy(16.dp),
                            ) {
                                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                    Icon(sound.icon, contentDescription = null, tint = sound.accent, modifier = Modifier.size(28.dp))
                                    if (ProGate.soundLocked(sound.label)) ProBadge()
                                }
                                Text(sound.label, fontSize = TextSize.Body, fontWeight = FontWeight.SemiBold)
                            }
                        }
                    }
                    Spacer(Modifier.height(10.dp))
                }

                item {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(Shapes.Card)
                            .background(CardNeutral)
                            .clickable { showTimerDialog = true }
                            .padding(18.dp, 16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Column {
                            Text("Sleep timer", fontSize = TextSize.Body, fontWeight = FontWeight.SemiBold)
                            Text("Stops after $timerMinutes minutes", color = TextSecondary, fontSize = TextSize.Caption)
                        }
                        Icon(Icons.Filled.ChevronRight, contentDescription = null, tint = TextSecondary, modifier = Modifier.size(16.dp))
                    }
                    Spacer(Modifier.height(12.dp))
                }

                item {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(Shapes.Card)
                            .background(Yellow)
                            .clickable { navController.navigate("morning") }
                            .padding(18.dp, 16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Column {
                            Text("Morning routine", fontSize = TextSize.Body, fontWeight = FontWeight.SemiBold)
                            Text("Meditation, breathing & stretching", color = Amber, fontSize = TextSize.Caption)
                        }
                        Icon(Icons.Filled.ChevronRight, contentDescription = null, tint = Ink, modifier = Modifier.size(16.dp))
                    }
                }
            }

            Box(modifier = Modifier.padding(24.dp, 8.dp, 24.dp, 28.dp)) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(60.dp)
                        .clip(Shapes.Pill)
                        .background(Ink)
                        .clickable {
                            if (playingSound != null) {
                                SleepSoundService.stop(context)
                            } else {
                                askNotifications()
                                SleepSoundService.play(context, selectedSound, timerMinutes)
                            }
                        },
                    contentAlignment = Alignment.Center,
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            if (playingSound != null) "Stop $playingSound" else "Play $selectedSound",
                            color = Color.White,
                            fontSize = TextSize.BodyLarge,
                            fontWeight = FontWeight.SemiBold,
                        )
                        if (playingSound != null) {
                            Text("Fades out in about $minutesLeft min", color = Color.White.copy(alpha = 0.6f), fontSize = TextSize.Caption)
                        }
                    }
                }
            }
        }

        if (showTimerDialog) {
            ReMakeUDialog(
                onDismissRequest = { showTimerDialog = false },
                title = "Sleep timer",
                primaryText = "Close",
                secondaryText = null,
                content = {
                    Column {
                        listOf(10, 15, 30, 45, 60, 90, 120).forEach { minutes ->
                            ReMakeUOptionRow("$minutes minutes", selected = minutes == timerMinutes) {
                                SleepStore.selectTimer(minutes)
                                if (playingSound != null) SleepSoundService.play(context, playingSound!!, minutes)
                            }
                        }
                    }
                },
            )
        }

        if (showBedtimeDialog) {
            ReMakeUDialog(
                onDismissRequest = { showBedtimeDialog = false },
                title = "Bedtime alarm",
                primaryText = "Close",
                secondaryText = null,
                content = {
                    Column {
                        ReMakeUOptionRow("Off", selected = !SleepStore.bedtimeAlarmOn) { SleepStore.updateBedtime(null) }
                        bedtimeOptions.forEach { time ->
                            ReMakeUOptionRow(formatClock(time), selected = SleepStore.bedtimeAlarmOn && time == SleepStore.bedtime) {
                                SleepStore.updateBedtime(time)
                                askNotifications()
                            }
                        }
                    }
                },
            )
        }

        if (showWakeDialog) {
            ReMakeUDialog(
                onDismissRequest = { showWakeDialog = false },
                title = "Wake-up alarm",
                primaryText = "Close",
                secondaryText = null,
                content = {
                    Column {
                        ReMakeUOptionRow("Off", selected = !SleepStore.wakeAlarmOn) { SleepStore.updateWakeTime(null) }
                        wakeOptions.forEach { time ->
                            ReMakeUOptionRow(formatClock(time), selected = SleepStore.wakeAlarmOn && time == SleepStore.wakeTime) {
                                SleepStore.updateWakeTime(time)
                                askNotifications()
                            }
                        }
                    }
                },
            )
        }
    }
}

@Composable
private fun SleepLogSummary(onClick: () -> Unit) {
    val today = LocalDate.now()
    val lastNight = SleepLogStore.on(today)
    val debt = SleepLogStore.debtMinutes(today)
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(Shapes.Card)
            .background(Ink)
            .clickable(onClick = onClick)
            .padding(18.dp, 16.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(modifier = Modifier.weight(1f)) {
            if (lastNight == null) {
                Text("Log last night's sleep", color = Color.White, fontSize = TextSize.BodyLarge, fontWeight = FontWeight.Bold)
                Text(
                    if (debt != null) "Sleep debt ${formatDuration(debt)} · 10-second check-in" else "Track your sleep debt and consistency",
                    color = Color.White.copy(alpha = 0.6f), fontSize = TextSize.BodySmall,
                )
            } else {
                Text("Last night · ${formatDuration(lastNight.minutes)}", color = Color.White, fontSize = TextSize.BodyLarge, fontWeight = FontWeight.Bold)
                Text(
                    debt?.let { "Sleep debt " + (if (it == 0) "none" else formatDuration(it)) + " · " + SleepMath.level(it).label.lowercase() }
                        ?: "Keep logging to see your sleep debt",
                    color = Color.White.copy(alpha = 0.6f), fontSize = TextSize.BodySmall,
                )
            }
        }
        Icon(Icons.Filled.ChevronRight, contentDescription = null, tint = Color.White, modifier = Modifier.size(18.dp))
    }
}
