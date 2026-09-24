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
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import org.sparcs.soap.R
import org.sparcs.soap.app.domain.error.NetworkError
import org.sparcs.soap.app.domain.helpers.TimetableSelectionStore
import org.sparcs.soap.app.domain.helpers.canKeepSavedTimetable
import org.sparcs.soap.app.domain.helpers.isOfflineFailure
import org.sparcs.soap.app.domain.models.otl.ActivityDraft
import org.sparcs.soap.app.domain.models.otl.Lecture
import org.sparcs.soap.app.domain.models.otl.Semester
import org.sparcs.soap.app.domain.models.otl.Timetable
import org.sparcs.soap.app.domain.models.otl.TimetableSummary
import org.sparcs.soap.app.domain.services.AnalyticsServiceProtocol
import org.sparcs.soap.app.domain.services.CrashlyticsServiceProtocol
import org.sparcs.soap.app.domain.usecases.otl.TimetableUseCaseProtocol
import org.sparcs.soap.app.features.timetable.event.TimetableViewEvent
import org.sparcs.soap.widgets.buddyTimetableWidget.TimetableWidgetSyncManager
import timber.log.Timber
import java.util.Date
import javax.inject.Inject

private val defaultTimetableLoadState = MutableStateFlow(TimetableLoadState()).asStateFlow()

