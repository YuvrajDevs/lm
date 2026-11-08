package com.glowup.app.ui.data

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.glowup.app.BuildConfig
import com.glowup.app.ui.data.style.Outfit
import org.json.JSONObject

enum class PlanType { SUBSCRIPTION, ONE_TIME }

data class ProPlan(
    val productId: String,
    val type: PlanType,
    val title: String,
    val fallbackPrice: String,
    val period: String,
    val note: String,
    val badge: String? = null,
)

/** Whether the user has Pro, and the single place that decides what Pro unlocks. */
object ProStore {
    private const val KEY = "pro"

    /** Google Play product IDs — these must match the products created in Play Console. */
    val plans = listOf(
        ProPlan("glowup_pro_yearly", PlanType.SUBSCRIPTION, "Yearly", "₹1,999", "/ year", "Just ₹167 a month", badge = "Best value"),
        ProPlan("glowup_pro_monthly", PlanType.SUBSCRIPTION, "Monthly", "₹299", "/ month", "Cancel anytime"),
        ProPlan("glowup_pro_lifetime", PlanType.ONE_TIME, "Lifetime", "₹4,999", "once", "Pay once, keep Pro forever"),
    )

    /** Confirmed by Google Play; cached so Pro works offline between launches. */
    var purchased by mutableStateOf(false)
        private set

    /** Debug builds only: lets testers try Pro without a Play purchase. */
    var debugOverride by mutableStateOf(false)
        private set

    val isPro: Boolean get() = purchased || (BuildConfig.DEBUG && debugOverride)

    /** Non-null while the plans popup is open; the text says what the user was trying to open. */
    var paywallReason by mutableStateOf<String?>(null)
        private set

    fun showPaywall(reason: String = "Unlock everything in Glow-Up") {
        paywallReason = reason
    }

    fun dismissPaywall() {
        paywallReason = null
    }

    fun updatePurchased(value: Boolean) {
        if (purchased == value) return
        purchased = value
        save()
    }

    fun updateDebugOverride(value: Boolean) {
        debugOverride = value
        save()
    }

    fun load() {
        val json = Storage.read(KEY)?.let(::JSONObject)
        purchased = json?.optBoolean("purchased") ?: false
        debugOverride = json?.optBoolean("debug") ?: false
        paywallReason = null
    }

    private fun save() {
        Storage.write(KEY, JSONObject().put("purchased", purchased).put("debug", debugOverride).toString())
    }
}

/** What's free and what needs Pro. Free users get a genuinely useful slice of every area. */
object ProGate {
    const val FREE_PROGRAM_SESSIONS = 14
    val freeGuides = setOf("smart-casual", "minimal", "athleisure")
    val proTalkLessons = setOf(3, 8, 9)
    val proGroomingLessons = setOf(7, 12)
    val proSleepSounds = setOf("Crackling Fire", "Deep Space", "Soft Wind", "Brown Noise")
    val proMorningSessions = setOf(2, 3)
    /** Free users see their strongest mood pattern; Pro shows every one. */
    const val FREE_MOOD_INSIGHTS = 1

    private val locked: Boolean get() = !ProStore.isPro

    fun sessionLocked(index: Int) = locked && index >= FREE_PROGRAM_SESSIONS
    fun guideLocked(key: String) = locked && key !in freeGuides
    fun outfitLocked(outfit: Outfit) = guideLocked(outfit.styleKey)
    fun talkLessonLocked(id: Int) = locked && id in proTalkLessons
    fun groomingLessonLocked(id: Int) = locked && id in proGroomingLessons
    fun soundLocked(name: String) = locked && name in proSleepSounds
    fun morningSessionLocked(index: Int) = locked && index in proMorningSessions
    val stylistLocked: Boolean get() = locked

    fun programmeLocked(id: String) = locked && com.glowup.app.ui.data.fitness.Programmes.get(id)?.free == false
    fun quickLocked(minutes: Int) = locked && minutes > com.glowup.app.ui.data.fitness.QuickSessions.FREE_MAX_MINUTES
    /** Auto-progression and per-exercise charts. */
    val progressionLocked: Boolean get() = locked
}
