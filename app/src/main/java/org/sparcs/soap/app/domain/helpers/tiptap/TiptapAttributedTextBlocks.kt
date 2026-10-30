package org.sparcs.soap.app.domain.helpers.tiptap

data class TiptapRun(val text: String, val markSet: Int? = null)

data class TiptapAttributedBlock(val runs: List<TiptapRun>) {
    val text: String
        get() = runs.joinToString("") { it.text }
}

data class TiptapMarkedRun(val text: String, val markSet: Int)

class TiptapAttributedTextBlocks internal constructor(
    val blocks: List<TiptapAttributedBlock>,
    internal val markSets: List<List<TiptapMark>>,
) {
    fun markedRuns(inBlock: Int): List<TiptapMarkedRun> {
        val block = blocks.getOrNull(inBlock) ?: return emptyList()
        return block.runs.mapNotNull { run ->
            val markSet = run.markSet ?: return@mapNotNull null
            run.text.trim().takeIf { it.isNotEmpty() }?.let { TiptapMarkedRun(it, markSet) }
        }
    }

    companion object {
        fun restoringMarks(
            runs: List<Pair<TiptapMarkedRun, String>>,
            onto: String,
        ): TiptapAttributedBlock {
            val markAt = arrayOfNulls<Int>(onto.length)
            var cursor = 0
            runs.forEach { (run, translation) ->
                val range = listOf(translation, run.text)
                    .map { it.trim(::isWhitespaceOrPunctuation) }
                    .filter { it.isNotEmpty() }
                    .firstNotNullOfOrNull { candidate -> onto.findRange(candidate, cursor) }
                    ?: return@forEach
                range.forEach { markAt[it] = run.markSet }
                cursor = range.last + 1
            }
            return TiptapAttributedBlock(groupRuns(onto, markAt))
        }

        private fun String.findRange(candidate: String, cursor: Int): IntRange? {
            val start = indexOf(candidate, cursor, ignoreCase = true).takeIf { it >= 0 }
                ?: indexOf(candidate, ignoreCase = true).takeIf { it >= 0 }
                ?: return null
            return start until start + candidate.length
        }

        private fun groupRuns(text: String, markAt: Array<Int?>): List<TiptapRun> = buildList {
            var start = 0
            for (index in 1..text.length) {
                if (index == text.length || markAt[index] != markAt[start]) {
                    add(TiptapRun(text.substring(start, index), markAt[start]))
                    start = index
                }
            }
        }

        private fun isWhitespaceOrPunctuation(char: Char): Boolean =
            char.isWhitespace() || Character.getType(char).toByte() in punctuationTypes

        private val punctuationTypes = setOf(
            Character.CONNECTOR_PUNCTUATION,
            Character.DASH_PUNCTUATION,
            Character.START_PUNCTUATION,
            Character.END_PUNCTUATION,
            Character.INITIAL_QUOTE_PUNCTUATION,
            Character.FINAL_QUOTE_PUNCTUATION,
            Character.OTHER_PUNCTUATION,
        )
    }
}

val TiptapDocument.attributedTextBlocks: TiptapAttributedTextBlocks
    get() {
        val markSets = mutableListOf<List<TiptapMark>>()
        fun indexOf(marks: List<TiptapMark>): Int =
            markSets.indexOf(marks).takeIf { it >= 0 } ?: markSets.size.also { markSets += marks }

        fun MutableList<TiptapRun>.appendInline(nodes: List<TiptapNode>) {
            nodes.forEach { node ->
                when (node.type) {
                    TiptapNode.TEXT -> add(TiptapRun(node.text.orEmpty(), node.marks.takeIf { it.isNotEmpty() }?.let(::indexOf)))
                    TiptapNode.HARD_BREAK -> add(TiptapRun("\n"))
                    else -> appendInline(node.content)
                }
            }
        }

        val blocks = buildList {
            visitTextBlocks(content) { node -> add(TiptapAttributedBlock(buildList { appendInline(node.content) })) }
        }
        return TiptapAttributedTextBlocks(blocks, markSets)
    }

fun TiptapDocument.replacingTextBlocks(
    replacements: List<TiptapAttributedBlock>,
    source: TiptapAttributedTextBlocks,
): TiptapDocument = rebuildTextBlocks(replacements.size) { index, node ->
    inlineNodes(replacements[index], source.markSets) + node.content.media()
}

private fun inlineNodes(block: TiptapAttributedBlock, markSets: List<List<TiptapMark>>): List<TiptapNode> {
    val nodes = mutableListOf<TiptapNode>()
    block.runs.forEach { run ->
        val marks = run.markSet?.let(markSets::getOrNull).orEmpty()
        lineNodes(run.text, marks).forEach { node ->
            val last = nodes.lastOrNull()
            if (node.type == TiptapNode.TEXT && last?.type == TiptapNode.TEXT && last.marks == marks) {
                nodes[nodes.lastIndex] = last.copy(text = last.text.orEmpty() + node.text.orEmpty())
            } else {
                nodes += node
            }
        }
    }
    return nodes
}
