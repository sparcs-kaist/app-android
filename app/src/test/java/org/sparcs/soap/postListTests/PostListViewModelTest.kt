package org.sparcs.soap.postListTests

import android.net.Uri
import androidx.lifecycle.SavedStateHandle
import com.google.gson.Gson
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.sparcs.soap.app.domain.models.ara.AraBoard
import org.sparcs.soap.app.domain.models.ara.AraPost
import org.sparcs.soap.app.domain.models.ara.AraPostPage
import org.sparcs.soap.app.features.postList.PostListViewModel
import org.sparcs.soap.app.shared.mocks.ara.mock
import org.sparcs.soap.app.shared.mocks.ara.mockList
import org.sparcs.soap.buddyTestSupport.MockAnalyticsService
import org.sparcs.soap.buddyTestSupport.useCase.MockAraBoardUseCase
import org.sparcs.soap.testSupport.MainDispatcherRule

@RunWith(RobolectricTestRunner::class)
class PostListViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private lateinit var mockAraBoardUseCase: MockAraBoardUseCase
    private lateinit var viewModel: PostListViewModel

    @Before
    fun setup() {
        mockAraBoardUseCase = MockAraBoardUseCase()
    }

    private fun createViewModel(board: AraBoard = AraBoard.mock()) {
        val boardJson = Uri.encode(Gson().toJson(board))
        val savedStateHandle = SavedStateHandle(mapOf("board_json" to boardJson))
        viewModel = PostListViewModel(
            savedStateHandle = savedStateHandle,
            araBoardUseCase = mockAraBoardUseCase,
            analyticsService = MockAnalyticsService(),
        )
    }

    private fun page(results: List<AraPost>, pages: Int, currentPage: Int) =
        AraPostPage(pages = pages, items = results.size, currentPage = currentPage, results = results)

    @Test
    fun `initial state is loading`() {
        createViewModel()
        assertEquals(PostListViewModel.ViewState.Loading, viewModel.state.value)
    }

    @Test
    fun `fetchInitialPosts success loads posts and flags more pages`() = runTest {
        val posts = AraPost.mockList().take(3)
        mockAraBoardUseCase.fetchPostsResult = Result.success(page(posts, pages = 2, currentPage = 1))
        createViewModel()

        viewModel.fetchInitialPosts()

        val state = viewModel.state.value
        assertTrue(state is PostListViewModel.ViewState.Loaded)
        state as PostListViewModel.ViewState.Loaded
        assertEquals(posts.map { it.id }, state.posts.map { it.id })
        assertTrue(viewModel.hasMorePages) // currentPage 1 < totalPages 2
        assertEquals(1, mockAraBoardUseCase.fetchPostsCallCount)
    }

    @Test
    fun `fetchInitialPosts on last page has no more pages`() = runTest {
        val posts = AraPost.mockList().take(2)
        mockAraBoardUseCase.fetchPostsResult = Result.success(page(posts, pages = 1, currentPage = 1))
        createViewModel()

        viewModel.fetchInitialPosts()

        assertFalse(viewModel.hasMorePages)
    }

    @Test
    fun `fetchInitialPosts failure sets error state`() = runTest {
        mockAraBoardUseCase.fetchPostsResult = Result.failure(Exception("Test failure"))
        createViewModel()

        viewModel.fetchInitialPosts()

        assertTrue(viewModel.state.value is PostListViewModel.ViewState.Error)
    }

    @Test
    fun `removePost removes the post from the loaded list`() = runTest {
        val posts = AraPost.mockList().take(3)
        mockAraBoardUseCase.fetchPostsResult = Result.success(page(posts, pages = 1, currentPage = 1))
        createViewModel()
        viewModel.fetchInitialPosts()

        val removedId = posts.first().id
        viewModel.removePost(removedId)

        assertTrue(viewModel.posts.none { it.id == removedId })
        assertEquals(posts.size - 1, viewModel.posts.size)
    }
}
