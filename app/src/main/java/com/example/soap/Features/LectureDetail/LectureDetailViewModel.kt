package com.example.soap.Features.LectureDetail

import android.util.Log
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.soap.Domain.Models.OTL.Lecture
import com.example.soap.Domain.Models.OTL.LectureReview
import com.example.soap.Domain.Repositories.OTL.OTLCourseRepositoryProtocol
import com.example.soap.Domain.Repositories.OTL.OTLLectureRepository
import com.example.soap.Domain.Usecases.UserUseCaseProtocol
import com.google.gson.Gson
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class LectureDetailViewModel @Inject constructor(
    private val otlLectureRepository: OTLLectureRepository,
    val userUseCase: UserUseCaseProtocol,
    val otlCourseRepository: OTLCourseRepositoryProtocol,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    sealed class ViewState {
        data object Loading : ViewState()
        data object Loaded : ViewState()
    }

    private val initialLecture: Lecture by lazy {
        val json = savedStateHandle.get<String>("lecture_json")
            ?: throw IllegalStateException("lecture_json is null. LectureDetailViewModel requires a lecture_json to initialize.")
        Gson().fromJson(json, Lecture::class.java)
    }

    // MARK: - Properties
    private val _lecture = MutableStateFlow(initialLecture)
    val lecture : StateFlow<Lecture> = _lecture.asStateFlow()

    private val _state = MutableStateFlow<ViewState>(ViewState.Loading)
    val state: StateFlow<ViewState> = _state

    private val _reviews = MutableStateFlow<List<LectureReview>>(emptyList())
    val reviews: StateFlow<List<LectureReview>> = _reviews

    fun fetchReviews(lectureID: Int) {
        viewModelScope.launch {
            try {
                val result = otlLectureRepository.fetchLectures(lectureID)
                _reviews.value = result
            } catch (e: Exception) {
                Log.e("LectureDetailVM", "fetchReviews failed", e)
            } finally {
                _state.value = ViewState.Loaded
            }
        }
    }
}
