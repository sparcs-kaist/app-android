package org.sparcs.soap.app.features.post

import org.sparcs.soap.app.domain.helpers.Constants
import org.sparcs.soap.app.domain.helpers.tiptap.TiptapNode
import org.sparcs.soap.app.domain.helpers.tiptap.TiptapParser
import org.sparcs.soap.app.domain.helpers.tiptap.TiptapValue
import org.sparcs.soap.app.domain.helpers.tiptap.plainText
import org.sparcs.soap.app.domain.models.ara.AraPost
import org.sparcs.soap.app.shared.extensions.htmlToPlainText
import org.sparcs.soap.app.shared.views.tiptap.TiptapLayout

sealed interface PostBody {
    val plainText: String
    val textLength: Int

    data class Tiptap(val layout: TiptapLayout, override val plainText: String) : PostBody {
        override val textLength: Int
            get() = plainText.count { !it.isWhitespace() }
    }

    data class Html(
        val html: String,
        val baseUrl: String,
        override val plainText: String = htmlText(html),
    ) : PostBody {
        override val textLength: Int
            get() {
                var count = 0
                var isInTag = false
                html.forEach { char ->
                    when {
                        char == '<' -> isInTag = true
                        char == '>' -> isInTag = false
                        !isInTag && !char.isWhitespace() -> count++
                    }
                }
                return count
            }
    }

    companion object {
        private const val PORTAL_URL = "https://portal.kaist.ac.kr/"
        private const val PORTAL_NOTICE_SLUG = "portal-notice"

        private val hiddenElementPattern = Regex("<(script|style)\\b[^>]*>.*?</\\1>", setOf(RegexOption.IGNORE_CASE, RegexOption.DOT_MATCHES_ALL))
        private val lineBreakPattern = Regex("<br\\s*/?>|</(p|div|li|h[1-6]|tr|blockquote)>", RegexOption.IGNORE_CASE)
        private val tagPattern = Regex("<[^>]*>")
        private val blankLinesPattern = Regex("\\n{3,}")

        fun baseUrl(boardSlug: String?): String =
            if (boardSlug == PORTAL_NOTICE_SLUG) PORTAL_URL else Constants.ARA_SHARE_URL

        fun from(content: String, boardSlug: String?): PostBody {
            val baseUrl = baseUrl(boardSlug)
            val document = TiptapParser().parseOrNull(content) ?: return Html(content, baseUrl)
            return Tiptap(TiptapLayout.from(document, baseUrl), document.plainText)
        }

        private fun htmlText(html: String): String {
            val text = html
                .replace(hiddenElementPattern, "")
                .replace(lineBreakPattern, "\n")
                .replace(tagPattern, "")
            return TiptapParser.decodeHtmlEntities(text)
                .lines()
                .joinToString("\n") { it.trim() }
                .replace(blankLinesPattern, "\n\n")
                .trim()
        }
    }
}

val AraPost.contentPlainText: String
    get() {
        val content = content.orEmpty()
        return TiptapParser().parseOrNull(content)?.plainText ?: content.htmlToPlainText()
    }

val AraPost.attachedImageUrls: List<String>
    get() {
        val images = attachments.orEmpty().filter { it.mimeType.startsWith("image/") && it.file != null }
        if (images.isEmpty()) return emptyList()

        val content = content.orEmpty()
        val references = TiptapParser().parseOrNull(content)
            ?.content?.flatMap { it.attributeStrings }?.joinToString("\n")
            ?: content
        return images.mapNotNull { attachment ->
            val url = attachment.file ?: return@mapNotNull null
            val names = listOf(url.path.substringAfterLast('/'), url.toString().substringAfterLast('/'))
            if (names.any { it.isNotEmpty() && references.contains(it) }) null else url.toString()
        }
    }

private val TiptapNode.attributeStrings: List<String>
    get() = attrs.values.mapNotNull(TiptapValue::stringValue) + content.flatMap { it.attributeStrings }
