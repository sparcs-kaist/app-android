package org.sparcs.Features.Course

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.gson.Gson
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import org.sparcs.Domain.Models.OTL.Course
import org.sparcs.Domain.Models.OTL.LectureReview
import org.sparcs.Domain.Repositories.OTL.OTLCourseRepositoryProtocol
import javax.inject.Inject


interface CourseViewModelProtocol {
    val course: StateFlow<Course>
    val reviews: StateFlow<List<LectureReview>>
    val state: StateFlow<CourseViewModel.ViewState>
    fun fetchReviews(courseId: Int)
}

@HiltViewModel
class CourseViewModel @Inject constructor(
    val otlCourseRepository: OTLCourseRepositoryProtocol,
    savedStateHandle: SavedStateHandle,
) : ViewModel(), CourseViewModelProtocol {

    sealed class ViewState {
        data object Loading : ViewState()
        data object Loaded : ViewState()
        data class Error(val message: String) : ViewState()
    }

    private val initialCourse: Course by lazy {
        val json = savedStateHandle.get<String>("course_json")
            ?: throw IllegalStateException("course_json is null. CourseViewModel requires a course_json to initialize.")
        Gson().fromJson(json, Course::class.java)
    }

    // MARK: - Properties
    private val _course = MutableStateFlow(initialCourse)
    override val course: StateFlow<Course> = _course.asStateFlow()

    // MARK: - State
    private val _reviews = MutableStateFlow<List<LectureReview>>(emptyList())
    override val reviews = _reviews.asStateFlow()

    private val _state = MutableStateFlow<ViewState>(ViewState.Loading)
    override val state = _state.asStateFlow()

    // MARK: - Functions
    override fun fetchReviews(courseId: Int) {
        viewModelScope.launch {
            try {
                _state.value = ViewState.Loading
                val result = otlCourseRepository.fetchReviews(courseId, 0, 100)
                _reviews.value = result
                _state.value = ViewState.Loaded
            } catch (e: Exception) {
                _state.value = ViewState.Error(e.localizedMessage ?: "Unknown error")
            }
        }
    }
}
