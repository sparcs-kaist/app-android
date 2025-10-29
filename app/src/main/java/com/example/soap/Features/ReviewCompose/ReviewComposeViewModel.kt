package com.example.soap.Features.ReviewCompose

import android.net.Uri
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import com.example.soap.Domain.Models.OTL.Lecture
import com.example.soap.Domain.Repositories.OTL.FakeOTLLectureRepository
import com.example.soap.Domain.Repositories.OTL.OTLLectureRepositoryProtocol
import com.example.soap.Shared.Mocks.mock
import com.google.gson.Gson
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

interface ReviewComposeViewModelProtocol {
    val lecture: Lecture
}

@HiltViewModel
class ReviewComposeViewModel @Inject constructor(
    val otlLectureRepository: OTLLectureRepositoryProtocol,
    savedStateHandle: SavedStateHandle
) : ViewModel(), ReviewComposeViewModelProtocol{

    private val initialLecture: Lecture by lazy {
        val json = savedStateHandle.get<String>("lecture_json")
            ?: throw IllegalStateException("lecture_json is null. ReviewComposeViewModel requires a lecture_json to initialize.")
        Gson().fromJson(Uri.decode(json), Lecture::class.java)
    }

    override val lecture: Lecture = initialLecture
}

class MockReviewComposeViewModel : ReviewComposeViewModelProtocol {
    override val lecture: Lecture = Lecture.mock()
}

