package org.sparcs.soap.postTests

import androidx.lifecycle.SavedStateHandle
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.sparcs.soap.app.domain.models.ara.AraPost
import org.sparcs.soap.app.domain.models.summarization.PostSummaryPhase
import org.sparcs.soap.app.domain.usecases.summarization.PostSummaryFormat
import org.sparcs.soap.app.domain.usecases.summarization.ReviewSummaryError
import org.sparcs.soap.app.domain.usecases.summarization.ReviewSummaryException
import org.sparcs.soap.app.domain.usecases.summarization.SummarizationResultState
import org.sparcs.soap.app.domain.usecases.summarization.SummarizationUseCase
import org.sparcs.soap.app.domain.usecases.summarization.SummarizationUseCaseProtocol
import org.sparcs.soap.app.domain.usecases.translation.PostTranslationResult
import org.sparcs.soap.app.domain.usecases.translation.PostTranslationUseCaseProtocol
import org.sparcs.soap.app.features.post.PostBody
import org.sparcs.soap.app.features.post.PostViewModel
import org.sparcs.soap.app.features.post.event.PostViewEvent
import org.sparcs.soap.app.shared.mocks.ara.mock
import org.sparcs.soap.app.shared.viewModels.TextProcessingDelegate
import org.sparcs.soap.buddyTestSupport.RecordingAnalyticsService
import org.sparcs.soap.buddyTestSupport.repository.MockAraUserRepository
import org.sparcs.soap.buddyTestSupport.useCase.MockReviewSummaryUseCase

@RunWith(RobolectricTestRunner::class)
class PostSummaryTest : PostTestBase() {

    private class FakeSummarizer : SummarizationUseCaseProtocol {
        var available = true
        var chunks: List<String> = listOf("A short summary.")
        var failure: Exception? = null

        override suspend fun isAvailable(): Boolean = available
        override suspend fun summarise(text: String, isHtml: Boolean) = SummarizationResultState.Unavailable
        override fun streamPostSummary(text: String): Flow<String> = flow {
            chunks.forEach { emit(it) }
            failure?.let { throw it }
        }
    }

    private val translation = object : PostTranslationUseCaseProtocol {
        override fun availableLanguages(): List<String> = emptyList()
        override fun suggestedLanguages(): List<String> = emptyList()
        override fun deviceLanguage(): String = "en"
        override suspend fun translate(text: String, targetLanguage: String, isHtml: Boolean, allowDownload: Boolean) =
            PostTranslationResult.Unsupported
    }

    private val summarizer = FakeSummarizer()
    private val analytics = RecordingAnalyticsService()

    private fun post(length: Int): AraPost {
        val text = "가".repeat(length)
        return AraPost.mock().copy(content = """{"type":"doc","content":[{"type":"paragraph","content":[{"type":"text","text":"$text"}]}]}""")
    }

    private fun viewModel(post: AraPost): PostViewModel {
        mockAraBoardUseCase.fetchPostResult = Result.success(post)
        return PostViewModel(
            savedStateHandle = SavedStateHandle(mapOf("postId" to post.id)),
            araBoardUseCase = mockAraBoardUseCase,
            araCommentUseCase = mockAraCommentUseCase,
            analyticsService = analytics,
            textProcessingDelegate = TextProcessingDelegate(translation, summarizer, MockReviewSummaryUseCase()),
            araUserRepository = MockAraUserRepository(),
        ).apply {
            backgroundDispatcher = Dispatchers.Unconfined
            fetchPost()
        }
    }

    @Test
    fun offersSummariesOnlyForLongPosts() {
        assertTrue(viewModel(post(250)).canSummarise.value)
        assertFalse(viewModel(post(50)).canSummarise.value)

        summarizer.available = false
        assertFalse(viewModel(post(250)).canSummarise.value)
    }

    @Test
    fun showsTheFinishedSummary() {
        summarizer.chunks = listOf("  A short", "  A short summary.\n")
        val viewModel = viewModel(post(250))

        viewModel.summarise()

        assertEquals(PostSummaryPhase.Finished("A short summary."), viewModel.summaryPhase.value)
        assertTrue(PostViewEvent.SummaryRequested.name in analytics.names)
        assertTrue(PostViewEvent.SummaryFinished.name in analytics.names)
    }

