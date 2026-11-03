package org.sparcs.soap.app.features.postList

import android.net.Uri
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
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
import org.sparcs.soap.app.domain.enums.ara.PostListType
import org.sparcs.soap.app.domain.enums.ara.PostOrigin
import org.sparcs.soap.app.domain.helpers.BoardSeenPostsStoreProtocol
import org.sparcs.soap.app.domain.models.ara.AraBoard
import org.sparcs.soap.app.domain.models.ara.AraBoardTopic
import org.sparcs.soap.app.domain.models.ara.AraPost
import org.sparcs.soap.app.domain.services.AnalyticsServiceProtocol
import org.sparcs.soap.app.domain.usecases.ara.AraBoardUseCaseProtocol
import org.sparcs.soap.app.features.postList.event.PostListViewEvent
import timber.log.Timber
import javax.inject.Inject

interface PostListViewModelProtocol {
    var state: StateFlow<PostListViewModel.ViewState>
    var board: AraBoard
    var posts: List<AraPost>
    var isLoadingMore: Boolean
    var hasMorePages: Boolean
    val searchKeyword: StateFlow<String>
    val selectedTopic: AraBoardTopic?

    var lastClickedPostId: Int?

    fun fetchInitialPosts()
    suspend fun loadNextPage()
    fun refreshItem(postID: Int)
    fun markRead(postID: Int)
    fun removePost(postID: Int)
    fun onSearchTextChange(text: String)
    fun selectTopic(topic: AraBoardTopic?)
    fun bind()
}

@HiltViewModel
class PostListViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val araBoardUseCase: AraBoardUseCaseProtocol,
    private val analyticsService: AnalyticsServiceProtocol,
    private val seenPostsStore: BoardSeenPostsStoreProtocol,
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

    private val _searchKeyword = MutableStateFlow("")
    override val searchKeyword: StateFlow<String> = _searchKeyword

    override fun onSearchTextChange(text: String) {
        _searchKeyword.value = text
    }

    override var selectedTopic: AraBoardTopic? by mutableStateOf(null)
        private set
    private var generation = 0

    private val listType: PostListType
        get() = PostListType.Board(boardID = board.id, topicID = selectedTopic?.id)

    override fun selectTopic(topic: AraBoardTopic?) {
        if (topic == selectedTopic) return
        selectedTopic = topic
        analyticsService.logEvent(PostListViewEvent.TopicSelected(topic?.slug))
        fetchInitialPosts()
    }

    override var isLoadingMore: Boolean by mutableStateOf(false)
    override var hasMorePages: Boolean = false
    private var currentPage: Int = 1
    private var totalPages: Int = 0
    private var pageSize: Int = 30

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

    override fun fetchInitialPosts() {
        val request = ++generation
        _state.value = ViewState.Loading
        isLoadingMore = false
        viewModelScope.launch {
            try {
                val page = araBoardUseCase.fetchPosts(
                    type = listType,
                    page = 1,
                    pageSize = pageSize,
                    searchKeyword = _searchKeyword.value.ifBlank { null }
                )
                if (request != generation) return@launch
                if (selectedTopic == null && _searchKeyword.value.isBlank()) {
                    page.results.maxOfOrNull { it.id }?.let { seenPostsStore.markSeen(it, board.id) }
                }
                totalPages = page.pages
                currentPage = page.currentPage
                posts = page.results
                hasMorePages = currentPage < totalPages
                _state.value = ViewState.Loaded(posts)
                analyticsService.logEvent(PostListViewEvent.PostsRefreshed)
            } catch (e: Exception) {
                if (request == generation) _state.value = ViewState.Error(e)
            }
        }
    }

    override suspend fun loadNextPage() {
        if (isLoadingMore || !hasMorePages) return
        val request = generation
        isLoadingMore = true
        try {
            val nextPage = currentPage + 1
            val page = araBoardUseCase.fetchPosts(
                type = listType,
                page = nextPage,
                pageSize = pageSize,
                searchKeyword = _searchKeyword.value.ifBlank { null }
            )
            if (request != generation) return
            currentPage = page.currentPage
            posts = (posts + page.results).distinctBy { it.id }
            hasMorePages = currentPage < totalPages
            _state.value = ViewState.Loaded(posts)
            analyticsService.logEvent(PostListViewEvent.NextPageLoaded)
        } catch (e: Exception) {
            Timber.e("Error loading next page: $e")
        } finally {
            if (request == generation) isLoadingMore = false
        }
    }

    override fun refreshItem(postID: Int) {
        viewModelScope.launch {
            val updated = try {
                araBoardUseCase.fetchPost(postID = postID, origin = PostOrigin.None)
            } catch (_: Exception) {
                null
            } ?: return@launch
            updatePost(postID) { it.refreshed(updated) }
        }
    }

    override fun markRead(postID: Int) {
        updatePost(postID) { it.markedRead() }
    }

    private fun updatePost(postID: Int, transform: (AraPost) -> AraPost) {
        posts = posts.updating(postID, transform) ?: return
        _state.value = ViewState.Loaded(posts)
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