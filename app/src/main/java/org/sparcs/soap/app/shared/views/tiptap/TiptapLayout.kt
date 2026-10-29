package org.sparcs.soap.app.shared.views.tiptap

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.LinkAnnotation
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.withLink
import androidx.compose.ui.text.withStyle
import org.sparcs.soap.app.domain.helpers.tiptap.TiptapDocument
import org.sparcs.soap.app.domain.helpers.tiptap.TiptapMark
import org.sparcs.soap.app.domain.helpers.tiptap.TiptapNode
import java.net.URI

data class TiptapLayout(val blocks: List<TiptapBlock>) {
    val isEmpty: Boolean
        get() = blocks.isEmpty()

    val images: List<TiptapImage>
        get() = buildList { collectImages(blocks) }

    private fun MutableList<TiptapImage>.collectImages(blocks: List<TiptapBlock>) {
        blocks.forEach { block ->
            when (val kind = block.kind) {
                is TiptapBlock.Kind.Image -> add(kind.image)
                is TiptapBlock.Kind.Blockquote -> collectImages(kind.blocks)
                is TiptapBlock.Kind.ListBlock -> kind.list.items.forEach { collectImages(it.blocks) }
                else -> Unit
            }
        }
    }

    companion object {
        fun from(document: TiptapDocument, baseUrl: String? = null): TiptapLayout {
            val blocks = TiptapLayoutBuilder(baseUrl).blocks(document.content, depth = 0)
            return TiptapLayout(blocks.trimmingEmptyParagraphs())
        }

        private fun List<TiptapBlock>.trimmingEmptyParagraphs(): List<TiptapBlock> {
            val first = indexOfFirst { !it.isEmptyParagraph }
            if (first == -1) return emptyList()
            return subList(first, indexOfLast { !it.isEmptyParagraph } + 1)
        }
    }
}

data class TiptapBlock(val id: Int, val kind: Kind) {
    sealed interface Kind {
        data class Paragraph(val text: AnnotatedString) : Kind
        data class Heading(val level: Int, val text: AnnotatedString) : Kind
        data class CodeBlock(val code: String, val language: String?) : Kind
        data class Image(val image: TiptapImage) : Kind
        data class Bookmark(val bookmark: TiptapBookmark) : Kind
        data class Blockquote(val blocks: List<TiptapBlock>) : Kind
        data class ListBlock(val list: TiptapList) : Kind
        data object HorizontalRule : Kind
    }

    val isEmptyParagraph: Boolean
        get() = kind is Kind.Paragraph && kind.text.text.isBlank()
}

data class TiptapImage(val url: String, val alt: String?, val width: Float?)

data class TiptapBookmark(val url: String, val title: String?) {
    val displayTitle: String
        get() {
            val trimmed = title?.trim().orEmpty()
            if (trimmed.isEmpty()) return "URL"
            val host = hostOf(trimmed) ?: hostOf("https://$trimmed")
            if (host == null || '.' !in host) return trimmed
            return host.split('.').dropLast(1).asReversed()
                .firstOrNull { it.length > 2 }?.uppercase() ?: trimmed
        }

    private fun hostOf(string: String): String? =
        runCatching { URI(string).host }.getOrNull()?.takeIf { it.isNotEmpty() }
}

data class TiptapList(val isOrdered: Boolean, val items: List<Item>) {
    data class Item(val id: Int, val marker: String, val blocks: List<TiptapBlock>)
}

private val TIPTAP_LINK_SCHEMES = setOf("http", "https", "mailto", "tel")

private class TiptapLayoutBuilder(private val baseUrl: String?) {
    private var nextId = 0

    private fun makeBlock(kind: TiptapBlock.Kind) = TiptapBlock(nextId++, kind)

    fun blocks(nodes: List<TiptapNode>, depth: Int): List<TiptapBlock> =
        buildList { nodes.forEach { append(it, depth, this) } }

