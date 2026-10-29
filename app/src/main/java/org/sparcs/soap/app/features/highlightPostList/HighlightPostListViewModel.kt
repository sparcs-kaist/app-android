package org.sparcs.soap.app.features.highlightPostList

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import org.sparcs.soap.app.domain.enums.ara.PostListType
import org.sparcs.soap.app.domain.enums.ara.PostOrigin
import org.sparcs.soap.app.domain.models.ara.AraPost
import org.sparcs.soap.app.domain.models.ara.BoardListSection
import org.sparcs.soap.app.domain.usecases.ara.AraBoardUseCaseProtocol
import org.sparcs.soap.app.features.boardList.BoardListViewModel
import org.sparcs.soap.app.features.postList.markedRead
import org.sparcs.soap.app.features.postList.refreshed
import org.sparcs.soap.app.features.postList.updating
import javax.inject.Inject

interface HighlightPostListViewModelProtocol {
    val section: BoardListSection
    val state: StateFlow<HighlightPostListViewModel.ViewState>
    val isRefreshing: StateFlow<Boolean>
    val isLoadingMore: StateFlow<Boolean>
    var lastClickedPostId: Int?

    fun fetchInitialPosts()
    fun refresh()
    fun loadNextPage()
    fun refreshItem(postID: Int)
    fun markRead(postID: Int)
}

@HiltViewModel
class HighlightPostListViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val araBoardUseCase: AraBoardUseCaseProtocol,
) : ViewModel(), HighlightPostListViewModelProtocol {

    sealed interface ViewState {
        data object Loading : ViewState
        data class Loaded(val posts: List<AraPost>) : ViewState
        data class Error(val error: Exception) : ViewState
    }

    override val section: BoardListSection =
        savedStateHandle.get<String>(SECTION_ARG)?.let(BoardListSection::fromId) ?: BoardListSection.Trending

    private val _state = MutableStateFlow<ViewState>(ViewState.Loading)
    override val state: StateFlow<ViewState> = _state.asStateFlow()

    private val _isRefreshing = MutableStateFlow(false)
    override val isRefreshing: StateFlow<Boolean> = _isRefreshing.asStateFlow()

    private val _isLoadingMore = MutableStateFlow(false)
    override val isLoadingMore: StateFlow<Boolean> = _isLoadingMore.asStateFlow()

    override var lastClickedPostId: Int? = null

    private var listType: PostListType? = null
    private var currentPage = 1
    private var totalPages = 1
    private var fetchGeneration = 0

    private val posts: List<AraPost>?
        get() = (_state.value as? ViewState.Loaded)?.posts

    override fun fetchInitialPosts() {
        viewModelScope.launch { loadFirstPage() }
    }

    override fun refresh() {
        viewModelScope.launch {
            _isRefreshing.value = true
            loadFirstPage()
            _isRefreshing.value = false
        }
    }

    private suspend fun loadFirstPage() {
        fetchGeneration += 1
        val generation = fetchGeneration
        try {
            val page = araBoardUseCase.fetchPosts(resolveListType(), page = 1, pageSize = PAGE_SIZE, searchKeyword = null)
            if (generation != fetchGeneration) return
            currentPage = page.currentPage
            totalPages = page.pages
            _state.value = ViewState.Loaded(page.results.distinctBy { it.id })
        } catch (e: Exception) {
            if (generation != fetchGeneration || posts != null) return
            _state.value = ViewState.Error(e)
        }
    }

    override fun loadNextPage() {
        val shown = posts ?: return
        val type = listType ?: return
        if (_isLoadingMore.value || currentPage >= totalPages) return
        _isLoadingMore.value = true
        val generation = fetchGeneration
        viewModelScope.launch {
            try {
                val page = araBoardUseCase.fetchPosts(type, page = currentPage + 1, pageSize = PAGE_SIZE, searchKeyword = null)
                if (generation != fetchGeneration) return@launch
                currentPage = page.currentPage
                val shownIds = shown.map { it.id }.toSet()
                _state.value = ViewState.Loaded(shown + page.results.filterNot { it.id in shownIds })
            } catch (_: Exception) {
            } finally {
                _isLoadingMore.value = false
            }
        }
    }

    override fun refreshItem(postID: Int) {
        viewModelScope.launch {
            val updated = try {
                araBoardUseCase.fetchPost(origin = PostOrigin.None, postID = postID)
            } catch (_: Exception) {
                return@launch
            }
            updatePost(postID) { it.refreshed(updated) }
        }
    }

    override fun markRead(postID: Int) {
        updatePost(postID) { it.markedRead() }
    }

    private fun updatePost(postID: Int, transform: (AraPost) -> AraPost) {
        val updated = posts?.updating(postID, transform) ?: return
        _state.value = ViewState.Loaded(updated)
    }

    private suspend fun resolveListType(): PostListType = listType ?: when (val section = section) {
        BoardListSection.Trending -> PostListType.Trending
        BoardListSection.Latest -> PostListType.Latest(BoardListViewModel.latestBoardIds(araBoardUseCase.fetchBoards()))
        is BoardListSection.Board -> PostListType.BoardSlug(section.slug)
    }.also { listType = it }

    companion object {
        const val SECTION_ARG = "section"
        const val PAGE_SIZE = 30
    }
}
