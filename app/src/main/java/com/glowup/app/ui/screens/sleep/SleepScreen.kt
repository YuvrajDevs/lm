package com.glowup.app.ui.screens.sleep

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.border
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.ui.unit.sp
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
import com.glowup.app.ui.components.GlowUpDialog
import com.glowup.app.ui.components.GlowUpOptionRow
import com.glowup.app.ui.components.rememberNotificationPermission
import com.glowup.app.ui.data.SleepStore
import com.glowup.app.ui.data.formatClock
import com.glowup.app.ui.theme.*
import kotlinx.coroutines.delay
import java.time.Duration
import java.time.LocalTime

private data class SoundTile(val label: String, val tint: Color, val icon: Int)

private val sounds = listOf(
    SoundTile("Night Rain", BlueTint, R.drawable.ic_moon),
    SoundTile("Forest", GreenTint, R.drawable.ic_tree),
    SoundTile("White Noise", PurpleTint, R.drawable.ic_ocean),
    SoundTile("Ocean Waves", CoralTint, R.drawable.ic_ocean),
    SoundTile("Crackling Fire", MaroonTint, R.drawable.ic_sun),
    SoundTile("Deep Space", NavyTint, R.drawable.ic_moon),
    SoundTile("Soft Wind", GreenTint, R.drawable.ic_tree),
    SoundTile("Brown Noise", PurpleTint, R.drawable.ic_ocean),
)

private val bedtimeOptions = listOf(LocalTime.of(21, 30), LocalTime.of(22, 0), LocalTime.of(22, 30), LocalTime.of(23, 0), LocalTime.of(23, 30), LocalTime.MIDNIGHT, LocalTime.of(0, 30))
private val wakeOptions = listOf(LocalTime.of(5, 0), LocalTime.of(5, 30), LocalTime.of(6, 0), LocalTime.of(6, 30), LocalTime.of(7, 0), LocalTime.of(7, 30), LocalTime.of(8, 0), LocalTime.of(8, 30), LocalTime.of(9, 0))

