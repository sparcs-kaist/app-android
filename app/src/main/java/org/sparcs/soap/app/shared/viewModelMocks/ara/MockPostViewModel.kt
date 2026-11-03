package org.sparcs.soap.app.shared.viewModelMocks.ara

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import org.sparcs.soap.app.domain.enums.ara.AraContentReportType
import org.sparcs.soap.app.domain.helpers.AlertState
import org.sparcs.soap.app.domain.models.ara.AraPost
import org.sparcs.soap.app.domain.models.ara.AraPostAuthor
import org.sparcs.soap.app.domain.models.ara.AraPostComment
import org.sparcs.soap.app.domain.models.summarization.PostSummaryPhase
import org.sparcs.soap.app.domain.models.summarization.SummarizationState
import org.sparcs.soap.app.domain.models.translation.TranslationState
import org.sparcs.soap.app.features.post.PostBody
import org.sparcs.soap.app.features.post.PostTranslationState
import org.sparcs.soap.app.features.post.PostViewModel
import org.sparcs.soap.app.features.post.PostViewModelProtocol

class MockPostViewModel(
    initialState: PostViewModel.ViewState,
    post: AraPost,
    translationState: PostTranslationState = PostTranslationState.Original,
    summaryPhase: PostSummaryPhase? = null,
) : PostViewModelProtocol {

    override val state: StateFlow<PostViewModel.ViewState> = MutableStateFlow(initialState)

    override var alertState: AlertState? = null
    override var isAlertPresented: Boolean = false

    override val postId: Int = post.id

    override val post: StateFlow<AraPost?> = MutableStateFlow(post).asStateFlow()
    override val contentBody: StateFlow<PostBody?> =
        MutableStateFlow(post.content?.let { PostBody.from(it, post.board?.slug) }).asStateFlow()
    override val postTranslationState: StateFlow<PostTranslationState> = MutableStateFlow(translationState).asStateFlow()
    override val translationTargets: StateFlow<List<String>> = MutableStateFlow(listOf("en", "ja", "zh")).asStateFlow()
    override val summaryPhase: StateFlow<PostSummaryPhase?> = MutableStateFlow(summaryPhase).asStateFlow()
    override val canSummarise: StateFlow<Boolean> = MutableStateFlow(true).asStateFlow()
    override val canEdit: StateFlow<Boolean> = MutableStateFlow(post.isMine == true).asStateFlow()

    override val translationState: StateFlow<TranslationState> = MutableStateFlow(TranslationState.Idle).asStateFlow()
    override val summarizationState: StateFlow<SummarizationState> = MutableStateFlow(SummarizationState.Idle).asStateFlow()
    override val commentTranslations: StateFlow<Map<String, TranslationState>> =
        MutableStateFlow<Map<String, TranslationState>>(emptyMap()).asStateFlow()

    override fun translationLanguages(): List<String> = listOf("en", "ja", "zh")
    override fun suggestedTranslationLanguages(): List<String> = listOf("en")
    override fun defaultTranslationLanguage(): String = "en"

    override fun translate(content: String, targetLanguage: String, allowDownload: Boolean, scope: CoroutineScope) {}
    override fun showOriginal() {}
    override fun summarize(content: String, scope: CoroutineScope) {}
    override fun summarizeReview(content: String, scope: CoroutineScope) {}
    override fun hideSummary() {}
    override fun translateComment(commentId: String, content: String, targetLanguage: String, allowDownload: Boolean, scope: CoroutineScope) {}
    override fun showCommentOriginal(commentId: String) {}

    override fun translatePost(targetLanguage: String, allowDownload: Boolean) {}
    override fun showPostOriginal() {}
    override fun summarise() {}
    override fun dismissSummary() {}
    override fun translateComment(commentId: Int, content: String, targetLanguage: String, allowDownload: Boolean) {}
    override fun showCommentOriginal(commentId: Int) {}

    override fun fetchPost() {}
    override fun upVote() {}
    override fun downVote() {}
    override fun report(type: AraContentReportType) {}
    override suspend fun deletePost(): Boolean = false
    override fun toggleBookmark() {}
    override fun setAuthorBlocked(author: AraPostAuthor, blocked: Boolean) {}

    override suspend fun writeComment(content: String): AraPostComment? = null
    override suspend fun writeThreadedComment(commentID: Int, content: String): AraPostComment? = null
    override suspend fun editComment(commentID: Int, content: String): AraPostComment? = null

    override fun upVoteComment(comment: AraPostComment) {}
    override fun downVoteComment(comment: AraPostComment) {}
    override fun reportComment(commentID: Int, type: AraContentReportType) {}
    override fun deleteComment(comment: AraPostComment) {}
}
