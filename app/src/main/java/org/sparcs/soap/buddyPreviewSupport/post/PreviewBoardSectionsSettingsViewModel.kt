package org.sparcs.soap.buddyPreviewSupport.post

import org.sparcs.soap.app.domain.models.ara.AraBoard
import org.sparcs.soap.app.domain.models.ara.BoardListSection
import org.sparcs.soap.app.features.settings.ara.BoardSectionsSettingsViewModel
import org.sparcs.soap.app.features.settings.ara.BoardSectionsSettingsViewModelProtocol
import org.sparcs.soap.app.shared.mocks.ara.mockList

class PreviewBoardSectionsSettingsViewModel(
    override val boardsState: BoardSectionsSettingsViewModel.BoardsState = loadedState(),
    override val sections: List<BoardListSection> = listOf(BoardListSection.Latest, BoardListSection.Board(AraBoard.MARKET_SLUG)),
) : BoardSectionsSettingsViewModelProtocol {

    override fun loadBoards() {}
    override fun add(section: BoardListSection) {}
    override fun remove(section: BoardListSection) {}
    override fun move(section: BoardListSection, offset: Int) {}
    override fun resetToDefault() {}

    companion object {
        fun loadedState(): BoardSectionsSettingsViewModel.BoardsState {
            val boards = AraBoard.mockList()
            return BoardSectionsSettingsViewModel.BoardsState.Loaded(boards, boards.map { it.group }.distinctBy { it.id })
        }
    }
}
