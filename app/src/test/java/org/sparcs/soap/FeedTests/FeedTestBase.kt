package org.sparcs.soap.FeedTests

import org.junit.Before
import org.sparcs.soap.BuddyTestSupport.MockAnalyticsService
import org.sparcs.soap.BuddyTestSupport.MockCrashlyticsService
import org.sparcs.soap.BuddyTestSupport.UseCase.MockAuthUseCase
import org.sparcs.soap.BuddyTestSupport.UseCase.MockFeedCommentUseCase
import org.sparcs.soap.BuddyTestSupport.UseCase.MockFeedPostUseCase

open class FeedTestBase {
    protected lateinit var mockFeedPostUseCase: MockFeedPostUseCase
    protected lateinit var mockFeedCommentUseCase: MockFeedCommentUseCase
    protected lateinit var mockAuthUseCase: MockAuthUseCase
    protected lateinit var mockCrashlyticsService: MockCrashlyticsService
    protected lateinit var mockAnalyticsService: MockAnalyticsService

    @Before
    open fun setup() {
        mockFeedPostUseCase = MockFeedPostUseCase()
        mockFeedCommentUseCase = MockFeedCommentUseCase()
        mockAuthUseCase = MockAuthUseCase()
        mockCrashlyticsService = MockCrashlyticsService()
        mockAnalyticsService = MockAnalyticsService()
    }
}