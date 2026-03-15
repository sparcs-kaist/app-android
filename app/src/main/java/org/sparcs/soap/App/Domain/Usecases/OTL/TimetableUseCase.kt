package org.sparcs.soap.App.Domain.Usecases.OTL

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.*
import org.sparcs.soap.App.Domain.Models.OTL.Lecture
import org.sparcs.soap.App.Domain.Models.OTL.Semester
import org.sparcs.soap.App.Domain.Models.OTL.Timetable
import org.sparcs.soap.App.Domain.Models.OTL.TimetableListItem
import org.sparcs.soap.App.Domain.Repositories.OTL.OTLTimetableRepositoryProtocol
import org.sparcs.soap.App.Shared.Extensions.StringProvider
import org.sparcs.soap.R
import javax.inject.Inject
import javax.inject.Singleton

interface TimetableUseCaseProtocol {
    val semesters: StateFlow<List<Semester>>
    val selectedSemester: StateFlow<Semester?>

    val currentSemesterTimetableList: StateFlow<List<TimetableListItem>>
    var selectedTimetable: StateFlow<TimetableListItem?>
    val selectedTimetableLectures: StateFlow<List<Lecture>>
    val selectedTimetableObject: StateFlow<Timetable>
    val isEditable: StateFlow<Boolean>

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
    private val otlTimetableRepository: OTLTimetableRepositoryProtocol,
    private val stringProvider: StringProvider,
) : TimetableUseCaseProtocol {
    companion object {
        const val MY_TABLE_ID = -1
    }
    private val _semesters = MutableStateFlow<List<Semester>>(emptyList())
    override val semesters: StateFlow<List<Semester>> = _semesters

    /// Prevent overlapping fetches per semester when the user switches quickly.
    private val fetchingSemesters = mutableSetOf<String>()

    private val _selectedSemesterID = MutableStateFlow<String?>(null)
    override val selectedSemester: StateFlow<Semester?> =
        combine(_selectedSemesterID, _semesters) { id, list ->
            list.firstOrNull { it.id == id }
        }.stateIn(CoroutineScope(Dispatchers.IO), SharingStarted.Lazily, null)

    private val _currentSemesterTimetableList = MutableStateFlow<List<TimetableListItem>>(emptyList())
    override val currentSemesterTimetableList: StateFlow<List<TimetableListItem>> = _currentSemesterTimetableList.asStateFlow()

    private val _selectedTimetableId = MutableStateFlow(-1)
    override var selectedTimetable: StateFlow<TimetableListItem?> = combine(_selectedTimetableId, currentSemesterTimetableList) { selectedId, list ->
        list.firstOrNull { it.id == selectedId } ?:
        if (selectedId == MY_TABLE_ID && selectedSemester.value !== null) {
            TimetableListItem(
                id = MY_TABLE_ID,
                name = stringProvider.get(R.string.my_table),
                year = selectedSemester.value!!.year,
                semester = selectedSemester.value!!.semesterType,
                timetableOrder = -1
            )
        } else null
    }.stateIn(CoroutineScope(Dispatchers.IO), SharingStarted.Lazily, null)

    private val _selectedTimetableLectures = MutableStateFlow<List<Lecture>>(emptyList())
    override val selectedTimetableLectures: StateFlow<List<Lecture>> = _selectedTimetableLectures.asStateFlow()

    override val selectedTimetableObject: StateFlow<Timetable> = 
        selectedTimetableLectures.map { Timetable(it) }.stateIn(CoroutineScope(Dispatchers.IO), SharingStarted.Lazily, Timetable(emptyList()))

    override val isEditable: StateFlow<Boolean> = selectedTimetable.map { tid -> tid?.id != MY_TABLE_ID
    }.stateIn(CoroutineScope(Dispatchers.IO), SharingStarted.Lazily, false)


    override val hasLectureInCurrentTable: (Lecture) -> Boolean
        get() = { lecture ->
            selectedTimetableLectures.value.any { it.id == lecture.id }
        }

    // MARK: - API
    override suspend fun load() {
        if (semesters.value.isNotEmpty()) return

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
            _selectedTimetableId.value = MY_TABLE_ID
        }
        getTimetableData(MY_TABLE_ID)
        // Fetch remote tables for the selected semester and merge
        refreshTablesForSelectedSemester()
    }

    override suspend fun selectSemester(id: String) {
        _selectedSemesterID.value = id
        _selectedTimetableId.value = MY_TABLE_ID
        getTimetableData(MY_TABLE_ID)
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
        val tid = _selectedTimetableId.value

        otlTimetableRepository.deleteTable(tid)

        // Update local store
        _currentSemesterTimetableList.value =
            _currentSemesterTimetableList.value.filter { it.id != tid }

        // Select my table
        _selectedTimetableId.value = MY_TABLE_ID
        getTimetableData(MY_TABLE_ID)
    }

    override suspend fun addLecture(lecture: Lecture) {
        otlTimetableRepository.addLecture(_selectedTimetableId.value, lecture.id)
        _selectedTimetableLectures.value += lecture
    }

    override suspend fun deleteLecture(lecture: Lecture) {
        otlTimetableRepository.deleteLecture(_selectedTimetableId.value, lecture.id)
        _selectedTimetableLectures.value = _selectedTimetableLectures.value.filter { it.id != lecture.id }
    }

    override suspend fun selectTimetable(id: Int) {
        _selectedTimetableId.value = id
        getTimetableData(id)
    }

    // MARK: - Helpers
    private suspend fun getTimetableData(id: Int) {
        if (id == MY_TABLE_ID) {
            val sid = _selectedSemesterID.value ?: return
            val semester = semesters.value.firstOrNull { it.id == sid } ?: return
            _selectedTimetableLectures.value = otlTimetableRepository.getMyTimetable(semester.year, semester.semesterType).lectures
        } else {
            _selectedTimetableLectures.value = otlTimetableRepository.getTimetable(id).lectures
        }
    }

    private suspend fun refreshTablesForSelectedSemester() {
        val sid = _selectedSemesterID.value?: return
        val semester = selectedSemester.value?: return

        if (fetchingSemesters.contains(sid)) return
        fetchingSemesters.add(sid)
        try {
            val fetched =
                otlTimetableRepository.getTimetables(semester.year, semester.semesterType)
            _currentSemesterTimetableList.value = listOf(
                TimetableListItem(
                    id = MY_TABLE_ID,
                    year = semester.year,
                    semester = semester.semesterType,
                    name = stringProvider.get(R.string.my_table),
                    timetableOrder = -1
                )
            ) + fetched

            if (_selectedTimetableId.value.let { fetched.none { t -> t.id == it } }) {
                if (_selectedTimetableId.value != MY_TABLE_ID) {
                    _selectedTimetableId.value = MY_TABLE_ID
                }
            }
        } finally {
            fetchingSemesters.remove(sid)
        }
    }
}