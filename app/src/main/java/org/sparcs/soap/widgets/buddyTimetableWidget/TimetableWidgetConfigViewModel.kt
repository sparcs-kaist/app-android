package org.sparcs.soap.widgets.buddyTimetableWidget

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Job
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.sparcs.soap.app.domain.enums.otl.SemesterType
import org.sparcs.soap.app.domain.models.otl.Semester
import org.sparcs.soap.app.domain.models.otl.Timetable
import org.sparcs.soap.app.domain.models.otl.TimetableSummary
import org.sparcs.soap.app.domain.usecases.otl.TimetableUseCaseProtocol
import timber.log.Timber
import javax.inject.Inject

data class TimetableWidgetConfigState(
    val semesters: List<Semester> = emptyList(),
    val selectedSemester: Semester? = null,
    val timetableList: List<TimetableSummary> = emptyList(),
    val selectedTimetableId: Int = -1,
    val selectedTimetable: Timetable? = null,
)

@HiltViewModel
class TimetableWidgetConfigViewModel @Inject constructor(
    private val timetableUseCase: TimetableUseCaseProtocol,
) : ViewModel() {
    private val _state = MutableStateFlow(TimetableWidgetConfigState())
    val state = _state.asStateFlow()
    private var navigationJob: Job? = null
    private var listJob: Job? = null
    private var tableJob: Job? = null
    private var selectionRevision = 0L

    fun initialize(savedTimetableId: Int, savedYear: Int, savedType: Int) {
        navigationJob?.cancel()
        val revision = selectionRevision
        navigationJob = viewModelScope.launch {
            try {
                val semesters = timetableUseCase.getSemesters().sortedDescending()
                val current = try {
                    timetableUseCase.getCurrentSemester()
                } catch (e: CancellationException) {
                    throw e
                } catch (_: Exception) {
                    semesters.firstOrNull()
                }
                currentCoroutineContext().ensureActive()
                if (revision != selectionRevision) return@launch
                val selected = if (savedYear != -1 && savedType in 1..4) {
                    semesters.find {
                        it.year == savedYear && it.semesterType == SemesterType.fromRawValue(
                            savedType
                        )
                    } ?: current
                } else current
                _state.value = TimetableWidgetConfigState(
                    semesters,
                    selected,
                    selectedTimetableId = savedTimetableId
                )
                loadSelection()
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                Timber.e(e, "Failed to load widget semesters")
            }
        }
    }

    fun selectSemester(semester: Semester) {
        if (_state.value.selectedSemester == semester) return
        selectionRevision++
        _state.update {
            it.copy(
                selectedSemester = semester,
                selectedTimetableId = -1,
                timetableList = emptyList(),
                selectedTimetable = null
            )
        }
        loadSelection()
    }

    fun selectTimetable(id: Int) {
        if (_state.value.selectedTimetableId == id) return
        selectionRevision++
        _state.update { it.copy(selectedTimetableId = id, selectedTimetable = null) }
        loadTable()
    }

    private fun loadSelection() {
        listJob?.cancel()
        val semester = _state.value.selectedSemester ?: return
        listJob = viewModelScope.launch {
            try {
                val cached = timetableUseCase.cachedState(semester, null).timetables
                currentCoroutineContext().ensureActive()
                if (_state.value.selectedSemester != semester) return@launch
                if (cached != null) _state.update { it.copy(timetableList = cached) }
                val fresh = timetableUseCase.refreshTimetableList(semester)
                currentCoroutineContext().ensureActive()
                if (_state.value.selectedSemester != semester) return@launch
                _state.update { it.copy(timetableList = fresh) }
                val id = _state.value.selectedTimetableId
                if (id != -1 && fresh.none { it.id == id }) selectTimetable(-1)
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                Timber.e(e, "Failed to load widget timetable list")
            }
        }
        loadTable()
    }

    private fun loadTable() {
        tableJob?.cancel()
        val selection = _state.value
        val semester = selection.selectedSemester ?: return
        val revision = selectionRevision
        tableJob = viewModelScope.launch {
            try {
                val table = loadTimetableForWidget(selection.selectedTimetableId, semester)
                if (revision == selectionRevision) _state.update { it.copy(selectedTimetable = table) }
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                if (revision == selectionRevision) _state.update { it.copy(selectedTimetable = null) }
                Timber.e(e, "Failed to load widget timetable")
            }
        }
    }

    suspend fun loadTimetableForWidget(id: Int, semester: Semester?): Timetable =
        if (id == -1) timetableUseCase.getMyTable(semester ?: timetableUseCase.getCurrentSemester())
        else timetableUseCase.getTable(id)
}
