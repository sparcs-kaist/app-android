package org.sparcs.soap.postListTests

import android.net.Uri
import androidx.lifecycle.SavedStateHandle
import com.google.gson.Gson
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.sparcs.soap.app.domain.helpers.LocalizedString
import org.sparcs.soap.app.domain.models.ara.AraBoard
import org.sparcs.soap.app.domain.models.ara.AraBoardTopic
import org.sparcs.soap.app.domain.models.ara.AraPost
import org.sparcs.soap.app.domain.models.ara.AraPostPage
import org.sparcs.soap.app.features.postList.PostListViewModel
import org.sparcs.soap.app.features.postList.event.PostListViewEvent
import org.sparcs.soap.app.shared.mocks.ara.mock
import org.sparcs.soap.app.shared.mocks.ara.mockList
import org.sparcs.soap.buddyTestSupport.RecordingAnalyticsService
import org.sparcs.soap.buddyTestSupport.helper.TestBoardSeenPostsStore
import org.sparcs.soap.buddyTestSupport.useCase.MockAraBoardUseCase
import org.sparcs.soap.testSupport.MainDispatcherRule

@RunWith(RobolectricTestRunner::class)
class PostListViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private lateinit var mockAraBoardUseCase: MockAraBoardUseCase
    private lateinit var viewModel: PostListViewModel
    private val analytics = RecordingAnalyticsService()
    private val seenPosts = TestBoardSeenPostsStore()
    private val topic = AraBoardTopic(7, "sell", LocalizedString(mapOf("ko" to "판매", "en" to "Sell")))

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
            analyticsService = analytics,
            seenPostsStore = seenPosts,
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
        assertTrue(viewModel.hasMorePages)
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

    private fun posts(vararg ids: Int, readStatus: AraPost.ReadStatus? = AraPost.ReadStatus.UNREAD) =
        ids.map { AraPost.mock().copy(id = it, readStatus = readStatus) }

    private fun loadBoard(posts: List<AraPost>) {
        mockAraBoardUseCase.fetchPostsResult = Result.success(page(posts, pages = 1, currentPage = 1))
        createViewModel()
        viewModel.fetchInitialPosts()
    }

    private fun loadedPosts() = (viewModel.state.value as PostListViewModel.ViewState.Loaded).posts

    @Test
    fun markReadGreysOutTheOpenedPostOnly() {
        loadBoard(posts(1, 2))

        viewModel.markRead(1)

        assertEquals(AraPost.ReadStatus.READ, loadedPosts().first { it.id == 1 }.readStatus)
        assertEquals(AraPost.ReadStatus.UNREAD, loadedPosts().first { it.id == 2 }.readStatus)
    }

    @Test
    fun markReadClearsTheUpdatedMarker() {
        loadBoard(posts(1, readStatus = AraPost.ReadStatus.UPDATED))

        viewModel.markRead(1)

        assertEquals(AraPost.ReadStatus.READ, viewModel.posts.single().readStatus)
    }

    @Test
    fun markReadIgnoresAPostNotInTheList() {
        loadBoard(posts(1))
        val before = viewModel.state.value

        viewModel.markRead(99)

        assertTrue(before === viewModel.state.value)
    }

    @Test
    fun refreshItemCarriesTheReadStatusBack() {
        loadBoard(posts(1))
        mockAraBoardUseCase.fetchPostResult = Result.success(
            AraPost.mock().copy(id = 1, commentCount = 5, readStatus = AraPost.ReadStatus.READ)
        )

        viewModel.refreshItem(1)

        assertEquals(AraPost.ReadStatus.READ, viewModel.posts.single().readStatus)
        assertEquals(5, viewModel.posts.single().commentCount)
    }

    @Test
    fun refreshItemKeepsAPostMarkedReadWhenTheDetailOmitsItsStatus() {
        loadBoard(posts(1))
        viewModel.markRead(1)
        mockAraBoardUseCase.fetchPostResult = Result.success(AraPost.mock().copy(id = 1, readStatus = null))

        viewModel.refreshItem(1)

        assertEquals(AraPost.ReadStatus.READ, viewModel.posts.single().readStatus)
    }

    @Test
    fun seeingTheWholeBoardMarksItsNewestPostSeen() {
        loadBoard(posts(40, 42, 41))

        assertEquals(42, seenPosts.newestSeen(AraBoard.mock().id))
    }

    @Test
    fun aTopicOrSearchDoesNotCountAsSeeingTheBoard() {
        mockAraBoardUseCase.fetchPostsResult = Result.success(page(posts(42), pages = 1, currentPage = 1))
        createViewModel(AraBoard.mock().copy(topics = listOf(topic)))

        viewModel.onSearchTextChange("keyword")
        viewModel.fetchInitialPosts()
        assertNull(seenPosts.newestSeen(AraBoard.mock().id))

        viewModel.selectTopic(topic)
        viewModel.onSearchTextChange("")
        viewModel.fetchInitialPosts()
        assertNull(seenPosts.newestSeen(AraBoard.mock().id))
    }

    @Test
    fun choosingATopicLogsItsSlug() {
        createViewModel(AraBoard.mock().copy(topics = listOf(topic)))

        viewModel.selectTopic(topic)
        viewModel.selectTopic(topic)
        viewModel.selectTopic(null)

        val selections = analytics.events.filterIsInstance<PostListViewEvent.TopicSelected>()
        assertEquals(listOf("topic_selected", "topic_selected"), selections.map { it.name })
        assertEquals(listOf("sell", "all"), selections.map { it.parameters["topic"] })
        assertEquals("PostListView", selections.first().parameters["source"])
    }
}
