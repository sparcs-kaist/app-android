package org.sparcs.soap.creditTests

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.sparcs.soap.app.domain.enums.otl.SemesterType
import org.sparcs.soap.app.domain.models.otl.Lecture
import org.sparcs.soap.app.domain.models.otl.LectureGrade
import org.sparcs.soap.app.domain.models.otl.OTLUserLectureSemester
import org.sparcs.soap.app.domain.models.otl.RetakeResolver
import org.sparcs.soap.app.domain.models.otl.Timetable
import org.sparcs.soap.app.features.timetable.creditCalculation.CreditCalculationViewState
import org.sparcs.soap.app.shared.mocks.otl.mockList

class RetakeResolverTest {
    private val attempts = (1..3).map { Lecture.mockList().first().copy(id = it, code = "CS101", credit = 3) }

    @Test fun highestGradeCountsOnceAndLaterAttemptWinsTies() {
        val grades = mapOf(1 to LectureGrade.A, 2 to LectureGrade.B, 3 to LectureGrade.A)
        assertEquals(listOf(3), RetakeResolver.countedLectures(attempts, grades).map { it.id })
        assertEquals(setOf(1, 2), RetakeResolver.supersededLectureIDs(attempts, grades))
    }

    @Test fun pendingAttemptReplacesFailureButNotAPassingGrade() {
        assertEquals(listOf(3), RetakeResolver.countedLectures(attempts, mapOf(1 to LectureGrade.FAIL)).map { it.id })
        assertEquals(listOf(1), RetakeResolver.countedLectures(attempts, mapOf(1 to LectureGrade.PASS)).map { it.id })
    }

    @Test fun passRanksAboveFailureAndBelowLetterGrades() {
        val grades = mapOf(1 to LectureGrade.FAIL, 2 to LectureGrade.PASS, 3 to LectureGrade.D_MINUS)
        assertEquals(listOf(3), RetakeResolver.countedLectures(attempts, grades).map { it.id })
        assertEquals(listOf(2), RetakeResolver.countedLectures(attempts.take(2), grades).map { it.id })
    }

    @Test fun nonRecordAttemptsNeitherCountNorReceiveRetakenBadge() {
        val grades = attempts.associate { it.id to LectureGrade.NON_RECORD }
        assertTrue(RetakeResolver.countedLectures(attempts, grades).isEmpty())
        assertTrue(RetakeResolver.supersededLectureIDs(attempts, grades).isEmpty())
    }

    @Test fun auRetakesUseSatisfiedAndUnsatisfiedRanking() {
        val lectures = attempts.map { it.copy(credit = 0, creditAU = 1) }
        val grades = mapOf(1 to LectureGrade.SATISFIED, 2 to LectureGrade.UNSATISFIED)
        assertEquals(listOf(1), RetakeResolver.countedLectures(lectures, grades).map { it.id })
    }

    @Test fun differentCodesKeepTheirOriginalOrder() {
        val lectures = attempts.map { it.copy(code = "CS${it.id}") }
        assertEquals(lectures, RetakeResolver.countedLectures(lectures, emptyMap()))
    }

    @Test fun cumulativeTotalsResolveRetakesWhileSemesterTotalsKeepEachAttempt() {
        val first = OTLUserLectureSemester(2025, SemesterType.SPRING, emptyList())
        val second = OTLUserLectureSemester(2026, SemesterType.SPRING, emptyList())
        val state = CreditCalculationViewState(
            isLoading = false,
            semesters = listOf(first, second),
            timetables = mapOf(first.id to Timetable("first", lectures = attempts.take(1)),
                second.id to Timetable("second", lectures = attempts.slice(1..1))),
            grades = mapOf(1 to LectureGrade.B, 2 to LectureGrade.A),
        )
        assertEquals(3, state.overallSummary.earnedCredits)
        assertEquals(4.0, state.overallSummary.gpa!!, 0.001)
        assertEquals(3.0, state.summary(first)!!.gpa!!, 0.001)
        assertEquals(setOf(1), state.supersededLectureIDs)
    }
}
