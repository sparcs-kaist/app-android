package org.sparcs.soap.App.Features.Timetable

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import org.sparcs.soap.App.Domain.Models.OTL.Lecture
import org.sparcs.soap.App.Domain.Models.OTL.Semester
import org.sparcs.soap.App.Domain.Models.OTL.Timetable
import org.sparcs.soap.App.Domain.Models.OTL.TimetableSummary
import org.sparcs.soap.App.Domain.Services.AnalyticsServiceProtocol
import org.sparcs.soap.App.Domain.Services.CrashlyticsServiceProtocol
import org.sparcs.soap.App.Domain.Usecases.OTL.TimetableUseCase
import org.sparcs.soap.App.Features.Timetable.Event.TimetableViewEvent
import org.sparcs.soap.R
import timber.log.Timber
import javax.inject.Inject

interface TimetableViewModelProtocol {
    val timetableUseCase: TimetableUseCase?
    val isLoading: MutableStateFlow<Boolean>
    val semesters: StateFlow<List<Semester>>
    val selectedSemester: StateFlow<Semester?>
    val selectedTimetable: StateFlow<Timetable?>
    val timetableList: StateFlow<List<TimetableSummary>>
    val selectedTimetableID: StateFlow<Int?>
    val candidateLecture: StateFlow<Lecture?>
    val isCandidateOverlapping: StateFlow<Boolean>
    val overlappingLecture: StateFlow<Lecture?>
    val isEditable: StateFlow<Boolean>

    var showAlert: Boolean
    var alertMessageRes: Int?

    fun setCandidateLecture(lecture: Lecture?)
    fun fetchData()
    suspend fun selectPreviousSemester()
    suspend fun selectNextSemester()
    fun selectTimetable(id: Int)
    fun createTable()
    fun deleteTable()
    fun renameTable(title: String)
    fun addLecture(lecture: Lecture)
    fun deleteLecture(lecture: Lecture)
    fun removeOverlappingLectures(newLecture: Lecture)
}

