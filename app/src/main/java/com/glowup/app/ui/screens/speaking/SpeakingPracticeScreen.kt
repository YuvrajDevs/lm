package com.glowup.app.ui.screens.speaking

import android.media.MediaPlayer
import android.media.MediaRecorder
import android.os.Build
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.Icon
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
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import com.glowup.app.ui.components.goBack
import com.glowup.app.ui.components.rememberMicPermission
import com.glowup.app.ui.components.ConsentSheet
import com.glowup.app.ui.data.Consent
import com.glowup.app.ui.data.ConsentStore
import com.glowup.app.ui.data.ProgressStore
import com.glowup.app.ui.theme.Coral
import com.glowup.app.ui.theme.Ink
import kotlinx.coroutines.delay
import java.io.File
import kotlin.random.Random

private val randomTopics = listOf(
    "Weekend", "A food you love", "Your favourite season", "A skill you want to learn",
    "The last trip you took", "A movie that stuck with you", "Your morning routine",
    "Someone who inspires you", "A small win this week", "Your ideal day off",
)

private const val PRACTICE_SECONDS = 60
private const val COMPLETION_POINTS = 5

private enum class Phase { READY, RECORDING, REVIEW }

@Composable
fun SpeakingPracticeScreen(navController: NavHostController, topic: String? = null) {
    val context = LocalContext.current
    val displayTopic = remember { topic ?: randomTopics[Random.nextInt(randomTopics.size)] }
    val file = remember { File(context.cacheDir, "speaking_practice.m4a") }

    var phase by remember { mutableStateOf(Phase.READY) }
    var remaining by remember { mutableStateOf(PRACTICE_SECONDS) }
    var recordedSeconds by remember { mutableStateOf(0) }
    var hasRecording by remember { mutableStateOf(false) }
    var micDenied by remember { mutableStateOf(false) }
    var level by remember { mutableStateOf(0f) }
    var isPlayingBack by remember { mutableStateOf(false) }
    var awarded by remember { mutableStateOf(false) }
    var recorder by remember { mutableStateOf<MediaRecorder?>(null) }
    var player by remember { mutableStateOf<MediaPlayer?>(null) }

    fun stopRecording() {
        recorder?.let { active ->
            hasRecording = runCatching { active.stop() }.isSuccess
            active.release()
        }
        recorder = null
        recordedSeconds = PRACTICE_SECONDS - remaining
        level = 0f
        phase = Phase.REVIEW
    }

    fun startRecording(withMic: Boolean) {
        remaining = PRACTICE_SECONDS
        hasRecording = false
        if (withMic) {
            recorder = runCatching {
                (if (Build.VERSION.SDK_INT >= 31) MediaRecorder(context) else @Suppress("DEPRECATION") MediaRecorder()).apply {
                    setAudioSource(MediaRecorder.AudioSource.MIC)
                    setOutputFormat(MediaRecorder.OutputFormat.MPEG_4)
                    setAudioEncoder(MediaRecorder.AudioEncoder.AAC)
                    setAudioSamplingRate(44_100)
                    setAudioEncodingBitRate(96_000)
                    setOutputFile(file.absolutePath)
                    prepare()
                    start()
                }
            }.getOrNull()
        }
        phase = Phase.RECORDING
    }

    val requestMic = rememberMicPermission { granted ->
        micDenied = !granted
        startRecording(withMic = granted)
    }
    var askConsent by remember { mutableStateOf(false) }
    fun startPractice() {
        if (ConsentStore.has(Consent.MICROPHONE)) requestMic() else askConsent = true
    }

    LaunchedEffect(phase) {
        if (phase != Phase.RECORDING) return@LaunchedEffect
        var ticks = 0
        while (phase == Phase.RECORDING && remaining > 0) {
            delay(100)
            level = recorder?.let { (it.maxAmplitude / 12_000f).coerceIn(0f, 1f) } ?: 0f
            if (++ticks % 10 == 0) remaining--
        }
        if (phase == Phase.RECORDING) stopRecording()
    }

    val view = androidx.compose.ui.platform.LocalView.current
    DisposableEffect(Unit) {
        val window = (view.context as? android.app.Activity)?.window
        val bars = window?.let { androidx.core.view.WindowCompat.getInsetsController(it, view) }
        bars?.isAppearanceLightStatusBars = false
        bars?.isAppearanceLightNavigationBars = false
        onDispose {
            bars?.isAppearanceLightStatusBars = true
            bars?.isAppearanceLightNavigationBars = true
            recorder?.let { runCatching { it.stop() }; it.release() }
            player?.release()
            file.delete()
        }
    }

    fun togglePlayback() {
        if (isPlayingBack) {
            player?.pause()
            isPlayingBack = false
            return
        }
        val active = player ?: MediaPlayer().apply {
            setDataSource(file.absolutePath)
            prepare()
            setOnCompletionListener { isPlayingBack = false }
        }.also { player = it }
        active.start()
        isPlayingBack = true
    }

    fun finish() {
        if (!awarded && recordedSeconds >= 20) {
            ProgressStore.award(COMPLETION_POINTS)
            awarded = true
        }
        navController.goBack()
    }

    val pulse by animateFloatAsState(targetValue = 1f + level * 0.35f, animationSpec = tween(120), label = "pulse")

    Box(modifier = Modifier.fillMaxSize().background(Ink).systemBarsPadding()) {
        Column(modifier = Modifier.fillMaxSize()) {
            Row(
                modifier = Modifier.fillMaxWidth().padding(24.dp, 24.dp, 24.dp, 0.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .clip(CircleShape)
                        .background(Color(0xFF1F1F22))
                        .clickable { navController.goBack() },
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = Color.White, modifier = Modifier.size(18.dp))
                }
                Text(
                    when (phase) {
                        Phase.READY -> "1 minute"
                        Phase.RECORDING -> "0:%02d left".format(remaining)
                        Phase.REVIEW -> "You spoke for ${recordedSeconds}s"
                    },
                    color = Color.White.copy(alpha = 0.6f),
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold,
                )
                Spacer(Modifier.size(48.dp))
            }

            Column(
                modifier = Modifier.weight(1f).fillMaxWidth().padding(horizontal = 24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center,
            ) {
                Text(
                    "TALK FOR 1 MINUTE ABOUT",
                    color = Color.White.copy(alpha = 0.45f),
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold,
                    letterSpacing = 0.5.sp,
                    textAlign = TextAlign.Center,
                )
                Spacer(Modifier.height(8.dp))
                Text(
                    "\"$displayTopic\"",
                    color = Color.White,
                    fontSize = if (displayTopic.length > 16) 24.sp else 34.sp,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center,
                )

                Spacer(Modifier.height(28.dp))

                Box(modifier = Modifier.size(170.dp), contentAlignment = Alignment.Center) {
                    if (phase == Phase.RECORDING) {
                        Box(
                            modifier = Modifier
                                .size(130.dp)
                                .scale(pulse)
                                .alpha(0.3f)
                                .clip(CircleShape)
                                .background(Coral),
                        )
                    }
                    Box(
                        modifier = Modifier
                            .size(110.dp)
                            .clip(CircleShape)
                            .background(if (phase == Phase.REVIEW && hasRecording) Color.White else Coral)
                            .clickable(enabled = phase == Phase.REVIEW && hasRecording) { togglePlayback() },
                        contentAlignment = Alignment.Center,
                    ) {
                        Icon(
                            when {
                                phase != Phase.REVIEW || !hasRecording -> Icons.Filled.Mic
                                isPlayingBack -> Icons.Filled.Pause
                                else -> Icons.Filled.PlayArrow
                            },
                            contentDescription = if (phase == Phase.REVIEW && hasRecording) "Play recording" else null,
                            tint = if (phase == Phase.REVIEW && hasRecording) Ink else Color.White,
                            modifier = Modifier.size(36.dp),
                        )
                    }
                }

                Spacer(Modifier.height(28.dp))

                Text(
                    when {
                        phase == Phase.REVIEW && hasRecording -> "Tap play to listen back. Notice your pace, pauses and filler words."
                        micDenied -> "No microphone access, so nothing is recorded — you can still practise against the timer."
                        else -> "Your voice is only yours. The recording stays on this phone and is deleted the moment you leave this screen."
                    },
                    color = Color.White.copy(alpha = 0.45f),
                    fontSize = 13.sp,
                    lineHeight = 19.sp,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.widthIn(max = 280.dp),
                )
            }

            Column(modifier = Modifier.padding(24.dp, 0.dp, 24.dp, 40.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                when (phase) {
                    Phase.READY -> WhiteButton("Start speaking") { startPractice() }
                    Phase.RECORDING -> WhiteButton("Stop") { stopRecording() }
                    Phase.REVIEW -> {
                        WhiteButton("Done") { finish() }
                        Text(
                            "Try again",
                            color = Color.White.copy(alpha = 0.7f),
                            fontSize = 14.sp,
                            fontWeight = FontWeight.SemiBold,
                            modifier = Modifier
                                .align(Alignment.CenterHorizontally)
                                .clickable {
                                    player?.release()
                                    player = null
                                    isPlayingBack = false
                                    startPractice()
                                }
                                .padding(8.dp),
                        )
                    }
                }
            }
        }

        if (askConsent) {
            ConsentSheet(
                Consent.MICROPHONE,
                androidx.compose.material.icons.Icons.Filled.Mic,
                onAllow = { askConsent = false; requestMic() },
                onDecline = { askConsent = false; micDenied = true; startRecording(withMic = false) },
            )
        }
    }
}

@Composable
private fun WhiteButton(label: String, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(60.dp)
            .clip(RoundedCornerShape(999.dp))
            .background(Color.White)
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        Text(label, color = Ink, fontSize = 15.5.sp, fontWeight = FontWeight.SemiBold)
    }
}
