package org.sparcs.soap.lectureSearchTests

import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.sparcs.soap.app.domain.models.otl.CourseLecture
import org.sparcs.soap.app.domain.models.otl.Semester
import org.sparcs.soap.app.features.lectureSearch.LectureSearchViewModel
import org.sparcs.soap.app.shared.mocks.otl.mockList
import org.sparcs.soap.buddyTestSupport.MockAnalyticsService
import org.sparcs.soap.buddyTestSupport.MockCrashlyticsService
import org.sparcs.soap.buddyTestSupport.useCase.MockLectureUseCase
import org.sparcs.soap.testSupport.MainDispatcherRule

class LectureSearchViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private lateinit var mockLectureUseCase: MockLectureUseCase
    private lateinit var viewModel: LectureSearchViewModel
    private val semester: Semester = Semester.mockList().first()

    @Before
    fun setup() {
        mockLectureUseCase = MockLectureUseCase()
        viewModel = LectureSearchViewModel(
            lectureUseCase = mockLectureUseCase,
            crashlyticsService = MockCrashlyticsService(),
            analyticsService = MockAnalyticsService(),
        )
    }

    @Test
    fun `initial state is loaded with empty courses`() {
        assertEquals(LectureSearchViewModel.ViewState.Loaded, viewModel.state.value)
        assertTrue(viewModel.courses.value.isEmpty())
    }

    @Test
    fun `fetchLectures with a keyword loads courses`() = runTest {
        val lectures = CourseLecture.mockList()
        mockLectureUseCase.searchLectureResult = Result.success(lectures)
        viewModel.onSearchTextChange("algorithms")

        viewModel.fetchLectures(semester)

        assertEquals(1, mockLectureUseCase.searchLectureCallCount)
        assertEquals(lectures, viewModel.courses.value)
        assertEquals(LectureSearchViewModel.ViewState.Loaded, viewModel.state.value)
        assertEquals("algorithms", mockLectureUseCase.lastRequest?.keyword)
    }

    @Test
    fun `fetchLectures failure sets error state`() = runTest {
        mockLectureUseCase.searchLectureResult = Result.failure(Exception("Test failure"))
        viewModel.onSearchTextChange("algorithms")

        viewModel.fetchLectures(semester)

        assertTrue(viewModel.state.value is LectureSearchViewModel.ViewState.Error)
    }

    @Test
    fun `fetchLectures with blank keyword and no filter does not search`() = runTest {
        viewModel.fetchLectures(semester)

        assertEquals(0, mockLectureUseCase.searchLectureCallCount)
        assertEquals(LectureSearchViewModel.ViewState.Loaded, viewModel.state.value)
    }
}
