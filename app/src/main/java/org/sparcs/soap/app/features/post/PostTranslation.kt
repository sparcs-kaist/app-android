package org.sparcs.soap.app.features.post

import org.sparcs.soap.app.domain.helpers.tiptap.TiptapAttributedTextBlocks
import org.sparcs.soap.app.domain.helpers.tiptap.TiptapDocument
import org.sparcs.soap.app.domain.helpers.tiptap.TiptapNode
import org.sparcs.soap.app.domain.helpers.tiptap.TiptapParser
import org.sparcs.soap.app.domain.helpers.tiptap.attributedTextBlocks
import org.sparcs.soap.app.domain.helpers.tiptap.replacingTextBlocks
import org.sparcs.soap.app.domain.usecases.translation.PostBatchTranslationResult
import org.sparcs.soap.app.features.postCompose.LegacyHtml
import org.sparcs.soap.app.shared.views.tiptap.TiptapLayout

data class PostTranslation(
    val title: String?,
    val layout: TiptapLayout,
    val sourceLanguage: String?,
    val targetLanguage: String,
)

sealed interface PostTranslationState {
    data object Original : PostTranslationState
    data class Translating(override val targetLanguage: String) : PostTranslationState
    data class DownloadRequired(val sourceLanguage: String, override val targetLanguage: String) : PostTranslationState
    data class Translated(val translation: PostTranslation) : PostTranslationState

    val targetLanguage: String?
        get() = (this as? Translated)?.translation?.targetLanguage
}

data class LanguagePickerSections(
    val suggested: List<String>,
    val others: List<String>,
) {
    val isEmpty: Boolean get() = suggested.isEmpty() && others.isEmpty()
}

object PostTranslator {
    private const val TITLE_KEY = "title"
    const val SUGGESTION_LIMIT = 5

    fun document(content: String, body: PostBody): TiptapDocument =
        TiptapParser().parseOrNull(content)
            ?: LegacyHtml.document(content)
            ?: TiptapDocument(body.plainText.lines().map(::paragraph))

    fun targetLanguages(available: List<String>, sourceLanguage: String?): List<String> =
        available.filter { it != sourceLanguage }

    fun suggestedTargets(suggested: List<String>, targets: List<String>): List<String> =
        suggested.filter { it in targets }

    fun quickTarget(suggested: List<String>, targets: List<String>, current: String?): String? =
        if (current != null) null else suggestedTargets(suggested, targets).firstOrNull()

    fun pickerSections(
        targets: List<String>,
        suggested: List<String>,
        current: String?,
        query: String,
        name: (String) -> String,
        aliases: (String) -> List<String> = { emptyList() },
    ): LanguagePickerSections {
        val byName = compareBy(String.CASE_INSENSITIVE_ORDER, name)
        val term = query.trim()
        if (term.isNotEmpty()) {
            val matches = targets.distinct().filter { code ->
                (aliases(code) + name(code)).any { it.contains(term, ignoreCase = true) } ||
                    code.startsWith(term, ignoreCase = true)
            }
            return LanguagePickerSections(suggested = emptyList(), others = matches.sortedWith(byName))
        }
        val top = suggestedTargets(listOfNotNull(current) + suggested, targets).distinct().take(SUGGESTION_LIMIT)
        val others = targets.distinct().filterNot { it in top }.sortedWith(byName)
        return LanguagePickerSections(suggested = top, others = others)
    }

    suspend fun translate(
        title: String?,
        document: TiptapDocument,
        baseUrl: String,
        targetLanguage: String,
        translate: suspend (List<String>) -> PostBatchTranslationResult,
    ): PostBatchTranslationOutcome {
        val attributed = document.attributedTextBlocks
        val requests = requests(title, attributed)

        val result = translate(requests.values.toList())
        if (result !is PostBatchTranslationResult.Success) return PostBatchTranslationOutcome.Incomplete(result)
        val texts = requests.keys.zip(result.texts).toMap()

        val blocks = attributed.blocks.mapIndexed { index, block ->
            val text = texts[blockKey(index)] ?: return@mapIndexed block
            val runs = attributed.markedRuns(index).mapIndexed { runIndex, run ->
                run to (texts[runKey(index, runIndex)] ?: run.text)
            }
            TiptapAttributedTextBlocks.restoringMarks(runs, onto = text)
        }
        val translated = document.replacingTextBlocks(blocks, attributed)
        return PostBatchTranslationOutcome.Translated(
            PostTranslation(
                title = texts[TITLE_KEY] ?: title,
                layout = TiptapLayout.from(translated, baseUrl),
                sourceLanguage = result.sourceLanguage,
                targetLanguage = targetLanguage,
            )
        )
    }

    private fun requests(title: String?, attributed: TiptapAttributedTextBlocks): Map<String, String> = buildMap {
        if (!title.isNullOrBlank()) put(TITLE_KEY, title)
        attributed.blocks.forEachIndexed { index, block ->
            if (block.text.isBlank()) return@forEachIndexed
            put(blockKey(index), block.text)
            attributed.markedRuns(index).forEachIndexed { runIndex, run -> put(runKey(index, runIndex), run.text) }
        }
    }

    private fun blockKey(index: Int) = "b$index"

    private fun runKey(index: Int, runIndex: Int) = "r$index-$runIndex"

    private fun paragraph(line: String) = TiptapNode(
        TiptapNode.PARAGRAPH,
        content = if (line.isEmpty()) emptyList() else listOf(TiptapNode(TiptapNode.TEXT, text = line)),
    )
}

sealed interface PostBatchTranslationOutcome {
    data class Translated(val translation: PostTranslation) : PostBatchTranslationOutcome
    data class Incomplete(val result: PostBatchTranslationResult) : PostBatchTranslationOutcome
}
