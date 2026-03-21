package org.sparcs.soap.App.Shared.Mocks.OTL

import org.sparcs.soap.App.Domain.Enums.OTL.LectureType
import org.sparcs.soap.App.Domain.Models.OTL.CourseSummary
import org.sparcs.soap.App.Domain.Models.OTL.Department
import org.sparcs.soap.App.Domain.Models.OTL.Professor

fun CourseSummary.Companion.mock(): CourseSummary {
    return CourseSummary(
        id = 16614,
        code = "CS.20300",
        name = "System Programming",
        summary = "Learn system-level programming with a focus on OS concepts and low-level abstractions.",
        department = Department(id = 9945, name = "School of Computing"),
        professors = listOf(
            Professor(id = 2269, name = "Jane Doe"),
            Professor(id = 2438, name = "John Smith")
        ),
        type = LectureType.ME,
        completed = false,
        open = true
    )
}

fun CourseSummary.Companion.mockList(): List<CourseSummary> {
    return listOf(
        CourseSummary(
            id = 16614,
            code = "CS.20300",
            name = "System Programming",
            summary = "Learn system-level programming with a focus on OS concepts and low-level abstractions.",
            department = Department(id = 9945, name = "School of Computing"),
            professors = listOf(
                Professor(id = 2269, name = "Jane Doe"),
                Professor(id = 2438, name = "John Smith")
            ),
            type = LectureType.ME,
            completed = false,
            open = true
        ),
        CourseSummary(
            id = 18725,
            code = "EE311",
            name = "Operating Systems for Electrical Engineering",
            summary = "",
            department = Department(id = 3845, name = "Department of Electrical Engineering"),
            professors = listOf(
                Professor(id = 1951, name = "Chris Park")
            ),
            type = LectureType.ME,
            completed = true,
            open = false
        ),
        CourseSummary(
            id = 17002,
            code = "CS.30300",
            name = "Operating Systems and Lab",
            summary = "Covers OS fundamentals with hands-on labs and systems design exercises.",
            department = Department(id = 9945, name = "School of Computing"),
            professors = listOf(
                Professor(id = 2510, name = "Alex Kim")
            ),
            type = LectureType.MR,
            completed = false,
            open = true
        )
    )
}