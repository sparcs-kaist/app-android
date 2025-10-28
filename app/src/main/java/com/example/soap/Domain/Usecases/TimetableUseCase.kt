package com.example.soap.Domain.Usecases

import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.example.soap.Domain.Models.OTL.Lecture
import com.example.soap.Domain.Models.OTL.OTLUser
import com.example.soap.Domain.Models.OTL.Semester
import com.example.soap.Domain.Models.OTL.Timetable
import com.example.soap.Domain.Repositories.OTL.OTLTimetableRepositoryProtocol
import com.example.soap.Shared.Mocks.mock
import com.example.soap.Shared.Mocks.mockList
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

interface TimetableUseCaseProtocol {
    val semesters: StateFlow<List<Semester>>
    var selectedSemesterID: String?
    var selectedTimetableID: String?
    val selectedSemester: Semester?
    val selectedTimetable: Timetable?
    val timetableIDsForSelectedSemester: List<String>
    val selectedTimetableDisplayName: String
    val isEditable: Boolean
    suspend fun load()
    suspend fun createTable()
    suspend fun deleteTable()
    suspend fun addLecture(lecture: Lecture)
    suspend fun deleteLecture(lecture: Lecture)
}

class MockTimetableUseCase : TimetableUseCaseProtocol {
    private val _semesters = MutableStateFlow(Semester.mockList())
    override val semesters: StateFlow<List<Semester>> = _semesters.asStateFlow()
    override var selectedSemesterID: String? = Semester.mockList()[9].id
    override var selectedTimetableID: String? = Timetable.mock().id
    override val selectedSemester: Semester?
        get() = semesters.value.firstOrNull { it.id == selectedSemesterID }
    override val selectedTimetable: Timetable?
        get() = selectedSemesterID?.let { Timetable(id = "$it-myTable", lectures = Lecture.mockList()) }
    override val timetableIDsForSelectedSemester: List<String>
        get() = selectedSemesterID?.let { listOf("$it-myTable") } ?: emptyList()
    override val selectedTimetableDisplayName: String
        get() = if (selectedTimetableID?.endsWith("-myTable") == true) "My Table" else "Unknown"
    override val isEditable: Boolean
        get() = selectedTimetableID?.endsWith("-myTable") == false

    override suspend fun load() {}
    override suspend fun createTable() {}
    override suspend fun deleteTable() {}
    override suspend fun addLecture(lecture: Lecture) {}
    override suspend fun deleteLecture(lecture: Lecture) {}
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

    override var selectedSemesterID: String? = null
        set(value) {
            field = value
            // Always default-select My Table of the chosen semester
            value?.let { _selectedTimetableID.value = "$it-myTable" }

            // Refresh tables for the newly selected semester
            CoroutineScope(Dispatchers.IO).launch { refreshTablesForSelectedSemester() }
        }

    private val _selectedTimetableID = MutableStateFlow<String?>(null)
    override var selectedTimetableID: String?
        get() = _selectedTimetableID.value
        set(value) {
            _selectedTimetableID.value = value
            _selectedTimetableDisplayName = value?.let { tid ->
                if (tid.endsWith("-myTable")) "My Table"
                else timetableIDsForSelectedSemester.indexOf(tid).let { idx ->
                    if (idx >= 0) "Table $idx" else "Unknown"
                }
            } ?: "Unknown"
        }


    // MARK: - Computed
    override val selectedSemester: Semester?
        get() = selectedSemesterID?.let { sid -> semesters.value.firstOrNull { it.id == sid } }

    override val selectedTimetable: Timetable?
        get() = selectedSemesterID?.let { sid ->
            _selectedTimetableID.value?.let { tid -> _store.value[sid]?.firstOrNull { it.id == tid } }
        }

    override val timetableIDsForSelectedSemester: List<String>
        get() = selectedSemesterID?.let { sid -> _store.value[sid]?.map { it.id } ?: emptyList() }
            ?: emptyList()

    /// Human-friendly display name for the selected timetable.
    /// - "My Table" for the local user table
    /// - "Table N" for the Nth server table (1-based index)
    /// - "Unknown" as a safe fallback

    private var _selectedTimetableDisplayName by mutableStateOf("Unknown")
    override val selectedTimetableDisplayName: String
        get() {
            val tid = _selectedTimetableID.value ?: return "Unknown"
            return if (tid.endsWith("-myTable")) "My Table"
            else timetableIDsForSelectedSemester.indexOf(tid).let { idx ->
                if (idx >= 0) "Table $idx" else "Unknown"
            }
        }

    override val isEditable: Boolean
        get() = selectedTimetable?.let { !it.id.contains("-myTable") } ?: false

    // MARK: - API
    override suspend fun load() {
        // Avoid re-loading if already populated
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
        selectedSemesterID =
            fetchedSemesters.find { it.year == currentSemester.year && it.semesterType == currentSemester.semesterType }?.id
                ?: fetchedSemesters.lastOrNull()?.id

        // Ensure a selected timetable for the chosen semester
        selectedSemesterID?.let { sid ->
            _selectedTimetableID.value = "$sid-myTable"
        }
        // Fetch remote tables for the selected semester and merge
        refreshTablesForSelectedSemester()
    }

