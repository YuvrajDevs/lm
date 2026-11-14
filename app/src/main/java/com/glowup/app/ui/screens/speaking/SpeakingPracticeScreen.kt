package com.glowup.app.ui.screens.speaking

import android.app.Activity
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
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.view.WindowCompat
import androidx.navigation.NavHostController
import com.glowup.app.BuildConfig
import com.glowup.app.R
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
import com.glowup.app.ui.design.AppButton
import com.glowup.app.ui.design.ButtonVariant
import com.glowup.app.ui.design.LineHeight
import com.glowup.app.ui.design.Shapes
import com.glowup.app.ui.design.Space
import com.glowup.app.ui.design.TextSize
import com.glowup.app.ui.theme.Coral
import com.glowup.app.ui.theme.Ink
import com.glowup.app.ui.theme.Lilac
import com.glowup.app.ui.theme.Mint
import com.glowup.app.ui.theme.Peach
import com.glowup.app.ui.theme.Sun
import java.io.File
import kotlin.random.Random
import kotlinx.coroutines.delay

private val randomTopics = listOf(
    "Weekend", "A food you love", "Your favourite season", "A skill you want to learn",
    "The last trip you took", "A movie that stuck with you", "Your morning routine",
    "Someone who inspires you", "A small win this week", "Your ideal day off"
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
            override fun onReadyForSpeech(params: Bundle?) = Unit
            override fun onBeginningOfSpeech() = Unit
            override fun onRmsChanged(rmsdB: Float) {
                level = ((rmsdB + 2f) / 12f).coerceIn(0f, 1f)
                if (phase == Phase.RECORDING) levels += LevelSample(System.currentTimeMillis() - startedAt, rmsdB)
            }
            override fun onBufferReceived(buffer: ByteArray?) = Unit
            override fun onEndOfSpeech() = Unit
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
            override fun onEvent(eventType: Int, params: Bundle?) = Unit
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
        finalText = ""
        partial = ""
        levels.clear()
        report = null
        startedAt = System.currentTimeMillis()
        phase = Phase.RECORDING
        if (!withMic) return
        if (mode == Mode.ANALYSE) {
            startRecognizer()
        } else {
            recorder = runCatching {
                (
                    if (Build.VERSION.SDK_INT >= 31) {
                        MediaRecorder(context)
                    } else {
                        @Suppress("DEPRECATION")
                        MediaRecorder()
                    }
                    ).apply {
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
    }

    var micGranted by remember { mutableStateOf(false) }
    fun afterPermission(granted: Boolean) {
        micGranted = granted
        micDenied = !granted
        if (scenario != null && scenario.thinkSeconds > 0) {
            thinkLeft = scenario.thinkSeconds
            phase = Phase.THINK
        } else {
            beginSpeaking(granted)
        }
    }
    val requestMic = rememberMicPermission { afterPermission(it) }
    fun startPractice() {
        if (ConsentStore.has(Consent.MICROPHONE)) requestMic() else askConsent = true
    }

    LaunchedEffect(phase) {
        if (phase == Phase.THINK) {
            while (thinkLeft > 0 && phase == Phase.THINK) {
                delay(1000)
                thinkLeft--
            }
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
            recorder?.let {
                runCatching { it.stop() }
                it.release()
            }
            recognizer?.destroy()
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
                Modifier.fillMaxWidth().padding(Space.Gutter, Space.Gutter, Space.Gutter, 0.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    Modifier.size(48.dp).clip(CircleShape).background(Color(0xFF1F1F22)).clickable {
                        navController.goBack()
                    },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = stringResource(R.string.common_back),
                        tint = Color.White,
                        modifier = Modifier.size(18.dp)
                    )
                }
                Text(
                    when (phase) {
                        Phase.READY -> if (totalSeconds >=
                            60
                        ) {
                            stringResource(R.string.speaking_practice_1_s_minute, totalSeconds / 60)
                        } else {
                            stringResource(R.string.speaking_practice_1_s_seconds, totalSeconds)
                        }
                        Phase.THINK -> stringResource(R.string.speaking_practice_think_1_s, thinkLeft)
                        Phase.RECORDING -> stringResource(R.string.speaking_practice_d_02d_left).format(remaining / 60, remaining % 60)
                        Phase.REVIEW -> stringResource(R.string.speaking_practice_you_spoke_for_1_ss, recordedSeconds)
                    },
                    color = Color.White.copy(alpha = 0.6f),
                    fontSize = TextSize.Body,
                    fontWeight = FontWeight.SemiBold
                )
                Spacer(Modifier.size(48.dp))
            }

            if (phase == Phase.REVIEW && mode == Mode.ANALYSE) {
                ReportView(report, transcript(), Modifier.weight(1f), onHistory = { navController.navigate("speech_history") })
            } else {
                Column(
                    Modifier.weight(1f).fillMaxWidth().verticalScroll(rememberScrollState()).padding(horizontal = Space.Gutter),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Spacer(Modifier.height(Space.Gutter))
                    Text(
                        (scenario?.category?.uppercase() ?: stringResource(R.string.speaking_practice_talk_for_1_minute_about)),
                        color = Color.White.copy(alpha = 0.45f),
                        fontSize = TextSize.Caption,
                        fontWeight = FontWeight.SemiBold,
                        letterSpacing = 0.5.sp,
                        textAlign = TextAlign.Center
                    )
                    Spacer(Modifier.height(Space.S))
                    Text(
                        if (scenario != null) displayTopic else "\"$displayTopic\"",
                        color = Color.White,
                        fontSize = if (displayTopic.length > 40) {
                            21.sp
                        } else if (displayTopic.length > 16) {
                            24.sp
                        } else {
                            34.sp
                        },
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center,
                        lineHeight = LineHeight.Display
                    )
                    scenario?.let {
                        Spacer(Modifier.height(Space.S))
                        Text(
                            stringResource(R.string.speaking_practice_tip_1_s, it.tip),
                            color = Mint,
                            fontSize = TextSize.BodySmall,
                            textAlign = TextAlign.Center,
                            lineHeight = LineHeight.Compact
                        )
                    }
                    Spacer(Modifier.height(Space.Section))
                    Box(Modifier.size(170.dp), contentAlignment = Alignment.Center) {
                        if (phase == Phase.RECORDING) Box(Modifier.size(130.dp).scale(pulse).alpha(0.3f).clip(CircleShape).background(Coral))
                        val canPlay = phase == Phase.REVIEW && hasRecording
                        Box(
                            Modifier
                                .size(110.dp)
                                .clip(CircleShape)
                                .background(
                                    when {
                                        canPlay -> Color.White
                                        phase == Phase.THINK -> Color.White.copy(alpha = 0.12f)
                                        else -> Coral
                                    }
                                )
                                // Only a button once there is a recording to play; otherwise it's a status light.
                                .then(if (canPlay) Modifier.clickable { togglePlayback() } else Modifier),
                            contentAlignment = Alignment.Center
                        ) {
                            if (phase == Phase.THINK) {
                                Text("$thinkLeft", color = Color.White, fontSize = TextSize.Number, fontWeight = FontWeight.Bold)
                            } else {
                                Icon(
                                    when {
                                        !canPlay -> Icons.Filled.Mic
                                        isPlayingBack -> Icons.Filled.Pause
                                        else -> Icons.Filled.PlayArrow
                                    },
                                    contentDescription = when {
                                        !canPlay -> null
                                        isPlayingBack -> "Pause recording"
                                        else -> "Play recording"
                                    },
                                    tint = if (canPlay) Ink else Color.White,
                                    modifier = Modifier.size(36.dp)
                                )
                            }
                        }
                    }
                    Spacer(Modifier.height(Space.Xl))
                    if (phase == Phase.RECORDING && mode == Mode.ANALYSE) {
                        Text(
                            transcript().ifBlank { "Start talking — your words will appear here" }.takeLast(220),
                            color = if (transcript().isBlank()) Color.White.copy(alpha = 0.4f) else Color.White.copy(alpha = 0.85f),
                            fontSize = TextSize.BodyLarge,
                            lineHeight = LineHeight.Relaxed,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.widthIn(max = 320.dp)
                        )
                    } else {
                        Text(
                            when {
                                phase == Phase.THINK -> stringResource(R.string.speaking_practice_plan_your_first_sentence_and)
                                phase == Phase.REVIEW && hasRecording -> stringResource(R.string.speaking_practice_tap_play_to_listen_back)
                                micDenied -> stringResource(R.string.speaking_practice_no_microphone_access_so_nothing)
                                mode == Mode.ANALYSE -> stringResource(R.string.speaking_practice_re_makeu_turns_your_words)
                                else -> stringResource(R.string.speaking_practice_the_recording_stays_on_this)
                            },
                            color = Color.White.copy(alpha = 0.45f),
                            fontSize = TextSize.BodySmall,
                            lineHeight = LineHeight.Body,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.widthIn(max = 290.dp)
                        )
                    }
                    if (phase == Phase.READY && recognitionAvailable) {
                        Spacer(Modifier.height(Space.L))
                        Row(Modifier.clip(Shapes.Pill).background(Color.White.copy(alpha = 0.08f)).padding(Space.Xs)) {
                            listOf(Mode.ANALYSE to "Analyse", Mode.LISTEN to "Listen back").forEach { (m, label) ->
                                Text(
                                    label,
                                    color = if (mode == m) Ink else Color.White,
                                    fontSize = TextSize.BodySmall,
                                    fontWeight = FontWeight.SemiBold,
                                    modifier = Modifier.clip(Shapes.Pill).background(
                                        if (mode ==
                                            m
                                        ) {
                                            Color.White
                                        } else {
                                            Color.Transparent
                                        }
                                    ).clickable { mode = m }.padding(Space.L, Space.S)
                                )
                            }
                        }
                    }
                    if (BuildConfig.DEBUG && phase == Phase.RECORDING && mode == Mode.ANALYSE) {
                        Text(
                            stringResource(R.string.speaking_practice_developer_use_sample_speech),
                            color = Lilac,
                            fontSize = TextSize.Caption,
                            modifier = Modifier.padding(top = Space.M).clickable {
                                finalText =
                                    "So um I think the best trip I took was to the mountains last year and like we basically hiked every single day you know and the views were amazing I mean honestly it was the the best week"
                                (0 until 300).forEach { i -> levels += LevelSample(i * 100L, if (i in 120..150) -1f else 4f + (i % 9)) }
                                remaining = totalSeconds - 32
                            }
                        )
                    }
                    Spacer(Modifier.height(Space.Gutter))
                }
            }

            Column(Modifier.padding(Space.Gutter, 0.dp, Space.Gutter, Space.Xxl), verticalArrangement = Arrangement.spacedBy(Space.S)) {
                when (phase) {
                    Phase.READY -> WhiteButton(
                        if (scenario != null &&
                            scenario.thinkSeconds > 0
                        ) {
                            stringResource(R.string.speaking_practice_start_1_ss_to_think, scenario.thinkSeconds)
                        } else {
                            stringResource(R.string.speaking_practice_start_speaking)
                        }
                    ) { startPractice() }
                    Phase.THINK -> WhiteButton(stringResource(R.string.speaking_practice_i_m_ready_start_now)) { beginSpeaking(micGranted) }
                    Phase.RECORDING -> WhiteButton(stringResource(R.string.speaking_practice_stop)) { stop() }
                    Phase.REVIEW -> {
                        WhiteButton(stringResource(R.string.common_done)) { finish() }
                        Text(
                            stringResource(R.string.speaking_practice_try_again),
                            color = Color.White.copy(alpha = 0.7f),
                            fontSize = TextSize.Body,
                            fontWeight = FontWeight.SemiBold,
                            modifier = Modifier.align(Alignment.CenterHorizontally).clickable {
                                player?.release()
                                player = null
                                isPlayingBack = false
                                phase = Phase.READY
                            }.padding(Space.S)
                        )
                    }
                }
            }
        }

        if (askConsent) {
            ConsentSheet(
                Consent.MICROPHONE,
                Icons.Filled.Mic,
                onAllow = {
                    askConsent = false
                    requestMic()
                },
                onDecline = {
                    askConsent = false
                    afterPermission(false)
                }
            )
        }
    }
}

