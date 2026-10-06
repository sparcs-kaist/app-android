package org.sparcs.soap.lectureSearchTests

import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.NonCancellable
import kotlinx.coroutines.withContext
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertFalse
import org.sparcs.soap.R
import org.sparcs.soap.app.domain.models.otl.OTLUser
import org.sparcs.soap.app.domain.repositories.otl.OTLUserRepositoryProtocol
import org.sparcs.soap.app.domain.usecases.otl.WishlistUseCase
import org.sparcs.soap.buddyTestSupport.useCase.MockUserUseCase
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.sparcs.soap.app.domain.models.otl.Lecture
import org.sparcs.soap.app.domain.models.otl.LectureTimeFilter
import org.sparcs.soap.app.domain.models.otl.LectureSearchRequest
import org.sparcs.soap.app.domain.enums.otl.DayType
import org.sparcs.soap.app.domain.usecases.otl.LectureUseCaseProtocol
import org.sparcs.soap.app.shared.mocks.otl.mock
import org.sparcs.soap.app.domain.models.otl.CourseLecture
import org.sparcs.soap.app.domain.models.otl.Semester
import org.sparcs.soap.app.features.lectureSearch.LectureSearchViewModel
import org.sparcs.soap.app.shared.mocks.otl.mockList
import org.sparcs.soap.buddyTestSupport.MockAnalyticsService
import org.sparcs.soap.buddyTestSupport.MockCrashlyticsService
import org.sparcs.soap.buddyTestSupport.useCase.MockLectureUseCase
import org.sparcs.soap.testSupport.MainDispatcherRule

