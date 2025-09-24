package com.example.soap.Features.UserPostList

import android.net.Uri
import android.util.Log
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.soap.Domain.Models.Ara.AraPost
import com.example.soap.Domain.Models.Ara.AraPostAuthor
import com.example.soap.Domain.Repositories.Ara.AraBoardRepositoryProtocol
import com.example.soap.Networking.RetrofitAPI.Ara.AraBoardTarget
import com.google.gson.Gson
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class UserPostListViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val araBoardRepository: AraBoardRepositoryProtocol
) : ViewModel(), UserPostListViewModelProtocol {

    //Mark - ViewState
    sealed class ViewState {
        data object Loading : ViewState()
        data class Loaded(val posts: List<AraPost>) : ViewState()
        data class Error(val message: String) : ViewState()
    }

    private val initialAuthor: AraPostAuthor by lazy {
        val json = savedStateHandle.get<String>("author_json")
            ?: throw IllegalStateException("author_json is null. UserPostListViewModel requires a author_json to initialize.")
        Gson().fromJson(Uri.decode(json), AraPostAuthor::class.java)
    }

    override var user: AraPostAuthor = initialAuthor

    //Mark - State
    private val _state = MutableStateFlow<ViewState>(ViewState.Loading)
    override val state: StateFlow<ViewState> = _state.asStateFlow()

    private val _posts = MutableStateFlow<List<AraPost>>(emptyList())
    override var posts: StateFlow<List<AraPost>> = _posts.asStateFlow()

    //Mark - Search
    private val _searchKeyword = MutableStateFlow("")
    override var searchKeyword: StateFlow<String> = _searchKeyword.asStateFlow()

    //Mark - Paging
    private val _isLoadingMore = MutableStateFlow(false)
    override val isLoadingMore: StateFlow<Boolean> = _isLoadingMore.asStateFlow()

    override var hasMorePages = true
    private var currentPage = 1
    private var totalPages = 0
    private val pageSize = 30

    //Mark - Search Properties
    fun setSearchKeyword(value: String) {
        _searchKeyword.value = value
    }

    override suspend fun fetchInitialPosts() {
        val userID = user.id.toIntOrNull() ?: return
        viewModelScope.launch {
            _state.value = ViewState.Loading
            try {
                val page = araBoardRepository.fetchPosts(
                    type = AraBoardTarget.PostListType.User(userID),
                    page = 1,
                    pageSize = pageSize,
                    searchKeyword = if (_searchKeyword.value.isBlank()) null else _searchKeyword.value
                )
                totalPages = page.pages
                currentPage = page.currentPage
                _posts.value = page.results
                hasMorePages = currentPage < totalPages
                _state.value = ViewState.Loaded(page.results)
            } catch (e: Exception) {
                _state.value = ViewState.Error(e.localizedMessage ?: "Unknown error")
            }
        }
    }

    override suspend fun loadNextPage() {
        val userID = user.id.toIntOrNull() ?: return
        if (_isLoadingMore.value || !hasMorePages) return

        _isLoadingMore.value = true
        viewModelScope.launch {
            try {
                val nextPage = currentPage + 1
                val page = araBoardRepository.fetchPosts(
                    type = AraBoardTarget.PostListType.User(userID),
                    page = nextPage,
                    pageSize = pageSize,
                    searchKeyword = if (_searchKeyword.value.isBlank()) null else _searchKeyword.value
                )
                currentPage = page.currentPage
                _posts.value = _posts.value + page.results
                hasMorePages = currentPage < totalPages
                _state.value = ViewState.Loaded(_posts.value)
                _isLoadingMore.value = false
            } catch (e: Exception) {
                _isLoadingMore.value = false
            }
        }
    }

    override fun refreshItem(postID: Int) {
        viewModelScope.launch {
            try {
                val updated = araBoardRepository.fetchPost(origin = AraBoardTarget.PostOrigin.None, postID = postID)
                val updatedPosts = _posts.value.toMutableList()
                val idx = updatedPosts.indexOfFirst { it.id == updated.id }
                if (idx != -1) {
                    val prev = updatedPosts[idx]
                    val merged = prev.copy(
                        upVotes = updated.upVotes,
                        downVotes = updated.downVotes,
                        commentCount = updated.commentCount
                    )
                    updatedPosts[idx] = merged
                    _posts.value = updatedPosts
                    _state.value = ViewState.Loaded(updatedPosts)
                }
            } catch (e: Exception) {
                Log.e("UserPostListViewModel", "Failed to refresh item")
                _state.value = ViewState.Error(e.localizedMessage ?: "Unknown error")
            }
        }
    }

    override fun removePost(postID: Int) {
        val updatedPosts = _posts.value.toMutableList()
        val idx = updatedPosts.indexOfFirst { it.id == postID }
        if (idx != -1) {
            updatedPosts.removeAt(idx)
            _posts.value = updatedPosts
            _state.value = ViewState.Loaded(updatedPosts)
        }
    }

    override fun bind() {
        viewModelScope.launch {
            _searchKeyword
                .map { it.trim() }
                .distinctUntilChanged()
                .debounce(350)
                .collectLatest {
                    fetchInitialPosts()
                }
        }
    }
}
