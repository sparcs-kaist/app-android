package org.sparcs.soap.buddyPreviewSupport.otl

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import org.sparcs.soap.app.domain.helpers.AlertState
import org.sparcs.soap.app.domain.models.otl.CourseFilterState
import org.sparcs.soap.app.domain.models.otl.CourseLecture
import org.sparcs.soap.app.domain.models.otl.Lecture
import org.sparcs.soap.app.domain.models.otl.LectureTimeFilter
import org.sparcs.soap.app.domain.models.otl.Semester
import org.sparcs.soap.app.features.lectureSearch.LectureSearchViewModel
import org.sparcs.soap.app.features.lectureSearch.LectureSearchViewModelProtocol
import org.sparcs.soap.app.shared.mocks.otl.mockList

class PreviewLectureSearchViewModel(initialState: LectureSearchViewModel.ViewState) :
    LectureSearchViewModelProtocol {

    private val _state = MutableStateFlow(initialState)
    override val state: StateFlow<LectureSearchViewModel.ViewState> = _state.asStateFlow()

    private val _searchText = MutableStateFlow("")
    override val searchText: StateFlow<String> = _searchText.asStateFlow()

    private val _courseFilterState = MutableStateFlow(CourseFilterState())
    override val courseFilterState: StateFlow<CourseFilterState> = _courseFilterState.asStateFlow()

    override val wishlistState = MutableStateFlow<LectureSearchViewModel.ViewState>(LectureSearchViewModel.ViewState.Loaded(CourseLecture.mockList()))
    override val wishlistedLectureIDs = MutableStateFlow(wishlistState.value.courses.flatMap { it.lectures }.map { it.id }.toSet())

    override var alertState: AlertState? by mutableStateOf(null)
    override var isAlertPresented: Boolean by mutableStateOf(false)

    override fun toggleWishlist(lecture: Lecture) {}
    override fun fetchWishlist(semester: Semester) {}
    override val time = MutableStateFlow(LectureTimeFilter())
    override val pagination = MutableStateFlow(LectureSearchViewModel.PaginationState.Idle())
    override fun onTimeChange(time: LectureTimeFilter) { this.time.value = time }
    override fun loadNextPage() {}

    override fun bind(selectedSemester: Semester) {}

    override fun fetchLectures(selectedSemester: Semester) {}

    override fun onSearchTextChange(text: String) {
        _searchText.value = text
    }

    override fun onFilterChange(filterState: CourseFilterState) {
        _courseFilterState.value = filterState
    }
}