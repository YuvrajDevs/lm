package com.glowup.app.audio

import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.content.pm.ServiceInfo
import android.media.AudioAttributes
import android.media.AudioFocusRequest
import android.media.AudioFormat
import android.media.AudioManager
import android.media.AudioTrack
import android.os.Build
import android.os.IBinder
import android.os.SystemClock
import androidx.compose.runtime.mutableStateOf
import androidx.core.app.NotificationCompat
import androidx.core.app.ServiceCompat
import androidx.core.content.ContextCompat
import com.glowup.app.R
import com.glowup.app.notify.Notifications

/** Plays a synthesized sleep sound in the foreground, fading out when the sleep timer runs out. */
class SleepSoundService : Service() {

    companion object {
        private const val ACTION_PLAY = "com.glowup.app.audio.PLAY"
        private const val ACTION_STOP = "com.glowup.app.audio.STOP"
        private const val EXTRA_SOUND = "sound"
        private const val EXTRA_MINUTES = "minutes"
        private const val NOTIFICATION_ID = 7001
        private const val SAMPLE_RATE = 44_100
        private const val FADE_IN_SECONDS = 4.0
        private const val FADE_OUT_SECONDS = 20.0

        /** The sound currently playing, observed by the Sleep screen. */
        val playing = mutableStateOf<String?>(null)

        /** Elapsed-realtime millis at which playback stops. */
        val endsAtElapsed = mutableStateOf(0L)

        fun play(context: Context, sound: String, minutes: Int) {
            val intent = Intent(context, SleepSoundService::class.java)
                .setAction(ACTION_PLAY)
                .putExtra(EXTRA_SOUND, sound)
                .putExtra(EXTRA_MINUTES, minutes)
            ContextCompat.startForegroundService(context, intent)
        }

        fun stop(context: Context) {
            if (playing.value == null) return
            context.startService(Intent(context, SleepSoundService::class.java).setAction(ACTION_STOP))
        }
    }

    @Volatile private var session = 0
    @Volatile private var stopRequested = false
    private val running get() = playing.value != null
    private var focusRequest: AudioFocusRequest? = null

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            ACTION_PLAY -> {
                val sound = intent.getStringExtra(EXTRA_SOUND) ?: "Night Rain"
                val minutes = intent.getIntExtra(EXTRA_MINUTES, 30)
                startInForeground(sound, minutes)
                startPlayback(sound, minutes)
            }
            ACTION_STOP -> requestStop()
            else -> stopSelf()
        }
        return START_NOT_STICKY
    }

    private fun startInForeground(sound: String, minutes: Int) {
        val stopIntent = PendingIntent.getService(
            this, 1, Intent(this, SleepSoundService::class.java).setAction(ACTION_STOP),
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT,
        )
        val notification = NotificationCompat.Builder(this, Notifications.CHANNEL_PLAYBACK)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle(sound)
            .setContentText("Playing · stops in $minutes min")
            .setContentIntent(Notifications.openAppIntent(this, "sleep", NOTIFICATION_ID))
            .addAction(0, "Stop", stopIntent)
            .setOngoing(true)
            .setSilent(true)
            .build()
        ServiceCompat.startForeground(
            this, NOTIFICATION_ID, notification,
            if (Build.VERSION.SDK_INT >= 29) ServiceInfo.FOREGROUND_SERVICE_TYPE_MEDIA_PLAYBACK else 0,
        )
    }

    private fun startPlayback(sound: String, minutes: Int) {
        val token = ++session
        if (focusRequest == null) requestFocus()

        val durationMillis = minutes * 60_000L
        endsAtElapsed.value = SystemClock.elapsedRealtime() + durationMillis
        playing.value = sound
        stopRequested = false

        Thread({ playLoop(token, sound, durationMillis) }, "sleep-sound").start()
    }

    private fun playLoop(token: Int, sound: String, durationMillis: Long) {
        val minBuffer = AudioTrack.getMinBufferSize(SAMPLE_RATE, AudioFormat.CHANNEL_OUT_MONO, AudioFormat.ENCODING_PCM_FLOAT)
        val track = AudioTrack.Builder()
            .setAudioAttributes(AudioAttributes.Builder().setUsage(AudioAttributes.USAGE_MEDIA).setContentType(AudioAttributes.CONTENT_TYPE_MUSIC).build())
            .setAudioFormat(AudioFormat.Builder().setSampleRate(SAMPLE_RATE).setEncoding(AudioFormat.ENCODING_PCM_FLOAT).setChannelMask(AudioFormat.CHANNEL_OUT_MONO).build())
            .setBufferSizeInBytes(maxOf(minBuffer, SAMPLE_RATE))
            .setTransferMode(AudioTrack.MODE_STREAM)
            .build()
        val synth = SoundSynth(sound, SAMPLE_RATE)
        val buffer = FloatArray(2048)
        val totalSamples = durationMillis / 1000.0 * SAMPLE_RATE
        val stopFadeSamples = 1.5 * SAMPLE_RATE
        var written = 0L
        var stopFadeFrom = -1L
        track.play()

        while (session == token) {
            if (stopRequested && stopFadeFrom < 0) stopFadeFrom = written
            synth.fill(buffer)
            for (i in buffer.indices) {
                val position = (written + i).toDouble()
                val fadeIn = (position / (FADE_IN_SECONDS * SAMPLE_RATE)).coerceAtMost(1.0)
                val fadeOut = ((totalSamples - position) / (FADE_OUT_SECONDS * SAMPLE_RATE)).coerceIn(0.0, 1.0)
                val manualFade = if (stopFadeFrom >= 0) (1.0 - (position - stopFadeFrom) / stopFadeSamples).coerceIn(0.0, 1.0) else 1.0
                buffer[i] = (buffer[i] * fadeIn * fadeOut * manualFade).toFloat()
            }
            track.write(buffer, 0, buffer.size, AudioTrack.WRITE_BLOCKING)
            written += buffer.size
            val stopFinished = stopFadeFrom >= 0 && written - stopFadeFrom > stopFadeSamples
            if (written >= totalSamples || stopFinished) break
        }

        track.stop()
        track.release()
        if (session == token) ContextCompat.getMainExecutor(this).execute { if (session == token) finish() }
    }

    /** Fades out in the background; the UI sees playback as stopped immediately so taps during the fade can't restart it. */
    private fun requestStop() {
        if (running) {
            stopRequested = true
            playing.value = null
        } else {
            finish()
        }
    }

    private fun finish() {
        session++
        playing.value = null
        abandonFocus()
        ServiceCompat.stopForeground(this, ServiceCompat.STOP_FOREGROUND_REMOVE)
        stopSelf()
    }

    private fun requestFocus() {
        val manager = getSystemService(AudioManager::class.java)
        val request = AudioFocusRequest.Builder(AudioManager.AUDIOFOCUS_GAIN)
            .setAudioAttributes(AudioAttributes.Builder().setUsage(AudioAttributes.USAGE_MEDIA).setContentType(AudioAttributes.CONTENT_TYPE_MUSIC).build())
            .setOnAudioFocusChangeListener { change ->
                if (change == AudioManager.AUDIOFOCUS_LOSS) requestStop()
            }
            .build()
        manager.requestAudioFocus(request)
        focusRequest = request
    }

    private fun abandonFocus() {
        focusRequest?.let { getSystemService(AudioManager::class.java).abandonAudioFocusRequest(it) }
        focusRequest = null
    }

    override fun onDestroy() {
        session++
        playing.value = null
        abandonFocus()
        super.onDestroy()
    }
}
