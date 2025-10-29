package com.example.soap.Domain.Usecases

import com.example.soap.Domain.Models.OTL.Lecture
import com.example.soap.Domain.Models.OTL.OTLUser
import com.example.soap.Domain.Models.OTL.Semester
import com.example.soap.Domain.Models.OTL.Timetable
import com.example.soap.Domain.Repositories.OTL.OTLTimetableRepositoryProtocol
import com.example.soap.Shared.Mocks.mockList
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

interface TimetableUseCaseProtocol {
    val semesters: StateFlow<List<Semester>>
    val selectedSemester: StateFlow<Semester?>
    val selectedTimetable: StateFlow<Timetable?>
    val selectedTimetableDisplayName: StateFlow<String>
    val isEditable: StateFlow<Boolean>
    val timetableIDsForSelectedSemester: List<String>

    // MARK: - Selected IDs
    var selectedSemesterID: StateFlow<String?>
    var selectedTimetableID: StateFlow<String?>

    val hasLectureInCurrentTable: (Lecture) -> Boolean

    // MARK: - Actions
    suspend fun load()
    suspend fun createTable()
    suspend fun deleteTable()
    suspend fun addLecture(lecture: Lecture)
    suspend fun deleteLecture(lecture: Lecture)

    fun selectSemester(id: String)
    fun selectTimetable(id: String)
    fun setSelectedSemesterID(id: String?)
}

class MockTimetableUseCase : TimetableUseCaseProtocol {

    private val _semesters = MutableStateFlow(Semester.mockList())
    override val semesters: StateFlow<List<Semester>> = _semesters.asStateFlow()

    private val _selectedSemesterID = MutableStateFlow(Semester.mockList()[0].id)
    override var selectedSemesterID: StateFlow<String?> = _selectedSemesterID.asStateFlow()

    private val _selectedTimetableID = MutableStateFlow("${_selectedSemesterID.value}-myTable")
    override var selectedTimetableID: StateFlow<String?> = _selectedTimetableID.asStateFlow()

    private val _selectedSemester = MutableStateFlow(semesters.value.firstOrNull { it.id == selectedSemesterID.value })
    override val selectedSemester: StateFlow<Semester?> = _selectedSemester.asStateFlow()

    private val _selectedTimetable = MutableStateFlow(Timetable(id = "${_selectedSemesterID.value}-myTable", lectures = Lecture.mockList()))
    override val selectedTimetable: StateFlow<Timetable?> = _selectedTimetable.asStateFlow()

    private val _selectedTimetableDisplayName = MutableStateFlow("My Table")
    override val selectedTimetableDisplayName: StateFlow<String> = _selectedTimetableDisplayName

    override val isEditable: StateFlow<Boolean> = MutableStateFlow(true)
    override val hasLectureInCurrentTable: (Lecture) -> Boolean
        get() = TODO("Not yet implemented")

    override val timetableIDsForSelectedSemester: List<String>
        get() = listOf("${_selectedSemesterID.value}-myTable")

    override suspend fun load() {}
    override suspend fun createTable() {}
    override suspend fun deleteTable() {}
    override suspend fun addLecture(lecture: Lecture) {}
    override suspend fun deleteLecture(lecture: Lecture) {}

    override fun selectSemester(id: String) {}
    override fun selectTimetable(id: String) {}
    override fun setSelectedSemesterID(id: String?) {}
}

