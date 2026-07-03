package org.sparcs.soap.app.shared.viewModelMocks

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import org.sparcs.soap.app.domain.models.ara.AraPost
import org.sparcs.soap.app.domain.models.otl.CourseSummary
import org.sparcs.soap.app.domain.models.SearchScope
import org.sparcs.soap.app.domain.models.taxi.TaxiRoom
import org.sparcs.soap.app.features.search.SearchViewModel
import org.sparcs.soap.app.features.search.SearchViewModelProtocol

class MockSearchViewModel(initialState: SearchViewModel.ViewState) : SearchViewModelProtocol {

    private val _courses = MutableStateFlow<List<CourseSummary>>(emptyList())
    override val courses: StateFlow<List<CourseSummary>> = _courses.asStateFlow()

    private val _posts = MutableStateFlow<List<AraPost>>(emptyList())
    override val posts: StateFlow<List<AraPost>> = _posts.asStateFlow()

    private val _taxiRooms = MutableStateFlow<List<TaxiRoom>>(emptyList())
    override val taxiRooms: StateFlow<List<TaxiRoom>> = _taxiRooms.asStateFlow()

    private val _state = MutableStateFlow(initialState)
    override val state: StateFlow<SearchViewModel.ViewState> = _state.asStateFlow()

    private val _searchText = MutableStateFlow("")
    override val searchText: StateFlow<String> = _searchText.asStateFlow()

    private val _searchScope = MutableStateFlow(SearchScope.All)
    override val searchScope: StateFlow<SearchScope> = _searchScope.asStateFlow()

    override suspend fun bind() {}
    override suspend fun fetchInitialData() {}
    override fun loadAraNextPage() {}
    override fun loadFull() {}
    override suspend fun scopedFetch() {}
    override fun onSearchTextChange(text: String) {}
    override fun onScopeChange(scope: SearchScope) {}
}
