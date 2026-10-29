package org.sparcs.soap.app.features.postCompose

import android.net.Uri
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.text.TextRange

class ComposeEditorState(initial: List<ComposeBlock> = emptyList()) {
    private val initialBlocks = initial.ifEmpty { listOf(ComposeBlock.Text("")) }
    private val initialSignature = signature(initialBlocks)

    var blocks: List<ComposeBlock> by mutableStateOf(initialBlocks)
        private set

    var focusedId: String? by mutableStateOf(initialBlocks.first().id)
        private set
    var focusRequest: Int by mutableIntStateOf(0)
        private set
    var selection: TextRange by mutableStateOf(TextRange.Zero)
        private set
    var isEditing: Boolean by mutableStateOf(false)

    var activeStyles: Set<ComposeStyle> by mutableStateOf(emptySet())
        private set
    var activeColor: String? by mutableStateOf(null)
        private set

    val isBlank: Boolean
        get() = blocks.all { it is ComposeBlock.Text && it.text.isBlank() }

    val isModified: Boolean
        get() = signature(blocks) != initialSignature

    val pickedImages: List<ComposeBlock.Image>
        get() = blocks.filterIsInstance<ComposeBlock.Image>().filter { it.source is ComposeBlock.Image.Source.Picked }

    val focusedText: ComposeBlock.Text?
        get() = textBlock(focusedId)

    fun focus(id: String? = blocks.firstOrNull { it is ComposeBlock.Text }?.id) {
        focusedId = id ?: return
        focusRequest++
    }

    fun select(id: String, range: TextRange) {
        if (focusedId == id && selection == range) return
        focusedId = id
        selection = range
        val block = focusedText ?: return
        if (range.collapsed) {
            activeStyles = ComposeSpans.stylesAt(block.spans, range.start)
            activeColor = ComposeSpans.colorAt(block.colors, range.start)
        } else {
            activeStyles = ComposeStyle.entries.filter { ComposeSpans.covers(block.spans, it, range.min, range.max) }.toSet()
            activeColor = ComposeSpans.colorCovering(block.colors, range.min, range.max)
        }
    }

    fun updateText(id: String, newText: String, newSelection: TextRange) {
        val block = textBlock(id) ?: return
        val returnAt = insertedReturn(block.text, newText)
        if (returnAt != null) handleReturn(block, returnAt)
        else applyEdit(block, newText.replace(LINE_SEPARATOR, LINE_BREAK), newSelection)
    }

    fun insertLineBreak(id: String) {
        val block = textBlock(id) ?: return
        val start = selection.min.coerceIn(0, block.text.length)
        val end = selection.max.coerceIn(start, block.text.length)
        applyEdit(block, block.text.replaceRange(start, end, LINE_BREAK.toString()), TextRange(start + 1))
    }

    fun toggle(style: ComposeStyle) {
        val on = style !in activeStyles
        activeStyles = if (on) activeStyles + style else activeStyles - style
        updateSelected { copy(spans = ComposeSpans.apply(spans, style, selection.min, selection.max, on)) }
    }

    fun setColor(hex: String?) {
        val color = ComposeColors.normalize(hex)
        activeColor = color
        updateSelected { copy(colors = ComposeSpans.paint(colors, selection.min, selection.max, color)) }
    }

    fun setBlockStyle(style: ComposeBlockStyle) = updateFocused {
        copy(style = if (this.style == style) ComposeBlockStyle.PARAGRAPH else style)
    }

    fun toggleList(list: ComposeList) = updateFocused {
        val innermost = lists.lastOrNull()
        copy(
            lists = when {
                innermost == null -> listOf(list)
                innermost.isOrdered == list.isOrdered -> lists.dropLast(1)
                else -> lists.dropLast(1) + list
            }
        )
    }

    fun indent() = updateFocused {
        val innermost = lists.lastOrNull()
        if (innermost == null || lists.size >= ComposeList.MAX_DEPTH) this else copy(lists = lists + ComposeList(innermost.isOrdered))
    }

    fun outdent() = updateFocused { copy(lists = lists.dropLast(1)) }

