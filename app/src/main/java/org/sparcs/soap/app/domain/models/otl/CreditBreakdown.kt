package org.sparcs.soap.app.domain.models.otl

import org.sparcs.soap.app.domain.enums.otl.LectureType

class CreditBreakdown(
    lectures: List<Lecture>,
    grades: Map<Int, LectureGrade>,
    majorDepartments: List<Department>,
) {
    data class MajorGroup(val department: Department, val required: Int = 0, val elective: Int = 0)

    private val earnedLectures = lectures.filter {
        grades[it.id] != LectureGrade.FAIL && grades[it.id] != LectureGrade.NON_RECORD &&
            grades[it.id] != LectureGrade.UNSATISFIED
    }
    val basicRequired = credits(LectureType.BR)
    val basicElective = credits(LectureType.BE)
    val hseCore = credits(LectureType.HSE_CORE)
    val hseGeneral = credits(LectureType.HSE_GENERAL)
    val hse = credits(LectureType.HSE)
    val etc = credits(LectureType.ETC)
    val au = earnedLectures.sumOf { it.creditAU }
    val majors: List<MajorGroup>

    init {
        val ownMajors = majorDepartments.distinctBy { it.id }.map { MajorGroup(it) }.toMutableList()
        val otherMajors = mutableMapOf<Int, MajorGroup>()
        earnedLectures.filter { it.type == LectureType.MR || it.type == LectureType.ME }.forEach { lecture ->
            val index = ownMajors.indexOfFirst {
                it.department.id == lecture.department.id || it.department.name == lecture.department.name
            }
            val group = if (index >= 0) ownMajors[index]
                else otherMajors[lecture.department.id] ?: MajorGroup(lecture.department)
            val updated = group.copy(
                department = lecture.department,
                required = group.required + if (lecture.type == LectureType.MR) lecture.credit else 0,
                elective = group.elective + if (lecture.type == LectureType.ME) lecture.credit else 0,
            )
            if (index >= 0) ownMajors[index] = updated else otherMajors[lecture.department.id] = updated
        }
        majors = ownMajors + otherMajors.values.filter { it.required + it.elective > 0 }
            .sortedBy { it.department.name }
    }

    private fun credits(type: LectureType): Int = earnedLectures.sumOf { if (it.type == type) it.credit else 0 }
}
