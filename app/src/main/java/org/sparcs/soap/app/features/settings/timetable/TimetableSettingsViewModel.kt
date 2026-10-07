package org.sparcs.soap.app.features.settings.timetable

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Job
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import org.sparcs.soap.app.domain.models.otl.DepartmentOption
import org.sparcs.soap.app.domain.services.CrashlyticsServiceProtocol
import org.sparcs.soap.app.domain.usecases.UserUseCaseProtocol
import org.sparcs.soap.app.domain.usecases.otl.LectureUseCaseProtocol
import javax.inject.Inject

interface TimetableSettingsViewModelProtocol {
    val state: StateFlow<TimetableSettingsViewModel.ViewState>
    fun load()
    fun fetchDepartments()
    fun selectDepartments(ids: Set<Int>)
    fun save(onSaved: () -> Unit)
}

@HiltViewModel
class TimetableSettingsViewModel @Inject constructor(
    private val userUseCase: UserUseCaseProtocol,
    private val lectureUseCase: LectureUseCaseProtocol,
    private val crashlyticsService: CrashlyticsServiceProtocol,
) : ViewModel(), TimetableSettingsViewModelProtocol {

    sealed class ViewState {
        data object Loading : ViewState()
        data class Loaded(
            val departments: List<DepartmentOption> = emptyList(),
            val savedDepartmentIDs: Set<Int> = emptySet(),
            val selectedDepartmentIDs: Set<Int> = emptySet(),
            val isSaving: Boolean = false,
        ) : ViewState() {
            val hasChanges get() = savedDepartmentIDs != selectedDepartmentIDs
        }
        data class Error(val error: Exception, val resId: Int? = null) : ViewState()
    }

    private val _state = MutableStateFlow<ViewState>(ViewState.Loading)
    override val state: StateFlow<ViewState> = _state.asStateFlow()

    private var loadJob: Job? = null

    override fun load() = load(requireUser = true)

    override fun fetchDepartments() = load(requireUser = false)

    private fun load(requireUser: Boolean) {
        val currentLoaded = _state.value as? ViewState.Loaded
        if (currentLoaded?.isSaving == true) return
        loadJob?.cancel()
        loadJob = viewModelScope.launch {
            _state.value = ViewState.Loading
            try {
                coroutineScope {
                    val options = async { lectureUseCase.fetchDepartmentOptions() }
                    try {
                        userUseCase.fetchOTLUser()
                    } catch (e: CancellationException) {
                        throw e
                    } catch (e: Exception) {
                        if (requireUser) throw e
                        crashlyticsService.recordException(e)
                    }
                    val ids =
                        userUseCase.otlUser?.interestedDepartments.orEmpty().map { it.id }.toSet()
                    val departments = options.await()
                    ensureActive()
                    _state.value = ViewState.Loaded(departments, ids, ids)
                }
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                ensureActive()
                crashlyticsService.recordException(e)
                _state.value = ViewState.Error(e)
            }
        }
    }

    override fun selectDepartments(ids: Set<Int>) {
        val current = _state.value as? ViewState.Loaded ?: return
        if (!current.isSaving) {
            _state.value = current.copy(selectedDepartmentIDs = ids)
        }
    }

    override fun save(onSaved: () -> Unit) {
        val current = _state.value as? ViewState.Loaded ?: return
        if (current.isSaving || !current.hasChanges) return
        val ids = current.selectedDepartmentIDs
        _state.value = current.copy(isSaving = true)
        viewModelScope.launch {
            try {
                userUseCase.updateInterestedDepartments(ids.sorted())
                _state.value = current.copy(savedDepartmentIDs = ids, selectedDepartmentIDs = ids, isSaving = false)
                onSaved()
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                ensureActive()
                crashlyticsService.recordException(e)
                _state.value = current.copy(isSaving = false)
            }
        }
    }
}
