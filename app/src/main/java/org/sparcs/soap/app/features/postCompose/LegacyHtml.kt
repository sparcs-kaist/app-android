package org.sparcs.soap.app.features.postCompose

import org.sparcs.soap.app.domain.helpers.tiptap.TiptapDocument
import org.sparcs.soap.app.domain.helpers.tiptap.TiptapNode
import org.sparcs.soap.app.domain.helpers.tiptap.TiptapParser
import org.sparcs.soap.app.domain.helpers.tiptap.TiptapValue

internal object LegacyHtml {
    private const val DEFAULT_IMAGE_WIDTH = 500.0
    private val paragraphPattern = Regex("<p>(.*?)</p>", RegexOption.DOT_MATCHES_ALL)
    private val imagePattern = Regex("^\\s*<img\\b([^>]*)>\\s*$", RegexOption.IGNORE_CASE)
    private val anchorPattern = Regex("<a\\b[^>]*>(.*?)</a>", setOf(RegexOption.IGNORE_CASE, RegexOption.DOT_MATCHES_ALL))

    fun document(html: String): TiptapDocument? {
        val trimmed = html.trim()
        if (trimmed.isEmpty()) return TiptapDocument(emptyList())

        val nodes = mutableListOf<TiptapNode>()
        var consumed = 0
        paragraphPattern.findAll(trimmed).forEach { match ->
            if (trimmed.substring(consumed, match.range.first).isNotBlank()) return null
            consumed = match.range.last + 1
            nodes += paragraph(match.groupValues[1]) ?: return null
        }
        if (trimmed.substring(consumed).isNotBlank() || nodes.isEmpty()) return null
        return TiptapDocument(nodes)
    }

    private fun paragraph(inner: String): TiptapNode? {
        val image = imagePattern.find(inner)
        if (image != null) return TiptapNode(TiptapNode.PARAGRAPH, content = listOf(imageNode(image.groupValues[1]) ?: return null))

        val text = anchorPattern.replace(inner) { it.groupValues[1] }
        if ('<' in text) return null
        val decoded = TiptapParser.decodeHtmlEntities(text)
        return TiptapNode(TiptapNode.PARAGRAPH, content = if (decoded.isEmpty()) emptyList() else listOf(TiptapNode(TiptapNode.TEXT, text = decoded)))
    }

    private fun imageNode(attributes: String): TiptapNode? {
        fun value(name: String) = Regex("$name=\"([^\"]*)\"").find(attributes)?.groupValues?.get(1)?.let(TiptapParser::decodeHtmlEntities)
        val src = value("src") ?: return null
        val attrs = buildMap {
            put("src", TiptapValue.Str(src))
            put("width", TiptapValue.Num(value("width")?.toDoubleOrNull() ?: DEFAULT_IMAGE_WIDTH))
            value("data-attachment")?.let { put("data-attachment", TiptapValue.Str(it)) }
        }
        return TiptapNode(TiptapNode.ATTACHMENT_IMAGE, attrs = attrs)
    }
}
