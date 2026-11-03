package org.sparcs.soap.app.features.boardList

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import org.sparcs.soap.app.domain.enums.ara.PostListType
import org.sparcs.soap.app.domain.helpers.BoardListSectionStoreProtocol
import org.sparcs.soap.app.domain.helpers.BoardSeenPostsStoreProtocol
import org.sparcs.soap.app.domain.models.ara.AraBoard
import org.sparcs.soap.app.domain.models.ara.AraBoardGroup
import org.sparcs.soap.app.domain.models.ara.AraPost
import org.sparcs.soap.app.domain.models.ara.BoardListSection
import org.sparcs.soap.app.domain.usecases.ara.AraBoardUseCaseProtocol
import javax.inject.Inject

interface BoardListViewModelProtocol {
    val state: StateFlow<BoardListViewModel.ViewState>
    val sections: List<BoardListSection>
    val boardsWithNewPosts: Set<Int>
    fun posts(section: BoardListSection): List<AraPost>?
    fun fetchBoards()
    fun reloadSections()
}

@HiltViewModel
class BoardListViewModel @Inject constructor(
    private val araBoardUseCase: AraBoardUseCaseProtocol,
    private val sectionStore: BoardListSectionStoreProtocol,
    private val seenPostsStore: BoardSeenPostsStoreProtocol,
) : ViewModel(), BoardListViewModelProtocol {

    sealed class ViewState {
        data object Loading : ViewState()
        data class Loaded(val boards: List<AraBoard>, val groups: List<AraBoardGroup>) : ViewState()
        data class Error(val error: Exception) : ViewState()
    }

    private val _state = MutableStateFlow<ViewState>(ViewState.Loading)
    override val state: StateFlow<ViewState> = _state.asStateFlow()

    override var sections: List<BoardListSection> by mutableStateOf(sectionStore.load())
        private set
    override var boardsWithNewPosts: Set<Int> by mutableStateOf(emptySet())
        private set
    private val sectionPosts = mutableStateMapOf<BoardListSection, List<AraPost>>()
    private var boards: List<AraBoard> = emptyList()

    override fun posts(section: BoardListSection): List<AraPost>? = sectionPosts[section]

    override fun reloadSections() {
        val loaded = sectionStore.load()
        if (loaded == sections) return
        sections = loaded
        viewModelScope.launch {
            loaded.filter { it !in sectionPosts }.forEach { load(it) }
        }
    }

    override fun fetchBoards() {
        viewModelScope.launch {
            coroutineScope {
                sections.filter { it != BoardListSection.Latest }.forEach { launch { load(it) } }
                launch { loadBoardsAndLatest() }
            }
        }
    }

    private suspend fun loadBoardsAndLatest() {
        try {
            val sortedBoards = araBoardUseCase.fetchBoards().sortedBy { it.id }
            val groups = sortedBoards.map { it.group }.distinctBy { it.id }.sortedBy { it.id }
            boards = sortedBoards
            _state.value = ViewState.Loaded(sortedBoards, groups)
            coroutineScope {
                launch { checkForNewPosts(sortedBoards) }
                if (BoardListSection.Latest in sections) launch { load(BoardListSection.Latest) }
            }
        } catch (e: Exception) {
            _state.value = ViewState.Error(e)
        }
    }

    private suspend fun load(section: BoardListSection) {
        val (type, pageSize) = when (section) {
            BoardListSection.Trending -> PostListType.Trending to HIGHLIGHT_COUNT
            BoardListSection.Latest -> {
                if (boards.isEmpty()) return
                PostListType.Latest(latestBoardIds(boards)) to HIGHLIGHT_COUNT
            }
            is BoardListSection.Board ->
                PostListType.BoardSlug(section.slug) to if (section.slug == AraBoard.MARKET_SLUG) MARKET_SHELF_COUNT else HIGHLIGHT_COUNT
        }
        val page = runCatching { araBoardUseCase.fetchPosts(type, page = 1, pageSize = pageSize, searchKeyword = null) }.getOrNull()
        sectionPosts[section] = page?.results ?: sectionPosts[section] ?: emptyList()
    }

    private suspend fun checkForNewPosts(boards: List<AraBoard>) {
        val newest = coroutineScope {
            boards.filter { it.userReadable != false }.map { board ->
                async {
                    val page = runCatching { araBoardUseCase.fetchPosts(PostListType.Board(board.id), 1, 1, null) }.getOrNull()
                    board.id to page?.results?.firstOrNull()
                }
            }.awaitAll()
        }
        boardsWithNewPosts = newest.mapNotNull { (boardId, post) ->
            boardId.takeIf { post != null && hasNewPost(post, seenPostsStore.newestSeen(boardId)) }
        }.toSet()
    }

    companion object {
        const val HIGHLIGHT_COUNT = 3
        const val MARKET_SHELF_COUNT = 10
        private const val PORTAL_NOTICE_SLUG = "portal-notice"

        fun latestBoardIds(boards: List<AraBoard>): List<Int> =
            boards.filter { it.slug != PORTAL_NOTICE_SLUG && it.userReadable != false }.map { it.id }

        fun hasNewPost(newest: AraPost, newestSeen: Int?): Boolean =
            newest.id > (newestSeen ?: Int.MIN_VALUE) && newest.readStatus != AraPost.ReadStatus.READ
    }
}
