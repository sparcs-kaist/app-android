package org.sparcs.soap.boardListTests

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.sparcs.soap.app.domain.enums.ara.PostListType
import org.sparcs.soap.app.domain.models.ara.AraBoard
import org.sparcs.soap.app.domain.models.ara.AraPost
import org.sparcs.soap.app.domain.models.ara.AraPostPage
import org.sparcs.soap.app.domain.models.ara.BoardListSection
import org.sparcs.soap.app.features.boardList.BoardListViewModel
import org.sparcs.soap.app.shared.mocks.ara.mock
import org.sparcs.soap.buddyTestSupport.helper.TestBoardListSectionStore
import org.sparcs.soap.buddyTestSupport.helper.TestBoardSeenPostsStore
import org.sparcs.soap.buddyTestSupport.useCase.MockAraBoardUseCase
import org.sparcs.soap.testSupport.MainDispatcherRule

class BoardSectionsTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val useCase = MockAraBoardUseCase()
    private val boards = listOf(
        AraBoard.mock().copy(id = 1, slug = "talk"),
        AraBoard.mock().copy(id = 2, slug = "portal-notice"),
        AraBoard.mock().copy(id = 3, slug = AraBoard.MARKET_SLUG),
        AraBoard.mock().copy(id = 4, slug = "secret", userReadable = false),
    )

    private fun page(vararg posts: AraPost) = AraPostPage(pages = 1, items = posts.size, currentPage = 1, results = posts.toList())

    @Test
    fun loadsEverySectionAndLeavesPortalNoticeOutOfLatest() {
        useCase.fetchBoardsResult = Result.success(boards)
        useCase.fetchPostsResult = Result.success(page(AraPost.mock().copy(id = 10)))
        val viewModel = BoardListViewModel(useCase, TestBoardListSectionStore(), TestBoardSeenPostsStore())

        viewModel.fetchBoards()

        assertTrue(PostListType.Trending in useCase.fetchedTypes)
        assertTrue(PostListType.Latest(listOf(1, 3)) in useCase.fetchedTypes)
        assertTrue(PostListType.BoardSlug(AraBoard.MARKET_SLUG) in useCase.fetchedTypes)
        BoardListSection.defaults.forEach { assertEquals(listOf(10), viewModel.posts(it)?.map { p -> p.id }) }
    }

    @Test
    fun marksBoardsWhoseNewestPostIsUnseenAndUnread() {
        assertTrue(BoardListViewModel.hasNewPost(AraPost.mock().copy(id = 5), newestSeen = null))
        assertTrue(BoardListViewModel.hasNewPost(AraPost.mock().copy(id = 5), newestSeen = 4))
        assertFalse(BoardListViewModel.hasNewPost(AraPost.mock().copy(id = 5), newestSeen = 5))
        assertFalse(BoardListViewModel.hasNewPost(AraPost.mock().copy(id = 9, readStatus = AraPost.ReadStatus.READ), newestSeen = 4))
    }

    @Test
    fun checksReadableBoardsForNewPosts() {
        useCase.fetchBoardsResult = Result.success(boards)
        useCase.fetchPostsResult = Result.success(page(AraPost.mock().copy(id = 20)))
        val viewModel = BoardListViewModel(useCase, TestBoardListSectionStore(emptyList()), TestBoardSeenPostsStore(mutableMapOf(1 to 20)))

        viewModel.fetchBoards()

        assertEquals(setOf(2, 3), viewModel.boardsWithNewPosts)
    }

    @Test
    fun reloadsOnlyWhenTheSectionsChange() {
        val store = TestBoardListSectionStore(listOf(BoardListSection.Trending))
        val viewModel = BoardListViewModel(useCase, store, TestBoardSeenPostsStore())
        viewModel.reloadSections()
        assertTrue(useCase.fetchedTypes.isEmpty())

        store.sections = listOf(BoardListSection.Latest, BoardListSection.Trending)
        viewModel.reloadSections()
        assertEquals(listOf(BoardListSection.Latest, BoardListSection.Trending), viewModel.sections)
        assertEquals(listOf<PostListType>(PostListType.Trending), useCase.fetchedTypes)
    }

    @Test
    fun readsStoredSectionIds() {
        assertEquals(BoardListSection.Board("market"), BoardListSection.fromId("board:market"))
        assertEquals(BoardListSection.Trending, BoardListSection.fromId("trending"))
        assertNull(BoardListSection.fromId("board:"))
        assertNull(BoardListSection.fromId("unknown"))
    }
}