@HiltViewModel
class TimetableViewModel @Inject constructor(
    override val timetableUseCase: TimetableUseCase,
    private val crashlyticsService: CrashlyticsServiceProtocol,
    private val analyticsService: AnalyticsServiceProtocol,
) : ViewModel(), TimetableViewModelProtocol {

    enum class ErrorType {
        AddLecture,
        CreateTable,
        DeleteTable,
        DeleteLecture,
        FetchData,
        RenameTable
    }

    companion object {
        const val MY_TABLE_ID = -1
    }

    override var showAlert by mutableStateOf(false)
    override var alertMessageRes by mutableStateOf<Int?>(null)

    override val isLoading = MutableStateFlow(false)

    private val _semesters = MutableStateFlow<List<Semester>>(emptyList())
    override val semesters: StateFlow<List<Semester>> = _semesters.asStateFlow()

    private val _selectedSemester = MutableStateFlow<Semester?>(null)
    override val selectedSemester: StateFlow<Semester?> = _selectedSemester.asStateFlow()

    private val _timetableList = MutableStateFlow<List<TimetableSummary>>(emptyList())
    override val timetableList: StateFlow<List<TimetableSummary>> = _timetableList.asStateFlow()

    private val _selectedTimetableID = MutableStateFlow<Int?>(null)
    override val selectedTimetableID: StateFlow<Int?> = _selectedTimetableID.asStateFlow()

    private val _timetable = MutableStateFlow<Timetable?>(null)
    override val selectedTimetable: StateFlow<Timetable?> = _timetable.asStateFlow()

    private val _candidateLecture = MutableStateFlow<Lecture?>(null)
    override val candidateLecture: StateFlow<Lecture?> = _candidateLecture.asStateFlow()

    override val isEditable: StateFlow<Boolean> = _selectedTimetableID
        .map { it != null }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = false
        )

    override val isCandidateOverlapping: StateFlow<Boolean> =
        combine(_timetable, _candidateLecture) { table, candidate ->
            table != null && candidate != null && table.hasCollision(candidate)
        }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = false
        )

    override val overlappingLecture: StateFlow<Lecture?> =
        combine(_timetable, _candidateLecture) { table, candidate ->
            if (table == null || candidate == null) return@combine null
            table.lectures.firstOrNull { table.hasCollision(candidate) && table.hasCollision(it) }
        }.distinctUntilChanged()
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5000),
                initialValue = null
            )


    init { fetchData() }
    // MARK: - Functions
    override fun fetchData() {
        viewModelScope.launch {
            isLoading.value = true
            try {
                val semesterList = timetableUseCase.getSemesters()
                val current = timetableUseCase.getCurrentSemester()
                _semesters.value = semesterList
                _selectedSemester.value = current
                updateTimetableList(current, forceRefresh = true)
            } catch (e: Exception) {
                Timber.e(e, "failed to fetch Timetable Data")
                handleException(e, ErrorType.FetchData)
            } finally {
                isLoading.value = false
            }
        }
    }

    private suspend fun updateTimetableList(semester: Semester, forceRefresh: Boolean = false) {
        try {
            val list = timetableUseCase.getTimetableList(semester)
            _timetableList.value = list

            if (_selectedTimetableID.value == null || list.none { it.id == _selectedTimetableID.value }) {
                _selectedTimetableID.value = list.firstOrNull()?.id
            }
            loadTimetable(forceRefresh = forceRefresh)
        } catch (e: Exception) {
            handleException(e, ErrorType.FetchData)
        }
    }

    private suspend fun loadTimetable(forceRefresh: Boolean = false) {
        val id = _selectedTimetableID.value
        val semester = _selectedSemester.value

        try {
            if (id != null) {
                _timetable.value = timetableUseCase.getTable(id, forceRefresh = forceRefresh)
            } else if (semester != null) {
                _timetable.value = timetableUseCase.getMyTable(semester, forceRefresh = forceRefresh)
            } else {
                _timetable.value = null
            }
        } catch (e: Exception) {
            _timetable.value = null
            handleException(e, ErrorType.FetchData)
        }
    }

    override fun selectTimetable(id: Int) {
        if (id == MY_TABLE_ID) {
            _selectedTimetableID.value = null
        } else {
            if (_selectedTimetableID.value == id) return
            _selectedTimetableID.value = id
        }

        viewModelScope.launch { loadTimetable() }
    }

    override fun setCandidateLecture(lecture: Lecture?) {
        _candidateLecture.value = lecture
    }

    override fun renameTable(title: String) {
        val id = _selectedTimetableID.value ?: return
        viewModelScope.launch {
            try {
                timetableUseCase.renameTable(id, title)
                _timetableList.value = _timetableList.value.map {
                    if (it.id == id) it.copy(title = title) else it
                }
                analyticsService.logEvent(TimetableViewEvent.TableRenamed)
            } catch (e: Exception) {
                Timber.e(e, "Error renaming table")
                handleException(e, ErrorType.RenameTable)
            }
        }
    }

    override fun createTable() {
        val semester = _selectedSemester.value ?: return
        viewModelScope.launch {
            try {
                val creation = timetableUseCase.createTable(semester)
                analyticsService.logEvent(TimetableViewEvent.TableCreated)
                _selectedTimetableID.value = creation.id
                updateTimetableList(semester)
            } catch (e: Exception) {
                Timber.e(e, "Error creating table")
                handleException(e, ErrorType.CreateTable)
            }
        }
    }

    override fun deleteTable() {
        val id = _selectedTimetableID.value ?: return
        viewModelScope.launch {
            try {
                timetableUseCase.deleteTable(id)
                analyticsService.logEvent(TimetableViewEvent.TableDeleted)
                _selectedTimetableID.value = null
                _selectedSemester.value?.let { updateTimetableList(it) }
            } catch (e: Exception) {
                Timber.e(e, "Error deleting table")
                handleException(e, ErrorType.DeleteTable)
            }
        }
    }

    override fun addLecture(lecture: Lecture) {
        val tableId = _selectedTimetableID.value ?: return
        viewModelScope.launch {
            try {
                if (_timetable.value?.hasCollision(lecture) == true) {
                    removeOverlappingLectures(lecture)
                }
                timetableUseCase.addLecture(tableId, lecture.id)
                loadTimetable()
                analyticsService.logEvent(TimetableViewEvent.LectureAdded)
            } catch (e: Exception) {
                Timber.e(e, "Error adding lecture")
                handleException(e, ErrorType.AddLecture)
            }
        }
    }

    override fun deleteLecture(lecture: Lecture) {
        val tableId = _selectedTimetableID.value ?: return
        viewModelScope.launch {
            try {
                timetableUseCase.deleteLecture(tableId, lecture.id)
                loadTimetable()
                analyticsService.logEvent(TimetableViewEvent.LectureDeleted)
            } catch (e: Exception) {
                Timber.e(e, "Error deleting lecture")
                handleException(e, ErrorType.DeleteLecture)
            }
        }
    }

    override fun removeOverlappingLectures(newLecture: Lecture) {
        val table = _timetable.value ?: return
        val collisions = table.lectures.filter { existing ->
            table.hasCollisions(newLecture, existing)
        }
        viewModelScope.launch {
            collisions.forEach { lecture ->
                try {
                    _selectedTimetableID.value?.let { id ->
                        timetableUseCase.deleteLecture(id, lecture.id)
                    }
                } catch (e: Exception) {
                    handleException(e, ErrorType.DeleteLecture)
                }
            }
        }
    }

    override suspend fun selectPreviousSemester() {
        moveSemester(-1)
    }

    override suspend fun selectNextSemester() {
        moveSemester(1)
    }

    private suspend fun moveSemester(offset: Int) {
        val list = _semesters.value
        val currentIndex = _selectedSemester.value?.let { current ->
            list.indexOfFirst { it.id == current.id }
        } ?: return

        val targetIndex = currentIndex + offset
        if (targetIndex in list.indices) {
            val newSemester = list[targetIndex]
            _selectedSemester.value = newSemester
            updateTimetableList(newSemester)
        }
    }

    private fun handleException(error: Exception, type: ErrorType) {
        val messageRes = when (type) {
            ErrorType.AddLecture -> R.string.error_add_lecture
            ErrorType.CreateTable -> R.string.error_create_table
            ErrorType.DeleteLecture -> R.string.error_delete_lecture
            ErrorType.DeleteTable -> R.string.error_delete_table
            ErrorType.FetchData -> R.string.error_fetch_data
            ErrorType.RenameTable -> R.string.error_rename_table
        }
        alertMessageRes = messageRes
        showAlert = true
        crashlyticsService.recordException(error)
    }
}