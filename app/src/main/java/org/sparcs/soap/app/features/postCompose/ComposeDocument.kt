package org.sparcs.soap.app.features.postCompose

import org.sparcs.soap.app.domain.helpers.tiptap.TiptapDocument
import org.sparcs.soap.app.domain.helpers.tiptap.TiptapMark
import org.sparcs.soap.app.domain.helpers.tiptap.TiptapNode
import org.sparcs.soap.app.domain.helpers.tiptap.TiptapParser
import org.sparcs.soap.app.domain.helpers.tiptap.TiptapValue
import java.net.URI

object ComposeDocument {
    private const val DEFAULT_IMAGE_WIDTH = 500.0

    fun fromContent(content: String, baseUrl: String): List<ComposeBlock>? {
        val document = TiptapParser().parseOrNull(content) ?: LegacyHtml.document(content) ?: return null
        return fromTiptap(document, baseUrl)
    }

    fun fromTiptap(document: TiptapDocument, baseUrl: String): List<ComposeBlock> =
        Importer(baseUrl).apply { appendBlocks(document.content, ComposeBlockStyle.PARAGRAPH, emptyList()) }.blocks

    fun toTiptap(blocks: List<ComposeBlock>, uploads: Map<String, Pair<Int, String>>): TiptapDocument {
        val pieces = blocks.mapNotNull { piece(it, uploads) }.trimmingBlankEdges()
        return TiptapDocument(nest(mergingRuns(pieces), depth = 0))
    }

    fun imageIds(document: TiptapDocument, existingUrls: Map<String, Int>): List<Int> = buildList {
        fun collect(nodes: List<TiptapNode>) {
            nodes.forEach { node ->
                if (node.type == TiptapNode.ATTACHMENT_IMAGE || node.type == TiptapNode.IMAGE) {
                    val id = node["data-attachment"]?.intValue ?: existingUrls[node["src"]?.stringValue]
                    if (id != null && id !in this) add(id)
                }
                collect(node.content)
            }
        }
        collect(document.content)
    }

    private data class Piece(val node: TiptapNode, val lists: List<ComposeList>, val style: ComposeBlockStyle)

    private fun piece(block: ComposeBlock, uploads: Map<String, Pair<Int, String>>): Piece? = when (block) {
        is ComposeBlock.Text -> Piece(textNode(block), block.lists, block.style)
        is ComposeBlock.Image -> imageNode(block, uploads)?.let {
            Piece(TiptapNode(TiptapNode.PARAGRAPH, content = listOf(it)), emptyList(), ComposeBlockStyle.PARAGRAPH)
        }
    }

    private fun textNode(block: ComposeBlock.Text): TiptapNode {
        val level = block.style.headingLevel
        return when {
            level != null -> TiptapNode(TiptapNode.HEADING, attrs = mapOf("level" to TiptapValue.Num(level.toDouble())), content = ComposeInline.nodes(block))
            block.style == ComposeBlockStyle.MONOSPACED -> codeBlock(block.text)
            block.style == ComposeBlockStyle.QUOTE -> TiptapNode(TiptapNode.BLOCKQUOTE, content = listOf(paragraph(block)))
            else -> paragraph(block)
        }
    }

    private fun paragraph(block: ComposeBlock.Text) = TiptapNode(TiptapNode.PARAGRAPH, content = ComposeInline.nodes(block))

    private fun codeBlock(code: String) =
        TiptapNode(TiptapNode.CODE_BLOCK, content = if (code.isEmpty()) emptyList() else listOf(TiptapNode(TiptapNode.TEXT, text = code)))

    private fun List<Piece>.trimmingBlankEdges(): List<Piece> {
        fun Piece.isBlank() = lists.isEmpty() && node.type == TiptapNode.PARAGRAPH &&
            node.content.all { it.type == TiptapNode.TEXT && it.text.isNullOrBlank() }
        val first = indexOfFirst { !it.isBlank() }
        if (first == -1) return emptyList()
        return subList(first, indexOfLast { !it.isBlank() } + 1)
    }

    private fun mergingRuns(pieces: List<Piece>): List<Piece> =
        pieces.fold(mutableListOf()) { merged, piece ->
            val joined = merged.lastOrNull()?.let { join(it, piece) }
            if (joined != null) merged[merged.lastIndex] = joined else merged += piece
            merged
        }

    private fun join(first: Piece, second: Piece): Piece? {
        if (first.style != second.style || first.lists != second.lists) return null
        return when (first.style) {
            ComposeBlockStyle.MONOSPACED -> first.copy(node = codeBlock(first.node.codeText + "\n" + second.node.codeText))
            ComposeBlockStyle.QUOTE -> first.copy(node = first.node.copy(content = first.node.content + second.node.content))
            else -> null
        }
    }

    private val TiptapNode.codeText: String
        get() = content.firstOrNull()?.text.orEmpty()

