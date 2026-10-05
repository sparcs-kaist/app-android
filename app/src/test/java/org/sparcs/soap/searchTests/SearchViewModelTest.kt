package org.sparcs.soap.searchTests

import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.sparcs.soap.app.domain.models.SearchScope
import org.sparcs.soap.app.domain.models.ara.AraPost
import org.sparcs.soap.app.domain.models.ara.AraPostPage
import org.sparcs.soap.app.domain.models.otl.CourseFilterState
import org.sparcs.soap.app.domain.models.otl.CourseSearchRequest
import org.sparcs.soap.app.domain.usecases.otl.CourseUseCaseProtocol
import org.sparcs.soap.app.domain.models.otl.CourseSummary
import org.sparcs.soap.app.features.search.SearchViewModel
import org.sparcs.soap.app.shared.mocks.ara.mockList
import org.sparcs.soap.app.shared.mocks.otl.mockList
import org.sparcs.soap.app.shared.mocks.otl.mock
import org.sparcs.soap.buddyTestSupport.repository.MockTaxiRoomRepository
import org.sparcs.soap.buddyTestSupport.useCase.MockAraBoardUseCase
import org.sparcs.soap.buddyTestSupport.useCase.MockCourseUseCase
import org.sparcs.soap.buddyTestSupport.useCase.MockTaxiLocationUseCase
import org.sparcs.soap.testSupport.MainDispatcherRule

class SearchViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private lateinit var mockAraBoardUseCase: MockAraBoardUseCase
    private lateinit var mockTaxiRoomRepository: MockTaxiRoomRepository
    private lateinit var mockTaxiLocationUseCase: MockTaxiLocationUseCase
    private lateinit var mockCourseUseCase: MockCourseUseCase
    private lateinit var viewModel: SearchViewModel

    @Before
    fun setup() {
        mockAraBoardUseCase = MockAraBoardUseCase()
        mockTaxiRoomRepository = MockTaxiRoomRepository()
        mockTaxiLocationUseCase = MockTaxiLocationUseCase()
        mockCourseUseCase = MockCourseUseCase()
        viewModel = SearchViewModel(
            araBoardUseCase = mockAraBoardUseCase,
            taxiRoomRepository = mockTaxiRoomRepository,
            taxiLocationUseCase = mockTaxiLocationUseCase,
            courseUseCase = mockCourseUseCase,
        )
    }

    private fun postPage(results: List<AraPost>) =
        AraPostPage(pages = 1, items = results.size, currentPage = 1, results = results)

    @Test
    fun `onSearchTextChange updates search text`() {
        viewModel.onSearchTextChange("hello")
        assertEquals("hello", viewModel.searchText.value)
    }

    @Test
    fun `onScopeChange reuses posts and refreshes only courses`() = runTest {
        mockAraBoardUseCase.fetchPostsResult = Result.success(postPage(AraPost.mockList().take(2)))
        mockCourseUseCase.searchCourseResult = Result.success(CourseSummary.mockList())
        viewModel.onSearchTextChange("algorithms")

        viewModel.fetchInitialData()
        val postRequests = mockAraBoardUseCase.fetchPostsCallCount
        viewModel.onScopeChange(SearchScope.Courses)
        assertEquals(postRequests, mockAraBoardUseCase.fetchPostsCallCount)

        assertEquals(SearchScope.Courses, viewModel.searchScope.value)
        assertTrue(viewModel.posts.value.isNotEmpty())
        assertTrue(viewModel.courses.value.isNotEmpty())
        assertEquals(SearchViewModel.ViewState.Loaded, viewModel.state.value)
    }

    @Test
    fun `onScopeChange with blank text only changes scope and does not search`() = runTest {
        viewModel.onScopeChange(SearchScope.Posts)

        assertEquals(SearchScope.Posts, viewModel.searchScope.value)
        assertEquals(0, mockAraBoardUseCase.fetchPostsCallCount)
        assertTrue(viewModel.posts.value.isEmpty())
    }

    @Test
    fun `filter only search and filter reset do not fetch posts`() = runTest {
        mockCourseUseCase.searchCourseResult = Result.success(CourseSummary.mockList())
        viewModel.onScopeChange(SearchScope.Courses)
        viewModel.onFilterChange(CourseFilterState(departments = listOf("9945")))
        assertTrue(viewModel.courses.value.isNotEmpty())
        assertEquals(0, mockAraBoardUseCase.fetchPostsCallCount)
        viewModel.onFilterChange(CourseFilterState())
        assertTrue(viewModel.courses.value.isEmpty())
        assertEquals(SearchViewModel.ViewState.Loaded, viewModel.state.value)
    }

    @Test
    fun `all scope ignores course filters and course scope restores them`() = runTest {
        val requests = mutableListOf<CourseSearchRequest>()
        val courses = object : CourseUseCaseProtocol by mockCourseUseCase {
            override suspend fun searchCourse(request: CourseSearchRequest): List<CourseSummary> {
                requests.add(request)
                return emptyList()
            }
        }
        viewModel = SearchViewModel(mockAraBoardUseCase, mockTaxiRoomRepository, mockTaxiLocationUseCase, courses)
        viewModel.onSearchTextChange("algorithms")
        viewModel.onScopeChange(SearchScope.Courses)
        viewModel.onFilterChange(CourseFilterState(departments = listOf("9945"), period = "2"))
        assertEquals(listOf("9945"), requests.last().department)
        assertEquals("2", requests.last().term)
        viewModel.onScopeChange(SearchScope.All)
        assertEquals(null, requests.last().department)
        assertEquals(null, requests.last().term)
        viewModel.onScopeChange(SearchScope.Courses)
        assertEquals(listOf("9945"), requests.last().department)
    }

    @Test
    fun `switching to posts or rides and back to all does not refetch courses`() = runTest {
        var courseRequests = 0
        val courses = object : CourseUseCaseProtocol by mockCourseUseCase {
            override suspend fun searchCourse(request: CourseSearchRequest): List<CourseSummary> {
                courseRequests++
                return CourseSummary.mockList()
            }
        }
        viewModel = SearchViewModel(mockAraBoardUseCase, mockTaxiRoomRepository, mockTaxiLocationUseCase, courses)
        viewModel.onSearchTextChange("algorithms")
        viewModel.fetchInitialData()

        viewModel.onScopeChange(SearchScope.Posts)
        viewModel.onScopeChange(SearchScope.Rides)
        viewModel.onScopeChange(SearchScope.All)

        assertEquals(1, courseRequests)
        assertEquals(1, mockAraBoardUseCase.fetchPostsCallCount)
        assertEquals(SearchViewModel.ViewState.Loaded, viewModel.state.value)
    }

    @Test
    fun `failed post page keeps loaded posts`() = runTest {
        val posts = AraPost.mockList().take(2)
        mockAraBoardUseCase.fetchPostsResult = Result.success(AraPostPage(pages = 3, items = 2, currentPage = 1, results = posts))
        viewModel.onSearchTextChange("algorithms")
        viewModel.fetchInitialData()
        mockAraBoardUseCase.fetchPostsResult = Result.failure(Exception("offline"))

        viewModel.loadAraNextPage()

        assertEquals(posts, viewModel.posts.value)
    }

    @Test
    fun `post page is not requested for a keyword that has not been searched yet`() = runTest {
        mockAraBoardUseCase.fetchPostsResult = Result.success(AraPostPage(pages = 3, items = 2, currentPage = 1, results = AraPost.mockList().take(2)))
        viewModel.onSearchTextChange("algorithms")
        viewModel.fetchInitialData()
        viewModel.onSearchTextChange("physics")

        viewModel.loadAraNextPage()

        assertEquals(1, mockAraBoardUseCase.fetchPostsCallCount)
    }

    @Test
    fun `source errors are caught by the search operation`() = runTest {
        mockAraBoardUseCase.fetchPostsResult = Result.failure(Exception("offline"))
        viewModel.onSearchTextChange("test")
        viewModel.fetchInitialData()
        assertTrue(viewModel.state.value is SearchViewModel.ViewState.Error)
    }

    @Test
    fun `course pagination appends unique results and retains filters`() = runTest {
        val requests = mutableListOf<CourseSearchRequest>()
        val course = CourseSummary.mock()
        var failPage = false
        val courses = object : CourseUseCaseProtocol by mockCourseUseCase {
            override suspend fun searchCourse(request: CourseSearchRequest): List<CourseSummary> {
                requests.add(request)
                if (request.offset == 0) return (1..150).map { course.copy(id = it) }
                if (failPage) error("offline")
                return listOf(course.copy(id = 150), course.copy(id = 151))
            }
        }
        viewModel = SearchViewModel(mockAraBoardUseCase, mockTaxiRoomRepository, mockTaxiLocationUseCase, courses)
        viewModel.onScopeChange(SearchScope.Courses)
        viewModel.onFilterChange(CourseFilterState(departments = listOf("9945")))
        assertTrue(viewModel.hasMoreCourses.value)
        failPage = true
        viewModel.loadCoursesNextPage()
        assertEquals(150, viewModel.courses.value.size)
        assertTrue(viewModel.coursePageError.value != null)
        failPage = false
        viewModel.loadCoursesNextPage()
        assertEquals(150, requests.last().offset)
        assertEquals(listOf("9945"), requests.last().department)
        assertEquals(151, viewModel.courses.value.size)
        assertEquals(false, viewModel.hasMoreCourses.value)
        assertEquals(0, mockAraBoardUseCase.fetchPostsCallCount)
    }
}
