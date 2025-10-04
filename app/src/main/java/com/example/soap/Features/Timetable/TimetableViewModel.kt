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

    val selectedTimetableDisplayName: String
        get() = timetableUseCase.selectedTimetableDisplayName

    var candidateLecture: Lecture? = null

    val isEditable: Boolean
        get() = selectedTimetable.value?.id?.contains("myTable")?.not() ?: false

    // MARK: - Functions
    fun fetchData() {
        viewModelScope.launch {
            isLoading.value = true
            try {
                timetableUseCase.load()
            } catch (e: Exception) {
                // TODO: handle error
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
            timetableUseCase.selectedSemesterID = semestersList[currentIndex - 1].id
        }
    }
    fun selectNextSemester() {
        val semestersList = timetableUseCase.semesters.value
        val currentIndex = timetableUseCase.selectedSemesterID?.let { id ->
            semestersList.indexOfFirst { it.id == id }
        } ?: return

        if (currentIndex >= 0 && currentIndex < semestersList.size - 1) {
            timetableUseCase.selectedSemesterID = semestersList[currentIndex + 1].id
        }
    }

    fun selectTimetable(id: String) {
        timetableUseCase.selectedTimetableID = id
        _selectedTimetable.value = timetableUseCase.selectedTimetable?.let { timetable ->
            candidateLecture?.let { lecture ->
                timetable.copy(lectures = timetable.lectures + lecture)
            } ?: timetable
        }
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
}