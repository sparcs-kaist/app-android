package org.sparcs.soap.postTests

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.sparcs.soap.app.domain.helpers.tiptap.TiptapAttributedBlock
import org.sparcs.soap.app.domain.helpers.tiptap.TiptapAttributedTextBlocks
import org.sparcs.soap.app.domain.helpers.tiptap.TiptapMark
import org.sparcs.soap.app.domain.helpers.tiptap.TiptapNode
import org.sparcs.soap.app.domain.helpers.tiptap.TiptapParser
import org.sparcs.soap.app.domain.helpers.tiptap.TiptapRun
import org.sparcs.soap.app.domain.helpers.tiptap.attributedTextBlocks
import org.sparcs.soap.app.domain.helpers.tiptap.replacingTextBlocks
import org.sparcs.soap.app.domain.helpers.tiptap.textBlocks

class TiptapTextBlocksTest {

    private val document = TiptapParser().parse(
        """
        {"type":"doc","content":[
          {"type":"heading","attrs":{"level":2},"content":[{"type":"text","marks":[{"type":"bold"}],"text":"안녕"}]},
          {"type":"paragraph","content":[{"type":"text","text":"첫 줄"},{"type":"hardBreak"},{"type":"text","marks":[{"type":"italic"}],"text":"둘째 줄"}]},
          {"type":"codeBlock","content":[{"type":"text","text":"let a = 1"}]},
          {"type":"bulletList","content":[{"type":"listItem","content":[{"type":"paragraph","content":[{"type":"text","text":"항목"}]}]}]},
          {"type":"paragraph","content":[{"type":"attachmentImage","attrs":{"src":"https://x.org/a.png"}}]}
        ]}
        """.trimIndent()
    )

    @Test
    fun listsTextBlocksInReadingOrderSkippingCode() {
        assertEquals(listOf("안녕", "첫 줄\n둘째 줄", "항목", ""), document.textBlocks)
    }

    @Test
    fun attributedBlocksKeepMarksPerRun() {
        val document = TiptapParser().parse(
            """
            {"type":"doc","content":[{"type":"paragraph","content":[
              {"type":"text","text":"설문 참여를 원하시면 "},
              {"type":"text","marks":[{"type":"link","attrs":{"href":"https://forms.gle/x"}},{"type":"underline"}],"text":"링크"},
              {"type":"text","text":"를 눌러 주세요"}
            ]}]}
            """.trimIndent()
        )
        val source = document.attributedTextBlocks
        val translated = TiptapAttributedBlock(
            source.blocks[0].runs.zip(listOf("To take part, tap ", "the link", " please")) { run, english ->
                TiptapRun(english, run.markSet)
            }
        )

        val inline = document.replacingTextBlocks(listOf(translated), source).content[0].content

        assertEquals(listOf("To take part, tap ", "the link", " please"), inline.map { it.text })
        assertEquals(listOf(TiptapMark.LINK, TiptapMark.UNDERLINE), inline[1].marks.map { it.type })
        assertEquals("https://forms.gle/x", inline[1].marks[0]["href"]?.stringValue)
    }

    @Test
    fun restoresMarksOntoPlainTranslations() {
        val document = TiptapParser().parse(
            """
            {"type":"doc","content":[{"type":"paragraph","content":[
              {"type":"text","marks":[{"type":"bold"}],"text":"설문"},
              {"type":"text","text":" 참여는 "},
              {"type":"text","marks":[{"type":"link","attrs":{"href":"https://forms.gle/x"}}],"text":"링크"},
              {"type":"text","text":" 또는 "},
              {"type":"text","marks":[{"type":"link","attrs":{"href":"https://sparcs.org"}}],"text":"https://sparcs.org"},
              {"type":"text","text":" "},
              {"type":"text","marks":[{"type":"italic"}],"text":"사라짐"}
            ]}]}
            """.trimIndent()
        )
        val source = document.attributedTextBlocks
        val runs = source.markedRuns(0)
        assertEquals(listOf("설문", "링크", "https://sparcs.org", "사라짐"), runs.map { it.text })

        val restored = TiptapAttributedTextBlocks.restoringMarks(
            runs.zip(listOf("Survey", "Link", "https://sparcs.org", "Vanished")),
            onto = "For the survey, use the link or https://sparcs.org.",
        )
        val marked = document.replacingTextBlocks(listOf(restored), source).content[0].content.filter { it.marks.isNotEmpty() }

        assertEquals(listOf("survey", "link", "https://sparcs.org"), marked.map { it.text })
        assertEquals(listOf(TiptapMark.BOLD, TiptapMark.LINK, TiptapMark.LINK), marked.map { it.marks.first().type })
        assertEquals("https://forms.gle/x", marked[1].marks.first()["href"]?.stringValue)
    }

    @Test
    fun replacesTextKeepingStructureAndUniformMarks() {
        val replaced = document.replacingTextBlocks(listOf("Hello", "First line\nSecond line", "Item", ""))

        assertEquals(listOf("Hello", "First line\nSecond line", "Item", ""), replaced.textBlocks)
        assertEquals(
            listOf(TiptapNode.HEADING, TiptapNode.PARAGRAPH, TiptapNode.CODE_BLOCK, TiptapNode.BULLET_LIST, TiptapNode.PARAGRAPH),
            replaced.content.map { it.type },
        )
        assertEquals(listOf(TiptapMark.BOLD), replaced.content[0].content.first().marks.map { it.type })
        assertEquals(listOf(TiptapNode.TEXT, TiptapNode.HARD_BREAK, TiptapNode.TEXT), replaced.content[1].content.map { it.type })
        assertTrue(replaced.content[1].content.all { it.marks.isEmpty() })
        assertEquals(document.content[2], replaced.content[2])
        assertEquals(document.content[4], replaced.content[4])
    }

    @Test
    fun missingReplacementsLeaveBlocksUntouched() {
        val replaced = document.replacingTextBlocks(listOf("Hello"))

        assertEquals(listOf("Hello", "첫 줄\n둘째 줄", "항목", ""), replaced.textBlocks)
    }
}
