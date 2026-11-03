package org.sparcs.soap.buddyPreviewSupport.post

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import org.sparcs.soap.app.domain.models.ara.AraBoard
import org.sparcs.soap.app.domain.models.ara.AraPost
import org.sparcs.soap.app.domain.models.ara.BoardListSection
import org.sparcs.soap.app.features.boardList.BoardListViewModel
import org.sparcs.soap.app.features.boardList.BoardListViewModelProtocol
import org.sparcs.soap.app.shared.mocks.ara.mockList

class PreviewBoardListViewModel(
    initialState: BoardListViewModel.ViewState,
    override val sections: List<BoardListSection> = emptyList(),
    override val boardsWithNewPosts: Set<Int> = emptySet(),
    private val sectionPosts: Map<BoardListSection, List<AraPost>> = emptyMap(),
) : BoardListViewModelProtocol {

    private val _state = MutableStateFlow(initialState)
    override val state: StateFlow<BoardListViewModel.ViewState> = _state.asStateFlow()

    override fun posts(section: BoardListSection): List<AraPost>? = sectionPosts[section]

    override fun fetchBoards() {}

    override fun reloadSections() {}

    companion object {
        fun loadedState(): BoardListViewModel.ViewState {
            val boards = AraBoard.mockList()

            val groups = boards.map { it.group }
                .distinctBy { it.id }
                .sortedBy { it.id }

            return BoardListViewModel.ViewState.Loaded(boards = boards, groups = groups)
        }
    }
}
