package org.sparcs.soap.App.Features.PostList

import android.net.Uri
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.gson.Gson
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import org.sparcs.soap.App.Domain.Enums.Ara.PostListType
import org.sparcs.soap.App.Domain.Enums.Ara.PostOrigin
import org.sparcs.soap.App.Domain.Models.Ara.AraBoard
import org.sparcs.soap.App.Domain.Models.Ara.AraPost
import org.sparcs.soap.App.Domain.Services.AnalyticsServiceProtocol
import org.sparcs.soap.App.Domain.Usecases.Ara.AraBoardUseCaseProtocol
import org.sparcs.soap.App.Features.PostList.Event.PostListViewEvent
import timber.log.Timber
import javax.inject.Inject

interface PostListViewModelProtocol {
    var state: StateFlow<PostListViewModel.ViewState>
    var board: AraBoard
    var posts: List<AraPost>
    var isLoadingMore: Boolean
    var hasMorePages: Boolean
    val searchKeyword: StateFlow<String>

    var lastClickedPostId: Int?

    suspend fun fetchInitialPosts()
    suspend fun loadNextPage()
    fun refreshItem(postID: Int)
    fun removePost(postID: Int)
    fun onSearchTextChange(text: String)
    fun bind()
}

@HiltViewModel
class PostListViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val araBoardUseCase: AraBoardUseCaseProtocol,
    private val analyticsService: AnalyticsServiceProtocol,
) : ViewModel(), PostListViewModelProtocol {

    private val initialBoard: AraBoard by lazy {
        val json = savedStateHandle.get<String>("board_json")
            ?: throw IllegalStateException("board_json is null. PostListViewModel requires a board_json to initialize.")
        Gson().fromJson(Uri.decode(json), AraBoard::class.java)
    }

    override var board: AraBoard = initialBoard

    sealed class ViewState {
        data object Loading : ViewState()
        data class Loaded(val posts: List<AraPost>) : ViewState()
        data class Error(val error: Exception) : ViewState()
    }

    private val _state = MutableStateFlow<ViewState>(ViewState.Loading)
    override var state: StateFlow<ViewState> = _state.asStateFlow()

    override var posts: List<AraPost> = emptyList()

    override var lastClickedPostId: Int? = null

    //Search Properties
    private val _searchKeyword = MutableStateFlow("")
    override val searchKeyword: StateFlow<String> = _searchKeyword

    override fun onSearchTextChange(text: String) {
        _searchKeyword.value = text
    }

    //Infinite Scroll Properties
    override var isLoadingMore: Boolean = false
    override var hasMorePages: Boolean = false
    private var currentPage: Int = 1
    private var totalPages: Int = 0
    private var pageSize: Int = 30

    //Mark: - Functions
    @OptIn(FlowPreview::class)
    override fun bind() {
        viewModelScope.launch {
            _searchKeyword
                .map { it.trim() }
                .distinctUntilChanged()
                .debounce(350)
                .collectLatest { keyword ->
                    if (keyword.isNotEmpty()) {
                        analyticsService.logEvent(PostListViewEvent.SearchPerformed(keyword))
                    }
                    fetchInitialPosts()
                }
        }
    }

    override suspend fun fetchInitialPosts() {
        _state.value = ViewState.Loading
        try {
            val page = araBoardUseCase.fetchPosts(
                type = PostListType.Board(boardID = board.id),
                page = 1,
                pageSize = pageSize,
                searchKeyword = _searchKeyword.value.ifBlank { null }
            )
            totalPages = page.pages
            currentPage = page.currentPage
            posts = page.results
            hasMorePages = currentPage < totalPages
            _state.value = ViewState.Loaded(posts)
            analyticsService.logEvent(PostListViewEvent.PostsRefreshed)
        } catch (e: Exception) {
            _state.value = ViewState.Error(e)
        }
    }

    override suspend fun loadNextPage() {
        if (isLoadingMore || !hasMorePages) return
        isLoadingMore = true
        try {
            val nextPage = currentPage + 1
            val page = araBoardUseCase.fetchPosts(
                type = PostListType.Board(boardID = board.id),
                page = nextPage,
                pageSize = pageSize,
                searchKeyword = _searchKeyword.value.ifBlank { null }
            )
            currentPage = page.currentPage
            posts = posts + page.results
            hasMorePages = currentPage < totalPages
            _state.value = ViewState.Loaded(posts)
            isLoadingMore = false
            analyticsService.logEvent(PostListViewEvent.NextPageLoaded)
        } catch (e: Exception) {
            Timber.e("Error loading next page: $e")
            isLoadingMore = false
        }
    }

    override fun refreshItem(postID: Int) {
        viewModelScope.launch {
            val updated = try {
                araBoardUseCase.fetchPost(postID = postID, origin = PostOrigin.None)
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