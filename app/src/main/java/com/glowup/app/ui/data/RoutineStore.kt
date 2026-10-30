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
import java.time.LocalDate

data class RoutineEntry(
    val id: Int,
    val area: String,
    val title: String,
    val streakDays: Int,
    val week: List<Boolean>,
    val tint: Color,
    val accent: Color,
    val icon: Int,
)

/** Single source of truth for routines, shared by the Routines list and its detail screen. */
object RoutineStore {
    val items = mutableStateListOf(
        RoutineEntry(1, "LOOKS · DAILY", "Skincare — morning & night", 12, listOf(true, true, true, true, true, true, true), CoralTint, Coral, R.drawable.ic_looks),
        RoutineEntry(2, "LIFE ADMIN · DAILY", "Make your bed", 7, listOf(true, true, true, true, true, true, true), NavyTint, Navy, R.drawable.ic_lifeadmin),
        RoutineEntry(3, "SOCIAL · 4X PER WEEK", "2-minute eye contact practice", 3, listOf(true, false, true, false, true, false, false), GreenTint, Green, R.drawable.ic_social),
        RoutineEntry(4, "MIND · DAILY", "Journaling — 5 minutes", 1, listOf(false, false, false, false, false, false, true), PurpleTint, Purple, R.drawable.ic_reflective),
        RoutineEntry(5, "STYLE · WEEKLY", "Monthly wardrobe refresh", 0, listOf(false, false, false, false, false, false, false), BlueTint, Navy, R.drawable.ic_style),
    )

    fun find(id: Int) = items.firstOrNull { it.id == id }

    private fun styleFor(category: String): Triple<Color, Color, Int> = when (category) {
        "Looks" -> Triple(CoralTint, Coral, R.drawable.ic_looks)
        "Mind" -> Triple(PurpleTint, Purple, R.drawable.ic_reflective)
        "Social" -> Triple(GreenTint, Green, R.drawable.ic_social)
        "Style" -> Triple(BlueTint, Navy, R.drawable.ic_style)
        "Sleep" -> Triple(BlueTint, Navy, R.drawable.ic_sleep)
        else -> Triple(NavyTint, Navy, R.drawable.ic_lifeadmin)
    }

    fun add(category: String, title: String, frequency: String = "DAILY") {
        if (items.any { it.title == title }) return
        val nextId = (items.maxOfOrNull { it.id } ?: 0) + 1
        val (tint, accent, icon) = styleFor(category)
        items.add(RoutineEntry(nextId, "${category.uppercase()} · $frequency", title, 0, List(7) { false }, tint, accent, icon))
    }

    fun toggleToday(id: Int) {
        val index = items.indexOfFirst { it.id == id }
        if (index < 0) return
        val entry = items[index]
        val todayIndex = (LocalDate.now().dayOfWeek.value - 1).coerceIn(0, 6)
        val wasDone = entry.week.getOrElse(todayIndex) { false }
        val newWeek = entry.week.toMutableList().also { it[todayIndex] = !wasDone }
        val newStreak = if (wasDone) (entry.streakDays - 1).coerceAtLeast(0) else entry.streakDays + 1
        items[index] = entry.copy(week = newWeek, streakDays = newStreak)
    }
}
