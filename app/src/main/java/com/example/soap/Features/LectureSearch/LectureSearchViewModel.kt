package com.example.soap.Features.LectureSearch

import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.soap.Domain.Models.OTL.Lecture
import com.example.soap.Domain.Models.OTL.LectureSearchRequest
import com.example.soap.Domain.Repositories.OTL.OTLLectureRepository
import com.example.soap.Domain.Usecases.TimetableUseCaseProtocol
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class LectureSearchViewModel @Inject constructor(
    private val otlLectureRepository: OTLLectureRepository,
    private val timetableUseCase: TimetableUseCaseProtocol
) : ViewModel() {

    sealed class ViewState {
        data object Loaded : ViewState()
        data class Error(val message: String) : ViewState()
    }

    private val _state = MutableStateFlow<ViewState>(ViewState.Loaded)
    val state: StateFlow<ViewState> = _state

    private val _lectures = MutableStateFlow<List<Lecture>>(emptyList())
    val lectures: StateFlow<List<Lecture>> = _lectures

    var searchKeyword by mutableStateOf("")

    private val searchKeywordFlow = MutableStateFlow("")

    private val itemsPerPage = 50
    private var currentPage = 0

    val isLastPage: Boolean
        get() = currentPage * itemsPerPage > _lectures.value.size

    fun bind() {
        viewModelScope.launch {
            searchKeywordFlow
                .debounce(350)
                .distinctUntilChanged()
                .collectLatest {
                    _lectures.value = emptyList()
                    _state.value = ViewState.Loaded
                    currentPage = 0
                    fetchLectures()
                }
        }
    }

    fun fetchLectures() {
        val selectedSemester = timetableUseCase.selectedSemester ?: return
        if (isLastPage) return

        viewModelScope.launch {
            try {
                val request = LectureSearchRequest(
                    semester = selectedSemester,
                    keyword = searchKeyword,
                    limit = itemsPerPage,
                    offset = currentPage * itemsPerPage
                )
                val page = otlLectureRepository.searchLectures(request)
                _lectures.value += page
                currentPage++
            } catch (e: Exception) {
                Log.e("LectureSearchVM", "fetchLectures failed", e)
                _state.value = ViewState.Error(e.localizedMessage ?: "Unknown error")
            }
        }
    }
}