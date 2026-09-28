package org.sparcs.soap.creditTests

import com.google.gson.Gson
import kotlinx.serialization.json.Json
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.sparcs.soap.app.domain.enums.otl.LectureType
import org.sparcs.soap.app.domain.enums.otl.SemesterType
import org.sparcs.soap.app.domain.models.otl.CreditBreakdown
import org.sparcs.soap.app.domain.models.otl.CreditRequirements
import org.sparcs.soap.app.domain.models.otl.Department
import org.sparcs.soap.app.domain.models.otl.Lecture
import org.sparcs.soap.app.domain.models.otl.LectureGrade
import org.sparcs.soap.app.domain.models.otl.SemesterGradeSummary
import org.sparcs.soap.app.domain.models.otl.Timetable
import org.sparcs.soap.app.networking.responseDTO.otl.LectureClassDTO
import org.sparcs.soap.app.networking.responseDTO.otl.OTLUserLectureHistoryDTO
import org.sparcs.soap.app.shared.mocks.otl.mockList

class CreditCalculationTest {
    private fun lecture(id: Int, credit: Int = 3, au: Int = 0, type: LectureType = LectureType.BR) =
        Lecture.mockList().first().copy(id = id, credit = credit, creditAU = au, type = type)

    @Test fun `GPA is weighted by credits and F counts as zero`() {
        val summary = SemesterGradeSummary.calculate(
            listOf(lecture(1, 3), lecture(2, 1), lecture(3, 2)),
            mapOf(1 to LectureGrade.A_PLUS, 2 to LectureGrade.B, 3 to LectureGrade.FAIL)
        )
        assertEquals(2.65, summary.gpa!!, 0.00001)
        assertEquals(6, summary.recordedCredits)
        assertEquals(4, summary.earnedCredits)
        assertTrue(summary.isComplete)
    }

    @Test fun `pass NR AU and ungraded have distinct credit rules`() {
        val lectures = listOf(lecture(1), lecture(2), lecture(3, 0, 2), lecture(4), lecture(5, 0, 1))
        val grades = mapOf(1 to LectureGrade.PASS, 2 to LectureGrade.NON_RECORD,
            3 to LectureGrade.SATISFIED, 5 to LectureGrade.UNSATISFIED)
        val summary = SemesterGradeSummary.calculate(lectures, grades)
        assertNull(summary.gpa)
        assertEquals(6, summary.recordedCredits)
        assertEquals(6, summary.earnedCredits)
        assertEquals(4, summary.gradedCount)
        assertFalse(summary.isComplete)
        assertEquals(2, CreditBreakdown(lectures, grades, emptyList()).au)
        assertEquals(listOf(LectureGrade.SATISFIED, LectureGrade.UNSATISFIED), LectureGrade.options(lectures[2]))
        assertTrue(LectureGrade.options(lectures[0]).containsAll(listOf(LectureGrade.SATISFIED, LectureGrade.UNSATISFIED)))
    }

    @Test fun `S earns credits but U earns neither credits nor AU and neither affects GPA`() {
        val lectures = listOf(lecture(1), lecture(2, au = 1), lecture(3))
        val grades = mapOf(1 to LectureGrade.SATISFIED, 2 to LectureGrade.UNSATISFIED, 3 to LectureGrade.A)
        val summary = SemesterGradeSummary.calculate(lectures, grades)
        assertEquals(4.0, summary.gpa!!, 0.00001)
        assertEquals(9, summary.recordedCredits)
        assertEquals(6, summary.earnedCredits)
        assertEquals(6, CreditBreakdown(lectures, grades, emptyList()).basicRequired)
        assertEquals(0, CreditBreakdown(lectures, grades, emptyList()).au)
    }

    @Test fun `breakdown keeps own majors first and matches department aliases`() {
        val own = Department(1, "Computing")
        val lectures = listOf(
            lecture(1, type = LectureType.MR).copy(department = Department(99, "Computing")),
            lecture(2, type = LectureType.ME).copy(department = Department(2, "Physics")),
            lecture(3, type = LectureType.HSE_CORE), lecture(4, type = LectureType.HSE_GENERAL),
            lecture(5, type = LectureType.BR), lecture(6, type = LectureType.BE)
        )
        val breakdown = CreditBreakdown(lectures, mapOf(5 to LectureGrade.FAIL, 6 to LectureGrade.NON_RECORD), listOf(own))
        assertEquals(listOf("Computing", "Physics"), breakdown.majors.map { it.department.name })
        assertEquals(3, breakdown.majors.first().required)
        assertEquals(3, breakdown.hseCore)
        assertEquals(3, breakdown.hseGeneral)
        assertEquals(0, breakdown.basicRequired)
        assertEquals(0, breakdown.basicElective)
        assertEquals(6, Timetable(id = "0", lectures = lectures).getCreditsFor(LectureType.HSE))
    }

    @Test fun `HSE subcategories parse in both languages and raw codes`() {
        listOf("인문사회선택(핵심)", "Humanities and Social Elective (Core)", "HSE_CORE",
            "Humanities & Social Elective(Social-Core)", "인선(인문-핵심)").forEach {
            assertEquals(LectureType.HSE_CORE, LectureType.fromString(it))
        }
        listOf("인문사회선택(일반)", "Humanities and Social Elective (General)", "HSE_GENERAL",
            "Humanities & Social Elective(Social-General)", "인선(사회-일반)").forEach {
            assertEquals(LectureType.HSE_GENERAL, LectureType.fromString(it))
        }
        assertEquals(LectureType.HSE, LectureType.fromString("인문사회선택"))
        assertEquals(LectureType.MR, LectureType.fromString(" major required "))
        assertEquals(LectureType.ETC, LectureType.fromString("Remember"))
    }

    @Test fun `saved requirements keep defaults for missing fields`() {
        val requirements = Json.decodeFromString<CreditRequirements>("""{"graduation":140}""")
        assertEquals(140, requirements.graduation)
        assertEquals(23, requirements.basicRequired)
        assertEquals(19, requirements.majorRequired(Department(1, "Computing")))
        assertFalse(requirements.copy(au = -1).isValid)
        assertTrue(requirements.copy(graduation = 0).isValid)
    }

    @Test fun `history DTO matches API and old lectures accept missing rooms`() {
        val history = Gson().fromJson("""{"lecturesWrap":[{"year":2024,"semester":3,"lectures":[{"courseId":10,"lectureId":20,"name":"Algorithms","code":"CS300","professors":[],"written":true}]}],"totalLecturesCount":1,"reviewedLecturesCount":1,"totalLikesCount":2}""",
            OTLUserLectureHistoryDTO::class.java).toModel()
        assertEquals(SemesterType.AUTUMN, history.semesters.first().semesterType)
        assertEquals(20, history.semesters.first().lectures.first().lectureID)
        assertTrue(history.semesters.first().lectures.first().hasWrittenReview)
        val room = Gson().fromJson("""{"day":0,"begin":540,"end":600,"buildingCode":null,"buildingName":null,"roomName":null}""", LectureClassDTO::class.java).toModel()
        assertEquals("", room.roomName)
        assertEquals("", room.buildingName)
        assertEquals("", room.buildingCode)
    }
}