    override suspend fun createTable() {
        val user = userUseCase.otlUser ?: return
        val semester = selectedSemester ?: return
        // Create on server
        val newTable =
            otlTimetableRepository.createTable(user.id, semester.year, semester.semesterType)

        // Insert into local store for the semester (dedup & keep myTable first)
        val sid = semester.id
        val existing = _store.value[sid] ?: emptyList()
        val merged = mergeKeepingMyTableFirst(existing, listOf(newTable))
        _store.value = _store.value.toMutableMap().apply {
            this[sid] = merged
        }

        // Select the newly created table
        _selectedTimetableID.value = newTable.id
    }

    override suspend fun deleteTable() {
        val sid = selectedSemesterID ?: return
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
        val sid = selectedSemesterID ?: return
        val tid = _selectedTimetableID.value ?: return
        val user = userUseCase.otlUser ?: return
        val timetableID = tid.toIntOrNull() ?: return

        // Patch local store
        val updatedTable = otlTimetableRepository.addLecture(user.id, timetableID, lecture.id)
        val tables = (_store.value[sid] ?: emptyList()).toMutableList()
        val idx = tables.indexOfFirst { it.id == tid }
        if (idx >= 0) tables[idx] = updatedTable else tables.add(updatedTable)

        _store.value = _store.value.toMutableMap().apply {
            this[sid] = mergeKeepingMyTableFirst(tables, emptyList())
        }
        // Ensure selection still points to the edited table
        _selectedTimetableID.value = updatedTable.id
    }

    override suspend fun deleteLecture(lecture: Lecture) {
        val sid = selectedSemesterID ?: return
        val tid = _selectedTimetableID.value ?: return
        val user = userUseCase.otlUser ?: return
        val timetableID = tid.toIntOrNull() ?: return

        // Patch local store
        val updatedTable = otlTimetableRepository.deleteLecture(user.id, timetableID, lecture.id)
        val tables = (_store.value[sid] ?: emptyList()).toMutableList()
        val idx = tables.indexOfFirst { it.id == tid }
        if (idx >= 0) tables[idx] = updatedTable else tables.add(updatedTable)

        // Keep -myTable (if any) at the front
        _store.value = _store.value.toMutableMap().apply {
            this[sid] = mergeKeepingMyTableFirst(tables, emptyList())
        }
        // Ensure selection still points to the edited table
        _selectedTimetableID.value = updatedTable.id
    }


    // MARK: - Helpers
    /// Merge helper that:
    /// - keeps order of existing tables
    /// - appends any new ones not present (by id)
    /// - keeps `-myTable` as the first element if present
    private fun mergeKeepingMyTableFirst(
        existing: List<Timetable>,
        incoming: List<Timetable>
    ): List<Timetable> {
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
    private fun seedMyTableIfNeeded(semester: Semester, user: OTLUser?) {
        val sid = semester.id
        val existing = _store.value[sid] ?: emptyList()
        if (existing.any { it.id.endsWith("-myTable") }) return
        // Pin myTable to the front, then append the rest
        val myTable = makeMyTable(semester, user)
        val updatedList = existing.filterNot { it.id.endsWith("-myTable") } + myTable
        _store.value = _store.value.toMutableMap().apply { put(sid, updatedList) }
    }

    /// Creates the local "My Table" for a semester using user's lectures.
    private fun makeMyTable(semester: Semester, user: OTLUser?): Timetable {
        val lectures =
            user?.myTimetableLectures?.filter { it.year == semester.year && it.semester == semester.semesterType }
                ?: emptyList()
        return Timetable(id = "${semester.id}-myTable", lectures = lectures)
    }

    /// Refresh tables for `selectedSemesterID`, seeding My Table if needed and merging server tables (deduped).
    private suspend fun refreshTablesForSelectedSemester() {
        val sid = selectedSemesterID ?: return
        val semester = semesters.value.firstOrNull { it.id == sid } ?: return
        // Seed myTable if missing
        val otlUser = userUseCase.otlUser
        seedMyTableIfNeeded(semester, otlUser)

        // Avoid concurrent fetches for the same semester
        if (!fetchingSemesters.contains(sid)) return
        try {
            fetchingSemesters.add(sid)
        } finally {
            fetchingSemesters.remove(sid)
        }

        val user = otlUser ?: return
        try {
            val fetched =
                otlTimetableRepository.getTables(user.id, semester.year, semester.semesterType)
            val existing = _store.value[sid] ?: emptyList()
            val merged = mergeKeepingMyTableFirst(existing, fetched)
            _store.value = _store.value.toMutableMap().apply { put(sid, merged) }

            // If current selection disappeared, fall back to myTable
            _selectedTimetableID.value?.let { tid ->
                if (merged.none { it.id == tid }) {
                    _selectedTimetableID.value = "${sid}-myTable"
                }
            }
        } catch (e: Exception) {
            Log.e("TimetableUsecase", "Error refreshing tables for semester $sid")
        }
    }
}