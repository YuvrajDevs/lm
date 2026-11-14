package com.glowup.app.ui.data

import androidx.compose.runtime.mutableStateMapOf
import java.time.LocalDate
import org.json.JSONObject

/**
 * What the user agreed to share with a feature, and when. Every feature that touches the camera,
 * microphone, body data or app usage asks once through a ConsentSheet before the system prompt,
 * and the Your data screen lists these so they can be withdrawn.
 */
enum class Consent(val key: String, val title: String, val summary: String, val collects: String, val purpose: String, val storage: String) {
    MICROPHONE(
        key = "mic",
        title = "Microphone for speaking practice",
        summary = "Listens only while you practise, to analyse your pace and filler words or let you play it back. No audio is kept.",
        collects = "Audio from your microphone, only while the practice timer is running. In Analyse mode it's turned into text by your phone's speech recogniser.",
        purpose = "To measure your pace, filler words and pauses, or let you play your answer back.",
        storage = "Audio is never saved — the listen-back file is deleted when you leave. Only the text and numbers from Analyse mode are kept, on this phone."
    ),
    FACE_SCAN(
        key = "face",
        title = "Face & posture scan",
        summary = "Measures your face and posture from the live camera. No photo is taken or saved — only the numbers are kept.",
        collects = "Live camera frames, read on your phone for a few seconds. No photo is taken, saved or uploaded.",
        purpose = "To estimate your face shape, a symmetry baseline and your neck angle, so advice fits you and you can track change.",
        storage = "Only the numbers (like \"Oval\", \"92\" or \"71°\") are saved, on this phone. Delete them any time in Your data."
    ),
    PROGRESS_PHOTOS(
        key = "photos",
        title = "Progress photos",
        summary = "Photos you choose to take for side-by-side progress. Kept inside the app only.",
        collects = "The progress photos you take — front, side or body.",
        purpose = "So you can compare how you look week to week, side by side.",
        storage = "Inside the app only: never in your gallery, never backed up, never uploaded. Delete any photo, or all of them, any time."
    ),
    APP_USAGE(
        key = "usage",
        title = "App blocker",
        summary = "Checks which app is open, only to compare it with your block list. Nothing about what you do in apps is kept.",
        collects = "The name of the app on screen, checked about once a second while blocking is on. No content, messages or history.",
        purpose = "To show the block or pause screen when you open an app you chose.",
        storage = "Only your lists and daily counts (like \"3 blocks\") are saved, on this phone, for 30 days."
    ),
    WARDROBE_PHOTOS(
        key = "wardrobe",
        title = "Wardrobe photos",
        summary = "Photos of your clothes, kept inside the app to show your closet and outfits.",
        collects = "The photos you take or pick of your clothes. We read the main colour from the photo on your phone.",
        purpose = "To show your closet, build outfits from what you own and track what you wear.",
        storage = "Inside the app only — never in your gallery, never backed up or uploaded. Deleting an item deletes its photo."
    ),
    BODY(
        key = "body",
        title = "Your body measurements",
        summary = "Height, weight and waist you type in, used for your fitness trend and safety checks.",
        collects = "Only what you type: height, weight and (optionally) waist. No photos, no health apps.",
        purpose = "To show your weekly trend, warn you if you're losing weight too fast, and work out waist-to-height.",
        storage = "On this phone only. Delete it any time from Settings → Your data → Workouts & body."
    )
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
