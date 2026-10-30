package org.sparcs.soap.postTests

import android.net.Uri
import androidx.compose.ui.text.TextRange
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.sparcs.soap.app.features.postCompose.ComposeBlock
import org.sparcs.soap.app.features.postCompose.ComposeBlockStyle
import org.sparcs.soap.app.features.postCompose.ComposeColor
import org.sparcs.soap.app.features.postCompose.ComposeEditorState
import org.sparcs.soap.app.features.postCompose.ComposeList
import org.sparcs.soap.app.features.postCompose.ComposeSpan
import org.sparcs.soap.app.features.postCompose.ComposeStyle

@RunWith(RobolectricTestRunner::class)
class ComposeEditorStateTest {

    private fun ComposeEditorState.text(index: Int = 0) = blocks[index] as ComposeBlock.Text

    private fun ComposeEditorState.type(text: String) {
        val block = blocks.first { it.id == focusedId } as ComposeBlock.Text
        val at = selection.start
        val updated = block.text.substring(0, at) + text + block.text.substring(at)
        updateText(block.id, updated, TextRange(at + text.length))
    }

    @Test
    fun boldAppliesToWhatIsTypedNext() {
        val state = ComposeEditorState()
        state.type("a")
        state.toggle(ComposeStyle.BOLD)
        state.type("bc")
        state.toggle(ComposeStyle.BOLD)
        state.type("d")
        assertEquals("abcd", state.text().text)
        assertEquals(listOf(ComposeSpan(1, 3, ComposeStyle.BOLD)), state.text().spans)
    }

    @Test
    fun formatsASelectionAndUnformatsPartOfIt() {
        val state = ComposeEditorState(listOf(ComposeBlock.Text("hello world")))
        val id = state.text().id
        state.select(id, TextRange(0, 11))
        state.toggle(ComposeStyle.ITALIC)
        state.select(id, TextRange(5, 6))
        assertTrue(ComposeStyle.ITALIC in state.activeStyles)
        state.toggle(ComposeStyle.ITALIC)
        assertEquals(listOf(ComposeSpan(0, 5, ComposeStyle.ITALIC), ComposeSpan(6, 11, ComposeStyle.ITALIC)), state.text().spans)
    }

    @Test
    fun deletingShrinksAndDropsSpans() {
        val state = ComposeEditorState(listOf(ComposeBlock.Text("abcdef", spans = listOf(ComposeSpan(2, 4, ComposeStyle.BOLD)))))
        val id = state.text().id
        state.updateText(id, "abef", TextRange(2))
        assertTrue(state.text().spans.isEmpty())
        state.updateText(id, "ef", TextRange(0))
        assertEquals("ef", state.text().text)
    }

    @Test
    fun photosAreInsertedAtTheCursor() {
        val state = ComposeEditorState(listOf(ComposeBlock.Text("beforeafter", spans = listOf(ComposeSpan(4, 8, ComposeStyle.BOLD)))))
        state.select(state.text().id, TextRange(6))
        state.insertImages(listOf(Uri.parse("content://1"), Uri.parse("content://2")))
        assertEquals(4, state.blocks.size)
        assertEquals("before", state.text(0).text)
        assertEquals(listOf(ComposeSpan(4, 6, ComposeStyle.BOLD)), state.text(0).spans)
        assertEquals("after", state.text(3).text)
        assertEquals(listOf(ComposeSpan(0, 2, ComposeStyle.BOLD)), state.text(3).spans)
        assertEquals(2, state.pickedImages.size)
        assertEquals(state.text(3).id, state.focusedId)
    }

