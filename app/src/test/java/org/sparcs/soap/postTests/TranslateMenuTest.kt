package org.sparcs.soap.postTests

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.sparcs.soap.app.features.post.PostTranslator

class TranslateMenuTest {

    private val names = mapOf(
        "en" to "English",
        "ja" to "Japanese",
        "zh" to "Chinese",
        "fr" to "French",
        "de" to "German",
        "es" to "Spanish",
        "ar" to "العربية",
        "he" to "עברית",
        "ko" to "Korean",
    )
    private val name: (String) -> String = { names[it] ?: it }
    private val targets = listOf("ja", "en", "zh", "fr", "de", "es", "ar", "he")

    private fun sections(
        suggested: List<String> = emptyList(),
        current: String? = null,
        query: String = "",
        targets: List<String> = this.targets,
        aliases: (String) -> List<String> = { emptyList() },
    ) = PostTranslator.pickerSections(targets, suggested, current, query, name, aliases)

    @Test
    fun `suggested keeps order and drops languages that are not targets`() {
        val result = sections(suggested = listOf("ko", "en", "ja"))
        assertEquals(listOf("en", "ja"), result.suggested)
    }

    @Test
    fun `suggested is capped at the limit`() {
        val result = sections(suggested = listOf("en", "ja", "zh", "fr", "de", "es", "ar"))
        assertEquals(PostTranslator.SUGGESTION_LIMIT, result.suggested.size)
        assertEquals(listOf("en", "ja", "zh", "fr", "de"), result.suggested)
    }

    @Test
    fun `current target leads suggestions without duplicates`() {
        val result = sections(suggested = listOf("en", "ja"), current = "fr")
        assertEquals(listOf("fr", "en", "ja"), result.suggested)
        assertTrue(result.others.none { it in result.suggested })
    }

    @Test
    fun `others exclude suggested and are sorted by display name`() {
        val result = sections(suggested = listOf("en"))
        assertEquals(listOf("zh", "fr", "de", "ja", "es", "he", "ar"), result.others)
    }

    @Test
    fun `source language is never offered because targets already exclude it`() {
        val result = sections(suggested = listOf("ko", "en"), targets = PostTranslator.targetLanguages(listOf("ko", "en", "ja"), "ko"))
        assertEquals(listOf("en"), result.suggested)
        assertEquals(listOf("ja"), result.others)
    }

    @Test
    fun `search filters by display name case insensitively and hides suggested group`() {
        val result = sections(suggested = listOf("en", "ja"), query = "  jAp ")
        assertEquals(emptyList<String>(), result.suggested)
        assertEquals(listOf("ja"), result.others)
    }

    @Test
    fun `search matches language code prefix`() {
        assertEquals(listOf("de"), sections(query = "de").others)
    }

    @Test
    fun `search matches aliases such as english or native names`() {
        val result = sections(query = "arabic", aliases = { if (it == "ar") listOf("Arabic") else emptyList() })
        assertEquals(listOf("ar"), result.others)
    }

    @Test
    fun `search matches right to left names`() {
        assertEquals(listOf("he"), sections(query = "עבר").others)
    }

    @Test
    fun `search with no matches is empty`() {
        assertTrue(sections(suggested = listOf("en"), query = "klingon").isEmpty)
    }

    @Test
    fun `no available languages yields empty sections`() {
        assertTrue(sections(suggested = listOf("en"), targets = emptyList()).isEmpty)
    }

    @Test
    fun `quick target is first suggested target when not translated`() {
        assertEquals("en", PostTranslator.quickTarget(listOf("ko", "en", "ja"), targets, current = null))
    }

    @Test
    fun `quick target is absent once translated or without suggestions`() {
        assertNull(PostTranslator.quickTarget(listOf("en"), targets, current = "ja"))
        assertNull(PostTranslator.quickTarget(listOf("ko"), targets, current = null))
        assertNull(PostTranslator.quickTarget(listOf("en"), emptyList(), current = null))
    }
}
