package org.sparcs.soap.app.shared.viewModels

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import org.sparcs.soap.app.domain.models.summarization.SummarizationState
import org.sparcs.soap.app.domain.models.translation.TranslationState
import org.sparcs.soap.app.domain.services.CrashlyticsServiceProtocol
import org.sparcs.soap.app.domain.usecases.summarization.SummarizationResultState
import org.sparcs.soap.app.domain.usecases.summarization.SummarizationUseCaseProtocol
import org.sparcs.soap.app.domain.usecases.translation.PostTranslationResult
import org.sparcs.soap.app.domain.usecases.translation.PostTranslationUseCaseProtocol
import javax.inject.Inject

interface TextProcessingProtocol {
    val translationState: StateFlow<TranslationState>
    val summarizationState: StateFlow<SummarizationState>
    val commentTranslations: StateFlow<Map<String, TranslationState>>

    fun translationLanguages(): List<String>
    fun suggestedTranslationLanguages(): List<String>
    fun defaultTranslationLanguage(): String

    fun translate(content: String, targetLanguage: String, allowDownload: Boolean = false, scope: CoroutineScope)
    fun showOriginal()

    fun summarize(content: String, scope: CoroutineScope)
    fun hideSummary()

    fun translateComment(commentId: String, content: String, targetLanguage: String, allowDownload: Boolean = false, scope: CoroutineScope)
    fun showCommentOriginal(commentId: String)
}

class TextProcessingDelegate @Inject constructor(
    private val postTranslationUseCase: PostTranslationUseCaseProtocol,
    private val summarizationUseCase: SummarizationUseCaseProtocol,
    private val crashlyticsService: CrashlyticsServiceProtocol,
) : TextProcessingProtocol {

    private val _translationState = MutableStateFlow<TranslationState>(TranslationState.Idle)
    override val translationState: StateFlow<TranslationState> = _translationState.asStateFlow()

    private val _summarizationState = MutableStateFlow<SummarizationState>(SummarizationState.Idle)
    override val summarizationState: StateFlow<SummarizationState> = _summarizationState.asStateFlow()

    private val _commentTranslations = MutableStateFlow<Map<String, TranslationState>>(emptyMap())
    override val commentTranslations: StateFlow<Map<String, TranslationState>> = _commentTranslations.asStateFlow()

    override fun translationLanguages(): List<String> = postTranslationUseCase.availableLanguages()
    override fun suggestedTranslationLanguages(): List<String> = postTranslationUseCase.suggestedLanguages()
    override fun defaultTranslationLanguage(): String = postTranslationUseCase.deviceLanguage()

    override fun translate(content: String, targetLanguage: String, allowDownload: Boolean, scope: CoroutineScope) {
        if (_translationState.value is TranslationState.Loading || _translationState.value is TranslationState.Downloading) return
        _translationState.value = if (allowDownload) TranslationState.Downloading else TranslationState.Loading
        scope.launch {
            _translationState.value = when (val result = postTranslationUseCase.translate(content, targetLanguage, isHtml = false, allowDownload = allowDownload)) {
                is PostTranslationResult.Success -> TranslationState.Translated(result.text, result.sourceLanguage)
                is PostTranslationResult.NeedsDownload -> TranslationState.DownloadRequired(result.sourceLanguage, result.targetLanguage)
                PostTranslationResult.SameLanguage -> TranslationState.SameLanguage
                PostTranslationResult.Unsupported -> TranslationState.Unsupported
                is PostTranslationResult.Failed -> {
                    TranslationState.Failed
                }
            }
        }
    }

    override fun showOriginal() { _translationState.value = TranslationState.Idle }

    override fun summarize(content: String, scope: CoroutineScope) {
        if (_summarizationState.value is SummarizationState.Loading) return
        _summarizationState.value = SummarizationState.Loading
        scope.launch {
            _summarizationState.value = when (val result = summarizationUseCase.summarise(content, isHtml = false)) {
                is SummarizationResultState.Success -> SummarizationState.Summarized(result.summary)
                SummarizationResultState.TooShort -> SummarizationState.TooShort
                SummarizationResultState.Unavailable -> SummarizationState.Unavailable
                is SummarizationResultState.Failed -> {
                    SummarizationState.Failed
                }
            }
        }
    }

    override fun hideSummary() { _summarizationState.value = SummarizationState.Idle }

    override fun translateComment(commentId: String, content: String, targetLanguage: String, allowDownload: Boolean, scope: CoroutineScope) {
        val current = _commentTranslations.value[commentId]
        if (current is TranslationState.Loading || current is TranslationState.Downloading) return
        _commentTranslations.value += (commentId to if (allowDownload) TranslationState.Downloading else TranslationState.Loading)
        scope.launch {
            val state = when (val result = postTranslationUseCase.translate(content, targetLanguage, isHtml = false, allowDownload = allowDownload)) {
                is PostTranslationResult.Success -> TranslationState.Translated(result.text, result.sourceLanguage)
                is PostTranslationResult.NeedsDownload -> TranslationState.DownloadRequired(result.sourceLanguage, result.targetLanguage)
                PostTranslationResult.SameLanguage -> TranslationState.SameLanguage
                PostTranslationResult.Unsupported -> TranslationState.Unsupported
                is PostTranslationResult.Failed -> TranslationState.Failed
            }
            _commentTranslations.value += (commentId to state)
        }
    }

    override fun showCommentOriginal(commentId: String) {
        _commentTranslations.value -= commentId
    }
}
