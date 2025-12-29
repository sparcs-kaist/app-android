package org.sparcs.App.Features.LectureSearch

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.launch
import org.sparcs.App.Domain.Models.OTL.Lecture
import org.sparcs.App.Domain.Models.OTL.LectureSearchRequest
import org.sparcs.App.Domain.Repositories.OTL.OTLLectureRepositoryProtocol
import org.sparcs.App.Domain.Usecases.TimetableUseCaseProtocol
import javax.inject.Inject

interface LectureSearchViewModelProtocol {
    val state: StateFlow<LectureSearchViewModel.ViewState>
    val lectures: StateFlow<List<Lecture>>
    var searchText: StateFlow<String>

    fun bind()
    fun fetchLectures()
    fun onSearchTextChange(text: String)
}

@HiltViewModel
class LectureSearchViewModel @Inject constructor(
    private val otlLectureRepository: OTLLectureRepositoryProtocol,
    private val timetableUseCase: TimetableUseCaseProtocol,
) : ViewModel(), LectureSearchViewModelProtocol {

    sealed class ViewState {
        data object Loaded : ViewState()
        data class Error(val message: String) : ViewState()
    }

    private val _state = MutableStateFlow<ViewState>(ViewState.Loaded)
    override val state: StateFlow<ViewState> = _state

    private val _lectures = MutableStateFlow<List<Lecture>>(emptyList())
    override val lectures: StateFlow<List<Lecture>> = _lectures

    private val _searchText = MutableStateFlow("")
    override var searchText: StateFlow<String> = _searchText
    private val searchKeywordFlow = MutableSharedFlow<String>(extraBufferCapacity = 1)

    private val itemsPerPage = 50
    private var currentPage = 0

    private val isLastPage: Boolean
        get() = currentPage * itemsPerPage > _lectures.value.size

    private var isBound = false

    override fun onSearchTextChange(text: String) {
        _searchText.value = text
        viewModelScope.launch {
            if (text.isNotBlank()) {
                fetchLectures()
                searchKeywordFlow.emit(text)
            }
        }
    }

    @OptIn(FlowPreview::class)
    override fun bind() {
        if(isBound) return
        isBound = true
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


    override fun fetchLectures() {
        Log.d("Asdasd", timetableUseCase.selectedSemester.value.toString())
        val selectedSemester = timetableUseCase.selectedSemester.value ?: return
        if (isLastPage) return

        viewModelScope.launch {
            try {
                val request = LectureSearchRequest(
                    semester = selectedSemester,
                    keyword = searchText.value,
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