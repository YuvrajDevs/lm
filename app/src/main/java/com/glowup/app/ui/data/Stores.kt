package com.glowup.app.ui.data

import com.glowup.app.ui.data.fitness.FitnessStore
import com.glowup.app.ui.data.glow.GlowCheckStore
import com.glowup.app.ui.data.program.ProgramStore
import com.glowup.app.ui.data.style.SavedLooksStore

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
        ProgramStore.load()
        FocusStore.load()
        SavedLooksStore.load()
        SleepLogStore.load()
        FitnessStore.load()
        GlowCheckStore.load()
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
