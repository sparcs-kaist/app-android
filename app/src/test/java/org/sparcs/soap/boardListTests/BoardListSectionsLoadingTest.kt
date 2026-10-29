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
import org.sparcs.soap.buddyTestSupport.error.TestError
import org.sparcs.soap.buddyTestSupport.helper.TestBoardListSectionStore
import org.sparcs.soap.buddyTestSupport.helper.TestBoardSeenPostsStore
import org.sparcs.soap.buddyTestSupport.useCase.MockAraBoardUseCase
import org.sparcs.soap.testSupport.MainDispatcherRule

class BoardListSectionsLoadingTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val talk = BoardListSection.Board("talk")
    private val market = BoardListSection.Board(AraBoard.MARKET_SLUG)
    private val boards = listOf(
        AraBoard.mock().copy(id = 1, slug = "portal-notice"),
        AraBoard.mock().copy(id = 2, slug = "talk"),
        AraBoard.mock().copy(id = 7, slug = AraBoard.MARKET_SLUG),
    )

    private fun post(id: Int) = AraPost.mock().copy(id = id)

    private fun page(vararg posts: AraPost) = AraPostPage(pages = 1, items = posts.size, currentPage = 1, results = posts.toList())

    private fun useCase(
        trending: List<AraPost> = emptyList(),
        latest: List<AraPost> = emptyList(),
        market: List<AraPost> = emptyList(),
        talk: List<AraPost> = emptyList(),
    ) = MockAraBoardUseCase().apply {
        fetchBoardsResult = Result.success(boards)
        fetchPostsHandler = { call ->
            when (val type = call.type) {
                PostListType.Trending -> page(*trending.toTypedArray())
                is PostListType.Latest -> page(*latest.toTypedArray())
                is PostListType.BoardSlug -> when (type.slug) {
                    AraBoard.MARKET_SLUG -> page(*market.toTypedArray())
                    "talk" -> page(*talk.toTypedArray())
                    else -> page()
                }
                else -> page()
            }
        }
    }

    private fun viewModel(useCase: MockAraBoardUseCase, store: TestBoardListSectionStore = TestBoardListSectionStore()) =
        BoardListViewModel(useCase, store, TestBoardSeenPostsStore())

    @Test
    fun sectionsStartUnloadedAndAreEmptyIfTheyFail() {
        val useCase = useCase().apply { fetchPostsHandler = { throw TestError.TestFailure() } }
        val viewModel = viewModel(useCase)

        BoardListSection.defaults.forEach { assertNull(viewModel.posts(it)) }

        viewModel.fetchBoards()

        BoardListSection.defaults.forEach { assertEquals(emptyList<AraPost>(), viewModel.posts(it)) }
    }

    @Test
    fun aFailedRefreshKeepsWhatWasShown() {
        val useCase = useCase(trending = listOf(post(1)))
        val viewModel = viewModel(useCase)
        viewModel.fetchBoards()

        useCase.fetchPostsHandler = { throw TestError.TestFailure() }
        viewModel.fetchBoards()

        assertEquals(listOf(1), viewModel.posts(BoardListSection.Trending)?.map { it.id })
    }

    @Test
    fun loadsOnlyTheChosenSectionsInTheirOrder() {
        val useCase = useCase(trending = listOf(post(1)), talk = listOf(post(9)))
        val viewModel = viewModel(useCase, TestBoardListSectionStore(listOf(talk, BoardListSection.Trending)))

        viewModel.fetchBoards()

        assertEquals(listOf(talk, BoardListSection.Trending), viewModel.sections)
        assertEquals(listOf(9), viewModel.posts(talk)?.map { it.id })
        assertEquals(listOf(1), viewModel.posts(BoardListSection.Trending)?.map { it.id })
        val sectionTypes = useCase.fetchPostsCalls.map { it.type }.filterNot { it is PostListType.Board }
        assertFalse(PostListType.BoardSlug(AraBoard.MARKET_SLUG) in sectionTypes)
        assertFalse(sectionTypes.any { it is PostListType.Latest })
    }

    @Test
    fun startsWithTheDefaultSections() {
        val viewModel = viewModel(useCase())
        assertEquals(listOf(BoardListSection.Trending, BoardListSection.Latest, market), viewModel.sections)
    }

    @Test
    fun asksForMarketBySlugAndLatestWithoutPortalNotice() {
        val useCase = useCase()
        viewModel(useCase).fetchBoards()
        val calls = useCase.fetchPostsCalls

        assertTrue(calls.any { it.type == PostListType.Trending && it.pageSize == BoardListViewModel.HIGHLIGHT_COUNT })
        assertTrue(calls.any { it.type == PostListType.BoardSlug(AraBoard.MARKET_SLUG) && it.pageSize == BoardListViewModel.MARKET_SHELF_COUNT })
        assertTrue(calls.any { it.type == PostListType.Latest(listOf(2, 7)) })
    }

    @Test
    fun boardSectionsLoadWithoutWaitingForTheBoards() {
        val useCase = useCase(talk = listOf(post(9))).apply { fetchBoardsResult = Result.failure(TestError.TestFailure()) }
        val viewModel = viewModel(useCase, TestBoardListSectionStore(listOf(talk)))

        viewModel.fetchBoards()

        assertEquals(listOf(9), viewModel.posts(talk)?.map { it.id })
    }
}
