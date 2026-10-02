package org.sparcs.soap.app.features.search

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.Job
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import org.sparcs.soap.app.domain.enums.ara.PostListType
import org.sparcs.soap.app.domain.models.SearchScope
import org.sparcs.soap.app.domain.models.ara.AraPost
import org.sparcs.soap.app.domain.models.otl.CourseFilterState
import org.sparcs.soap.app.domain.models.otl.CourseSearchRequest
import org.sparcs.soap.app.domain.models.otl.CourseSummary
import org.sparcs.soap.app.domain.models.otl.ETC_DEPARTMENT_ID
import org.sparcs.soap.app.domain.models.taxi.TaxiRoom
import org.sparcs.soap.app.domain.repositories.taxi.TaxiRoomRepositoryProtocol
import org.sparcs.soap.app.domain.usecases.ara.AraBoardUseCaseProtocol
import org.sparcs.soap.app.domain.usecases.otl.CourseUseCaseProtocol
import org.sparcs.soap.app.domain.usecases.taxi.TaxiLocationUseCaseProtocol
import javax.inject.Inject

interface SearchViewModelProtocol {
    val courses: StateFlow<List<CourseSummary>>
    val posts: StateFlow<List<AraPost>>
    val taxiRooms: StateFlow<List<TaxiRoom>>

    val state: StateFlow<SearchViewModel.ViewState>
    val searchText: StateFlow<String>
    val searchScope: StateFlow<SearchScope>
    val courseFilterState: StateFlow<CourseFilterState>

    val hasMoreCourses: StateFlow<Boolean>
    val isLoadingMoreCourses: StateFlow<Boolean>
    val coursePageError: StateFlow<Exception?>

    suspend fun bind()
    suspend fun fetchInitialData()
    fun loadCoursesNextPage()
    fun loadAraNextPage()
    fun loadFull()
    suspend fun scopedFetch()
    fun onSearchTextChange(text: String)
    fun onScopeChange(scope: SearchScope)
    fun onFilterChange(filterState: CourseFilterState)
}

