package org.sparcs.soap.App.Shared.Mocks.OTL

import org.sparcs.soap.App.Domain.Models.OTL.Course
import org.sparcs.soap.App.Domain.Models.OTL.CourseHistory
import org.sparcs.soap.App.Domain.Models.OTL.Department

fun Course.Companion.mock(): Course {
    return Course(
        id = 765,
        code = "CS.20300",
        department = Department(
            id = 9945,
            name = "전산학부",
        ),
        type = "전공선택",
        name = "시스템프로그래밍",
        summary = "시스템 프로그램의 두가지 관점인 기계중심의 관점과 산술중심의 관점을 균형있게 익힌다. 기계중심의 관점에서는 명령어 아키텍쳐의 이해와 어셈블리 프로그래밍, 운영체제의 원리와 실습 등을 다룬다. 산술중심의관점에서는 고급의 언어를 이용한 프로그래밍 기법들을 중심으로, 데이터와 함수, 반복과 재귀, 함수와 데이터를 이용한 프로그램의 요약, 모듈 프로그래밍 등을 익힌다.",
        credit = 3,
        creditAu = 0,
        classDuration = 3,
        expDuration = 0,
        history = CourseHistory.mockList()
    )
}

fun Course.Companion.mockList(): List<Course> {
    return listOf(
        Course(
            id = 4300,
            code = "EE311",
            department = Department(
                id = 3845,
                name = "전기 및 전자공학과",
            ),
            type = "전공필수",
            name = "전자공학을 위한 운영체제 및 시스템 프로그래밍",
            summary = "",
            credit = 3,
            creditAu = 0,
            classDuration = 3,
            expDuration = 0,
            history = CourseHistory.mockList()
        ),
        Course(
            id = 750,
            code = "CS.30300",
            department = Department(
                id = 9945,
                name = "전산학부"
            ),
            type = "전공필수",
            name = "운영체제 및 실험",
            summary = "운영체제는 컴퓨터 시스템에서 가장 중요한 요소 중 하나로, 이 과목에서는 운영 체제의 기본 개념과 여러가지 기능들을 다룬다. 또한, 운영 체제의 몇 가지 기능들을 설계하고 구현하는 실습을 병행한다. (선수 과목 : CS230)",
            credit = 3,
            creditAu = 0,
            classDuration = 3,
            expDuration = 0,
            history = CourseHistory.mockList()
        )
    )
}