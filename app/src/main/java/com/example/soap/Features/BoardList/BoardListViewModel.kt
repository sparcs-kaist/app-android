package com.example.soap.Features.BoardList

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.soap.Domain.Models.Ara.AraBoard
import com.example.soap.Domain.Models.Ara.AraBoardGroup
import com.example.soap.Domain.Repositories.Ara.AraBoardRepositoryProtocol
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject


@HiltViewModel
class BoardListViewModel @Inject constructor(
    private val araBoardRepository: AraBoardRepositoryProtocol
) : ViewModel() {

    sealed class ViewState {
        data object Loading : ViewState()
        data class Loaded(val boards: List<AraBoard>, val groups: List<AraBoardGroup>) : ViewState()
        data class Error(val message: String) : ViewState()
    }

    // MARK: - Properties
    private val _state = MutableStateFlow<ViewState>(ViewState.Loading)
    val state: StateFlow<ViewState> = _state.asStateFlow()

    var boards: List<AraBoard> = emptyList()
        private set
    var groups: List<AraBoardGroup> = emptyList()
        private set

    fun fetchBoards() {
        viewModelScope.launch {
            try {
                val fetchedBoards = araBoardRepository.fetchBoards()

                val sortedBoards = fetchedBoards.sortedBy { it.id }
                val uniqueGroups = sortedBoards.map { it.group }.distinctBy { it.id }.sortedBy { it.id }

                boards = sortedBoards
                groups = uniqueGroups
                _state.value = ViewState.Loaded(sortedBoards, uniqueGroups)
            } catch (e: Exception) {
                _state.value = ViewState.Error("Failed to load boards.")
            }
        }
    }
}