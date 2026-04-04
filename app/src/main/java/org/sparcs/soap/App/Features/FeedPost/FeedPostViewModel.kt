package org.sparcs.soap.App.Features.FeedPost

import android.graphics.Bitmap
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
import org.sparcs.soap.App.Domain.Enums.Feed.FeedReportType
import org.sparcs.soap.App.Domain.Enums.Feed.FeedVoteType
import org.sparcs.soap.App.Domain.Error.Feed.FeedCommentUseCaseError
import org.sparcs.soap.App.Domain.Helpers.AlertState
import org.sparcs.soap.App.Domain.Models.Feed.FeedComment
import org.sparcs.soap.App.Domain.Models.Feed.FeedCreateComment
import org.sparcs.soap.App.Domain.Models.Feed.FeedPost
import org.sparcs.soap.App.Domain.Models.Feed.FeedUser
import org.sparcs.soap.App.Domain.Services.AnalyticsServiceProtocol
import org.sparcs.soap.App.Domain.Services.CrashlyticsServiceProtocol
import org.sparcs.soap.App.Domain.Usecases.Feed.FeedCommentUseCaseProtocol
import org.sparcs.soap.App.Domain.Usecases.Feed.FeedPostUseCaseProtocol
import org.sparcs.soap.App.Domain.Usecases.UserUseCaseProtocol
import org.sparcs.soap.App.Features.Feed.Event.FeedPostRowEvent
import org.sparcs.soap.App.Features.FeedPost.Event.FeedPostViewEvent
import org.sparcs.soap.App.Shared.Extensions.toAlertState
import org.sparcs.soap.R
import javax.inject.Inject

interface FeedPostViewModelProtocol {
    val state: StateFlow<FeedPostViewModel.ViewState>
    val post: FeedPost?
    var comments: List<FeedComment>
    var text: String
    var image: Bitmap?
    var isAnonymous: Boolean
    var isSubmittingComment: Boolean
    val feedUser: FeedUser?

    var alertState: AlertState?
    var isAlertPresented: Boolean

    fun fetchComments(postID: String, initial: Boolean)
    suspend fun submitComment(postID: String, replyingTo: FeedComment?): FeedComment?
    fun reportPost(postID: String, reason: FeedReportType) {}
    fun fetchFeedUser()

    fun voteComment(comment: FeedComment, type: FeedVoteType?)
    fun deleteComment(comment: FeedComment)
    fun reportComment(commentID: String, reason: FeedReportType)
}

