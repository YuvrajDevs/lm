package com.glowup.app.ui.data

import androidx.compose.runtime.mutableStateMapOf
import org.json.JSONObject
import java.time.LocalDate

/**
 * What the user agreed to share with a feature, and when. Every feature that touches the camera,
 * microphone, body data or app usage asks once through a ConsentSheet before the system prompt,
 * and the Your data screen lists these so they can be withdrawn.
 */
enum class Consent(
    val key: String,
    val title: String,
    val summary: String,
    val collects: String,
    val purpose: String,
    val storage: String,
) {
    MICROPHONE(
        key = "mic",
        title = "Microphone for speaking practice",
        summary = "Records only while you practise so you can play it back. Deleted when you leave the screen.",
        collects = "Audio from your microphone, only while the practice timer is running.",
        purpose = "So you can play your answer back and hear how you sound.",
        storage = "A temporary file on this phone, deleted as soon as you leave the practice screen. Never uploaded.",
    ),
    FACE_SCAN(
        key = "face",
        title = "Face & posture scan",
        summary = "Measures your face and posture from the live camera. No photo is taken or saved — only the numbers are kept.",
        collects = "Live camera frames, read on your phone for a few seconds. No photo is taken, saved or uploaded.",
        purpose = "To estimate your face shape, a symmetry baseline and your neck angle, so advice fits you and you can track change.",
        storage = "Only the numbers (like \"Oval\", \"92\" or \"71°\") are saved, on this phone. Delete them any time in Your data.",
    ),
    PROGRESS_PHOTOS(
        key = "photos",
        title = "Progress photos",
        summary = "Photos you choose to take for side-by-side progress. Kept inside the app only.",
        collects = "The progress photos you take — front, side or body.",
        purpose = "So you can compare how you look week to week, side by side.",
        storage = "Inside the app only: never in your gallery, never backed up, never uploaded. Delete any photo, or all of them, any time.",
    ),
    BODY(
        key = "body",
        title = "Your body measurements",
        summary = "Height, weight and waist you type in, used for your fitness trend and safety checks.",
        collects = "Only what you type: height, weight and (optionally) waist. No photos, no health apps.",
        purpose = "To show your weekly trend, warn you if you're losing weight too fast, and work out waist-to-height.",
        storage = "On this phone only. Delete it any time from Settings → Your data → Workouts & body.",
    ),
    ;

    companion object {
        fun fromKey(key: String) = entries.firstOrNull { it.key == key }
    }
}

object ConsentStore {
    private const val KEY = "consents"

    /** Consent → ISO date it was given. */
    val granted = mutableStateMapOf<Consent, String>()

    fun has(consent: Consent) = consent in granted

    fun grant(consent: Consent, today: LocalDate = LocalDate.now()) {
        granted[consent] = today.toString()
        save()
    }

    fun withdraw(consent: Consent) {
        granted.remove(consent)
        save()
    }

    fun load() {
        granted.clear()
        val json = Storage.read(KEY)?.let(::JSONObject) ?: return
        json.keys().forEach { key -> Consent.fromKey(key)?.let { granted[it] = json.getString(key) } }
    }

    private fun save() {
        Storage.write(KEY, JSONObject(granted.mapKeys { it.key.key }).toString())
    }
}
