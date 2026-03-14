package org.sparcs.soap.App.Domain.Usecases.OTL

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.*
import org.sparcs.soap.App.Domain.Models.OTL.Lecture
import org.sparcs.soap.App.Domain.Models.OTL.Semester
import org.sparcs.soap.App.Domain.Models.OTL.Timetable
import org.sparcs.soap.App.Domain.Models.OTL.TimetableListItem
import org.sparcs.soap.App.Domain.Repositories.OTL.OTLTimetableRepositoryProtocol
import org.sparcs.soap.App.Domain.Usecases.UserUseCaseProtocol
import org.sparcs.soap.App.Shared.Extensions.StringProvider
import org.sparcs.soap.R
import javax.inject.Inject
import javax.inject.Singleton

interface TimetableUseCaseProtocol {
    val semesters: StateFlow<List<Semester>>
    val selectedSemester: StateFlow<Semester?>
    val selectedTimetable: StateFlow<Timetable?>
    val selectedTimetableDisplayName: StateFlow<String>
    val isEditable: StateFlow<Boolean>
    val timetableList: StateFlow<List<TimetableListItem>>

    // MARK: - Selected IDs
    var selectedSemesterID: StateFlow<String?>
    var selectedTimetableID: StateFlow<Int>

    val hasLectureInCurrentTable: (Lecture) -> Boolean

    // MARK: - Actions
    suspend fun load()
    suspend fun selectSemester(id: String)
    suspend fun createTable()
    suspend fun deleteTable()
    suspend fun addLecture(lecture: Lecture)
    suspend fun deleteLecture(lecture: Lecture)

    suspend fun selectTimetable(id: Int)
}

