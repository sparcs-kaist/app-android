package org.sparcs.soap.app.features.post

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.sparcs.soap.R
import org.sparcs.soap.app.domain.enums.ara.AraContentReportType
import org.sparcs.soap.app.domain.enums.ara.PostOrigin
import org.sparcs.soap.app.domain.error.NetworkError
import org.sparcs.soap.app.domain.helpers.AlertState
import org.sparcs.soap.app.domain.models.ara.AraPost
import org.sparcs.soap.app.domain.models.ara.AraPostAuthor
import org.sparcs.soap.app.domain.models.ara.AraPostComment
import org.sparcs.soap.app.domain.models.summarization.PostSummaryPhase
import org.sparcs.soap.app.domain.repositories.ara.AraUserRepositoryProtocol
import org.sparcs.soap.app.domain.services.AnalyticsServiceProtocol
import org.sparcs.soap.app.domain.usecases.ara.AraBoardUseCaseProtocol
import org.sparcs.soap.app.domain.usecases.ara.AraCommentUseCaseProtocol
import org.sparcs.soap.app.domain.usecases.summarization.ReviewSummaryError
import org.sparcs.soap.app.domain.usecases.summarization.ReviewSummaryException
import org.sparcs.soap.app.domain.usecases.translation.PostBatchTranslationResult
import org.sparcs.soap.app.features.post.event.PostCommentCellEvent
import org.sparcs.soap.app.features.post.event.PostViewEvent
import org.sparcs.soap.app.features.postCompose.ComposeDocument
import org.sparcs.soap.app.shared.extensions.toAlertState
import org.sparcs.soap.app.shared.viewModels.TextProcessingProtocol
import timber.log.Timber
import javax.inject.Inject

interface PostViewModelProtocol : TextProcessingProtocol {
    val postId: Int
    val post: StateFlow<AraPost?>
    val state: StateFlow<PostViewModel.ViewState>
    val contentBody: StateFlow<PostBody?>
    val postTranslationState: StateFlow<PostTranslationState>
    val translationTargets: StateFlow<List<String>>
    val summaryPhase: StateFlow<PostSummaryPhase?>
    val canSummarise: StateFlow<Boolean>
    val canEdit: StateFlow<Boolean>

    var alertState: AlertState?
    var isAlertPresented: Boolean

    fun translatePost(targetLanguage: String, allowDownload: Boolean = false)
    fun showPostOriginal()
    fun summarise()
    fun dismissSummary()
    fun translateComment(commentId: Int, content: String, targetLanguage: String, allowDownload: Boolean = false)
    fun showCommentOriginal(commentId: Int)

