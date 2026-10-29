package org.sparcs.soap.postTests

import android.net.Uri
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.sparcs.soap.app.domain.helpers.tiptap.TiptapMark
import org.sparcs.soap.app.domain.helpers.tiptap.TiptapNode
import org.sparcs.soap.app.domain.helpers.tiptap.TiptapParser
import org.sparcs.soap.app.domain.helpers.tiptap.plainText
import org.sparcs.soap.app.domain.helpers.tiptap.toJson
import org.sparcs.soap.app.features.postCompose.ComposeBlock
import org.sparcs.soap.app.features.postCompose.ComposeBlockStyle
import org.sparcs.soap.app.features.postCompose.ComposeDocument
import org.sparcs.soap.app.features.postCompose.ComposeLink
import org.sparcs.soap.app.features.postCompose.ComposeList
import org.sparcs.soap.app.features.postCompose.ComposeSpan
import org.sparcs.soap.app.features.postCompose.ComposeStyle

@RunWith(RobolectricTestRunner::class)
class ComposeDocumentTest {

    private val base = "https://newara.sparcs.org/post/"

    @Test
    fun writesMarksAndLineBreaks() {
        val block = ComposeBlock.Text(
            text = "Hello world\nagain",
            spans = listOf(ComposeSpan(6, 11, ComposeStyle.BOLD), ComposeSpan(0, 11, ComposeStyle.ITALIC)),
            links = listOf(ComposeLink(12, 17, "https://sparcs.org")),
        )
        val inline = ComposeDocument.toTiptap(listOf(block), emptyMap()).content.single().content
        assertEquals(listOf(TiptapNode.TEXT, TiptapNode.TEXT, TiptapNode.HARD_BREAK, TiptapNode.TEXT), inline.map { it.type })
        assertEquals(listOf(TiptapMark.ITALIC), inline[0].marks.map { it.type })
        assertEquals(listOf(TiptapMark.BOLD, TiptapMark.ITALIC), inline[1].marks.map { it.type })
        assertEquals("https://sparcs.org", inline[3].marks.single()["href"]?.stringValue)
    }

    @Test
    fun groupsConsecutiveListBlocks() {
        val blocks = listOf(
            ComposeBlock.Text("a", lists = listOf(ComposeList.BULLET)),
            ComposeBlock.Text("b", lists = listOf(ComposeList.BULLET)),
            ComposeBlock.Text("c", lists = listOf(ComposeList.NUMBERED)),
            ComposeBlock.Text("d", style = ComposeBlockStyle.HEADING),
            ComposeBlock.Text("e", style = ComposeBlockStyle.QUOTE),
        )
        val document = ComposeDocument.toTiptap(blocks, emptyMap())
        assertEquals(
            listOf(TiptapNode.BULLET_LIST, TiptapNode.ORDERED_LIST, TiptapNode.HEADING, TiptapNode.BLOCKQUOTE),
            document.content.map { it.type }
        )
        assertEquals(2, document.content[0].content.size)
        assertEquals("a\nb\nc\nd\ne", document.plainText)
    }

    @Test
    fun writesUploadedPhotosWhereTheySitAndTrimsBlankEdges() {
        val photo = ComposeBlock.Image(ComposeBlock.Image.Source.Picked(Uri.parse("content://photo/1")))
        val blocks = listOf(ComposeBlock.Text(""), ComposeBlock.Text("before"), photo, ComposeBlock.Text("after"), ComposeBlock.Text(""))
        val document = ComposeDocument.toTiptap(blocks, mapOf(photo.id to (7 to "https://newara.cdn.sparcs.org/7.jpg")))

        assertEquals(3, document.content.size)
        val image = document.content[1].content.single()
        assertEquals(TiptapNode.ATTACHMENT_IMAGE, image.type)
        assertEquals("https://newara.cdn.sparcs.org/7.jpg", image["src"]?.stringValue)
        assertEquals("7", image["data-attachment"]?.stringValue)
        assertEquals(500, image["width"]?.intValue)
    }

    @Test
    fun dropsPhotosThatFailedToUpload() {
        val photo = ComposeBlock.Image(ComposeBlock.Image.Source.Picked(Uri.parse("content://photo/1")))
        assertEquals("x", ComposeDocument.toTiptap(listOf(ComposeBlock.Text("x"), photo), emptyMap()).plainText)
    }

    @Test
    fun roundTripsAnExistingPost() {
        val json = """
            {"type":"doc","content":[
              {"type":"heading","attrs":{"level":1},"content":[{"type":"text","text":"제목"}]},
              {"type":"paragraph","content":[{"type":"text","marks":[{"type":"bold"}],"text":"굵게"},{"type":"text","text":" 보통"}]},
              {"type":"paragraph","content":[{"type":"attachmentImage","attrs":{"src":"/files/a.png","data-attachment":"3"}}]},
              {"type":"bulletList","content":[{"type":"listItem","content":[{"type":"paragraph","content":[{"type":"text","text":"항목"}]}]}]}
            ]}
        """
        val blocks = ComposeDocument.fromContent(json, base)!!
        val image = blocks[2] as ComposeBlock.Image
        assertEquals("https://newara.sparcs.org/files/a.png", (image.source as ComposeBlock.Image.Source.Existing).url)
        assertEquals(listOf(ComposeList.BULLET), (blocks[3] as ComposeBlock.Text).lists)

        val rewritten = TiptapParser().parse(ComposeDocument.toTiptap(blocks, emptyMap()).toJson())
        assertEquals("제목\n굵게 보통\n\n항목", rewritten.plainText)
        assertEquals(listOf(TiptapNode.HEADING, TiptapNode.PARAGRAPH, TiptapNode.PARAGRAPH, TiptapNode.BULLET_LIST), rewritten.content.map { it.type })
        assertEquals(listOf(3), ComposeDocument.imageIds(rewritten, emptyMap()))
    }

    @Test
    fun readsTheOldComposersHtml() {
        val html = """<p>첫 줄</p><p>A &amp; B <a href="https://x.org">https://x.org</a></p><p><img src="https://cdn/1.jpg" width="500" data-attachment="9"></p>"""
        val blocks = ComposeDocument.fromContent(html, base)!!
        assertEquals(listOf("첫 줄", "A & B https://x.org"), blocks.filterIsInstance<ComposeBlock.Text>().map { it.text })
        assertTrue(blocks.last() is ComposeBlock.Image)
    }

    @Test
    fun refusesRichHtmlItCannotEdit() {
        assertNull(ComposeDocument.fromContent("<p><b>bold</b></p>", base))
        assertNull(ComposeDocument.fromContent("<div>portal</div>", base))
    }

    @Test
    fun findsImageIdsByAttachmentOrUrl() {
        val document = TiptapParser().parse(
            """{"type":"doc","content":[{"type":"image","attrs":{"src":"https://cdn/old.jpg"}},{"type":"attachmentImage","attrs":{"src":"https://cdn/new.jpg","data-attachment":"5"}}]}"""
        )
        assertEquals(listOf(4, 5), ComposeDocument.imageIds(document, mapOf("https://cdn/old.jpg" to 4)))
    }
}
