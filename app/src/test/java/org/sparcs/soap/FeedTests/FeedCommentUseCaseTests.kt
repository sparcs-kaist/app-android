package org.sparcs.soap.FeedTests

import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Assert.fail
import org.junit.Before
import org.junit.Test
import org.sparcs.soap.App.Domain.Enums.Feed.FeedReportType
import org.sparcs.soap.App.Domain.Enums.Feed.FeedVoteType
import org.sparcs.soap.App.Domain.Error.Feed.FeedCommentUseCaseError
import org.sparcs.soap.App.Domain.Error.NetworkError
import org.sparcs.soap.App.Domain.Usecases.Feed.FeedCommentUseCase
import org.sparcs.soap.BuddyTestSupport.Error.TestError
import org.sparcs.soap.BuddyTestSupport.Helper.UseCaseTestFixtures
import org.sparcs.soap.BuddyTestSupport.MockCrashlyticsService
import org.sparcs.soap.BuddyTestSupport.Repository.MockFeedCommentRepository

class FeedCommentUseCaseTests : FeedTestBase() {

    private lateinit var useCase: FeedCommentUseCase
    private lateinit var mockRepo: MockFeedCommentRepository
    private lateinit var mockCrashlytics: MockCrashlyticsService

    @Before
    override fun setup() {
        super.setup()
        mockRepo = MockFeedCommentRepository()
        mockCrashlytics = MockCrashlyticsService()
        useCase = FeedCommentUseCase(
            feedCommentRepository = mockRepo,
            crashlyticsService = mockCrashlytics
        )
    }

    @Test
    fun `fetchComments delegates to repository`() = runTest {
        val comments = listOf(
            UseCaseTestFixtures.makeComment(id = "1"),
            UseCaseTestFixtures.makeComment(id = "2")
        )
        mockRepo.fetchCommentsResult = Result.success(comments)

        val result = useCase.fetchComments(postID = "post-1")

        assertEquals(1, mockRepo.fetchCommentsCallCount)
        assertEquals("post-1", mockRepo.lastFetchPostID)
        assertEquals(2, result.size)
    }

    @Test
    fun `writeComment delegates to repository`() = runTest {
        val expectedComment = UseCaseTestFixtures.makeComment(id = "new-comment")
        mockRepo.writeCommentResult = Result.success(expectedComment)

        val request = UseCaseTestFixtures.makeCreateComment(content = "Hello", isAnonymous = true)
        val result = useCase.writeComment(postID = "post-1", request = request)

        assertEquals(1, mockRepo.writeCommentCallCount)
        assertEquals("post-1", mockRepo.lastWriteCommentPostID)
        assertEquals("Hello", mockRepo.lastWriteCommentRequest?.content)
        assertEquals(true, mockRepo.lastWriteCommentRequest?.isAnonymous)
        assertEquals("new-comment", result.id)
    }

    @Test
    fun `writeReply delegates to repository`() = runTest {
        val expectedReply =
            UseCaseTestFixtures.makeComment(id = "reply-1", parentCommentID = "comment-1")
        mockRepo.writeReplyResult = Result.success(expectedReply)

        val request = UseCaseTestFixtures.makeCreateComment(content = "Reply content")
        val result = useCase.writeReply(commentID = "comment-1", request = request)

        assertEquals(1, mockRepo.writeReplyCallCount)
        assertEquals("comment-1", mockRepo.lastWriteReplyCommentID)
        assertEquals("reply-1", result.id)
    }

    @Test
    fun `deleteComment delegates to repository`() = runTest {
        useCase.deleteComment(commentID = "comment-123")

        assertEquals(1, mockRepo.deleteCommentCallCount)
        assertEquals("comment-123", mockRepo.lastDeleteCommentID)
    }

    @Test
    fun `deleteComment with 409 error throws cannotDeleteCommentWithVote`() = runTest {
        mockRepo.deleteCommentResult = Result.failure(NetworkError.ServerError(code = 409))

        try {
            useCase.deleteComment(commentID = "comment-123")
            fail("Expected error to be thrown")
        } catch (error: FeedCommentUseCaseError) {
            assertTrue(error is FeedCommentUseCaseError.CannotDeleteCommentWithVote)
        } catch (e: Exception) {
            fail("Unexpected error type: $e")
        }
    }

    @Test
    fun `vote delegates to repository`() = runTest {
        useCase.vote(commentID = "comment-1", type = FeedVoteType.DOWN)

        assertEquals(1, mockRepo.voteCallCount)
        assertEquals("comment-1", mockRepo.lastVoteCommentID)
        assertEquals(FeedVoteType.DOWN, mockRepo.lastVoteType)
    }

    @Test
    fun `deleteVote delegates to repository`() = runTest {
        useCase.deleteVote(commentID = "comment-1")

        assertEquals(1, mockRepo.deleteVoteCallCount)
        assertEquals("comment-1", mockRepo.lastDeleteVoteCommentID)
    }

    @Test
    fun `reportComment delegates to repository`() = runTest {
        useCase.reportComment(
            commentID = "comment-1",
            reason = FeedReportType.ABUSIVE_LANGUAGE,
            detail = "Offensive"
        )

        assertEquals(1, mockRepo.reportCommentCallCount)
        assertEquals("comment-1", mockRepo.lastReportCommentID)
        assertEquals(FeedReportType.ABUSIVE_LANGUAGE, mockRepo.lastReportReason)
        assertEquals("Offensive", mockRepo.lastReportDetail)
    }

    @Test
    fun `NetworkError is recorded to Crashlytics`() = runTest {
        mockRepo.voteResult = Result.failure(NetworkError.ServerError(code = 500))

        try {
            useCase.vote(commentID = "comment-1", type = FeedVoteType.UP)
            fail("Expected error to be thrown")
        } catch (e: Exception) {
            assertEquals(1, mockCrashlytics.recordErrorWithContextCallCount)
            assertEquals("FeedComment", mockCrashlytics.lastRecordedContext?.feature)
        }
    }

    @Test
    fun `unknown error is wrapped in FeedCommentUseCaseError unknown`() = runTest {
        mockRepo.fetchCommentsResult = Result.failure(TestError.TestFailure)

        try {
            useCase.fetchComments(postID = "post-1")
            fail("Expected error to be thrown")
        } catch (error: FeedCommentUseCaseError) {
            if (error is FeedCommentUseCaseError.Unknown) {
                assertTrue(error.underlying != null)
            } else {
                fail("Expected unknown error")
            }
        } catch (e: Exception) {
            fail("Unexpected error type: $e")
        }
    }
}