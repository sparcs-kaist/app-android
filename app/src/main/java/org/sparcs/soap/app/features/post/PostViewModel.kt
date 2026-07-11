package org.sparcs.soap.app.features.post

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import org.sparcs.soap.R
import org.sparcs.soap.app.domain.enums.ara.AraContentReportType
import org.sparcs.soap.app.domain.enums.ara.PostOrigin
import org.sparcs.soap.app.domain.helpers.AlertState
import org.sparcs.soap.app.domain.models.ara.AraPost
import org.sparcs.soap.app.domain.models.ara.AraPostComment
import org.sparcs.soap.app.domain.models.summarization.SummarizationState
import org.sparcs.soap.app.domain.models.translation.TranslationState
import org.sparcs.soap.app.domain.services.AnalyticsServiceProtocol
import org.sparcs.soap.app.domain.usecases.ara.AraBoardUseCaseProtocol
import org.sparcs.soap.app.domain.usecases.ara.AraCommentUseCaseProtocol
import org.sparcs.soap.app.domain.usecases.summarization.SummarizationResultState
import org.sparcs.soap.app.domain.usecases.summarization.SummarizationUseCaseProtocol
import org.sparcs.soap.app.domain.usecases.translation.PostTranslationResult
import org.sparcs.soap.app.domain.usecases.translation.PostTranslationUseCaseProtocol
import org.sparcs.soap.app.features.post.event.PostCommentCellEvent
import org.sparcs.soap.app.features.post.event.PostViewEvent
import org.sparcs.soap.app.shared.extensions.araContentToPlainText
import org.sparcs.soap.app.shared.extensions.toAlertState
import timber.log.Timber
import javax.inject.Inject

interface PostViewModelProtocol {
    val postId: Int
    val post: StateFlow<AraPost?>
    val state: StateFlow<PostViewModel.ViewState>
    val isFoundationModelsAvailable: Boolean

    var alertState: AlertState?
    var isAlertPresented: Boolean

    val translationState: StateFlow<TranslationState>
    val summarizationState: StateFlow<SummarizationState>
    fun translationLanguages(): List<String>
    fun suggestedTranslationLanguages(): List<String>
    fun defaultTranslationLanguage(): String
    fun translatePost(targetLanguage: String, allowDownload: Boolean = false)
    fun showOriginal()
    fun summarizePost()
    fun hideSummary()

    val commentTranslations: StateFlow<Map<Int, TranslationState>>
    fun translateComment(
        commentId: Int,
        content: String,
        targetLanguage: String,
        allowDownload: Boolean = false,
    )
    fun showCommentOriginal(commentId: Int)

    fun fetchPost()
    fun upVote()
    fun downVote()
    fun report(type: AraContentReportType)
    suspend fun deletePost(): Boolean
    fun toggleBookmark()

    suspend fun summarisedContent(): String

    suspend fun writeComment(content: String): AraPostComment?
    suspend fun writeThreadedComment(
        commentID: Int,
        content: String,
    ): AraPostComment?
    suspend fun editComment(commentID: Int, content: String): AraPostComment?

    fun upVoteComment(comment: AraPostComment)
    fun downVoteComment(comment: AraPostComment)
    fun reportComment(commentID: Int, type: AraContentReportType)
    fun deleteComment(comment: AraPostComment)
}

@HiltViewModel
class PostViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val araBoardUseCase: AraBoardUseCaseProtocol,
    val araCommentUseCase: AraCommentUseCaseProtocol,
