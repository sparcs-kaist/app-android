package org.sparcs.soap.App.Features.UserPostList

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
import org.sparcs.soap.App.Domain.Models.Ara.AraPost
import org.sparcs.soap.App.Domain.Models.Ara.AraPostAuthor
import org.sparcs.soap.App.Domain.Services.AnalyticsServiceProtocol
import org.sparcs.soap.App.Domain.Usecases.Ara.AraBoardUseCaseProtocol
import org.sparcs.soap.App.Features.PostList.Event.PostListViewEvent
import timber.log.Timber
import javax.inject.Inject

interface UserPostListViewModelProtocol {
    val state: StateFlow<UserPostListViewModel.ViewState>
    val user: AraPostAuthor
    var posts: StateFlow<List<AraPost>>
    var searchKeyword: StateFlow<String>

    val isLoadingMore: StateFlow<Boolean>
    var hasMorePages: Boolean

    var lastClickedPostId: Int?

    fun onSearchTextChange(text: String)

    suspend fun fetchInitialPosts()
    suspend fun loadNextPage()
    fun refreshItem(postID: Int)
    fun removePost(postID: Int)
    fun bind()
}

@HiltViewModel
class UserPostListViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val araBoardUseCase: AraBoardUseCaseProtocol,
    private val analyticsService: AnalyticsServiceProtocol,
) : ViewModel(), UserPostListViewModelProtocol {

    //Mark - ViewState
    sealed class ViewState {
        data object Loading : ViewState()
        data class Loaded(val posts: List<AraPost>) : ViewState()
        data class Error(val error: Exception) : ViewState()
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

    override var lastClickedPostId: Int? = null

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
    override fun onSearchTextChange(text: String) {
        _searchKeyword.value = text
    }

    override suspend fun fetchInitialPosts() {
        val userID = user.id.toDoubleOrNull()?.toInt() ?: return
        _state.value = ViewState.Loading
        try {
            val page = araBoardUseCase.fetchPosts(
                type = PostListType.User(userID),
                page = 1,
                pageSize = pageSize,
                searchKeyword = _searchKeyword.value.ifBlank { null }
            )
            totalPages = page.pages
            currentPage = page.currentPage
            _posts.value = page.results
            hasMorePages = currentPage < totalPages
            _state.value = ViewState.Loaded(page.results)
        } catch (e: Exception) {
            _state.value = ViewState.Error(e)
        }
    }

    override suspend fun loadNextPage() {
        val userID = user.id.toDoubleOrNull()?.toInt() ?: return
        if (_isLoadingMore.value || !hasMorePages) return

        _isLoadingMore.value = true
        try {
            val nextPage = currentPage + 1
            val page = araBoardUseCase.fetchPosts(
                type = PostListType.User(userID),
                page = nextPage,
                pageSize = pageSize,
                searchKeyword = _searchKeyword.value.ifBlank { null }
            )
            currentPage = page.currentPage
            _posts.value += page.results
            hasMorePages = currentPage < totalPages
            _state.value = ViewState.Loaded(_posts.value)
            _isLoadingMore.value = false
        } catch (e: Exception) {
            _isLoadingMore.value = false

        }
    }

    override fun refreshItem(postID: Int) {
        viewModelScope.launch {
            try {
                val updated =
                    araBoardUseCase.fetchPost(origin = PostOrigin.None, postID = postID)
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
                Timber.e("Failed to refresh item")
                _state.value = ViewState.Error(e)
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

    @OptIn(FlowPreview::class)
    override fun bind() {
        viewModelScope.launch {
            _searchKeyword
                .map { it.trim() }
                .distinctUntilChanged()
                .debounce(350)
                .collectLatest { keyword ->
                    if (keyword.isNotBlank()) {
                        analyticsService.logEvent(PostListViewEvent.SearchPerformed(keyword))
                    }
                    fetchInitialPosts()
                }
        }
    }
}
