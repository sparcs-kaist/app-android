package org.sparcs.soap.postTests

import androidx.lifecycle.SavedStateHandle
import kotlinx.coroutines.Dispatchers
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.sparcs.soap.app.domain.models.ara.AraPost
import org.sparcs.soap.app.domain.usecases.summarization.SummarizationResultState
import org.sparcs.soap.app.domain.usecases.summarization.SummarizationUseCaseProtocol
import org.sparcs.soap.app.domain.usecases.translation.PostBatchTranslationResult
import org.sparcs.soap.app.domain.usecases.translation.PostTranslationResult
import org.sparcs.soap.app.domain.usecases.translation.PostTranslationUseCaseProtocol
import org.sparcs.soap.app.features.post.PostTranslationState
import org.sparcs.soap.app.features.post.PostViewModel
import org.sparcs.soap.app.shared.mocks.ara.mock
import org.sparcs.soap.app.shared.viewModels.TextProcessingDelegate
import org.sparcs.soap.buddyTestSupport.repository.MockAraUserRepository
import org.sparcs.soap.buddyTestSupport.useCase.MockReviewSummaryUseCase

@RunWith(RobolectricTestRunner::class)
class PostTranslationTest : PostTestBase() {

    private class FakeTranslator : PostTranslationUseCaseProtocol {
        var result: (List<String>) -> PostBatchTranslationResult = { texts ->
            PostBatchTranslationResult.Success(texts.map { it.uppercase() }, "ko")
        }
        val requests = mutableListOf<List<String>>()

        override fun availableLanguages(): List<String> = listOf("en", "ja", "ko")
        override fun suggestedLanguages(): List<String> = listOf("en")
        override fun deviceLanguage(): String = "en"
        override suspend fun detectLanguage(text: String): String = "ko"
        override suspend fun translate(text: String, targetLanguage: String, isHtml: Boolean, allowDownload: Boolean) =
            PostTranslationResult.Unsupported

        override suspend fun translateAll(
            texts: List<String>,
            sourceLanguage: String?,
            targetLanguage: String,
            allowDownload: Boolean,
        ): PostBatchTranslationResult {
            requests += texts
            return result(texts)
        }
    }

    private val summarization = object : SummarizationUseCaseProtocol {
        override suspend fun isAvailable(): Boolean = false
        override suspend fun summarise(text: String, isHtml: Boolean) = SummarizationResultState.Unavailable
    }

    private val translator = FakeTranslator()

    private val post = AraPost.mock().copy(
        title = "title",
        content = """{"type":"doc","content":[{"type":"paragraph","content":[{"type":"text","text":"hello "},{"type":"text","marks":[{"type":"bold"}],"text":"world"}]},{"type":"codeBlock","content":[{"type":"text","text":"code"}]}]}""",
    )

    private fun viewModel(post: AraPost = this.post): PostViewModel {
        mockAraBoardUseCase.fetchPostResult = Result.success(post)
        return PostViewModel(
            savedStateHandle = SavedStateHandle(mapOf("postId" to post.id)),
            araBoardUseCase = mockAraBoardUseCase,
            araCommentUseCase = mockAraCommentUseCase,
            analyticsService = mockAnalyticsService,
            textProcessingDelegate = TextProcessingDelegate(translator, summarization, MockReviewSummaryUseCase()),
            araUserRepository = MockAraUserRepository(),
        ).apply {
            backgroundDispatcher = Dispatchers.Unconfined
            fetchPost()
        }
    }

    @Test
    fun excludesThePostsOwnLanguageFromTargets() {
        assertEquals(listOf("en", "ja"), viewModel().translationTargets.value)
    }

    @Test
    fun translatesTitleAndBlocksInPlace() {
        val viewModel = viewModel()

        viewModel.translatePost("en")

        val state = viewModel.postTranslationState.value as PostTranslationState.Translated
        assertEquals("TITLE", state.translation.title)
        assertEquals("ko", state.translation.sourceLanguage)
        assertEquals("en", state.translation.targetLanguage)
        assertEquals(listOf("title", "hello world", "world"), translator.requests.single())
    }

    @Test
    fun asksBeforeDownloadingALanguagePack() {
        translator.result = { PostBatchTranslationResult.NeedsDownload("ko", "en") }
        val viewModel = viewModel()

        viewModel.translatePost("en")

        assertEquals(PostTranslationState.DownloadRequired("ko", "en"), viewModel.postTranslationState.value)
    }

    @Test
    fun aFailedTranslationShowsTheOriginalWithAnAlert() {
        translator.result = { PostBatchTranslationResult.Failed(IllegalStateException()) }
        val viewModel = viewModel()

        viewModel.translatePost("en")

        assertEquals(PostTranslationState.Original, viewModel.postTranslationState.value)
        assertTrue(viewModel.isAlertPresented)
    }

    @Test
    fun showOriginalResetsTheTranslation() {
        val viewModel = viewModel()
        viewModel.translatePost("en")

        viewModel.showPostOriginal()

        assertEquals(PostTranslationState.Original, viewModel.postTranslationState.value)
    }

    @Test
    fun editedContentResetsTheTranslation() {
        val viewModel = viewModel()
        viewModel.translatePost("en")

        mockAraBoardUseCase.fetchPostResult = Result.success(post.copy(title = "edited"))
        viewModel.fetchPost()

        assertEquals(PostTranslationState.Original, viewModel.postTranslationState.value)
    }

    @Test
    fun aRefreshWithTheSameContentKeepsTheTranslation() {
        val viewModel = viewModel()
        viewModel.translatePost("en")

        viewModel.fetchPost()

        assertTrue(viewModel.postTranslationState.value is PostTranslationState.Translated)
    }

    @Test
    fun translatesHtmlPostsAsParagraphs() {
        val viewModel = viewModel(post.copy(content = "<div>first</div><div><span>second</span></div>"))

        viewModel.translatePost("en")

        assertTrue(viewModel.postTranslationState.value is PostTranslationState.Translated)
        assertEquals(listOf("title", "first", "second"), translator.requests.single())
    }
}