    @Test
    fun aSummaryShownIsNotAskedForAgain() {
        val viewModel = viewModel(post(250))
        viewModel.summarise()
        summarizer.chunks = listOf("Another summary.")

        viewModel.summarise()

        assertEquals(PostSummaryPhase.Finished("A short summary."), viewModel.summaryPhase.value)
    }

    @Test
    fun showsWhyItFailedAndTriesAgain() {
        summarizer.chunks = emptyList()
        summarizer.failure = ReviewSummaryException(ReviewSummaryError.UNSAFE_CONTENT)
        val viewModel = viewModel(post(250))

        viewModel.summarise()
        assertEquals(PostSummaryPhase.Failed(ReviewSummaryError.UNSAFE_CONTENT), viewModel.summaryPhase.value)
        assertTrue(PostViewEvent.SummaryFailed(ReviewSummaryError.UNSAFE_CONTENT.name).name in analytics.names)

        summarizer.failure = null
        summarizer.chunks = listOf("Second time lucky.")
        viewModel.summarise()
        assertEquals(PostSummaryPhase.Finished("Second time lucky."), viewModel.summaryPhase.value)
    }

    @Test
    fun anEmptySummaryIsAFailure() {
        summarizer.chunks = listOf(" \n ")
        val viewModel = viewModel(post(250))

        viewModel.summarise()

        assertEquals(PostSummaryPhase.Failed(ReviewSummaryError.FAILED), viewModel.summaryPhase.value)
    }

    @Test
    fun dismissingClearsTheSummary() {
        val viewModel = viewModel(post(250))
        viewModel.summarise()

        viewModel.dismissSummary()

        assertNull(viewModel.summaryPhase.value)
    }

    @Test
    fun countsHTMLTextWithoutItsMarkup() {
        val body = PostBody.Html("""<p class="lead">Hello <b>there</b></p>""", "https://example.com")
        assertEquals("Hellothere".length, body.textLength)
    }

    @Test
    fun cutsOverlongParagraphs() {
        val pieces = SummarizationUseCase.paragraphs("b".repeat(3_200))

        assertEquals(listOf(1_500, 1_500, 200), pieces.map { it.length })
    }

    @Test
    fun writesTheGistThenEachFactOnItsOwnLine() {
        val response = """
            GIST: 기숙사 의자 판매
            FACTS:
            - 3만원
            - 11월 5일까지
            - 카톡 연락
        """.trimIndent()

        assertEquals("기숙사 의자 판매\n• 3만원\n• 11월 5일까지\n• 카톡 연락", PostSummaryFormat.text(response))
    }

    @Test
    fun showsAPartialGistWhileTheFactsAreStillComing() {
        assertEquals("Selling a desk", PostSummaryFormat.text("GIST: Selling a desk\nFACTS:"))
    }

    @Test
    fun leavesOutNoneAndRepeatedFactsAndKeepsAtMostFour() {
        val response = "GIST: Event\nFACTS:\n- a\n- a\n- b\n- c\n- d\n- e"

        assertEquals("Event\n• a\n• b\n• c\n• d", PostSummaryFormat.text(response))
        assertEquals("Question about exams", PostSummaryFormat.text("GIST: Question about exams\nFACTS: None"))
    }

    @Test
    fun stripsMarkdownTheModelAddsAnyway() {
        assertEquals("Job offer\n• Pay 2만원", PostSummaryFormat.text("**GIST:** Job offer\n**FACTS:**\n* Pay 2만원"))
    }

    @Test
    fun treatsAnUnlabelledFirstLineAsTheGist() {
        assertEquals("Lost wallet\n• Near N1", PostSummaryFormat.text("Lost wallet\n- Near N1"))
    }

    @Test
    fun asksForTheGistAndFactsInTheDeviceLanguage() {
        val prompt = SummarizationUseCase.summaryPrompt("본문", "Korean")

        assertTrue(prompt.contains("GIST:"))
        assertTrue(prompt.contains("FACTS:"))
        assertTrue(prompt.contains("Write in Korean"))
        assertTrue(prompt.contains("3만원"))
        assertTrue(prompt.endsWith("본문"))
    }
}
