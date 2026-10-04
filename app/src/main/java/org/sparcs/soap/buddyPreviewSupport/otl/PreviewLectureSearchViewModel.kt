package org.sparcs.soap.buddyPreviewSupport.otl

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
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

    private val _courses = MutableStateFlow(CourseLecture.mockList())
    override val courses: StateFlow<List<CourseLecture>> = _courses.asStateFlow()

    private val _searchText = MutableStateFlow("")
    override val searchText: StateFlow<String> = _searchText.asStateFlow()

    private val _courseFilterState = MutableStateFlow(CourseFilterState())
    override val courseFilterState: StateFlow<CourseFilterState> = _courseFilterState.asStateFlow()

    override val wishlist = MutableStateFlow(CourseLecture.mockList())
    override val wishlistedLectureIDs = MutableStateFlow(wishlist.value.flatMap { it.lectures }.map { it.id }.toSet())
    override val wishlistError = MutableStateFlow<Exception?>(null)
    override val wishlistLoading = MutableStateFlow(false)
    override fun toggleWishlist(lecture: Lecture) {}
    override fun dismissWishlistError() {}
    override fun fetchWishlist(semester: Semester) {}
    override val time = MutableStateFlow(LectureTimeFilter())
    override val pagination = MutableStateFlow(LectureSearchViewModel.PaginationState())
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