    private fun append(node: TiptapNode, depth: Int, blocks: MutableList<TiptapBlock>) {
        when (node.type) {
            TiptapNode.PARAGRAPH -> appendInline(node.content, blocks) { TiptapBlock.Kind.Paragraph(it) }

            TiptapNode.HEADING -> {
                val level = (node["level"]?.intValue ?: 1).coerceIn(1, 3)
                appendInline(node.content, blocks) { TiptapBlock.Kind.Heading(level, it) }
            }

            TiptapNode.CODE_BLOCK -> {
                val code = node.content.joinToString("") { if (it.type == TiptapNode.HARD_BREAK) "\n" else it.text.orEmpty() }
                blocks += makeBlock(TiptapBlock.Kind.CodeBlock(code, node["language"]?.stringValue))
            }

            TiptapNode.BLOCKQUOTE -> {
                val children = blocks(node.content, depth)
                if (children.isNotEmpty()) blocks += makeBlock(TiptapBlock.Kind.Blockquote(children))
            }

            TiptapNode.BULLET_LIST, TiptapNode.ORDERED_LIST -> {
                val isOrdered = node.type == TiptapNode.ORDERED_LIST
                val start = node["start"]?.intValue ?: 1
                val items = node.content.mapIndexed { offset, item ->
                    val children = if (item.type == TiptapNode.LIST_ITEM) item.content else listOf(item)
                    val marker = if (isOrdered) "${start + offset}." else BULLETS[depth % BULLETS.size]
                    TiptapList.Item(nextId++, marker, blocks(children, depth + 1))
                }
                if (items.isNotEmpty()) blocks += makeBlock(TiptapBlock.Kind.ListBlock(TiptapList(isOrdered, items)))
            }

            TiptapNode.HORIZONTAL_RULE -> blocks += makeBlock(TiptapBlock.Kind.HorizontalRule)

            TiptapNode.TEXT, TiptapNode.HARD_BREAK, TiptapNode.ATTACHMENT_IMAGE, TiptapNode.IMAGE, TiptapNode.LINK_BOOKMARK ->
                appendInline(listOf(node), blocks) { TiptapBlock.Kind.Paragraph(it) }

            else -> node.content.forEach { append(it, depth, blocks) }
        }
    }

    private fun appendInline(
        nodes: List<TiptapNode>,
        blocks: MutableList<TiptapBlock>,
        wrap: (AnnotatedString) -> TiptapBlock.Kind,
    ) {
        var run = AnnotatedString.Builder()
        var hasMedia = false

        fun flush() {
            val text = run.toAnnotatedString()
            if (text.isNotEmpty() && !(hasMedia && text.text.isBlank())) blocks += makeBlock(wrap(text))
            run = AnnotatedString.Builder()
        }

        fun visit(nodes: List<TiptapNode>) {
            nodes.forEach { node ->
                when (node.type) {
                    TiptapNode.TEXT -> run.append(styledText(node))
                    TiptapNode.HARD_BREAK -> run.append("\n")
                    TiptapNode.ATTACHMENT_IMAGE, TiptapNode.IMAGE -> {
                        hasMedia = true
                        flush()
                        image(node)?.let { blocks += makeBlock(TiptapBlock.Kind.Image(it)) }
                    }
                    TiptapNode.LINK_BOOKMARK -> {
                        hasMedia = true
                        flush()
                        resolveUrl(node["href"]?.stringValue)?.let { url ->
                            blocks += makeBlock(TiptapBlock.Kind.Bookmark(TiptapBookmark(url, node["title"]?.stringValue)))
                        }
                    }
                    else -> visit(node.content)
                }
            }
        }

        visit(nodes)
        if (!hasMedia && run.length == 0) blocks += makeBlock(wrap(AnnotatedString(""))) else flush()
    }

    private fun styledText(node: TiptapNode): AnnotatedString = buildAnnotatedString {
        val text = node.text.orEmpty()
        val link = node.marks.firstOrNull { it.type == TiptapMark.LINK }?.let { resolveUrl(it["href"]?.stringValue) }
        val decorations = node.marks.mapNotNull { decoration(it) }
        val style = node.marks.fold(SpanStyle()) { style, mark -> style.merge(spanStyle(mark)) }
            .let { if (decorations.isEmpty()) it else it.copy(textDecoration = TextDecoration.combine(decorations)) }

        withStyle(style) {
            if (link != null) withLink(LinkAnnotation.Url(link)) { append(text) } else append(text)
        }
    }

