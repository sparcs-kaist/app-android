package org.sparcs.soap.postTests

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.sparcs.soap.app.domain.helpers.tiptap.TiptapDocument
import org.sparcs.soap.app.domain.helpers.tiptap.TiptapMark
import org.sparcs.soap.app.domain.helpers.tiptap.TiptapNode
import org.sparcs.soap.app.domain.helpers.tiptap.TiptapParser
import org.sparcs.soap.app.domain.helpers.tiptap.plainText
import org.sparcs.soap.app.domain.helpers.tiptap.toJson
import org.sparcs.soap.app.features.postCompose.ComposeBlock
import org.sparcs.soap.app.features.postCompose.ComposeBlockStyle
import org.sparcs.soap.app.features.postCompose.ComposeColor
import org.sparcs.soap.app.features.postCompose.ComposeDocument
import org.sparcs.soap.app.features.postCompose.ComposeList
import org.sparcs.soap.app.features.postCompose.ComposeListMarkers
import org.sparcs.soap.app.features.postCompose.ComposeSpan
import org.sparcs.soap.app.features.postCompose.ComposeStyle

@RunWith(RobolectricTestRunner::class)
class ComposeRoundTripTest {

    private val base = "https://newara.sparcs.org/post/"

    private fun blocks(json: String) = ComposeDocument.fromContent(json, base)!!

    private fun export(blocks: List<ComposeBlock>): TiptapDocument =
        TiptapParser().parse(ComposeDocument.toTiptap(blocks, emptyMap()).toJson())

    private fun roundTrip(json: String): TiptapDocument = export(blocks(json))

    private fun doc(vararg nodes: String) = """{"type":"doc","content":[${nodes.joinToString(",")}]}"""

    private fun text(value: String, marks: String = "") =
        if (marks.isEmpty()) """{"type":"text","text":"$value"}""" else """{"type":"text","marks":[$marks],"text":"$value"}"""

    private fun paragraph(vararg inline: String) = """{"type":"paragraph","content":[${inline.joinToString(",")}]}"""

    private fun item(vararg content: String) = """{"type":"listItem","content":[${content.joinToString(",")}]}"""

    @Test
    fun roundTripsWhatTheEditorSupports() {
        val json = doc(
            """{"type":"heading","attrs":{"level":1},"content":[${text("Camping")}]}""",
            """{"type":"heading","attrs":{"level":2},"content":[${text("Gear")}]}""",
            """{"type":"heading","attrs":{"level":3},"content":[${text("Tents")}]}""",
            paragraph(
                text("Plain "),
                text("bold", """{"type":"bold"}"""),
                text(" code", """{"type":"code"}"""),
                """{"type":"hardBreak"}""",
                text("red", """{"type":"textStyle","attrs":{"color":"#e03e2d"}}"""),
                text("link", """{"type":"underline"},{"type":"link","attrs":{"href":"https://sparcs.org"}}"""),
            ),
            """{"type":"paragraph"}""",
            """{"type":"bulletList","content":[${item(paragraph(text("one")), """{"type":"orderedList","attrs":{"start":3},"content":[${item(paragraph(text("nested")))}]}""")},${item(paragraph(text("two")))}]}""",
            """{"type":"codeBlock","content":[${text("let a = 1\\nlet b = 2")}]}""",
            paragraph("""{"type":"attachmentImage","attrs":{"data-attachment":"7","src":"https://x.org/a.png","width":250}}"""),
        )
        assertEquals(TiptapParser().parse(json), roundTrip(json))
    }

    @Test
    fun keepsHeadingLevels() {
        val json = doc(
            """{"type":"heading","attrs":{"level":3},"content":[${text("c")}]}""",
            """{"type":"heading","attrs":{"level":1},"content":[${text("a")}]}""",
            """{"type":"heading","attrs":{"level":2},"content":[${text("b")}]}""",
        )
        val styles = blocks(json).map { (it as ComposeBlock.Text).style }
        assertEquals(listOf(ComposeBlockStyle.SUBHEADING, ComposeBlockStyle.TITLE, ComposeBlockStyle.HEADING), styles)
        assertEquals(listOf(3, 1, 2), roundTrip(json).content.map { it["level"]?.intValue })
    }

    @Test
    fun treatsDeepOrMissingHeadingLevelsAsTheNearestStyle() {
        val json = doc(
            """{"type":"heading","attrs":{"level":6},"content":[${text("deep")}]}""",
            """{"type":"heading","content":[${text("none")}]}""",
        )
        assertEquals(listOf(3, 1), roundTrip(json).content.map { it["level"]?.intValue })
    }

