package com.glowup.app.ui.data

import androidx.compose.runtime.mutableStateListOf
import androidx.compose.ui.graphics.Color
import com.glowup.app.R
import com.glowup.app.ui.theme.CardNeutral
import com.glowup.app.ui.theme.CoralTint
import com.glowup.app.ui.theme.GreenTint
import com.glowup.app.ui.theme.NavyTint

enum class TodoBucket(val label: String) { TODAY("Today"), WEEK("This week") }

data class TodoEntry(
    val id: Int,
    val title: String,
    val meta: String?,
    val done: Boolean,
    val bucket: TodoBucket,
    val tint: Color = CardNeutral,
    val icon: Int? = null,
)

/** Single source of truth for to-dos, shared by the Home "Today's plan" list and the To-do screen. */
object TodoStore {
    private var nextId = 100

    val items = mutableStateListOf(
        TodoEntry(1, "Skincare — morning", "7:30 AM · Looks", true, TodoBucket.TODAY, CardNeutral, R.drawable.ic_check),
        TodoEntry(2, "Lay out tomorrow's outfit", "8:00 PM · Style", false, TodoBucket.TODAY, CoralTint, R.drawable.ic_outfit),
        TodoEntry(3, "2-minute eye contact practice", "Anytime · Confidence", false, TodoBucket.TODAY, GreenTint, R.drawable.ic_social),
        TodoEntry(4, "Make your bed", "Morning · Life admin", false, TodoBucket.TODAY, NavyTint, R.drawable.ic_lifeadmin),
        TodoEntry(5, "Pay electricity bill", "Due today · 6:00 PM", false, TodoBucket.TODAY),
        TodoEntry(6, "Book haircut appointment", "Today", true, TodoBucket.TODAY),
        TodoEntry(7, "Plan tomorrow's top 3", "No due date", false, TodoBucket.TODAY),
        TodoEntry(8, "Order new running shoes", "Fri", false, TodoBucket.WEEK),
        TodoEntry(9, "Call mom", "Sun", false, TodoBucket.WEEK),
    )

    fun toggle(id: Int) {
        val index = items.indexOfFirst { it.id == id }
        if (index >= 0) items[index] = items[index].copy(done = !items[index].done)
    }

    fun add(title: String, bucket: TodoBucket, meta: String?) {
        items.add(0, TodoEntry(nextId++, title, meta?.ifBlank { null } ?: bucket.label, false, bucket))
    }

    fun doneCount(bucket: TodoBucket) = items.count { it.bucket == bucket && it.done }
    fun totalCount(bucket: TodoBucket) = items.count { it.bucket == bucket }
}
