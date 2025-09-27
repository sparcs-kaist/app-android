package com.example.soap.Features.FeedPost

import android.graphics.Bitmap
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.soap.Domain.Models.Feed.FeedComment
import com.example.soap.Domain.Models.Feed.FeedCreateComment
import com.example.soap.Domain.Repositories.Feed.FeedCommentRepositoryProtocol
import com.example.soap.Domain.Usecases.UserUseCaseProtocol
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class FeedPostViewModel @Inject constructor(
    val feedCommentRepository: FeedCommentRepositoryProtocol,
    val userUseCase: UserUseCaseProtocol
) : ViewModel(), FeedPostViewModelProtocol {

    sealed interface ViewState {
        data object Loading : ViewState
        data object Loaded : ViewState
        data class Error(val message: String) : ViewState
    }

    init {
        viewModelScope.launch { userUseCase.fetchFeedUser() }
    }

    // MARK: - Properties
    private val _state = MutableStateFlow<ViewState>(ViewState.Loading)
    override val state: StateFlow<ViewState> = _state.asStateFlow()

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
    override suspend fun fetchComments(postId: String) {
        _state.value = ViewState.Loading
        try {
            val fetchedComments = feedCommentRepository.fetchComments(postId)
            _comments.value = fetchedComments
            _state.value = ViewState.Loaded
        } catch (e: Exception) {
            _state.value = ViewState.Error(e.localizedMessage ?: "Unknown error")
        }
    }

    override suspend fun writeComment(postId: String): FeedComment {
        val request = FeedCreateComment(
            content = text,
            isAnonymous = isAnonymous,
            image = null
        )
        val comment = feedCommentRepository.writeComment(postId, request)
        _comments.value += comment
        return comment
    }

    private fun insertReply(commentsList: List<FeedComment>, comment: FeedComment): List<FeedComment> {
        val parentId = comment.parentCommentID ?: return commentsList
        return commentsList.map { c ->
            if (c.id == parentId) {
                c.copy(replies = c.replies + comment)
            } else {
                c
            }
        }
    }

    override suspend fun writeReply(commentId: String): FeedComment {
        val request = FeedCreateComment(
            content = text,
            isAnonymous = isAnonymous,
            image = null
        )
        val comment = feedCommentRepository.writeReply(commentId, request)
        _comments.value = insertReply(_comments.value, comment)
        return comment
    }
}
