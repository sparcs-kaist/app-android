package org.sparcs.soap.feedPostTests

import androidx.lifecycle.SavedStateHandle
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.sparcs.soap.app.domain.models.feed.FeedComment
import org.sparcs.soap.app.domain.models.feed.FeedPost
import org.sparcs.soap.app.domain.usecases.summarization.SummarizationResultState
import org.sparcs.soap.app.domain.usecases.summarization.SummarizationUseCaseProtocol
import org.sparcs.soap.app.domain.usecases.translation.PostTranslationResult
import org.sparcs.soap.app.domain.usecases.translation.PostTranslationUseCaseProtocol
import org.sparcs.soap.app.features.feedPost.FeedPostViewModel
import org.sparcs.soap.buddyTestSupport.MockAnalyticsService
import org.sparcs.soap.buddyTestSupport.MockCrashlyticsService
import org.sparcs.soap.buddyTestSupport.helper.UseCaseTestFixtures
import org.sparcs.soap.buddyTestSupport.useCase.MockFeedCommentUseCase
import org.sparcs.soap.buddyTestSupport.useCase.MockFeedPostUseCase
import org.sparcs.soap.buddyTestSupport.useCase.MockUserUseCase
import org.sparcs.soap.testSupport.MainDispatcherRule

class FeedPostViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private lateinit var mockFeedPostUseCase: MockFeedPostUseCase
    private lateinit var mockFeedCommentUseCase: MockFeedCommentUseCase
    private lateinit var viewModel: FeedPostViewModel

    private val fakeTranslationUseCase = object : PostTranslationUseCaseProtocol {
        override fun availableLanguages(): List<String> = emptyList()
        override fun suggestedLanguages(): List<String> = emptyList()
        override fun deviceLanguage(): String = "en"
        override suspend fun translate(
            text: String,
            targetLanguage: String,
            isHtml: Boolean,
            allowDownload: Boolean,
        ) = PostTranslationResult.Unsupported
    }

    private val fakeSummarizationUseCase = object : SummarizationUseCaseProtocol {
        override suspend fun isAvailable(): Boolean = false
        override suspend fun summarise(text: String, isHtml: Boolean) =
            SummarizationResultState.Unavailable
    }

    @Before
    fun setup() {
        mockFeedPostUseCase = MockFeedPostUseCase()
        mockFeedCommentUseCase = MockFeedCommentUseCase()
    }
    private fun createViewModel(feedId: String = "1") {
        viewModel = FeedPostViewModel(
            savedStateHandle = SavedStateHandle(mapOf("feedId" to feedId)),
            feedCommentUseCase = mockFeedCommentUseCase,
            feedPostUseCase = mockFeedPostUseCase,
            userUseCase = MockUserUseCase(),
            crashlyticsService = MockCrashlyticsService(),
            analyticsService = MockAnalyticsService(),
            postTranslationUseCase = fakeTranslationUseCase,
            summarizationUseCase = fakeSummarizationUseCase,
        )
    }

    @Test
    fun `init loads post and comments into loaded state`() = runTest {
        val post = UseCaseTestFixtures.makePost()
        val comments = listOf(UseCaseTestFixtures.makeComment())
        mockFeedPostUseCase.fetchPostResult = Result.success(post)
        mockFeedCommentUseCase.fetchCommentsResult = Result.success(comments)

        createViewModel()

        val state = viewModel.state.first { it !is FeedPostViewModel.ViewState.Loading }

        assertTrue("Expected Loaded state but was $state", state is FeedPostViewModel.ViewState.Loaded)
        assertEquals(post.id, viewModel.post?.id)
        assertEquals(comments.size, viewModel.comments.size)
    }

    @Test
    fun `init failure sets error state`() = runTest {
        mockFeedPostUseCase.fetchPostResult = Result.failure(Exception("Test failure"))

        createViewModel()

        val state = viewModel.state.first { it !is FeedPostViewModel.ViewState.Loading }

        assertTrue("Expected Error state but was $state", state is FeedPostViewModel.ViewState.Error)
    }

    @Test
    fun `submitComment appends the comment and clears the text`() = runTest {
        mockFeedPostUseCase.fetchPostResult = Result.success(UseCaseTestFixtures.makePost())
        mockFeedCommentUseCase.fetchCommentsResult = Result.success(emptyList())
        val posted = UseCaseTestFixtures.makeComment()
        mockFeedCommentUseCase.writeCommentResult = Result.success(posted)
        createViewModel()
        advanceUntilIdle()

        viewModel.text = "hello"
        val created = viewModel.submitComment(postID = "1", replyingTo = null)

        assertEquals(posted.id, created?.id)
        assertTrue(viewModel.comments.any { it.id == posted.id })
        assertEquals("", viewModel.text)
    }
}