@Composable
private fun ReportView(report: SpeechReport?, transcript: String, modifier: Modifier, onHistory: () -> Unit) {
    Column(modifier.fillMaxWidth().verticalScroll(rememberScrollState()).padding(Space.Gutter, Space.Xl, Space.Gutter, Space.L)) {
        if (report == null || transcript.isBlank()) {
            Text(
                stringResource(R.string.speaking_practice_we_couldn_t_hear_any),
                color = Color.White,
                fontSize = TextSize.TitleLarge,
                fontWeight = FontWeight.Bold
            )
            Text(
                stringResource(R.string.speaking_practice_check_your_microphone_isn_t),
                color = Color.White.copy(alpha = 0.6f),
                fontSize = TextSize.Body,
                lineHeight = LineHeight.Body,
                modifier = Modifier.padding(top = Space.S)
            )
            return@Column
        }
        Text(
            stringResource(R.string.speaking_practice_your_report),
            color = Color.White.copy(alpha = 0.5f),
            fontSize = TextSize.Caption,
            fontWeight = FontWeight.Bold,
            letterSpacing = 0.6.sp
        )
        Spacer(Modifier.height(Space.M))
        Row(horizontalArrangement = Arrangement.spacedBy(Space.S)) {
            Metric(
                "${report.wpm}",
                "words / min",
                when {
                    report.wpm in 125..165 -> Mint
                    report.wpm > 175 || report.wpm < 100 -> Peach
                    else -> Sun
                },
                Modifier.weight(1f)
            )
            Metric(
                "${report.fillerTotal}",
                "fillers",
                if (report.fillersPerMinute <
                    2
                ) {
                    Mint
                } else if (report.fillersPerMinute < 4) {
                    Sun
                } else {
                    Peach
                },
                Modifier.weight(1f)
            )
            Metric("${report.longPauses}", "long pauses", if (report.longPauses <= 3) Mint else Sun, Modifier.weight(1f))
        }
        Spacer(Modifier.height(Space.S))
        Row(horizontalArrangement = Arrangement.spacedBy(Space.S)) {
            Metric(
                "${report.variety}",
                "voice variety",
                if (report.variety >= 55) {
                    Mint
                } else if (report.variety >= 35) {
                    Sun
                } else {
                    Peach
                },
                Modifier.weight(1f)
            )
            Metric("${report.words}", "words", Color.White, Modifier.weight(1f))
            Metric("${report.longestPauseSeconds}s", "longest pause", Color.White, Modifier.weight(1f))
        }
        Text(
            stringResource(R.string.speaking_practice_target_pace_130_160_words),
            color = Color.White.copy(alpha = 0.45f),
            fontSize = TextSize.Caption,
            modifier = Modifier.padding(top = Space.S)
        )

        val good = report.wentWell()
        if (good.isNotEmpty()) {
            Spacer(Modifier.height(Space.Xl))
            Text(stringResource(R.string.speaking_practice_what_went_well), color = Mint, fontSize = TextSize.BodyLarge, fontWeight = FontWeight.Bold)
            good.forEach { Note(it.title, it.body) }
        }
        val work = report.workOn()
        if (work.isNotEmpty()) {
            Spacer(Modifier.height(Space.L))
            Text(stringResource(R.string.speaking_practice_work_on_next), color = Sun, fontSize = TextSize.BodyLarge, fontWeight = FontWeight.Bold)
            work.forEach { Note(it.title, it.body) }
        }
        if (report.fillers.isNotEmpty()) {
            Spacer(Modifier.height(Space.L))
            Text(
                stringResource(R.string.speaking_practice_filler_words) + report.fillers.joinToString(" · ") {
                    "\"${it.word}\" ×${it.count}"
                },
                color = Color.White.copy(alpha = 0.7f),
                fontSize = TextSize.BodySmall,
                lineHeight = LineHeight.Compact
            )
        }

        Spacer(Modifier.height(Space.Xl))
        Text(stringResource(R.string.speaking_practice_transcript), color = Color.White, fontSize = TextSize.BodyLarge, fontWeight = FontWeight.Bold)
        Spacer(Modifier.height(Space.S))
        val fillerWords = report.fillers.map { it.word }.toSet()
        Text(
            buildAnnotatedString {
                val tokens = transcript.split(" ").filter { it.isNotBlank() }
                val clean = tokens.map { it.lowercase().trim(',', '.', '?', '!') }
                // Mark single fillers and both words of two-word fillers ("you know", "I mean").
                val marked = BooleanArray(tokens.size) { clean[it] in fillerWords }
                for (i in 0 until tokens.size - 1) {
                    if ("${clean[i]} ${clean[i + 1]}" in fillerWords) {
                        marked[i] = true
                        marked[i + 1] = true
                    }
                }
                tokens.forEachIndexed { i, w ->
                    if (marked[i]) withStyle(SpanStyle(color = Peach, fontWeight = FontWeight.Bold)) { append(w) } else append(w)
                    append(" ")
                }
            },
            color = Color.White.copy(alpha = 0.8f),
            fontSize = TextSize.Body,
            lineHeight = LineHeight.Relaxed
        )
        Text(
            stringResource(R.string.speaking_practice_speech_recognition_can_miss_um),
            color = Color.White.copy(alpha = 0.4f),
            fontSize = TextSize.Caption,
            lineHeight = LineHeight.Tight,
            modifier = Modifier.padding(top = Space.S)
        )
        Text(
            stringResource(R.string.speaking_practice_see_your_progress_over_time),
            color = Lilac,
            fontSize = TextSize.Body,
            fontWeight = FontWeight.SemiBold,
            modifier = Modifier.padding(top = Space.L).clickable(onClick = onHistory)
        )
    }
}

@Composable
private fun Metric(value: String, label: String, color: Color, modifier: Modifier) {
    Column(modifier.clip(Shapes.Medium).background(Color.White.copy(alpha = 0.07f)).padding(Space.M)) {
        Text(value, color = color, fontSize = TextSize.Headline, fontWeight = FontWeight.Bold)
        Text(label, color = Color.White.copy(alpha = 0.55f), fontSize = TextSize.Caption)
    }
}

@Composable
private fun Note(title: String, body: String) {
    Column(Modifier.fillMaxWidth().padding(top = Space.S).clip(Shapes.Medium).background(Color.White.copy(alpha = 0.07f)).padding(Space.M, Space.M)) {
        Text(title, color = Color.White, fontSize = TextSize.Body, fontWeight = FontWeight.SemiBold)
        Text(body, color = Color.White.copy(alpha = 0.65f), fontSize = TextSize.BodySmall, lineHeight = LineHeight.Compact)
    }
}

@Composable
private fun WhiteButton(label: String, onClick: () -> Unit) {
    AppButton(label, variant = ButtonVariant.OnDarkPrimary, onClick = onClick)
}
