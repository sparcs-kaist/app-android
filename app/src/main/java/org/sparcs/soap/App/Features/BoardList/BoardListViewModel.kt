package org.sparcs.soap.App.Features.BoardList

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import org.sparcs.soap.App.Domain.Models.Ara.AraBoard
import org.sparcs.soap.App.Domain.Models.Ara.AraBoardGroup
import org.sparcs.soap.App.Domain.Usecases.Ara.AraBoardUseCaseProtocol
import javax.inject.Inject

interface BoardListViewModelProtocol {
    val state: StateFlow<BoardListViewModel.ViewState>
    fun fetchBoards()
}

@HiltViewModel
class BoardListViewModel @Inject constructor(
    private val araBoardUseCase: AraBoardUseCaseProtocol,
) : ViewModel(), BoardListViewModelProtocol {

    sealed class ViewState {
        data object Loading : ViewState()
        data class Loaded(val boards: List<AraBoard>, val groups: List<AraBoardGroup>) : ViewState()
        data class Error(val error: Exception) : ViewState()
    }

    // MARK: - Properties
    private val _state = MutableStateFlow<ViewState>(ViewState.Loading)
    override val state: StateFlow<ViewState> = _state.asStateFlow()

    private var boards: List<AraBoard> = emptyList()
        private set
    private var groups: List<AraBoardGroup> = emptyList()
        private set

    override fun fetchBoards() {
        viewModelScope.launch {
            try {
                val fetchedBoards = araBoardUseCase.fetchBoards()

                val sortedBoards = fetchedBoards.sortedBy { it.id }
                val uniqueGroups =
                    sortedBoards.map { it.group }.distinctBy { it.id }.sortedBy { it.id }

                boards = sortedBoards
                groups = uniqueGroups
                _state.value = ViewState.Loaded(sortedBoards, uniqueGroups)
            } catch (e: Exception) {
                _state.value = ViewState.Error(e)
            }
        }
    }
}