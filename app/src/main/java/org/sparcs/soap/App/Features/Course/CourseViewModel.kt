package org.sparcs.soap.App.Features.Course

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import org.sparcs.soap.App.Domain.Models.OTL.Course
import org.sparcs.soap.App.Domain.Models.OTL.ReviewResponse
import org.sparcs.soap.App.Domain.Repositories.OTL.OTLCourseRepositoryProtocol
import org.sparcs.soap.App.Domain.Repositories.OTL.OTLReviewRepositoryProtocol
import javax.inject.Inject


interface CourseViewModelProtocol {
    val state: StateFlow<CourseViewModel.ViewState>
    fun loadCourse()
}

@HiltViewModel
class CourseViewModel @Inject constructor(
    val otlCourseRepository: OTLCourseRepositoryProtocol,
    val otlReviewRepository: OTLReviewRepositoryProtocol,
    savedStateHandle: SavedStateHandle,
) : ViewModel(), CourseViewModelProtocol {

    sealed class ViewState {
        data object Loading : ViewState()
        data class Loaded(
            val course: Course,
            val reviews: ReviewResponse
        ) : ViewState()
        data class Error(val message: String) : ViewState()
    }

    private val initialCourseId: String? = savedStateHandle["courseId"]

    // MARK: - State
    private val _state = MutableStateFlow<ViewState>(ViewState.Loading)
    override val state = _state.asStateFlow()

    init {
        loadCourse()
    }

    override fun loadCourse() {
        initialCourseId?.let { id ->
            viewModelScope.launch {
                try {
                    _state.value = ViewState.Loading

                    val reviewsDeferred = async { otlReviewRepository.fetchReviews(courseId = id.toInt(), offset = 0, limit = 100) }
                    val courseDeferred = async { otlCourseRepository.getCourseDetail(id.toInt()) }

                    _state.value = ViewState.Loaded(
                        course = courseDeferred.await(),
                        reviews = reviewsDeferred.await()
                    )
                } catch (e: Exception) {
                    _state.value = ViewState.Error(e.localizedMessage ?: "Failed to load course / review")
                }
            }
        }
    }
}