interface TimetableViewModelProtocol {
    val loadState: StateFlow<TimetableLoadState> get() = defaultTimetableLoadState
    fun connectivityChanged(connected: Boolean) {}
    suspend fun refreshActivityTable(timetableID: Int): Timetable
    suspend fun saveActivity(timetableID: Int, activityID: Int?, draft: ActivityDraft): Timetable
    suspend fun deleteActivity(timetableID: Int, activityID: Int): Timetable
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
    private val timetableUseCase: TimetableUseCaseProtocol,
    private val crashlyticsService: CrashlyticsServiceProtocol,
    private val analyticsService: AnalyticsServiceProtocol,
    @param:ApplicationContext private val context: Context
) : ViewModel(), TimetableViewModelProtocol {

    override suspend fun refreshActivityTable(timetableID: Int): Timetable =
        timetableUseCase.getTable(timetableID, forceRefresh = true).also(::activityTableUpdated)

    override suspend fun saveActivity(timetableID: Int, activityID: Int?, draft: ActivityDraft): Timetable {
        if (loadState.value.isReadOnly) throw NetworkError.NoConnection()
        loadGeneration++
        return timetableUseCase.saveActivity(timetableID, activityID, draft).also(::activityTableUpdated)
    }

    override suspend fun deleteActivity(timetableID: Int, activityID: Int): Timetable {
        if (loadState.value.isReadOnly) throw NetworkError.NoConnection()
        loadGeneration++
        return timetableUseCase.deleteActivity(timetableID, activityID).also(::activityTableUpdated)
    }

    private fun activityTableUpdated(table: Timetable) {
        if (_selectedTimetableID.value?.toString() == table.id) {
            loadGeneration++
            _timetable.value = table
            succeeded("table")
            _loadState.value = _loadState.value.copy(isShowingSavedData = false, lastUpdated = Date(), loadError = null)
        }
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

    private val _loadState = MutableStateFlow(TimetableLoadState())
    override val loadState: StateFlow<TimetableLoadState> = _loadState.asStateFlow()
    private val refreshFailures = mutableSetOf<String>()
    private val offlineFailures = mutableSetOf<String>()
    private var networkUnavailable = false
    private var refreshJob: Job? = null
    private var refreshPending = false
    private var loadGeneration = 0L
    private var listGeneration = 0L

    override val isEditable: StateFlow<Boolean> = combine(_selectedTimetableID, _timetable, _loadState) { id, table, state ->
        id != null && table?.id == id.toString() && !state.isReadOnly
    }.stateIn(viewModelScope, SharingStarted.Eagerly, false)

    override fun connectivityChanged(connected: Boolean) {
        val shouldRefresh = connected && (networkUnavailable || refreshFailures.isNotEmpty())
        networkUnavailable = !connected
        if (connected) offlineFailures.clear()
        publishStatus()
        if (shouldRefresh) {
            if (refreshJob?.isActive == true) refreshPending = true else fetchData()
        }
    }

    private fun publishStatus() {
        _loadState.value = _loadState.value.copy(
            isOffline = networkUnavailable || offlineFailures.isNotEmpty(),
            refreshFailed = refreshFailures.isNotEmpty(),
        )
    }

    private fun succeeded(resource: String) {
        refreshFailures.remove(resource)
        offlineFailures.remove(resource)
        publishStatus()
    }

    private fun failed(error: Exception, resource: String) {
        refreshFailures.add(resource)
        offlineFailures.remove(resource)
        if (error.isOfflineFailure()) offlineFailures.add(resource)
        publishStatus()
        crashlyticsService.recordException(error)
    }

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
                if (title == null) context.getString(R.string.timetable_number, id)
                else if (title.isEmpty()) context.getString(R.string.untitled) else title
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
        if (refreshJob?.isActive == true) return
        refreshJob = viewModelScope.launch {
            _loadState.value = _loadState.value.copy(isRefreshing = true)
            val revision = selectionRevision
            try {
                val cached = timetableUseCase.cachedState()
                if (revision != selectionRevision) return@launch
                if (_semesters.value.isEmpty()) _semesters.value = cached.semesters.orEmpty()
                restoreSelection(cached.currentSemester, authoritative = false)
                _selectedSemester.value?.let { restoreCachedTable(it) }
                isLoading.value = _semesters.value.isEmpty()
                try {
                    val fresh = timetableUseCase.refreshSemesters()
                    if (revision != selectionRevision) return@launch
                    _semesters.value = fresh
                    succeeded("semesters")
                    restoreSelection(cached.currentSemester?.takeIf { it in fresh }, authoritative = true)
                } catch (e: CancellationException) { throw e }
                catch (e: Exception) { failed(e, "semesters") }
                if (revision != selectionRevision) return@launch
                try {
                    val current = timetableUseCase.refreshCurrentSemester()
                    if (revision != selectionRevision) return@launch
                    succeeded("current")
                    restoreSelection(current, authoritative = "semesters" !in refreshFailures)
                } catch (e: CancellationException) { throw e }
                catch (e: Exception) { failed(e, "current") }
                if (revision != selectionRevision) return@launch
                val semester = _selectedSemester.value
                if (semester != null) updateTimetableList(semester)
                else _loadState.value = _loadState.value.copy(loadError = R.string.timetable_download_required)
            } finally {
                isLoading.value = false
                _loadState.value = _loadState.value.copy(isRefreshing = false)
                refreshJob = null
                if (refreshPending && !networkUnavailable) {
                    refreshPending = false
                    fetchData()
                }
            }
        }
    }

    private fun restoreSelection(current: Semester?, authoritative: Boolean) {
        val previous = _selectedSemester.value
        if (previous != null && previous in _semesters.value) return
        val saved = selectionStore.selection
        val preferred = _semesters.value.firstOrNull { it.id == saved?.semesterID }
        if (saved != null && preferred == null && !authoritative) return
        val semester = preferred ?: current ?: return
        if (previous != semester) {
            _timetable.value = null
            _timetableList.value = emptyList()
            _loadState.value = _loadState.value.copy(lastUpdated = null, isShowingSavedData = false)
        }
        _selectedSemester.value = semester
        _selectedTimetableID.value = if (semester.id == saved?.semesterID) saved.timetableID else null
        persistSelection()
    }

    private suspend fun restoreCachedTable(semester: Semester, restoreList: Boolean = true, isCurrent: () -> Boolean = { true }) {
        val id = _selectedTimetableID.value
        val revision = selectionRevision
        val cached = timetableUseCase.cachedState(semester, id)
        if (_selectedSemester.value != semester || revision != selectionRevision || !isCurrent()) return
        if (restoreList) cached.timetables?.let { _timetableList.value = it }
        cached.timetable?.let {
            _timetable.value = it
            _loadState.value = _loadState.value.copy(
                lastUpdated = cached.updatedAt, isShowingSavedData = true, loadError = null,
            )
        }
    }

    private suspend fun updateTimetableList(semester: Semester) {
        val revision = selectionRevision
        val generation = ++listGeneration
        restoreCachedTable(semester, isCurrent = { generation == listGeneration })
        try {
            val list = timetableUseCase.refreshTimetableList(semester)
            if (_selectedSemester.value != semester || revision != selectionRevision || generation != listGeneration) return
            _timetableList.value = list
            succeeded("list")
            if (_selectedTimetableID.value != null && list.none { it.id == _selectedTimetableID.value }) {
                _selectedTimetableID.value = null
                _timetable.value = null
                _loadState.value = _loadState.value.copy(lastUpdated = null, isShowingSavedData = false)
            }
            persistSelection()
        } catch (e: CancellationException) { throw e }
        catch (e: Exception) {
            if (_selectedSemester.value != semester || revision != selectionRevision || generation != listGeneration) return
            failed(e, "list")
        }
        if (_selectedSemester.value == semester && revision == selectionRevision && generation == listGeneration) {
            loadTimetable()
        }
    }

    private suspend fun loadTimetable() {
        val id = _selectedTimetableID.value
        val semester = _selectedSemester.value ?: return
        val revision = selectionRevision
        val generation = ++loadGeneration
        restoreCachedTable(semester, restoreList = false, isCurrent = { generation == loadGeneration })
        try {
            val table = if (id == null || id == MY_TABLE_ID) timetableUseCase.getMyTable(semester, forceRefresh = true)
                else timetableUseCase.getTable(id, forceRefresh = true)
            if (_selectedSemester.value != semester || revision != selectionRevision || generation != loadGeneration) return
            _timetable.value = table
            succeeded("table")
            _loadState.value = _loadState.value.copy(lastUpdated = Date(), isShowingSavedData = false, loadError = null)
            viewModelScope.launch { TimetableWidgetSyncManager(context).syncMatchingTimetable(table, semester) }
        } catch (e: CancellationException) { throw e }
        catch (e: Exception) {
            if (_selectedSemester.value != semester || revision != selectionRevision || generation != loadGeneration) return
            failed(e, "table")
            if (!e.canKeepSavedTimetable()) _timetable.value = null
            _loadState.value = _loadState.value.copy(
                isShowingSavedData = _timetable.value != null,
                lastUpdated = _loadState.value.lastUpdated.takeIf { _timetable.value != null },
                loadError = if (_timetable.value != null) null else if (e.isOfflineFailure())
                    R.string.timetable_unavailable_offline else R.string.error_fetch_data,
            )
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
        _loadState.value = _loadState.value.copy(lastUpdated = null, isShowingSavedData = false, loadError = null)
        persistSelection()
        viewModelScope.launch { loadTimetable() }
    }

    override fun setCandidateLecture(lecture: Lecture?) {
        _candidateLecture.value = lecture
    }

    override fun renameTable(title: String) {
        if (_loadState.value.isReadOnly) return
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
        if (_loadState.value.isReadOnly) return
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
        if (_loadState.value.isReadOnly) return
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
        if (_loadState.value.isReadOnly) return
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
        if (_loadState.value.isReadOnly) return
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
        if (_loadState.value.isReadOnly) return
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
        if (_loadState.value.isReadOnly) return
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
            _loadState.value = _loadState.value.copy(lastUpdated = null, isShowingSavedData = false, loadError = null)
            persistSelection()
            updateTimetableList(newSemester)
        }
    }

    private fun handleException(error: Exception, type: ErrorType) {
        if (error is CancellationException) throw error
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