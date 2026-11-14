package com.glowup.app.ui.screens.speaking

import android.content.Intent
import android.media.MediaPlayer
import android.media.MediaRecorder
import android.os.Build
import android.os.Bundle
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
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
import androidx.compose.runtime.mutableStateListOf
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
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import com.glowup.app.BuildConfig
import com.glowup.app.ui.components.ConsentSheet
import com.glowup.app.ui.components.goBack
import com.glowup.app.ui.components.rememberMicPermission
import com.glowup.app.ui.data.Consent
import com.glowup.app.ui.data.ConsentStore
import com.glowup.app.ui.data.LevelSample
import com.glowup.app.ui.data.ProgressStore
import com.glowup.app.ui.data.Scenario
import com.glowup.app.ui.data.SpeechAnalysis
import com.glowup.app.ui.data.SpeechReport
import com.glowup.app.ui.data.SpeechStore
import com.glowup.app.ui.theme.Coral
import com.glowup.app.ui.theme.Ink
import kotlinx.coroutines.delay
import java.io.File
import kotlin.random.Random
import com.glowup.app.ui.design.LineHeight
import com.glowup.app.ui.design.Shapes
import com.glowup.app.ui.design.Sizes
import com.glowup.app.ui.design.TextSize
import com.glowup.app.ui.theme.Lilac
import com.glowup.app.ui.theme.Mint
import com.glowup.app.ui.theme.Peach
import com.glowup.app.ui.theme.Sun
import android.app.Activity
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

private val randomTopics = listOf(
    "Weekend", "A food you love", "Your favourite season", "A skill you want to learn",
    "The last trip you took", "A movie that stuck with you", "Your morning routine",
    "Someone who inspires you", "A small win this week", "Your ideal day off",
)

private const val COMPLETION_POINTS = 5

private enum class Phase { READY, THINK, RECORDING, REVIEW }
private enum class Mode { ANALYSE, LISTEN }

