package org.sparcs.App.Shared.ViewModelMocks.OTL

import kotlinx.coroutines.flow.MutableStateFlow
import org.sparcs.App.Domain.Models.OTL.Lecture
import org.sparcs.App.Domain.Models.OTL.Semester
import org.sparcs.App.Domain.Models.OTL.Timetable
import org.sparcs.App.Domain.Usecases.TimetableUseCase
import org.sparcs.App.Features.Timetable.TimetableViewModel
import org.sparcs.App.Features.Timetable.TimetableViewModelProtocol
import org.sparcs.App.Shared.Mocks.mock
import org.sparcs.App.Shared.Mocks.mockList

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
