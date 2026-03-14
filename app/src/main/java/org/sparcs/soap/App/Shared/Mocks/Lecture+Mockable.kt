package org.sparcs.soap.App.Shared.Mocks

import org.sparcs.soap.App.Domain.Enums.OTL.DayType
import org.sparcs.soap.App.Domain.Enums.OTL.LectureType
import org.sparcs.soap.App.Domain.Models.OTL.ClassTime
import org.sparcs.soap.App.Domain.Models.OTL.Department
import org.sparcs.soap.App.Domain.Models.OTL.Lecture
import org.sparcs.soap.App.Domain.Models.OTL.Professor

fun Lecture.Companion.mock(): Lecture {
    return Lecture(
        id = 1916768,
        courseId = 23374,
        code = "AI.50400",
        classNo = "",
        name = "인공지능을 위한 프로그래밍",
        subtitle = "",
        department = Department(
            id = 19525,
            name = "김재철AI대학원"
        ),
        isEnglish = true,
        credit = 3,
        creditAu = 0,
        capacity = 0,
        numberOfPeople = 140,
        grade = 15.000001,
        load = 14.64670425922474,
        speech = 15.000001,
        type = LectureType.ETC,
        professors = listOf(
            Professor(
                id = 2281,
                name = "최윤재",
            )
        ),
        classTimes = listOf(
            ClassTime(
                buildingCode = "0",
                buildingName = "(0) 서울캠퍼스기타(기타)",
                roomName = "성남 킨스타워 (18F Lecture Room)",
                day = DayType.TUE,
                begin = 630,
                end = 720
            ),
            ClassTime(
                buildingCode = "0",
                buildingName = "(0) 서울캠퍼스기타(기타)",
                roomName = "성남 킨스타워 (18F Lecture Room)",
                day = DayType.THU,
                begin = 630,
                end = 720
            )
        ),
        examTimes = emptyList()
    )
}

