package org.sparcs.soap.postListTests

import android.net.Uri
import androidx.lifecycle.SavedStateHandle
import com.google.gson.Gson
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.sparcs.soap.app.domain.models.ara.AraPost
import org.sparcs.soap.app.domain.models.ara.AraPostPage
import org.sparcs.soap.app.domain.models.ara.AraUser
import org.sparcs.soap.app.features.search.SearchViewModel
import org.sparcs.soap.app.features.settings.ara.AraMyPostViewModel
import org.sparcs.soap.app.features.userPostList.UserPostListViewModel
import org.sparcs.soap.app.shared.mocks.ara.mock
import org.sparcs.soap.buddyTestSupport.MockAnalyticsService
import org.sparcs.soap.buddyTestSupport.repository.MockTaxiRoomRepository
import org.sparcs.soap.buddyTestSupport.useCase.MockAraBoardUseCase
import org.sparcs.soap.buddyTestSupport.useCase.MockCourseUseCase
import org.sparcs.soap.buddyTestSupport.useCase.MockTaxiLocationUseCase
import org.sparcs.soap.buddyTestSupport.useCase.MockUserUseCase
import org.sparcs.soap.testSupport.MainDispatcherRule

@RunWith(RobolectricTestRunner::class)
class MarkReadTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val useCase = MockAraBoardUseCase().apply {
        fetchPostsResult = Result.success(page(AraPost.ReadStatus.UNREAD, AraPost.ReadStatus.UPDATED))
        fetchBookmarksResult = fetchPostsResult
    }

    private fun page(vararg statuses: AraPost.ReadStatus) = AraPostPage(
        pages = 1,
        items = statuses.size,
        currentPage = 1,
        results = statuses.mapIndexed { index, status -> AraPost.mock().copy(id = index + 1, readStatus = status) }
    )

    private fun List<AraPost>.statuses() = map { it.readStatus }

    private val greyedFirst = listOf(AraPost.ReadStatus.READ, AraPost.ReadStatus.UPDATED)
    private val greyedBoth = listOf(AraPost.ReadStatus.READ, AraPost.ReadStatus.READ)

    @Test
    fun userPostListGreysOutTheOpenedPost() {
        val viewModel = UserPostListViewModel(
            savedStateHandle = SavedStateHandle(
                mapOf("author_json" to Uri.encode(Gson().toJson(AraPost.mock().author.copy(id = "42"))))
            ),
            araBoardUseCase = useCase,
            analyticsService = MockAnalyticsService(),
        )
        viewModel.fetchInitialPosts()

        viewModel.markRead(1)
        assertEquals(greyedFirst, viewModel.posts.value.statuses())
        viewModel.markRead(2)
        viewModel.markRead(99)
        assertEquals(greyedBoth, (viewModel.state.value as UserPostListViewModel.ViewState.Loaded).posts.statuses())
    }

    @Test
    fun myPostsGreyOutTheOpenedPost() = runTest {
        AraMyPostViewModel.PostType.entries.forEach { type ->
            val viewModel = AraMyPostViewModel(
                savedStateHandle = SavedStateHandle(mapOf("type_json" to Uri.encode(Gson().toJson(type)))),
                userUseCase = MockUserUseCase().apply { araUser = AraUser(42, "me", null, false, false) },
                araBoardUseCase = useCase,
            )
            viewModel.fetchInitialPosts()

            viewModel.markRead(1)
            assertEquals(greyedFirst, viewModel.posts.value.statuses())
            viewModel.markRead(2)
            viewModel.markRead(99)
            assertEquals(greyedBoth, (viewModel.state.value as AraMyPostViewModel.ViewState.Loaded).posts.statuses())
        }
    }

    @Test
    fun searchGreysOutTheOpenedPost() = runTest {
        val viewModel = SearchViewModel(
            araBoardUseCase = useCase,
            taxiRoomRepository = MockTaxiRoomRepository(),
            taxiLocationUseCase = MockTaxiLocationUseCase(),
            courseUseCase = MockCourseUseCase(),
        )
        viewModel.onSearchTextChange("post")
        viewModel.fetchInitialData()

        viewModel.markRead(1)
        assertEquals(greyedFirst, viewModel.posts.value.statuses())
        viewModel.markRead(2)
        viewModel.markRead(99)
        assertEquals(greyedBoth, viewModel.posts.value.statuses())
    }
}
