package org.sparcs.soap.app.features.postCompose

internal object ComposeSpans {

    fun shift(spans: List<ComposeSpan>, old: String, new: String, typingStyles: Set<ComposeStyle>): List<ComposeSpan> {
        val edit = Edit.between(old, new) ?: return spans
        val shifted = edit.map(spans, extendsAtEnd = true)
        if (edit.inserted == 0) return normalize(shifted)

        val trimmed = shifted.flatMap { span ->
            if (span.style in typingStyles) listOf(span) else span.cut(edit.start, edit.insertEnd)
        }
        val added = typingStyles.map { ComposeSpan(edit.start, edit.insertEnd, it) }
        return normalize(trimmed + added)
    }

    fun shiftLinks(links: List<ComposeLink>, old: String, new: String): List<ComposeLink> =
        Edit.between(old, new)?.map(links, extendsAtEnd = false) ?: links

    fun shiftColors(colors: List<ComposeColor>, old: String, new: String, typingColor: String?): List<ComposeColor> {
        val edit = Edit.between(old, new) ?: return colors
        val shifted = edit.map(colors, extendsAtEnd = true)
        if (edit.inserted == 0) return normalizeColors(shifted)
        return paint(shifted, edit.start, edit.insertEnd, typingColor)
    }

    fun stylesAt(spans: List<ComposeSpan>, offset: Int): Set<ComposeStyle> =
        spans.filter { offset > it.start && offset <= it.end }.map { it.style }.toSet()

    fun colorAt(colors: List<ComposeColor>, offset: Int): String? =
        colors.firstOrNull { offset > it.start && offset <= it.end }?.hex

    fun colorCovering(colors: List<ComposeColor>, start: Int, end: Int): String? =
        colors.firstOrNull { it.start <= start && end <= it.end }?.hex

    fun covers(spans: List<ComposeSpan>, style: ComposeStyle, start: Int, end: Int): Boolean {
        if (start >= end) return false
        var cursor = start
        spans.filter { it.style == style }.sortedBy { it.start }.forEach { span ->
            if (span.start <= cursor && span.end > cursor) cursor = span.end
        }
        return cursor >= end
    }

    fun apply(spans: List<ComposeSpan>, style: ComposeStyle, start: Int, end: Int, on: Boolean): List<ComposeSpan> {
        val others = spans.filter { it.style != style }
        val same = spans.filter { it.style == style }.flatMap { it.cut(start, end) }
        return normalize(others + same + if (on) listOf(ComposeSpan(start, end, style)) else emptyList())
    }

    fun paint(colors: List<ComposeColor>, start: Int, end: Int, hex: String?): List<ComposeColor> {
        val kept = colors.flatMap { it.cut(start, end) }
        return normalizeColors(kept + listOfNotNull(hex?.let { ComposeColor(start, end, it) }))
    }

    fun split(block: ComposeBlock.Text, at: Int): Pair<ComposeBlock.Text, ComposeBlock.Text> {
        val cut = at.coerceIn(0, block.text.length)
        val head = block.copy(
            text = block.text.substring(0, cut),
            spans = block.spans.before(cut),
            links = block.links.before(cut),
            colors = block.colors.before(cut),
        )
        val tail = block.copy(
            text = block.text.substring(cut),
            spans = block.spans.after(cut),
            links = block.links.after(cut),
            colors = block.colors.after(cut),
        )
        return head to tail
    }

    fun join(first: ComposeBlock.Text, second: ComposeBlock.Text): ComposeBlock.Text {
        val offset = first.text.length
        return first.copy(
            text = first.text + second.text,
            spans = normalize(first.spans + second.spans.offset(offset)),
            links = first.links + second.links.offset(offset),
            colors = normalizeColors(first.colors + second.colors.offset(offset)),
        )
    }

    private fun <T : ComposeRange<T>> List<T>.before(cut: Int): List<T> =
        mapNotNull { range -> range.withRange(range.start, minOf(range.end, cut)).takeIf { range.start < cut && it.end > it.start } }

    private fun <T : ComposeRange<T>> List<T>.after(cut: Int): List<T> =
        mapNotNull { range -> range.withRange(maxOf(range.start, cut) - cut, range.end - cut).takeIf { range.end > cut && it.end > it.start } }

    private fun <T : ComposeRange<T>> List<T>.offset(by: Int): List<T> =
        map { it.withRange(it.start + by, it.end + by) }

    private fun <T : ComposeRange<T>> T.cut(start: Int, end: Int): List<T> =
        if (this.end <= start || this.start >= end) listOf(this)
        else listOfNotNull(
            withRange(this.start, start).takeIf { it.end > it.start },
            withRange(end, this.end).takeIf { it.end > it.start },
        )

    private fun normalize(spans: List<ComposeSpan>): List<ComposeSpan> =
        spans.groupBy { it.style }
            .flatMap { (_, group) -> merged(group) }
            .sortedWith(compareBy({ it.start }, { it.style.ordinal }))

    private fun normalizeColors(colors: List<ComposeColor>): List<ComposeColor> =
        colors.groupBy { it.hex }
            .flatMap { (_, group) -> merged(group) }
            .sortedBy { it.start }

    private fun <T : ComposeRange<T>> merged(ranges: List<T>): List<T> =
        ranges.sortedBy { it.start }.fold(mutableListOf()) { merged, range ->
            val last = merged.lastOrNull()
            if (last != null && range.start <= last.end) merged[merged.lastIndex] = last.withRange(last.start, maxOf(last.end, range.end))
            else merged += range
            merged
        }

    private class Edit(val start: Int, val removed: Int, val inserted: Int) {
        val insertEnd: Int
            get() = start + inserted

        fun <T : ComposeRange<T>> map(ranges: List<T>, extendsAtEnd: Boolean): List<T> = ranges.mapNotNull { range ->
            val start = map(range.start, isEnd = false)
            val end = map(range.end, isEnd = extendsAtEnd)
            if (end > start) range.withRange(start, end) else null
        }

        private fun map(offset: Int, isEnd: Boolean): Int = when {
            offset < start || (offset == start && !isEnd) -> offset
            offset <= start + removed -> if (isEnd && offset == start && removed == 0) offset else start
            else -> offset - removed + inserted
        }

        companion object {
            fun between(old: String, new: String): Edit? {
                if (old == new) return null
                val prefix = old.commonPrefixWith(new).length
                val suffix = old.substring(prefix).commonSuffixWith(new.substring(prefix)).length
                return Edit(prefix, old.length - prefix - suffix, new.length - prefix - suffix)
            }
        }
    }
}
