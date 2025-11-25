package com.sparcs.soap.Shared.ViewModelMocks.OTL

import com.sparcs.soap.Domain.Models.OTL.Lecture
import com.sparcs.soap.Features.LectureSearch.LectureSearchViewModel
import com.sparcs.soap.Features.LectureSearch.LectureSearchViewModelProtocol
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class MockLectureSearchViewModel(initialState: LectureSearchViewModel.ViewState) : LectureSearchViewModelProtocol {

    private val _state = MutableStateFlow(initialState)
    override val state: StateFlow<LectureSearchViewModel.ViewState> = _state

    private val _lectures = MutableStateFlow<List<Lecture>>(listOf())
    override val lectures: StateFlow<List<Lecture>> = _lectures.asStateFlow()

    override var searchKeyword: String = ""
    override val searchKeywordFlow = MutableStateFlow("")

    override fun bind() {}
    override fun fetchLectures() {}
}