@Composable
fun SleepScreen(navController: NavHostController) {
    val context = LocalContext.current
    val selectedSound = SleepStore.sound
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
    val sleepLabel = "${sleepHours.toHours()}h${if (sleepHours.toMinutesPart() > 0) " ${sleepHours.toMinutesPart()}m" else ""}"

    Box(modifier = Modifier.fillMaxSize().background(Surface).systemBarsPadding()) {
        Column(modifier = Modifier.fillMaxSize()) {
            DetailHeader("Sleep") { navController.goBack() }

            LazyColumn(
                modifier = Modifier.weight(1f),
                contentPadding = PaddingValues(24.dp, 16.dp, 24.dp, 16.dp),
            ) {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(28.dp))
                            .background(PurpleTint)
                            .padding(20.dp, 18.dp, 86.dp, 18.dp),
                    ) {
                        Column(modifier = Modifier.fillMaxWidth(0.72f)) {
                            Text("YOUR SLEEP WINDOW", color = Color(0xFF6B4FD1), fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
                            Spacer(Modifier.height(2.dp))
                            Text(formatClock(SleepStore.bedtime), fontSize = 26.sp, fontWeight = FontWeight.Bold)
                            Spacer(Modifier.height(2.dp))
                            Text("Up at ${formatClock(SleepStore.wakeTime)} · $sleepLabel of sleep", color = Color(0xFF5A4A8C), fontSize = 12.5.sp)
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
                                    .clip(RoundedCornerShape(22.dp))
                                    .background(CardNeutral)
                                    .clickable { onClick() }
                                    .padding(16.dp, 14.dp),
                                verticalArrangement = Arrangement.spacedBy(6.dp),
                            ) {
                                Text(label, color = TextSecondary, fontSize = 11.5.sp, fontWeight = FontWeight.Medium)
                                Text(time, fontSize = 18.sp, fontWeight = FontWeight.Bold, color = if (time == "Off") TextMuted else Ink)
                            }
                        }
                    }
                    if ((SleepStore.wakeAlarmOn || SleepStore.bedtimeAlarmOn) && !exactAllowed && Build.VERSION.SDK_INT >= 31) {
                        Spacer(Modifier.height(10.dp))
                        Text(
                            "Alarms may arrive a few minutes late. Tap to allow exact alarms.",
                            color = Maroon,
                            fontSize = 12.sp,
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(14.dp))
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
                    Text("Sleep sounds", fontSize = 15.sp, fontWeight = FontWeight.SemiBold)
                    Text("Works offline · keeps playing with the screen off", color = TextSecondary, fontSize = 12.sp, fontWeight = FontWeight.Medium)
                    Spacer(Modifier.height(12.dp))
                }

                items(sounds.chunked(2)) { pair ->
                    Row(horizontalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.fillMaxWidth()) {
                        pair.forEach { sound ->
                            val isSelected = selectedSound == sound.label
                            Column(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(22.dp))
                                    .background(sound.tint)
                                    .border(2.dp, if (isSelected) Ink else Color.Transparent, RoundedCornerShape(22.dp))
                                    .clickable {
                                        SleepStore.selectSound(sound.label)
                                        if (playingSound != null) SleepSoundService.play(context, sound.label, timerMinutes)
                                    }
                                    .padding(16.dp),
                                verticalArrangement = Arrangement.spacedBy(16.dp),
                            ) {
                                Image(painterResource(sound.icon), contentDescription = null, modifier = Modifier.size(28.dp))
                                Text(sound.label, fontSize = 13.5.sp, fontWeight = FontWeight.SemiBold)
                            }
                        }
                    }
                    Spacer(Modifier.height(10.dp))
                }

                item {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(22.dp))
                            .background(CardNeutral)
                            .clickable { showTimerDialog = true }
                            .padding(18.dp, 16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Column {
                            Text("Sleep timer", fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
                            Text("Stops after $timerMinutes minutes", color = TextSecondary, fontSize = 12.sp)
                        }
                        Icon(Icons.Filled.ChevronRight, contentDescription = null, tint = TextSecondary, modifier = Modifier.size(16.dp))
                    }
                    Spacer(Modifier.height(12.dp))
                }

                item {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(22.dp))
                            .background(Yellow)
                            .clickable { navController.navigate("morning") }
                            .padding(18.dp, 16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Column {
                            Text("Morning routine", fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
                            Text("Meditation, breathing & stretching", color = Color(0xFF7A6600), fontSize = 12.sp)
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
                        .clip(RoundedCornerShape(999.dp))
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
                            fontSize = 15.5.sp,
                            fontWeight = FontWeight.SemiBold,
                        )
                        if (playingSound != null) {
                            Text("Fades out in about $minutesLeft min", color = Color.White.copy(alpha = 0.6f), fontSize = 11.5.sp)
                        }
                    }
                }
            }
        }

        if (showTimerDialog) {
            GlowUpDialog(
                onDismissRequest = { showTimerDialog = false },
                title = "Sleep timer",
                primaryText = "Close",
                secondaryText = null,
                content = {
                    Column {
                        listOf(10, 15, 30, 45, 60, 90, 120).forEach { minutes ->
                            GlowUpOptionRow("$minutes minutes", selected = minutes == timerMinutes) {
                                SleepStore.selectTimer(minutes)
                                if (playingSound != null) SleepSoundService.play(context, playingSound!!, minutes)
                            }
                        }
                    }
                },
            )
        }

        if (showBedtimeDialog) {
            GlowUpDialog(
                onDismissRequest = { showBedtimeDialog = false },
                title = "Bedtime alarm",
                primaryText = "Close",
                secondaryText = null,
                content = {
                    Column {
                        GlowUpOptionRow("Off", selected = !SleepStore.bedtimeAlarmOn) { SleepStore.updateBedtime(null) }
                        bedtimeOptions.forEach { time ->
                            GlowUpOptionRow(formatClock(time), selected = SleepStore.bedtimeAlarmOn && time == SleepStore.bedtime) {
                                SleepStore.updateBedtime(time)
                                askNotifications()
                            }
                        }
                    }
                },
            )
        }

        if (showWakeDialog) {
            GlowUpDialog(
                onDismissRequest = { showWakeDialog = false },
                title = "Wake-up alarm",
                primaryText = "Close",
                secondaryText = null,
                content = {
                    Column {
                        GlowUpOptionRow("Off", selected = !SleepStore.wakeAlarmOn) { SleepStore.updateWakeTime(null) }
                        wakeOptions.forEach { time ->
                            GlowUpOptionRow(formatClock(time), selected = SleepStore.wakeAlarmOn && time == SleepStore.wakeTime) {
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