class TimetableUseCase @Inject constructor(
    private val userUseCase: UserUseCaseProtocol,
    private val otlTimetableRepository: OTLTimetableRepositoryProtocol,
) : TimetableUseCaseProtocol {
    // MARK: - Properties
    private val _store = MutableStateFlow<Map<String, List<Timetable>>>(emptyMap())
    val store: StateFlow<Map<String, List<Timetable>>> = _store

    private val _semesters = MutableStateFlow<List<Semester>>(emptyList())
    override val semesters: StateFlow<List<Semester>> = _semesters

    /// Prevent overlapping fetches per semester when the user switches quickly.
    private val fetchingSemesters = mutableSetOf<String>()

    private val _selectedSemesterID = MutableStateFlow<String?>(null)
    override var selectedSemesterID: StateFlow<String?> = _selectedSemesterID.asStateFlow()

    private val _selectedTimetableID = MutableStateFlow<String?>(null)
    override var selectedTimetableID: StateFlow<String?> = _selectedTimetableID.asStateFlow()

    // MARK: - Computed
    override val selectedSemester: StateFlow<Semester?> = combine(
        _selectedSemesterID,
        _semesters
    ) { sid, semesters ->
        semesters.firstOrNull { it.id == sid }
    }.stateIn(CoroutineScope(Dispatchers.IO), SharingStarted.Lazily, null)

    override val selectedTimetable: StateFlow<Timetable?> = combine(
        _selectedSemesterID,
        _selectedTimetableID,
        _store
    ) { sid, tid, store ->
        if (sid != null && tid != null) store[sid]?.firstOrNull { it.id == tid } else null
    }.stateIn(CoroutineScope(Dispatchers.IO), SharingStarted.Lazily, null)

    /// Human-friendly display name for the selected timetable.
    /// - "My Table" for the local user table
    /// - "Table N" for the Nth server table (1-based index)
    /// - "Unknown" as a safe fallback
    override val selectedTimetableDisplayName: StateFlow<String> = combine(
        _selectedTimetableID,
        _selectedSemesterID,
        _store
    ) { tid, sid, store ->
        if (tid == null) "Unknown"
        else if (tid.endsWith("-myTable")) "My Table"
        else sid?.let { s -> store[s]?.map { it.id }?.indexOf(tid)?.let { "Table $it" } ?: "Unknown" } ?: "Unknown"
    }.stateIn(CoroutineScope(Dispatchers.IO), SharingStarted.Lazily, "Unknown")

    override val isEditable: StateFlow<Boolean> = selectedTimetable.map { it?.id?.endsWith("-myTable") == false }
        .stateIn(CoroutineScope(Dispatchers.IO), SharingStarted.Lazily, false)

    override val timetableIDsForSelectedSemester: List<String>
        get() = _selectedSemesterID.value?.let { sid ->
            _store.value[sid]?.map { it.id } ?: emptyList()
        } ?: emptyList()


    override val hasLectureInCurrentTable: (Lecture) -> Boolean
    get() = { lecture ->
        selectedTimetable.value?.lectures?.any { it.id == lecture.id } == true
    }

    // MARK: - API
    override suspend fun load() {
        if (_store.value.isNotEmpty() && semesters.value.isNotEmpty()) return

        val fetchedSemesters = otlTimetableRepository.getSemesters()
        val currentSemester = otlTimetableRepository.getCurrentSemester()
        val user = userUseCase.otlUser ?: run {
            userUseCase.fetchOTLUser()
            userUseCase.otlUser!!
        }

        // Persist semesters
        _semesters.value = fetchedSemesters
        // Seed each semester with a local "My Table" derived from user lectures
        _store.value = fetchedSemesters.associate { semester ->
            semester.id to listOf(makeMyTable(semester, user))
        }

        // Select the current semester if it exists; otherwise last
        _selectedSemesterID.value =
            fetchedSemesters.find { it.year == currentSemester.year && it.semesterType == currentSemester.semesterType }?.id
                ?: fetchedSemesters.lastOrNull()?.id

        // Ensure a selected timetable for the chosen semester
        _selectedSemesterID.value?.let { sid ->
            _selectedTimetableID.value = "$sid-myTable"
        }
        // Fetch remote tables for the selected semester and merge
        refreshTablesForSelectedSemester()
    }

    override suspend fun createTable() {
        val user = userUseCase.otlUser ?: return
        val semester = selectedSemester.value ?: return
        // Create on server
        val newTable = otlTimetableRepository.createTable(user.id, semester.year, semester.semesterType)

        // Insert into local store for the semester (dedup & keep myTable first)
        val sid = semester.id
        val existing = _store.value[sid] ?: emptyList()
        val merged = mergeKeepingMyTableFirst(existing, listOf(newTable))
        _store.value = _store.value.toMutableMap().apply { put(sid, merged) }

        // Select the newly created table
        _selectedTimetableID.value = newTable.id
    }

    override suspend fun deleteTable() {
        val sid = _selectedSemesterID.value ?: return
        val tid = _selectedTimetableID.value ?: return
        val user = userUseCase.otlUser ?: return
        val timetableID = tid.toIntOrNull() ?: return

        // Delete on server
        otlTimetableRepository.deleteTable(user.id, timetableID)

        // Update local store
        val tables = (_store.value[sid] ?: emptyList()).toMutableList()
        tables.removeAll { it.id == tid }
        _store.value = _store.value.toMutableMap().apply { put(sid, tables) }

        // Select the last timetable if available
        _selectedTimetableID.value = tables.lastOrNull()?.id
    }

    override suspend fun addLecture(lecture: Lecture) {
        val sid = _selectedSemesterID.value ?: return
        val tid = _selectedTimetableID.value ?: return
        val user = userUseCase.otlUser ?: return
        val timetableID = tid.toIntOrNull() ?: return

        // Patch local store
        val updatedTable = otlTimetableRepository.addLecture(user.id, timetableID, lecture.id)
        val tables = (_store.value[sid] ?: emptyList()).toMutableList()
        val idx = tables.indexOfFirst { it.id == tid }
        if (idx >= 0) tables[idx] = updatedTable else tables.add(updatedTable)

        _store.value = _store.value.toMutableMap().apply { put(sid, mergeKeepingMyTableFirst(tables, emptyList())) }

        // Ensure selection still points to the edited table
        _selectedTimetableID.value = updatedTable.id
    }

    override suspend fun deleteLecture(lecture: Lecture) {
        val sid = _selectedSemesterID.value ?: return
        val tid = _selectedTimetableID.value ?: return
        val user = userUseCase.otlUser ?: return
        val timetableID = tid.toIntOrNull() ?: return

        // Patch local store
        val updatedTable = otlTimetableRepository.deleteLecture(user.id, timetableID, lecture.id)
        val tables = (_store.value[sid] ?: emptyList()).toMutableList()
        val idx = tables.indexOfFirst { it.id == tid }
        if (idx >= 0) tables[idx] = updatedTable else tables.add(updatedTable)

        // Keep -myTable (if any) at the front
        _store.value = _store.value.toMutableMap().apply { put(sid, mergeKeepingMyTableFirst(tables, emptyList())) }

        // Ensure selection still points to the edited table
        _selectedTimetableID.value = updatedTable.id
    }

    override fun selectSemester(id: String) {
        _selectedSemesterID.value = id
        _selectedTimetableID.value = "$id-myTable"
    }

    override fun selectTimetable(id: String) {
        _selectedTimetableID.value = id
    }

    override fun setSelectedSemesterID(id: String?) {
        _selectedSemesterID.value = id
    }

    // MARK: - Helpers
    /// Merge helper that:
    /// - keeps order of existing tables
    /// - appends any new ones not present (by id)
    /// - keeps `-myTable` as the first element if present
    private fun mergeKeepingMyTableFirst(existing: List<Timetable>, incoming: List<Timetable>): List<Timetable> {
        val seen = existing.map { it.id }.toMutableSet()
        val result = existing.toMutableList()
        for (t in incoming) if (!seen.contains(t.id)) {
            result.add(t)
            seen.add(t.id)
        }

        // Ensure -myTable is at index 0 if present
        val myIdx = result.indexOfFirst { it.id.endsWith("-myTable") }
        return if (myIdx > 0) {
            val copy = result.toMutableList()
            val my = copy.removeAt(myIdx)
            copy.add(0, my)
            copy
        } else result
    }

    /// Ensures there is a `-myTable` entry for the given semester,
    /// filled with the user's lectures for that semester.

    private fun makeMyTable(semester: Semester, user: OTLUser?) =
        Timetable(id = "${semester.id}-myTable",
            lectures = user?.myTimetableLectures?.filter { it.year == semester.year && it.semester == semester.semesterType }
                ?: emptyList())

    private suspend fun refreshTablesForSelectedSemester() {
        val sid = _selectedSemesterID.value ?: return
        val semester = semesters.value.firstOrNull { it.id == sid } ?: return
        val otlUser = userUseCase.otlUser

        if (_store.value[sid]?.any { it.id.endsWith("-myTable") } != true) {
            val myTable = makeMyTable(semester, otlUser)
            val updatedList = listOf(myTable) + (_store.value[sid]?.filterNot { it.id.endsWith("-myTable") } ?: emptyList())
            _store.value = _store.value.toMutableMap().apply { put(sid, updatedList) }
        }

        if (fetchingSemesters.contains(sid)) return
        fetchingSemesters.add(sid)
        try {
            val user = otlUser ?: return
            val fetched = otlTimetableRepository.getTables(user.id, semester.year, semester.semesterType)
            val existing = _store.value[sid] ?: emptyList()
            val merged = mergeKeepingMyTableFirst(existing, fetched)
            _store.value = _store.value.toMutableMap().apply { put(sid, merged) }

            if (_selectedTimetableID.value?.let { merged.none { t -> t.id == it } } == true) {
                _selectedTimetableID.value = "$sid-myTable"
            }
        } finally {
            fetchingSemesters.remove(sid)
        }
    }
}
