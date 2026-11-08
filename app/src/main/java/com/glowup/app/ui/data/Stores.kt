package com.glowup.app.ui.data

object Stores {
    fun loadAll() {
        ProStore.load()
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
        com.glowup.app.ui.data.program.ProgramStore.load()
        FocusStore.load()
        com.glowup.app.ui.data.style.SavedLooksStore.load()
        SleepLogStore.load()
        com.glowup.app.ui.data.fitness.FitnessStore.load()
        com.glowup.app.ui.data.glow.GlowCheckStore.load()
        BlockStore.load()
        SpeechStore.load()
        ConsentStore.load()
        ProgressStore.applyFreezes()
    }

    /** Wipes every saved value and reloads stores back to a first-launch state. */
    fun deleteAll() {
        Storage.clearAll()
        PhotoVault.deleteAll()
        loadAll()
        Storage.onScheduleChanged()
    }
}