@HiltViewModel
class SearchViewModel @Inject constructor(
    private val araBoardUseCase: AraBoardUseCaseProtocol,
    private val taxiRoomRepository: TaxiRoomRepositoryProtocol,
    private val taxiLocationUseCase: TaxiLocationUseCaseProtocol,
    private val courseUseCase: CourseUseCaseProtocol,
) : ViewModel(), SearchViewModelProtocol {

    private val _state = MutableStateFlow<ViewState>(ViewState.Loaded)
    override val state: StateFlow<ViewState> = _state

    private val _courses = MutableStateFlow<List<CourseSummary>>(emptyList())
    override val courses: StateFlow<List<CourseSummary>> = _courses

    private val _posts = MutableStateFlow<List<AraPost>>(emptyList())
    override val posts: StateFlow<List<AraPost>> = _posts

    private val _taxiRooms = MutableStateFlow<List<TaxiRoom>>(emptyList())
    override val taxiRooms: StateFlow<List<TaxiRoom>> = _taxiRooms

    private val _searchText = MutableStateFlow("")
    override val searchText: StateFlow<String> = _searchText

    private val _searchScope = MutableStateFlow(SearchScope.All)
    override val searchScope: StateFlow<SearchScope> = _searchScope

    private val _courseFilterState = MutableStateFlow(CourseFilterState())
    override val courseFilterState: StateFlow<CourseFilterState> = _courseFilterState

    private var araPagination = PaginationInfo()

    private var searchJob: Job? = null
    private var courseJob: Job? = null
    private var courseGeneration = 0
    private var courseOffset = 0
    private var lastCourseRequest: CourseSearchRequest? = null
    private var coursePageJob: Job? = null
    private val _hasMoreCourses = MutableStateFlow(false)
    override val hasMoreCourses: StateFlow<Boolean> = _hasMoreCourses
    private val _isLoadingMoreCourses = MutableStateFlow(false)
    override val isLoadingMoreCourses: StateFlow<Boolean> = _isLoadingMoreCourses
    private val _coursePageError = MutableStateFlow<Exception?>(null)
    override val coursePageError: StateFlow<Exception?> = _coursePageError

    sealed class ViewState {
        data object Loading : ViewState()
        data object Loaded : ViewState()
        data class Error(val error: Exception) : ViewState()
    }

    data class PaginationInfo(
        var currentPage: Int = 1,
        var totalPages: Int = 0,
        var isLoading: Boolean = false,
    ) {
        val hasMore: Boolean get() = currentPage < totalPages
    }

    init {
        setupSearchSubscription()
    }

    @OptIn(FlowPreview::class)
    private fun setupSearchSubscription() {
        searchJob = viewModelScope.launch {
            _searchText.map { it.trim() }.distinctUntilChanged().debounce(350).collectLatest { performSearch(it) }
        }
    }

    private suspend fun performSearch(keyword: String) {
        courseJob?.cancel()
        resetSearchState()
        if (keyword.isBlank()) {
            if (_searchScope.value == SearchScope.Courses && !_courseFilterState.value.isEmpty()) {
                refreshCourses()
            } else _state.value = ViewState.Loaded
            return
        }
        _state.value = ViewState.Loading
        try {
            coroutineScope {
                launch { searchAra(keyword) }
                launch { searchTaxi(keyword) }
                launch { searchCourses(keyword) }
            }
            _state.value = ViewState.Loaded
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            _state.value = ViewState.Error(e)
        }
    }

    private suspend fun searchAra(keyword: String, page: Int = 1) {
        val postPage = araBoardUseCase.fetchPosts(
            type = PostListType.All,
            page = page,
            pageSize = 30,
            searchKeyword = keyword
        )
        araPagination = araPagination.copy(
            currentPage = postPage.currentPage,
            totalPages = postPage.pages
        )

        if (page == 1) {
            _posts.value = postPage.results
        } else {
            _posts.value += postPage.results
        }
    }

    private suspend fun searchTaxi(keyword: String) {
        val allRooms = taxiRoomRepository.fetchRooms()
        val matchedLocations = taxiLocationUseCase.queryLocation(keyword).map { it.id }.toHashSet()
        val searchKeyword = keyword.lowercase().trim()

        _taxiRooms.value = allRooms.filter { room ->
            val matchesLocation =
                room.source.id in matchedLocations || room.destination.id in matchedLocations
            val matchesTitle = room.title.lowercase().contains(searchKeyword)
            matchesLocation || matchesTitle
        }.distinctBy { it.id }
    }

    private suspend fun searchCourses(keyword: String) {
        val generation = ++courseGeneration
        val filter = if (_searchScope.value == SearchScope.Courses) _courseFilterState.value else CourseFilterState()
        resetCoursePagination()
        val request = CourseSearchRequest(
                keyword = keyword,
                offset = 0,
                limit = 150,
                type = filter.classifications.ifEmpty { null },
                department = filter.departments
                    .filter { it != ETC_DEPARTMENT_ID }
                    .ifEmpty { null },
                level = filter.levels.ifEmpty { null },
                term = filter.period
        )
        val result = courseUseCase.searchCourse(request)
        if (generation == courseGeneration && keyword == _searchText.value.trim()) {
            _courses.value = result
            lastCourseRequest = request
            courseOffset = result.size
            _hasMoreCourses.value = result.size >= request.limit
        }
    }

    override fun onSearchTextChange(text: String) {
        if (text.trim() == _searchText.value.trim()) {
            _searchText.value = text
            return
        }
        courseJob?.cancel()
        courseGeneration++
        resetCoursePagination()
        _searchText.value = text
    }

    override fun onScopeChange(scope: SearchScope) {
        if (_searchScope.value == scope) return
        _searchScope.value = scope
        refreshCourses()
    }

    override fun onFilterChange(filterState: CourseFilterState) {
        if (_courseFilterState.value == filterState) return
        _courseFilterState.value = filterState
        refreshCourses()
    }

    private fun refreshCourses() {
        courseJob?.cancel()
        courseGeneration++
        resetCoursePagination()
        val keyword = _searchText.value.trim()
        if (keyword.isBlank() && (_searchScope.value != SearchScope.Courses || _courseFilterState.value.isEmpty())) {
            _courses.value = emptyList()
            _state.value = ViewState.Loaded
            return
        }
        courseJob = viewModelScope.launch {
            _state.value = ViewState.Loading
            try {
                searchCourses(keyword)
                ensureActive()
                _state.value = ViewState.Loaded
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                _state.value = ViewState.Error(e)
            }
        }
    }

    override fun loadAraNextPage() {
        if (araPagination.isLoading || !araPagination.hasMore) return

        viewModelScope.launch {
            araPagination.isLoading = true
            try {
                searchAra(_searchText.value, araPagination.currentPage + 1)
            } finally {
                araPagination.isLoading = false
            }
        }
    }

    private fun resetCoursePagination() {
        coursePageJob?.cancel()
        lastCourseRequest = null
        courseOffset = 0
        _hasMoreCourses.value = false
        _isLoadingMoreCourses.value = false
        _coursePageError.value = null
    }

    override fun loadCoursesNextPage() {
        if (!_hasMoreCourses.value || _isLoadingMoreCourses.value) return
        val request = lastCourseRequest?.copy(offset = courseOffset) ?: return
        val generation = courseGeneration
        _isLoadingMoreCourses.value = true
        _coursePageError.value = null
        coursePageJob = viewModelScope.launch {
            try {
                val page = courseUseCase.searchCourse(request)
                ensureActive()
                if (generation != courseGeneration) return@launch
                _courses.value = (_courses.value + page).distinctBy { it.id }
                courseOffset += page.size
                _hasMoreCourses.value = page.size >= request.limit
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                ensureActive()
                if (generation == courseGeneration) _coursePageError.value = e
            } finally {
                if (generation == courseGeneration) _isLoadingMoreCourses.value = false
            }
        }
    }

    private fun resetSearchState() {
        resetCoursePagination()
        _courses.value = emptyList()
        _posts.value = emptyList()
        _taxiRooms.value = emptyList()
        araPagination = PaginationInfo()
    }

    override suspend fun bind() {
        val currentText = _searchText.value
        if (currentText.isNotBlank()) {
            performSearch(currentText)
        }
    }

    override suspend fun fetchInitialData() {
        performSearch(_searchText.value)
    }

    override fun loadFull() {
        _state.value = ViewState.Loaded
    }

    override suspend fun scopedFetch() {
        performSearch(_searchText.value)
    }
}