    @Test
    fun backspaceAtTheStartMergesOrRemovesThePhotoAbove() {
        val state = ComposeEditorState(listOf(ComposeBlock.Text("a"), ComposeBlock.Text("b", spans = listOf(ComposeSpan(0, 1, ComposeStyle.BOLD)))))
        state.mergeWithPrevious(state.text(1).id)
        assertEquals("ab", state.text().text)
        assertEquals(listOf(ComposeSpan(1, 2, ComposeStyle.BOLD)), state.text().spans)

        state.select(state.text().id, TextRange(2))
        state.insertImages(listOf(Uri.parse("content://1")))
        state.mergeWithPrevious(state.focusedId!!)
        assertTrue(state.blocks.none { it is ComposeBlock.Image })
    }

    @Test
    fun removingTheLastBlockLeavesAnEmptyParagraph() {
        val state = ComposeEditorState()
        state.remove(state.text().id)
        assertEquals(1, state.blocks.size)
        assertTrue(state.isBlank)
    }

    @Test
    fun returnInAParagraphStartsANewParagraph() {
        val state = ComposeEditorState()
        state.type("one")
        state.type("\n")
        state.type("two")
        assertEquals(listOf("one", "two"), state.blocks.map { (it as ComposeBlock.Text).text })
        assertEquals(state.text(1).id, state.focusedId)
    }

    @Test
    fun returnInTheMiddleSplitsTheTextAndItsFormatting() {
        val state = ComposeEditorState(listOf(ComposeBlock.Text("abcd", spans = listOf(ComposeSpan(1, 3, ComposeStyle.BOLD)))))
        state.select(state.text().id, TextRange(2))
        state.type("\n")
        assertEquals(listOf("ab", "cd"), state.blocks.map { (it as ComposeBlock.Text).text })
        assertEquals(listOf(ComposeSpan(1, 2, ComposeStyle.BOLD)), state.text(0).spans)
        assertEquals(listOf(ComposeSpan(0, 1, ComposeStyle.BOLD)), state.text(1).spans)
    }

    @Test
    fun shiftReturnInsertsALineBreakInTheSameParagraph() {
        val state = ComposeEditorState()
        state.type("a")
        state.insertLineBreak(state.text().id)
        state.type("b")
        assertEquals(1, state.blocks.size)
        assertEquals("a\nb", state.text().text)
    }

    @Test
    fun aLineSeparatorBecomesALineBreak() {
        val state = ComposeEditorState()
        state.updateText(state.text().id, "a\u2028b", TextRange(3))
        assertEquals(1, state.blocks.size)
        assertEquals("a\nb", state.text().text)
    }

    @Test
    fun returnAfterAHeadingContinuesInBody() {
        val state = ComposeEditorState()
        state.setBlockStyle(ComposeBlockStyle.HEADING)
        state.type("Heading")
        state.type("\n")
        state.type("body")
        assertEquals(ComposeBlockStyle.HEADING, state.text(0).style)
        assertEquals(ComposeBlockStyle.PARAGRAPH, state.text(1).style)
    }

    @Test
    fun returnInsideAHeadingKeepsBothHalvesHeadings() {
        val state = ComposeEditorState(listOf(ComposeBlock.Text("TitleRest", style = ComposeBlockStyle.TITLE)))
        state.select(state.text().id, TextRange(5))
        state.type("\n")
        assertEquals(listOf(ComposeBlockStyle.TITLE, ComposeBlockStyle.TITLE), state.blocks.map { (it as ComposeBlock.Text).style })
    }

    @Test
    fun returnInAQuoteContinuesTheQuoteAndOnAnEmptyLineEndsIt() {
        val state = ComposeEditorState()
        state.setBlockStyle(ComposeBlockStyle.QUOTE)
        state.type("quoted")
        state.type("\n")
        assertEquals(ComposeBlockStyle.QUOTE, state.text(1).style)
        state.type("\n")
        assertEquals(2, state.blocks.size)
        assertEquals(ComposeBlockStyle.PARAGRAPH, state.text(1).style)
    }

