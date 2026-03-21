package org.sparcs.soap.BuddyPreviewSupport.OTL

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import org.sparcs.soap.App.Domain.Models.OTL.CourseLecture
import org.sparcs.soap.App.Domain.Models.OTL.Semester
import org.sparcs.soap.App.Features.LectureSearch.LectureSearchViewModel
import org.sparcs.soap.App.Features.LectureSearch.LectureSearchViewModelProtocol
import org.sparcs.soap.App.Shared.Mocks.OTL.mockList

class PreviewLectureSearchViewModel(initialState: LectureSearchViewModel.ViewState) :
    LectureSearchViewModelProtocol {

    private val _state = MutableStateFlow(initialState)
    override val state: StateFlow<LectureSearchViewModel.ViewState> = _state.asStateFlow()

    private val _courses = MutableStateFlow<List<CourseLecture>>(CourseLecture.mockList())
    override val courses: StateFlow<List<CourseLecture>> = _courses.asStateFlow()

    private val _searchText = MutableStateFlow("")
    override val searchText: StateFlow<String> = _searchText.asStateFlow()

    override fun bind(selectedSemester: Semester) {}

    override fun fetchLectures(selectedSemester: Semester) {}

    override fun onSearchTextChange(text: String) {
        _searchText.value = text
    }
}