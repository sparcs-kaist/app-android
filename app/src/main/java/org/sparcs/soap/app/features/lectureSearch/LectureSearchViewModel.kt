package org.sparcs.soap.app.features.lectureSearch

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.Job
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import org.sparcs.soap.R
import org.sparcs.soap.app.domain.helpers.AlertState
import org.sparcs.soap.app.domain.models.otl.CourseFilterState
import org.sparcs.soap.app.domain.models.otl.CourseLecture
import org.sparcs.soap.app.domain.models.otl.ETC_DEPARTMENT_ID
import org.sparcs.soap.app.domain.models.otl.Lecture
import org.sparcs.soap.app.domain.models.otl.LectureSearchRequest
import org.sparcs.soap.app.domain.models.otl.LectureTimeFilter
import org.sparcs.soap.app.domain.models.otl.Semester
import org.sparcs.soap.app.domain.services.AnalyticsServiceProtocol
import org.sparcs.soap.app.domain.services.CrashlyticsServiceProtocol
import org.sparcs.soap.app.domain.usecases.otl.LectureUseCaseProtocol
import org.sparcs.soap.app.domain.usecases.otl.WishlistUseCase
import org.sparcs.soap.app.features.lectureSearch.event.LectureSearchViewEvent
import org.sparcs.soap.app.shared.extensions.toAlertState
import javax.inject.Inject

interface LectureSearchViewModelProtocol {
    val state: StateFlow<LectureSearchViewModel.ViewState>
    val searchText: StateFlow<String>
    val courseFilterState: StateFlow<CourseFilterState>

    val wishlistState: StateFlow<LectureSearchViewModel.ViewState>
    val wishlistedLectureIDs: StateFlow<Set<Int>>
    var alertState: AlertState?
    var isAlertPresented: Boolean
    fun toggleWishlist(lecture: Lecture)
    fun fetchWishlist(semester: Semester)
    val time: StateFlow<LectureTimeFilter>
    val pagination: StateFlow<LectureSearchViewModel.PaginationState>
    fun onTimeChange(time: LectureTimeFilter)
    fun loadNextPage()
    fun bind(selectedSemester: Semester)
    fun fetchLectures(selectedSemester: Semester)
    fun onSearchTextChange(text: String)
    fun onFilterChange(filterState: CourseFilterState)
}