    @Test
    fun returnInAListStartsANewItem() {
        val state = ComposeEditorState()
        state.toggleList(ComposeList.BULLET)
        state.type("one")
        state.type("\n")
        assertEquals(2, state.blocks.size)
        assertEquals(listOf(ComposeList.BULLET), state.text(1).lists)
    }

    @Test
    fun returnOnAnEmptyItemEndsTheListOneLevelAtATime() {
        val state = ComposeEditorState()
        state.toggleList(ComposeList.BULLET)
        state.type("one")
        state.type("\n")
        state.indent()
        state.type("nested")
        state.type("\n")
        assertEquals(listOf(ComposeList.BULLET, ComposeList.BULLET), state.text(2).lists)

        state.type("\n")
        assertEquals(listOf(ComposeList.BULLET), state.text(2).lists)
        state.type("\n")
        assertTrue(state.text(2).lists.isEmpty())
        assertEquals(3, state.blocks.size)
        state.type("after")
        assertEquals("after", state.text(2).text)
    }

    @Test
    fun togglingAListOfTheOtherTypeSwitchesTheInnermostLevel() {
        val state = ComposeEditorState(listOf(ComposeBlock.Text("x", lists = listOf(ComposeList.BULLET, ComposeList.BULLET))))
        state.select(state.text().id, TextRange(0))
        state.toggleList(ComposeList.NUMBERED)
        assertEquals(listOf(ComposeList.BULLET, ComposeList.NUMBERED), state.text().lists)
        state.toggleList(ComposeList.NUMBERED)
        assertEquals(listOf(ComposeList.BULLET), state.text().lists)
    }

    @Test
    fun indentStopsAtTheMaximumDepthAndOutdentAtZero() {
        val state = ComposeEditorState(listOf(ComposeBlock.Text("x")))
        state.select(state.text().id, TextRange(0))
        state.indent()
        assertTrue(state.text().lists.isEmpty())
        state.toggleList(ComposeList.BULLET)
        repeat(ComposeList.MAX_DEPTH + 3) { state.indent() }
        assertEquals(ComposeList.MAX_DEPTH, state.text().lists.size)
        repeat(ComposeList.MAX_DEPTH + 3) { state.outdent() }
        assertTrue(state.text().lists.isEmpty())
    }

    @Test
    fun backspaceAtTheStartOfAListItemOutdentsIt() {
        val state = ComposeEditorState(listOf(ComposeBlock.Text("x", lists = listOf(ComposeList.NUMBERED, ComposeList.BULLET))))
        state.mergeWithPrevious(state.text().id)
        assertEquals(listOf(ComposeList.NUMBERED), state.text().lists)
        state.mergeWithPrevious(state.text().id)
        assertTrue(state.text().lists.isEmpty())
    }

    @Test
    fun colourAppliesToTheSelectionAndToWhatIsTypedNext() {
        val state = ComposeEditorState(listOf(ComposeBlock.Text("hello")))
        val id = state.text().id
        state.select(id, TextRange(0, 5))
        state.setColor("#E03E2D")
        assertEquals(listOf(ComposeColor(0, 5, "#e03e2d")), state.text().colors)

        state.select(id, TextRange(5))
        state.setColor(null)
        state.type("!")
        assertEquals(listOf(ComposeColor(0, 5, "#e03e2d")), state.text().colors)
    }

    @Test
    fun nearBlackOrInvalidColoursMeanTheDefault() {
        val state = ComposeEditorState(listOf(ComposeBlock.Text("hello")))
        state.select(state.text().id, TextRange(0, 5))
        state.setColor("#000000")
        assertTrue(state.text().colors.isEmpty())
        state.setColor("nope")
        assertNull(state.activeColor)
    }

    @Test
    fun tracksWhetherAnythingChanged() {
        val state = ComposeEditorState(listOf(ComposeBlock.Text("original")))
        assertFalse(state.isModified)
        state.select(state.text().id, TextRange(8))
        assertFalse(state.isModified)
        state.type("!")
        assertTrue(state.isModified)
    }
}
