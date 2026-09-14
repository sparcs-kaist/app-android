package org.sparcs.soap.app.features.timetable

import android.content.Context
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import org.sparcs.soap.R
import org.sparcs.soap.app.domain.helpers.TimetableSelectionStore
import org.sparcs.soap.app.domain.models.otl.Lecture
import org.sparcs.soap.app.domain.models.otl.Semester
import org.sparcs.soap.app.domain.models.otl.Timetable
import org.sparcs.soap.app.domain.models.otl.TimetableSummary
import org.sparcs.soap.app.domain.services.AnalyticsServiceProtocol
import org.sparcs.soap.app.domain.services.CrashlyticsServiceProtocol
import org.sparcs.soap.app.domain.usecases.otl.TimetableUseCaseProtocol
import org.sparcs.soap.app.features.timetable.event.TimetableViewEvent
import timber.log.Timber
import org.sparcs.soap.widgets.buddyTimetableWidget.TimetableWidgetSyncManager
import javax.inject.Inject

interface TimetableViewModelProtocol {
    val timetableUseCase: TimetableUseCaseProtocol?
    val isLoading: MutableStateFlow<Boolean>
    val semesters: StateFlow<List<Semester>>
    val selectedSemester: StateFlow<Semester?>
    val selectedTimetable: StateFlow<Timetable?>
    val timetableList: StateFlow<List<TimetableSummary>>
    val selectedTimetableID: StateFlow<Int?>
    val candidateLecture: StateFlow<Lecture?>
    val isCandidateOverlapping: StateFlow<Boolean>
    val overlappingLectures: StateFlow<List<Lecture>>
    val isEditable: StateFlow<Boolean>
    val timetableName: StateFlow<String>
    /** Duplicating replays every lecture and activity, so the menu entry must not be tappable twice. */
    val isDuplicatingTable: StateFlow<Boolean>

    var showAlert: Boolean
    var alertTitleRes: Int?
    var alertMessageRes: Int?

    fun activityTableUpdated(table: Timetable) {}
    fun setCandidateLecture(lecture: Lecture?)
    fun fetchData()
    suspend fun selectPreviousSemester()
    suspend fun selectNextSemester()
    fun selectTimetable(id: Int)
    fun createTable()
    fun duplicateMyTable()
    fun deleteTable()
    fun renameTable(title: String)
    fun addLecture(lecture: Lecture)
    fun deleteLecture(lecture: Lecture)
    fun removeOverlappingLectures(newLecture: Lecture)
}