    @Test
    fun mergesConsecutiveMonospacedLinesIntoOneCodeBlock() {
        val document = export(
            listOf(
                ComposeBlock.Text("intro"),
                ComposeBlock.Text("let a = 1", style = ComposeBlockStyle.MONOSPACED),
                ComposeBlock.Text("", style = ComposeBlockStyle.MONOSPACED),
                ComposeBlock.Text("let b = 2", style = ComposeBlockStyle.MONOSPACED),
                ComposeBlock.Text("after"),
            )
        )
        assertEquals(listOf(TiptapNode.PARAGRAPH, TiptapNode.CODE_BLOCK, TiptapNode.PARAGRAPH), document.content.map { it.type })
        assertEquals("let a = 1\n\nlet b = 2", document.content[1].content.single().text)
    }

    @Test
    fun importsACodeBlockAsMonospacedLines() {
        val json = doc("""{"type":"codeBlock","content":[${text("a\\nb")},{"type":"hardBreak"},${text("c")}]}""")
        val lines = blocks(json).map { it as ComposeBlock.Text }
        assertEquals(listOf("a", "b", "c"), lines.map { it.text })
        assertTrue(lines.all { it.style == ComposeBlockStyle.MONOSPACED })
    }

    @Test
    fun anEmptyCodeBlockSurvives() {
        val json = doc(paragraph(text("x")), """{"type":"codeBlock"}""", paragraph(text("y")))
        assertEquals(listOf(TiptapNode.PARAGRAPH, TiptapNode.CODE_BLOCK, TiptapNode.PARAGRAPH), roundTrip(json).content.map { it.type })
    }

    @Test
    fun roundTripsTheCodeMark() {
        val json = doc(paragraph(text("run "), text("make", """{"type":"code"}""")))
        val block = blocks(json).single() as ComposeBlock.Text
        assertEquals(listOf(ComposeSpan(4, 8, ComposeStyle.CODE)), block.spans)
        assertEquals(listOf(TiptapMark.CODE), roundTrip(json).content.single().content[1].marks.map { it.type })
    }

    @Test
    fun writesTextColourAsTextStyle() {
        val block = ComposeBlock.Text("red text", colors = listOf(ComposeColor(0, 3, "#e03e2d")))
        val inline = export(listOf(block)).content.single().content
        assertEquals(TiptapMark.TEXT_STYLE, inline[0].marks.single().type)
        assertEquals("#e03e2d", inline[0].marks.single()["color"]?.stringValue)
        assertTrue(inline[1].marks.isEmpty())
    }

    @Test
    fun normalisesImportedColoursAndDropsDefaultOrInvalidOnes() {
        val json = doc(
            paragraph(
                text("short", """{"type":"textStyle","attrs":{"color":"#F00"}}"""),
                text("rgb", """{"type":"textStyle","attrs":{"color":"rgb(53, 152, 219)"}}"""),
                text("black", """{"type":"textStyle","attrs":{"color":"#000000"}}"""),
                text("white", """{"type":"textStyle","attrs":{"color":"#fefefe"}}"""),
                text("junk", """{"type":"textStyle","attrs":{"color":"banana"}}"""),
                text("none", """{"type":"textStyle"}"""),
            )
        )
        val block = blocks(json).single() as ComposeBlock.Text
        assertEquals(listOf(ComposeColor(0, 5, "#ff0000"), ComposeColor(5, 8, "#3598db")), block.colors)
        val colours = roundTrip(json).content.single().content.mapNotNull { node ->
            node.marks.firstOrNull { it.type == TiptapMark.TEXT_STYLE }?.get("color")?.stringValue
        }
        assertEquals(listOf("#ff0000", "#3598db"), colours)
    }

    @Test
    fun rebuildsNestedLists() {
        val document = export(
            listOf(
                ComposeBlock.Text("one", lists = listOf(ComposeList.BULLET)),
                ComposeBlock.Text("nested", lists = listOf(ComposeList.BULLET, ComposeList(isOrdered = true, start = 3))),
                ComposeBlock.Text("two", lists = listOf(ComposeList.BULLET)),
                ComposeBlock.Text("after"),
            )
        )
        assertEquals(listOf(TiptapNode.BULLET_LIST, TiptapNode.PARAGRAPH), document.content.map { it.type })
        val items = document.content[0].content
        assertEquals(2, items.size)
        assertEquals(listOf(TiptapNode.PARAGRAPH, TiptapNode.ORDERED_LIST), items[0].content.map { it.type })
        assertEquals(3, items[0].content[1]["start"]?.intValue)
        assertEquals("nested", items[0].content[1].plainText())
        assertEquals("two", items[1].plainText())
    }

