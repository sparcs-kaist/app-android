package com.example.soap.Features.Timetable

import android.util.Log
import androidx.lifecycle.ViewModel
import com.example.soap.Domain.Models.OTL.Lecture
import com.example.soap.Domain.Models.OTL.Semester
import com.example.soap.Domain.Models.OTL.Timetable
import com.example.soap.Domain.Usecases.TimetableUseCaseProtocol
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class TimetableViewModel @Inject constructor(
    private val timetableUseCase: TimetableUseCaseProtocol
): ViewModel() {
    private val scope = CoroutineScope(Dispatchers.Main)

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
        scope.launch {
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
        scope.launch {
            try {
                timetableUseCase.createTable()
            } catch (e: Exception) {
                Log.e("TimetableViewModel", "Error creating table", e)
            }
        }
    }
}