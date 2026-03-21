package org.sparcs.soap.App.Features.LectureDetail

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.gson.Gson
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import org.sparcs.soap.App.Domain.Models.OTL.Course
import org.sparcs.soap.App.Domain.Models.OTL.Lecture
import org.sparcs.soap.App.Domain.Models.OTL.LectureHistory
import org.sparcs.soap.App.Domain.Models.OTL.LectureReview
import org.sparcs.soap.App.Domain.Models.OTL.Semester
import org.sparcs.soap.App.Domain.Services.AnalyticsServiceProtocol
import org.sparcs.soap.App.Domain.Services.CrashlyticsServiceProtocol
import org.sparcs.soap.App.Domain.Usecases.OTL.CourseUseCaseProtocol
import org.sparcs.soap.App.Domain.Usecases.OTL.ReviewUseCaseProtocol
import org.sparcs.soap.App.Domain.Usecases.OTL.TimetableUseCaseProtocol
import org.sparcs.soap.App.Domain.Usecases.UserUseCaseProtocol
import org.sparcs.soap.App.Features.LectureDetail.Event.LectureDetailViewEvent
import org.sparcs.soap.App.Shared.Extensions.unescapeHash
import timber.log.Timber
import java.util.Date
import javax.inject.Inject

interface LectureDetailViewModelProtocol {
    val lecture: StateFlow<Lecture>
    val course: StateFlow<Course?>
    val state: StateFlow<LectureDetailViewModel.ViewState>
    val reviews: StateFlow<List<LectureReview>>

    val writtenReview: StateFlow<LectureReview?>
    val canWriteReview: StateFlow<Boolean>

    suspend fun fetchCourse(courseID: Int)
    fun fetchReviews(lecture: Lecture)
    fun toggleReviewLike(review: LectureReview)
    fun updateWrittenReview(newReview: LectureReview)
}