//    private val foundationModelsUseCase: FoundationModelsUseCaseProtocol
    private val analyticsService: AnalyticsServiceProtocol,
    private val postTranslationUseCase: PostTranslationUseCaseProtocol,
    private val summarizationUseCase: SummarizationUseCaseProtocol,
) : ViewModel(), PostViewModelProtocol {

    sealed interface ViewState {
        data object Loading : ViewState
        data object Loaded : ViewState
        data class Error(val error: Exception) : ViewState
    }

    // MARK: - Properties
    private val _state = MutableStateFlow<ViewState>(ViewState.Loading)
    override val state: StateFlow<ViewState> = _state.asStateFlow()

    override val postId: Int = savedStateHandle.get<Int>("postId")
        ?: throw IllegalArgumentException("postId is missing")

    private val _post = MutableStateFlow<AraPost?>(null)
    override val post: StateFlow<AraPost?> = _post.asStateFlow()

    override val isFoundationModelsAvailable: Boolean
        get() = false // TODO: foundationModelsUseCase.isAvailable

    private var isToggling = false

    override var alertState by mutableStateOf<AlertState?>(null)
    override var isAlertPresented by mutableStateOf(false)

    // MARK: - Translation
    private val _translationState = MutableStateFlow<TranslationState>(TranslationState.Idle)
    override val translationState: StateFlow<TranslationState> = _translationState.asStateFlow()

    override fun translationLanguages(): List<String> =
        postTranslationUseCase.availableLanguages()

    override fun suggestedTranslationLanguages(): List<String> =
        postTranslationUseCase.suggestedLanguages()

    override fun defaultTranslationLanguage(): String =
        postTranslationUseCase.deviceLanguage()

    override fun translatePost(targetLanguage: String, allowDownload: Boolean) {
        if (_translationState.value is TranslationState.Loading ||
            _translationState.value is TranslationState.Downloading
        ) return
        val content = _post.value?.content ?: return
        _translationState.value =
            if (allowDownload) TranslationState.Downloading else TranslationState.Loading
        viewModelScope.launch {
            _translationState.value = when (
                val result = postTranslationUseCase.translate(
                    content.araContentToPlainText(), targetLanguage,
                    isHtml = false, allowDownload = allowDownload
                )
            ) {
                is PostTranslationResult.Success ->
                    TranslationState.Translated(result.text, result.sourceLanguage)

                is PostTranslationResult.NeedsDownload ->
                    TranslationState.DownloadRequired(result.sourceLanguage, result.targetLanguage)

                PostTranslationResult.SameLanguage,
                PostTranslationResult.Unsupported -> TranslationState.Unsupported

                is PostTranslationResult.Failed -> TranslationState.Failed
            }
        }
    }

    override fun showOriginal() {
        _translationState.value = TranslationState.Idle
    }

    // MARK: - Summarization
    private val _summarizationState = MutableStateFlow<SummarizationState>(SummarizationState.Idle)
    override val summarizationState: StateFlow<SummarizationState> = _summarizationState.asStateFlow()

    override fun summarizePost() {
        if (_summarizationState.value is SummarizationState.Loading) return
        val content = _post.value?.content ?: return
        _summarizationState.value = SummarizationState.Loading
        viewModelScope.launch {
            _summarizationState.value = when (
                val result = summarizationUseCase.summarise(
                    content.araContentToPlainText(), isHtml = false
                )
            ) {
                is SummarizationResultState.Success ->
                    SummarizationState.Summarized(result.summary)

                SummarizationResultState.Unavailable -> SummarizationState.Unavailable

                is SummarizationResultState.Failed -> SummarizationState.Failed
            }
        }
    }

    override fun hideSummary() {
        _summarizationState.value = SummarizationState.Idle
    }

    // MARK: - Comment translation (one-tap, device language)
    private val _commentTranslations = MutableStateFlow<Map<Int, TranslationState>>(emptyMap())
    override val commentTranslations: StateFlow<Map<Int, TranslationState>> =
        _commentTranslations.asStateFlow()

    override fun translateComment(
        commentId: Int,
        content: String,
        targetLanguage: String,
        allowDownload: Boolean,
    ) {
        val current = _commentTranslations.value[commentId]
        if (current is TranslationState.Loading || current is TranslationState.Downloading) return
        _commentTranslations.value += (
            commentId to if (allowDownload) TranslationState.Downloading else TranslationState.Loading
        )
        viewModelScope.launch {
            val state = when (
                val result = postTranslationUseCase.translate(
                    content, targetLanguage, isHtml = false, allowDownload = allowDownload
                )
            ) {
                is PostTranslationResult.Success ->
                    TranslationState.Translated(result.text, result.sourceLanguage)

                is PostTranslationResult.NeedsDownload ->
                    TranslationState.DownloadRequired(result.sourceLanguage, result.targetLanguage)

                PostTranslationResult.SameLanguage,
                PostTranslationResult.Unsupported -> TranslationState.Unsupported

                is PostTranslationResult.Failed -> TranslationState.Failed
            }
            _commentTranslations.value += (commentId to state)
        }
    }

    override fun showCommentOriginal(commentId: Int) {
        _commentTranslations.value -= commentId
    }

    private fun insertThreadedComment(
        comments: MutableList<AraPostComment>,
        comment: AraPostComment,
    ): Boolean {
        val parentComment = comment.parentComment ?: return false
        for (idx in comments.indices) {
            if (comments[idx].id == parentComment) {
                comments[idx].comments.add(comment)
                return true
            }
        }
        return false
    }

    // MARK: - Functions
    override fun fetchPost() {
        val isFirstTime = _post.value == null // Case: Deep link entry (PostOrigin.All)
        val origin = if (isFirstTime) PostOrigin.All else PostOrigin.Board

        if (isFirstTime) _state.value = ViewState.Loading

        viewModelScope.launch {
            try {
                val fetchedPost = araBoardUseCase.fetchPost(origin = origin, postID = postId)
                _post.value = fetchedPost
                if (isFirstTime) _state.value = ViewState.Loaded
            } catch (e: Exception) {
                if (isFirstTime) _state.value = ViewState.Error(e)
                alertState = e.toAlertState(R.string.unable_to_fetch_post)
                isAlertPresented = true
            }
        }
    }

    override fun upVote() {
        val currentPost = _post.value ?: return
        val previousMyVote = currentPost.myVote
        val previousUpVotes = currentPost.upVotes
        val previousDownVotes = currentPost.downVotes

        viewModelScope.launch {
            try {
                if (previousMyVote == true) {
                    // cancel upvote
                    val updatedComments = currentPost.comments.toList().toMutableList()
                    _post.value = currentPost.copy(
                        myVote = null,
                        upVotes = previousUpVotes - 1,
                        comments = updatedComments
                    )
                    araBoardUseCase.cancelVote(currentPost.id)
                } else {
                    // upvote
                    val newDownVotes =
                        if (previousMyVote == false) previousDownVotes - 1 else previousDownVotes
                    val updatedComments = currentPost.comments.toList().toMutableList()
                    _post.value = currentPost.copy(
                        myVote = true,
                        upVotes = previousUpVotes + 1,
                        downVotes = newDownVotes,
                        comments = updatedComments
                    )
                    araBoardUseCase.upVotePost(currentPost.id)
                }
                analyticsService.logEvent(PostViewEvent.PostUpVoted)
            } catch (_: Exception) {
                val recoveryComments = currentPost.comments.toList().toMutableList()
                _post.value = currentPost.copy(
                    myVote = previousMyVote,
                    upVotes = previousUpVotes,
                    downVotes = previousDownVotes,
                    comments = recoveryComments
                )
            }
        }
    }

    override fun downVote() {
        val currentPost = _post.value ?: return
        val previousMyVote = currentPost.myVote
        val previousUpVotes = currentPost.upVotes
        val previousDownVotes = currentPost.downVotes

        viewModelScope.launch {
            try {
                if (previousMyVote == false) {
                    // cancel downvote
                    val updatedComments = currentPost.comments.toList().toMutableList()
                    _post.value = currentPost.copy(
                        myVote = null,
                        downVotes = previousDownVotes - 1,
                        comments = updatedComments
                    )
                    araBoardUseCase.cancelVote(currentPost.id)
                } else {
                    // downvote
                    val newUpVotes =
                        if (previousMyVote == true) previousUpVotes - 1 else previousUpVotes
                    val updatedComments = currentPost.comments.toList().toMutableList()
                    _post.value = currentPost.copy(
                        myVote = false,
                        downVotes = previousDownVotes + 1,
                        upVotes = newUpVotes,
                        comments = updatedComments
                    )
                    araBoardUseCase.downVotePost(currentPost.id)
                }
                analyticsService.logEvent(PostViewEvent.PostDownVoted)
            } catch (_: Exception) {
                val recoveryComments = currentPost.comments.toList().toMutableList()
                _post.value = currentPost.copy(
                    myVote = previousMyVote,
                    upVotes = previousUpVotes,
                    downVotes = previousDownVotes,
                    comments = recoveryComments
                )
            }
        }
    }

    override suspend fun writeComment(content: String): AraPostComment? {
        val current = _post.value ?: return null
        return try {
            val comment = araCommentUseCase.writeComment(postID = current.id, content = content)
            comment.isMine = true

            val updatedComments = current.comments.toMutableList().apply { add(comment) }
            _post.value = current.copy(
                comments = updatedComments,
                commentCount = current.commentCount + 1
            )
            analyticsService.logEvent(PostViewEvent.CommentSubmitted)
            comment
        } catch (e: Exception) {
            alertState = e.toAlertState(R.string.unexpected_error_uploading_comment)
            isAlertPresented = true
            null
        }
    }

    override suspend fun writeThreadedComment(
        commentID: Int,
        content: String,
    ): AraPostComment? {
        val current = _post.value ?: return null
        return try {
            // insert threaded comments
            val comment = araCommentUseCase.writeThreadedComment(commentID = commentID, content = content)
            comment.isMine = true

            val updatedComments = current.comments.toMutableList()
            insertThreadedComment(updatedComments, comment)

            _post.value = current.copy(
                comments = updatedComments,
                commentCount = current.commentCount + 1
            )
            analyticsService.logEvent(PostViewEvent.CommentSubmitted)
            comment
        } catch (e: Exception) {
            alertState = e.toAlertState(R.string.unexpected_error_uploading_comment)
            isAlertPresented = true
            null
        }
    }

    override suspend fun editComment(commentID: Int, content: String): AraPostComment? {
        val current = _post.value ?: return null
        try {
            val editedComment =
                araCommentUseCase.editComment(commentID = commentID, content = content)
            editedComment.isMine = true

            val updatedComments = updateCommentInList(current.comments, commentID) { target ->
                target.copy(content = content)
            }

            _post.value = current.copy(comments = updatedComments.toMutableList())
            return editedComment
        } catch (e: Exception) {
            alertState = e.toAlertState(R.string.unexpected_error_editing_comment)
            isAlertPresented = true
            return null
        }
    }

    override fun report(type: AraContentReportType) {
        val current = _post.value ?: return
        viewModelScope.launch {
            try {
                araBoardUseCase.reportPost(postID = current.id, type = type)
                analyticsService.logEvent(PostViewEvent.PostReported(type.name))
            } catch (e: Exception) {
                alertState = e.toAlertState(R.string.error_unable_to_submit_report)
                isAlertPresented = true
            }
        }
    }

    override suspend fun summarisedContent(): String {
//        return foundationModelsUseCase.summarise(post.content ?: "", maxWords = 50, tone = "concise")
        return ""
    }

    override suspend fun deletePost(): Boolean {
        val current = _post.value ?: return false
        return try {
            araBoardUseCase.deletePost(postID = current.id)
            true
        } catch (e: Exception) {
            alertState = e.toAlertState(R.string.unexpected_error_deleting_post)
            isAlertPresented = true
            false
        }
    }

    override fun toggleBookmark() {
        if (isToggling) return
        val current = _post.value ?: return
        val previous = current.myScrap
        val originalScrapId = current.scrapID
        isToggling = true

        _post.value = current.copy(myScrap = !previous)

        viewModelScope.launch {
            try {
                if (previous) {
                    val scrapId = originalScrapId ?: return@launch
                    araBoardUseCase.removeBookmark(scrapId)
                    _post.value = _post.value?.copy(scrapID = null)
                } else {
                    val newScrapId = araBoardUseCase.addBookmark(current.id)
                    _post.value = _post.value?.copy(scrapID = newScrapId)
                }

                analyticsService.logEvent(
                    PostViewEvent.BookmarkToggled(_post.value?.myScrap ?: false),
                )
            } catch (e: Exception) {
                Timber.e(e, "toggleBookmark error")
                _post.value = current.copy(
                    myScrap = previous,
                    scrapID = originalScrapId
                )
            } finally {
                isToggling = false
            }
        }
    }

    // MARK: - Comment Operations
    override fun upVoteComment(comment: AraPostComment) {
        val currentPost = _post.value ?: return
        val previousMyVote = comment.myVote
        val previousUpVotes = comment.upVotes
        val previousDownVotes = comment.downVotes

        val updatedComments = updateCommentInList(currentPost.comments, comment.id) { target ->
            if (previousMyVote == true) {
                target.copy(myVote = null, upVotes = previousUpVotes - 1)
            } else {
                val newDownVotes =
                    if (previousMyVote == false) previousDownVotes - 1 else previousDownVotes
                target.copy(myVote = true, upVotes = previousUpVotes + 1, downVotes = newDownVotes)
            }
        }
        _post.value = currentPost.copy(comments = updatedComments.toMutableList())

        viewModelScope.launch {
            try {
                if (previousMyVote == true) {
                    araCommentUseCase.cancelVote(comment.id)
                } else {
                    araCommentUseCase.upVoteComment(comment.id)
                }
                analyticsService.logEvent(PostCommentCellEvent.CommentUpVoted)
            } catch (_: Exception) {
                _post.value = currentPost
            }
        }
    }

    override fun downVoteComment(comment: AraPostComment) {
        val currentPost = _post.value ?: return
        val previousMyVote = comment.myVote
        val previousUpVotes = comment.upVotes
        val previousDownVotes = comment.downVotes

        val updatedComments = updateCommentInList(currentPost.comments, comment.id) { target ->
            if (previousMyVote == false) {
                target.copy(myVote = null, downVotes = previousDownVotes - 1)
            } else {
                val newUpVotes =
                    if (previousMyVote == true) previousUpVotes - 1 else previousUpVotes
                target.copy(myVote = false, downVotes = previousDownVotes + 1, upVotes = newUpVotes)
            }
        }
        _post.value = currentPost.copy(comments = updatedComments.toMutableList())

        viewModelScope.launch {
            try {
                if (previousMyVote == false) {
                    araCommentUseCase.cancelVote(comment.id)
                } else {
                    araCommentUseCase.downVoteComment(comment.id)
                }
                analyticsService.logEvent(PostCommentCellEvent.CommentDownVoted)
            } catch (_: Exception) {
                _post.value = currentPost
            }
        }
    }

    override fun reportComment(commentID: Int, type: AraContentReportType) {
        viewModelScope.launch {
            try {
                araCommentUseCase.reportComment(commentID = commentID, type = type)
                analyticsService.logEvent(PostCommentCellEvent.CommentReported(type.name))
            } catch (e: Exception) {
                Timber.e(e, "Error during report: ${e.message}")
            }
        }
    }

    override fun deleteComment(comment: AraPostComment) {
        val currentPost = _post.value ?: return

        val updatedComments = updateCommentInList(currentPost.comments, comment.id) { target ->
            target.copy(content = null)
        }
        _post.value = currentPost.copy(comments = updatedComments.toMutableList())

        viewModelScope.launch {
            try {
                araCommentUseCase.deleteComment(commentID = comment.id)
                analyticsService.logEvent(PostCommentCellEvent.CommentDeleted)
            } catch (e: Exception) {
                Timber.e(e, "Error during delete: ${e.message}")
                _post.value = currentPost
            }
        }
    }

    private fun updateCommentInList(
        comments: List<AraPostComment>,
        commentID: Int,
        transform: (AraPostComment) -> AraPostComment,
    ): List<AraPostComment> {
        return comments.map { parent ->
            if (parent.id == commentID) {
                transform(parent)
            } else {
                parent.copy(
                    comments = parent.comments.map { child ->
                        if (child.id == commentID) transform(child) else child
                    }.toMutableList()
                )
            }
        }
    }
}