    private fun nest(pieces: List<Piece>, depth: Int): List<TiptapNode> = buildList {
        var index = 0
        while (index < pieces.size) {
            val list = pieces[index].lists.getOrNull(depth)
            if (list == null) {
                add(pieces[index].node)
                index++
                continue
            }
            var end = index
            while (end < pieces.size && pieces[end].lists.getOrNull(depth) == list) end++
            add(listNode(list, items(pieces.subList(index, end), depth)))
            index = end
        }
    }

    private fun items(run: List<Piece>, depth: Int): List<TiptapNode> = buildList {
        var start = 0
        while (start < run.size) {
            var end = start + 1
            while (end < run.size && run[end].lists.size > depth + 1) end++
            val head = run[start]
            val content = if (head.lists.size == depth + 1) listOf(head.node) + nest(run.subList(start + 1, end), depth + 1)
            else nest(run.subList(start, end), depth + 1)
            add(TiptapNode(TiptapNode.LIST_ITEM, content = content))
            start = end
        }
    }

    private fun listNode(list: ComposeList, items: List<TiptapNode>): TiptapNode = when {
        !list.isOrdered -> TiptapNode(TiptapNode.BULLET_LIST, content = items)
        list.start == 1 -> TiptapNode(TiptapNode.ORDERED_LIST, content = items)
        else -> TiptapNode(TiptapNode.ORDERED_LIST, attrs = mapOf("start" to TiptapValue.Num(list.start.toDouble())), content = items)
    }

    private fun imageNode(block: ComposeBlock.Image, uploads: Map<String, Pair<Int, String>>): TiptapNode? =
        when (val source = block.source) {
            is ComposeBlock.Image.Source.Existing -> source.node
            is ComposeBlock.Image.Source.Picked -> uploads[block.id]?.let { (id, url) ->
                TiptapNode(
                    TiptapNode.ATTACHMENT_IMAGE,
                    attrs = mapOf(
                        "src" to TiptapValue.Str(url),
                        "width" to TiptapValue.Num(DEFAULT_IMAGE_WIDTH),
                        "data-attachment" to TiptapValue.Str(id.toString()),
                    )
                )
            }
        }

    private class Importer(private val baseUrl: String) {
        val blocks = mutableListOf<ComposeBlock>()

        fun appendBlocks(nodes: List<TiptapNode>, style: ComposeBlockStyle, lists: List<ComposeList>) {
            nodes.forEach { node ->
                when (node.type) {
                    TiptapNode.PARAGRAPH -> appendInline(node.content, style, lists)
                    TiptapNode.HEADING -> appendInline(node.content, ComposeBlockStyle.heading(node["level"]?.intValue), lists)
                    TiptapNode.CODE_BLOCK -> appendCode(node, lists)
                    TiptapNode.BLOCKQUOTE -> appendBlocks(node.content, ComposeBlockStyle.QUOTE, lists)
                    TiptapNode.BULLET_LIST, TiptapNode.ORDERED_LIST -> appendList(node, style, lists)
                    TiptapNode.TEXT, TiptapNode.HARD_BREAK, TiptapNode.IMAGE, TiptapNode.ATTACHMENT_IMAGE, TiptapNode.LINK_BOOKMARK ->
                        appendInline(listOf(node), style, lists)
                    TiptapNode.HORIZONTAL_RULE -> Unit
                    else -> appendBlocks(node.content, style, lists)
                }
            }
        }

        private fun appendList(node: TiptapNode, style: ComposeBlockStyle, lists: List<ComposeList>) {
            val list = if (node.type == TiptapNode.ORDERED_LIST) ComposeList(isOrdered = true, start = node["start"]?.intValue ?: 1)
            else ComposeList.BULLET
            node.content.forEach { item ->
                appendBlocks(if (item.type == TiptapNode.LIST_ITEM) item.content else listOf(item), style, lists + list)
            }
        }

        private fun appendCode(node: TiptapNode, lists: List<ComposeList>) {
            val code = node.content.joinToString("") { if (it.type == TiptapNode.HARD_BREAK) "\n" else it.text.orEmpty() }
            code.split('\n').forEach { line ->
                blocks += ComposeBlock.Text(line, style = ComposeBlockStyle.MONOSPACED, lists = lists)
            }
        }

