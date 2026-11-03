package org.sparcs.soap.postTests

import androidx.compose.ui.text.LinkAnnotation
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.sparcs.soap.app.domain.helpers.tiptap.TiptapParser
import org.sparcs.soap.app.shared.views.tiptap.TiptapBlock
import org.sparcs.soap.app.shared.views.tiptap.TiptapBookmark
import org.sparcs.soap.app.shared.views.tiptap.TiptapColor
import org.sparcs.soap.app.shared.views.tiptap.TiptapLayout

class TiptapLayoutTest {

    private fun layout(json: String, baseUrl: String? = null) =
        TiptapLayout.from(TiptapParser().parse(json), baseUrl)

    @Test
    fun liftsInlineMediaOutOfParagraphs() {
        val layout = layout(
            """
            {"type":"doc","content":[{"type":"paragraph","content":[
              {"type":"text","text":"before"},
              {"type":"attachmentImage","attrs":{"src":"/media/a.png","width":250}},
              {"type":"hardBreak"},
              {"type":"linkBookmark","attrs":{"href":"https://news.kaist.ac.kr/x","title":"https://news.kaist.ac.kr/x"}},
              {"type":"text","text":"after"}
            ]}]}
            """,
            baseUrl = "https://newara.sparcs.org"
        )
        val kinds = layout.blocks.map { it.kind }
        assertEquals(4, kinds.size)
        assertEquals("before", (kinds[0] as TiptapBlock.Kind.Paragraph).text.text)
        val image = (kinds[1] as TiptapBlock.Kind.Image).image
        assertEquals("https://newara.sparcs.org/media/a.png", image.url)
        assertEquals(250f, image.width)
        assertEquals("KAIST", (kinds[2] as TiptapBlock.Kind.Bookmark).bookmark.displayTitle)
        assertEquals("after", (kinds[3] as TiptapBlock.Kind.Paragraph).text.text)
    }

    @Test
    fun collectsImagesInReadingOrderIncludingNested() {
        val layout = layout(
            """
            {"type":"doc","content":[
              {"type":"paragraph","content":[{"type":"attachmentImage","attrs":{"src":"https://x.org/1.png"}}]},
              {"type":"blockquote","content":[{"type":"paragraph","content":[{"type":"attachmentImage","attrs":{"src":"https://x.org/2.png"}}]}]},
              {"type":"bulletList","content":[{"type":"listItem","content":[{"type":"paragraph","content":[{"type":"attachmentImage","attrs":{"src":"https://x.org/3.png"}}]}]}]}
            ]}
            """
        )
        assertEquals(listOf("1.png", "2.png", "3.png"), layout.images.map { it.url.substringAfterLast('/') })
    }

    @Test
    fun trimsOuterBlankParagraphsButKeepsInnerOnes() {
        val layout = layout(
            """{"type":"doc","content":[{"type":"paragraph"},{"type":"paragraph","content":[{"type":"text","text":"a"}]},{"type":"paragraph"},{"type":"paragraph","content":[{"type":"text","text":"b"}]},{"type":"paragraph"}]}"""
        )
        assertEquals(listOf(false, true, false), layout.blocks.map { it.isEmptyParagraph })
    }

    @Test
    fun appliesMarksAndDropsUnsafeLinks() {
        val layout = layout(
            """
            {"type":"doc","content":[{"type":"paragraph","content":[
              {"type":"text","marks":[{"type":"bold"},{"type":"italic"},{"type":"link","attrs":{"href":"https://sparcs.org"}}],"text":"x"},
              {"type":"text","marks":[{"type":"link","attrs":{"href":"javascript:alert(1)"}}],"text":"y"}
            ]}]}
            """
        )
        val text = (layout.blocks.first().kind as TiptapBlock.Kind.Paragraph).text
        val style = text.spanStyles.first { it.start == 0 }.item
        assertEquals(FontWeight.Bold, style.fontWeight)
        assertEquals(FontStyle.Italic, style.fontStyle)
        val links = text.getLinkAnnotations(0, text.length)
        assertEquals(1, links.size)
        assertEquals("https://sparcs.org", (links.single().item as LinkAnnotation.Url).url)
        assertEquals(0, links.single().start)
    }