fun Lecture.Companion.mockList(): List<Lecture> {
    return listOf(
        Lecture(
            id = 1916768,
            courseId = 23374,
            code = "AI.50400",
            classNo = "",
            name = "인공지능을 위한 프로그래밍",
            subtitle = "",
            department = Department(
                id = 19525,
                name = "김재철AI대학원",
            ),
            isEnglish = true,
            credit = 3,
            creditAu = 0,
            capacity = 0,
            numberOfPeople = 140,
            grade = 15.000001,
            load = 14.64670425922474,
            speech = 15.000001,
            type = LectureType.ETC,
            professors = listOf(
                Professor(
                    id = 2281,
                    name = "최윤재",
                )
            ),
            classTimes = listOf(
                ClassTime(
                    buildingCode = "0",
                    buildingName = "(0)서울캠퍼스기타(기타)",
                    roomName = "성남 킨스타워 (18F Lecture Room)",
                    day = DayType.TUE,
                    begin = 630,
                    end = 720
                ),
                ClassTime(
                    buildingCode = "0",
                    buildingName = "(0) 서울캠퍼스기타(기타)",
                    roomName = "성남 킨스타워 (18F Lecture Room)",
                    day = DayType.THU,
                    begin = 630,
                    end = 720
                )
            ),
            examTimes = emptyList()
        ),
        Lecture(
            id = 1908724,
            courseId = 744,
            code = "CS.10001",
            classNo = "",
            name = "프로그래밍기초",
            subtitle = "",
            department = Department(
                id = 9945,
                name = "전산학부",
            ),
            isEnglish = true,
            credit = 3,
            creditAu = 0,
            capacity = 48,
            numberOfPeople = 53,
            grade = 12.74118225329123,
            load = 11.99264633489851,
            speech = 13.873856226664769,
            type = LectureType.BR,
            professors = listOf(
                Professor(
                    id = 1541,
                    name = "고인영",
                )
            ),
            classTimes = listOf(
                ClassTime(
                    buildingCode = "E11",
                    buildingName = "(E11) 창의학습관",
                    roomName = "(304호)강의실",
                    day = DayType.MON,
                    begin = 540,
                    end = 615
                ),
                ClassTime(
                    buildingCode = "E11",
                    buildingName = "(E11) 창의학습관",
                    roomName = "(304호)강의실",
                    day = DayType.MON,
                    begin = 780,
                    end = 960
                )
            ),
            examTimes = emptyList()
        ),
        Lecture(
            id = 1908725,
            courseId = 744,
            code = "CS.10001",
            classNo = "",
            name = "프로그래밍기초",
            subtitle = "",
            department = Department(
                id = 9945,
                name = "전산학부",
            ),
            isEnglish = true,
            credit = 3,
            creditAu = 0,
            capacity = 48,
            numberOfPeople = 30,
            grade = 12.74118225329123,
            load = 11.99264633489851,
            speech = 13.873856226664769,
            type = LectureType.BR,
            professors = listOf(
                Professor(
                    id = 1541,
                    name = "고인영",
                )
            ),
            classTimes = listOf(
                ClassTime(
                    buildingCode = "E11",
                    buildingName = "(E11) 창의학습관",
                    roomName = "(308호)강의실",
                    day = DayType.FRI,
                    begin = 780,
                    end = 960
                ),
                ClassTime(
                    buildingCode = "E11",
                    buildingName = "(E11) 창의학습관",
                    roomName = "(304호)강의실",
                    day = DayType.MON,
                    begin = 1005,
                    end = 1080
                )
            ),
            examTimes = emptyList()
        ),

        Lecture(
            id = 1908726,
            courseId = 744,
            code = "CS.10001",
            classNo = "A",
            name = "프로그래밍기초",
            subtitle = "Introduction to Programming",
            department = Department(
                id = 9945,
                name = "전산학부",
            ),
            isEnglish = true,
            credit = 3,
            creditAu = 0,
            capacity = 48,
            numberOfPeople = 61,
            grade = 12.74118225329123,
            load = 11.99264633489851,
            speech = 13.873856226664769,
            type = LectureType.BR,
            professors = listOf(
                Professor(
                    id = 1541,
                    name = "고인영",
                )
            ),
            classTimes = listOf(
                ClassTime(
                    buildingCode = "E11",
                    buildingName = "(E11) 창의학습관",
                    roomName = "(304호)강의실",
                    day = DayType.MON,
                    begin = 630,
                    end = 750
                ),
                ClassTime(
                    buildingCode = "E11",
                    buildingName = "(E11) 창의학습관",
                    roomName = "(307호)강의실",
                    day = DayType.TUE,
                    begin = 540,
                    end = 720
                )
            ),
            examTimes = emptyList()
        ),
        Lecture(
            id = 1908727,
            courseId = 744,
            classNo = "A",
            code = "CS.10001",
            name = "프로그래밍기초",
            subtitle = "Introduction to Programming",
            department = Department(
                id = 9945,
                name = "전산학부",
            ),
            isEnglish = true,
            credit = 3,
            creditAu = 0,
            capacity = 48,
            numberOfPeople = 39,
            grade = 12.74118225329123,
            load = 11.99264633489851,
            speech = 13.873856226664769,
            type = LectureType.BR,
            professors = listOf(
                Professor(
                    id = 1541,
                    name = "고인영",
                )
            ),
            classTimes = listOf(
                ClassTime(
                    buildingCode = "E11",
                    buildingName = "(E11) 창의학습관",
                    roomName = "(304호)강의실",
                    day = DayType.MON,
                    begin = 630,
                    end = 750
                ),
                ClassTime(
                    buildingCode = "E11",
                    buildingName = "(E11) 창의학습관",
                    roomName = "(308호)강의실",
                    day = DayType.TUE,
                    begin = 540,
                    end = 720
                )
            ),
            examTimes = emptyList()
        ),

        Lecture(
            id = 1908728,
            courseId = 744,
            classNo = "A",
            code = "CS.10001",
            name = "프로그래밍기초",
            subtitle = "Introduction to Programming",
            department = Department(
                id = 9945,
                name = "전산학부",
            ),
            isEnglish = true,
            credit = 3,
            creditAu = 0,
            capacity = 48,
            numberOfPeople = 47,
            grade = 12.74118225329123,
            load = 11.99264633489851,
            speech = 13.873856226664769,
            type = LectureType.BR,
            professors = listOf(
                Professor(
                    id = 1541,
                    name = "고인영",
                )
            ),
            classTimes = listOf(
                ClassTime(
                    buildingCode = "E11",
                    buildingName = "(E11) 창의학습관",
                    roomName = "(304호)강의실",
                    day = DayType.MON,
                    begin = 630,
                    end = 750
                ),
                ClassTime(
                    buildingCode = "E11",
                    buildingName = "(E11) 창의학습관",
                    roomName = "(307호)강의실",
                    day = DayType.WED,
                    begin = 780,
                    end = 960
                )
            ),
            examTimes = emptyList()
        ),
        Lecture(
            id = 1908729,
            courseId = 744,
            code = "CS.10001",
            classNo = "A",
            name = "프로그래밍기초",
            subtitle = "Introduction to Programming",
            department = Department(
                id = 9945,
                name = "전산학부",
            ),
            isEnglish = true,
            credit = 3,
            creditAu = 0,
            capacity = 48,
            numberOfPeople = 24,
            grade = 12.2092786938052,
            load = 10.821787294177309,
            speech = 10.887041589757208,
            type = LectureType.BR,
            professors = listOf(
                Professor(
                    id = 1544,
                    name = "백종문",
                )
            ),
            classTimes = listOf(
                ClassTime(
                    buildingCode = "E11",
                    buildingName = "(E11) 창의학습관",
                    roomName = "(104호)강의실-터만홀",
                    day = DayType.MON,
                    begin = 630,
                    end = 750
                ),
                ClassTime(
                    buildingCode = "E11",
                    buildingName = "(E11) 창의학습관",
                    roomName = "(308호)강의실",
                    day = DayType.WED,
                    begin = 780,
                    end = 960
                )
            ),
            examTimes = emptyList()
        ),

        Lecture(
            id = 1908444,
            courseId = 744,
            code = "CS.10001",
            classNo = "A",
            name = "프로그래밍기초",
            subtitle = "Introduction to Programming",
            department = Department(
                id = 9945,
                name = "전산학부",
            ),
            isEnglish = true,
            credit = 3,
            creditAu = 0,
            capacity = 48,
            numberOfPeople = 49,
            grade = 12.2092786938052,
            load = 10.821787294177309,
            speech = 10.887041589757208,
            type = LectureType.BR,
            professors = listOf(
                Professor(
                    id = 1544,
                    name = "백종문",
                )
            ),
            classTimes = listOf(
                ClassTime(
                    buildingCode = "E11",
                    buildingName = "(E11) 창의학습관",
                    roomName = "(104호)강의실-터만홀",
                    day = DayType.MON,
                    begin = 630,
                    end = 750
                ),
                ClassTime(
                    buildingCode = "E11",
                    buildingName = "(E11) 창의학습관",
                    roomName = "(307호)강의실",
                    day = DayType.THU,
                    begin = 540,
                    end = 720
                )
            ),
            examTimes = emptyList()
        ),
        Lecture(
            id = 1908730,
            courseId = 744,
            code = "CS.10001",
            classNo = "A",
            name = "프로그래밍기초",
            subtitle = "Introduction to Programming",
            department = Department(
                id = 9945,
                name = "전산학부",
            ),
            isEnglish = true,
            credit = 3,
            creditAu = 0,
            capacity = 48,
            numberOfPeople = 37,
            grade = 12.2092786938052,
            load = 10.821787294177309,
            speech = 10.887041589757208,
            type = LectureType.BR,
            professors = listOf(
                Professor(
                    id = 1544,
                    name = "백종문",
                )
            ),
            classTimes = listOf(
                ClassTime(
                    buildingCode = "E11",
                    buildingName = "(E11) 창의학습관",
                    roomName = "(104호)강의실-터만홀",
                    day = DayType.MON,
                    begin = 630,
                    end = 750
                ),
                ClassTime(
                    buildingCode = "E11",
                    buildingName = "(E11) 창의학습관",
                    roomName = "(308호)강의실",
                    day = DayType.THU,
                    begin = 540,
                    end = 720
                )
            ),
            examTimes = emptyList()
        ),

        Lecture(
            id = 1908445,
            courseId = 744,
            code = "CS.10001",
            classNo = "A",
            name = "프로그래밍기초",
            subtitle = "Introduction to Programming",
            department = Department(
                id = 9945,
                name = "전산학부",
            ),
            isEnglish = true,
            credit = 3,
            creditAu = 0,
            capacity = 48,
            numberOfPeople = 75,
            grade = 12.2092786938052,
            load = 10.821787294177309,
            speech = 10.887041589757208,
            type = LectureType.BR,
            professors = listOf(
                Professor(
                    id = 1544,
                    name = "백종문",
                )
            ),
            classTimes = listOf(
                ClassTime(
                    buildingCode = "E11",
                    buildingName = "(E11) 창의학습관",
                    roomName = "(104호)강의실-터만홀",
                    day = DayType.MON,
                    begin = 630,
                    end = 750
                ),
                ClassTime(
                    buildingCode = "E11",
                    buildingName = "(E11) 창의학습관",
                    roomName = "(307호)강의실",
                    day = DayType.FRI,
                    begin = 540,
                    end = 720
                )
            ),
            examTimes = emptyList()
        ),
        Lecture(
            id = 1908389,
            courseId = 744,
            code = "CS.10001",
            classNo = "A",
            name = "프로그래밍기초",
            subtitle = "Introduction to Programming",
            department = Department(
                id = 9945,
                name = "전산학부",
            ),
            isEnglish = true,
            credit = 3,
            creditAu = 0,
            capacity = 48,
            numberOfPeople = 66,
            grade = 12.2092786938052,
            load = 10.821787294177309,
            speech = 10.887041589757208,
            type = LectureType.BR,
            professors = listOf(
                Professor(
                    id = 1544,
                    name = "백종문",
                )
            ),
            classTimes = listOf(
                ClassTime(
                    buildingCode = "E11",
                    buildingName = "(E11) 창의학습관",
                    roomName = "(104호)강의실-터만홀",
                    day = DayType.MON,
                    begin = 630,
                    end = 750
                ),
                ClassTime(
                    buildingCode = "E11",
                    buildingName = "(E11) 창의학습관",
                    roomName = "(308호)강의실",
                    day = DayType.FRI,
                    begin = 540,
                    end = 720
                )
            ),
            examTimes = emptyList()
        ),

        Lecture(
            id = 1909208,
            courseId = 763,
            code = "CS.10009",
            classNo = "A",
            name = "프로그래밍 실습",
            subtitle = "프로그래밍 실습",
            department = Department(
                id = 9945,
                name = "전산학부",
            ),
            isEnglish = true,
            credit = 3,
            creditAu = 0,
            capacity = 40,
            numberOfPeople = 37,
            grade = 0.000001,
            load = 0.000001,
            speech = 0.000001,
            type = LectureType.BE,
            professors = listOf(
                Professor(
                    id = 39201,
                    name = "문은영",
                )
            ),
            classTimes = listOf(
                ClassTime(
                    buildingCode = "E3-5",
                    buildingName = "(E3-5) 크래프톤 빌딩",
                    roomName = "(210호)대형강의실",
                    day = DayType.TUE,
                    begin = 630,
                    end = 690
                ),
                ClassTime(
                    buildingCode = "E3-5",
                    buildingName = "(E3-5) 크래프톤 빌딩",
                    roomName = "(210호)대형강의실",
                    day = DayType.THU,
                    begin = 630,
                    end = 690
                )
            ),
            examTimes = emptyList()
        ),

        Lecture(
            id = 1909089,
            courseId = 765,
            code = "CS.20300",
            classNo = "A",
            name = "시스템프로그래밍",
            subtitle = "시스템프로그래밍",
            department = Department(
                id = 9945,
                name = "전산학부",
            ),
            isEnglish = true,
            credit = 3,
            creditAu = 0,
            capacity = 0,
            numberOfPeople = 140,
            grade = 13.2942608089639,
            load = 8.842953455343785,
            speech = 14.209425118646509,
            type = LectureType.ME,
            professors = listOf(
                Professor(
                    id = 1474,
                    name = "허재혁",
                )
            ),
            classTimes = listOf(
                ClassTime(
                    buildingCode = "501",
                    buildingName = "(501) 온라인강의실",
                    roomName = "비대면강의(시험장소 별도)",
                    day = DayType.TUE,
                    begin = 870,
                    end = 960
                ),
                ClassTime(
                    buildingCode = "501",
                    buildingName = "(501) 온라인강의실",
                    roomName = "비대면강의(시험장소 별도)",
                    day = DayType.THU,
                    begin = 870,
                    end = 960
                )
            ),
            examTimes = emptyList()
        ),
        Lecture(
            id = 1908696,
            courseId = 749,
            code = "CS.30200",
            classNo = "A",
            name = "프로그래밍언어",
            subtitle = "프로그래밍언어",
            department = Department(
                id = 9945,
                name = "전산학부",
            ),
            isEnglish = true,
            credit = 3,
            creditAu = 0,
            capacity = 260,
            numberOfPeople = 195,
            grade = 12.3725250982223,
            load = 11.04803889115866,
            speech = 13.15433246923384,
            type = LectureType.MR,
            professors = listOf(
                Professor(
                    id = 1652,
                    name = "류석영",
                )
            ),
            classTimes = listOf(
                ClassTime(
                    buildingCode = "E3-1",
                    buildingName = "(E3-1) 전산학부동",
                    roomName = "(1501호)강의실-제1공동강의실",
                    day = DayType.MON,
                    begin = 870,
                    end = 960
                ),
                ClassTime(
                    buildingCode = "E3-1",
                    buildingName = "(E3-1) 전산학부동",
                    roomName = "(1501호)강의실-제1공동강의실",
                    day = DayType.WED,
                    begin = 870,
                    end = 960
                )
            ),
            examTimes = emptyList()
        ),

        Lecture(
            id = 1908593,
            courseId = 1984,
            code = "CS.50200",
            classNo = "A",
            name = "프로그래밍언어이론",
            subtitle = "프로그래밍언어이론",
            department = Department(
                id = 9945,
                name = "전산학부",
            ),
            isEnglish = true,
            credit = 3,
            creditAu = 0,
            capacity = 30,
            numberOfPeople = 20,
            grade = 0.000001,
            load = 0.000001,
            speech = 0.000001,
            type = LectureType.ETC,
            professors = listOf(
                Professor(
                    id = 2092,
                    name = "양홍석",
                )
            ),
            classTimes = listOf(
                ClassTime(
                    buildingCode = "E3-1",
                    buildingName = "(E3-1) 전산학부동",
                    roomName = "(2445호)강의실-4강의실",
                    day = DayType.TUE,
                    begin = 870,
                    end = 960
                ),
                ClassTime(
                    buildingCode = "E3-1",
                    buildingName = "(E3-1) 전산학부동",
                    roomName = "(2445호)강의실-4강의실",
                    day = DayType.THU,
                    begin = 870,
                    end = 960
                )
            ),
            examTimes = emptyList()
        ),

        Lecture(
            id = 1908548,
            courseId = 24180,
            code = "EB.50002",
            classNo = "A",
            name = "공학생물학 프로그래밍",
            subtitle = "공학생물학 프로그래밍",
            department = Department(
                id = 22265,
                name = "공학생물학대학원",
            ),
            isEnglish = true,
            credit = 3,
            creditAu = 0,
            capacity = 60,
            numberOfPeople = 10,
            grade = 0.000001,
            load = 0.000001,
            speech = 0.000001,
            type = LectureType.MR,
            professors = listOf(
                Professor(
                    id = 75186,
                    name = "김하성",
                ),
                Professor(
                    id = 2140,
                    name = "김현욱",
                ),
                Professor(
                    id = 2332,
                    name = "이영석",
                )
            ),
            classTimes = listOf(
                ClassTime(
                    buildingCode = "E11",
                    buildingName = "(E11) 창의학습관",
                    roomName = "(403호)강의실",
                    day = DayType.MON,
                    begin = 870,
                    end = 960
                ),
                ClassTime(
                    buildingCode = "E11",
                    buildingName = "(E11) 창의학습관",
                    roomName = "(403호)강의실",
                    day = DayType.WED,
                    begin = 870,
                    end = 960
                )
            ),
            examTimes = emptyList()
        ),
        Lecture(
            id = 1908600,
            courseId = 809,
            code = "EE.20009",
            classNo = "A",
            name = "프로그래밍 및 컴퓨터시스템 개론",
            subtitle = "프로그래밍 및 컴퓨터시스템 개론",
            department = Department(
                id = 9947,
                name = "전기및전자공학부",
            ),
            isEnglish = true,
            credit = 3,
            creditAu = 0,
            capacity = 0,
            numberOfPeople = 169,
            grade = 14.38955367736754,
            load = 9.93522217051653,
            speech = 13.65590001299735,
            type = LectureType.MR,
            professors = listOf(
                Professor(
                    id = 1912,
                    name = "한동수",
                )
            ),
            classTimes = listOf(
                ClassTime(
                    buildingCode = "N1",
                    buildingName = "(N1) N1",
                    roomName = "(117호) 다목적홀",
                    day = DayType.MON,
                    begin = 870,
                    end = 960
                ),
                ClassTime(
                    buildingCode = "N1",
                    buildingName = "(N1) N1",
                    roomName = "(117호) 다목적홀",
                    day = DayType.WED,
                    begin = 870,
                    end = 960
                )
            ),
            examTimes = emptyList()
        ),
        Lecture(
            id = 1909135,
            courseId = 23635,
            code = "EE.30009",
            classNo = "A",
            name = "전기공학을 위한 고급 프로그래밍 기술",
            subtitle = "전기공학을 위한 고급 프로그래밍 기술",
            department = Department(
                id = 9947,
                name = "전기및전자공학부",
            ),
            isEnglish = true,
            credit = 3,
            creditAu = 0,
            capacity = 30,
            numberOfPeople = 26,
            grade = 0.000001,
            load = 0.000001,
            speech = 0.000001,
            type = LectureType.ME,
            professors = listOf(
                Professor(
                    id = 2342,
                    name = "윤인수",
                )
            ),
            classTimes = listOf(
                ClassTime(
                    buildingCode = "N1",
                    buildingName = "(N1) N1",
                    roomName = "(111호)강의실-강의실5",
                    day = DayType.TUE,
                    begin = 540,
                    end = 630
                ),
                ClassTime(
                    buildingCode = "N1",
                    buildingName = "(N1) N1",
                    roomName = "(111호)강의실-강의실5",
                    day = DayType.THU,
                    begin = 540,
                    end = 630
                )
            ),
            examTimes = emptyList()
        ),

        Lecture(
            id = 1908479,
            courseId = 824,
            code = "EE.40015",
            classNo = "A",
            name = "전자공학을 위한 운영체제 및 시스템 프로그래밍",
            subtitle = "전자공학을 위한 운영체제 및 시스템 프로그래밍",
            department = Department(
                id = 9947,
                name = "전기및전자공학부",
            ),
            isEnglish = true,
            credit = 3,
            creditAu = 0,
            capacity = 40,
            numberOfPeople = 22,
            grade = 0.000001,
            load = 0.000001,
            speech = 0.000001,
            type = LectureType.ME,
            professors = listOf(
                Professor(
                    id = 2214,
                    name = "원유집",
                )
            ),
            classTimes = listOf(
                ClassTime(
                    buildingCode = "N1",
                    buildingName = "(N1) N1",
                    roomName = "(102호)강의실-대강의실",
                    day = DayType.MON,
                    begin = 870,
                    end = 960
                ),
                ClassTime(
                    buildingCode = "N1",
                    buildingName = "(N1) N1",
                    roomName = "(102호)강의실-대강의실",
                    day = DayType.WED,
                    begin = 870,
                    end = 960
                )
            ),
            examTimes = emptyList()
        ),
        Lecture(
            id = 1908973,
            courseId = 23998,
            code = "HSS.60011",
            classNo = "A",
            name = "인문사회과학연구를 위한 프로그래밍",
            subtitle = "인문사회과학연구를 위한 프로그래밍",
            department = Department(
                id = 20686,
                name = "디지털인문사회과학부",
            ),
            isEnglish = true,
            credit = 3,
            creditAu = 0,
            capacity = 15,
            numberOfPeople = 8,
            grade = 0.000001,
            load = 0.000001,
            speech = 0.000001,
            type = LectureType.MR,
            professors = listOf(
                Professor(
                    id = 2531,
                    name = "김태균",
                )
            ),
            classTimes = listOf(
                ClassTime(
                    buildingCode = "N4",
                    buildingName = "(N4) N4",
                    roomName = "(1309호)강의실-대학원생 세미나실",
                    day = DayType.TUE,
                    begin = 630,
                    end = 720
                ),
                ClassTime(
                    buildingCode = "N4",
                    buildingName = "(N4) N4",
                    roomName = "(1309호)강의실-대학원생 세미나실",
                    day = DayType.THU,
                    begin = 630,
                    end = 720
                )
            ),
            examTimes = emptyList()
        ),

        Lecture(
            id = 1910137,
            courseId = 23812,
            code = "ME.40055",
            classNo = "A",
            name = "자율모바일시스템 프로그래밍",
            subtitle = "자율모바일시스템 프로그래밍",
            department = Department(
                id = 9942,
                name = "기계공학과",
            ),
            isEnglish = true,
            credit = 3,
            creditAu = 0,
            capacity = 20,
            numberOfPeople = 13,
            grade = 0.000001,
            load = 0.000001,
            speech = 0.000001,
            type = LectureType.ME,
            professors = listOf(
                Professor(
                    id = 2141,
                    name = "윤국진",
                )
            ),
            classTimes = listOf(
                ClassTime(
                    buildingCode = "N7-4",
                    buildingName = "(N7-4) N7-4",
                    roomName = "(4102호)강의실-Edu 3.0 강의실",
                    day = DayType.MON,
                    begin = 960,
                    end = 1080
                ),
                ClassTime(
                    buildingCode = "N7-4",
                    buildingName = "(N7-4) N7-4",
                    roomName = "(4102호)강의실-Edu 3.0 강의실",
                    day = DayType.WED,
                    begin = 960,
                    end = 1140
                )
            ),
            examTimes = emptyList()
        )
    )
}