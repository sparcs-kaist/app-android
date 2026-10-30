package org.sparcs.soap.app.domain.helpers.tiptap

val TiptapDocument.textBlocks: List<String>
    get() = buildList { visitTextBlocks(content) { add(inlineText(it.content)) } }

fun TiptapDocument.replacingTextBlocks(replacements: List<String>): TiptapDocument =
    rebuildTextBlocks(replacements.size) { index, node ->
        replacingText(node.content, replacements[index])
    }

internal fun TiptapDocument.rebuildTextBlocks(
    replacementCount: Int,
    replace: (index: Int, block: TiptapNode) -> List<TiptapNode>,
): TiptapDocument {
    var index = 0
    fun rebuild(nodes: List<TiptapNode>): List<TiptapNode> = nodes.map { node ->
        when {
            node.isTextBlock -> {
                val current = index++
                if (current < replacementCount) node.copy(content = replace(current, node)) else node
            }
            node.type == TiptapNode.CODE_BLOCK -> node
            else -> node.copy(content = rebuild(node.content))
        }
    }
    return TiptapDocument(rebuild(content))
}

internal fun visitTextBlocks(nodes: List<TiptapNode>, visit: (TiptapNode) -> Unit) {
    nodes.forEach { node ->
        when {
            node.isTextBlock -> visit(node)
            node.type == TiptapNode.CODE_BLOCK -> Unit
            else -> visitTextBlocks(node.content, visit)
        }
    }
}

internal val TiptapNode.isTextBlock: Boolean
    get() = type == TiptapNode.PARAGRAPH || type == TiptapNode.HEADING

internal val TiptapNode.isInlineText: Boolean
    get() = type == TiptapNode.TEXT || type == TiptapNode.HARD_BREAK

internal fun List<TiptapNode>.media(): List<TiptapNode> = filterNot { it.isInlineText }

internal fun lineNodes(text: String, marks: List<TiptapMark>): List<TiptapNode> = buildList {
    text.split('\n').forEachIndexed { lineIndex, line ->
        if (lineIndex > 0) add(TiptapNode(TiptapNode.HARD_BREAK))
        if (line.isNotEmpty()) add(TiptapNode(TiptapNode.TEXT, marks = marks, text = line))
    }
}

private fun inlineText(nodes: List<TiptapNode>): String = nodes.joinToString("") { node ->
    when (node.type) {
        TiptapNode.TEXT -> node.text.orEmpty()
        TiptapNode.HARD_BREAK -> "\n"
        else -> inlineText(node.content)
    }
}

private fun replacingText(nodes: List<TiptapNode>, text: String): List<TiptapNode> {
    val textNodes = nodes.filter { it.type == TiptapNode.TEXT }
    val firstMarks = textNodes.firstOrNull()?.marks.orEmpty()
    val marks = if (textNodes.all { it.marks == firstMarks }) firstMarks else emptyList()
    return lineNodes(text, marks) + nodes.media()
}
