package com.example.soap.Features.Timetable

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.soap.Domain.Models.TimeTable.Lecture
import com.example.soap.Domain.Models.TimeTable.Semester
import com.example.soap.Domain.Models.TimeTable.Timetable
import com.example.soap.Shared.Mocks.mockList
import kotlinx.coroutines.launch

class TimetableViewModel : ViewModel() {
    var isLoading: Boolean = true
    var timetables by mutableStateOf<List<Timetable>>(emptyList())
    var selectedTimetable by mutableStateOf<Timetable?>(null)
    var semesters by mutableStateOf<List<Semester>>(emptyList())
    var selectedSemester by mutableStateOf<Semester?>(null)
    var selectedLecture by mutableStateOf<Lecture?>(null)
    var timetablesForSelectedSemester by mutableStateOf<List<Timetable>>(emptyList())

    fun fetchData() {
        viewModelScope.launch {
            try {
                timetables = Timetable.mockList()
                semesters = timetables.map { it.semester }.toSet().sorted()
                selectedTimetable = timetables.firstOrNull()
                selectedSemester = selectedTimetable?.semester
                isLoading = false
            } catch (exception: Exception) {
                println("[TimetableViewModel] fetchData failed.")
            }
        }
    }

    fun selectPreviousSemester() {
        selectedSemester?.let { currentSemester ->
            val currentIndex = semesters.indexOf(currentSemester)
            if (currentIndex > 0) {
                selectedSemester = semesters[currentIndex - 1]
                updateTimetablesForSelectedSemester()
            }
        }
    }

    fun selectNextSemester() {
        selectedSemester?.let { currentSemester ->
            val currentIndex = semesters.indexOf(currentSemester)
            if (currentIndex < semesters.size - 1) {
                selectedSemester = semesters[currentIndex + 1]
                updateTimetablesForSelectedSemester()
            }
        }
    }

    // Private Functions
    private fun updateTimetablesForSelectedSemester() {
        selectedSemester?.let {
            timetablesForSelectedSemester = timetables.filter { it.semester == selectedSemester }
            selectedTimetable = timetablesForSelectedSemester.firstOrNull()
        } ?: run {
            timetablesForSelectedSemester = emptyList()
            selectedTimetable = null
        }
    }
}
