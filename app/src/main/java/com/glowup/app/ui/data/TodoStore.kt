package com.glowup.app.ui.data

import androidx.compose.runtime.mutableStateListOf
import androidx.compose.ui.graphics.Color
import com.glowup.app.R
import com.glowup.app.ui.theme.CardNeutral
import com.glowup.app.ui.theme.CoralTint
import com.glowup.app.ui.theme.GreenTint
import com.glowup.app.ui.theme.NavyTint
import com.glowup.app.ui.theme.PurpleTint
import org.json.JSONArray
import org.json.JSONObject

enum class TodoBucket(val label: String) { TODAY("Today"), WEEK("This week") }

data class TodoEntry(
    val id: Int,
    val title: String,
    val meta: String?,
    val done: Boolean,
    val bucket: TodoBucket,
    val category: String? = null,
) {
    val tint: Color get() = when (category) {
        "Looks", "Style" -> CoralTint
        "Confidence" -> GreenTint
        "Life admin" -> NavyTint
        "Mind" -> PurpleTint
        else -> CardNeutral
    }

    val icon: Int? get() = when (category) {
        "Looks" -> R.drawable.ic_looks
        "Style" -> R.drawable.ic_outfit
        "Confidence" -> R.drawable.ic_social
        "Life admin" -> R.drawable.ic_lifeadmin
        "Mind" -> R.drawable.ic_reflective
        else -> null
    }
}

/** Single source of truth for to-dos, shared by the Home "Today's plan" list and the To-do screen. */
object TodoStore {
    private const val KEY = "todos"
    const val POINTS_PER_TODO = 2

    val items = mutableStateListOf<TodoEntry>()

    private fun seed() = listOf(
        TodoEntry(1, "Do the skin-type blot test", "5 min · Looks", false, TodoBucket.TODAY, "Looks"),
        TodoEntry(2, "Lay out tomorrow's outfit", "8:00 PM · Style", false, TodoBucket.TODAY, "Style"),
        TodoEntry(3, "Message a friend you miss", "Anytime · Confidence", false, TodoBucket.TODAY, "Confidence"),
        TodoEntry(4, "Plan tomorrow's top 3", "Evening · Life admin", false, TodoBucket.TODAY, "Life admin"),
        TodoEntry(5, "Book a haircut", "This week · Looks", false, TodoBucket.WEEK, "Looks"),
        TodoEntry(6, "Clean your everyday shoes", "This week · Style", false, TodoBucket.WEEK, "Style"),
        TodoEntry(7, "Try a look from a style guide", "This week", false, TodoBucket.WEEK, "Style"),
    )

    fun toggle(id: Int) {
        val index = items.indexOfFirst { it.id == id }
        if (index < 0) return
        val nowDone = !items[index].done
        items[index] = items[index].copy(done = nowDone)
        if (nowDone) ProgressStore.award(POINTS_PER_TODO) else ProgressStore.revoke(POINTS_PER_TODO)
        save()
    }

    fun add(title: String, bucket: TodoBucket, meta: String?) {
        val nextId = (items.maxOfOrNull { it.id } ?: 0) + 1
        items.add(0, TodoEntry(nextId, title, meta?.ifBlank { null } ?: bucket.label, false, bucket))
        save()
    }

    fun remove(id: Int) {
        items.removeAll { it.id == id }
        save()
    }

    fun clearDone() {
        items.removeAll { it.done }
        save()
    }

    fun doneCount(bucket: TodoBucket) = items.count { it.bucket == bucket && it.done }
    fun totalCount(bucket: TodoBucket) = items.count { it.bucket == bucket }

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
                TodoEntry(
                    id = json.getInt("id"),
                    title = json.getString("title"),
                    meta = json.optStringOrNull("meta"),
                    done = json.getBoolean("done"),
                    bucket = TodoBucket.valueOf(json.getString("bucket")),
                    category = json.optStringOrNull("category"),
                ),
            )
        }
    }

    private fun save() {
        val array = JSONArray()
        items.forEach { item ->
            array.put(
                JSONObject()
                    .put("id", item.id)
                    .put("title", item.title)
                    .put("meta", item.meta ?: JSONObject.NULL)
                    .put("done", item.done)
                    .put("bucket", item.bucket.name)
                    .put("category", item.category ?: JSONObject.NULL),
            )
        }
        Storage.write(KEY, array.toString())
    }
}
