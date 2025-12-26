package org.sparcs.Domain.Models.OTL

import org.sparcs.Domain.Enums.OTL.LectureType

data class LectureCreditData(
    val lectureType: LectureType,
    val credits: Int
) {
    val id: String
        get() = lectureType.code.localized("en")

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