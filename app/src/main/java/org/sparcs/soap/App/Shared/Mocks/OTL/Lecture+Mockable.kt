package org.sparcs.soap.App.Shared.Mocks.OTL

import org.sparcs.soap.App.Domain.Enums.OTL.DayType
import org.sparcs.soap.App.Domain.Enums.OTL.LectureType
import org.sparcs.soap.App.Domain.Models.OTL.Department
import org.sparcs.soap.App.Domain.Models.OTL.Lecture
import org.sparcs.soap.App.Domain.Models.OTL.LectureClass
import org.sparcs.soap.App.Domain.Models.OTL.LectureExam
import org.sparcs.soap.App.Domain.Models.OTL.Professor

fun Lecture.Companion.mock(): Lecture {
    return Lecture(
        id = 10101,
        courseID = 20300,
        section = "A",
        name = "System Programming",
        subtitle = "Fall 2025",
        code = "CS.20300",
        department = Department(id = 9945, name = "School of Computing"),
        type = LectureType.ME,
        capacity = 45,
        enrolledCount = 42,
        credit = 3,
        creditAU = 0,
        grade = 4.2,
        load = 3.8,
        speech = 4.0,
        isEnglish = true,
        professors = listOf(
            Professor(id = 2269, name = "Jane Doe")
        ),
        classes = listOf(
            LectureClass(
                day = DayType.MON,
                begin = 540,
                end = 630,
                buildingCode = "E11",
                buildingName = "Creative Learning B/D",
                roomName = "304"
            ),
            LectureClass(
                day = DayType.WED,
                begin = 540,
                end = 630,
                buildingCode = "E11",
                buildingName = "Creative Learning B/D",
                roomName = "304"
            )
        ),
        exams = listOf(
            LectureExam(
                day = DayType.THU,
                description = "Midterm",
                begin = 600,
                end = 720
            )
        ),
        classDuration = 90,
        expDuration = 120
    )
}

fun Lecture.Companion.mockList(): List<Lecture> {
    return listOf(
        Lecture.mock(),
        Lecture(
            id = 10102,
            courseID = 31100,
            section = "B",
            name = "Operating Systems",
            subtitle = "Fall 2025",
            code = "CS.31100",
            department = Department(id = 9945, name = "School of Computing"),
            type = LectureType.MR,
            capacity = 60,
            enrolledCount = 58,
            credit = 3,
            creditAU = 0,
            grade = 4.0,
            load = 3.6,
            speech = 3.7,
            isEnglish = false,
            professors = listOf(
                Professor(id = 2438, name = "John Smith")
            ),
            classes = listOf(
                LectureClass(
                    day = DayType.TUE,
                    begin = 660,
                    end = 750,
                    buildingCode = "E3",
                    buildingName = "Tech Building",
                    roomName = "201"
                ),
                LectureClass(
                    day = DayType.THU,
                    begin = 660,
                    end = 750,
                    buildingCode = "E3",
                    buildingName = "Tech Building",
                    roomName = "201"
                )
            ),
            exams = listOf(
                LectureExam(
                    day = DayType.FRI,
                    description = "Final",
                    begin = 540,
                    end = 720
                )
            ),
            classDuration = 90,
            expDuration = 120
        )
    )
}