@HiltViewModel
class LectureSearchViewModel @Inject constructor(
    private val lectureUseCase: LectureUseCaseProtocol,
    private val crashlyticsService: CrashlyticsServiceProtocol,
    private val analyticsService: AnalyticsServiceProtocol,
    private val wishlistUseCase: WishlistUseCase? = null,
) : ViewModel(), LectureSearchViewModelProtocol {

    sealed class ViewState {
        open val courses: List<CourseLecture> = emptyList()

        data object Loading : ViewState()
        data class Loaded(override val courses: List<CourseLecture> = emptyList()) : ViewState()
        data class Error(val error: Exception, override val courses: List<CourseLecture> = emptyList()) : ViewState()
    }

    sealed interface PaginationState {
        data class Idle(val hasMore: Boolean = false) : PaginationState
        data object Loading : PaginationState
        data class Error(val error: Exception) : PaginationState
    }

    private val _state = MutableStateFlow<ViewState>(ViewState.Loaded())
    override val state = _state.asStateFlow()

    private val _searchText = MutableStateFlow("")
    override val searchText = _searchText.asStateFlow()

    private val _courseFilterState = MutableStateFlow(CourseFilterState())
    override val courseFilterState = _courseFilterState.asStateFlow()

    private val _time = MutableStateFlow(LectureTimeFilter())
    override val time = _time.asStateFlow()

    private var currentSemester: Semester? = null
    private var isBound = false
    private var searchJob: Job? = null
    private var currentRequest: LectureSearchRequest? = null

    private val _pagination = MutableStateFlow<PaginationState>(PaginationState.Idle())
    override val pagination = _pagination.asStateFlow()
    private var nextOffset = 0

    private val _wishlistState = MutableStateFlow<ViewState>(ViewState.Loaded())
    override val wishlistState = _wishlistState.asStateFlow()

    private val _wishlistedLectureIDs = MutableStateFlow<Set<Int>>(emptySet())
    override val wishlistedLectureIDs = _wishlistedLectureIDs.asStateFlow()

    private val wishlistMutex = Mutex()
    private val wishlistChanges = mutableSetOf<Int>()
    private var wishlistJob: Job? = null

    override var alertState: AlertState? by mutableStateOf(null)
    override var isAlertPresented: Boolean by mutableStateOf(false)

    override fun fetchWishlist(semester: Semester) {
        val useCase = wishlistUseCase ?: return
        wishlistJob?.cancel()
        wishlistJob = viewModelScope.launch {
            val previous = _wishlistState.value.courses
            _wishlistState.value = ViewState.Loading
            try {
                wishlistMutex.withLock {
                    val result = useCase.fetchWishlist(semester)
                    ensureActive()
                    if (semester == currentSemester) {
                        _wishlistState.value = ViewState.Loaded(result)
                        _wishlistedLectureIDs.value = result.flatMap { it.lectures }.map { it.id }.toSet()
                    }
                }
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                crashlyticsService.recordException(e)
                if (semester == currentSemester) _wishlistState.value = ViewState.Error(e, previous)
            }
        }
    }

    override fun toggleWishlist(lecture: Lecture) {
        val useCase = wishlistUseCase ?: return
        val semester = currentSemester ?: return
        if (!wishlistChanges.add(lecture.id)) return
        viewModelScope.launch {
            try {
                wishlistMutex.withLock {
                    if (semester != currentSemester) return@withLock
                    val previous = _wishlistState.value
                    val previousIDs = _wishlistedLectureIDs.value
                    val isAdding = lecture.id !in previousIDs
                    _wishlistedLectureIDs.value = if (isAdding) previousIDs + lecture.id else previousIDs - lecture.id
                    if (!isAdding) _wishlistState.value = ViewState.Loaded(previous.courses.mapNotNull { course ->
                        course.copy(lectures = course.lectures.filter { it.id != lecture.id }).takeIf { it.lectures.isNotEmpty() }
                    })
                    try {
                        useCase.setWishlisted(isAdding, lecture.id)
                    } catch (e: CancellationException) {
                        throw e
                    } catch (e: Exception) {
                        if (semester == currentSemester) {
                            _wishlistState.value = previous
                            _wishlistedLectureIDs.value = previousIDs
                            presentWishlistError(e)
                        }
                        crashlyticsService.recordException(e)
                        return@withLock
                    }
                    if (isAdding && semester == currentSemester) {
                        try {
                            val result = useCase.fetchWishlist(semester)
                            if (semester == currentSemester) _wishlistState.value = ViewState.Loaded(result)
                        } catch (e: CancellationException) {
                            throw e
                        } catch (e: Exception) {
                            crashlyticsService.recordException(e)
                            if (semester == currentSemester) presentWishlistError(e)
                        }
                    }
                }
            } finally {
                wishlistChanges.remove(lecture.id)
            }
        }
    }

    override fun onSearchTextChange(text: String) {
        if (text.trim() == _searchText.value.trim()) {
            _searchText.value = text
            return
        }
        searchJob?.cancel()
        currentRequest = null
        _pagination.value = PaginationState.Idle()
        _state.value = if (currentSemester != null) ViewState.Loading else ViewState.Loaded()
        _searchText.value = text
    }

    override fun onFilterChange(filterState: CourseFilterState) {
        if (_courseFilterState.value == filterState) return
        _courseFilterState.value = filterState
        currentSemester?.let(::fetchLectures)
    }

    override fun onTimeChange(time: LectureTimeFilter) {
        if (_time.value == time) return
        _time.value = time
        currentSemester?.let(::fetchLectures)
    }

    @OptIn(FlowPreview::class)
    override fun bind(selectedSemester: Semester) {
        val changed = currentSemester != selectedSemester
        currentSemester = selectedSemester
        if (changed) {
            _wishlistState.value = ViewState.Loaded()
            _wishlistedLectureIDs.value = emptySet()
            fetchWishlist(selectedSemester)
        }
        if (isBound) {
            if (changed) fetchLectures(selectedSemester)
            return
        }
        isBound = true
        viewModelScope.launch {
            _searchText.map { it.trim() }.distinctUntilChanged().debounce(350).collectLatest {
                currentSemester?.let(::fetchLectures)
            }
        }
    }

    override fun fetchLectures(selectedSemester: Semester) {
        searchJob?.cancel()
        currentRequest = null
        nextOffset = 0
        _pagination.value = PaginationState.Idle()
        _state.value = ViewState.Loaded()
        val keyword = _searchText.value.trim()
        val filter = _courseFilterState.value
        if (keyword.isBlank() && filter.isEmpty() && _time.value.isEmpty) {
            _state.value = ViewState.Loaded()
            return
        }
        currentRequest = LectureSearchRequest(
            semester = selectedSemester, keyword = keyword, limit = 100, offset = 0,
            type = filter.classifications.ifEmpty { null },
            department = filter.departments.filter { it != ETC_DEPARTMENT_ID }.ifEmpty { null },
            level = filter.levels.ifEmpty { null }, term = filter.period, time = _time.value,
        )
        _state.value = ViewState.Loading
        searchPage(isFirstPage = true)
    }

    override fun loadNextPage() {
        when (val page = _pagination.value) {
            is PaginationState.Idle -> if (!page.hasMore) return
            PaginationState.Loading -> return
            is PaginationState.Error -> Unit
        }
        searchPage(isFirstPage = false)
    }

    private fun searchPage(isFirstPage: Boolean) {
        val request = currentRequest?.copy(offset = nextOffset) ?: return
        _pagination.value = PaginationState.Loading
        searchJob = viewModelScope.launch {
            try {
                val result = lectureUseCase.searchLecture(request)
                ensureActive()
                val count = result.sumOf { it.lectures.size }
                nextOffset += count
                _state.value = ViewState.Loaded(mergeCourses(if (isFirstPage) emptyList() else _state.value.courses, result))
                _pagination.value = PaginationState.Idle(hasMore = count >= request.limit)
                analyticsService.logEvent(LectureSearchViewEvent.LecturesSearched)
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                ensureActive()
                crashlyticsService.recordException(e)
                _pagination.value = if (isFirstPage) PaginationState.Idle() else PaginationState.Error(e)
                if (isFirstPage) _state.value = ViewState.Error(e)
            }
        }
    }

    private fun presentWishlistError(error: Exception) {
        alertState = error.toAlertState(R.string.wishlist_update_failed)
        isAlertPresented = true
    }

    companion object {
        fun mergeCourses(existing: List<CourseLecture>, incoming: List<CourseLecture>): List<CourseLecture> {
            val courses = existing.associateByTo(linkedMapOf()) { it.id }
            incoming.forEach { course ->
                val previous = courses[course.id]
                courses[course.id] = if (previous == null) course else previous.copy(
                    lectures = (previous.lectures + course.lectures).distinctBy { it.id }
                )
            }
            return courses.values.toList()
        }
    }
}
