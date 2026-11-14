package com.glowup.app.ui

import java.io.File
import org.junit.Assert.assertTrue
import org.junit.Test

/** Guards the UI copy in strings_ui.xml against mistakes Android won't flag at build time. */
class StringResourcesTest {
    private val strings: List<Pair<String, String>> =
        Regex("""<string name="([^"]+)"[^>]*>(.*)</string>""")
            .findAll(File("src/main/res/values/strings_ui.xml").readText())
            .map { it.groupValues[1] to it.groupValues[2] }
            .toList()

    @Test
    fun edgeSpacesAreQuotedSoAndroidKeepsThem() {
        // Android trims unquoted leading/trailing spaces, which glues sentences together ("survives.Pro").
        val broken = strings.filter { (_, v) -> !v.startsWith("\"") && (v.startsWith(" ") || v.endsWith(" ")) }
        assertTrue("Wrap these values in quotes: ${broken.map { it.first }}", broken.isEmpty())
    }

    @Test
    fun placeholdersAreNumberedInOrder() {
        val bad = strings.filter { (_, v) ->
            val numbers = Regex("%(\\d+)\\\$s").findAll(v).map { it.groupValues[1].toInt() }.toSortedSet()
            numbers.isNotEmpty() && numbers.toList() != (1..numbers.size).toList()
        }
        assertTrue("Placeholders should run %1\$s, %2\$s…: ${bad.map { it.first }}", bad.isEmpty())
    }

    @Test
    fun noDuplicateCopyUnderDifferentKeys() {
        val dupes = strings.groupBy { it.second }.filterValues { it.size > 1 }.keys
        assertTrue("Reuse one key for identical copy: $dupes", dupes.isEmpty())
    }
}
