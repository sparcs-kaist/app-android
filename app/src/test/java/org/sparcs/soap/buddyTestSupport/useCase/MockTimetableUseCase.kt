package org.sparcs.soap.buddyTestSupport.useCase

import org.sparcs.soap.app.domain.models.otl.Semester
import org.sparcs.soap.app.domain.models.otl.Timetable
import org.sparcs.soap.app.domain.models.otl.TimetableCreation
import org.sparcs.soap.app.domain.models.otl.TimetableSummary
import org.sparcs.soap.app.domain.usecases.otl.TimetableUseCaseProtocol
import org.sparcs.soap.app.shared.mocks.otl.mock
import org.sparcs.soap.app.shared.mocks.otl.mockList

class MockTimetableUseCase : TimetableUseCaseProtocol {

    var getSemestersResult: Result<List<Semester>> = Result.success(Semester.mockList())
    var getCurrentSemesterResult: Result<Semester> = Result.success(Semester.mockList().first())
    var getTimetableListResult: Result<List<TimetableSummary>> = Result.success(emptyList())
    var getTableResult: Result<Timetable> = Result.success(Timetable.mock())
    var getMyTableResult: Result<Timetable> = Result.success(Timetable.mock())
    var addLectureResult: Result<Unit> = Result.success(Unit)
    var deleteLectureResult: Result<Unit> = Result.success(Unit)
    var deleteTableResult: Result<Unit> = Result.success(Unit)
    var renameTableResult: Result<Unit> = Result.success(Unit)

    var deleteLectureCallCount = 0

    override suspend fun getSemesters(): List<Semester> = getSemestersResult.getOrThrow()
    override suspend fun getCurrentSemester(): Semester = getCurrentSemesterResult.getOrThrow()
    override suspend fun getTimetableList(semester: Semester): List<TimetableSummary> =
        getTimetableListResult.getOrThrow()

    override suspend fun getTable(id: Int, forceRefresh: Boolean): Timetable =
        getTableResult.getOrThrow()

    override suspend fun getMyTable(semester: Semester, forceRefresh: Boolean): Timetable =
        getMyTableResult.getOrThrow()

    override suspend fun deleteTable(id: Int) {
        deleteTableResult.getOrThrow()
    }

    override suspend fun renameTable(id: Int, title: String) {
        renameTableResult.getOrThrow()
    }

    override suspend fun createTable(semester: Semester): TimetableCreation =
        error("MockTimetableUseCase.createTable not configured")

    override suspend fun addLecture(timetableID: Int, lectureID: Int) {
        addLectureResult.getOrThrow()
    }

    override suspend fun deleteLecture(timetableID: Int, lectureID: Int) {
        deleteLectureCallCount += 1
        deleteLectureResult.getOrThrow()
    }
}
