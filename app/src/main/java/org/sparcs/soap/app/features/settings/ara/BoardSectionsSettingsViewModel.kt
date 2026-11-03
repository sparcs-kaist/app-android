package org.sparcs.soap.app.features.settings.ara

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import org.sparcs.soap.app.domain.helpers.BoardListSectionStoreProtocol
import org.sparcs.soap.app.domain.models.ara.AraBoard
import org.sparcs.soap.app.domain.models.ara.AraBoardGroup
import org.sparcs.soap.app.domain.models.ara.BoardListSection
import org.sparcs.soap.app.domain.services.AnalyticsServiceProtocol
import org.sparcs.soap.app.domain.usecases.ara.AraBoardUseCaseProtocol
import org.sparcs.soap.app.features.boardList.event.BoardListViewEvent
import javax.inject.Inject

interface BoardSectionsSettingsViewModelProtocol {
    val boardsState: BoardSectionsSettingsViewModel.BoardsState
    val sections: List<BoardListSection>
    fun loadBoards()
    fun add(section: BoardListSection)
    fun remove(section: BoardListSection)
    fun move(section: BoardListSection, offset: Int)
    fun resetToDefault()
}

@HiltViewModel
class BoardSectionsSettingsViewModel @Inject constructor(
    private val araBoardUseCase: AraBoardUseCaseProtocol,
    private val sectionStore: BoardListSectionStoreProtocol,
    private val analyticsService: AnalyticsServiceProtocol,
) : ViewModel(), BoardSectionsSettingsViewModelProtocol {

    sealed interface BoardsState {
        data object Loading : BoardsState
        data class Loaded(val boards: List<AraBoard>, val groups: List<AraBoardGroup>) : BoardsState
        data class Error(val error: Exception) : BoardsState
    }

    override var boardsState: BoardsState by mutableStateOf(BoardsState.Loading)
        private set
    override var sections: List<BoardListSection> by mutableStateOf(sectionStore.load())
        private set

    override fun loadBoards() {
        if (boardsState is BoardsState.Error) boardsState = BoardsState.Loading
        viewModelScope.launch {
            boardsState = try {
                val boards = araBoardUseCase.fetchBoards().sortedBy { it.id }
                BoardsState.Loaded(boards, boards.map { it.group }.distinctBy { it.id }.sortedBy { it.id })
            } catch (e: Exception) {
                BoardsState.Error(e)
            }
        }
    }

    override fun add(section: BoardListSection) {
        if (section !in sections) update(sections + section)
    }

    override fun remove(section: BoardListSection) {
        update(sections - section)
    }

    override fun move(section: BoardListSection, offset: Int) {
        update(BoardSectionsOrder.move(sections, sections.indexOf(section), offset))
    }

    override fun resetToDefault() {
        update(BoardListSection.defaults)
    }

    private fun update(newSections: List<BoardListSection>) {
        if (newSections == sections) return
        sections = newSections
        sectionStore.save(newSections)
        analyticsService.logEvent(BoardListViewEvent.SectionsEdited(newSections.size))
    }
}

object BoardSectionsOrder {
    val highlights: List<BoardListSection> = listOf(BoardListSection.Trending, BoardListSection.Latest)

    fun addableHighlights(sections: List<BoardListSection>): List<BoardListSection> =
        highlights.filterNot { it in sections }

    fun addableBoards(sections: List<BoardListSection>, boards: List<AraBoard>, group: AraBoardGroup): List<AraBoard> =
        boards.filter { it.group.id == group.id && it.userReadable != false && BoardListSection.Board(it.slug) !in sections }

    fun move(sections: List<BoardListSection>, index: Int, offset: Int): List<BoardListSection> {
        val target = index + offset
        if (index !in sections.indices || target !in sections.indices) return sections
        return sections.toMutableList().apply { add(target, removeAt(index)) }
    }
}
