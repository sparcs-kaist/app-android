package org.sparcs.soap.boardListTests

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.sparcs.soap.app.domain.models.ara.AraBoard
import org.sparcs.soap.app.domain.models.ara.BoardListSection
import org.sparcs.soap.app.features.boardList.event.BoardListViewEvent
import org.sparcs.soap.app.features.settings.ara.BoardSectionsSettingsViewModel
import org.sparcs.soap.app.shared.mocks.ara.mock
import org.sparcs.soap.buddyTestSupport.error.TestError
import org.sparcs.soap.buddyTestSupport.RecordingAnalyticsService
import org.sparcs.soap.buddyTestSupport.helper.TestBoardListSectionStore
import org.sparcs.soap.buddyTestSupport.useCase.MockAraBoardUseCase
import org.sparcs.soap.testSupport.MainDispatcherRule

class BoardSectionsSettingsViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val useCase = MockAraBoardUseCase()
    private val store = TestBoardListSectionStore()
    private val analytics = RecordingAnalyticsService()
    private val talk = BoardListSection.Board("talk")

    private fun viewModel() = BoardSectionsSettingsViewModel(useCase, store, analytics)

    @Test
    fun savesEachChangeAndLogsIt() {
        val viewModel = viewModel()

        viewModel.add(talk)
        viewModel.move(talk, -1)
        viewModel.remove(BoardListSection.Trending)

        val expected = listOf(BoardListSection.Latest, talk, BoardListSection.Board(AraBoard.MARKET_SLUG))
        assertEquals(expected, viewModel.sections)
        assertEquals(expected, store.sections)
        assertEquals(listOf(4, 4, 3), analytics.events.map { (it as BoardListViewEvent.SectionsEdited).count })
        assertEquals("board_sections_edited", analytics.events.first().name)
    }

    @Test
    fun ignoresChangesThatChangeNothing() {
        val viewModel = viewModel()

        viewModel.add(BoardListSection.Trending)
        viewModel.move(BoardListSection.Trending, -1)
        viewModel.resetToDefault()

        assertTrue(analytics.events.isEmpty())
    }

    @Test
    fun resetsToTheDefaults() {
        store.sections = listOf(talk)
        val viewModel = viewModel()

        viewModel.resetToDefault()

        assertEquals(BoardListSection.defaults, store.sections)
    }

    @Test
    fun retriesLoadingTheBoardsAfterAFailure() {
        useCase.fetchBoardsResult = Result.failure(TestError.TestFailure())
        val viewModel = viewModel()
        viewModel.loadBoards()
        assertTrue(viewModel.boardsState is BoardSectionsSettingsViewModel.BoardsState.Error)

        useCase.fetchBoardsResult = Result.success(listOf(AraBoard.mock().copy(id = 2), AraBoard.mock().copy(id = 1)))
        viewModel.loadBoards()

        val loaded = viewModel.boardsState as BoardSectionsSettingsViewModel.BoardsState.Loaded
        assertEquals(listOf(1, 2), loaded.boards.map { it.id })
    }
}
