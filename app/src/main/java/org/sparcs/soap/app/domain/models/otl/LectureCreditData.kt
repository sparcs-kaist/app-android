package org.sparcs.soap.app.domain.models.otl

import org.sparcs.soap.app.domain.enums.otl.LectureType

data class LectureCreditData(
    val lectureType: LectureType,
    val credits: Int
) {
    val id: Int
        get() = lectureType.labelRes

    companion object {
        val mockList = listOf(
            LectureCreditData(LectureType.BR, 6),
            LectureCreditData(LectureType.BE, 3),
            LectureCreditData(LectureType.MR, 3),
            LectureCreditData(LectureType.ME, 0),
            LectureCreditData(LectureType.HSE, 3),
            LectureCreditData(LectureType.ETC, 3),
        )
    }
}