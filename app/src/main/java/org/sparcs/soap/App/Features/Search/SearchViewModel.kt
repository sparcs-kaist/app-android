package org.sparcs.soap.App.Features.Search

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import org.sparcs.soap.App.Domain.Enums.Ara.PostListType
import org.sparcs.soap.App.Domain.Models.Ara.AraPost
import org.sparcs.soap.App.Domain.Models.OTL.CourseSearchRequest
import org.sparcs.soap.App.Domain.Models.OTL.CourseSummary
import org.sparcs.soap.App.Domain.Models.SearchScope
import org.sparcs.soap.App.Domain.Models.Taxi.TaxiRoom
import org.sparcs.soap.App.Domain.Repositories.Taxi.TaxiRoomRepositoryProtocol
import org.sparcs.soap.App.Domain.Usecases.Ara.AraBoardUseCaseProtocol
import org.sparcs.soap.App.Domain.Usecases.OTL.CourseUseCaseProtocol
import org.sparcs.soap.App.Domain.Usecases.Taxi.TaxiLocationUseCaseProtocol
import javax.inject.Inject

interface SearchViewModelProtocol {
    val courses: StateFlow<List<CourseSummary>>
    val posts: StateFlow<List<AraPost>>
    val taxiRooms: StateFlow<List<TaxiRoom>>

    val state: StateFlow<SearchViewModel.ViewState>
    val searchText: StateFlow<String>
    val searchScope: StateFlow<SearchScope>

    suspend fun bind()
    suspend fun fetchInitialData()
    fun loadAraNextPage()
    fun loadFull()
    suspend fun scopedFetch()
    fun onSearchTextChange(text: String)
    fun onScopeChange(scope: SearchScope)
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

    private var araPagination = PaginationInfo()

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
        _searchText
            .debounce(350)
            .filter { it.isNotBlank() }
            .distinctUntilChanged()
            .onEach { performSearch(it) }
            .launchIn(viewModelScope)
    }

    private suspend fun performSearch(keyword: String) {
        _state.value = ViewState.Loading
        resetSearchState()

        try {
            val jobs = listOf(
                viewModelScope.launch { searchAra(keyword) },
                viewModelScope.launch { searchTaxi(keyword) },
                viewModelScope.launch { searchCourses(keyword) }
            )
            jobs.forEach { it.join() }
            _state.value = ViewState.Loaded
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
        val matchedLocations = taxiLocationUseCase.queryLocation(keyword)
        val searchKeyword = keyword.lowercase().trim()

        _taxiRooms.value = allRooms.filter { room ->
            val matchesLocation =
                matchedLocations.any { it.id == room.source.id || it.id == room.destination.id }
            val matchesTitle = room.title.lowercase().contains(searchKeyword)
            matchesLocation || matchesTitle
        }.distinctBy { it.id }
    }

    private suspend fun searchCourses(keyword: String) {
        _courses.value = courseUseCase.searchCourse(
            CourseSearchRequest(keyword = keyword, offset = 0, limit = 150)
        )
    }

    override fun onSearchTextChange(text: String) {
        _searchText.value = text
    }

    override fun onScopeChange(scope: SearchScope) {
        _searchScope.value = scope
        viewModelScope.launch { performSearch(_searchText.value) }
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

    private fun resetSearchState() {
        _courses.value = emptyList()
        _posts.value = emptyList()
        _taxiRooms.value = emptyList()
        araPagination = PaginationInfo()
    }

    override suspend fun bind() {}
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