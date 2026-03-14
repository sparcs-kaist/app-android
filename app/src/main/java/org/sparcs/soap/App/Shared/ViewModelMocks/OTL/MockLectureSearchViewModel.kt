package org.sparcs.soap.App.Shared.ViewModelMocks.OTL

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import org.sparcs.soap.App.Domain.Models.OTL.LectureWrapperCourse
import org.sparcs.soap.App.Features.LectureSearch.LectureSearchViewModel
import org.sparcs.soap.App.Features.LectureSearch.LectureSearchViewModelProtocol

class MockLectureSearchViewModel(initialState: LectureSearchViewModel.ViewState) :
    LectureSearchViewModelProtocol {

    private val _state = MutableStateFlow(initialState)
    override val state: StateFlow<LectureSearchViewModel.ViewState> = _state

    private val _lectures = MutableStateFlow<List<LectureWrapperCourse>>(listOf())
    override val lectures: StateFlow<List<LectureWrapperCourse>> = _lectures.asStateFlow()

    private val _searchText = MutableStateFlow("")
    override var searchText: StateFlow<String> = _searchText
    override fun bind() {}
    override fun fetchLectures() {}
    override fun onSearchTextChange(text: String) {}

}
