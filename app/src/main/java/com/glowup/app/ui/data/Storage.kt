package com.glowup.app.ui.data

import android.content.Context
import org.json.JSONArray
import org.json.JSONObject

interface KeyValueBackend {
    fun read(key: String): String?
    fun write(key: String, value: String)
    fun remove(key: String)
    fun clear()
    fun all(): Map<String, String>
}

private class PrefsBackend(context: Context) : KeyValueBackend {
    private val prefs = context.getSharedPreferences("glowup_store", Context.MODE_PRIVATE)
    override fun read(key: String) = prefs.getString(key, null)
    override fun write(key: String, value: String) = prefs.edit().putString(key, value).apply()
    override fun remove(key: String) = prefs.edit().remove(key).apply()
    override fun clear() {
        prefs.edit().clear().commit()
    }
    override fun all() = prefs.all.mapNotNull { (key, value) -> (value as? String)?.let { key to it } }.toMap()
}

/**
 * Key/value persistence for every store. Without a backend (e.g. plain JVM tests) reads return
 * null and writes are dropped, so stores behave as in-memory only.
 */
object Storage {
    var backend: KeyValueBackend? = null

    /** Invoked after any change that affects scheduled notifications. */
    var onScheduleChanged: () -> Unit = {}

    /** Invoked after any saved value changes — used to refresh home-screen widgets. */
    var onDataChanged: () -> Unit = {}

    fun init(context: Context) {
        backend = PrefsBackend(context.applicationContext)
    }

    fun read(key: String): String? = backend?.read(key)

    fun write(key: String, value: String) {
        backend?.write(key, value)
        onDataChanged()
    }

    fun remove(key: String) {
        backend?.remove(key)
        onDataChanged()
    }

    fun clearAll() {
        backend?.clear()
        onDataChanged()
    }

    fun exportJson(): String {
        val root = JSONObject()
        backend?.all()?.toSortedMap()?.forEach { (key, text) ->
            root.put(key, runCatching { JSONObject(text) }.getOrElse { runCatching { JSONArray(text) }.getOrDefault(text) })
        }
        return root.toString(2)
    }
}

fun JSONArray.strings(): List<String> = List(length()) { getString(it) }

fun Iterable<String>.toJsonArray(): JSONArray = JSONArray().also { array -> forEach { array.put(it) } }

fun JSONArray.objects(): List<JSONObject> = List(length()) { getJSONObject(it) }

fun JSONObject.optStringOrNull(key: String): String? = if (!has(key) || isNull(key)) null else getString(key)
