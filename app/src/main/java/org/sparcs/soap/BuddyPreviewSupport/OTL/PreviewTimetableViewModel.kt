package org.sparcs.soap.BuddyPreviewSupport.OTL

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import org.sparcs.soap.App.Domain.Models.OTL.Lecture
import org.sparcs.soap.App.Domain.Models.OTL.Semester
import org.sparcs.soap.App.Domain.Models.OTL.Timetable
import org.sparcs.soap.App.Domain.Models.OTL.TimetableSummary
import org.sparcs.soap.App.Domain.Usecases.OTL.TimetableUseCase
import org.sparcs.soap.App.Features.Timetable.TimetableViewModelProtocol
import org.sparcs.soap.App.Shared.Mocks.OTL.mock
import org.sparcs.soap.App.Shared.Mocks.OTL.mockList

class PreviewTimetableViewModel : TimetableViewModelProtocol {
    override val timetableUseCase: TimetableUseCase? = null
    override val isLoading = MutableStateFlow(false)

    private val _semesters = MutableStateFlow(Semester.mockList())
    override val semesters: StateFlow<List<Semester>> = _semesters.asStateFlow()

    private val _selectedSemester = MutableStateFlow<Semester?>(Semester.mockList()[9])
    override val selectedSemester: StateFlow<Semester?> = _selectedSemester.asStateFlow()

    private val _selectedTimetable = MutableStateFlow<Timetable?>(Timetable.mock())
    override val selectedTimetable: StateFlow<Timetable?> = _selectedTimetable.asStateFlow()

    private val _timetableList = MutableStateFlow(TimetableSummary.mockList())
    override val timetableList: StateFlow<List<TimetableSummary>> = _timetableList.asStateFlow()

    private val _selectedTimetableID = MutableStateFlow<Int?>(1)
    override val selectedTimetableID: StateFlow<Int?> = _selectedTimetableID.asStateFlow()

    private val _candidateLecture = MutableStateFlow<Lecture?>(null)
    override val candidateLecture: StateFlow<Lecture?> = _candidateLecture.asStateFlow()

    private val _isCandidateOverlapping = MutableStateFlow(false)
    override val isCandidateOverlapping: StateFlow<Boolean> = _isCandidateOverlapping.asStateFlow()

    private val _overlappingLecture = MutableStateFlow<Lecture?>(null)
    override val overlappingLecture: StateFlow<Lecture?> = _overlappingLecture.asStateFlow()

    private val _isEditable = MutableStateFlow(true)
    override val isEditable: StateFlow<Boolean> = _isEditable.asStateFlow()

    private val _timetableName = MutableStateFlow("My Table")
    override val timetableName: StateFlow<String> = _timetableName.asStateFlow()

    override var showAlert: Boolean = false
    override var alertMessageRes: Int? = null

    override fun setCandidateLecture(lecture: Lecture?) {
        _candidateLecture.value = lecture
    }

    override fun fetchData() {}
    override suspend fun selectPreviousSemester() {}
    override suspend fun selectNextSemester() {}

    override fun selectTimetable(id: Int) {
        _selectedTimetableID.value = id
    }

    override fun createTable() {}
    override fun deleteTable() {}
    override fun renameTable(title: String) {}

    override fun addLecture(lecture: Lecture) {
        val current = _selectedTimetable.value
        if (current != null) {
            _selectedTimetable.value = current.copy(lectures = current.lectures + lecture)
        }
    }

    override fun deleteLecture(lecture: Lecture) {
        val current = _selectedTimetable.value
        if (current != null) {
            _selectedTimetable.value = current.copy(lectures = current.lectures.filter { it.id != lecture.id })
        }
    }

    override fun removeOverlappingLectures(newLecture: Lecture) {}
}