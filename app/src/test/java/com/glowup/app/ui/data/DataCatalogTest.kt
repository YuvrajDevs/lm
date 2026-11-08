package com.glowup.app.ui.data

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder
import java.io.File
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime

class DataCatalogTest {
    @get:Rule
    val temp = TemporaryFolder()

    private lateinit var backend: InMemoryBackend

    @Before
    fun setUp() {
        backend = freshStores()
    }

    private fun useEverything() {
        val day = LocalDate.of(2026, 6, 1)
        ProfileStore.completeOnboarding("Sam", listOf("Looks"), "Oval", "18–24")
        RoutineStore.toggleToday(RoutineStore.items.first().id, day)
        MoodStore.logToday("Happy", "note", listOf("Friends"), day)
        MoodStore.addCustomTag("Gaming")
        SleepLogStore.log(SleepEntry(day, LocalTime.of(23, 0), LocalTime.of(7, 0), 4))
        FocusStore.start(25, "Study", 0)
        FocusStore.finishIfDue(25 * 60_000L)
        TodoStore.add("Buy shoes", TodoBucket.TODAY, null)
        NotesStore.add("Body", day)
        NotesStore.toggleLiked("A quote")
        ConsentStore.grant(Consent.MICROPHONE, day)
        ReminderStore.add("Drink water", Schedule.Daily(LocalTime.of(9, 0)))
        SleepStore.updateBedtime(LocalTime.of(22, 30))
        WardrobeStore.add(com.glowup.app.ui.data.style.Garment.TEE, com.glowup.app.ui.data.style.Palette.White, "White tee")
        com.glowup.app.ui.data.style.SavedLooksStore.toggle(com.glowup.app.ui.data.style.StyleLibrary.outfits.first().id)
        LessonStore.markDone(LessonStore.lessons.first().id)
        GroomingStore.markDone(GroomingStore.lessons.first().id)
        ConfidenceStore.toggle("Card", 5, day)
        com.glowup.app.ui.data.program.ProgramStore.complete(0, day)
    }

    @Test
    fun everyStorageKeyBelongsToACategory() {
        useEverything()
        val uncovered = backend.values.keys - DataCatalog.coveredKeys
        assertTrue("Not listed on the Your data screen: $uncovered", uncovered.isEmpty())
    }

    @Test
    fun clearingACategoryOnlyRemovesItsData() {
        useEverything()
        val mood = DataCatalog.categories.first { it.id == "mood" }
        mood.clear!!()
        assertEquals(0, MoodStore.checkInCount)
        assertTrue(MoodStore.customTags.isEmpty())
        assertEquals(1, SleepLogStore.entries.size)
        assertTrue(ProgressStore.points > 0) // points already earned are kept

        DataCatalog.categories.first { it.id == "routines" }.clear!!()
        Stores.loadAll()
        assertTrue("Default routines must not come back after deleting", RoutineStore.items.isEmpty())
        assertEquals(0, MoodStore.checkInCount)
    }

    @Test
    fun everyDeletableCategoryEmptiesItsSummary() {
        useEverything()
        DataCatalog.categories.forEach { it.clear?.invoke() }
        Stores.loadAll()
        assertEquals(0, SleepLogStore.entries.size)
        assertEquals(0, FocusStore.totalSessions)
        assertTrue(TodoStore.items.isEmpty())
        assertTrue(NotesStore.notes.isEmpty())
        assertFalse(SleepStore.bedtimeAlarmOn)
        assertTrue(ReminderStore.items.none { it.enabled })
    }

    @Test
    fun photoVaultStaysInsideItsFolder() {
        val root = temp.newFolder("nobackup")
        PhotoVault.init(root)
        val a = PhotoVault.save(PhotoVault.Album.PROGRESS, byteArrayOf(1, 2, 3), LocalDateTime.of(2026, 1, 1, 9, 0))!!
        val b = PhotoVault.save(PhotoVault.Album.PROGRESS, byteArrayOf(4), LocalDateTime.of(2026, 1, 1, 9, 0))!!
        assertTrue(a != b)
        assertEquals(2, PhotoVault.count(PhotoVault.Album.PROGRESS))
        assertEquals(4L, PhotoVault.bytes(PhotoVault.Album.PROGRESS))

        val outside = File(temp.root, "elsewhere.jpg").apply { writeBytes(byteArrayOf(9)) }
        assertFalse(PhotoVault.delete(outside))
        assertTrue(outside.exists())

        assertTrue(PhotoVault.delete(a))
        assertEquals(1, PhotoVault.totalCount())
        PhotoVault.deleteAll()
        assertEquals(0, PhotoVault.totalCount())
    }

    @Test
    fun deleteEverythingRemovesPhotosToo() {
        PhotoVault.init(temp.newFolder("nb2"))
        PhotoVault.save(PhotoVault.Album.WARDROBE, byteArrayOf(1))
        useEverything()
        Stores.deleteAll()
        assertEquals(0, PhotoVault.totalCount())
        assertEquals(0, MoodStore.checkInCount)
    }

    @Test
    fun ageRangeDecidesMinorStatus() {
        assertTrue(ProfileStore.isMinor) // unknown counts as a minor
        ProfileStore.updateAgeBand("16–17")
        assertTrue(ProfileStore.isMinor)
        ProfileStore.updateAgeBand("25–34")
        assertFalse(ProfileStore.isMinor)
        Stores.loadAll()
        assertEquals("25–34", ProfileStore.ageBand)
        ProfileStore.updateAgeBand("nonsense")
        assertEquals(null, ProfileStore.ageBand)
    }
}
