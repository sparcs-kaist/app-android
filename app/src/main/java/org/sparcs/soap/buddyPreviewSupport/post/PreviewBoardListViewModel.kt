package org.sparcs.soap.buddyPreviewSupport.post

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import org.sparcs.soap.app.domain.models.ara.AraBoard
import org.sparcs.soap.app.features.boardList.BoardListViewModel
import org.sparcs.soap.app.features.boardList.BoardListViewModelProtocol
import org.sparcs.soap.app.shared.mocks.ara.mockList

class PreviewBoardListViewModel(
    initialState: BoardListViewModel.ViewState
) : BoardListViewModelProtocol {

    private val _state = MutableStateFlow(initialState)
    override val state: StateFlow<BoardListViewModel.ViewState> = _state.asStateFlow()

    override fun fetchBoards() { }

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