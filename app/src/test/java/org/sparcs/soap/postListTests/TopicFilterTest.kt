package org.sparcs.soap.postListTests

import android.net.Uri
import androidx.lifecycle.SavedStateHandle
import com.google.gson.Gson
import kotlinx.coroutines.CompletableDeferred
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.sparcs.soap.app.domain.enums.ara.PostListType
import org.sparcs.soap.app.domain.helpers.LocalizedString
import org.sparcs.soap.app.domain.models.ara.AraBoard
import org.sparcs.soap.app.domain.models.ara.AraBoardTopic
import org.sparcs.soap.app.domain.models.ara.AraPost
import org.sparcs.soap.app.domain.models.ara.AraPostPage
import org.sparcs.soap.app.features.postList.PostListViewModel
import org.sparcs.soap.app.shared.mocks.ara.mock
import org.sparcs.soap.buddyTestSupport.MockAnalyticsService
import org.sparcs.soap.buddyTestSupport.helper.TestBoardSeenPostsStore
import org.sparcs.soap.buddyTestSupport.useCase.MockAraBoardUseCase
import org.sparcs.soap.testSupport.MainDispatcherRule

@RunWith(RobolectricTestRunner::class)
class TopicFilterTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val useCase = MockAraBoardUseCase()
    private val seenPosts = TestBoardSeenPostsStore()
    private val topic = AraBoardTopic(7, "sell", LocalizedString(mapOf("ko" to "판매", "en" to "Sell")))
    private val board = AraBoard.mock().copy(topics = listOf(topic))

    private val viewModel = PostListViewModel(
        savedStateHandle = SavedStateHandle(mapOf("board_json" to Uri.encode(Gson().toJson(board)))),
        araBoardUseCase = useCase,
        analyticsService = MockAnalyticsService(),
        seenPostsStore = seenPosts,
    )

    private fun page(vararg ids: Int) = AraPostPage(
        pages = 1,
        items = ids.size,
        currentPage = 1,
        results = ids.map { AraPost.mock().copy(id = it) }
    )

    @Test
    fun selectingATopicLoadsOnlyItsPosts() {
        viewModel.selectTopic(topic)
        assertEquals(PostListType.Board(board.id, topicID = 7), useCase.fetchedTypes.last())
        viewModel.selectTopic(null)
        assertEquals(PostListType.Board(board.id, topicID = null), useCase.fetchedTypes.last())
    }

    @Test
    fun reselectingTheSameTopicDoesNotReload() {
        viewModel.selectTopic(topic)
        viewModel.selectTopic(topic)
        assertEquals(1, useCase.fetchedTypes.size)
    }

    @Test
    fun dropsAPageThatArrivesForAnEarlierTopic() {
        val earlier = CompletableDeferred<AraPostPage>()
        val later = CompletableDeferred<AraPostPage>()
        useCase.fetchPostsGates += earlier
        useCase.fetchPostsGates += later

        viewModel.fetchInitialPosts()
        viewModel.selectTopic(topic)
        later.complete(page(2))
        earlier.complete(page(1))

        val state = viewModel.state.value as PostListViewModel.ViewState.Loaded
        assertEquals(listOf(2), state.posts.map { it.id })
    }
}
