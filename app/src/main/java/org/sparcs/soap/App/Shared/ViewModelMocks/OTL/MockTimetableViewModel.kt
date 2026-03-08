package org.sparcs.soap.App.Shared.ViewModelMocks.OTL

import kotlinx.coroutines.flow.MutableStateFlow
import org.sparcs.soap.App.Domain.Models.OTL.Lecture
import org.sparcs.soap.App.Domain.Models.OTL.Semester
import org.sparcs.soap.App.Domain.Models.OTL.Timetable
import org.sparcs.soap.App.Domain.Usecases.OTL.TimetableUseCase
import org.sparcs.soap.App.Features.Timetable.TimetableViewModelProtocol
import org.sparcs.soap.App.Shared.Mocks.mock
import org.sparcs.soap.App.Shared.Mocks.mockList

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

    override var showAlert: Boolean = false
    override var alertMessageRes: Int? = null

    override fun setCandidateLecture(lecture: Lecture?) {
        candidateLecture.value = lecture
    }

    override fun fetchData() {}

    override suspend fun selectPreviousSemester() {}
    override suspend fun selectNextSemester() {}
    override fun selectTimetable(id: String) {}
    override fun createTable() {}
    override fun deleteTable() {}
    override fun addLecture(lecture: Lecture) {}
    override fun deleteLecture(lecture: Lecture) {}
    override fun removeOverlappingLectures(newLecture: Lecture) {}
}
