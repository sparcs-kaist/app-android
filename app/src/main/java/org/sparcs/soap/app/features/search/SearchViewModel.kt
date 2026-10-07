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
import timber.log.Timber
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
    private var araPageJob: Job? = null

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
        val keyword: String = "",
        val currentPage: Int = 1,
        val totalPages: Int = 0,
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
                launch { searchCourses(courseRequest(keyword)) }
            }
            if (courseJob?.isActive != true) _state.value = ViewState.Loaded
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
        araPagination = PaginationInfo(keyword, postPage.currentPage, postPage.pages)

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

    private fun courseRequest(keyword: String): CourseSearchRequest {
        val filter = if (_searchScope.value == SearchScope.Courses) _courseFilterState.value else CourseFilterState()
        return CourseSearchRequest(
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
    }

    private suspend fun searchCourses(request: CourseSearchRequest) {
        val generation = ++courseGeneration
        resetCoursePagination()
        val result = courseUseCase.searchCourse(request)
        if (generation == courseGeneration && request.keyword == _searchText.value.trim()) {
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
        araPageJob?.cancel()
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
        if (_searchScope.value == SearchScope.Posts || _searchScope.value == SearchScope.Rides) return
        val keyword = _searchText.value.trim()
        val request = courseRequest(keyword)
        if (request == lastCourseRequest) return
        courseJob?.cancel()
        courseGeneration++
        resetCoursePagination()
        if (keyword.isBlank() && (_searchScope.value != SearchScope.Courses || _courseFilterState.value.isEmpty())) {
            _courses.value = emptyList()
            _state.value = ViewState.Loaded
            return
        }
        courseJob = viewModelScope.launch {
            _state.value = ViewState.Loading
            try {
                searchCourses(request)
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
        val pagination = araPagination
        if (araPageJob?.isActive == true || !pagination.hasMore) return
        if (pagination.keyword != _searchText.value.trim()) return
        araPageJob = viewModelScope.launch {
            try {
                searchAra(pagination.keyword, pagination.currentPage + 1)
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                Timber.e(e, "Failed to load next page of posts")
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
        araPageJob?.cancel()
        resetCoursePagination()
        _courses.value = emptyList()
        _posts.value = emptyList()
        _taxiRooms.value = emptyList()
        araPagination = PaginationInfo()
    }

    override suspend fun bind() {
        performSearch(_searchText.value.trim())
    }

    override suspend fun fetchInitialData() {
        performSearch(_searchText.value.trim())
    }

    override suspend fun scopedFetch() {
        performSearch(_searchText.value.trim())
    }
}