    fun fetchPost()
    fun upVote()
    fun downVote()
    fun report(type: AraContentReportType)
    suspend fun deletePost(): Boolean
    fun toggleBookmark()
    fun setAuthorBlocked(author: AraPostAuthor, blocked: Boolean)

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
    private val analyticsService: AnalyticsServiceProtocol,
    private val textProcessingDelegate: TextProcessingProtocol,
    private val araUserRepository: AraUserRepositoryProtocol,
) : ViewModel(), PostViewModelProtocol, TextProcessingProtocol by textProcessingDelegate {

    sealed interface ViewState {
        data object Loading : ViewState
        data object Loaded : ViewState
        data class Error(val error: Exception) : ViewState
    }

    internal var backgroundDispatcher: CoroutineDispatcher = Dispatchers.Default

    override val postId: Int = savedStateHandle.get<Int>("postId")
        ?: throw IllegalArgumentException("postId is missing")

    private val _state = MutableStateFlow<ViewState>(ViewState.Loading)
    override val state: StateFlow<ViewState> = _state.asStateFlow()

    private val _post = MutableStateFlow<AraPost?>(null)
    override val post: StateFlow<AraPost?> = _post.asStateFlow()

    private val _contentBody = MutableStateFlow<PostBody?>(null)
    override val contentBody: StateFlow<PostBody?> = _contentBody.asStateFlow()

    private val _postTranslationState = MutableStateFlow<PostTranslationState>(PostTranslationState.Original)
    override val postTranslationState: StateFlow<PostTranslationState> = _postTranslationState.asStateFlow()

    private val _translationTargets = MutableStateFlow<List<String>>(emptyList())
    override val translationTargets: StateFlow<List<String>> = _translationTargets.asStateFlow()

    private val _summaryPhase = MutableStateFlow<PostSummaryPhase?>(null)
    override val summaryPhase: StateFlow<PostSummaryPhase?> = _summaryPhase.asStateFlow()

    private val _canSummarise = MutableStateFlow(false)
    override val canSummarise: StateFlow<Boolean> = _canSummarise.asStateFlow()

    private val _canEdit = MutableStateFlow(false)
    override val canEdit: StateFlow<Boolean> = _canEdit.asStateFlow()

    override var alertState by mutableStateOf<AlertState?>(null)
    override var isAlertPresented by mutableStateOf(false)

    private var sourceLanguage: String? = null
    private var isSummarizationAvailable = false
    private var isToggling = false
    private var bodyJob: Job? = null
    private var translationJob: Job? = null
    private var summaryJob: Job? = null

    init {
        viewModelScope.launch {
            isSummarizationAvailable = runCatching { isSummarizationAvailable() }.getOrDefault(false)
            updateCanSummarise()
        }
    }

    override fun fetchPost() {
        val isFirstTime = _post.value == null
        val origin = if (isFirstTime) PostOrigin.All else PostOrigin.Board

        if (isFirstTime) _state.value = ViewState.Loading

        viewModelScope.launch {
            try {
                setPost(araBoardUseCase.fetchPost(origin = origin, postID = postId))
                if (isFirstTime) _state.value = ViewState.Loaded
            } catch (e: Exception) {
                if (isFirstTime) _state.value = ViewState.Error(e)
                alertState = e.toAlertState(R.string.unable_to_fetch_post)
                isAlertPresented = true
            }
        }
    }

    private fun setPost(newPost: AraPost) {
        val previous = _post.value
        _post.value = newPost
        val contentChanged = previous?.content != newPost.content || previous?.board?.slug != newPost.board?.slug
        if (contentChanged || previous?.title != newPost.title) resetTranslation()
        if (contentChanged) prepareContentBody(newPost)
    }

    private fun prepareContentBody(post: AraPost) {
        val content = post.content ?: return
        val boardSlug = post.board?.slug
        val hasBoard = post.board != null
        val title = post.title.orEmpty()
        bodyJob?.cancel()
        bodyJob = viewModelScope.launch {
            val (body, isEditable) = withContext(backgroundDispatcher) {
                val baseUrl = PostBody.baseUrl(boardSlug)
                PostBody.from(content, boardSlug) to (hasBoard && ComposeDocument.fromContent(content, baseUrl) != null)
            }
            _contentBody.value = body
            _canEdit.value = isEditable
            updateCanSummarise()
            loadTranslationTargets(listOf(title, body.plainText).joinToString(LINE_SEPARATOR))
        }
    }

    private suspend fun loadTranslationTargets(text: String) {
        sourceLanguage = runCatching { detectLanguage(text) }.getOrNull()
        _translationTargets.value = PostTranslator.targetLanguages(translationLanguages(), sourceLanguage)
    }

    private fun updateCanSummarise() {
        val length = _contentBody.value?.textLength ?: 0
        _canSummarise.value = isSummarizationAvailable && length >= MINIMUM_SUMMARY_LENGTH
    }

    override fun translatePost(targetLanguage: String, allowDownload: Boolean) {
        val current = _post.value ?: return
        val content = current.content ?: return
        val body = _contentBody.value ?: return
        val source = sourceLanguage
        val translating = PostTranslationState.Translating(targetLanguage)

        translationJob?.cancel()
        _postTranslationState.value = translating
        analyticsService.logEvent(PostViewEvent.TranslationRequested(targetLanguage))

        translationJob = viewModelScope.launch {
            val document = withContext(backgroundDispatcher) { PostTranslator.document(content, body) }
            val outcome = PostTranslator.translate(
                title = current.title,
                document = document,
                baseUrl = PostBody.baseUrl(current.board?.slug),
                targetLanguage = targetLanguage,
            ) { texts -> translateTexts(texts, source, targetLanguage, allowDownload) }

            if (_postTranslationState.value != translating) return@launch
            _postTranslationState.value = when (outcome) {
                is PostBatchTranslationOutcome.Translated -> PostTranslationState.Translated(outcome.translation)
                is PostBatchTranslationOutcome.Incomplete -> incompleteTranslationState(outcome.result, targetLanguage)
            }
        }
    }

    private fun incompleteTranslationState(result: PostBatchTranslationResult, targetLanguage: String): PostTranslationState {
        if (result is PostBatchTranslationResult.NeedsDownload) {
            return PostTranslationState.DownloadRequired(result.sourceLanguage, targetLanguage)
        }
        alertState = AlertState(
            titleResId = R.string.post_translation_failed_title,
            messageResId = if (result is PostBatchTranslationResult.Unsupported) R.string.translation_unsupported else R.string.translation_failed,
        )
        isAlertPresented = true
        return PostTranslationState.Original
    }

    override fun showPostOriginal() = resetTranslation()

    private fun resetTranslation() {
        translationJob?.cancel()
        translationJob = null
        _postTranslationState.value = PostTranslationState.Original
    }

    override fun summarise() {
        if (!_canSummarise.value || _summaryPhase.value?.isShown == true) return
        val text = _contentBody.value?.plainText ?: return

        _summaryPhase.value = PostSummaryPhase.Summarising("")
        analyticsService.logEvent(PostViewEvent.SummaryRequested)

        summaryJob = viewModelScope.launch {
            var summary = ""
            try {
                streamPostSummary(text).collect { partial ->
                    summary = partial
                    _summaryPhase.value = PostSummaryPhase.Summarising(partial)
                }
                val finished = summary.trim()
                if (finished.isEmpty()) throw ReviewSummaryException(ReviewSummaryError.FAILED)
                _summaryPhase.value = PostSummaryPhase.Finished(finished)
                analyticsService.logEvent(PostViewEvent.SummaryFinished)
            } catch (cancelled: CancellationException) {
                throw cancelled
            } catch (error: Exception) {
                val reason = (error as? ReviewSummaryException)?.reason ?: ReviewSummaryError.FAILED
                _summaryPhase.value = PostSummaryPhase.Failed(reason)
                analyticsService.logEvent(PostViewEvent.SummaryFailed(reason.name))
            }
        }
    }

    override fun dismissSummary() {
        summaryJob?.cancel()
        summaryJob = null
        _summaryPhase.value = null
    }

    override fun translateComment(commentId: Int, content: String, targetLanguage: String, allowDownload: Boolean) {
        translateComment(commentId.toString(), content, targetLanguage, allowDownload, viewModelScope)
    }

    override fun showCommentOriginal(commentId: Int) {
        showCommentOriginal(commentId.toString())
    }

    override fun setAuthorBlocked(author: AraPostAuthor, blocked: Boolean) {
        val userId = author.blockableUserId ?: return
        viewModelScope.launch {
            try {
                if (blocked) araUserRepository.blockUser(userId) else araUserRepository.unblockUser(userId)
                analyticsService.logEvent(PostViewEvent.AuthorBlockToggled(blocked))
                fetchPost()
            } catch (cancelled: CancellationException) {
                throw cancelled
            } catch (e: Exception) {
                alertState = blockAlert(e, blocked)
                isAlertPresented = true
            }
        }
    }

    private fun blockAlert(error: Exception, blocked: Boolean): AlertState {
        if (blocked && error is NetworkError.ServerError && error.code == HTTP_FORBIDDEN) {
            return AlertState(messageResId = R.string.block_daily_limit)
        }
        return error.toAlertState(if (blocked) R.string.error_block_user else R.string.error_unblock_user)
    }

    override fun upVote() {
        val currentPost = _post.value ?: return
        if (currentPost.isMine == true) {
            presentVoteWarning(R.string.post_vote_warning)
            return
        }
        val previousMyVote = currentPost.myVote
        val previousUpVotes = currentPost.upVotes
        val previousDownVotes = currentPost.downVotes

        viewModelScope.launch {
            try {
                if (previousMyVote == true) {
                    _post.value = currentPost.copy(myVote = null, upVotes = previousUpVotes - 1)
                    araBoardUseCase.cancelVote(currentPost.id)
                } else {
                    val newDownVotes = if (previousMyVote == false) previousDownVotes - 1 else previousDownVotes
                    _post.value = currentPost.copy(myVote = true, upVotes = previousUpVotes + 1, downVotes = newDownVotes)
                    araBoardUseCase.upVotePost(currentPost.id)
                }
                analyticsService.logEvent(PostViewEvent.PostUpVoted)
            } catch (_: Exception) {
                _post.value = currentPost.copy(myVote = previousMyVote, upVotes = previousUpVotes, downVotes = previousDownVotes)
            }
        }
    }

    override fun downVote() {
        val currentPost = _post.value ?: return
        if (currentPost.isMine == true) {
            presentVoteWarning(R.string.post_vote_warning)
            return
        }
        val previousMyVote = currentPost.myVote
        val previousUpVotes = currentPost.upVotes
        val previousDownVotes = currentPost.downVotes

        viewModelScope.launch {
            try {
                if (previousMyVote == false) {
                    _post.value = currentPost.copy(myVote = null, downVotes = previousDownVotes - 1)
                    araBoardUseCase.cancelVote(currentPost.id)
                } else {
                    val newUpVotes = if (previousMyVote == true) previousUpVotes - 1 else previousUpVotes
                    _post.value = currentPost.copy(myVote = false, downVotes = previousDownVotes + 1, upVotes = newUpVotes)
                    araBoardUseCase.downVotePost(currentPost.id)
                }
                analyticsService.logEvent(PostViewEvent.PostDownVoted)
            } catch (_: Exception) {
                _post.value = currentPost.copy(myVote = previousMyVote, upVotes = previousUpVotes, downVotes = previousDownVotes)
            }
        }
    }

    private fun presentVoteWarning(messageResId: Int) {
        alertState = AlertState(titleResId = R.string.warning, messageResId = messageResId)
        isAlertPresented = true
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
        return try {
            val editedComment = araCommentUseCase.editComment(commentID = commentID, content = content)
            editedComment.isMine = true

            val updatedComments = updateCommentInList(current.comments, commentID) { target ->
                target.copy(content = content)
            }
            _post.value = current.copy(comments = updatedComments.toMutableList())
            editedComment
        } catch (e: Exception) {
            alertState = e.toAlertState(R.string.unexpected_error_editing_comment)
            isAlertPresented = true
            null
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

    override fun upVoteComment(comment: AraPostComment) {
        val currentPost = _post.value ?: return
        if (comment.isMine == true) {
            presentVoteWarning(R.string.comment_vote_warning)
            return
        }
        val previousMyVote = comment.myVote
        val previousUpVotes = comment.upVotes
        val previousDownVotes = comment.downVotes

        val updatedComments = updateCommentInList(currentPost.comments, comment.id) { target ->
            if (previousMyVote == true) {
                target.copy(myVote = null, upVotes = previousUpVotes - 1)
            } else {
                val newDownVotes = if (previousMyVote == false) previousDownVotes - 1 else previousDownVotes
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
        if (comment.isMine == true) {
            presentVoteWarning(R.string.comment_vote_warning)
            return
        }
        val previousMyVote = comment.myVote
        val previousUpVotes = comment.upVotes
        val previousDownVotes = comment.downVotes

        val updatedComments = updateCommentInList(currentPost.comments, comment.id) { target ->
            if (previousMyVote == false) {
                target.copy(myVote = null, downVotes = previousDownVotes - 1)
            } else {
                val newUpVotes = if (previousMyVote == true) previousUpVotes - 1 else previousUpVotes
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
    ): List<AraPostComment> = comments.map { parent ->
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

    private fun insertThreadedComment(
        comments: MutableList<AraPostComment>,
        comment: AraPostComment,
    ): Boolean {
        val parentComment = comment.parentComment ?: return false
        val parent = comments.firstOrNull { it.id == parentComment } ?: return false
        parent.comments.add(comment)
        return true
    }

    companion object {
        const val MINIMUM_SUMMARY_LENGTH = 200
        private const val HTTP_FORBIDDEN = 403
        private const val LINE_SEPARATOR = "\n"
    }
}
