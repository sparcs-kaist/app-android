package org.sparcs.soap.app.features.course

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Job
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import org.sparcs.soap.R
import org.sparcs.soap.app.domain.helpers.AlertState
import org.sparcs.soap.app.domain.models.otl.Course
import org.sparcs.soap.app.domain.models.otl.CourseHistory
import org.sparcs.soap.app.domain.models.otl.LectureReview
import org.sparcs.soap.app.domain.models.otl.LectureReviewPage
import org.sparcs.soap.app.domain.models.otl.Professor
import org.sparcs.soap.app.domain.services.AnalyticsServiceProtocol
import org.sparcs.soap.app.domain.services.CrashlyticsServiceProtocol
import org.sparcs.soap.app.domain.usecases.otl.CourseUseCaseProtocol
import org.sparcs.soap.app.domain.usecases.otl.ReviewUseCaseProtocol
import org.sparcs.soap.app.features.course.event.CourseViewEvent
import org.sparcs.soap.app.shared.extensions.toAlertState
import org.sparcs.soap.app.shared.viewModels.TextProcessingProtocol
import javax.inject.Inject

interface CourseViewModelProtocol : TextProcessingProtocol {
    val course: StateFlow<Course?>
    val courseError: StateFlow<Exception?>
    val selectedProfessorID: StateFlow<Int?>
    val professors: List<Professor>
    val state: StateFlow<CourseViewModel.ViewState>

    val alertState: AlertState?
    var isAlertPresented: Boolean

    fun selectProfessor(id: Int?)
    fun fetchReviews()
    fun loadCourse()
    fun toggleReviewLike(review: LectureReview)
}

