package com.example.soap.Features.PostList

import android.net.Uri
import android.util.Log
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.soap.Domain.Models.Ara.AraBoard
import com.example.soap.Domain.Models.Ara.AraPost
import com.example.soap.Domain.Repositories.Ara.AraBoardRepositoryProtocol
import com.example.soap.Networking.RetrofitAPI.Ara.AraBoardTarget
import com.google.gson.Gson
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class PostListViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val araBoardRepository: AraBoardRepositoryProtocol
) : ViewModel(), PostListViewModelProtocol {

    private val initialBoard: AraBoard by lazy {
        val json = savedStateHandle.get<String>("board_json")
            ?: throw IllegalStateException("board_json is null. PostListViewModel requires a board_json to initialize.")
        Gson().fromJson(Uri.decode(json), AraBoard::class.java)
    }

    override var board: AraBoard = initialBoard

    sealed class ViewState{
        data object Loading : ViewState()
        data class Loaded(val posts: List<AraPost>) : ViewState()
        data class Error(val message: String) : ViewState()
    }

    private val _state = MutableStateFlow<ViewState>(ViewState.Loading)
    override var state: StateFlow<ViewState> = _state.asStateFlow()

    override var posts: List<AraPost> = emptyList()

    //Search Properties
    var _searchKeyword: MutableStateFlow<String> = MutableStateFlow("")
    override var searchKeyword: String =""
        get() = _searchKeyword.value
        set(value) {
            _searchKeyword.value = value
            field = value
        }

    //Infinite Scroll Properties
    override var isLoadingMore: Boolean = false
    override var hasMorePages: Boolean = true
    var currentPage: Int = 1
    var totalPages: Int = 0
    var pageSize: Int = 30

    //Mark: - Initializer
    init {
        viewModelScope.launch {
            fetchInitialPosts()
        }
    }

    //Mark: - Functions
    @OptIn(FlowPreview::class)
    override fun bind() {
        viewModelScope.launch {
            _searchKeyword
                .debounce(350)
                .distinctUntilChanged()
                .collectLatest {
                    fetchInitialPosts()
                }
        }
    }

    override suspend fun fetchInitialPosts() {
        viewModelScope.launch {
            try {
                val page = araBoardRepository.fetchPosts(
                    type = AraBoardTarget.PostListType.Board(boardID = board.id),
                    page = 1,
                    pageSize = pageSize,
                    searchKeyword = searchKeyword.ifBlank { null }
                )
                totalPages = page.pages
                currentPage = page.currentPage
                posts = page.results
                hasMorePages = currentPage < totalPages
                _state.value = ViewState.Loaded(posts)
            } catch (e: Exception) {
                _state.value = ViewState.Error(e.localizedMessage ?: "Unknown Error")
            }
        }
    }

    override suspend fun loadNextPage() {
        if (isLoadingMore || !hasMorePages) return
        isLoadingMore = true
        viewModelScope.launch {
            try {
                val nextPage = currentPage + 1
                val page = araBoardRepository.fetchPosts(
                    type = AraBoardTarget.PostListType.Board(boardID = board.id),
                    page = nextPage,
                    pageSize = pageSize,
                    searchKeyword = if (searchKeyword.isBlank()) null else searchKeyword
                )
                currentPage = page.currentPage
                posts = posts + page.results
                hasMorePages = currentPage < totalPages
                _state.value = ViewState.Loaded(posts)
                isLoadingMore = false

            } catch (e: Exception) {
                Log.e("PostListViewModel", "Error loading next page: $e")
                isLoadingMore = false
            }
        }
    }

    override fun refreshItem(postID: Int) {
        viewModelScope.launch {
            val updated = try {
                araBoardRepository.fetchPost(postID = postID, origin = AraBoardTarget.PostOrigin.None)
            } catch (e: Exception) {
                null
            } ?: return@launch

            val idx = posts.indexOfFirst { it.id == updated.id }
            if (idx != -1) {
                val mutablePosts = posts.toMutableList()
                val previousPost = mutablePosts[idx]
                mutablePosts[idx] = previousPost.copy(
                    upVotes = updated.upVotes,
                    downVotes = updated.downVotes,
                    commentCount = updated.commentCount
                )
                posts = mutablePosts
                _state.value = ViewState.Loaded(posts)
            }
        }
    }

    override fun removePost(postID: Int) {
        val idx = posts.indexOfFirst { it.id == postID }
        if (idx != -1) {
            val mutablePosts = posts.toMutableList()
            mutablePosts.removeAt(idx)
            posts = mutablePosts
            _state.value = ViewState.Loaded(posts)
        }
    }
}