@Composable
fun SpeakingPracticeScreen(navController: NavHostController, topic: String? = null, scenario: Scenario? = null) {
    val context = LocalContext.current
    val displayTopic = remember { scenario?.prompt ?: topic ?: randomTopics[Random.nextInt(randomTopics.size)] }
    val totalSeconds = scenario?.speakSeconds ?: 60
    val file = remember { File(context.cacheDir, "speaking_practice.m4a") }
    val recognitionAvailable = remember { SpeechRecognizer.isRecognitionAvailable(context) }

    var mode by remember { mutableStateOf(if (recognitionAvailable) Mode.ANALYSE else Mode.LISTEN) }
    var phase by remember { mutableStateOf(Phase.READY) }
    var remaining by remember { mutableStateOf(totalSeconds) }
    var thinkLeft by remember { mutableStateOf(scenario?.thinkSeconds ?: 0) }
    var recordedSeconds by remember { mutableStateOf(0) }
    var hasRecording by remember { mutableStateOf(false) }
    var micDenied by remember { mutableStateOf(false) }
    var level by remember { mutableStateOf(0f) }
    var isPlayingBack by remember { mutableStateOf(false) }
    var awarded by remember { mutableStateOf(false) }
    var recorder by remember { mutableStateOf<MediaRecorder?>(null) }
    var player by remember { mutableStateOf<MediaPlayer?>(null) }
    var askConsent by remember { mutableStateOf(false) }
    var report by remember { mutableStateOf<SpeechReport?>(null) }
    var finalText by remember { mutableStateOf("") }
    var partial by remember { mutableStateOf("") }
    val levels = remember { mutableStateListOf<LevelSample>() }
    var startedAt by remember { mutableStateOf(0L) }
    var recognizer by remember { mutableStateOf<SpeechRecognizer?>(null) }

    fun transcript() = (finalText + " " + partial).trim()

    // ---- Analyse mode: the phone's speech recogniser, restarted whenever it stops on silence ----
    fun recognizerIntent() = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
        putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
        putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS, true)
        putExtra(RecognizerIntent.EXTRA_PREFER_OFFLINE, true)
        putExtra(RecognizerIntent.EXTRA_SPEECH_INPUT_COMPLETE_SILENCE_LENGTH_MILLIS, 4000L)
        putExtra(RecognizerIntent.EXTRA_SPEECH_INPUT_POSSIBLY_COMPLETE_SILENCE_LENGTH_MILLIS, 4000L)
    }

    fun startRecognizer() {
        val r = recognizer ?: SpeechRecognizer.createSpeechRecognizer(context).also { recognizer = it }
        r.setRecognitionListener(object : RecognitionListener {
            override fun onReadyForSpeech(params: Bundle?) {}
            override fun onBeginningOfSpeech() {}
            override fun onRmsChanged(rmsdB: Float) {
                level = ((rmsdB + 2f) / 12f).coerceIn(0f, 1f)
                if (phase == Phase.RECORDING) levels += LevelSample(System.currentTimeMillis() - startedAt, rmsdB)
            }
            override fun onBufferReceived(buffer: ByteArray?) {}
            override fun onEndOfSpeech() {}
            override fun onError(error: Int) {
                if (phase == Phase.RECORDING) runCatching { r.startListening(recognizerIntent()) }
            }
            override fun onResults(results: Bundle?) {
                results?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)?.firstOrNull()?.let { finalText = (finalText + " " + it).trim() }
                partial = ""
                if (phase == Phase.RECORDING) runCatching { r.startListening(recognizerIntent()) }
            }
            override fun onPartialResults(partialResults: Bundle?) {
                partialResults?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)?.firstOrNull()?.let { partial = it }
            }
            override fun onEvent(eventType: Int, params: Bundle?) {}
        })
        runCatching { r.startListening(recognizerIntent()) }
    }

    fun stop() {
        recordedSeconds = totalSeconds - remaining
        level = 0f
        if (mode == Mode.ANALYSE) {
            recognizer?.stopListening()
            phase = Phase.REVIEW
            val text = transcript()
            val r = SpeechAnalysis.analyse(text, recordedSeconds.coerceAtLeast(1), levels.toList())
            report = r
            if (text.isNotBlank()) SpeechStore.save(scenario?.title ?: displayTopic, r, text)
        } else {
            recorder?.let { active ->
                hasRecording = runCatching { active.stop() }.isSuccess
                active.release()
            }
            recorder = null
            phase = Phase.REVIEW
        }
    }

    fun beginSpeaking(withMic: Boolean) {
        remaining = totalSeconds
        hasRecording = false
        finalText = ""; partial = ""; levels.clear(); report = null
        startedAt = System.currentTimeMillis()
        phase = Phase.RECORDING
        if (!withMic) return
        if (mode == Mode.ANALYSE) startRecognizer()
        else recorder = runCatching {
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

    var micGranted by remember { mutableStateOf(false) }
    fun afterPermission(granted: Boolean) {
        micGranted = granted
        micDenied = !granted
        if (scenario != null && scenario.thinkSeconds > 0) { thinkLeft = scenario.thinkSeconds; phase = Phase.THINK } else beginSpeaking(granted)
    }
    val requestMic = rememberMicPermission { afterPermission(it) }
    fun startPractice() {
        if (ConsentStore.has(Consent.MICROPHONE)) requestMic() else askConsent = true
    }

    LaunchedEffect(phase) {
        if (phase == Phase.THINK) {
            while (thinkLeft > 0 && phase == Phase.THINK) { delay(1000); thinkLeft-- }
            if (phase == Phase.THINK) beginSpeaking(micGranted)
        }
        if (phase != Phase.RECORDING) return@LaunchedEffect
        var ticks = 0
        while (phase == Phase.RECORDING && remaining > 0) {
            delay(100)
            if (mode == Mode.LISTEN) level = recorder?.let { (it.maxAmplitude / 12_000f).coerceIn(0f, 1f) } ?: 0f
            if (++ticks % 10 == 0) remaining--
        }
        if (phase == Phase.RECORDING) stop()
    }

    val view = LocalView.current
    DisposableEffect(Unit) {
        val window = (view.context as? Activity)?.window
        val bars = window?.let { WindowCompat.getInsetsController(it, view) }
        bars?.isAppearanceLightStatusBars = false
        bars?.isAppearanceLightNavigationBars = false
        view.keepScreenOn = true
        onDispose {
            bars?.isAppearanceLightStatusBars = true
            bars?.isAppearanceLightNavigationBars = true
            view.keepScreenOn = false
            recorder?.let { runCatching { it.stop() }; it.release() }
            recognizer?.destroy()
            player?.release()
            file.delete()
        }
    }

    fun togglePlayback() {
        if (isPlayingBack) { player?.pause(); isPlayingBack = false; return }
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
            Row(Modifier.fillMaxWidth().padding(24.dp, 24.dp, 24.dp, 0.dp), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Box(Modifier.size(48.dp).clip(CircleShape).background(Color(0xFF1F1F22)).clickable { navController.goBack() }, contentAlignment = Alignment.Center) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = Color.White, modifier = Modifier.size(18.dp))
                }
                Text(
                    when (phase) {
                        Phase.READY -> if (totalSeconds >= 60) "${totalSeconds / 60} minute" else "$totalSeconds seconds"
                        Phase.THINK -> "Think · $thinkLeft"
                        Phase.RECORDING -> "%d:%02d left".format(remaining / 60, remaining % 60)
                        Phase.REVIEW -> "You spoke for ${recordedSeconds}s"
                    },
                    color = Color.White.copy(alpha = 0.6f), fontSize = TextSize.Body, fontWeight = FontWeight.SemiBold,
                )
                Spacer(Modifier.size(48.dp))
            }

            if (phase == Phase.REVIEW && mode == Mode.ANALYSE) {
                ReportView(report, transcript(), Modifier.weight(1f), onHistory = { navController.navigate("speech_history") })
            } else {
                Column(
                    Modifier.weight(1f).fillMaxWidth().verticalScroll(rememberScrollState()).padding(horizontal = 24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center,
                ) {
                    Spacer(Modifier.height(24.dp))
                    Text(
                        (scenario?.category?.uppercase() ?: "TALK FOR 1 MINUTE ABOUT"),
                        color = Color.White.copy(alpha = 0.45f), fontSize = TextSize.Caption, fontWeight = FontWeight.SemiBold, letterSpacing = 0.5.sp, textAlign = TextAlign.Center,
                    )
                    Spacer(Modifier.height(8.dp))
                    Text(
                        if (scenario != null) displayTopic else "\"$displayTopic\"",
                        color = Color.White, fontSize = if (displayTopic.length > 40) 21.sp else if (displayTopic.length > 16) 24.sp else 34.sp,
                        fontWeight = FontWeight.Bold, textAlign = TextAlign.Center, lineHeight = LineHeight.Display,
                    )
                    scenario?.let {
                        Spacer(Modifier.height(10.dp))
                        Text("Tip: ${it.tip}", color = Mint, fontSize = TextSize.BodySmall, textAlign = TextAlign.Center, lineHeight = LineHeight.Compact)
                    }
                    Spacer(Modifier.height(28.dp))
                    Box(Modifier.size(170.dp), contentAlignment = Alignment.Center) {
                        if (phase == Phase.RECORDING) Box(Modifier.size(130.dp).scale(pulse).alpha(0.3f).clip(CircleShape).background(Coral))
                        val canPlay = phase == Phase.REVIEW && hasRecording
                        Box(
                            Modifier.size(110.dp).clip(CircleShape).background(if (canPlay) Color.White else if (phase == Phase.THINK) Color(0xFF2A2A2E) else Coral)
                                .clickable(enabled = canPlay) { togglePlayback() },
                            contentAlignment = Alignment.Center,
                        ) {
                            if (phase == Phase.THINK) Text("$thinkLeft", color = Color.White, fontSize = TextSize.Number, fontWeight = FontWeight.Bold)
                            else Icon(
                                when { !canPlay -> Icons.Filled.Mic; isPlayingBack -> Icons.Filled.Pause; else -> Icons.Filled.PlayArrow },
                                contentDescription = if (canPlay) "Play recording" else null,
                                tint = if (canPlay) Ink else Color.White, modifier = Modifier.size(36.dp),
                            )
                        }
                    }
                    Spacer(Modifier.height(20.dp))
                    if (phase == Phase.RECORDING && mode == Mode.ANALYSE) {
                        Text(
                            transcript().ifBlank { "Start talking — your words will appear here" }.takeLast(220),
                            color = if (transcript().isBlank()) Color.White.copy(alpha = 0.4f) else Color.White.copy(alpha = 0.85f),
                            fontSize = TextSize.BodyLarge, lineHeight = LineHeight.Relaxed, textAlign = TextAlign.Center, modifier = Modifier.widthIn(max = 320.dp),
                        )
                    } else {
                        Text(
                            when {
                                phase == Phase.THINK -> "Plan your first sentence and your last one. Recording starts automatically."
                                phase == Phase.REVIEW && hasRecording -> "Tap play to listen back. Notice your pace, pauses and filler words."
                                micDenied -> "No microphone access, so nothing is recorded — you can still practise against the timer."
                                mode == Mode.ANALYSE -> "Re:MakeU turns your words into text on your phone to measure pace, filler words and pauses. No audio is saved."
                                else -> "The recording stays on this phone and is deleted the moment you leave this screen."
                            },
                            color = Color.White.copy(alpha = 0.45f), fontSize = TextSize.BodySmall, lineHeight = LineHeight.Body, textAlign = TextAlign.Center, modifier = Modifier.widthIn(max = 290.dp),
                        )
                    }
                    if (phase == Phase.READY && recognitionAvailable) {
                        Spacer(Modifier.height(18.dp))
                        Row(Modifier.clip(Shapes.Pill).background(Color.White.copy(alpha = 0.08f)).padding(4.dp)) {
                            listOf(Mode.ANALYSE to "Analyse", Mode.LISTEN to "Listen back").forEach { (m, label) ->
                                Text(
                                    label, color = if (mode == m) Ink else Color.White, fontSize = TextSize.BodySmall, fontWeight = FontWeight.SemiBold,
                                    modifier = Modifier.clip(Shapes.Pill).background(if (mode == m) Color.White else Color.Transparent).clickable { mode = m }.padding(16.dp, 8.dp),
                                )
                            }
                        }
                    }
                    if (BuildConfig.DEBUG && phase == Phase.RECORDING && mode == Mode.ANALYSE) {
                        Text(
                            "Developer: use sample speech", color = Lilac, fontSize = TextSize.Caption, modifier = Modifier.padding(top = 12.dp).clickable {
                                finalText = "So um I think the best trip I took was to the mountains last year and like we basically hiked every single day you know and the views were amazing I mean honestly it was the the best week"
                                (0 until 300).forEach { i -> levels += LevelSample(i * 100L, if (i in 120..150) -1f else 4f + (i % 9)) }
                                remaining = totalSeconds - 32
                            },
                        )
                    }
                    Spacer(Modifier.height(24.dp))
                }
            }

            Column(Modifier.padding(24.dp, 0.dp, 24.dp, 32.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                when (phase) {
                    Phase.READY -> WhiteButton(if (scenario != null && scenario.thinkSeconds > 0) "Start · ${scenario.thinkSeconds}s to think" else "Start speaking") { startPractice() }
                    Phase.THINK -> WhiteButton("I'm ready — start now") { beginSpeaking(micGranted) }
                    Phase.RECORDING -> WhiteButton("Stop") { stop() }
                    Phase.REVIEW -> {
                        WhiteButton("Done") { finish() }
                        Text(
                            "Try again", color = Color.White.copy(alpha = 0.7f), fontSize = TextSize.Body, fontWeight = FontWeight.SemiBold,
                            modifier = Modifier.align(Alignment.CenterHorizontally).clickable {
                                player?.release(); player = null; isPlayingBack = false
                                phase = Phase.READY
                            }.padding(8.dp),
                        )
                    }
                }
            }
        }

        if (askConsent) {
            ConsentSheet(
                Consent.MICROPHONE, Icons.Filled.Mic,
                onAllow = { askConsent = false; requestMic() },
                onDecline = { askConsent = false; afterPermission(false) },
            )
        }
    }
}

