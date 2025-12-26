package org.sparcs.Features.Post

import android.util.Log
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import org.sparcs.Domain.Enums.Ara.AraContentReportType
import org.sparcs.Domain.Enums.Ara.PostOrigin
import org.sparcs.Domain.Helpers.CrashlyticsHelper
import org.sparcs.Domain.Models.Ara.AraPost
import org.sparcs.Domain.Models.Ara.AraPostComment
import org.sparcs.Domain.Repositories.Ara.AraBoardRepositoryProtocol
import org.sparcs.Domain.Repositories.Ara.AraCommentRepositoryProtocol
import javax.inject.Inject

interface PostViewModelProtocol {
    val state: StateFlow<PostViewModel.ViewState>
    val post: StateFlow<AraPost?>
    val isFoundationModelsAvailable: Boolean

    suspend fun fetchPost()
    suspend fun upVote()
    suspend fun downVote()
    suspend fun writeComment(content: String): AraPostComment
    suspend fun writeThreadedComment(commentID: Int, content: String): AraPostComment
    suspend fun editComment(commentID: Int, content: String): AraPostComment
    suspend fun report(type: AraContentReportType)
    suspend fun summarisedContent(): String
    suspend fun deletePost()
    suspend fun toggleBookmark()
    fun handleException(error: Throwable)
}

@HiltViewModel
class PostViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val araBoardRepository: AraBoardRepositoryProtocol,
    val araCommentRepository: AraCommentRepositoryProtocol,
