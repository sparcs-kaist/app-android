package com.example.soap.Features.Search

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.soap.Domain.Enums.PostListType
import com.example.soap.Domain.Models.Ara.AraPost
import com.example.soap.Domain.Models.OTL.Course
import com.example.soap.Domain.Models.SearchScope
import com.example.soap.Domain.Models.Taxi.TaxiRoom
import com.example.soap.Domain.Repositories.Ara.AraBoardRepositoryProtocol
import com.example.soap.Domain.Repositories.OTL.OTLCourseRepositoryProtocol
import com.example.soap.Domain.Repositories.Taxi.TaxiRoomRepositoryProtocol
import com.example.soap.Domain.Usecases.TaxiLocationUseCaseProtocol
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import javax.inject.Inject

interface SearchViewModelProtocol {
    val courses: StateFlow<List<Course>>
    val posts: StateFlow<List<AraPost>>
    val taxiRooms: StateFlow<List<TaxiRoom>>

    val state: StateFlow<SearchViewModel.ViewState>
    val searchText: StateFlow<String>
    val searchScope: StateFlow<SearchScope>

    suspend fun bind()
    suspend fun fetchInitialData()
    suspend fun loadAraNextPage()
    fun loadFull()
    suspend fun scopedFetch()
    fun onSearchTextChange(text: String)
    fun onScopeChange(scope: SearchScope)
}

@HiltViewModel
class SearchViewModel @Inject constructor(
    private val araBoardRepository: AraBoardRepositoryProtocol,
    private val taxiRoomRepository: TaxiRoomRepositoryProtocol,
    private val taxiLocationUseCase: TaxiLocationUseCaseProtocol,
    private val otlCourseRepository: OTLCourseRepositoryProtocol
) : ViewModel(), SearchViewModelProtocol {

    // MARK: - Properties
    sealed class ViewState {
        data object Loading : ViewState()
        data object Loaded : ViewState()
        data class Error(val message: String) : ViewState()
    }

    private val _state = MutableStateFlow<ViewState>(ViewState.Loading)
    override val state: StateFlow<ViewState> = _state

    private val _courses = MutableStateFlow<List<Course>>(emptyList())
    override val courses: StateFlow<List<Course>> = _courses

    private val _posts = MutableStateFlow<List<AraPost>>(emptyList())
    override val posts: StateFlow<List<AraPost>> = _posts

    private val _taxiRooms = MutableStateFlow<List<TaxiRoom>>(emptyList())
    override val taxiRooms: StateFlow<List<TaxiRoom>> = _taxiRooms

    private val _searchText = MutableStateFlow("")
    override val searchText: StateFlow<String> = _searchText

    private val _searchScope = MutableStateFlow(SearchScope.All)
    override val searchScope: StateFlow<SearchScope> = _searchScope

    private val searchKeywordFlow = MutableSharedFlow<String>(extraBufferCapacity = 1)

    // Infinite scroll vars
    private var isLoadingMore = false
    private var hasMorePages = true
    private var currentPage = 1
    private var totalPages = 0
    private var pageSize = 30

    override fun onSearchTextChange(text: String) {
        _searchText.value = text
        viewModelScope.launch {
            if (text.isNotBlank()) scopedFetch()
        }
    }

    override fun onScopeChange(scope: SearchScope) {
        _searchScope.value = scope
        searchKeywordFlow.tryEmit(searchText.value)
    }

    @OptIn(FlowPreview::class)
    override suspend fun bind() {
        searchKeywordFlow
            .map { it.trim() }
            .distinctUntilChanged()
            .onEach { text ->
                if (text.isNotEmpty()) _state.value = ViewState.Loading
            }
            .debounce(350)
            .filter { it.isNotEmpty() }
            .onEach {
                _courses.value = emptyList()
                _posts.value = emptyList()
                _taxiRooms.value = emptyList()
                scopedFetch()
            }
            .launchIn(viewModelScope)
    }

    override suspend fun fetchInitialData() {
        _state.value = ViewState.Loading

        try {
            val keyword = searchText.value

            val postPage = araBoardRepository.fetchPosts(
                type = PostListType.All,
                page = 1,
                pageSize = pageSize,
                searchKeyword = keyword
            )

            totalPages = postPage.pages
            currentPage = postPage.currentPage
            _posts.value = postPage.results
            hasMorePages = currentPage < totalPages

            val fetchedRooms = taxiRoomRepository.fetchRooms()

            taxiLocationUseCase.fetchLocations()
            val matchedLocations = taxiLocationUseCase.queryLocation(keyword)

            val added = mutableSetOf<TaxiRoom>()
            val matchedRooms = mutableListOf<TaxiRoom>()

            fetchedRooms.forEach { room ->
                matchedLocations.forEach { location ->
                    if ((room.source.id == location.id || room.destination.id == location.id) && added.add(room)) {
                        matchedRooms.add(room)
                    }
                }
                if (room.title.lowercase().contains(keyword.lowercase().trim()) && added.add(room)) {
                    matchedRooms.add(room)
                }
            }

            _taxiRooms.value = matchedRooms

            _courses.value = otlCourseRepository.searchCourse(
                name = keyword,
                offset = 0,
                limit = 150
            )

            _state.value = ViewState.Loaded

        } catch (e: Exception) {
            _state.value = ViewState.Error(e.localizedMessage ?: "Unknown error")
        }
    }

    override suspend fun loadAraNextPage() {
        if (isLoadingMore || !hasMorePages) return
        isLoadingMore = true

        try {
            val nextPage = currentPage + 1
            val page = araBoardRepository.fetchPosts(
                type = PostListType.All,
                page = nextPage,
                pageSize = pageSize,
                searchKeyword = searchText.value
            )
            currentPage = page.currentPage
            _posts.value += page.results
            hasMorePages = currentPage < totalPages
            isLoadingMore = false
            _state.value = ViewState.Loaded

        } catch (e: Exception) {
            _state.value = ViewState.Error(e.localizedMessage ?: "Unknown error")
            isLoadingMore = false
        }
    }

    override fun loadFull() {
        _state.value = ViewState.Loaded
    }

    override suspend fun scopedFetch() {
        fetchInitialData()
        if (searchScope.value != SearchScope.All) {
            loadFull()
        }
    }
}