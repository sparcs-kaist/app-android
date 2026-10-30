package org.sparcs.soap.postTests

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertThrows
import org.junit.Test
import org.sparcs.soap.app.domain.helpers.tiptap.TiptapMark
import org.sparcs.soap.app.domain.helpers.tiptap.TiptapNode
import org.sparcs.soap.app.domain.helpers.tiptap.TiptapParseException
import org.sparcs.soap.app.domain.helpers.tiptap.TiptapParser
import org.sparcs.soap.app.domain.helpers.tiptap.TiptapValue
import org.sparcs.soap.app.domain.helpers.tiptap.plainText

class TiptapParserTest {

    private val parser = TiptapParser()

    @Test
    fun readsHugeNumbersWithoutOverflow() {
        assertNull(TiptapValue.Num(1e300).intValue)
        assertEquals(1e300.toString(), TiptapValue.Num(1e300).stringValue)
        assertEquals("3", TiptapValue.Num(3.0).stringValue)
        assertEquals(2, TiptapValue.Num(2.5).intValue)
        assertEquals(3, TiptapValue.Str(" 3 ").intValue)
        assertNull(TiptapValue.Num(Double.NaN).intValue)
    }

    @Test
    fun parsesStarterKitDocument() {
        val document = parser.parse(
            """
            {"type":"doc","content":[
              {"type":"heading","attrs":{"level":2},"content":[{"type":"text","text":"Title"}]},
              {"type":"paragraph","content":[
                {"type":"text","text":"Hello "},
                {"type":"text","marks":[{"type":"bold"}],"text":"world"},
                {"type":"hardBreak"},
                {"type":"text","text":"again"}
              ]},
              {"type":"bulletList","content":[
                {"type":"listItem","content":[{"type":"paragraph","content":[{"type":"text","text":"one"}]}]}
              ]}
            ]}
            """
        )
        assertEquals(listOf(TiptapNode.HEADING, TiptapNode.PARAGRAPH, TiptapNode.BULLET_LIST), document.content.map { it.type })
        assertEquals(2, document.content[0]["level"]?.intValue)
        assertEquals(listOf(TiptapMark.BOLD), document.content[1].content[1].marks.map { it.type })
        assertEquals("Title\nHello world\nagain\none", document.plainText)
    }

    @Test
    fun rejectsNonJson() {
        listOf("<p>Hello</p>", "plain text", "").forEach { content ->
            assertThrows(TiptapParseException.NotJson::class.java) { parser.parse(content) }
        }
    }

    @Test
    fun rejectsBrokenJson() {
        assertThrows(TiptapParseException.Malformed::class.java) { parser.parse("{\"type\":\"doc\",") }
    }

    @Test
    fun acceptsWrappedDocument() {
        val json = """{"content":{"type":"doc","content":[{"type":"paragraph","content":[{"type":"text","text":"x"}]}]}}"""
        assertEquals("x", parser.parse(json).plainText)
    }

    @Test
    fun repairsLinkifiedUrls() {
        val json = """
            {"type":"doc","content":[{"type":"paragraph","content":[
              {"type":"text","marks":[{"type":"link","attrs":{"href":"<a href="https://sparcs.org/?a=1&amp;b=2" rel="nofollow">https://sparcs.org/?a=1&amp;b=2</a>"}}],"text":"see <a href="https://sparcs.org" rel="nofollow">https://sparcs.org</a> now"},
              {"type":"attachmentImage","attrs":{"src":"<a href="https://newara.cdn.sparcs.org/a.png" rel="nofollow">https://newara.cdn.sparcs.org/a.png</a>","width":250}}
            ]}]}
        """
        val paragraph = parser.parse(json).content.first()
        assertEquals("see https://sparcs.org now", paragraph.content[0].text)
        assertEquals("https://sparcs.org/?a=1&b=2", paragraph.content[0].marks[0]["href"]?.stringValue)
        assertEquals("https://newara.cdn.sparcs.org/a.png", paragraph.content[1]["src"]?.stringValue)
    }

    @Test
    fun strictParserDoesNotRepair() {
        val json = """{"type":"doc","content":[{"type":"text","text":"<a href="https://x.org">https://x.org</a>"}]}"""
        assertThrows(TiptapParseException.Malformed::class.java) {
            TiptapParser(repairLinkifiedUrls = false, decodeHtmlEntities = false).parse(json)
        }
    }

    @Test
    fun decodesHtmlEntitiesInText() {
        val json = """{"type":"doc","content":[{"type":"paragraph","content":[{"type":"text","text":"A &amp; B &lt;3 &#54620;&#xAE00; &bogus; &"}]}]}"""
        assertEquals("A & B <3 한글 &bogus; &", parser.parse(json).plainText)
    }

    @Test
    fun ignoresInvalidNumericReferences() {
        val json = """{"type":"doc","content":[{"type":"paragraph","content":[{"type":"text","text":"&#xD800; &#99999999;"}]}]}"""
        assertEquals("&#xD800; &#99999999;", parser.parse(json).plainText)
    }

    @Test
    fun toleratesUnknownNodesAndOddAttributes() {
        val json = """{"type":"doc","content":[{"type":"callout","attrs":{"level":"3","x":[1,{"y":null}]},"content":[{"type":"paragraph","content":[{"type":"text","text":"inside"}]}]}]}"""
        val document = parser.parse(json)
        assertEquals("callout", document.content[0].type)
        assertEquals(3, document.content[0]["level"]?.intValue)
        assertEquals("inside", document.plainText)
    }

    @Test
    fun leavesMediaOutOfPlainText() {
        val json = """{"type":"doc","content":[{"type":"paragraph","content":[{"type":"text","text":"a"},{"type":"attachmentImage","attrs":{"src":"https://x.org/a.png"}},{"type":"linkBookmark","attrs":{"href":"https://x.org"}}]}]}"""
        assertEquals("a", parser.parse(json).plainText)
    }
}
