package org.sparcs.soap.BuddyPreviewSupport.Post

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import org.sparcs.soap.App.Domain.Models.Ara.AraBoard
import org.sparcs.soap.App.Features.BoardList.BoardListViewModel
import org.sparcs.soap.App.Features.BoardList.BoardListViewModelProtocol
import org.sparcs.soap.App.Shared.Mocks.Ara.mockList

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