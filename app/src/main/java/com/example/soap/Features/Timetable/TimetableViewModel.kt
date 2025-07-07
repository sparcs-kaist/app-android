package com.example.soap.Features.Timetable

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.soap.Models.TimeTable.Lecture
import com.example.soap.Models.TimeTable.Semester
import com.example.soap.Models.TimeTable.Timetable
import com.example.soap.Utilities.Mocks.mockList
import kotlinx.coroutines.launch

class TimetableViewModel : ViewModel() {
    var isLoading: Boolean = true
    var timetables: List<Timetable> = emptyList()
    var selectedTimetable: Timetable? = null
    var semesters: List<Semester> = emptyList()
    var selectedSemester: Semester? = null
    var selectedLecture: Lecture? = null
    var timetablesForSelectedSemester: List<Timetable> = emptyList()

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
            }
        }
    }

    fun selectNextSemester() {
        selectedSemester?.let { currentSemester ->
            val currentIndex = semesters.indexOf(currentSemester)
            if (currentIndex < semesters.size - 1) {
                selectedSemester = semesters[currentIndex + 1]
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
