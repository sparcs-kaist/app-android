package org.sparcs.Features.LectureDetail

import android.util.Log
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.gson.Gson
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import org.sparcs.Domain.Models.OTL.Lecture
import org.sparcs.Domain.Models.OTL.LectureReview
import org.sparcs.Domain.Repositories.OTL.OTLCourseRepositoryProtocol
import org.sparcs.Domain.Repositories.OTL.OTLLectureRepository
import org.sparcs.Domain.Usecases.TimetableUseCaseProtocol
import org.sparcs.Domain.Usecases.UserUseCaseProtocol
import org.sparcs.Shared.Extensions.unescapeHash
import javax.inject.Inject

interface LectureDetailViewModelProtocol{
    val lecture: StateFlow<Lecture>
    val state: StateFlow<LectureDetailViewModel.ViewState>
    val reviews: StateFlow<List<LectureReview>>
    val isInCurrentTimetable: Boolean
    fun fetchReviews(lectureID: Int)
    fun writeReview(lectureID: Int,review: LectureReview)
}

@HiltViewModel
class LectureDetailViewModel @Inject constructor(
    private val otlLectureRepository: OTLLectureRepository,
    val userUseCase: UserUseCaseProtocol,
    val otlCourseRepository: OTLCourseRepositoryProtocol,
    val timetableUseCase: TimetableUseCaseProtocol,
    savedStateHandle: SavedStateHandle
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
    override val lecture : StateFlow<Lecture> = _lecture.asStateFlow()

    private val _state = MutableStateFlow<ViewState>(ViewState.Loading)
    override val state: StateFlow<ViewState> = _state

    private val _reviews = MutableStateFlow<List<LectureReview>>(emptyList())
    override val reviews: StateFlow<List<LectureReview>> = _reviews

    override val isInCurrentTimetable = timetableUseCase.hasLectureInCurrentTable(lecture.value)

    override fun fetchReviews(lectureID: Int) {
        viewModelScope.launch {
            try {
                val result = otlLectureRepository.fetchLectures(lectureID)
                _reviews.value = result
            } catch (e: Exception) {
                Log.e("LectureDetailVM", "fetchReviews failed", e)
                _state.value = ViewState.Error(e.localizedMessage ?: "Unknown error")
            } finally {
                _state.value = ViewState.Loaded
            }
        }
    }

    override fun writeReview(lectureID: Int,review: LectureReview){
        viewModelScope.launch {
            try {
                val result = otlLectureRepository.writeReview(
                    lectureID = lectureID,
                    content = review.content,
                    grade = review.grade,
                    load = review.load,
                    speech = review.speech
                )
                _reviews.value += result
            } catch (e: Exception) {
                Log.e("LectureDetailVM", "writeReview failed", e)
            }
        }
    }
}
