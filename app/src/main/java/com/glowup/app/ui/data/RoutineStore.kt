package com.glowup.app.ui.data

import androidx.compose.runtime.mutableStateListOf
import androidx.compose.ui.graphics.Color
import com.glowup.app.R
import com.glowup.app.ui.theme.BlueTint
import com.glowup.app.ui.theme.Coral
import com.glowup.app.ui.theme.CoralTint
import com.glowup.app.ui.theme.Green
import com.glowup.app.ui.theme.GreenTint
import com.glowup.app.ui.theme.Navy
import com.glowup.app.ui.theme.NavyTint
import com.glowup.app.ui.theme.Purple
import com.glowup.app.ui.theme.PurpleTint
import org.json.JSONArray
import org.json.JSONObject
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.LocalTime
import java.time.temporal.TemporalAdjusters

data class RoutineEntry(
    val id: Int,
    val category: String,
    val frequency: String,
    val title: String,
    val doneDates: Set<LocalDate> = emptySet(),
    val reminderTime: LocalTime? = null,
) {
    val area: String get() = "${category.uppercase()} · $frequency"
    val tint: Color get() = RoutineStore.styleFor(category).first
    val accent: Color get() = RoutineStore.styleFor(category).second
    val icon: Int get() = RoutineStore.styleFor(category).third

    fun streakDays(today: LocalDate = LocalDate.now()) = ProgressStore.streakFrom(doneDates, today)

    /** Mon..Sun of the week containing [today]. */
    fun week(today: LocalDate = LocalDate.now()): List<Boolean> {
        val monday = today.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY))
        return List(7) { monday.plusDays(it.toLong()) in doneDates }
    }

    fun isDoneOn(date: LocalDate) = date in doneDates
}

object RoutineStore {
    private const val KEY = "routines"
    const val POINTS_PER_CHECK_IN = 5

    val categories = listOf("Looks", "Fitness", "Mind", "Social", "Style", "Sleep", "Life admin")

    val items = mutableStateListOf<RoutineEntry>()

    private fun seed() = listOf(
        RoutineEntry(1, "Looks", "DAILY", "Skincare — morning & night"),
        RoutineEntry(2, "Life admin", "DAILY", "Make your bed"),
        RoutineEntry(3, "Social", "DAILY", "2-minute eye contact practice"),
        RoutineEntry(4, "Mind", "DAILY", "Journaling — 5 minutes"),
        RoutineEntry(5, "Style", "WEEKLY", "Wardrobe refresh"),
    )

    fun find(id: Int) = items.firstOrNull { it.id == id }

    fun styleFor(category: String): Triple<Color, Color, Int> = when (category) {
        "Looks" -> Triple(CoralTint, Coral, R.drawable.ic_looks)
        "Fitness" -> Triple(GreenTint, Green, R.drawable.ic_determined)
        "Mind" -> Triple(PurpleTint, Purple, R.drawable.ic_reflective)
        "Social" -> Triple(GreenTint, Green, R.drawable.ic_social)
        "Style" -> Triple(BlueTint, Navy, R.drawable.ic_style)
        "Sleep" -> Triple(BlueTint, Navy, R.drawable.ic_sleep)
        else -> Triple(NavyTint, Navy, R.drawable.ic_lifeadmin)
    }

    fun add(category: String, title: String, frequency: String = "DAILY", reminderTime: LocalTime? = null): Boolean {
        if (items.any { it.title.equals(title, ignoreCase = true) }) return false
        val nextId = (items.maxOfOrNull { it.id } ?: 0) + 1
        items.add(RoutineEntry(nextId, category, frequency, title, reminderTime = reminderTime))
        save()
        if (reminderTime != null) Storage.onScheduleChanged()
        return true
    }

    fun remove(id: Int) {
        items.removeAll { it.id == id }
        save()
        Storage.onScheduleChanged()
    }

    fun setReminder(id: Int, time: LocalTime?) {
        update(id) { it.copy(reminderTime = time) }
        Storage.onScheduleChanged()
    }

    fun toggleToday(id: Int, today: LocalDate = LocalDate.now()) {
        val entry = find(id) ?: return
        if (entry.isDoneOn(today)) {
            update(id) { it.copy(doneDates = it.doneDates - today) }
            ProgressStore.revoke(POINTS_PER_CHECK_IN, today)
        } else {
            update(id) { it.copy(doneDates = it.doneDates + today) }
            ProgressStore.award(POINTS_PER_CHECK_IN, today)
        }
    }

    private fun update(id: Int, change: (RoutineEntry) -> RoutineEntry) {
        val index = items.indexOfFirst { it.id == id }
        if (index < 0) return
        items[index] = change(items[index])
        save()
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
            items.add(
                RoutineEntry(
                    id = json.getInt("id"),
                    category = json.getString("category"),
                    frequency = json.getString("frequency"),
                    title = json.getString("title"),
                    doneDates = json.getJSONArray("done").strings().mapTo(HashSet()) { LocalDate.parse(it) },
                    reminderTime = json.optStringOrNull("reminder")?.let { LocalTime.parse(it) },
                ),
            )
        }
    }

    private fun save() {
        val array = JSONArray()
        items.forEach { entry ->
            array.put(
                JSONObject()
                    .put("id", entry.id)
                    .put("category", entry.category)
                    .put("frequency", entry.frequency)
                    .put("title", entry.title)
                    .put("done", entry.doneDates.map { it.toString() }.sorted().toJsonArray())
                    .put("reminder", entry.reminderTime?.toString() ?: JSONObject.NULL),
            )
        }
        Storage.write(KEY, array.toString())
    }
}
