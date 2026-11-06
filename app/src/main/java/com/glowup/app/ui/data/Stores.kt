package com.glowup.app.ui.data

object Stores {
    fun loadAll() {
        ProfileStore.load()
        ProgressStore.load()
        RoutineStore.load()
        TodoStore.load()
        MoodStore.load()
        LessonStore.load()
        GroomingStore.load()
        WardrobeStore.load()
        NotesStore.load()
        ReminderStore.load()
        SleepStore.load()
        ConfidenceStore.load()
        StylingStore.load()
        com.glowup.app.ui.data.program.ProgramStore.load()
        FocusStore.load()
    }

    /** Wipes every saved value and reloads stores back to a first-launch state. */
    fun deleteAll() {
        Storage.clearAll()
        loadAll()
        Storage.onScheduleChanged()
    }
}
