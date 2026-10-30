package org.sparcs.soap.feedPostTests

import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.isActive
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotSame
import org.junit.Assert.assertNull
import org.junit.Assert.assertSame
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.sparcs.soap.app.domain.models.feed.FeedComment
import org.sparcs.soap.app.domain.models.feed.FeedCreateComment
import org.sparcs.soap.app.domain.usecases.feed.FeedCommentUseCaseProtocol
import org.sparcs.soap.app.domain.usecases.summarization.SummarizationResultState
import org.sparcs.soap.app.domain.usecases.summarization.SummarizationUseCaseProtocol
import org.sparcs.soap.app.domain.usecases.translation.PostTranslationResult
import org.sparcs.soap.app.domain.usecases.translation.PostTranslationUseCaseProtocol
import org.sparcs.soap.app.features.feed.ActiveFeedPostViewModel
import org.sparcs.soap.app.features.feedPost.FeedPostViewModel
import org.sparcs.soap.app.shared.viewModels.TextProcessingDelegate
import org.sparcs.soap.buddyTestSupport.MockAnalyticsService
import org.sparcs.soap.buddyTestSupport.MockCrashlyticsService
import org.sparcs.soap.buddyTestSupport.helper.UseCaseTestFixtures
import org.sparcs.soap.buddyTestSupport.useCase.MockFeedCommentUseCase
import org.sparcs.soap.buddyTestSupport.useCase.MockFeedPostUseCase
import org.sparcs.soap.buddyTestSupport.useCase.MockReviewSummaryUseCase
import org.sparcs.soap.buddyTestSupport.useCase.MockUserUseCase
import org.sparcs.soap.testSupport.MainDispatcherRule

@OptIn(ExperimentalCoroutinesApi::class)
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
    private fun createViewModel(
        feedId: String = "1",
        commentUseCase: FeedCommentUseCaseProtocol = mockFeedCommentUseCase,
    ): FeedPostViewModel {
        viewModel = FeedPostViewModel(
            feedId = feedId,
            feedCommentUseCase = commentUseCase,
            feedPostUseCase = mockFeedPostUseCase,
            userUseCase = MockUserUseCase(),
            crashlyticsService = MockCrashlyticsService(),
            analyticsService = MockAnalyticsService(),
            textProcessingDelegate = TextProcessingDelegate(fakeTranslationUseCase, fakeSummarizationUseCase, MockReviewSummaryUseCase()),
        )
        return viewModel
    }

    private fun activePost(commentUseCase: FeedCommentUseCaseProtocol = mockFeedCommentUseCase) =
        ActiveFeedPostViewModel(object : FeedPostViewModel.Factory {
            override fun create(feedId: String) = createViewModel(feedId, commentUseCase)
        })

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
    @Test
    fun `active post reuses the same model and draft across presentations`() = runTest {
        val active = activePost()
        val first = active["1"]!!
        advanceUntilIdle()
        first.text = "draft"
        first.targetComment = UseCaseTestFixtures.makeComment()
        first.isAnonymous = false

        val second = active["1"]!!
        assertSame(first, second)
        assertEquals("draft", second.text)
        assertEquals(first.targetComment, second.targetComment)
        assertFalse(second.isAnonymous)
        assertEquals(1, mockFeedPostUseCase.fetchPostCallCount)
    }

    @Test
    fun `switching or closing disposes the model instead of resetting its fields`() = runTest {
        val active = activePost()
        val first = active["1"]!!
        first.text = "draft"
        first.targetComment = UseCaseTestFixtures.makeComment()
        first.isAnonymous = false

        val second = active["2"]!!
        advanceUntilIdle()
        assertNotSame(first, second)
        assertFalse(first.viewModelScope.isActive)
        assertEquals("", second.text)
        assertNull(second.targetComment)
        assertTrue(second.isAnonymous)

        assertNull(active[null])
        assertFalse(second.viewModelScope.isActive)
        assertNotSame(second, active["2"])
    }

    @Test
    fun `retry reloads post after initial fetch failure`() = runTest {
        mockFeedPostUseCase.fetchPostResult = Result.failure(Exception("offline"))
        createViewModel()
        advanceUntilIdle()
        mockFeedPostUseCase.fetchPostResult = Result.success(UseCaseTestFixtures.makePost())

        viewModel.retry()
        advanceUntilIdle()

        assertTrue(viewModel.state.value is FeedPostViewModel.ViewState.Loaded)
        assertEquals(2, mockFeedPostUseCase.fetchPostCallCount)
    }

    @Test
    fun `submission survives the old composer leaving composition`() = runTest {
        val pending = CompletableDeferred<FeedComment>()
        val useCase = object : FeedCommentUseCaseProtocol by mockFeedCommentUseCase {
            override suspend fun writeComment(postID: String, request: FeedCreateComment) = pending.await()
        }
        createViewModel(commentUseCase = useCase)
        advanceUntilIdle()
        val previousCount = viewModel.post!!.commentCount
        viewModel.text = "send while rotating"
        val caller = async { viewModel.submitComment("1", null) }
        runCurrent()
        caller.cancel()
        val uploaded = UseCaseTestFixtures.makeComment()
        pending.complete(uploaded)
        advanceUntilIdle()

        assertEquals("", viewModel.text)
        assertTrue(viewModel.comments.any { it.id == uploaded.id })
        assertEquals(previousCount + 1, viewModel.post!!.commentCount)
        assertFalse(viewModel.isSubmittingComment)
    }

    @Test
    fun `switching posts cancels old comment fetch`() = runTest {
        val pending = CompletableDeferred<List<FeedComment>>()
        val useCase = object : FeedCommentUseCaseProtocol by mockFeedCommentUseCase {
            override suspend fun fetchComments(postID: String): List<FeedComment> =
                if (postID == "1") pending.await() else emptyList()
        }
        val active = activePost(useCase)
        val first = active["1"]!!
        runCurrent()
        val second = active["2"]!!
        pending.complete(listOf(UseCaseTestFixtures.makeComment()))
        advanceUntilIdle()

        assertFalse(first.viewModelScope.isActive)
        assertTrue(second.comments.isEmpty())
        assertFalse(second.isLoadingComments)
        assertTrue(second.state.value is FeedPostViewModel.ViewState.Loaded)
    }

}
