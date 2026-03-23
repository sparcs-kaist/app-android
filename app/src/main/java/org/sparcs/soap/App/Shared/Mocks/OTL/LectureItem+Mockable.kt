package org.sparcs.soap.App.Shared.Mocks.OTL

import org.sparcs.soap.App.Domain.Models.OTL.Lecture
import org.sparcs.soap.App.Domain.Models.OTL.LectureItem

fun LectureItem.Companion.mock(): LectureItem {
    val lecture = Lecture.mock()
    return LectureItem(
        lecture = lecture,
        lectureClass = lecture.classes.first(),
        index = 0
    )
}

fun LectureItem.Companion.mockList(): List<LectureItem> {
    val lectures = Lecture.mockList()

    return listOf(
        LectureItem(lecture = lectures[0], lectureClass = lectures[0].classes[0], index = 0),
        LectureItem(lecture = lectures[0], lectureClass = lectures[0].classes[1], index = 1),
        LectureItem(lecture = lectures[1], lectureClass = lectures[1].classes[0], index = 2)
    )
}