@Singleton
class TimetableUseCase @Inject constructor(
    private val userUseCase: UserUseCaseProtocol,
    private val otlTimetableRepository: OTLTimetableRepositoryProtocol,
    private val stringProvider: StringProvider,
) : TimetableUseCaseProtocol {
    companion object {
        const val MY_TABLE_ID = -1
    }

    // MARK: - Properties
    private val _store = MutableStateFlow<Map<String, List<TimetableListItem>>>(emptyMap())
    val store: StateFlow<Map<String, List<TimetableListItem>>> = _store

    private val _semesters = MutableStateFlow<List<Semester>>(emptyList())
    override val semesters: StateFlow<List<Semester>> = _semesters

    /// Prevent overlapping fetches per semester when the user switches quickly.
    private val fetchingSemesters = mutableSetOf<String>()

    private val _selectedSemesterID = MutableStateFlow<String?>(null)
    override var selectedSemesterID: StateFlow<String?> = _selectedSemesterID.asStateFlow()

    private val _selectedTimetableID = MutableStateFlow(-1)
    override var selectedTimetableID: StateFlow<Int> = _selectedTimetableID.asStateFlow()

    // MARK: - Computed
    override val selectedSemester: StateFlow<Semester?> =
        combine(_selectedSemesterID, _semesters) { id, list ->
            list.firstOrNull { it.id == id }
        }.stateIn(CoroutineScope(Dispatchers.IO), SharingStarted.Lazily, null)

    private val _selectedTimetable = MutableStateFlow<Timetable?>(null)
    override val selectedTimetable: StateFlow<Timetable?> = _selectedTimetable.asStateFlow()

    override val timetableList: StateFlow<List<TimetableListItem>> =
        combine(_selectedSemesterID, _store, _semesters) { sid, store, semesters ->
            if (sid == null) {
                emptyList()
            } else {
                val semester = semesters.firstOrNull { it.id == sid }
                val myList = if (semester != null) {
                    listOf(
                        TimetableListItem(
                            id = MY_TABLE_ID,
                            name = stringProvider.get(R.string.my_table),
                            year = semester.year,
                            semester = semester.semesterType.ordinal, // Assuming conversion
                            timetableOrder = -1 // My table always first
                        )
                    )
                } else {
                    emptyList()
                }
                val serverList = store[sid] ?: emptyList()
                myList + serverList
            }
        }.stateIn(CoroutineScope(Dispatchers.IO), SharingStarted.Lazily, emptyList())

    override val selectedTimetableDisplayName: StateFlow<String> = combine(
        _selectedTimetableID,
        timetableList
    ) { tid, list ->
        list.find { it.id == tid }?.name.let { if (it == "") "No Title" else it } ?: stringProvider.get(R.string.unknown)
    }.stateIn(
        CoroutineScope(Dispatchers.IO),
        SharingStarted.Lazily,
        stringProvider.get(R.string.unknown)
    )

    override val isEditable: StateFlow<Boolean> = selectedTimetableID.map { tid ->
        tid != MY_TABLE_ID
    }.stateIn(CoroutineScope(Dispatchers.IO), SharingStarted.Lazily, false)


    override val hasLectureInCurrentTable: (Lecture) -> Boolean
        get() = { lecture ->
            selectedTimetable.value?.lectures?.any { it.id == lecture.id } == true
        }

    // MARK: - API
    override suspend fun load() {
        if (_store.value.isNotEmpty() && semesters.value.isNotEmpty()) return

        val fetchedSemesters = otlTimetableRepository.getSemesters()
        val currentSemester = otlTimetableRepository.getCurrentSemester()

        // Persist semesters
        _semesters.value = fetchedSemesters

        // Select the current semester if it exists; otherwise last
        _selectedSemesterID.value =
            fetchedSemesters.find { it.year == currentSemester.year && it.semesterType == currentSemester.semesterType }?.id
                ?: fetchedSemesters.lastOrNull()?.id

        // Ensure a selected timetable for the chosen semester
        _selectedSemesterID.value?.let {
            _selectedTimetableID.value = MY_TABLE_ID
        }
        getTimetableData(MY_TABLE_ID)
        // Fetch remote tables for the selected semester and merge
        refreshTablesForSelectedSemester()
    }

    override suspend fun selectSemester(id: String) {
        _selectedSemesterID.value = id
        _selectedTimetableID.value = MY_TABLE_ID
        refreshTablesForSelectedSemester()
    }

    override suspend fun createTable() {
        val semester = selectedSemester.value ?: return
        // Create on server
        val newTableId =
            otlTimetableRepository.createTable(semester.year, semester.semesterType)

        // Refresh tables for the current semester to get the new table list
        refreshTablesForSelectedSemester()

        // Select the newly created table
        selectTimetable(newTableId)
    }

    override suspend fun deleteTable() {
        val sid = _selectedSemesterID.value ?: return
        val tid = _selectedTimetableID.value

        // Delete on server
        otlTimetableRepository.deleteTable(tid)

        // Update local store
        val tables = (_store.value[sid] ?: emptyList()).toMutableList()
        tables.removeAll { it.id == tid }
        _store.value = _store.value.toMutableMap().apply { put(sid, tables) }

        // Select my table
        _selectedTimetableID.value = MY_TABLE_ID
    }

    override suspend fun addLecture(lecture: Lecture) {
        val tid = _selectedTimetableID.value
        if (tid == MY_TABLE_ID) return

        val table = _selectedTimetable.value ?: return

        otlTimetableRepository.addLecture(tid, lecture.id)

        val newLectures = table.lectures + lecture
        _selectedTimetable.value = table.copy(lectures = newLectures)
    }

    override suspend fun deleteLecture(lecture: Lecture) {
        val tid = _selectedTimetableID.value
        if (tid == MY_TABLE_ID) return

        val table = _selectedTimetable.value ?: return

        otlTimetableRepository.deleteLecture(tid, lecture.id)

        val newLectures = table.lectures.filter { it.id != lecture.id }
        _selectedTimetable.value = table.copy(lectures = newLectures)
    }

    override suspend fun selectTimetable(id: Int) {
        _selectedTimetableID.value = id
        getTimetableData(id)
    }

    // MARK: - Helpers
    private suspend fun getTimetableData(id: Int) {
        if (id == MY_TABLE_ID) {
            val sid = _selectedSemesterID.value ?: return
            val semester = semesters.value.firstOrNull { it.id == sid } ?: return
            _selectedTimetable.value = otlTimetableRepository.getMyTimetable(semester.year, semester.semesterType)
        } else {
            _selectedTimetable.value = otlTimetableRepository.getTimetable(id)
        }
    }

    private suspend fun refreshTablesForSelectedSemester() {
        val sid = _selectedSemesterID.value ?: return
        val semester = semesters.value.firstOrNull { it.id == sid } ?: return

        if (fetchingSemesters.contains(sid)) return
        fetchingSemesters.add(sid)
        try {
            val fetched =
                otlTimetableRepository.getTimetables(semester.year, semester.semesterType)
            _store.value = _store.value.toMutableMap().apply { put(sid, fetched) }

            if (_selectedTimetableID.value?.let { fetched.none { t -> t.id == it } } == true) {
                if (_selectedTimetableID.value != MY_TABLE_ID) {
                    _selectedTimetableID.value = MY_TABLE_ID
                }
            }
        } finally {
            fetchingSemesters.remove(sid)
        }
    }
}