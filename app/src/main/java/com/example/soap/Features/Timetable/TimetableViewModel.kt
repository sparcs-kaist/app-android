package com.example.soap.Features.Timetable

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.soap.Domain.Models.OTL.Lecture
import com.example.soap.Domain.Models.OTL.Semester
import com.example.soap.Domain.Models.OTL.Timetable
import com.example.soap.Domain.Usecases.TimetableUseCaseProtocol
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class TimetableViewModel @Inject constructor(
    private val timetableUseCase: TimetableUseCaseProtocol
): ViewModel() {

    val isLoading = MutableStateFlow(false)

    val semesters: StateFlow<List<Semester>> = timetableUseCase.semesters

    private val _selectedSemester = MutableStateFlow(timetableUseCase.selectedSemester)
    val selectedSemester: StateFlow<Semester?> = _selectedSemester

    private val _selectedTimetable = MutableStateFlow(timetableUseCase.selectedTimetable)
    val selectedTimetable: StateFlow<Timetable?> = _selectedTimetable

    val timetableIDsForSelectedSemester: List<String>
        get() = timetableUseCase.timetableIDsForSelectedSemester

    private val _selectedTimetableDisplayName = MutableStateFlow(timetableUseCase.selectedTimetableDisplayName)
    val selectedTimetableDisplayName: StateFlow<String> = _selectedTimetableDisplayName

    val _candidateLecture = MutableStateFlow<Lecture?>(null)
    val candidateLecture: StateFlow<Lecture?> = _candidateLecture

    fun setCandidateLecture(lecture: Lecture?) {
        _candidateLecture.value = lecture
    }

    val isEditable: Boolean
        get() = selectedTimetable.value?.id?.contains("myTable")?.not() ?: false

    // MARK: - Functions
    fun fetchData() {
        viewModelScope.launch {
            isLoading.value = true
            try {
                timetableUseCase.load()
                _selectedSemester.value = timetableUseCase.selectedSemester
                _selectedTimetable.value = timetableUseCase.selectedTimetable
                _selectedTimetableDisplayName.value = timetableUseCase.selectedTimetableDisplayName
            } catch (e: Exception) {
                Log.e("TimetableViewModel", "failed to fetch Timetable Data")
            } finally {
                isLoading.value = false
            }
        }
    }

    fun selectPreviousSemester() {
        val semestersList = timetableUseCase.semesters.value
        val currentIndex = timetableUseCase.selectedSemesterID?.let { id ->
            semestersList.indexOfFirst { it.id == id }
        } ?: return

        if (currentIndex > 0) {
            val newSemester = semestersList[currentIndex - 1]
            timetableUseCase.selectedSemesterID = newSemester.id
            _selectedSemester.value = newSemester
        }
    }

    fun selectNextSemester() {
        val semestersList = timetableUseCase.semesters.value
        val currentIndex = timetableUseCase.selectedSemesterID?.let { id ->
            semestersList.indexOfFirst { it.id == id }
        } ?: return

        if (currentIndex >= 0 && currentIndex < semestersList.size - 1) {
            val newSemester = semestersList[currentIndex + 1]
            timetableUseCase.selectedSemesterID = newSemester.id
            _selectedSemester.value = newSemester
        }
    }

    fun selectTimetable(id: String) {
        timetableUseCase.selectedTimetableID = id
        _selectedTimetable.value = timetableUseCase.selectedTimetable?.let { timetable ->
            candidateLecture.value?.let { lecture ->
                timetable.copy(lectures = timetable.lectures + lecture)
            } ?: timetable
        }
        _selectedTimetableDisplayName.value = timetableUseCase.selectedTimetableDisplayName
    }

    fun createTable() {
        viewModelScope.launch {
            try {
                timetableUseCase.createTable()
                _selectedTimetableDisplayName.value = timetableUseCase.selectedTimetableDisplayName
                _selectedTimetable.value = timetableUseCase.selectedTimetable
            } catch (e: Exception) {
                Log.e("TimetableViewModel", "Error creating table", e)
            }
        }
    }

    fun deleteTable() {
        viewModelScope.launch {
            try {
                timetableUseCase.deleteTable()
                _selectedTimetableDisplayName.value = timetableUseCase.selectedTimetableDisplayName
                _selectedTimetable.value = timetableUseCase.selectedTimetable
            } catch (e: Exception) {
                Log.e("TimetableViewModel", "Error deleting table", e)
            }
        }
    }

    fun addLecture(lecture: Lecture) {
        viewModelScope.launch {
            try {
                timetableUseCase.addLecture(lecture)
                _selectedTimetable.value = timetableUseCase.selectedTimetable
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
}