@HiltViewModel
class LectureDetailViewModel @Inject constructor(
    private val courseUseCase: CourseUseCaseProtocol,
    private val reviewUseCase: ReviewUseCaseProtocol,
    private val timetableUseCase: TimetableUseCaseProtocol,
    private val userUseCase: UserUseCaseProtocol,
    private val crashlyticsService: CrashlyticsServiceProtocol,
    private val analyticsService: AnalyticsServiceProtocol,
    savedStateHandle: SavedStateHandle,
) : ViewModel(), LectureDetailViewModelProtocol {

    sealed class ViewState {
        data object Loading : ViewState()
        data object Loaded : ViewState()
        data class Error(val message: String) : ViewState()
    }

    private val initialLecture: Lecture by lazy {
        val json = savedStateHandle.get<String>("lecture_json")?.unescapeHash()
            ?: throw IllegalStateException("lecture_json is null. LectureDetailViewModel requires a lecture_json to initialize.")
        Gson().fromJson(json, Lecture::class.java)
    }

    // MARK: - Properties
    private val _lecture = MutableStateFlow(initialLecture)
    override val lecture: StateFlow<Lecture> = _lecture.asStateFlow()

    private val _course = MutableStateFlow<Course?>(null)
    override val course: StateFlow<Course?> = _course.asStateFlow()

    private val _state = MutableStateFlow<ViewState>(ViewState.Loading)
    override val state: StateFlow<ViewState> = _state

    private val _reviews = MutableStateFlow<List<LectureReview>>(emptyList())
    override val reviews: StateFlow<List<LectureReview>> = _reviews.asStateFlow()

    private val _writtenReview = MutableStateFlow<LectureReview?>(null)
    override val writtenReview: StateFlow<LectureReview?> = _writtenReview.asStateFlow()

    private val _canWriteReview = MutableStateFlow(false)
    override val canWriteReview: StateFlow<Boolean> = _canWriteReview.asStateFlow()

    init {
        val json = savedStateHandle.get<String>("lecture_json")?.unescapeHash()
        if (json != null) {
            val initialLecture = Gson().fromJson(json, Lecture::class.java)

            viewModelScope.launch {
                fetchCourse(initialLecture.courseID)
                fetchReviews(initialLecture)
            }
        } else {
            _state.value = ViewState.Error("Lecture data is missing")
        }
    }

    override suspend fun fetchCourse(courseID: Int) {
        try {
            _course.value = courseUseCase.getCourse(courseID = courseID)
            analyticsService.logEvent(LectureDetailViewEvent.CourseLoaded)
        } catch (e: Exception) {
            crashlyticsService.recordException(e)
            _state.value = ViewState.Error(message = e.localizedMessage ?: "Unknown Error")
        }
    }

    override fun fetchReviews(lecture: Lecture) {
        viewModelScope.launch {
            try {
                _state.value = ViewState.Loading

                ensureUserLoaded()

                val currentUserId = userUseCase.otlUser?.id

                val allReviewsDef = async {
                    reviewUseCase.fetchReviews(
                        lecture.courseID,
                        lecture.professors.firstOrNull()?.id,
                        0,
                        100
                    )
                }
                val myWrittenReviewsDef = async { reviewUseCase.getWrittenReviews() }
                val currentSemesterDef = async { timetableUseCase.getCurrentSemester() }

                val historyDef = async {
                    if (currentUserId != null) reviewUseCase.fetchLectureHistory(currentUserId) else null
                }

                val allReviews = allReviewsDef.await().reviews
                val myTotalReviews = myWrittenReviewsDef.await()
                val currentSemester = currentSemesterDef.await()
                val historyList = historyDef.await()

                splitMyReviewFromOthers(allReviews, myTotalReviews, lecture.courseID)
                updateWritingPermission(lecture, historyList, currentSemester)

                _state.value = ViewState.Loaded
                analyticsService.logEvent(LectureDetailViewEvent.ReviewsLoaded)
            } catch (e: Exception) {
                crashlyticsService.recordException(e)
                _state.value = ViewState.Error(message = e.localizedMessage ?: "Unknown Error")
            }
        }
    }

    private suspend fun ensureUserLoaded() {
        if (userUseCase.otlUser == null) {
            try {
                userUseCase.fetchOTLUser()
            } catch (e: Exception) {
                Timber.e(e, "User fetch failed")
            }
        }
    }

    private fun updateWritingPermission(
        currentLecture: Lecture,
        historyList: List<LectureHistory>?,
        currentSemester: Semester?,
    ) {
        if (currentSemester == null) {
            _canWriteReview.value = false
            return
        }

        val targetId = currentLecture.id.toString().trim()

        var matchedHistory: LectureHistory? = null
        historyList?.forEach { history ->
            val hasMatch = history.lectures.any { it.lectureId.toString().trim() == targetId }
            if (hasMatch) {
                matchedHistory = history
                return@forEach
            }
        }

        if (matchedHistory != null) {
            val history = matchedHistory!!
            val isCurrentSemester =
                history.year.toString().trim() == currentSemester.year.toString().trim() &&
                        history.semester.toString()
                            .trim() == currentSemester.semesterType.toString().trim()

            _canWriteReview.value = if (isCurrentSemester) {
                val dropDeadline = currentSemester.eventDate?.dropDeadlineDate
                val now = Date()
                val isAfter = dropDeadline?.before(now) ?: false
                isAfter
            } else {
                true
            }
        } else {
            _canWriteReview.value = false
        }
    }

    private fun splitMyReviewFromOthers(
        allReviews: List<LectureReview>,
        myTotalReviews: List<LectureReview>,
        courseId: Int,
    ) {
        val myReview = myTotalReviews.find { it.courseID == courseId }
        _writtenReview.value = myReview
        _reviews.value =
            if (myReview != null) allReviews.filter { it.id != myReview.id } else allReviews
    }

    override fun toggleReviewLike(review: LectureReview) {
        val currentReviews = _reviews.value
        val isCurrentlyLiked = review.likedByUser

        val updatedReviews = currentReviews.map { target ->
            if (target.id == review.id) {
                val nextLikeCount = if (isCurrentlyLiked) target.like - 1 else target.like + 1
                target.copy(
                    likedByUser = !isCurrentlyLiked,
                    like = nextLikeCount
                )
            } else {
                target
            }
        }.toList()

        _reviews.value = updatedReviews

        viewModelScope.launch {
            try {
                reviewUseCase.likeReview(review.id, !isCurrentlyLiked)
                analyticsService.logEvent(LectureDetailViewEvent.ReviewLiked)
            } catch (e: Exception) {
                _reviews.value = currentReviews
                crashlyticsService.recordException(e)
            }
        }
    }

    override fun updateWrittenReview(newReview: LectureReview) {
        _writtenReview.value = newReview
    }
}