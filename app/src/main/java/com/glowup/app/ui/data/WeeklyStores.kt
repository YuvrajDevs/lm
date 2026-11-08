package com.glowup.app.ui.data

import androidx.compose.runtime.mutableStateListOf
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

    fun clearAll() {
        Storage.remove(KEY)
        load()
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
