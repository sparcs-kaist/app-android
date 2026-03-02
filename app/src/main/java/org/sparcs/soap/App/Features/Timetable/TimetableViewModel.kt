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
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import org.sparcs.soap.App.Domain.Models.OTL.Lecture
import org.sparcs.soap.App.Domain.Models.OTL.Semester
import org.sparcs.soap.App.Domain.Models.OTL.Timetable
import org.sparcs.soap.App.Domain.Services.CrashlyticsService
import org.sparcs.soap.App.Domain.Usecases.OTL.TimetableUseCase
import org.sparcs.soap.App.Shared.Extensions.isNetworkError
import org.sparcs.soap.R
import timber.log.Timber
import javax.inject.Inject

interface TimetableViewModelProtocol {
    val timetableUseCase: TimetableUseCase?
    val isLoading: MutableStateFlow<Boolean>
    val semesters: StateFlow<List<Semester>>
    val selectedSemester: StateFlow<Semester?>
    val selectedTimetable: StateFlow<Timetable?>
    val selectedTimetableDisplayName: StateFlow<String>
    val isEditable: StateFlow<Boolean>
    val timetableIDsForSelectedSemester: List<String>
    val candidateLecture: StateFlow<Lecture?>
    val isCandidateOverlapping: StateFlow<Boolean>
    val overlappingLecture: StateFlow<Lecture?>

    var showAlert: Boolean
    var alertMessageRes: Int?

    fun setCandidateLecture(lecture: Lecture?)
    fun fetchData()
    suspend fun selectPreviousSemester()
    suspend fun selectNextSemester()
    fun selectTimetable(id: String)
    fun createTable()
    fun deleteTable()
    fun addLecture(lecture: Lecture)
    fun deleteLecture(lecture: Lecture)
    fun removeOverlappingLectures(newLecture: Lecture)
}

