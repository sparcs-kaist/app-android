package com.sparcs.soap.Shared.ViewModelMocks.OTL

import com.sparcs.soap.Domain.Models.OTL.Lecture
import com.sparcs.soap.Domain.Models.OTL.Semester
import com.sparcs.soap.Domain.Models.OTL.Timetable
import com.sparcs.soap.Domain.Usecases.TimetableUseCase
import com.sparcs.soap.Features.Timetable.TimetableViewModel
import com.sparcs.soap.Features.Timetable.TimetableViewModelProtocol
import com.sparcs.soap.Shared.Mocks.mock
import com.sparcs.soap.Shared.Mocks.mockList
import kotlinx.coroutines.flow.MutableStateFlow

class MockTimetableViewModel: TimetableViewModelProtocol {

    override val timetableUseCase: TimetableUseCase? = null
    override val isLoading = MutableStateFlow(false)
    override val semesters = MutableStateFlow(Semester.mockList())
    override val selectedSemester = MutableStateFlow(Semester.mockList()[32])
    override val selectedTimetable = MutableStateFlow(Timetable.mock())
    override val selectedTimetableDisplayName = MutableStateFlow("")
    override val isEditable = MutableStateFlow(true)
    override val timetableIDsForSelectedSemester: List<String> = emptyList()
    override val candidateLecture = MutableStateFlow<Lecture?>(null)
    override val isCandidateOverlapping = MutableStateFlow(false)
    override val overlappingLecture = MutableStateFlow<Lecture?>(null)

    override fun setCandidateLecture(lecture: Lecture?) {
        candidateLecture.value = lecture
    }

    override fun fetchData() {}

    override fun selectPreviousSemester() {}
    override fun selectNextSemester() {}
    override fun selectTimetable(id: String) {}
    override fun createTable() {}
    override fun deleteTable() {}
    override fun addLecture(lecture: Lecture) {}
    override fun deleteLecture(lecture: Lecture) {}
    override fun removeOverlappingLectures(newLecture: Lecture) {}
    override fun handleException(error: Throwable, type: TimetableViewModel.ErrorType) {}
}
