package org.sparcs.soap.FeedTests

import MockFeedPostRepository
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Assert.fail
import org.junit.Before
import org.junit.Test
import org.sparcs.soap.App.Domain.Enums.Feed.FeedReportType
import org.sparcs.soap.App.Domain.Enums.Feed.FeedVoteType
import org.sparcs.soap.App.Domain.Error.Feed.FeedPostUseCaseError
import org.sparcs.soap.App.Domain.Error.NetworkError
import org.sparcs.soap.App.Domain.Usecases.Feed.FeedPostUseCase
import org.sparcs.soap.BuddyTestSupport.Error.TestError
import org.sparcs.soap.BuddyTestSupport.Helper.UseCaseTestFixtures
import org.sparcs.soap.BuddyTestSupport.MockCrashlyticsService

class FeedPostUseCaseTests : FeedTestBase() {

    private lateinit var useCase: FeedPostUseCase
    private lateinit var mockRepo: MockFeedPostRepository
    private lateinit var mockCrashlytics: MockCrashlyticsService

    @Before
    override fun setup() {
        super.setup()
        mockRepo = MockFeedPostRepository()
        mockCrashlytics = MockCrashlyticsService()
        useCase = FeedPostUseCase(
            feedPostRepository = mockRepo,
            crashlyticsService = mockCrashlytics
        )
    }

    @Test
    fun `fetchPosts delegates to repository with correct parameters`() = runTest {
        val posts = listOf(
            UseCaseTestFixtures.makePost(id = "1"),
            UseCaseTestFixtures.makePost(id = "2")
        )
        mockRepo.fetchPostsResult = Result.success(
            UseCaseTestFixtures.makePostPage(posts = posts, nextCursor = "next", hasNext = true)
        )

        val result = useCase.fetchPosts(cursor = "cursor-1", page = 20)

        assertEquals(1, mockRepo.fetchPostsCallCount)
        assertEquals("cursor-1", mockRepo.lastCursor)
        assertEquals(20, mockRepo.lastPage)
        assertEquals(2, result.items.size)
        assertEquals("next", result.nextCursor)
        assertEquals(true, result.hasNext)
    }

    @Test
    fun `fetchPosts with nil cursor`() = runTest {
        useCase.fetchPosts(cursor = null, page = 10)

        assertNull(mockRepo.lastCursor)
        assertEquals(10, mockRepo.lastPage)
    }

    @Test
    fun `writePost delegates to repository`() = runTest {
        val request =
            UseCaseTestFixtures.makeCreatePost(content = "Hello world", isAnonymous = true)

        useCase.writePost(request = request)

        assertEquals(1, mockRepo.writePostCallCount)
        assertEquals("Hello world", mockRepo.lastWriteRequest?.content)
        assertEquals(true, mockRepo.lastWriteRequest?.isAnonymous)
    }

    @Test
    fun `deletePost delegates to repository`() = runTest {
        useCase.deletePost(postID = "post-123")

        assertEquals(1, mockRepo.deletePostCallCount)
        assertEquals("post-123", mockRepo.lastDeletePostID)
    }

    @Test
    fun `deletePost with 409 error throws cannotDeletePostWithVoteOrComment`() = runTest {
        mockRepo.deletePostResult = Result.failure(NetworkError.ServerError(code = 409))

        try {
            useCase.deletePost(postID = "post-123")
            fail("Expected error to be thrown")
        } catch (error: FeedPostUseCaseError) {
            assertTrue(error is FeedPostUseCaseError.CannotDeletePostWithVoteOrComment)
        } catch (e: Exception) {
            fail("Unexpected error type: $e")
        }
    }

    @Test
    fun `deletePost with other NetworkError propagates and records to Crashlytics`() = runTest {
        mockRepo.deletePostResult = Result.failure(NetworkError.ServerError(code = 500))

        try {
            useCase.deletePost(postID = "post-123")
            fail("Expected error to be thrown")
        } catch (e: Exception) {
            assertEquals(1, mockCrashlytics.recordErrorWithContextCallCount)
            assertEquals("FeedPost", mockCrashlytics.lastRecordedContext?.feature)
        }
    }

    @Test
    fun `vote delegates to repository with correct parameters`() = runTest {
        useCase.vote(postID = "post-1", type = FeedVoteType.UP)

        assertEquals(1, mockRepo.voteCallCount)
        assertEquals("post-1", mockRepo.lastVotePostID)
        assertEquals(FeedVoteType.UP, mockRepo.lastVoteType)
    }

    @Test
    fun `deleteVote delegates to repository`() = runTest {
        useCase.deleteVote(postID = "post-1")

        assertEquals(1, mockRepo.deleteVoteCallCount)
        assertEquals("post-1", mockRepo.lastDeleteVotePostID)
    }

    @Test
    fun `reportPost delegates to repository with all parameters`() = runTest {
        useCase.reportPost(postID = "post-1", reason = FeedReportType.SPAM, detail = "This is spam")

        assertEquals(1, mockRepo.reportPostCallCount)
        assertEquals("post-1", mockRepo.lastReportPostID)
        assertEquals(FeedReportType.SPAM, mockRepo.lastReportReason)
        assertEquals("This is spam", mockRepo.lastReportDetail)
    }

    @Test
    fun `unknown error is wrapped and recorded to Crashlytics`() = runTest {
        mockRepo.fetchPostsResult = Result.failure(TestError.TestFailure)

        try {
            useCase.fetchPosts(cursor = null, page = 20)
            fail("Expected error to be thrown")
        } catch (error: FeedPostUseCaseError) {
            if (error is FeedPostUseCaseError.Unknown) {
                assertTrue(error.underlying != null)
            } else {
                fail("Expected unknown error")
            }
            assertEquals(1, mockCrashlytics.recordErrorWithContextCallCount)
        } catch (e: Exception) {
            fail("Unexpected error type: $e")
        }
    }
}