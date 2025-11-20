package com.sparcs.soap.Features.Timetable

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sparcs.soap.Domain.Models.OTL.Lecture
import com.sparcs.soap.Domain.Models.OTL.Semester
import com.sparcs.soap.Domain.Models.OTL.Timetable
import com.sparcs.soap.Domain.Usecases.TimetableUseCaseProtocol
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class TimetableViewModel @Inject constructor(
    val timetableUseCase: TimetableUseCaseProtocol,
) : ViewModel() {

    val isLoading = MutableStateFlow(false)

    val semesters: StateFlow<List<Semester>> = timetableUseCase.semesters
    val selectedSemester: StateFlow<Semester?> = timetableUseCase.selectedSemester
    val selectedTimetable: StateFlow<Timetable?> = timetableUseCase.selectedTimetable

    val selectedTimetableDisplayName: StateFlow<String> =
        timetableUseCase.selectedTimetableDisplayName
    val isEditable: StateFlow<Boolean> = timetableUseCase.isEditable

    val timetableIDsForSelectedSemester: List<String>
        get() = timetableUseCase.timetableIDsForSelectedSemester

    private val _candidateLecture = MutableStateFlow<Lecture?>(null)
    val candidateLecture: StateFlow<Lecture?> = _candidateLecture.asStateFlow()

    val isCandidateOverlapping: StateFlow<Boolean> =
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

    val overlappingLecture: StateFlow<Lecture?> =
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

    fun setCandidateLecture(lecture: Lecture?) {
        _candidateLecture.value = lecture
    }

    // MARK: - Functions
    fun fetchData() {
        viewModelScope.launch {
            isLoading.value = true
            try {
                timetableUseCase.load()
            } catch (e: Exception) {
                Log.e("TimetableViewModel", "failed to fetch Timetable Data")
            } finally {
                isLoading.value = false
            }
        }
    }

    fun selectPreviousSemester() {
        val semestersList = timetableUseCase.semesters.value
        val currentIndex = timetableUseCase.selectedSemesterID.value?.let { id ->
            semestersList.indexOfFirst { it.id == id }
        } ?: return

        if (currentIndex > 0) {
            val newSemester = semestersList[currentIndex - 1]
            timetableUseCase.setSelectedSemesterID(newSemester.id)
        }
        val defaultTableId = timetableUseCase.timetableIDsForSelectedSemester.firstOrNull()
        defaultTableId?.let { selectTimetable(it) }
    }

    fun selectNextSemester() {
        val semestersList = timetableUseCase.semesters.value
        val currentIndex = timetableUseCase.selectedSemesterID.value?.let { id ->
            semestersList.indexOfFirst { it.id == id }
        } ?: return

        if (currentIndex >= 0 && currentIndex < semestersList.size - 1) {
            val newSemester = semestersList[currentIndex + 1]
            timetableUseCase.setSelectedSemesterID(newSemester.id)
        }
        val defaultTableId = timetableUseCase.timetableIDsForSelectedSemester.firstOrNull()
        defaultTableId?.let { selectTimetable(it) }
    }

    fun selectTimetable(id: String) {
        timetableUseCase.selectTimetable(id)
    }

    fun createTable() {
        viewModelScope.launch {
            try {
                timetableUseCase.createTable()
            } catch (e: Exception) {
                Log.e("TimetableViewModel", "Error creating table", e)
            }
        }
    }

    fun deleteTable() {
        viewModelScope.launch {
            try {
                timetableUseCase.deleteTable()
            } catch (e: Exception) {
                Log.e("TimetableViewModel", "Error deleting table", e)
            }
        }
    }

    fun addLecture(lecture: Lecture) {
        viewModelScope.launch {
            try {
                timetableUseCase.addLecture(lecture)
            } catch (e: Exception) {
                Log.e("TimetableViewModel", "Error adding lecture", e)
            }
        }
    }

    fun deleteLecture(lecture: Lecture) {
        viewModelScope.launch {
            try {
                timetableUseCase.deleteLecture(lecture)
            } catch (e: Exception) {
                Log.e("TimetableViewModel", "Error deleting lecture", e)
            }
        }
    }

    fun removeOverlappingLectures(newLecture: Lecture) {
        val timetable = timetableUseCase.selectedTimetable.value ?: return
        val toRemove = timetable.lectures.firstOrNull {
            timetable.hasCollision(it) && timetable.hasCollision(newLecture)
        } ?: return

        deleteLecture(toRemove)
    }
}
