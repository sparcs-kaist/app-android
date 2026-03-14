package org.sparcs.soap.App.Domain.Models.OTL

import org.sparcs.soap.App.Domain.Enums.OTL.LectureType

data class LectureCreditData(
    val lectureType: LectureType,
    val credits: Int
) {
    val id: Int
        get() = lectureType.code

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