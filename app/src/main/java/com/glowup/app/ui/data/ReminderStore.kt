package com.glowup.app.ui.data

import androidx.compose.runtime.mutableStateListOf
import org.json.JSONArray
import org.json.JSONObject
import java.time.DayOfWeek
import java.time.LocalTime

data class Reminder(val id: Int, val title: String, val schedule: Schedule, val enabled: Boolean, val builtIn: Boolean = false)

object ReminderStore {
    private const val KEY = "reminders"

    val items = mutableStateListOf<Reminder>()

    val suggestions: List<Pair<String, Schedule>> = listOf(
        "Posture check" to Schedule.EveryHours(2),
        "Stand up & stretch" to Schedule.EveryHours(1),
        "Skincare — PM routine" to Schedule.Daily(LocalTime.of(21, 30)),
        "Floss before bed" to Schedule.Daily(LocalTime.of(22, 0)),
        "Drink water" to Schedule.EveryHours(2),
        "Lay out tomorrow's outfit" to Schedule.Daily(LocalTime.of(20, 0)),
        "Wind down — screens off" to Schedule.Daily(LocalTime.of(22, 30)),
        "Weekly wardrobe refresh" to Schedule.Weekly(DayOfWeek.SUNDAY, LocalTime.of(10, 0)),
    )

    private fun seed() = listOf(
        Reminder(1, "Sunscreen reapply", Schedule.EveryHours(2), enabled = false, builtIn = true),
        Reminder(2, "Eating nudge", Schedule.Daily(LocalTime.of(13, 0)), enabled = false, builtIn = true),
    )

    val custom: List<Reminder> get() = items.filter { !it.builtIn }
    val builtIn: List<Reminder> get() = items.filter { it.builtIn }

    fun add(title: String, schedule: Schedule, enabled: Boolean = true) {
        val nextId = (items.maxOfOrNull { it.id } ?: 0) + 1
        items.add(0, Reminder(nextId, title.trim(), schedule, enabled))
        changed()
    }

    fun setEnabled(id: Int, enabled: Boolean) = update(id) { it.copy(enabled = enabled) }

    fun setSchedule(id: Int, schedule: Schedule) = update(id) { it.copy(schedule = schedule) }

    fun remove(id: Int) {
        items.removeAll { it.id == id && !it.builtIn }
        changed()
    }

    private fun update(id: Int, change: (Reminder) -> Reminder) {
        val index = items.indexOfFirst { it.id == id }
        if (index < 0) return
        items[index] = change(items[index])
        changed()
    }

    private fun changed() {
        save()
        Storage.onScheduleChanged()
    }

    fun clearAll() {
        items.clear()
        save()
        Storage.onScheduleChanged()
    }

    fun load() {
        items.clear()
        val raw = Storage.read(KEY)
        if (raw == null) {
            items.addAll(seed())
            save()
            return
        }
        JSONArray(raw).objects().forEach { json ->
            val schedule = Schedule.decode(json.getString("schedule")) ?: return@forEach
            items.add(Reminder(json.getInt("id"), json.getString("title"), schedule, json.getBoolean("enabled"), json.optBoolean("builtIn")))
        }
    }

    private fun save() {
        val array = JSONArray()
        items.forEach {
            array.put(
                JSONObject()
                    .put("id", it.id)
                    .put("title", it.title)
                    .put("schedule", it.schedule.encode())
                    .put("enabled", it.enabled)
                    .put("builtIn", it.builtIn),
            )
        }
        Storage.write(KEY, array.toString())
    }
}
