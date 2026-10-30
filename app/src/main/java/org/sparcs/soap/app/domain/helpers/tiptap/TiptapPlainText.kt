package org.sparcs.soap.app.domain.helpers.tiptap

val TiptapDocument.plainText: String
    get() = buildList { collectLines(content, this) }.joinToString("\n")

private fun collectLines(nodes: List<TiptapNode>, lines: MutableList<String>) {
    nodes.forEach { node ->
        if (node.isTextblock || node.type == TiptapNode.TEXT || node.type == TiptapNode.HARD_BREAK) {
            lines += buildString { appendInlineText(node) }
        } else {
            collectLines(node.content, lines)
        }
    }
}

private fun StringBuilder.appendInlineText(node: TiptapNode) {
    when (node.type) {
        TiptapNode.TEXT -> append(node.text.orEmpty())
        TiptapNode.HARD_BREAK -> append('\n')
        TiptapNode.ATTACHMENT_IMAGE, TiptapNode.IMAGE, TiptapNode.LINK_BOOKMARK -> Unit
        else -> node.content.forEach { appendInlineText(it) }
    }
}
