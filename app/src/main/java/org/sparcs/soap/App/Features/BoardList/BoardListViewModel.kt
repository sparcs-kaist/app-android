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
import org.sparcs.soap.App.Domain.Repositories.Ara.AraBoardRepositoryProtocol
import org.sparcs.soap.App.Shared.Extensions.isNetworkError
import org.sparcs.soap.R
import javax.inject.Inject


@HiltViewModel
class BoardListViewModel @Inject constructor(
    private val araBoardRepository: AraBoardRepositoryProtocol,
) : ViewModel() {

    sealed class ViewState {
        data object Loading : ViewState()
        data class Loaded(val boards: List<AraBoard>, val groups: List<AraBoardGroup>) : ViewState()
        data class Error(val message: Int) : ViewState()
    }

    // MARK: - Properties
    private val _state = MutableStateFlow<ViewState>(ViewState.Loading)
    val state: StateFlow<ViewState> = _state.asStateFlow()

    private var boards: List<AraBoard> = emptyList()
        private set
    private var groups: List<AraBoardGroup> = emptyList()
        private set

    fun fetchBoards() {
        viewModelScope.launch {
            try {
                val fetchedBoards = araBoardRepository.fetchBoards()

                val sortedBoards = fetchedBoards.sortedBy { it.id }
                val uniqueGroups =
                    sortedBoards.map { it.group }.distinctBy { it.id }.sortedBy { it.id }

                boards = sortedBoards
                groups = uniqueGroups
                _state.value = ViewState.Loaded(sortedBoards, uniqueGroups)
            } catch (e: Exception) {
                if(e.isNetworkError()) {
                    _state.value = ViewState.Error(R.string.network_connection_error)
                } else {
                    _state.value = ViewState.Error(R.string.error_fetch_boards)
                }
            }
        }
    }
}