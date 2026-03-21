package org.sparcs.soap.FeedTests

import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.sparcs.soap.App.Domain.Models.Feed.FeedPost
import org.sparcs.soap.App.Features.Feed.FeedViewModel
import org.sparcs.soap.App.Shared.Mocks.Ara.mockList
import org.sparcs.soap.BuddyTestSupport.Error.TestError
import org.sparcs.soap.BuddyTestSupport.Helper.FeedTestFixtures

class FeedViewModelTests : FeedTestBase() {

    private lateinit var viewModel: FeedViewModel

    @Before
    override fun setup() {
        super.setup()
        viewModel = FeedViewModel(
            feedPostUseCase = mockFeedPostUseCase,
            analyticsService = mockAnalyticsService,
            crashlyticsService = mockCrashlyticsService
        )
    }

    @Test
    fun `Initial state is loading`() {
        assertEquals(FeedViewModel.ViewState.Loading, viewModel.state)
        assertTrue(viewModel.posts.isEmpty())
    }

    @Test
    fun `fetchInitialData loads posts and sets state to loaded`() = runTest {
        val posts = listOf(
            FeedTestFixtures.makePost(id = "1"),
            FeedTestFixtures.makePost(id = "2")
        )
        mockFeedPostUseCase.fetchPostsResult = Result.success(
            FeedTestFixtures.makePostPage(
                posts = posts,
                nextCursor = "cursor-1",
                hasNext = true
            )
        )

        viewModel.fetchInitialData()

        assertEquals(FeedViewModel.ViewState.Loaded(FeedPost.mockList()), viewModel.state)
        assertEquals(2, viewModel.posts.size)
        assertEquals("1", viewModel.posts[0].id)
        assertEquals("2", viewModel.posts[1].id)
        assertEquals(1, mockFeedPostUseCase.fetchPostsCallCount)
    }

    @Test
    fun `fetchInitialData error sets state to error`() = runTest {
        mockFeedPostUseCase.fetchPostsResult = Result.failure(TestError.TestFailure)

        viewModel.fetchInitialData()

        val currentState = viewModel.state.value
        if (currentState is FeedViewModel.ViewState.Error) {
            assertTrue(currentState.message.contains("Test failure") || currentState.message.isNotEmpty())
        } else {
            throw AssertionError("Expected error state but was $currentState")
        }
    }

    @Test
    fun `deletePost removes post from list on success`() = runTest {
        viewModel.posts = mutableListOf(
            FeedTestFixtures.makePost(id = "1"),
            FeedTestFixtures.makePost(id = "2"),
            FeedTestFixtures.makePost(id = "3")
        )

        viewModel.deletePost(postID = "2")

        assertEquals(2, viewModel.posts.size)
        assertTrue(viewModel.posts.none { it.id == "2" })
        assertEquals(1, mockFeedPostUseCase.deletePostCallCount)
        assertEquals("2", mockFeedPostUseCase.lastDeletePostID)
    }

    @Test
    fun `deletePost shows alert on error`() = runTest {
        mockFeedPostUseCase.deletePostResult = Result.failure(TestError.TestFailure)
        viewModel.posts = mutableListOf(
            FeedTestFixtures.makePost(id = "1")
        )

        viewModel.deletePost(postID = "1")

        assertEquals(1, viewModel.posts.size)
        assertTrue(viewModel.isAlertPresented)
        assertTrue(viewModel.alertState != null)
    }
}