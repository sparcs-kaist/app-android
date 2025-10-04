package com.example.soap.Features.Course

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.soap.Domain.Models.OTL.LectureReview
import com.example.soap.Domain.Repositories.OTL.OTLCourseRepositoryProtocol
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class CourseViewModel @Inject constructor(
    private val otlCourseRepository: OTLCourseRepositoryProtocol
) : ViewModel() {

    sealed class ViewState {
        object Loading : ViewState()
        object Loaded : ViewState()
        data class Error(val message: String) : ViewState()
    }

    // MARK: - State
    private val _reviews = MutableStateFlow<List<LectureReview>>(emptyList())
    val reviews = _reviews.asStateFlow()

    private val _state = MutableStateFlow<ViewState>(ViewState.Loading)
    val state = _state.asStateFlow()

    // MARK: - Functions
    fun fetchReviews(courseId: Int) {
        viewModelScope.launch {
            try {
                _state.value = ViewState.Loading
                val result = otlCourseRepository.fetchReviews(courseId, 0, 100)
                _reviews.value = result
                _state.value = ViewState.Loaded
            } catch (e: Exception) {
                e.printStackTrace()
                _state.value = ViewState.Error(e.localizedMessage ?: "Unknown error")
            }
        }
    }
}
