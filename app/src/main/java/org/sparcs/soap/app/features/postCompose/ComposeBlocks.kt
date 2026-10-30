package org.sparcs.soap.app.features.postCompose

import android.net.Uri
import org.sparcs.soap.app.domain.helpers.tiptap.TiptapMark
import org.sparcs.soap.app.domain.helpers.tiptap.TiptapNode
import java.util.UUID

enum class ComposeStyle(val markType: String) {
    BOLD(TiptapMark.BOLD),
    ITALIC(TiptapMark.ITALIC),
    UNDERLINE(TiptapMark.UNDERLINE),
    STRIKE(TiptapMark.STRIKE),
    CODE(TiptapMark.CODE);

    companion object {
        fun fromMark(type: String): ComposeStyle? = entries.firstOrNull { it.markType == type }
    }
}

enum class ComposeBlockStyle(val headingLevel: Int? = null) {
    PARAGRAPH,
    TITLE(1),
    HEADING(2),
    SUBHEADING(3),
    MONOSPACED,
    QUOTE;

    val isHeading: Boolean
        get() = headingLevel != null

    val next: ComposeBlockStyle
        get() = if (isHeading) PARAGRAPH else this

    companion object {
        fun heading(level: Int?): ComposeBlockStyle = when (level ?: 1) {
            1 -> TITLE
            2 -> HEADING
            else -> SUBHEADING
        }
    }
}

data class ComposeList(val isOrdered: Boolean, val start: Int = 1) {
    companion object {
        const val MAX_DEPTH = 6
        val BULLET = ComposeList(isOrdered = false)
        val NUMBERED = ComposeList(isOrdered = true)
    }
}

interface ComposeRange<T : ComposeRange<T>> {
    val start: Int
    val end: Int
    fun withRange(start: Int, end: Int): T
}

data class ComposeSpan(override val start: Int, override val end: Int, val style: ComposeStyle) : ComposeRange<ComposeSpan> {
    override fun withRange(start: Int, end: Int) = copy(start = start, end = end)
}

data class ComposeLink(override val start: Int, override val end: Int, val href: String) : ComposeRange<ComposeLink> {
    override fun withRange(start: Int, end: Int) = copy(start = start, end = end)
}

data class ComposeColor(override val start: Int, override val end: Int, val hex: String) : ComposeRange<ComposeColor> {
    override fun withRange(start: Int, end: Int) = copy(start = start, end = end)
}

sealed interface ComposeBlock {
    val id: String

    data class Text(
        val text: String,
        val spans: List<ComposeSpan> = emptyList(),
        val style: ComposeBlockStyle = ComposeBlockStyle.PARAGRAPH,
        val links: List<ComposeLink> = emptyList(),
        val colors: List<ComposeColor> = emptyList(),
        val lists: List<ComposeList> = emptyList(),
        override val id: String = newId(),
    ) : ComposeBlock

    data class Image(
        val source: Source,
        override val id: String = newId(),
    ) : ComposeBlock {
        sealed interface Source {
            data class Picked(val uri: Uri) : Source
            data class Existing(val node: TiptapNode, val url: String) : Source
        }
    }

    companion object {
        fun newId(): String = UUID.randomUUID().toString()
    }
}

object ComposeListMarkers {
    private val BULLETS = listOf("•", "◦", "▪")

    fun of(blocks: List<ComposeBlock>): Map<String, String> = buildMap {
        var path = emptyList<ComposeList>()
        var counts = emptyList<Int>()
        blocks.forEach { block ->
            val lists = (block as? ComposeBlock.Text)?.lists.orEmpty()
            if (lists.isEmpty()) {
                path = emptyList()
                counts = emptyList()
                return@forEach
            }
            val shared = path.zip(lists).takeWhile { (a, b) -> a == b }.size
            counts = counts.take(minOf(shared, lists.size)).let { kept ->
                if (kept.size == lists.size) kept.dropLast(1) + (kept.last() + 1)
                else kept + lists.drop(kept.size).map { it.start }
            }
            path = lists
            put(block.id, if (lists.last().isOrdered) "${counts.last()}." else BULLETS[(lists.size - 1) % BULLETS.size])
        }
    }
}
