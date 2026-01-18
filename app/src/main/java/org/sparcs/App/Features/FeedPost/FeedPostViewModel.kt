package org.sparcs.App.Features.FeedPost

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
import org.sparcs.App.Domain.Helpers.CrashlyticsHelper
import org.sparcs.App.Domain.Models.Feed.FeedComment
import org.sparcs.App.Domain.Models.Feed.FeedCreateComment
import org.sparcs.App.Domain.Models.Feed.FeedPost
import org.sparcs.App.Domain.Repositories.Feed.FeedCommentRepositoryProtocol
import org.sparcs.App.Domain.Repositories.Feed.FeedPostRepositoryProtocol
import org.sparcs.App.Domain.Usecases.UserUseCaseProtocol
import javax.inject.Inject

interface FeedPostViewModelProtocol {
    val state: StateFlow<FeedPostViewModel.ViewState>
    val post: FeedPost?
    var comments: List<FeedComment>
    var text: String
    var image: Bitmap?
    var isAnonymous: Boolean

    suspend fun fetchComments(postID: String, initial: Boolean)
    suspend fun writeComment(postID: String): FeedComment
    suspend fun writeReply(commentID: String): FeedComment
}

@HiltViewModel
class FeedPostViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    val feedCommentRepository: FeedCommentRepositoryProtocol,
    val userUseCase: UserUseCaseProtocol,
    private val feedPostRepository: FeedPostRepositoryProtocol,
    private val crashlyticsHelper: CrashlyticsHelper
) : ViewModel(), FeedPostViewModelProtocol {

    sealed interface ViewState {
        data object Loading : ViewState
        data class Loaded(
            val post: FeedPost,
            val comments: List<FeedComment>
        ) : ViewState
        data class Error(val message: String) : ViewState
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
            _state.value = ViewState.Loading
            try {
                val data = feedPostRepository.fetchPost(feedId)
                _state.value = ViewState.Loaded(data, emptyList())
            } catch (e: Exception) {
                _state.value = ViewState.Error(e.localizedMessage ?: "Unknown error")
            }
        }
    }

    private val _comments = MutableStateFlow<List<FeedComment>>(emptyList())
    override var comments: List<FeedComment>
        get() = _comments.value
        set(value) {
            _comments.value = value
        }

    override var text by mutableStateOf("")
    override var image by mutableStateOf<Bitmap?>(null)
    override var isAnonymous by mutableStateOf(true)

    // MARK: - Functions
    override suspend fun fetchComments(postID: String, initial: Boolean) {
        if (_state.value is ViewState.Loading && !initial) return
        val currentPost = post ?: return
        try {
            val fetchedComments = feedCommentRepository.fetchComments(postID)
            _comments.value = fetchedComments
            _state.value = ViewState.Loaded(currentPost, fetchedComments)
        } catch (e: Exception) {
            _state.value = ViewState.Error(e.localizedMessage ?: "Unknown error")
        }
    }

    override suspend fun writeComment(postID: String): FeedComment {
        val request = FeedCreateComment(
            content = text,
            isAnonymous = isAnonymous,
            image = null
        )
        val comment = feedCommentRepository.writeComment(postID, request)
        _comments.value += comment
        return comment
    }

    private fun insertReply(
        commentsList: List<FeedComment>,
        comment: FeedComment,
    ): List<FeedComment> {
        val parentId = comment.parentCommentID ?: return commentsList
        return commentsList.map { c ->
            if (c.id == parentId) {
                c.copy(replies = c.replies + comment)
            } else {
                c
            }
        }
    }

    override suspend fun writeReply(commentID: String): FeedComment {
        val request = FeedCreateComment(
            content = text,
            isAnonymous = isAnonymous,
            image = null
        )
        val comment = feedCommentRepository.writeReply(commentID, request)
        _comments.value = insertReply(_comments.value, comment)
        return comment
    }
}
