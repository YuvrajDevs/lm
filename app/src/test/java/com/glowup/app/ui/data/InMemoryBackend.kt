package com.glowup.app.ui.data

class InMemoryBackend : KeyValueBackend {
    val values = mutableMapOf<String, String>()
    override fun read(key: String) = values[key]
    override fun write(key: String, value: String) {
        values[key] = value
    }
    override fun clear() = values.clear()
    override fun all(): Map<String, String> = values.toMap()
}

/** Gives each test a clean, persistent-in-memory storage and freshly loaded stores. */
fun freshStores(): InMemoryBackend {
    val backend = InMemoryBackend()
    Storage.backend = backend
    Storage.onScheduleChanged = {}
    Stores.loadAll()
    return backend
}
