package com.glowup.app.ui.data

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import org.json.JSONObject
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.temporal.TemporalAdjusters

fun weekKey(today: LocalDate = LocalDate.now()): String =
    today.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY)).toString()

/** Confidence cards completed this week; resets every Monday. */
object ConfidenceStore {
    private const val KEY = "confidence"

    private var week = weekKey()
    val doneTitles = mutableStateListOf<String>()

    fun isDone(title: String, today: LocalDate = LocalDate.now()): Boolean =
        weekKey(today) == week && title in doneTitles

    fun toggle(title: String, points: Int, today: LocalDate = LocalDate.now()) {
        rollOver(today)
        if (title in doneTitles) {
            doneTitles.remove(title)
            ProgressStore.revoke(points, today)
        } else {
            doneTitles.add(title)
            ProgressStore.award(points, today)
        }
        save()
    }

    private fun rollOver(today: LocalDate) {
        val current = weekKey(today)
        if (current != week) {
            week = current
            doneTitles.clear()
            save()
        }
    }

    fun load() {
        doneTitles.clear()
        val json = Storage.read(KEY)?.let(::JSONObject)
        week = json?.optString("week")?.ifBlank { null } ?: weekKey()
        json?.optJSONArray("done")?.strings()?.let(doneTitles::addAll)
        rollOver(LocalDate.now())
    }

    private fun save() {
        Storage.write(KEY, JSONObject().put("week", week).put("done", doneTitles.toJsonArray()).toString())
    }
}

/** Free outfit ideas used this week; resets every Monday. */
object StylingStore {
    private const val KEY = "styling"
    const val FREE_IDEAS_PER_WEEK = 3

    private var week = weekKey()
    var ideasUsed by mutableStateOf(0)
        private set

    fun ideasLeft(today: LocalDate = LocalDate.now()): Int =
        if (weekKey(today) != week) FREE_IDEAS_PER_WEEK else (FREE_IDEAS_PER_WEEK - ideasUsed).coerceAtLeast(0)

    /** Returns false when the weekly allowance is used up. */
    fun useIdea(today: LocalDate = LocalDate.now()): Boolean {
        rollOver(today)
        if (ideasLeft(today) == 0) return false
        ideasUsed += 1
        save()
        return true
    }

    private fun rollOver(today: LocalDate) {
        val current = weekKey(today)
        if (current != week) {
            week = current
            ideasUsed = 0
            save()
        }
    }

    fun load() {
        val json = Storage.read(KEY)?.let(::JSONObject)
        week = json?.optString("week")?.ifBlank { null } ?: weekKey()
        ideasUsed = json?.optInt("used") ?: 0
        rollOver(LocalDate.now())
    }

    private fun save() {
        Storage.write(KEY, JSONObject().put("week", week).put("used", ideasUsed).toString())
    }
}
