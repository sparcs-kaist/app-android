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
import org.sparcs.soap.app.domain.models.otl.CourseSummary
import org.sparcs.soap.app.features.search.SearchViewModel
import org.sparcs.soap.app.shared.mocks.ara.mockList
import org.sparcs.soap.app.shared.mocks.otl.mockList
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
    fun `onScopeChange with a keyword searches all sources`() = runTest {
        mockAraBoardUseCase.fetchPostsResult = Result.success(postPage(AraPost.mockList().take(2)))
        mockCourseUseCase.searchCourseResult = Result.success(CourseSummary.mockList())
        viewModel.onSearchTextChange("algorithms")

        viewModel.onScopeChange(SearchScope.Courses)

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
}
