package org.sparcs.soap.feedTests

import org.junit.Before
import org.junit.Rule
import org.sparcs.soap.buddyTestSupport.MockAnalyticsService
import org.sparcs.soap.buddyTestSupport.MockCrashlyticsService
import org.sparcs.soap.buddyTestSupport.useCase.MockAuthUseCase
import org.sparcs.soap.buddyTestSupport.useCase.MockFeedCommentUseCase
import org.sparcs.soap.buddyTestSupport.useCase.MockFeedPostUseCase
import org.sparcs.soap.testSupport.MainDispatcherRule

open class FeedTestBase {
    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

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