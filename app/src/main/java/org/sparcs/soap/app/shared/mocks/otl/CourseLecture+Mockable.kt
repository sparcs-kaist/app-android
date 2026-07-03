package org.sparcs.soap.app.shared.mocks.otl

import org.sparcs.soap.app.domain.enums.otl.LectureType
import org.sparcs.soap.app.domain.models.otl.CourseLecture
import org.sparcs.soap.app.domain.models.otl.Lecture

fun CourseLecture.Companion.mock() = CourseLecture(
    id = 1,
    name = "소프트웨어 프로젝트",
    code = "CS408",
    type = LectureType.ME,
    lectures = listOf(Lecture.mock()),
    completed = false
)

fun CourseLecture.Companion.mockList() = listOf(
    CourseLecture.mock(),
    CourseLecture.mock().copy(
        id = 2,
        name = "이산구조",
        code = "CS204",
        type = LectureType.MR,
        completed = true
    ),
    CourseLecture.mock().copy(
        id = 3,
        name = "인공지능 개론",
        code = "CS311",
        type = LectureType.ME,
        completed = false
    ),
    CourseLecture.mock().copy(
        id = 4,
        name = "인문사회 특강",
        code = "HSS001",
        type = LectureType.HSE,
        completed = true
    )
)