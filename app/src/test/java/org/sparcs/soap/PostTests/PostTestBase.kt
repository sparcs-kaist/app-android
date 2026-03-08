package org.sparcs.soap.PostTests

import org.junit.Before
import org.sparcs.soap.BuddyTestSupport.MockAnalyticsService
import org.sparcs.soap.BuddyTestSupport.MockCrashlyticsService
import org.sparcs.soap.BuddyTestSupport.UseCase.MockAraBoardUseCase
import org.sparcs.soap.BuddyTestSupport.UseCase.MockAraCommentUseCase
import org.sparcs.soap.BuddyTestSupport.UseCase.MockAuthUseCase

open class PostTestBase {
    protected lateinit var mockAraBoardUseCase: MockAraBoardUseCase
    protected lateinit var mockAraCommentUseCase: MockAraCommentUseCase
    protected lateinit var mockAuthUseCase: MockAuthUseCase
    protected lateinit var mockCrashlyticsService: MockCrashlyticsService
    protected lateinit var mockAnalyticsService: MockAnalyticsService

    @Before
    open fun setup() {
        mockAraBoardUseCase = MockAraBoardUseCase()
        mockAraCommentUseCase = MockAraCommentUseCase()
        mockAuthUseCase = MockAuthUseCase()
        mockCrashlyticsService = MockCrashlyticsService()
        mockAnalyticsService = MockAnalyticsService()
    }
}