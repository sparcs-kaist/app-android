package org.sparcs.soap.postTests

import androidx.lifecycle.SavedStateHandle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.sparcs.soap.app.domain.models.ara.AraPost
import org.sparcs.soap.app.domain.models.ara.AraPostComment
import org.sparcs.soap.app.domain.usecases.summarization.SummarizationResultState
import org.sparcs.soap.app.domain.usecases.summarization.SummarizationUseCaseProtocol
import org.sparcs.soap.app.domain.usecases.translation.PostTranslationResult
import org.sparcs.soap.app.domain.usecases.translation.PostTranslationUseCaseProtocol
import org.sparcs.soap.app.features.post.PostViewModel
import org.sparcs.soap.app.shared.mocks.ara.mock
import org.sparcs.soap.app.shared.mocks.ara.mockList

class PostViewModelTest : PostTestBase() {

    private lateinit var viewModel: PostViewModel

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

    private fun createViewModel(post: AraPost = AraPost.mock()) {
        val savedStateHandle = SavedStateHandle(mapOf("postId" to post.id))
        viewModel = PostViewModel(
            savedStateHandle = savedStateHandle,
            araBoardUseCase = mockAraBoardUseCase,
            araCommentUseCase = mockAraCommentUseCase,
            analyticsService = mockAnalyticsService,
            postTranslationUseCase = fakeTranslationUseCase,
            summarizationUseCase = fakeSummarizationUseCase
        )
    }

    private fun seedPost(post: AraPost) {
        mockAraBoardUseCase.fetchPostResult = Result.success(post)
        createViewModel(post)
        viewModel.fetchPost()
    }

    @Test
    fun `initial state is loading and no alert`() {
        createViewModel()

        assertEquals(PostViewModel.ViewState.Loading, viewModel.state.value)
        assertNull(viewModel.alertState)
        assertFalse(viewModel.isAlertPresented)
    }

    @Test
    fun `fetchPost success loads post and sets loaded state`() = runTest {
        val expectedPost = AraPost.mockList()[1]
        mockAraBoardUseCase.fetchPostResult = Result.success(expectedPost)

        createViewModel()
        viewModel.fetchPost()

        assertEquals(1, mockAraBoardUseCase.fetchPostCallCount)
        assertEquals(expectedPost.id, viewModel.post.value?.id)
        assertEquals(PostViewModel.ViewState.Loaded, viewModel.state.value)
    }

    @Test
    fun `fetchPost failure sets error state`() = runTest {
        mockAraBoardUseCase.fetchPostResult = Result.failure(Exception("Test failure"))

        createViewModel()
        viewModel.fetchPost()

        assertEquals(1, mockAraBoardUseCase.fetchPostCallCount)
        assertTrue(viewModel.state.value is PostViewModel.ViewState.Error)
    }

    @Test
    fun `writeComment appends comment and increments count`() = runTest {
        val initialPost = AraPost.mock().copy(
            comments = mutableListOf(),
            commentCount = 0
        )
        seedPost(initialPost)

        val postedComment = AraPostComment.mock()
        mockAraCommentUseCase.writeCommentResult = Result.success(postedComment)

        val createdComment = viewModel.writeComment(content = "Hello")

        assertEquals(postedComment.id, createdComment?.id)
        assertEquals(1, viewModel.post.value?.comments?.size)
        assertEquals(1, viewModel.post.value?.commentCount)
    }

    @Test
    fun `deleteComment rolls back content when deletion fails`() = runTest {
        val initialComment = AraPostComment.mock()
        val initialPost = AraPost.mock().copy(
            comments = mutableListOf(initialComment)
        )
        seedPost(initialPost)

        mockAraCommentUseCase.deleteCommentResult = Result.failure(Exception("Test failure"))
        val previousContent = initialComment.content

        viewModel.deleteComment(comment = initialComment)

        assertEquals(
            previousContent,
            viewModel.post.value?.comments?.find { it.id == initialComment.id }?.content
        )
    }
}
