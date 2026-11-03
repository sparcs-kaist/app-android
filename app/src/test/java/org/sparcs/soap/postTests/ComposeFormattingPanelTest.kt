package org.sparcs.soap.postTests

import androidx.compose.ui.text.TextRange
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.sparcs.soap.app.features.postCompose.ComposeBlock
import org.sparcs.soap.app.features.postCompose.ComposeEditorState
import org.sparcs.soap.app.features.postCompose.ComposeList
import org.sparcs.soap.app.features.postCompose.ComposeStyle

@RunWith(RobolectricTestRunner::class)
class ComposeFormattingPanelTest {

    private fun editing() = ComposeEditorState(listOf(ComposeBlock.Text("hello"))).apply { isEditing = true }

    @Test
    fun startsClosed() {
        assertFalse(editing().isFormatting)
    }

    @Test
    fun formatButtonTogglesPanel() {
        val state = editing()
        state.toggleFormatting()
        assertTrue(state.isFormatting)
        state.toggleFormatting()
        assertFalse(state.isFormatting)
    }

    @Test
    fun cannotOpenWithoutBodyFocus() {
        val state = ComposeEditorState(listOf(ComposeBlock.Text("hello")))
        state.toggleFormatting()
        assertFalse(state.isFormatting)
    }

    @Test
    fun losingBodyFocusClosesPanel() {
        val state = editing()
        state.toggleFormatting()
        state.isEditing = false
        assertFalse(state.isFormatting)
        state.isEditing = true
        assertFalse(state.isFormatting)
    }

    @Test
    fun closeFormattingClosesPanel() {
        val state = editing()
        state.toggleFormatting()
        state.closeFormatting()
        assertFalse(state.isFormatting)
    }

    @Test
    fun formattingActionsKeepPanelOpenAndFocus() {
        val state = editing()
        val id = state.blocks.first().id
        state.toggleFormatting()
        state.select(id, TextRange(0, 5))
        state.toggle(ComposeStyle.BOLD)
        state.setColor("#e15241")
        state.toggleList(ComposeList.BULLET)
        state.indent()
        state.outdent()
        assertTrue(state.isFormatting)
        assertTrue(state.isEditing)
        assertEquals(id, state.focusedId)
    }
}
