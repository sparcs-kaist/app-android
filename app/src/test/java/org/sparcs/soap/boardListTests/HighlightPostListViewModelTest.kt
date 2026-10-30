package org.sparcs.soap.boardListTests

import androidx.lifecycle.SavedStateHandle
import kotlinx.coroutines.CompletableDeferred
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.sparcs.soap.app.domain.enums.ara.PostListType
import org.sparcs.soap.app.domain.models.ara.AraBoard
import org.sparcs.soap.app.domain.models.ara.AraPost
import org.sparcs.soap.app.domain.models.ara.AraPostPage
import org.sparcs.soap.app.domain.models.ara.BoardListSection
import org.sparcs.soap.app.features.highlightPostList.HighlightPostListViewModel
import org.sparcs.soap.app.shared.mocks.ara.mock
import org.sparcs.soap.buddyTestSupport.error.TestError
import org.sparcs.soap.buddyTestSupport.useCase.MockAraBoardUseCase
import org.sparcs.soap.testSupport.MainDispatcherRule

class HighlightPostListViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val useCase = MockAraBoardUseCase()

    private fun post(id: Int) = AraPost.mock().copy(id = id)

    private fun page(ids: List<Int>, currentPage: Int = 1, pages: Int = 2) =
        AraPostPage(pages = pages, items = ids.size, currentPage = currentPage, results = ids.map(::post))

    private fun viewModel(section: BoardListSection = BoardListSection.Trending) = HighlightPostListViewModel(
        SavedStateHandle(mapOf(HighlightPostListViewModel.SECTION_ARG to section.id)),
        useCase
    )

    private val HighlightPostListViewModel.postIds: List<Int>?
        get() = (state.value as? HighlightPostListViewModel.ViewState.Loaded)?.posts?.map { it.id }

    @Test
    fun loadsTrendingThirtyAtATime() {
        useCase.fetchPostsResult = Result.success(page(listOf(1, 2)))
        val viewModel = viewModel()

        viewModel.fetchInitialPosts()

        assertEquals(listOf(1, 2), viewModel.postIds)
        assertEquals(PostListType.Trending, useCase.fetchPostsCalls.single().type)
        assertEquals(HighlightPostListViewModel.PAGE_SIZE, useCase.fetchPostsCalls.single().pageSize)
    }

    @Test
    fun loadsLatestAcrossReadableBoardsButPortalNotice() {
        useCase.fetchBoardsResult = Result.success(
            listOf(
                AraBoard.mock().copy(id = 1, slug = "portal-notice"),
                AraBoard.mock().copy(id = 2, slug = "talk"),
                AraBoard.mock().copy(id = 3, slug = "secret", userReadable = false),
            )
        )
        useCase.fetchPostsResult = Result.success(page(listOf(1)))

        viewModel(BoardListSection.Latest).fetchInitialPosts()

        assertEquals(PostListType.Latest(listOf(2)), useCase.fetchPostsCalls.single().type)
    }

    @Test
    fun skipsPostsAlreadyShownWhenLoadingTheNextPage() {
        useCase.fetchPostsResult = Result.success(page(listOf(1, 2)))
        val viewModel = viewModel()
        viewModel.fetchInitialPosts()

        useCase.fetchPostsResult = Result.success(page(listOf(2, 3), currentPage = 2))
        viewModel.loadNextPage()

        assertEquals(listOf(1, 2, 3), viewModel.postIds)
        assertEquals(2, useCase.fetchPostsCalls.last().page)
    }

    @Test
    fun stopsAtTheLastPage() {
        useCase.fetchPostsResult = Result.success(page(listOf(1), pages = 1))
        val viewModel = viewModel()
        viewModel.fetchInitialPosts()

        viewModel.loadNextPage()

        assertEquals(1, useCase.fetchPostsCallCount)
    }

    @Test
    fun dropsANextPageRequestedBeforeARefresh() {
        useCase.fetchPostsResult = Result.success(page(listOf(1, 2)))
        val viewModel = viewModel()
        viewModel.fetchInitialPosts()

        val nextPage = CompletableDeferred<AraPostPage>()
        useCase.fetchPostsGates.addLast(nextPage)
        viewModel.loadNextPage()
        assertTrue(viewModel.isLoadingMore.value)

        useCase.fetchPostsResult = Result.success(page(listOf(5, 6)))
        viewModel.refresh()
        nextPage.complete(page(listOf(3, 4), currentPage = 2))

        assertEquals(listOf(5, 6), viewModel.postIds)
        assertFalse(viewModel.isLoadingMore.value)
    }

    @Test
    fun aFailedRefreshKeepsWhatWasShown() {
        useCase.fetchPostsResult = Result.success(page(listOf(1)))
        val viewModel = viewModel()
        viewModel.fetchInitialPosts()

        useCase.fetchPostsResult = Result.failure(TestError.TestFailure())
        viewModel.refresh()

        assertEquals(listOf(1), viewModel.postIds)
        assertFalse(viewModel.isRefreshing.value)
    }

    @Test
    fun showsAnErrorWhenNothingLoaded() {
        useCase.fetchPostsResult = Result.failure(TestError.TestFailure())
        val viewModel = viewModel()

        viewModel.fetchInitialPosts()

        assertTrue(viewModel.state.value is HighlightPostListViewModel.ViewState.Error)
    }

    @Test
    fun marksAnOpenedPostRead() {
        useCase.fetchPostsResult = Result.success(page(listOf(1, 2)))
        val viewModel = viewModel()
        viewModel.fetchInitialPosts()

        viewModel.markRead(2)

        val posts = (viewModel.state.value as HighlightPostListViewModel.ViewState.Loaded).posts
        assertEquals(AraPost.ReadStatus.READ, posts.first { it.id == 2 }.readStatus)
    }
}
