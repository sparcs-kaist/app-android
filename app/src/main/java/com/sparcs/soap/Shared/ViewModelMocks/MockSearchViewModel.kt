package com.sparcs.soap.Shared.ViewModelMocks

import com.sparcs.soap.Domain.Models.Ara.AraPost
import com.sparcs.soap.Domain.Models.OTL.Course
import com.sparcs.soap.Domain.Models.SearchScope
import com.sparcs.soap.Domain.Models.Taxi.TaxiRoom
import com.sparcs.soap.Features.Search.SearchViewModel
import com.sparcs.soap.Features.Search.SearchViewModelProtocol
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class MockSearchViewModel(initialState: SearchViewModel.ViewState) : SearchViewModelProtocol {

    private val _courses = MutableStateFlow<List<Course>>(emptyList())
    override val courses: StateFlow<List<Course>> = _courses.asStateFlow()

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
    override suspend fun loadAraNextPage() {}
    override fun loadFull() {}
    override suspend fun scopedFetch() {}
    override fun onSearchTextChange(text: String) {}
    override fun onScopeChange(scope: SearchScope) {}
}