        private fun appendInline(nodes: List<TiptapNode>, style: ComposeBlockStyle, lists: List<ComposeList>) {
            val run = ComposeInline.Builder()
            var hasMedia = false

            fun flush(force: Boolean = false) {
                if (run.isNotEmpty || force) blocks += run.build(style, lists)
                run.clear()
            }

            fun visit(children: List<TiptapNode>) {
                children.forEach { node ->
                    when (node.type) {
                        TiptapNode.TEXT -> run.append(node.text.orEmpty(), node.marks)
                        TiptapNode.HARD_BREAK -> run.append("\n", emptyList())
                        TiptapNode.LINK_BOOKMARK -> node["href"]?.stringValue?.takeIf { it.isNotBlank() }?.let { href ->
                            val title = node["title"]?.stringValue?.takeIf { it.isNotBlank() } ?: href
                            run.appendLink(title, href)
                        }
                        TiptapNode.IMAGE, TiptapNode.ATTACHMENT_IMAGE -> {
                            val url = resolve(node["src"]?.stringValue) ?: return@forEach
                            flush()
                            hasMedia = true
                            blocks += ComposeBlock.Image(ComposeBlock.Image.Source.Existing(node, url))
                        }
                        else -> visit(node.content)
                    }
                }
            }

            visit(nodes)
            flush(force = !hasMedia)
        }

        private fun resolve(src: String?): String? {
            val trimmed = src?.trim()?.takeIf { it.isNotEmpty() } ?: return null
            return runCatching { URI(baseUrl).resolve(trimmed).toString() }.getOrNull()
        }
    }
}

private object ComposeInline {

    fun nodes(block: ComposeBlock.Text): List<TiptapNode> {
        if (block.text.isEmpty()) return emptyList()
        val boundaries = (listOf(0, block.text.length) +
            block.spans.boundaries() + block.links.boundaries() + block.colors.boundaries() +
            block.text.indices.filter { block.text[it] == '\n' }.flatMap { listOf(it, it + 1) })
            .filter { it in 0..block.text.length }
            .distinct()
            .sorted()

        return boundaries.zipWithNext().mapNotNull { (start, end) ->
            val piece = block.text.substring(start, end)
            when {
                piece.isEmpty() -> null
                piece == "\n" -> TiptapNode(TiptapNode.HARD_BREAK)
                else -> TiptapNode(TiptapNode.TEXT, marks = marks(block, start, end), text = piece)
            }
        }
    }

    private fun marks(block: ComposeBlock.Text, start: Int, end: Int): List<TiptapMark> {
        fun <T : ComposeRange<T>> List<T>.covering() = firstOrNull { it.start <= start && end <= it.end }
        val styles = ComposeStyle.entries
            .filter { style -> block.spans.any { it.style == style && it.start <= start && end <= it.end } }
            .map { TiptapMark(it.markType) }
        val color = block.colors.covering()?.let { TiptapMark(TiptapMark.TEXT_STYLE, mapOf("color" to TiptapValue.Str(it.hex))) }
        val link = block.links.covering()?.let { TiptapMark(TiptapMark.LINK, mapOf("href" to TiptapValue.Str(it.href))) }
        return styles + listOfNotNull(color, link)
    }

    private fun <T : ComposeRange<T>> List<T>.boundaries(): List<Int> = flatMap { listOf(it.start, it.end) }

    class Builder {
        private val text = StringBuilder()
        private val spans = mutableListOf<ComposeSpan>()
        private val links = mutableListOf<ComposeLink>()
        private val colors = mutableListOf<ComposeColor>()

        val isNotEmpty: Boolean
            get() = text.isNotEmpty()

        fun append(string: String, marks: List<TiptapMark>) {
            val start = text.length
            text.append(string)
            val end = text.length
            if (end == start) return
            marks.forEach { mark ->
                ComposeStyle.fromMark(mark.type)?.let { spans += ComposeSpan(start, end, it) }
                when (mark.type) {
                    TiptapMark.LINK -> mark["href"]?.stringValue?.let { links += ComposeLink(start, end, it) }
                    TiptapMark.TEXT_STYLE -> ComposeColors.normalize(mark["color"]?.stringValue)?.let { colors += ComposeColor(start, end, it) }
                }
            }
        }

        fun appendLink(title: String, href: String) {
            if (text.isNotEmpty() && !text.last().isWhitespace()) text.append(' ')
            append(title, listOf(TiptapMark(TiptapMark.LINK, mapOf("href" to TiptapValue.Str(href)))))
        }

        fun build(style: ComposeBlockStyle, lists: List<ComposeList>) = ComposeBlock.Text(
            text = text.toString(),
            spans = merged(spans) { a, b -> a.style == b.style },
            style = style,
            links = merged(links) { a, b -> a.href == b.href },
            colors = merged(colors) { a, b -> a.hex == b.hex },
            lists = lists,
        )

        fun clear() {
            text.clear()
            spans.clear()
            links.clear()
            colors.clear()
        }

        private fun <T : ComposeRange<T>> merged(ranges: List<T>, same: (T, T) -> Boolean): List<T> =
            ranges.fold(mutableListOf()) { result, range ->
                val index = result.indexOfLast { same(it, range) && it.end == range.start }
                if (index >= 0) result[index] = result[index].withRange(result[index].start, range.end) else result += range
                result
            }
    }
}
