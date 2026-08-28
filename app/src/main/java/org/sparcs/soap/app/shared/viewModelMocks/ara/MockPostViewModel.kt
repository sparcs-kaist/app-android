package org.sparcs.soap.app.shared.viewModelMocks.ara

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import org.sparcs.soap.app.domain.enums.ara.AraContentReportType
import org.sparcs.soap.app.domain.helpers.AlertState
import org.sparcs.soap.app.domain.models.ara.AraPost
import org.sparcs.soap.app.domain.models.ara.AraPostComment
import org.sparcs.soap.app.domain.models.summarization.SummarizationState
import org.sparcs.soap.app.domain.models.translation.TranslationState
import org.sparcs.soap.app.features.post.PostViewModel
import org.sparcs.soap.app.features.post.PostViewModelProtocol


class MockPostViewModel(initialState: PostViewModel.ViewState, post: AraPost) : PostViewModelProtocol {

    override val state: StateFlow<PostViewModel.ViewState> =
        MutableStateFlow(initialState)
    override val isFoundationModelsAvailable = true

    override var alertState: AlertState? = null
    override var isAlertPresented: Boolean = false

    override val postId: Int = post.id

    private val _post = MutableStateFlow(post)
    override val post: StateFlow<AraPost?> = _post.asStateFlow()

    override val translationState: StateFlow<TranslationState> =
        MutableStateFlow(TranslationState.Idle).asStateFlow()
    override val summarizationState: StateFlow<SummarizationState> =
        MutableStateFlow(SummarizationState.Idle).asStateFlow()
    override fun translationLanguages(): List<String> = emptyList()
    override fun suggestedTranslationLanguages(): List<String> = emptyList()
    override fun defaultTranslationLanguage(): String = "en"

    override fun translate(content: String, targetLanguage: String, allowDownload: Boolean, scope: CoroutineScope) {}
    override fun summarize(content: String, scope: CoroutineScope) {}

    override fun translatePost(targetLanguage: String, allowDownload: Boolean) {}
    override fun showOriginal() {}
    override fun summarizePost() {}
    override fun hideSummary() {}

    override val commentTranslations: StateFlow<Map<String, TranslationState>> =
        MutableStateFlow<Map<String, TranslationState>>(emptyMap()).asStateFlow()

    override fun translateComment(commentId: String, content: String, targetLanguage: String, allowDownload: Boolean, scope: CoroutineScope) {}
    override fun showCommentOriginal(commentId: String) {}

    override fun translateComment(
        commentId: Int,
        content: String,
        targetLanguage: String,
        allowDownload: Boolean,
    ) {}
    override fun showCommentOriginal(commentId: Int) {}

    override fun fetchPost() {}

    override fun upVote() {}

    override fun downVote() {}

    override suspend fun writeComment(content: String): AraPostComment?{
        return null
    }

    override suspend fun writeThreadedComment(commentID: Int, content: String): AraPostComment? {
        return null
    }

    override suspend fun editComment(commentID: Int, content: String): AraPostComment? {
        return null
    }

    override fun report(type: AraContentReportType) {}

    override suspend fun summarisedContent(): String {
        return ""
    }

    override suspend fun deletePost(): Boolean { return false }
    override fun toggleBookmark() {}
    override fun upVoteComment(comment: AraPostComment) {}
    override fun downVoteComment(comment: AraPostComment) {}
    override fun reportComment(commentID: Int, type: AraContentReportType) {}
    override fun deleteComment(comment: AraPostComment) {}
}
