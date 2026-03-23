package org.sparcs.soap.App.Features.ReviewCompose

import android.net.Uri
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import com.google.gson.Gson
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import org.sparcs.soap.App.Domain.Helpers.AlertState
import org.sparcs.soap.App.Domain.Models.OTL.Lecture
import org.sparcs.soap.App.Domain.Models.OTL.LectureReview
import org.sparcs.soap.App.Domain.Services.CrashlyticsServiceProtocol
import org.sparcs.soap.App.Domain.Usecases.OTL.ReviewUseCaseProtocol
import org.sparcs.soap.App.Networking.ResponseDTO.OTL.ratingToString
import org.sparcs.soap.R
import javax.inject.Inject

interface ReviewComposeViewModelProtocol {
    val lecture: Lecture
    val writtenReview: StateFlow<LectureReview?>

    val alertState: AlertState?
    var isAlertPresented: Boolean
    var isUploading: Boolean

    suspend fun submitReview(
        content: String,
        grade: Int,
        load: Int,
        speech: Int,
    ): LectureReview?

    fun handleException(error: Throwable)
}

@HiltViewModel
class ReviewComposeViewModel @Inject constructor(
    private val reviewUseCase: ReviewUseCaseProtocol,
    private val crashlyticsService: CrashlyticsServiceProtocol,
    savedStateHandle: SavedStateHandle,
) : ViewModel(), ReviewComposeViewModelProtocol {

    private val initialLecture: Lecture by lazy {
        val json = savedStateHandle.get<String>("lecture_json")
            ?: throw IllegalStateException("lecture_json is null. ReviewComposeViewModel requires a lecture_json to initialize.")
        Gson().fromJson(Uri.decode(json), Lecture::class.java)
    }

    private val initialWrittenReview: LectureReview? by lazy {
        val json = savedStateHandle.get<String>("written_review_json") ?: return@lazy null
        Gson().fromJson(Uri.decode(json), LectureReview::class.java)
    }

    private val _writtenReview = MutableStateFlow(initialWrittenReview)
    override val writtenReview: StateFlow<LectureReview?> = _writtenReview.asStateFlow()

    override var alertState: AlertState? by mutableStateOf(null)
    override var isAlertPresented: Boolean by mutableStateOf(false)
    override var isUploading: Boolean by mutableStateOf(false)

    override val lecture: Lecture = initialLecture
    override suspend fun submitReview(
        content: String,
        grade: Int,
        load: Int,
        speech: Int,
    ): LectureReview? {
        if (content.isBlank()) return null
        isUploading = true

        return try {
            val currentReview = _writtenReview.value

            if (currentReview != null) {
                reviewUseCase.updateReview(currentReview.id, content, grade, load, speech)
            } else {
                reviewUseCase.writeReview(lecture.id, content, grade, load, speech)
            }

            currentReview?.copy(
                content = content,
                grade = ratingToString(grade),
                load = ratingToString(load),
                speech = ratingToString(speech)
            )
        } catch (e: Exception) {
            handleException(e)
            alertState = AlertState(
                titleResId = R.string.unexpected_error_uploading_review,
                message = e.localizedMessage ?: "Unknown error"
            )
            isAlertPresented = true
            null
        } finally {
            isUploading = false
        }
    }

    override fun handleException(error: Throwable) {
        crashlyticsService.recordException(error)
    }
}