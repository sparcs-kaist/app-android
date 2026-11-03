package org.sparcs.soap.postTests

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import org.sparcs.soap.app.domain.helpers.tiptap.TiptapDocument
import org.sparcs.soap.app.domain.helpers.tiptap.TiptapMark
import org.sparcs.soap.app.domain.helpers.tiptap.TiptapNode
import org.sparcs.soap.app.domain.helpers.tiptap.TiptapParser
import org.sparcs.soap.app.domain.helpers.tiptap.TiptapValue
import org.sparcs.soap.app.domain.helpers.tiptap.toJson

class TiptapJsonWriterTest {

    private val document = TiptapDocument(
        listOf(
            TiptapNode(
                TiptapNode.HEADING,
                attrs = mapOf("level" to TiptapValue.Num(2.0)),
                content = listOf(TiptapNode(TiptapNode.TEXT, text = "Title"))
            ),
            TiptapNode(
                TiptapNode.PARAGRAPH,
                content = listOf(
                    TiptapNode(
                        TiptapNode.TEXT,
                        marks = listOf(TiptapMark(TiptapMark.LINK, mapOf("href" to TiptapValue.Str("https://sparcs.org/a?b=1&c=2")))),
                        text = "see sparcs.org"
                    ),
                    TiptapNode(TiptapNode.HARD_BREAK),
                    TiptapNode(TiptapNode.TEXT, marks = listOf(TiptapMark(TiptapMark.BOLD)), text = "A & B <3 \"quoted\" \\ 한글\n\t"),
                )
            ),
            TiptapNode(
                TiptapNode.ATTACHMENT_IMAGE,
                attrs = mapOf(
                    "src" to TiptapValue.Str("https://newara.cdn.sparcs.org/x.png"),
                    "width" to TiptapValue.Num(500.0),
                    "data-attachment" to TiptapValue.Num(7.0),
                )
            ),
        )
    )

    private val strictParser = TiptapParser(repairLinkifiedUrls = false, decodeHtmlEntities = false)

    @Test
    fun roundTripsThroughTheParser() {
        assertEquals(document, strictParser.parse(document.toJson()))
    }

    @Test
    fun leavesNothingForASanitizerToChange() {
        val json = document.toJson()
        listOf("&", "<", ">", ".", "/").forEach { assertFalse("contains $it", json.contains(it)) }
        assertTrue(json.startsWith("{\"type\":\"doc\""))
    }

    @Test
    fun plainOutputIsReadable() {
        assertTrue(document.toJson(sanitizerSafe = false).contains("https://sparcs.org/a?b=1&c=2"))
    }

    @Test
    fun writesWholeNumbersWithoutAFraction() {
        val json = TiptapDocument(listOf(TiptapNode(TiptapNode.IMAGE, attrs = mapOf("width" to TiptapValue.Num(500.0), "ratio" to TiptapValue.Num(1.5))))).toJson(sanitizerSafe = false)
        assertTrue(json.contains("\"width\":500"))
        assertTrue(json.contains("\"ratio\":1.5"))
    }
}