    @Test
    fun keepsUnderlineAndStrikeTogether() {
        val layout = layout("""{"type":"doc","content":[{"type":"paragraph","content":[{"type":"text","marks":[{"type":"underline"},{"type":"strike"}],"text":"x"}]}]}""")
        val text = (layout.blocks.single().kind as TiptapBlock.Kind.Paragraph).text
        assertEquals(
            TextDecoration.combine(listOf(TextDecoration.Underline, TextDecoration.LineThrough)),
            text.spanStyles.single().item.textDecoration
        )
    }

    @Test
    fun numbersOrderedListsFromStart() {
        val layout = layout(
            """
            {"type":"doc","content":[{"type":"orderedList","attrs":{"start":3},"content":[
              {"type":"listItem","content":[{"type":"paragraph","content":[{"type":"text","text":"a"}]}]},
              {"type":"listItem","content":[{"type":"paragraph","content":[{"type":"text","text":"b"}]},
                {"type":"bulletList","content":[{"type":"listItem","content":[{"type":"paragraph","content":[{"type":"text","text":"c"}]}]}]}]}
            ]}]}
            """
        )
        val list = (layout.blocks.first().kind as TiptapBlock.Kind.ListBlock).list
        val nested = (list.items.last().blocks.last().kind as TiptapBlock.Kind.ListBlock).list
        assertEquals(listOf("3.", "4."), list.items.map { it.marker })
        assertEquals(listOf("◦"), nested.items.map { it.marker })
    }

    @Test
    fun blockIdsAreUnique() {
        val layout = layout(
            """
            {"type":"doc","content":[{"type":"blockquote","content":[{"type":"paragraph","content":[{"type":"text","text":"q"}]}]},
              {"type":"bulletList","content":[{"type":"listItem","content":[{"type":"paragraph","content":[{"type":"text","text":"a"}]}]}]},
              {"type":"codeBlock","content":[{"type":"text","text":"let x = 1"}]},{"type":"horizontalRule"}]}
            """
        )
        val ids = mutableListOf<Int>()
        fun collect(blocks: List<TiptapBlock>) {
            blocks.forEach { block ->
                ids += block.id
                when (val kind = block.kind) {
                    is TiptapBlock.Kind.Blockquote -> collect(kind.blocks)
                    is TiptapBlock.Kind.ListBlock -> kind.list.items.forEach { item ->
                        ids += item.id
                        collect(item.blocks)
                    }
                    else -> Unit
                }
            }
        }
        collect(layout.blocks)
        assertEquals(ids.size, ids.toSet().size)
    }

    @Test
    fun keepsCodeBlockLineBreaks() {
        val layout = layout("""{"type":"doc","content":[{"type":"codeBlock","attrs":{"language":"kotlin"},"content":[{"type":"text","text":"a"},{"type":"hardBreak"},{"type":"text","text":"b"}]}]}""")
        assertEquals(TiptapBlock.Kind.CodeBlock("a\nb", "kotlin"), layout.blocks.single().kind)
    }

    @Test
    fun parsesOnlyReadableColours() {
        assertNotNull(TiptapColor.parse("#e03131"))
        assertNull(TiptapColor.parse("#000000"))
        assertNull(TiptapColor.parse("#fff"))
        assertNotNull(TiptapColor.parse("rgb(34, 139, 230)"))
        assertNull(TiptapColor.parse("red"))
        assertNotNull(TiptapColor.parse("#ffffff", allowsExtremes = true))
    }

    @Test
    fun bookmarkTitlesFallBackSensibly() {
        assertEquals("URL", TiptapBookmark("https://x.org", null).displayTitle)
        assertEquals("SPARCS", TiptapBookmark("https://sparcs.org", "sparcs.org").displayTitle)
        assertEquals("공지", TiptapBookmark("https://x.org", "공지").displayTitle)
        assertTrue(TiptapBookmark("https://x.org", "  ").displayTitle == "URL")
    }
}
