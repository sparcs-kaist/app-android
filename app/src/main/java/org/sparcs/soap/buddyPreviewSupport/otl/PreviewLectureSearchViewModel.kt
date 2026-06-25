package org.sparcs.soap.buddyPreviewSupport.otl

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import org.sparcs.soap.app.domain.models.otl.CourseLecture
import org.sparcs.soap.app.domain.models.otl.Semester
import org.sparcs.soap.app.features.lectureSearch.LectureSearchViewModel
import org.sparcs.soap.app.features.lectureSearch.LectureSearchViewModelProtocol
import org.sparcs.soap.app.shared.mocks.otl.mockList

class PreviewLectureSearchViewModel(initialState: LectureSearchViewModel.ViewState) :
    LectureSearchViewModelProtocol {

    private val _state = MutableStateFlow(initialState)
    override val state: StateFlow<LectureSearchViewModel.ViewState> = _state.asStateFlow()

    private val _courses = MutableStateFlow(CourseLecture.mockList())
    override val courses: StateFlow<List<CourseLecture>> = _courses.asStateFlow()

    private val _searchText = MutableStateFlow("")
    override val searchText: StateFlow<String> = _searchText.asStateFlow()

    override fun bind(selectedSemester: Semester) {}

    override fun fetchLectures(selectedSemester: Semester) {}

    override fun onSearchTextChange(text: String) {
        _searchText.value = text
    }
}