    private fun decoration(mark: TiptapMark): TextDecoration? = when (mark.type) {
        TiptapMark.UNDERLINE -> TextDecoration.Underline
        TiptapMark.STRIKE -> TextDecoration.LineThrough
        else -> null
    }

    private fun spanStyle(mark: TiptapMark): SpanStyle = when (mark.type) {
        TiptapMark.BOLD -> SpanStyle(fontWeight = FontWeight.Bold)
        TiptapMark.ITALIC -> SpanStyle(fontStyle = FontStyle.Italic)
        TiptapMark.CODE -> SpanStyle(fontFamily = FontFamily.Monospace)
        TiptapMark.TEXT_STYLE -> TiptapColor.parse(mark["color"]?.stringValue)?.let { SpanStyle(color = it) } ?: SpanStyle()
        TiptapMark.HIGHLIGHT -> TiptapColor.parse(mark["color"]?.stringValue, allowsExtremes = true)
            ?.let { SpanStyle(background = it.copy(alpha = 0.35f)) } ?: SpanStyle()
        else -> SpanStyle()
    }

    private fun image(node: TiptapNode): TiptapImage? {
        val url = resolveUrl(node["src"]?.stringValue) ?: return null
        val width = node["width"]?.doubleValue?.takeIf { it > 0 }?.toFloat()
        val alt = node["alt"]?.stringValue ?: node["title"]?.stringValue
        return TiptapImage(url, alt, width)
    }

    private fun resolveUrl(raw: String?): String? {
        val trimmed = raw?.trim()?.takeIf { it.isNotEmpty() } ?: return null
        val resolved = runCatching {
            when {
                trimmed.startsWith("www.", ignoreCase = true) -> URI("https://$trimmed")
                URI(trimmed).scheme != null -> URI(trimmed)
                baseUrl != null -> URI(baseUrl).resolve(trimmed)
                else -> null
            }
        }.getOrNull() ?: return null
        return resolved.takeIf { it.scheme?.lowercase() in TIPTAP_LINK_SCHEMES }?.toString()
    }

    companion object {
        private val BULLETS = listOf("•", "◦", "▪")
    }
}

internal object TiptapColor {
    fun parse(css: String?, allowsExtremes: Boolean = false): Color? {
        val value = css?.trim()?.lowercase()?.takeIf { it.isNotEmpty() } ?: return null
        val (red, green, blue) = components(value) ?: return null
        if (!allowsExtremes) {
            val luminance = 0.2126 * red + 0.7152 * green + 0.0722 * blue
            if (luminance !in 0.12..0.9) return null
        }
        return Color(red.toFloat(), green.toFloat(), blue.toFloat())
    }

    private fun components(css: String): Triple<Double, Double, Double>? {
        if (css.startsWith("#")) {
            var hex = css.drop(1)
            if (hex.length == 3 || hex.length == 4) hex = hex.take(3).flatMap { listOf(it, it) }.joinToString("")
            val value = hex.takeIf { it.length >= 6 }?.take(6)?.toLongOrNull(16) ?: return null
            return Triple(
                ((value shr 16) and 0xFF) / 255.0,
                ((value shr 8) and 0xFF) / 255.0,
                (value and 0xFF) / 255.0,
            )
        }
        if (css.startsWith("rgb")) {
            val open = css.indexOf('(')
            val close = css.lastIndexOf(')')
            if (open == -1 || close <= open) return null
            val channels = css.substring(open + 1, close)
                .split(',', ' ', '/')
                .filter { it.isNotBlank() }
                .take(3)
                .mapNotNull { it.toDoubleOrNull() }
            if (channels.size != 3) return null
            return Triple(channels[0] / 255, channels[1] / 255, channels[2] / 255)
        }
        return null
    }
}