@HiltViewModel
class TimetableViewModel @Inject constructor(
    override val timetableUseCase: TimetableUseCaseProtocol,
    private val crashlyticsService: CrashlyticsServiceProtocol,
    private val analyticsService: AnalyticsServiceProtocol,
    @param:ApplicationContext private val context: Context
) : ViewModel(), TimetableViewModelProtocol {

    override fun activityTableUpdated(table: Timetable) {
        if (_selectedTimetableID.value?.toString() == table.id) _timetable.value = table
        viewModelScope.launch { TimetableWidgetSyncManager(context).syncSavedTimetable(table) }
    }

    enum class ErrorType {
        AddLecture,
        CreateTable,
        DuplicateTable,
        DeleteTable,
        DeleteLecture,
        FetchData,
        RenameTable
    }

    companion object {
        const val MY_TABLE_ID = -1
    }

    override var showAlert by mutableStateOf(false)
    override var alertTitleRes by mutableStateOf<Int?>(null)
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

    private val _isDuplicatingTable = MutableStateFlow(false)
    override val isDuplicatingTable: StateFlow<Boolean> = _isDuplicatingTable.asStateFlow()

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

    override val overlappingLectures: StateFlow<List<Lecture>> =
        combine(_timetable, _candidateLecture) { table, candidate ->
            if (table == null || candidate == null) return@combine emptyList()
            table.lectures.filter { table.hasCollisions(candidate, it) }
        }.distinctUntilChanged()
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5000),
                initialValue = emptyList()
            )

    override val timetableName: StateFlow<String> = combine(
        _selectedTimetableID,
        _timetableList
    ) { id, list ->
        when {
            id == null || id == MY_TABLE_ID -> context.getString(R.string.my_table)
            else -> {
                val title = list.find { it.id == id }?.title
                if (title.isNullOrEmpty()) context.getString(R.string.untitled) else title
            }
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = context.getString(R.string.my_table)
    )

    private val selectionStore = TimetableSelectionStore(context)
    private var selectionRevision = 0L

    private fun persistSelection() {
        _selectedSemester.value?.let { selectionStore.save(it, _selectedTimetableID.value) }
    }

    init { fetchData() }
    // MARK: - Functions
    override fun fetchData() {
        viewModelScope.launch {
            isLoading.value = true
            val revision = selectionRevision
            try {
                val semesterList = timetableUseCase.getSemesters()
                val saved = selectionStore.selection
                val previous = _selectedSemester.value
                val preferredID = previous?.id ?: saved?.semesterID
                val semester = semesterList.firstOrNull { it.id == preferredID }
                    ?: timetableUseCase.getCurrentSemester()
                // A refresh must not undo a choice made while its request was in flight.
                if (revision != selectionRevision) return@launch
                _semesters.value = semesterList
                _selectedSemester.value = semester
                _selectedTimetableID.value = when {
                    previous?.id == semester.id -> _selectedTimetableID.value
                    previous == null && saved?.semesterID == semester.id -> saved.timetableID
                    else -> null
                }
                persistSelection()
                updateTimetableList(semester, forceRefresh = true)
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                Timber.e(e, "failed to fetch Timetable Data")
                handleException(e, ErrorType.FetchData)
            } finally {
                isLoading.value = false
            }
        }
    }

    private suspend fun updateTimetableList(semester: Semester, forceRefresh: Boolean = false) {
        val revision = selectionRevision
        try {
            val list = timetableUseCase.getTimetableList(semester)
            if (_selectedSemester.value != semester || revision != selectionRevision) return
            _timetableList.value = list

            if (list.none { it.id == _selectedTimetableID.value }) {
                _selectedTimetableID.value = null
            }
            persistSelection()

            loadTimetable(forceRefresh = forceRefresh)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            if (_selectedSemester.value == semester && revision == selectionRevision) handleException(e, ErrorType.FetchData)
        }
    }

    private suspend fun loadTimetable(forceRefresh: Boolean = false) {
        val id = _selectedTimetableID.value
        val semester = _selectedSemester.value

        try {
            val table = when {
                id == MY_TABLE_ID || (id == null && semester != null) -> {
                    timetableUseCase.getMyTable(semester!!, forceRefresh = forceRefresh)
                }
                id != null -> {
                    timetableUseCase.getTable(id, forceRefresh = forceRefresh)
                }
                else -> null
            }
            if (_selectedSemester.value == semester && _selectedTimetableID.value == id) {
                _timetable.value = table
            }
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            if (_selectedSemester.value == semester && _selectedTimetableID.value == id) {
                _timetable.value = null
                handleException(e, ErrorType.FetchData)
            }
        }
    }

    override fun selectTimetable(id: Int) {
        if (id == MY_TABLE_ID) {
            _selectedTimetableID.value = null
        } else {
            if (_selectedTimetableID.value == id) return
            _selectedTimetableID.value = id
        }

        selectionRevision++
        _timetable.value = null
        persistSelection()
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
                if (_selectedSemester.value == semester) {
                    selectionRevision++
                    _selectedTimetableID.value = creation.id
                    persistSelection()
                    updateTimetableList(semester)
                }
            } catch (e: Exception) {
                Timber.e(e, "Error creating table")
                handleException(e, ErrorType.CreateTable)
            }
        }
    }

    /** Copies "My Table" of the selected semester into a new table and selects it. */
    override fun duplicateMyTable() {
        val semester = _selectedSemester.value ?: return
        if (_isDuplicatingTable.value) return
        _isDuplicatingTable.value = true
        viewModelScope.launch {
            try {
                val duplication = timetableUseCase.duplicateMyTable(semester, duplicateTitle())
                analyticsService.logEvent(TimetableViewEvent.TableDuplicated)
                if (_selectedSemester.value == semester) {
                    selectionRevision++
                    _selectedTimetableID.value = duplication.id
                    persistSelection()
                    updateTimetableList(semester)
                }
                // The table exists either way; tell the user only when it is incomplete.
                if (!duplication.isComplete) {
                    alertTitleRes = R.string.timetable_duplicate_partial_title
                    alertMessageRes = R.string.timetable_duplicate_partial_message
                    showAlert = true
                }
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                Timber.e(e, "Error duplicating table")
                handleException(e, ErrorType.DuplicateTable)
            } finally {
                _isDuplicatingTable.value = false
            }
        }
    }

    /**
     * A title that does not clash with the semester's existing tables, so repeated duplicates stay
     * distinguishable in the selector.
     */
    private fun duplicateTitle(): String {
        val base = context.getString(R.string.timetable_duplicate_title)
        val existing = _timetableList.value.map { it.title }.toSet()
        if (base !in existing) return base

        var index = 2
        while ("$base $index" in existing) index++
        return "$base $index"
    }

    override fun deleteTable() {
        val id = _selectedTimetableID.value ?: return
        viewModelScope.launch {
            try {
                timetableUseCase.deleteTable(id)
                analyticsService.logEvent(TimetableViewEvent.TableDeleted)
                if (_selectedTimetableID.value == id) {
                    selectionRevision++
                    _selectedTimetableID.value = null
                    persistSelection()
                }
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
                val table = timetableUseCase.getTable(tableId, forceRefresh = true)
                if (table.activities.any { block -> lecture.classes.any { it.day.value == block.day && it.begin < block.end && it.end > block.begin } }) {
                    alertMessageRes = R.string.activity_conflict
                    showAlert = true
                    return@launch
                }
                if (table.hasCollision(lecture) == true) {
                    val collisions = table.lectures.filter { table.hasCollision(lecture) && table.hasCollisions(lecture, it) }

                    collisions.forEach { overlapping ->
                        timetableUseCase.deleteLecture(tableId, overlapping.id)
                    }
                }
                timetableUseCase.addLecture(tableId, lecture.id)
                _candidateLecture.value = null
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
            selectionRevision++
            _selectedSemester.value = newSemester
            _selectedTimetableID.value = null
            _timetableList.value = emptyList()
            _timetable.value = null
            persistSelection()
            updateTimetableList(newSemester)
        }
    }

    private fun handleException(error: Exception, type: ErrorType) {
        val messageRes = when (type) {
            ErrorType.AddLecture -> R.string.error_add_lecture
            ErrorType.CreateTable -> R.string.error_create_table
            ErrorType.DuplicateTable -> R.string.error_duplicate_table
            ErrorType.DeleteLecture -> R.string.error_delete_lecture
            ErrorType.DeleteTable -> R.string.error_delete_table
            ErrorType.FetchData -> R.string.error_fetch_data
            ErrorType.RenameTable -> R.string.error_rename_table
        }
        alertTitleRes = null
        alertMessageRes = messageRes
        showAlert = true
        crashlyticsService.recordException(error)
    }
}