@OptIn(ExperimentalCoroutinesApi::class)
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

    private fun wishlistModel(
        fetch: suspend () -> List<CourseLecture>,
        update: suspend () -> Unit = {},
    ): LectureSearchViewModel {
        val user = MockUserUseCase().apply { otlUser = OTLUser.mock() }
        val repository = object : OTLUserRepositoryProtocol {
            override suspend fun fetchWishlist(userID: Int, semester: Semester) = fetch()
            override suspend fun updateWishlist(userID: Int, lectureID: Int, isWishlisted: Boolean) = update()
            override suspend fun updateInterestedDepartments(userID: Int, departmentIDs: List<Int>) = Unit
            override suspend fun register(ssoInfo: String) = Unit
            override suspend fun fetchUser() = OTLUser.mock()
        }
        return LectureSearchViewModel(
            mockLectureUseCase, MockCrashlyticsService(), MockAnalyticsService(), WishlistUseCase(repository, user),
        )
    }

    @Test
    fun `wishlist transitions from loading to loaded with results`() = runTest {
        val response = CompletableDeferred<List<CourseLecture>>()
        val model = wishlistModel({ response.await() })
        model.bind(semester)
        assertEquals(LectureSearchViewModel.ViewState.Loading, model.wishlistState.value)
        val courses = CourseLecture.mockList()
        response.complete(courses)
        runCurrent()
        assertEquals(LectureSearchViewModel.ViewState.Loaded(courses), model.wishlistState.value)
        assertEquals(courses.flatMap { it.lectures }.map { it.id }.toSet(), model.wishlistedLectureIDs.value)
    }

    @Test
    fun `wishlist fetch failure is inline and a successful retry clears error`() = runTest {
        var fails = true
        val courses = CourseLecture.mockList()
        val model = wishlistModel({ if (fails) error("Fetch failed") else courses })
        model.bind(semester)
        assertTrue(model.wishlistState.value is LectureSearchViewModel.ViewState.Error)
        assertFalse(model.isAlertPresented)
        fails = false
        model.fetchWishlist(semester)
        assertEquals(LectureSearchViewModel.ViewState.Loaded(courses), model.wishlistState.value)
    }

    @Test
    fun `wishlist update failure restores data and presents global alert`() = runTest {
        val courses = CourseLecture.mockList()
        val model = wishlistModel({ courses }, { error("Update failed") })
        model.bind(semester)
        val ids = model.wishlistedLectureIDs.value
        model.toggleWishlist(courses.first().lectures.first())
        assertEquals(LectureSearchViewModel.ViewState.Loaded(courses), model.wishlistState.value)
        assertEquals(ids, model.wishlistedLectureIDs.value)
        assertTrue(model.isAlertPresented)
        assertEquals(R.string.wishlist_update_failed, model.alertState?.messageResId)
    }

    @Test
    fun `initial state is loaded with empty courses`() {
        assertEquals(LectureSearchViewModel.ViewState.Loaded(), viewModel.state.value)
        assertTrue(viewModel.state.value.courses.isEmpty())
    }

    @Test
    fun `fetchLectures with a keyword loads courses`() = runTest {
        val lectures = CourseLecture.mockList()
        mockLectureUseCase.searchLectureResult = Result.success(lectures)
        viewModel.onSearchTextChange("algorithms")

        viewModel.fetchLectures(semester)

        assertEquals(1, mockLectureUseCase.searchLectureCallCount)
        assertEquals(lectures, viewModel.state.value.courses)
        assertEquals(LectureSearchViewModel.ViewState.Loaded(lectures), viewModel.state.value)
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
        assertEquals(LectureSearchViewModel.ViewState.Loaded(), viewModel.state.value)
    }

    @Test
    fun `time only search sends selected day and minutes`() = runTest {
        val time = LectureTimeFilter(DayType.MON, 600, 900)
        viewModel.onTimeChange(time)
        viewModel.fetchLectures(semester)
        assertEquals(time, mockLectureUseCase.lastRequest?.time)
        assertEquals("", mockLectureUseCase.lastRequest?.keyword)
    }

    @Test
    fun `pages use lecture offset and merge split courses without duplicates`() = runTest {
        val course = CourseLecture.mockList().first()
        val lectures = (1..100).map { Lecture.mock().copy(id = it) }
        mockLectureUseCase.searchLectureResult = Result.success(listOf(course.copy(lectures = lectures)))
        viewModel.onSearchTextChange("course")
        viewModel.fetchLectures(semester)
        assertTrue((viewModel.pagination.value as LectureSearchViewModel.PaginationState.Idle).hasMore)
        mockLectureUseCase.searchLectureResult = Result.success(listOf(course.copy(lectures = listOf(lectures.last(), Lecture.mock().copy(id = 101)))))
        viewModel.loadNextPage()
        assertEquals(100, mockLectureUseCase.lastRequest?.offset)
        assertEquals(1, viewModel.state.value.courses.size)
        assertEquals(101, viewModel.state.value.courses.first().lectures.size)
        assertEquals(false, (viewModel.pagination.value as LectureSearchViewModel.PaginationState.Idle).hasMore)
    }

    @Test
    fun `failed next page retains results and retries the same offset`() = runTest {
        val course = CourseLecture.mockList().first().copy(lectures = (1..100).map { Lecture.mock().copy(id = it) })
        mockLectureUseCase.searchLectureResult = Result.success(listOf(course))
        viewModel.onSearchTextChange("course")
        viewModel.fetchLectures(semester)
        mockLectureUseCase.searchLectureResult = Result.failure(Exception("offline"))
        viewModel.loadNextPage()
        assertEquals(listOf(course), viewModel.state.value.courses)
        assertTrue(viewModel.pagination.value is LectureSearchViewModel.PaginationState.Error)
        mockLectureUseCase.searchLectureResult = Result.success(emptyList())
        viewModel.loadNextPage()
        assertEquals(100, mockLectureUseCase.lastRequest?.offset)
        assertEquals(false, (viewModel.pagination.value as LectureSearchViewModel.PaginationState.Idle).hasMore)
    }

    @Test
    fun `typing shows loading instead of an empty result until the search runs`() = runTest {
        viewModel.bind(semester)
        viewModel.onSearchTextChange("algorithms")

        assertEquals(LectureSearchViewModel.ViewState.Loading, viewModel.state.value)
        assertEquals(LectureSearchViewModel.PaginationState.Idle(), viewModel.pagination.value)
    }

    @Test
    fun `cancelled search cannot publish stale results`() = runTest {
        val old = CompletableDeferred<List<CourseLecture>>()
        val current = CourseLecture.mockList().take(1)
        val useCase = object : LectureUseCaseProtocol by mockLectureUseCase {
            override suspend fun searchLecture(request: LectureSearchRequest): List<CourseLecture> =
                if (request.keyword == "old") withContext(NonCancellable) { old.await() } else current
        }
        viewModel = LectureSearchViewModel(useCase, MockCrashlyticsService(), MockAnalyticsService())
        viewModel.onSearchTextChange("old")
        viewModel.fetchLectures(semester)
        viewModel.onSearchTextChange("new")
        viewModel.fetchLectures(semester)
        old.complete(emptyList())
        runCurrent()
        assertEquals(current, viewModel.state.value.courses)
    }
}