@HiltViewModel
class TimetableViewModel @Inject constructor(
    override val timetableUseCase: TimetableUseCase,
    private val crashlyticsService: CrashlyticsService,
) : ViewModel(), TimetableViewModelProtocol {

    enum class ErrorType {
        AddLecture,
        CreateTable,
        DeleteTable,
        DeleteLecture,
        FetchData
    }

    override var showAlert by mutableStateOf(false)
    override var alertMessageRes by mutableStateOf<Int?>(null)

    override val isLoading = MutableStateFlow(false)

    override val semesters: StateFlow<List<Semester>> = timetableUseCase.semesters
    override val selectedSemester: StateFlow<Semester?> = timetableUseCase.selectedSemester
    override val selectedTimetable: StateFlow<Timetable?> = timetableUseCase.selectedTimetable

    override val selectedTimetableDisplayName: StateFlow<String> =
        timetableUseCase.selectedTimetableDisplayName
    override val isEditable: StateFlow<Boolean> = timetableUseCase.isEditable

    override val timetableIDsForSelectedSemester: List<String>
        get() = timetableUseCase.timetableIDsForSelectedSemester

    private val _candidateLecture = MutableStateFlow<Lecture?>(null)
    override val candidateLecture: StateFlow<Lecture?> = _candidateLecture.asStateFlow()

    override val isCandidateOverlapping: StateFlow<Boolean> =
        combine(
            timetableUseCase.selectedTimetable,
            candidateLecture
        ) { timetable, candidate ->
            if (timetable == null || candidate == null) {
                false
            } else {
                timetable.hasCollision(candidate)
            }
        }.distinctUntilChanged()
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5000),
                initialValue = false
            )

    override val overlappingLecture: StateFlow<Lecture?> =
        combine(
            timetableUseCase.selectedTimetable,
            candidateLecture
        ) { timetable, candidate ->
            if (timetable == null || candidate == null) {
                null
            } else {
                timetable.lectures.firstOrNull { other ->
                    timetable.hasCollision(candidate) && timetable.hasCollision(other)
                }
            }
        }.distinctUntilChanged()
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5000),
                initialValue = null
            )

    // MARK: - Functions
    override fun setCandidateLecture(lecture: Lecture?) {
        _candidateLecture.value = lecture
    }

    override fun fetchData() {
        viewModelScope.launch {
            isLoading.value = true
            try {
                timetableUseCase.load()
                isLoading.value = false
            } catch (e: Exception) {
                Timber.e("failed to fetch Timetable Data")
                handleException(e, ErrorType.FetchData)
            }
        }
    }

    override suspend fun selectPreviousSemester() {
        val semestersList = timetableUseCase.semesters.value
        val currentIndex = timetableUseCase.selectedSemesterID.value?.let { id ->
            semestersList.indexOfFirst { it.id == id }
        } ?: return

        if (currentIndex > 0) {
            val newSemester = semestersList[currentIndex - 1]
            timetableUseCase.selectSemester(newSemester.id)
        }
        val defaultTableId = timetableUseCase.timetableIDsForSelectedSemester.firstOrNull()
        defaultTableId?.let { selectTimetable(it) }
    }

    override suspend fun selectNextSemester() {
        val semestersList = timetableUseCase.semesters.value
        val currentIndex = timetableUseCase.selectedSemesterID.value?.let { id ->
            semestersList.indexOfFirst { it.id == id }
        } ?: return

        if (currentIndex >= 0 && currentIndex < semestersList.size - 1) {
            val newSemester = semestersList[currentIndex + 1]
            timetableUseCase.selectSemester(newSemester.id)
        }
        val defaultTableId = timetableUseCase.timetableIDsForSelectedSemester.firstOrNull()
        defaultTableId?.let { selectTimetable(it) }
    }

    override fun selectTimetable(id: String) {
        timetableUseCase.selectTimetable(id)
    }

    override fun createTable() {
        viewModelScope.launch {
            try {
                timetableUseCase.createTable()
            } catch (e: Exception) {
                Timber.e(e, "Error creating table")
                handleException(e, ErrorType.CreateTable)
            }
        }
    }

    override fun deleteTable() {
        viewModelScope.launch {
            try {
                timetableUseCase.deleteTable()
            } catch (e: Exception) {
                Timber.e(e, "Error deleting table")
                handleException(e, ErrorType.DeleteTable)
            }
        }
    }

    override fun addLecture(lecture: Lecture) {
        viewModelScope.launch {
            try {
                val timetable = timetableUseCase.selectedTimetable.value
                if (timetable != null && timetable.hasCollision(lecture)) {
                    removeOverlappingLectures(lecture)
                }
                timetableUseCase.addLecture(lecture)
            } catch (e: Exception) {
                Timber.e(e, "Error adding lecture")
                handleException(e, ErrorType.AddLecture)
            }
        }
    }

    override fun deleteLecture(lecture: Lecture) {
        viewModelScope.launch {
            try {
                timetableUseCase.deleteLecture(lecture)
            } catch (e: Exception) {
                Timber.e(e, "Error deleting lecture")
                handleException(e, ErrorType.DeleteLecture)
            }
        }
    }

    override fun removeOverlappingLectures(newLecture: Lecture) {
        val timetable = timetableUseCase.selectedTimetable.value ?: return

        val collisions = timetable.lectures.filter { existing ->
            timetable.hasCollisions(newLecture, existing)
        }

        viewModelScope.launch {
            collisions.forEach { lecture ->
                try {
                    timetableUseCase.deleteLecture(lecture)
                } catch (e: Exception) {
                    handleException(e, ErrorType.DeleteLecture)
                }
            }
        }
    }

    private fun handleException(error: Exception, type: ErrorType) {
        val messageRes = if (error.isNetworkError()) {
            R.string.network_connection_error
        } else {
            crashlyticsService.recordException(error)
            when (type) {
                ErrorType.AddLecture -> R.string.error_add_lecture
                ErrorType.CreateTable -> R.string.error_create_table
                ErrorType.DeleteLecture -> R.string.error_delete_lecture
                ErrorType.DeleteTable -> R.string.error_delete_table
                ErrorType.FetchData -> R.string.error_fetch_data
            }
        }

        alertMessageRes = messageRes
        showAlert = true
    }
}