@Composable
private fun ReportView(report: SpeechReport?, transcript: String, modifier: Modifier, onHistory: () -> Unit) {
    Column(modifier.fillMaxWidth().verticalScroll(rememberScrollState()).padding(24.dp, 20.dp, 24.dp, 16.dp)) {
        if (report == null || transcript.isBlank()) {
            Text("We couldn't hear any words", color = Color.White, fontSize = TextSize.TitleLarge, fontWeight = FontWeight.Bold)
            Text(
                "Check your microphone isn't muted and speak a little louder. Speech recognition needs your phone's speech services — or switch to Listen back.",
                color = Color.White.copy(alpha = 0.6f), fontSize = TextSize.Body, lineHeight = LineHeight.Body, modifier = Modifier.padding(top = 6.dp),
            )
            return@Column
        }
        Text("YOUR REPORT", color = Color.White.copy(alpha = 0.5f), fontSize = TextSize.Caption, fontWeight = FontWeight.Bold, letterSpacing = 0.6.sp)
        Spacer(Modifier.height(12.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            Metric("${report.wpm}", "words / min", when { report.wpm in 125..165 -> Mint; report.wpm > 175 || report.wpm < 100 -> Peach; else -> Sun }, Modifier.weight(1f))
            Metric("${report.fillerTotal}", "fillers", if (report.fillersPerMinute < 2) Mint else if (report.fillersPerMinute < 4) Sun else Peach, Modifier.weight(1f))
            Metric("${report.longPauses}", "long pauses", if (report.longPauses <= 3) Mint else Sun, Modifier.weight(1f))
        }
        Spacer(Modifier.height(10.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            Metric("${report.variety}", "voice variety", if (report.variety >= 55) Mint else if (report.variety >= 35) Sun else Peach, Modifier.weight(1f))
            Metric("${report.words}", "words", Color.White, Modifier.weight(1f))
            Metric("${report.longestPauseSeconds}s", "longest pause", Color.White, Modifier.weight(1f))
        }
        Text("Target pace: 130–160 words a minute.", color = Color.White.copy(alpha = 0.45f), fontSize = TextSize.Caption, modifier = Modifier.padding(top = 8.dp))

        val good = report.wentWell()
        if (good.isNotEmpty()) {
            Spacer(Modifier.height(20.dp))
            Text("What went well", color = Mint, fontSize = TextSize.BodyLarge, fontWeight = FontWeight.Bold)
            good.forEach { Note(it.title, it.body) }
        }
        val work = report.workOn()
        if (work.isNotEmpty()) {
            Spacer(Modifier.height(16.dp))
            Text("Work on next", color = Sun, fontSize = TextSize.BodyLarge, fontWeight = FontWeight.Bold)
            work.forEach { Note(it.title, it.body) }
        }
        if (report.fillers.isNotEmpty()) {
            Spacer(Modifier.height(16.dp))
            Text("Filler words: " + report.fillers.joinToString(" · ") { "\"${it.word}\" ×${it.count}" }, color = Color.White.copy(alpha = 0.7f), fontSize = TextSize.BodySmall, lineHeight = LineHeight.Compact)
        }

        Spacer(Modifier.height(20.dp))
        Text("Transcript", color = Color.White, fontSize = TextSize.BodyLarge, fontWeight = FontWeight.Bold)
        Spacer(Modifier.height(6.dp))
        val fillerWords = report.fillers.map { it.word }.toSet()
        Text(
            buildAnnotatedString {
                val tokens = transcript.split(" ").filter { it.isNotBlank() }
                val clean = tokens.map { it.lowercase().trim(',', '.', '?', '!') }
                // Mark single fillers and both words of two-word fillers ("you know", "I mean").
                val marked = BooleanArray(tokens.size) { clean[it] in fillerWords }
                for (i in 0 until tokens.size - 1) if ("${clean[i]} ${clean[i + 1]}" in fillerWords) { marked[i] = true; marked[i + 1] = true }
                tokens.forEachIndexed { i, w ->
                    if (marked[i]) withStyle(SpanStyle(color = Peach, fontWeight = FontWeight.Bold)) { append(w) } else append(w)
                    append(" ")
                }
            },
            color = Color.White.copy(alpha = 0.8f), fontSize = TextSize.Body, lineHeight = LineHeight.Relaxed,
        )
        Text("Speech recognition can miss \"um\" and \"uh\", so the real filler count may be a little higher.", color = Color.White.copy(alpha = 0.4f), fontSize = TextSize.Caption, lineHeight = LineHeight.Tight, modifier = Modifier.padding(top = 10.dp))
        Text(
            "See your progress over time →", color = Lilac, fontSize = TextSize.Body, fontWeight = FontWeight.SemiBold,
            modifier = Modifier.padding(top = 16.dp).clickable(onClick = onHistory),
        )
    }
}

@Composable
private fun Metric(value: String, label: String, color: Color, modifier: Modifier) {
    Column(modifier.clip(Shapes.Medium).background(Color.White.copy(alpha = 0.07f)).padding(12.dp)) {
        Text(value, color = color, fontSize = TextSize.Headline, fontWeight = FontWeight.Bold)
        Text(label, color = Color.White.copy(alpha = 0.55f), fontSize = TextSize.Caption)
    }
}

@Composable
private fun Note(title: String, body: String) {
    Column(Modifier.fillMaxWidth().padding(top = 8.dp).clip(Shapes.Medium).background(Color.White.copy(alpha = 0.07f)).padding(14.dp, 12.dp)) {
        Text(title, color = Color.White, fontSize = TextSize.Body, fontWeight = FontWeight.SemiBold)
        Text(body, color = Color.White.copy(alpha = 0.65f), fontSize = TextSize.BodySmall, lineHeight = LineHeight.Compact)
    }
}

@Composable
private fun WhiteButton(label: String, onClick: () -> Unit) {
    Box(Modifier.fillMaxWidth().height(Sizes.Button).clip(Shapes.Pill).background(Color.White).clickable(onClick = onClick), contentAlignment = Alignment.Center) {
        Text(label, color = Ink, fontSize = TextSize.BodyLarge, fontWeight = FontWeight.SemiBold)
    }
}
