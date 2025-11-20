package com.sparcs.soap.Features.FeedPost

import android.graphics.Bitmap
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sparcs.soap.Domain.Models.Feed.FeedComment
import com.sparcs.soap.Domain.Models.Feed.FeedCreateComment
import com.sparcs.soap.Domain.Models.Feed.FeedPost
import com.sparcs.soap.Domain.Repositories.Feed.FeedCommentRepositoryProtocol
import com.sparcs.soap.Domain.Repositories.Feed.FeedPostRepositoryProtocol
import com.sparcs.soap.Domain.Usecases.UserUseCaseProtocol
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

interface FeedPostViewModelProtocol {
    val state: StateFlow<FeedPostViewModel.ViewState>
    val post: StateFlow<FeedPost?>
    var comments: List<FeedComment>
    var text: String
    var image: Bitmap?
    var isAnonymous: Boolean

    suspend fun fetchComments(postID: String)
    suspend fun writeComment(postID: String): FeedComment
    suspend fun writeReply(commentID: String): FeedComment
}

@HiltViewModel
class FeedPostViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    val feedCommentRepository: FeedCommentRepositoryProtocol,
    val userUseCase: UserUseCaseProtocol,
    private val feedPostRepository: FeedPostRepositoryProtocol,
) : ViewModel(), FeedPostViewModelProtocol {

    sealed interface ViewState {
        data object Loading : ViewState
        data object Loaded : ViewState
        data class Error(val message: String) : ViewState
    }

    // MARK: - Properties
    private val _state = MutableStateFlow<ViewState>(ViewState.Loading)
    override val state: StateFlow<ViewState> = _state.asStateFlow()

    private val feedId: String =
        savedStateHandle["feedId"] ?: throw IllegalArgumentException("feedId is missing")

    private val _post = MutableStateFlow<FeedPost?>(null)
    override val post: StateFlow<FeedPost?> = _post.asStateFlow()

    // MARK: - Initializer
    init {
        viewModelScope.launch {
            userUseCase.fetchFeedUser()
        }

        viewModelScope.launch {
            _state.value = ViewState.Loading
            try {
                val data = feedPostRepository.fetchPost(feedId)
                _post.value = data
                _state.value = ViewState.Loaded
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
    override suspend fun fetchComments(postID: String) {
        _state.value = ViewState.Loading
        try {
            val fetchedComments = feedCommentRepository.fetchComments(postID)
            _comments.value = fetchedComments
            _state.value = ViewState.Loaded
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
