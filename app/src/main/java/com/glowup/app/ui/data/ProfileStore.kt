package com.glowup.app.ui.data

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import org.json.JSONArray
import org.json.JSONObject

object ProfileStore {
    private const val KEY = "profile"

    val focusOptions = listOf("Looks", "Style", "Confidence", "Talk", "Sleep", "Fitness", "Mind")

    /** Age ranges. Used only to keep advice age-appropriate (e.g. no weight-loss goals for minors). */
    val ageBands = listOf("Under 16", "16–17", "18–24", "25–34", "35–44", "45+")
    private val minorBands = setOf("Under 16", "16–17")

    var name by mutableStateOf("")
        private set
    var faceShape by mutableStateOf<String?>(null)
        private set
    var onboarded by mutableStateOf(false)
        private set
    var ageBand by mutableStateOf<String?>(null)
        private set

    /** Unknown age is treated as a minor, so age-sensitive advice stays safe until we know. */
    val isMinor: Boolean get() = ageBand == null || ageBand in minorBands
    val focusAreas = mutableStateListOf<String>()

    val firstName: String get() = name.trim().substringBefore(" ").ifBlank { "there" }

    fun completeOnboarding(name: String, focus: List<String>, faceShape: String?, ageBand: String? = null) {
        this.name = name.trim()
        this.ageBand = ageBand
        this.faceShape = faceShape
        focusAreas.clear()
        focusAreas.addAll(focus)
        onboarded = true
        save()
    }

    fun updateName(value: String) {
        name = value.trim()
        save()
    }

    fun updateFaceShape(value: String?) {
        faceShape = value
        save()
    }

    fun updateAgeBand(value: String?) {
        ageBand = value?.takeIf { it in ageBands }
        save()
    }

    fun updateFocus(values: List<String>) {
        focusAreas.clear()
        focusAreas.addAll(values)
        save()
    }

    fun load() {
        val json = Storage.read(KEY)?.let(::JSONObject)
        name = json?.optString("name").orEmpty()
        faceShape = json?.optStringOrNull("faceShape")
        onboarded = json?.optBoolean("onboarded") ?: false
        ageBand = json?.optStringOrNull("age")?.takeIf { it in ageBands }
        focusAreas.clear()
        json?.optJSONArray("focus")?.strings()?.let(focusAreas::addAll)
    }

    private fun save() {
        Storage.write(
            KEY,
            JSONObject()
                .put("name", name)
                .put("faceShape", faceShape ?: JSONObject.NULL)
                .put("onboarded", onboarded)
                .put("age", ageBand ?: JSONObject.NULL)
                .put("focus", JSONArray(focusAreas.toList()))
                .toString(),
        )
    }
}