@HiltViewModel
class CourseViewModel @Inject constructor(
    private val courseUseCase: CourseUseCaseProtocol,
    private val reviewUseCase: ReviewUseCaseProtocol,
    private val crashlyticsService: CrashlyticsServiceProtocol,
    private val analyticsService: AnalyticsServiceProtocol,
    private val textProcessingDelegate: TextProcessingProtocol,
    savedStateHandle: SavedStateHandle,
) : ViewModel(), CourseViewModelProtocol, TextProcessingProtocol by textProcessingDelegate {

    sealed class ViewState {
        data object Loading : ViewState()
        data class Loaded(
            val course: Course?,
            val reviews: List<LectureReview>,
            val writtenReview: LectureReview?,
            val reviewPage: LectureReviewPage,
        ) : ViewState()

        data class Error(val error: Exception) : ViewState()
    }

    private val courseId: Int? = savedStateHandle.get<String>("courseId")?.toIntOrNull()

    override var alertState: AlertState? by mutableStateOf(null)
    override var isAlertPresented: Boolean by mutableStateOf(false)

    // MARK: - State
    private val _state = MutableStateFlow<ViewState>(ViewState.Loading)
    override val state = _state.asStateFlow()

    private val _course = MutableStateFlow<Course?>(null)
    override val course = _course.asStateFlow()

    private val _courseError = MutableStateFlow<Exception?>(null)
    override val courseError = _courseError.asStateFlow()

    private val _selectedProfessorID = MutableStateFlow<Int?>(null)
    override val selectedProfessorID = _selectedProfessorID.asStateFlow()

    override var professors: List<Professor> by mutableStateOf(emptyList())
        private set

    private var courseJob: Job? = null

    private var reviewTask: Job? = null
    private var writtenReviewTask: Job? = null
    private var myTotalReviews: List<LectureReview>? = null

    private val likesInFlight = mutableSetOf<Int>()

    init {
        loadCourse()
        fetchReviews()
    }

    override fun loadCourse() {
        val id = courseId ?: return
        courseJob?.cancel()
        courseJob = viewModelScope.launch {
            _courseError.value = null
            try {
                val loaded = courseUseCase.getCourse(id)
                ensureActive()
                _course.value = loaded.copy(
                    history = loaded.history.sortedWith(
                    compareByDescending<CourseHistory> { it.year }.thenByDescending { it.semester.intValue }
                ))
                professors = loaded.history.flatMap { it.classes }.flatMap { it.professors }
                    .distinctBy { it.id }.sortedBy { it.name }
                (_state.value as? ViewState.Loaded)?.let {
                    _state.value = it.copy(course = _course.value)
                }
                analyticsService.logEvent(CourseViewEvent.CourseLoaded)
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                ensureActive()
                crashlyticsService.recordException(e)
                _courseError.value = e
            }
        }
    }

    override fun selectProfessor(id: Int?) {
        if (id == _selectedProfessorID.value) return
        _selectedProfessorID.value = id
        analyticsService.logEvent(CourseViewEvent.ProfessorSelected)
        fetchReviews()
    }

    override fun fetchReviews() {
        val id = courseId ?: return
        val professorID = _selectedProfessorID.value
        loadWrittenReviews()
        reviewTask?.cancel()
        reviewTask = viewModelScope.launch {
            _state.value = ViewState.Loading
            try {
                val page = reviewUseCase.fetchReviews(id, professorID, 0, 100)
                ensureActive()
                val myReview = findWrittenReview(professorID)
                _state.value = ViewState.Loaded(
                    course = _course.value,
                    reviews = page.reviews.filter { it.id != myReview?.id },
                    writtenReview = myReview,
                    reviewPage = page,
                )
                analyticsService.logEvent(CourseViewEvent.ReviewsLoaded)
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                ensureActive()
                crashlyticsService.recordException(e)
                _state.value = ViewState.Error(e)
            }
        }
    }

    private fun findWrittenReview(professorID: Int?): LectureReview? =
        myTotalReviews?.find { review ->
            review.courseID == courseId && (professorID == null || review.professors.any { it.id == professorID })
        }

    private fun loadWrittenReviews() {
        if (myTotalReviews != null || writtenReviewTask?.isActive == true) return
        writtenReviewTask = viewModelScope.launch {
            try {
                myTotalReviews = reviewUseCase.getWrittenReviews()
                ensureActive()
                val loaded = _state.value as? ViewState.Loaded ?: return@launch
                val myReview = findWrittenReview(_selectedProfessorID.value)
                _state.value = loaded.copy(
                    writtenReview = myReview,
                    reviews = loaded.reviews.filter { it.id != myReview?.id },
                )
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                crashlyticsService.recordException(e)
            }
        }
    }

    override fun toggleReviewLike(review: LectureReview) {
        val currentState = _state.value as? ViewState.Loaded ?: return

        if (review.id == currentState.writtenReview?.id) {
            alertState = AlertState(
                titleResId = R.string.warning,
                messageResId = R.string.review_like_warning
            )
            isAlertPresented = true
            return
        }

        if (!likesInFlight.add(review.id)) return
        val professorID = _selectedProfessorID.value
        val originalReview = currentState.reviews.find { it.id == review.id } ?: review
        val isCurrentlyLiked = originalReview.likedByUser
        val updatedReviews = currentState.reviews.map { target ->
            if (target.id == review.id) {
                val nextLikeCount = if (isCurrentlyLiked) target.like - 1 else target.like + 1
                target.copy(
                    likedByUser = !isCurrentlyLiked,
                    like = nextLikeCount
                )
            } else {
                target
            }
        }

        _state.value = currentState.copy(reviews = updatedReviews.toList())

        viewModelScope.launch {
            try {
                reviewUseCase.likeReview(review.id, !isCurrentlyLiked)
                analyticsService.logEvent(CourseViewEvent.LikeReview)
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                val latest = _state.value as? ViewState.Loaded
                if (latest != null && professorID == _selectedProfessorID.value) {
                    _state.value = latest.copy(reviews = latest.reviews.map {
                        if (it.id == review.id) originalReview else it
                    })
                }
                crashlyticsService.recordException(e)
                alertState = e.toAlertState(R.string.failed_to_like_review)
                isAlertPresented = true
            } finally {
                likesInFlight.remove(review.id)
            }
        }
    }
}