@HiltViewModel
class FeedPostViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val feedCommentUseCase: FeedCommentUseCaseProtocol,
    private val feedPostUseCase: FeedPostUseCaseProtocol,
    private val userUseCase: UserUseCaseProtocol,
    private val crashlyticsService: CrashlyticsServiceProtocol,
    private val analyticsService: AnalyticsServiceProtocol,
) : ViewModel(), FeedPostViewModelProtocol {

    sealed interface ViewState {
        data object Loading : ViewState
        data class Loaded(val post: FeedPost) : ViewState
        data class Error(val error: Exception) : ViewState
    }

    // MARK: - Properties
    private val _state = MutableStateFlow<ViewState>(ViewState.Loading)
    override val state: StateFlow<ViewState> = _state.asStateFlow()

    private val feedId: String =
        savedStateHandle["feedId"] ?: throw IllegalArgumentException("feedId is missing")

    override val post: FeedPost?
        get() = (state.value as? ViewState.Loaded)?.post

    // MARK: - Initializer
    init {
        viewModelScope.launch {
            loadInitialPage()
        }
    }

    override var comments by mutableStateOf<List<FeedComment>>(emptyList())

    override var text by mutableStateOf("")
    override var image by mutableStateOf<Bitmap?>(null)

    override var isAnonymous: Boolean by mutableStateOf(true)
    override var isSubmittingComment: Boolean by mutableStateOf(false)
    override var feedUser: FeedUser? by mutableStateOf(null)

    override var alertState: AlertState? by mutableStateOf(null)
    override var isAlertPresented: Boolean by mutableStateOf(false)

    // MARK: - Functions
    private suspend fun loadInitialPage() {
        _state.value = ViewState.Loading
        try {
            val postData = feedPostUseCase.fetchPost(feedId)
            _state.value = ViewState.Loaded(postData)

            fetchComments(feedId, initial = true)
        } catch (e: Exception) {
            _state.value = ViewState.Error(e)
        }
    }

    override fun fetchComments(postID: String, initial: Boolean) {
        if (_state.value is ViewState.Loading && !initial) return
        viewModelScope.launch {
            try {
                val fetchedComments = feedCommentUseCase.fetchComments(postID)
                comments = fetchedComments
                if (!initial) {
                    analyticsService.logEvent(FeedPostViewEvent.CommentsRefreshed)
                }
            } catch (e: Exception) {
                _state.value = ViewState.Error(e)
                crashlyticsService.recordException(e)
            }
        }
    }

    override suspend fun submitComment(postID: String, replyingTo: FeedComment?): FeedComment? {
        isSubmittingComment = true
        return try {
            val request = FeedCreateComment(
                content = text,
                isAnonymous = isAnonymous,
                image = null
            )

            val uploaded: FeedComment = if (replyingTo != null) {
                feedCommentUseCase.writeReply(replyingTo.id, request)
            } else {
                feedCommentUseCase.writeComment(postID, request)
            }

            if (replyingTo != null) {
                insertReplyLocally(uploaded)
            } else {
                this.comments += uploaded
            }
            analyticsService.logEvent(
                FeedPostViewEvent.CommentSubmitted(
                    isReply = (replyingTo != null),
                    isAnonymous = isAnonymous
                )
            )
            text = ""
            uploaded
        } catch (e: Exception) {
            alertState = e.toAlertState(R.string.unexpected_error_uploading_comment)
            isAlertPresented = true
            crashlyticsService.recordException(e)
            null
        } finally {
            isSubmittingComment = false
        }
    }

    private fun insertReplyLocally(reply: FeedComment) {
        val parentID = reply.parentCommentID ?: return
        this.comments = this.comments.map { comment ->
            if (comment.id == parentID) {
                comment.copy(replies = comment.replies + reply)
            } else {
                comment
            }
        }
    }

    override fun reportPost(postID: String, reason: FeedReportType) {
        viewModelScope.launch {
            try {
                feedPostUseCase.reportPost(postID = postID, reason = reason, detail = "")
                alertState = AlertState(
                    titleResId = R.string.report_submitted_title,
                    messageResId = R.string.report_submitted_message
                )
                isAlertPresented = true
                analyticsService.logEvent(FeedPostRowEvent.PostReported(reason.name))
            } catch (e: Exception) {
                alertState = e.toAlertState(R.string.error_unable_to_submit_report)
                isAlertPresented = true
                crashlyticsService.recordException(e)
            }
        }
    }

    override fun fetchFeedUser() {
        viewModelScope.launch {
            feedUser = userUseCase.feedUser
        }
    }

    override fun voteComment(comment: FeedComment, type: FeedVoteType?) {
            val prevComments = this@FeedPostViewModel.comments

            updateCommentLocally(comment.id) { old ->
                var newUp = old.upVotes
                var newDown = old.downVotes

                if (old.myVote == FeedVoteType.UP) newUp--
                if (old.myVote == FeedVoteType.DOWN) newDown--

                if (type == FeedVoteType.UP) newUp++
                if (type == FeedVoteType.DOWN) newDown++

                old.copy(myVote = type, upVotes = newUp, downVotes = newDown)
            }

        viewModelScope.launch {
            try {
                if (type == null) {
                    feedCommentUseCase.deleteVote(comment.id)
                } else {
                    feedCommentUseCase.vote(comment.id, type)
                }
            } catch (e: Exception) {
                this@FeedPostViewModel.comments = prevComments
                alertState = e.toAlertState(R.string.error_failed_to_upvote)
                isAlertPresented = true
            }
        }
    }

    override fun deleteComment(comment: FeedComment) {
        updateCommentLocally(comment.id) { it.copy(isDeleted = true) }
        viewModelScope.launch {
            try {
                feedCommentUseCase.deleteComment(comment.id)
            } catch (e: Exception) {
                updateCommentLocally(comment.id) { it.copy(isDeleted = false) }
                val useCaseError = e as? FeedCommentUseCaseError
                alertState = e.toAlertState(
                    useCaseError?.messageRes
                        ?: R.string.unexpected_error_deleting_comment
                )
                isAlertPresented = true
            }
        }
    }

    override fun reportComment(commentID: String, reason: FeedReportType) {
        viewModelScope.launch {
            try {
                feedCommentUseCase.reportComment(
                    commentID = commentID,
                    reason = reason,
                    detail = ""
                )
                alertState = AlertState(
                    titleResId = R.string.report_submitted_title,
                    messageResId = R.string.report_submitted_message
                )
                isAlertPresented = true
            } catch (e: Exception) {
                alertState = e.toAlertState(R.string.error_unable_to_submit_report)
                isAlertPresented = true
            }
        }
    }

    private fun updateCommentLocally(commentID: String, transform: (FeedComment) -> FeedComment) {
        this.comments = this.comments.map { comment ->
            if (comment.id == commentID) {
                transform(comment)
            } else if (comment.replies.any { it.id == commentID }) {
                comment.copy(
                    replies = comment.replies.map { reply ->
                        if (reply.id == commentID) transform(reply) else reply
                    }
                )
            } else {
                comment
            }
        }
    }
}
