package com.example.soap.Features.Feed

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.soap.Domain.Models.Feed.FeedPost
import com.example.soap.Domain.Repositories.Feed.FeedPostRepositoryProtocol
import com.example.soap.Domain.Usecases.AuthUseCaseProtocol
import com.example.soap.Features.TaxiList.TaxiListViewModel.ViewState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class FeedViewModel @Inject constructor(
    private val authUseCase: AuthUseCaseProtocol,
    val feedPostRepository: FeedPostRepositoryProtocol,
) : ViewModel(), FeedViewModelProtocol {

    sealed interface ViewState {
        data object Loading : ViewState
        data object Loaded : ViewState
        data class Error(val message: String) : ViewState
    }

    private val _state = MutableStateFlow<ViewState>(ViewState.Loading)
    override val state: StateFlow<ViewState> = _state.asStateFlow()

    private val _posts = MutableStateFlow<List<FeedPost>>(emptyList())
    override val posts: StateFlow<List<FeedPost>> = _posts.asStateFlow()

    private var nextCursor: String? = null
    private var hasNext: Boolean = false

    override suspend fun signOut() {
        viewModelScope.launch {
            try {
                authUseCase.signOut()
            } catch (e: Exception) {
                Log.e("FeedViewModel", "failed to signOut")
            }
        }
    }

    override suspend fun fetchInitialData() {
        _state.value = ViewState.Loading
        try {
            val page = feedPostRepository.fetchPosts(cursor = null, page = 20)
            _posts.value = page.items
            nextCursor = page.nextCursor
            hasNext = page.hasNext
            _state.value = ViewState.Loaded
        } catch (e: Exception) {
            Log.e("FeedViewModel", "failed to fetch initial data", e)
            _state.value = ViewState.Error(e.localizedMessage ?: "Unknown error")
        }
    }

    override suspend fun deletePost(postID: String) {
        try {
            feedPostRepository.deletePost(postID)
            _posts.value = _posts.value.filterNot { it.id == postID }
        } catch (e: Exception) {
            Log.e("FeedViewModel", "failed to delete post", e)
        }
    }
}