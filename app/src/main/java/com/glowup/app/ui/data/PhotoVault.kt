package com.glowup.app.ui.data

import java.io.File
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

/**
 * The only place photos are ever written. Files live in the app's no-backup directory, so they are
 * private to the app, never appear in the gallery, and are excluded from Android cloud backup.
 * Scan photos never come here — they are measured in memory and discarded.
 */
object PhotoVault {
    enum class Album(val folder: String, val label: String) {
        PROGRESS("progress", "Progress photos"),
        WARDROBE("wardrobe", "Wardrobe items")
    }

    private var root: File? = null

    fun init(noBackupDir: File) {
        root = File(noBackupDir, "photos")
    }

    private fun dir(album: Album): File? = root?.let { File(it, album.folder) }

    /** Writes JPEG bytes and returns the saved file, or null if storage isn't available. */
    fun save(album: Album, jpeg: ByteArray, now: LocalDateTime = LocalDateTime.now(), tag: String? = null): File? {
        val folder = dir(album)?.apply { mkdirs() } ?: return null
        val base = (tag?.let { "$it-" } ?: "") + now.format(DateTimeFormatter.ofPattern("yyyyMMdd-HHmmss"))
        var file = File(folder, "$base.jpg")
        var n = 1
        while (file.exists()) file = File(folder, "$base-${n++}.jpg")
        file.writeBytes(jpeg)
        return file
    }

    fun list(album: Album): List<File> = dir(album)?.listFiles { f -> f.isFile && f.extension == "jpg" }?.sortedBy { it.lastModified() }.orEmpty()

    /** Photos saved with [tag], oldest first. */
    fun list(album: Album, tag: String): List<File> = list(album).filter { it.name.startsWith("$tag-") }

    /** The date a photo was taken, from its file name. */
    fun dateOf(file: File): LocalDate? = Regex("(\\d{8})-\\d{6}").find(file.name)?.groupValues?.get(1)?.let {
        runCatching { LocalDate.parse(it, DateTimeFormatter.BASIC_ISO_DATE) }.getOrNull()
    }

    fun count(album: Album) = list(album).size

    fun totalCount() = Album.entries.sumOf { count(it) }

    fun bytes(album: Album) = list(album).sumOf { it.length() }

    fun delete(file: File): Boolean {
        val folder = root ?: return false
        // Only ever delete files inside the vault.
        if (!file.canonicalPath.startsWith(folder.canonicalPath + File.separator)) return false
        return file.delete()
    }

    fun deleteAlbum(album: Album) {
        dir(album)?.deleteRecursively()
    }

    fun deleteAll() {
        root?.deleteRecursively()
    }
}
