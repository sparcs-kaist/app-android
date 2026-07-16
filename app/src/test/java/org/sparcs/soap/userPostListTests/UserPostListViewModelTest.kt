package org.sparcs.soap.userPostListTests

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
import org.sparcs.soap.app.domain.models.ara.AraPost
import org.sparcs.soap.app.domain.models.ara.AraPostAuthor
import org.sparcs.soap.app.domain.models.ara.AraPostPage
import org.sparcs.soap.app.features.userPostList.UserPostListViewModel
import org.sparcs.soap.app.shared.mocks.ara.mock
import org.sparcs.soap.app.shared.mocks.ara.mockList
import org.sparcs.soap.buddyTestSupport.MockAnalyticsService
import org.sparcs.soap.buddyTestSupport.useCase.MockAraBoardUseCase
import org.sparcs.soap.testSupport.MainDispatcherRule

@RunWith(RobolectricTestRunner::class)
class UserPostListViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private lateinit var mockAraBoardUseCase: MockAraBoardUseCase
    private lateinit var viewModel: UserPostListViewModel

    @Before
    fun setup() {
        mockAraBoardUseCase = MockAraBoardUseCase()
    }

    private fun author(id: String = "42"): AraPostAuthor = AraPost.mock().author.copy(id = id)

    private fun createViewModel(author: AraPostAuthor = author()) {
        val authorJson = Uri.encode(Gson().toJson(author))
        val savedStateHandle = SavedStateHandle(mapOf("author_json" to authorJson))
        viewModel = UserPostListViewModel(
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
        assertEquals(UserPostListViewModel.ViewState.Loading, viewModel.state.value)
    }

    @Test
    fun `fetchInitialPosts success loads posts and flags more pages`() = runTest {
        val posts = AraPost.mockList().take(3)
        mockAraBoardUseCase.fetchPostsResult = Result.success(page(posts, pages = 2, currentPage = 1))
        createViewModel()

        viewModel.fetchInitialPosts()

        val state = viewModel.state.value
        assertTrue(state is UserPostListViewModel.ViewState.Loaded)
        assertEquals(posts.map { it.id }, viewModel.posts.value.map { it.id })
        assertTrue(viewModel.hasMorePages)
        assertEquals(1, mockAraBoardUseCase.fetchPostsCallCount)
    }

    @Test
    fun `fetchInitialPosts failure sets error state`() = runTest {
        mockAraBoardUseCase.fetchPostsResult = Result.failure(Exception("Test failure"))
        createViewModel()

        viewModel.fetchInitialPosts()

        assertTrue(viewModel.state.value is UserPostListViewModel.ViewState.Error)
    }

    @Test
    fun `fetchInitialPosts does nothing when author id is not numeric`() = runTest {
        createViewModel(author(id = "not-a-number"))

        viewModel.fetchInitialPosts()

        assertEquals(UserPostListViewModel.ViewState.Loading, viewModel.state.value)
        assertEquals(0, mockAraBoardUseCase.fetchPostsCallCount)
    }

    @Test
    fun `removePost removes the post from the loaded list`() = runTest {
        val posts = AraPost.mockList().take(3)
        mockAraBoardUseCase.fetchPostsResult = Result.success(page(posts, pages = 1, currentPage = 1))
        createViewModel()
        viewModel.fetchInitialPosts()

        val removedId = posts.first().id
        viewModel.removePost(removedId)

        assertTrue(viewModel.posts.value.none { it.id == removedId })
        assertEquals(posts.size - 1, viewModel.posts.value.size)
        assertFalse(viewModel.state.value is UserPostListViewModel.ViewState.Loading)
    }
}
