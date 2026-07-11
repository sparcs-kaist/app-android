package org.sparcs.soap.postTests

import org.junit.Before
import org.junit.Rule
import org.sparcs.soap.buddyTestSupport.MockAnalyticsService
import org.sparcs.soap.buddyTestSupport.MockCrashlyticsService
import org.sparcs.soap.buddyTestSupport.useCase.MockAraBoardUseCase
import org.sparcs.soap.buddyTestSupport.useCase.MockAraCommentUseCase
import org.sparcs.soap.buddyTestSupport.useCase.MockAuthUseCase
import org.sparcs.soap.testSupport.MainDispatcherRule

open class PostTestBase {
    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

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