package org.sparcs.soap.PostTests

import androidx.lifecycle.SavedStateHandle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.sparcs.soap.App.Domain.Models.Ara.AraPost
import org.sparcs.soap.App.Domain.Models.Ara.AraPostComment
import org.sparcs.soap.App.Features.Post.PostViewModel
import org.sparcs.soap.App.Shared.Mocks.Ara.mock
import org.sparcs.soap.App.Shared.Mocks.Ara.mockList

class PostViewModelTest : PostTestBase() {

    private lateinit var viewModel: PostViewModel

    private fun createViewModel(initialPost: AraPost = AraPost.mock()) {
        val savedStateHandle = SavedStateHandle(mapOf("post" to initialPost))
        viewModel = PostViewModel(
            savedStateHandle = savedStateHandle,
            araBoardUseCase = mockAraBoardUseCase,
            araCommentUseCase = mockAraCommentUseCase,
            crashlyticsService = mockCrashlyticsService,
            analyticsService = mockAnalyticsService
        )
    }

    @Test
    fun `initial alert state is empty`() {
        createViewModel()

        assertNull(viewModel.alertState)
        assertFalse(viewModel.isAlertPresented)
    }

    @Test
    fun `fetchPost success updates post without presenting alert`() = runTest {
        val expectedPost = AraPost.mockList()[1]
        mockAraBoardUseCase.fetchPostResult = Result.success(expectedPost)

        createViewModel()

        viewModel.fetchPost()

        assertEquals(1, mockAraBoardUseCase.fetchPostCallCount)
        assertEquals(expectedPost.id, viewModel.post.value?.id)
        assertNull(viewModel.alertState)
        assertFalse(viewModel.isAlertPresented)
    }

    @Test
    fun `fetchPost failure presents alert state`() = runTest {
        mockAraBoardUseCase.fetchPostResult = Result.failure(Exception("Test failure"))

        createViewModel()

        viewModel.fetchPost()

        assertEquals(1, mockAraBoardUseCase.fetchPostCallCount)
        assertTrue(viewModel.isAlertPresented)
        assertNotNull(viewModel.alertState)
        assertTrue(viewModel.alertState?.message?.contains("Test failure") == true)
    }

    @Test
    fun `writeComment appends comment and increments count`() = runTest {
        val postedComment = AraPostComment.mock()
        mockAraCommentUseCase.writeCommentResult = Result.success(postedComment)

        val initialPost = AraPost.mock().copy(
            comments = mutableListOf(),
            commentCount = 0
        )

        createViewModel(initialPost)

        val createdComment = viewModel.writeComment(content = "Hello")

        assertEquals(postedComment.id, createdComment.id)
        assertEquals(1, viewModel.post.value?.comments?.size)
        assertEquals(1, viewModel.post.value?.commentCount)
    }

    @Test
    fun `deleteComment rolls back content when deletion fails`() = runTest {
        mockAraCommentUseCase.deleteCommentResult = Result.failure(Exception("Test failure"))

        val initialComment = AraPostComment.mock()
        val initialPost = AraPost.mock().copy(
            comments = mutableListOf(initialComment)
        )

        createViewModel(initialPost)

        val previousContent = initialComment.content

        viewModel.deleteComment(comment = initialComment)

        assertEquals(previousContent, viewModel.post.value?.comments?.find { it.id == initialComment.id }?.content)
    }
}