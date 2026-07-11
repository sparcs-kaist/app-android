package org.sparcs.soap.boardListTests

import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.sparcs.soap.app.domain.models.ara.AraBoard
import org.sparcs.soap.app.features.boardList.BoardListViewModel
import org.sparcs.soap.app.shared.mocks.ara.mockList
import org.sparcs.soap.buddyTestSupport.useCase.MockAraBoardUseCase
import org.sparcs.soap.testSupport.MainDispatcherRule

class BoardListViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private lateinit var mockAraBoardUseCase: MockAraBoardUseCase
    private lateinit var viewModel: BoardListViewModel

    @Before
    fun setup() {
        mockAraBoardUseCase = MockAraBoardUseCase()
        viewModel = BoardListViewModel(mockAraBoardUseCase)
    }

    @Test
    fun `initial state is loading`() {
        assertEquals(BoardListViewModel.ViewState.Loading, viewModel.state.value)
    }

    @Test
    fun `fetchBoards success sorts boards by id and derives unique sorted groups`() = runTest {
        val boards = AraBoard.mockList()
        mockAraBoardUseCase.fetchBoardsResult = Result.success(boards)

        viewModel.fetchBoards()

        val state = viewModel.state.value
        assertTrue(state is BoardListViewModel.ViewState.Loaded)
        state as BoardListViewModel.ViewState.Loaded

        assertEquals(boards.sortedBy { it.id }.map { it.id }, state.boards.map { it.id })

        val expectedGroups = boards.map { it.group }
            .distinctBy { it.id }
            .sortedBy { it.id }
            .map { it.id }
        assertEquals(expectedGroups, state.groups.map { it.id })
        assertEquals(1, mockAraBoardUseCase.fetchBoardsCallCount)
    }

    @Test
    fun `fetchBoards failure sets error state`() = runTest {
        mockAraBoardUseCase.fetchBoardsResult = Result.failure(Exception("Test failure"))

        viewModel.fetchBoards()

        assertTrue(viewModel.state.value is BoardListViewModel.ViewState.Error)
    }
}