//    private val foundationModelsUseCase: FoundationModelsUseCaseProtocol
    private val crashlyticsHelper: CrashlyticsHelper
) : ViewModel(), PostViewModelProtocol {

    sealed interface ViewState {
        data object Loading : ViewState
        data object Loaded : ViewState
        data class Error(val message: String) : ViewState
    }

    // MARK: - Properties
    private val _state = MutableStateFlow<ViewState>(ViewState.Loading)
    override val state: StateFlow<ViewState> = _state.asStateFlow()

    private val postId: Int = savedStateHandle.get<Int>("postId")
        ?: throw IllegalArgumentException("postId is missing")

    private val _post = MutableStateFlow<AraPost?>(null)
    override val post: StateFlow<AraPost?> = _post.asStateFlow()

    override val isFoundationModelsAvailable: Boolean
        get() = false // TODO: foundationModelsUseCase.isAvailable

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
    override suspend fun fetchPost() {
        val isFirstTime = _post.value == null // Case: Deep link entry (PostOrigin.All)
        val origin = if (isFirstTime) PostOrigin.All else PostOrigin.Board

        if (isFirstTime) _state.value = ViewState.Loading

        try {
            val fetchedPost = araBoardRepository.fetchPost(origin = origin, postID = postId)
            _post.value = fetchedPost
            if (isFirstTime) _state.value = ViewState.Loaded
        } catch (e: Exception) {
            if (isFirstTime) _state.value = ViewState.Error(e.localizedMessage ?: "Error")
            Log.e("PostViewModel", "Error fetching post", e)
        }
    }

    override suspend fun upVote() {
        val current = _post.value ?: return
        val previousMyVote = current.myVote
        val previousUpVotes = current.upVotes
        val previousDownVotes = current.downVotes

        val newPost = when (previousMyVote) {
            // cancel upvote
            true -> current.copy(myVote = null, upVotes = current.upVotes - 1)
            // upvote
            false -> current.copy(
                myVote = true,
                upVotes = current.upVotes + 1,
                // remove downvote if there was
                downVotes = current.downVotes - 1
            )

            null -> current.copy(myVote = true, upVotes = current.upVotes + 1)
        }
        _post.value = newPost

        try {
            when (previousMyVote) {
                true -> araBoardRepository.cancelVote(current.id)
                else -> araBoardRepository.upVotePost(current.id)
            }
        } catch (e: Exception) {
            Log.e("PostViewModel", "upvote error", e)
            _post.value = current.copy(
                myVote = previousMyVote,
                upVotes = previousUpVotes,
                downVotes = previousDownVotes
            )
        }
    }

    override suspend fun downVote() {
        val current = _post.value ?: return
        val previousMyVote = current.myVote
        val previousUpVotes = current.upVotes
        val previousDownVotes = current.downVotes

        val newPost = when (previousMyVote) {
            // cancel downvote
            false -> current.copy(myVote = null, downVotes = current.downVotes - 1)
            // downvote
            true -> current.copy(
                myVote = false,
                // remove upvote if there was
                upVotes = current.upVotes - 1,
                downVotes = current.downVotes + 1
            )

            null -> current.copy(myVote = false, downVotes = current.downVotes + 1)
        }
        _post.value = newPost

        try {
            when (previousMyVote) {
                false -> araBoardRepository.cancelVote(current.id)
                else -> araBoardRepository.downVotePost(current.id)
            }
        } catch (e: Exception) {
            Log.e("PostViewModel", "downvote error", e)
            _post.value = current.copy(
                myVote = previousMyVote,
                upVotes = previousUpVotes,
                downVotes = previousDownVotes
            )
        }
    }

    override suspend fun writeComment(content: String): AraPostComment {
        val current = _post.value ?: throw IllegalStateException("Post not loaded")
        val comment = araCommentRepository.writeComment(postID = current.id, content = content)
        comment.isMine = true
        current.comments.add(comment)
        current.commentCount += 1
        return comment
    }

    override suspend fun writeThreadedComment(commentID: Int, content: String): AraPostComment {
        val current = _post.value ?: throw IllegalStateException("Post not loaded")
        val comment =
            araCommentRepository.writeThreadedComment(commentID = commentID, content = content)
        comment.isMine = true

        // insert threaded comments
        val comments = current.comments.toMutableList()
        insertThreadedComment(comments, comment)
        current.comments = comments
        current.commentCount += 1

        return comment
    }

    override suspend fun editComment(commentID: Int, content: String): AraPostComment {
        val current = _post.value ?: throw IllegalStateException("Post not loaded")
        val comment = araCommentRepository.editComment(commentID = commentID, content = content)
        comment.isMine = true

        for (i in current.comments.indices) {
            if (current.comments[i].id == commentID) {
                current.comments[i].content = content
                return current.comments[i]
            }

            // scan through threads
            for (j in current.comments[i].comments.indices) {
                if (current.comments[i].comments[j].id == commentID) {
                    current.comments[i].comments[j].content = content
                    return current.comments[i].comments[j]
                }
            }
        }

        return comment
    }

    override suspend fun report(type: AraContentReportType) {
        val current = _post.value ?: return
        araBoardRepository.reportPost(postID = current.id, type = type)
    }

    override suspend fun summarisedContent(): String {
//        return foundationModelsUseCase.summarise(post.content ?: "", maxWords = 50, tone = "concise")
        return ""
    }

    override suspend fun deletePost() {
        val current = _post.value ?: return
        araBoardRepository.deletePost(postID = current.id)
    }

    override suspend fun toggleBookmark() {
        var current = _post.value ?: return
        val previous = current.myScrap

        current = current.copy(myScrap = !previous)

        try {
            if (previous) {
                val scrapId = current.scrapID ?: return
                araBoardRepository.removeBookmark(scrapId)
                current = current.copy(scrapID = null)
                _post.value = current
            } else {
                araBoardRepository.addBookmark(current.id)
            }
        } catch (e: Exception) {
            Log.e("PostViewModel", "toggleBookmark error", e)
            _post.value = current
        }
    }
    override fun handleException(error: Throwable) {
        Log.e("FeedPostViewModel","failed to create a report: $error")
        crashlyticsHelper.recordException(
            error = error,
            alertMessage = "An unexpected error occurred while reporting a user. Please try again later."
        )
    }
}