    fun mergeWithPrevious(id: String): Boolean {
        val index = blocks.indexOfFirst { it.id == id }
        val block = blocks.getOrNull(index) as? ComposeBlock.Text ?: return false
        when {
            block.lists.isNotEmpty() -> update(block) { copy(lists = lists.dropLast(1)) }
            block.style != ComposeBlockStyle.PARAGRAPH -> update(block) { copy(style = ComposeBlockStyle.PARAGRAPH) }
            else -> when (val previous = blocks.getOrNull(index - 1) ?: return false) {
                is ComposeBlock.Image -> blocks = blocks - previous
                is ComposeBlock.Text -> {
                    val merged = ComposeSpans.join(previous, block)
                    blocks = blocks.toMutableList().apply { set(index - 1, merged); removeAt(index) }
                    moveCursor(merged.id, previous.text.length)
                }
            }
        }
        return true
    }

    fun insertImages(uris: List<Uri>) {
        if (uris.isEmpty()) return
        val images = uris.map { ComposeBlock.Image(ComposeBlock.Image.Source.Picked(it)) }
        val block = focusedText
        val index = blocks.indexOfFirst { it.id == focusedId }.takeIf { it >= 0 } ?: blocks.lastIndex
        val (head, tail) = block?.let { ComposeSpans.split(it, selection.start) } ?: (null to ComposeBlock.Text(""))
        val after = tail.copy(id = ComposeBlock.newId())
        blocks = blocks.toMutableList().apply {
            if (head != null) set(index, head)
            addAll(index + 1, images + after)
        }
        moveCursor(after.id, 0)
    }

    fun remove(id: String) {
        blocks = blocks.filterNot { it.id == id }.ifEmpty { listOf(ComposeBlock.Text("")) }
    }

    private fun applyEdit(block: ComposeBlock.Text, newText: String, newSelection: TextRange) {
        val updated = update(block) {
            copy(
                text = newText,
                spans = ComposeSpans.shift(spans, text, newText, activeStyles),
                links = ComposeSpans.shiftLinks(links, text, newText),
                colors = ComposeSpans.shiftColors(colors, text, newText, activeColor),
            )
        }
        focusedId = block.id
        selection = newSelection
        if (newSelection.collapsed && activeStyles.isEmpty()) activeStyles = ComposeSpans.stylesAt(updated.spans, newSelection.start)
    }

    private fun handleReturn(block: ComposeBlock.Text, at: Int) {
        when {
            block.text.isEmpty() && block.lists.isNotEmpty() -> update(block) { copy(lists = lists.dropLast(1)) }
            block.text.isEmpty() && block.style == ComposeBlockStyle.QUOTE -> update(block) { copy(style = ComposeBlockStyle.PARAGRAPH) }
            at == 0 && block.text.isNotEmpty() -> {
                replace(block, listOf(ComposeBlock.Text("", style = block.style, lists = block.lists), block))
                moveCursor(block.id, 0)
            }
            else -> {
                val (head, tail) = ComposeSpans.split(block, at)
                val next = tail.copy(id = ComposeBlock.newId(), style = if (tail.text.isEmpty()) block.style.next else block.style)
                replace(block, listOf(head, next))
                moveCursor(next.id, 0)
            }
        }
    }

    private fun textBlock(id: String?): ComposeBlock.Text? = blocks.firstOrNull { it.id == id } as? ComposeBlock.Text

    private fun updateFocused(transform: ComposeBlock.Text.() -> ComposeBlock.Text) {
        focusedText?.let { update(it, transform) }
    }

    private fun updateSelected(transform: ComposeBlock.Text.() -> ComposeBlock.Text) {
        if (!selection.collapsed) updateFocused(transform)
    }

    private fun update(block: ComposeBlock.Text, transform: ComposeBlock.Text.() -> ComposeBlock.Text): ComposeBlock.Text =
        block.transform().also { replace(block, listOf(it)) }

    private fun replace(block: ComposeBlock, with: List<ComposeBlock>) {
        val index = blocks.indexOfFirst { it.id == block.id }
        if (index == -1) return
        blocks = blocks.toMutableList().apply { removeAt(index); addAll(index, with) }
    }

    private fun moveCursor(id: String, offset: Int) {
        focusedId = id
        selection = TextRange(offset)
        focusRequest++
    }

    companion object {
        const val LINE_BREAK = '\n'
        private const val LINE_SEPARATOR = '\u2028'

        private fun insertedReturn(old: String, new: String): Int? {
            if (new.length != old.length + 1) return null
            val at = old.commonPrefixWith(new).length
            return at.takeIf { new[it] == LINE_BREAK && new.removeRange(it, it + 1) == old }
        }

        private fun signature(blocks: List<ComposeBlock>): List<ComposeBlock> = blocks.map { block ->
            when (block) {
                is ComposeBlock.Text -> block.copy(id = "")
                is ComposeBlock.Image -> block.copy(id = "")
            }
        }
    }
}