    @Test
    fun aSubListOfAnotherTypeDoesNotSplitTheOuterList() {
        val document = export(
            listOf(
                ComposeBlock.Text("a", lists = listOf(ComposeList.NUMBERED)),
                ComposeBlock.Text("a.1", lists = listOf(ComposeList.NUMBERED, ComposeList.BULLET)),
                ComposeBlock.Text("b", lists = listOf(ComposeList.NUMBERED)),
            )
        )
        assertEquals(listOf(TiptapNode.ORDERED_LIST), document.content.map { it.type })
        assertEquals(2, document.content.single().content.size)
    }

    @Test
    fun roundTripsDeepNesting() {
        val lists = List(5) { if (it % 2 == 0) ComposeList.BULLET else ComposeList.NUMBERED }
        val json = ComposeDocument.toTiptap(listOf(ComposeBlock.Text("deep", lists = lists)), emptyMap()).toJson()
        assertEquals(lists, (blocks(json).single() as ComposeBlock.Text).lists)
        assertEquals(TiptapParser().parse(json), roundTrip(json))
    }

    @Test
    fun aListItemWithoutAParagraphStillNests() {
        val json = doc("""{"type":"bulletList","content":[${item("""{"type":"bulletList","content":[${item(paragraph(text("inner")))}]}""")}]}""")
        val block = blocks(json).single() as ComposeBlock.Text
        assertEquals(listOf(ComposeList.BULLET, ComposeList.BULLET), block.lists)
        assertEquals(TiptapParser().parse(json), roundTrip(json))
    }

    @Test
    fun keepsTheOrderedListStart() {
        val json = doc("""{"type":"orderedList","attrs":{"start":5},"content":[${item(paragraph(text("five")))},${item(paragraph(text("six")))}]}""")
        assertEquals(5, roundTrip(json).content.single()["start"]?.intValue)
        val imported = blocks(json)
        val markers = ComposeListMarkers.of(imported)
        assertEquals(listOf("5.", "6."), imported.map { markers[it.id] })
    }

    @Test
    fun omitsTheDefaultOrderedListStart() {
        val json = doc("""{"type":"orderedList","attrs":{"start":1},"content":[${item(paragraph(text("one")))}]}""")
        assertNull(roundTrip(json).content.single()["start"])
    }

    @Test
    fun usesALinkBookmarksTitleAsItsText() {
        val json = doc(
            paragraph("""{"type":"linkBookmark","attrs":{"href":"https://sparcs.org","title":"SPARCS"}}"""),
            paragraph("""{"type":"linkBookmark","attrs":{"href":"https://kaist.ac.kr","title":""}}"""),
        )
        val document = roundTrip(json)
        assertEquals("SPARCS\nhttps://kaist.ac.kr", document.plainText)
        assertEquals("https://sparcs.org", document.content[0].content.single().marks.single()["href"]?.stringValue)
    }

    @Test
    fun keepsTheContentOfBlocksTheEditorCannotShow() {
        val json = doc(
            """{"type":"horizontalRule"}""",
            """{"type":"mystery","content":[${paragraph(text("kept"))}]}""",
        )
        assertEquals("kept", roundTrip(json).plainText)
    }

    @Test
    fun anEmptyDocumentStaysEmpty() {
        assertTrue(roundTrip(doc()).content.isEmpty())
        assertTrue(export(listOf(ComposeBlock.Text(""), ComposeBlock.Text("  "))).content.isEmpty())
    }

    @Test
    fun numbersNestedListsPerLevel() {
        val blocks = listOf(
            ComposeBlock.Text("a", lists = listOf(ComposeList.NUMBERED)),
            ComposeBlock.Text("a.1", lists = listOf(ComposeList.NUMBERED, ComposeList.NUMBERED)),
            ComposeBlock.Text("a.2", lists = listOf(ComposeList.NUMBERED, ComposeList.NUMBERED)),
            ComposeBlock.Text("b", lists = listOf(ComposeList.NUMBERED)),
            ComposeBlock.Text("dot", lists = listOf(ComposeList.NUMBERED, ComposeList.BULLET)),
            ComposeBlock.Text("break"),
            ComposeBlock.Text("again", lists = listOf(ComposeList.NUMBERED)),
        )
        val markers = ComposeListMarkers.of(blocks)
        assertEquals(listOf("1.", "1.", "2.", "2.", "◦", null, "1."), blocks.map { markers[it.id] })
    }

    private fun TiptapNode.plainText(): String = TiptapDocument(listOf(this)).plainText
}
