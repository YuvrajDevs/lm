package com.glowup.app.ui.data

import java.io.File
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
        WARDROBE("wardrobe", "Wardrobe items"),
    }

    private var root: File? = null

    fun init(noBackupDir: File) {
        root = File(noBackupDir, "photos")
    }

    private fun dir(album: Album): File? = root?.let { File(it, album.folder) }

    /** Writes JPEG bytes and returns the saved file, or null if storage isn't available. */
    fun save(album: Album, jpeg: ByteArray, now: LocalDateTime = LocalDateTime.now()): File? {
        val folder = dir(album)?.apply { mkdirs() } ?: return null
        val base = now.format(DateTimeFormatter.ofPattern("yyyyMMdd-HHmmss"))
        var file = File(folder, "$base.jpg")
        var n = 1
        while (file.exists()) file = File(folder, "$base-${n++}.jpg")
        file.writeBytes(jpeg)
        return file
    }

    fun list(album: Album): List<File> =
        dir(album)?.listFiles { f -> f.isFile && f.extension == "jpg" }?.sortedBy { it.name }.orEmpty()

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
