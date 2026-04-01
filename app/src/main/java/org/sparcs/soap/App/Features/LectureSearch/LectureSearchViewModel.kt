package org.sparcs.soap.App.Features.LectureSearch

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import org.sparcs.soap.App.Domain.Models.OTL.CourseLecture
import org.sparcs.soap.App.Domain.Models.OTL.LectureSearchRequest
import org.sparcs.soap.App.Domain.Models.OTL.Semester
import org.sparcs.soap.App.Domain.Services.AnalyticsServiceProtocol
import org.sparcs.soap.App.Domain.Services.CrashlyticsServiceProtocol
import org.sparcs.soap.App.Domain.Usecases.OTL.LectureUseCaseProtocol
import org.sparcs.soap.App.Features.LectureSearch.Event.LectureSearchViewEvent
import timber.log.Timber
import javax.inject.Inject

interface LectureSearchViewModelProtocol {
    val state: StateFlow<LectureSearchViewModel.ViewState>
    val courses: StateFlow<List<CourseLecture>>
    val searchText: StateFlow<String>

    fun bind(selectedSemester: Semester)
    fun fetchLectures(selectedSemester: Semester)
    fun onSearchTextChange(text: String)
}

@HiltViewModel
class LectureSearchViewModel @Inject constructor(
    private val lectureUseCase: LectureUseCaseProtocol,
    private val crashlyticsService: CrashlyticsServiceProtocol,
    private val analyticsService: AnalyticsServiceProtocol,
) : ViewModel(), LectureSearchViewModelProtocol {

    sealed class ViewState {
        data object Loading : ViewState()
        data object Loaded : ViewState()
        data class Error(val error: Exception) : ViewState()
    }

    private val _state = MutableStateFlow<ViewState>(ViewState.Loaded)
    override val state: StateFlow<ViewState> = _state

    private val _courses = MutableStateFlow<List<CourseLecture>>(emptyList())
    override val courses: StateFlow<List<CourseLecture>> = _courses

    private val _searchText = MutableStateFlow("")
    override val searchText: StateFlow<String> = _searchText.asStateFlow()

    private var isBound = false

    override fun onSearchTextChange(text: String) {
        _searchText.value = text
    }

    @OptIn(FlowPreview::class)
    override fun bind(selectedSemester: Semester) {
        if (isBound) return
        isBound = true
        viewModelScope.launch {
            _searchText
                .map { it.trim() }
                .distinctUntilChanged()
                .debounce(350)
                .distinctUntilChanged()
                .collectLatest { query ->
                    if (query.isEmpty()) {
                        _courses.value = emptyList()
                        _state.value = ViewState.Loading
                        return@collectLatest
                    }

                    fetchLectures(selectedSemester, query)
                }
        }
    }

    override fun fetchLectures(selectedSemester: Semester) {
        fetchLectures(selectedSemester, _searchText.value)
    }

    private fun fetchLectures(selectedSemester: Semester, keyword: String) {
        if (keyword.isBlank()) return

        viewModelScope.launch {
            try {
                _state.value = ViewState.Loading

                val request = LectureSearchRequest(
                    semester = selectedSemester,
                    keyword = keyword,
                    limit = 100,
                    offset = 0
                )

                val result = lectureUseCase.searchLecture(request)
                _courses.value = result
                _state.value = ViewState.Loaded

                analyticsService.logEvent(LectureSearchViewEvent.LecturesSearched)
            } catch (e: Exception) {
                Timber.e(e, "Search failed")
                crashlyticsService.recordException(e)
                _state.value = ViewState.Error(e)
            }
        }
    }
}