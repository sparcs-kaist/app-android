package org.sparcs.soap.app.domain.helpers.tiptap

data class TiptapNode(
    val type: String,
    val attrs: Map<String, TiptapValue> = emptyMap(),
    val content: List<TiptapNode> = emptyList(),
    val marks: List<TiptapMark> = emptyList(),
    val text: String? = null,
) {
    val isTextblock: Boolean
        get() = type == PARAGRAPH || type == HEADING || type == CODE_BLOCK

    operator fun get(attribute: String): TiptapValue? = attrs[attribute]

    companion object {
        const val DOC = "doc"
        const val PARAGRAPH = "paragraph"
        const val TEXT = "text"
        const val HEADING = "heading"
        const val BLOCKQUOTE = "blockquote"
        const val BULLET_LIST = "bulletList"
        const val ORDERED_LIST = "orderedList"
        const val LIST_ITEM = "listItem"
        const val CODE_BLOCK = "codeBlock"
        const val HARD_BREAK = "hardBreak"
        const val HORIZONTAL_RULE = "horizontalRule"
        const val IMAGE = "image"
        const val ATTACHMENT_IMAGE = "attachmentImage"
        const val LINK_BOOKMARK = "linkBookmark"
    }
}

data class TiptapMark(
    val type: String,
    val attrs: Map<String, TiptapValue> = emptyMap(),
) {
    operator fun get(attribute: String): TiptapValue? = attrs[attribute]

    companion object {
        const val BOLD = "bold"
        const val ITALIC = "italic"
        const val UNDERLINE = "underline"
        const val STRIKE = "strike"
        const val CODE = "code"
        const val LINK = "link"
        const val TEXT_STYLE = "textStyle"
        const val HIGHLIGHT = "highlight"
    }
}

data class TiptapDocument(val content: List<TiptapNode>) {
    val isEmpty: Boolean
        get() = content.isEmpty()
}
