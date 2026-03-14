package org.sparcs.soap.App.Features.LectureDetail

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.gson.Gson
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import org.sparcs.soap.App.Domain.Models.OTL.Lecture
import org.sparcs.soap.App.Domain.Models.OTL.Review
import org.sparcs.soap.App.Domain.Models.OTL.ReviewResponse
import org.sparcs.soap.App.Domain.Repositories.OTL.OTLReviewRepositoryProtocol
import org.sparcs.soap.App.Domain.Repositories.OTL.OTLTimetableRepositoryProtocol
import org.sparcs.soap.App.Domain.Usecases.OTL.TimetableUseCaseProtocol
import org.sparcs.soap.App.Domain.Usecases.UserUseCaseProtocol
import org.sparcs.soap.App.Shared.Extensions.unescapeHash
import timber.log.Timber
import java.util.*
import javax.inject.Inject

interface LectureDetailViewModelProtocol {
    val lecture: StateFlow<Lecture>
    val state: StateFlow<LectureDetailViewModel.ViewState>
    val reviews: StateFlow<ReviewResponse>
    val canWriteReview: StateFlow<Boolean>
    val writtenReview: StateFlow<Review?>
    val isInCurrentTimetable: Boolean
    fun fetchReviews()
    fun writeReview(content: String, grade: Int, load: Int, speech: Int, editing: Boolean)
}

@HiltViewModel
class LectureDetailViewModel @Inject constructor(
    val userUseCase: UserUseCaseProtocol,
    val otlTimetableRepository: OTLTimetableRepositoryProtocol,
    val otlReviewRepository: OTLReviewRepositoryProtocol,
    val timetableUseCase: TimetableUseCaseProtocol,
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
    private val initialWrittenReview: Review? by lazy {
        val json = savedStateHandle.get<String>("written_review_json")?.unescapeHash() ?: return@lazy null
        Gson().fromJson(json, Review::class.java)
    }

    // MARK: - Properties
    private val _lecture = MutableStateFlow(initialLecture)
    override val lecture: StateFlow<Lecture> = _lecture.asStateFlow()

    private val _state = MutableStateFlow<ViewState>(ViewState.Loading)
    override val state: StateFlow<ViewState> = _state

    private val _reviews = MutableStateFlow<ReviewResponse>(ReviewResponse(emptyList(), 0.0, 0.0, 0.0))
    override val reviews: StateFlow<ReviewResponse> = _reviews

    private val _canWriteReview = MutableStateFlow(false)
    override val canWriteReview: StateFlow<Boolean> = _canWriteReview.asStateFlow()

    private val _writtenReview = MutableStateFlow<Review?>(initialWrittenReview)
    override val writtenReview: StateFlow<Review?> = _writtenReview.asStateFlow()

    override val isInCurrentTimetable = timetableUseCase.hasLectureInCurrentTable(lecture.value)

    override fun fetchReviews() {
        viewModelScope.launch {
            try {
                _reviews.value = otlReviewRepository.fetchReviews(
                    courseId = lecture.value.courseId, professorId = lecture.value.professors[0].id, offset = 0, limit = 100)
                checkCanWriteAndWritten()
            } catch (e: Exception) {
                Timber.e(e, "fetchReviews failed")
                _state.value = ViewState.Error(e.localizedMessage ?: "Unknown error")
            } finally {
                _state.value = ViewState.Loaded
            }
        }
    }

    private suspend fun checkCanWriteAndWritten() {
        val semesters = otlTimetableRepository.getSemesters()
        val historyList = userUseCase.otlUser?.id?.let { otlReviewRepository.fetchLectureHistory(it) }

        historyList?.forEach { history ->
            history.lectures.find { it.lectureId == lecture.value.id }?.let { takenLecture ->
                val semester = semesters.find { it.year == history.year && it.semesterType == history.semester }
                val isBeforeDropDeadline = semester?.eventDate?.dropDeadlineDate?.before(Date()) ?: false

                _canWriteReview.value = isBeforeDropDeadline
                if (takenLecture.written) {
                    val writtenReviews = otlReviewRepository.getWrittenReviews()
                    _writtenReview.value = writtenReviews.find { it.lectureId == lecture.value.id }
                }
                return
            }
        }
    }

    override fun writeReview(content: String, grade: Int, load: Int, speech: Int, editing: Boolean) {
        viewModelScope.launch {
            try {
                if (editing) {
                    otlReviewRepository.updateReview(
                        reviewId = _writtenReview.value?.id ?: throw IllegalStateException("No review to edit"),
                        content = content,
                        grade = grade,
                        load = load,
                        speech = speech
                    )
                } else {
                    otlReviewRepository.createReview(lectureId = _lecture.value.id, content = content, grade = grade, load = load, speech = speech)
                }
            } catch (e: Exception) {
                Timber.e(e, "writeReview failed")
            }
        }
    }
}
