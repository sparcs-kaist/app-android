package com.sparcs.soap.Shared.Mocks

import com.sparcs.soap.Domain.Enums.OTL.DayType
import com.sparcs.soap.Domain.Enums.OTL.LectureType
import com.sparcs.soap.Domain.Enums.OTL.SemesterType
import com.sparcs.soap.Domain.Helpers.LocalizedString
import com.sparcs.soap.Domain.Models.OTL.ClassTime
import com.sparcs.soap.Domain.Models.OTL.Department
import com.sparcs.soap.Domain.Models.OTL.Lecture
import com.sparcs.soap.Domain.Models.OTL.Professor

fun Lecture.Companion.mock(): Lecture {
    return Lecture(
        id = 1916768,
        course = 23374,
        code = "AI.50400",
        section = null,
        year = 2025,
        semester = SemesterType.AUTUMN,
        title = LocalizedString(mapOf("ko" to "인공지능을 위한 프로그래밍", "en" to "Programming for AI")),
        department = Department(
            id = 19525,
            name = LocalizedString(mapOf("ko" to "김재철AI대학원", "en" to "Kim Jaechul Graduate School of AI")),
            code = "AI"
        ),
        isEnglish = true,
        credit = 3,
        creditAu = 0,
        capacity = 0,
        numberOfPeople = 140,
        grade = 15.000001,
        load = 14.64670425922474,
        speech = 15.000001,
        reviewTotalWeight = 38.937776244449054,
        type = LectureType.ETC,
        typeDetail = LocalizedString(mapOf("ko" to "선택(석/박사)", "en" to "Elective(Graduate)")),
        professors = listOf(
            Professor(
                id = 2281,
                name = LocalizedString(mapOf("ko" to "최윤재", "en" to "Choi  Yoonjae")),
                reviewTotalWeight = 45.46003874444905
            )
        ),
        classTimes = listOf(
            ClassTime(
                buildingCode = "0",
                classroomName = LocalizedString(
                    mapOf(
                        "ko" to "(0) 서울캠퍼스기타(기타) 성남 킨스타워 (18F Lecture Room)",
                        "en" to "(0) 서울캠퍼스기타(기타) 성남 킨스타워 (18F Lecture Room)"
                    )
                ),
                classroomNameShort = LocalizedString(
                    mapOf(
                        "ko" to "(0) 성남 킨스타워 (18F Lecture Room)",
                        "en" to "(0) 성남 킨스타워 (18F Lecture Room)"
                    )
                ),
                roomName = "성남 킨스타워 (18F Lecture Room)",
                day = DayType.TUE,
                begin = 630,
                end = 720
            ),
            ClassTime(
                buildingCode = "0",
                classroomName = LocalizedString(
                    mapOf(
                        "ko" to "(0) 서울캠퍼스기타(기타) 성남 킨스타워 (18F Lecture Room)",
                        "en" to "(0) 서울캠퍼스기타(기타) 성남 킨스타워 (18F Lecture Room)"
                    )
                ),
                classroomNameShort = LocalizedString(
                    mapOf(
                        "ko" to "(0) 성남 킨스타워 (18F Lecture Room)",
                        "en" to "(0) 성남 킨스타워 (18F Lecture Room)"
                    )
                ),
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
            course = 23374,
            code = "AI.50400",
            section = null,
            year = 2025,
            semester = SemesterType.AUTUMN,
            title = LocalizedString(mapOf("ko" to "인공지능을 위한 프로그래밍", "en" to "Programming for AI")),
            department = Department(
                id = 19525,
                name = LocalizedString(mapOf("ko" to "김재철AI대학원", "en" to "Kim Jaechul Graduate School of AI")),
                code = "AI"
            ),
            isEnglish = true,
            credit = 3,
            creditAu = 0,
            capacity = 0,
            numberOfPeople = 140,
            grade = 15.000001,
            load = 14.64670425922474,
            speech = 15.000001,
            reviewTotalWeight = 38.937776244449054,
            type = LectureType.ETC,
            typeDetail = LocalizedString(mapOf("ko" to "선택(석/박사)", "en" to "Elective(Graduate)")),
            professors = listOf(
                Professor(
                    id = 2281,
                    name = LocalizedString(mapOf("ko" to "최윤재", "en" to "Choi  Yoonjae")),
                    reviewTotalWeight = 45.46003874444905
                )
            ),
            classTimes = listOf(
                ClassTime(
                    buildingCode = "0",
                    classroomName = LocalizedString(
                        mapOf(
                            "ko" to "(0) 서울캠퍼스기타(기타) 성남 킨스타워 (18F Lecture Room)",
                            "en" to "(0) 서울캠퍼스기타(기타) 성남 킨스타워 (18F Lecture Room)"
                        )
                    ),
                    classroomNameShort = LocalizedString(
                        mapOf(
                            "ko" to "(0) 성남 킨스타워 (18F Lecture Room)",
                            "en" to "(0) 성남 킨스타워 (18F Lecture Room)"
                        )
                    ),
                    roomName = "성남 킨스타워 (18F Lecture Room)",
                    day = DayType.TUE,
                    begin = 630,
                    end = 720
                ),
                ClassTime(
                    buildingCode = "0",
                    classroomName = LocalizedString(
                        mapOf(
                            "ko" to "(0) 서울캠퍼스기타(기타) 성남 킨스타워 (18F Lecture Room)",
                            "en" to "(0) 서울캠퍼스기타(기타) 성남 킨스타워 (18F Lecture Room)"
                        )
                    ),
                    classroomNameShort = LocalizedString(
                        mapOf(
                            "ko" to "(0) 성남 킨스타워 (18F Lecture Room)",
                            "en" to "(0) 성남 킨스타워 (18F Lecture Room)"
                        )
                    ),
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
            course = 744,
            code = "CS.10001",
            section = "A",
            year = 2025,
            semester = SemesterType.AUTUMN,
            title = LocalizedString(mapOf("ko" to "프로그래밍기초", "en" to "Introduction to Programming")),
            department = Department(
                id = 9945,
                name = LocalizedString(mapOf("ko" to "전산학부", "en" to "School of Computing")),
                code = "CS"
            ),
            isEnglish = true,
            credit = 3,
            creditAu = 0,
            capacity = 48,
            numberOfPeople = 53,
            grade = 12.74118225329123,
            load = 11.99264633489851,
            speech = 13.873856226664769,
            reviewTotalWeight = 25.14024708177435,
            type = LectureType.BR,
            typeDetail = LocalizedString(mapOf("ko" to "기초필수", "en" to "Basic Required")),
            professors = listOf(
                Professor(
                    id = 1541,
                    name = LocalizedString(mapOf("ko" to "고인영", "en" to "Ko, In-Young")),
                    reviewTotalWeight = 70.72051054315308
                )
            ),
            classTimes = listOf(
                ClassTime(
                    buildingCode = "E11",
                    classroomName = LocalizedString(
                        mapOf(
                            "ko" to "(E11) 창의학습관 (304호)강의실",
                            "en" to "(E11) Creative Learning B/D (304호)강의실"
                        )
                    ),
                    classroomNameShort = LocalizedString(
                        mapOf(
                            "ko" to "(E11) (304호)강의실",
                            "en" to "(E11) (304호)강의실"
                        )
                    ),
                    roomName = "(304호)강의실",
                    day = DayType.MON,
                    begin = 630,
                    end = 750
                ),
                ClassTime(
                    buildingCode = "E11",
                    classroomName = LocalizedString(
                        mapOf(
                            "ko" to "(E11) 창의학습관 (307호)강의실",
                            "en" to "(E11) Creative Learning B/D (307호)강의실"
                        )
                    ),
                    classroomNameShort = LocalizedString(
                        mapOf(
                            "ko" to "(E11) (307호)강의실",
                            "en" to "(E11) (307호)강의실"
                        )
                    ),
                    roomName = "(307호)강의실",
                    day = DayType.MON,
                    begin = 780,
                    end = 960
                )
            ),
            examTimes = emptyList()
        ),
        Lecture(
            id = 1908725,
            course = 744,
            code = "CS.10001",
            section = "B",
            year = 2025,
            semester = SemesterType.AUTUMN,
            title = LocalizedString(mapOf("ko" to "프로그래밍기초", "en" to "Introduction to Programming")),
            department = Department(
                id = 9945,
                name = LocalizedString(mapOf("ko" to "전산학부", "en" to "School of Computing")),
                code = "CS"
            ),
            isEnglish = true,
            credit = 3,
            creditAu = 0,
            capacity = 48,
            numberOfPeople = 30,
            grade = 12.74118225329123,
            load = 11.99264633489851,
            speech = 13.873856226664769,
            reviewTotalWeight = 25.14024708177435,
            type = LectureType.BR,
            typeDetail = LocalizedString(mapOf("ko" to "기초필수", "en" to "Basic Required")),
            professors = listOf(
                Professor(
                    id = 1541,
                    name = LocalizedString(mapOf("ko" to "고인영", "en" to "Ko, In-Young")),
                    reviewTotalWeight = 70.72051054315308
                )
            ),
            classTimes = listOf(
                ClassTime(
                    buildingCode = "E11",
                    classroomName = LocalizedString(mapOf("ko" to "(E11) 창의학습관 (308호)강의실", "en" to "(E11) Creative Learning B/D (308호)강의실")),
                    classroomNameShort = LocalizedString(mapOf("ko" to "(E11) (308호)강의실", "en" to "(E11) (308호)강의실")),
                    roomName = "(308호)강의실",
                    day = DayType.MON,
                    begin = 780,
                    end = 960
                ),
                ClassTime(
                    buildingCode = "E11",
                    classroomName = LocalizedString(mapOf("ko" to "(E11) 창의학습관 (304호)강의실", "en" to "(E11) Creative Learning B/D (304호)강의실")),
                    classroomNameShort = LocalizedString(mapOf("ko" to "(E11) (304호)강의실", "en" to "(E11) (304호)강의실")),
                    roomName = "(304호)강의실",
                    day = DayType.MON,
                    begin = 630,
                    end = 750
                )
            ),
            examTimes = emptyList()
        ),

        Lecture(
            id = 1908726,
            course = 744,
            code = "CS.10001",
            section = "C",
            year = 2025,
            semester = SemesterType.AUTUMN,
            title = LocalizedString(mapOf("ko" to "프로그래밍기초", "en" to "Introduction to Programming")),
            department = Department(
                id = 9945,
                name = LocalizedString(mapOf("ko" to "전산학부", "en" to "School of Computing")),
                code = "CS"
            ),
            isEnglish = true,
            credit = 3,
            creditAu = 0,
            capacity = 48,
            numberOfPeople = 61,
            grade = 12.74118225329123,
            load = 11.99264633489851,
            speech = 13.873856226664769,
            reviewTotalWeight = 25.14024708177435,
            type = LectureType.BR,
            typeDetail = LocalizedString(mapOf("ko" to "기초필수", "en" to "Basic Required")),
            professors = listOf(
                Professor(
                    id = 1541,
                    name = LocalizedString(mapOf("ko" to "고인영", "en" to "Ko, In-Young")),
                    reviewTotalWeight = 70.72051054315308
                )
            ),
            classTimes = listOf(
                ClassTime(
                    buildingCode = "E11",
                    classroomName = LocalizedString(mapOf("ko" to "(E11) 창의학습관 (304호)강의실", "en" to "(E11) Creative Learning B/D (304호)강의실")),
                    classroomNameShort = LocalizedString(mapOf("ko" to "(E11) (304호)강의실", "en" to "(E11) (304호)강의실")),
                    roomName = "(304호)강의실",
                    day = DayType.MON,
                    begin = 630,
                    end = 750
                ),
                ClassTime(
                    buildingCode = "E11",
                    classroomName = LocalizedString(mapOf("ko" to "(E11) 창의학습관 (307호)강의실", "en" to "(E11) Creative Learning B/D (307호)강의실")),
                    classroomNameShort = LocalizedString(mapOf("ko" to "(E11) (307호)강의실", "en" to "(E11) (307호)강의실")),
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
            course = 744,
            code = "CS.10001",
            section = "D",
            year = 2025,
            semester = SemesterType.AUTUMN,
            title = LocalizedString(mapOf("ko" to "프로그래밍기초", "en" to "Introduction to Programming")),
            department = Department(
                id = 9945,
                name = LocalizedString(mapOf("ko" to "전산학부", "en" to "School of Computing")),
                code = "CS"
            ),
            isEnglish = true,
            credit = 3,
            creditAu = 0,
            capacity = 48,
            numberOfPeople = 39,
            grade = 12.74118225329123,
            load = 11.99264633489851,
            speech = 13.873856226664769,
            reviewTotalWeight = 25.14024708177435,
            type = LectureType.BR,
            typeDetail = LocalizedString(mapOf("ko" to "기초필수", "en" to "Basic Required")),
            professors = listOf(
                Professor(
                    id = 1541,
                    name = LocalizedString(mapOf("ko" to "고인영", "en" to "Ko, In-Young")),
                    reviewTotalWeight = 70.72051054315308
                )
            ),
            classTimes = listOf(
                ClassTime(
                    buildingCode = "E11",
                    classroomName = LocalizedString(mapOf("ko" to "(E11) 창의학습관 (304호)강의실", "en" to "(E11) Creative Learning B/D (304호)강의실")),
                    classroomNameShort = LocalizedString(mapOf("ko" to "(E11) (304호)강의실", "en" to "(E11) (304호)강의실")),
                    roomName = "(304호)강의실",
                    day = DayType.MON,
                    begin = 630,
                    end = 750
                ),
                ClassTime(
                    buildingCode = "E11",
                    classroomName = LocalizedString(mapOf("ko" to "(E11) 창의학습관 (308호)강의실", "en" to "(E11) Creative Learning B/D (308호)강의실")),
                    classroomNameShort = LocalizedString(mapOf("ko" to "(E11) (308호)강의실", "en" to "(E11) (308호)강의실")),
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
            course = 744,
            code = "CS.10001",
            section = "E",
            year = 2025,
            semester = SemesterType.AUTUMN,
            title = LocalizedString(mapOf("ko" to "프로그래밍기초", "en" to "Introduction to Programming")),
            department = Department(
                id = 9945,
                name = LocalizedString(mapOf("ko" to "전산학부", "en" to "School of Computing")),
                code = "CS"
            ),
            isEnglish = true,
            credit = 3,
            creditAu = 0,
            capacity = 48,
            numberOfPeople = 47,
            grade = 12.74118225329123,
            load = 11.99264633489851,
            speech = 13.873856226664769,
            reviewTotalWeight = 25.14024708177435,
            type = LectureType.BR,
            typeDetail = LocalizedString(mapOf("ko" to "기초필수", "en" to "Basic Required")),
            professors = listOf(
                Professor(
                    id = 1541,
                    name = LocalizedString(mapOf("ko" to "고인영", "en" to "Ko, In-Young")),
                    reviewTotalWeight = 70.72051054315308
                )
            ),
            classTimes = listOf(
                ClassTime(
                    buildingCode = "E11",
                    classroomName = LocalizedString(mapOf("ko" to "(E11) 창의학습관 (304호)강의실", "en" to "(E11) Creative Learning B/D (304호)강의실")),
                    classroomNameShort = LocalizedString(mapOf("ko" to "(E11) (304호)강의실", "en" to "(E11) (304호)강의실")),
                    roomName = "(304호)강의실",
                    day = DayType.MON,
                    begin = 630,
                    end = 750
                ),
                ClassTime(
                    buildingCode = "E11",
                    classroomName = LocalizedString(mapOf("ko" to "(E11) 창의학습관 (307호)강의실", "en" to "(E11) Creative Learning B/D (307호)강의실")),
                    classroomNameShort = LocalizedString(mapOf("ko" to "(E11) (307호)강의실", "en" to "(E11) (307호)강의실")),
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
            course = 744,
            code = "CS.10001",
            section = "F",
            year = 2025,
            semester = SemesterType.AUTUMN,
            title = LocalizedString(mapOf("ko" to "프로그래밍기초", "en" to "Introduction to Programming")),
            department = Department(
                id = 9945,
                name = LocalizedString(mapOf("ko" to "전산학부", "en" to "School of Computing")),
                code = "CS"
            ),
            isEnglish = true,
            credit = 3,
            creditAu = 0,
            capacity = 48,
            numberOfPeople = 24,
            grade = 12.2092786938052,
            load = 10.821787294177309,
            speech = 10.887041589757208,
            reviewTotalWeight = 153.6584538144532,
            type = LectureType.BR,
            typeDetail = LocalizedString(mapOf("ko" to "기초필수", "en" to "Basic Required")),
            professors = listOf(
                Professor(
                    id = 1544,
                    name = LocalizedString(mapOf("ko" to "백종문", "en" to "Baik, Jongmoon")),
                    reviewTotalWeight = 166.1578013219865
                )
            ),
            classTimes = listOf(
                ClassTime(
                    buildingCode = "E11",
                    classroomName = LocalizedString(mapOf("ko" to "(E11) 창의학습관 (104호)강의실-터만홀", "en" to "(E11) Creative Learning B/D (104호)강의실-터만홀")),
                    classroomNameShort = LocalizedString(mapOf("ko" to "(E11) (104호)강의실-터만홀", "en" to "(E11) (104호)강의실-터만홀")),
                    roomName = "(104호)강의실-터만홀",
                    day = DayType.MON,
                    begin = 630,
                    end = 750
                ),
                ClassTime(
                    buildingCode = "E11",
                    classroomName = LocalizedString(mapOf("ko" to "(E11) 창의학습관 (308호)강의실", "en" to "(E11) Creative Learning B/D (308호)강의실")),
                    classroomNameShort = LocalizedString(mapOf("ko" to "(E11) (308호)강의실", "en" to "(E11) (308호)강의실")),
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
            course = 744,
            code = "CS.10001",
            section = "G",
            year = 2025,
            semester = SemesterType.AUTUMN,
            title = LocalizedString(mapOf("ko" to "프로그래밍기초", "en" to "Introduction to Programming")),
            department = Department(
                id = 9945,
                name = LocalizedString(mapOf("ko" to "전산학부", "en" to "School of Computing")),
                code = "CS"
            ),
            isEnglish = true,
            credit = 3,
            creditAu = 0,
            capacity = 48,
            numberOfPeople = 49,
            grade = 12.2092786938052,
            load = 10.821787294177309,
            speech = 10.887041589757208,
            reviewTotalWeight = 153.6584538144532,
            type = LectureType.BR,
            typeDetail = LocalizedString(mapOf("ko" to "기초필수", "en" to "Basic Required")),
            professors = listOf(
                Professor(
                    id = 1544,
                    name = LocalizedString(mapOf("ko" to "백종문", "en" to "Baik, Jongmoon")),
                    reviewTotalWeight = 166.1578013219865
                )
            ),
            classTimes = listOf(
                ClassTime(
                    buildingCode = "E11",
                    classroomName = LocalizedString(mapOf("ko" to "(E11) 창의학습관 (104호)강의실-터만홀", "en" to "(E11) Creative Learning B/D (104호)강의실-터만홀")),
                    classroomNameShort = LocalizedString(mapOf("ko" to "(E11) (104호)강의실-터만홀", "en" to "(E11) (104호)강의실-터만홀")),
                    roomName = "(104호)강의실-터만홀",
                    day = DayType.MON,
                    begin = 630,
                    end = 750
                ),
                ClassTime(
                    buildingCode = "E11",
                    classroomName = LocalizedString(mapOf("ko" to "(E11) 창의학습관 (307호)강의실", "en" to "(E11) Creative Learning B/D (307호)강의실")),
                    classroomNameShort = LocalizedString(mapOf("ko" to "(E11) (307호)강의실", "en" to "(E11) (307호)강의실")),
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
            course = 744,
            code = "CS.10001",
            section = "H",
            year = 2025,
            semester = SemesterType.AUTUMN,
            title = LocalizedString(mapOf("ko" to "프로그래밍기초", "en" to "Introduction to Programming")),
            department = Department(
                id = 9945,
                name = LocalizedString(mapOf("ko" to "전산학부", "en" to "School of Computing")),
                code = "CS"
            ),
            isEnglish = true,
            credit = 3,
            creditAu = 0,
            capacity = 48,
            numberOfPeople = 37,
            grade = 12.2092786938052,
            load = 10.821787294177309,
            speech = 10.887041589757208,
            reviewTotalWeight = 153.6584538144532,
            type = LectureType.BR,
            typeDetail = LocalizedString(mapOf("ko" to "기초필수", "en" to "Basic Required")),
            professors = listOf(
                Professor(
                    id = 1544,
                    name = LocalizedString(mapOf("ko" to "백종문", "en" to "Baik, Jongmoon")),
                    reviewTotalWeight = 166.1578013219865
                )
            ),
            classTimes = listOf(
                ClassTime(
                    buildingCode = "E11",
                    classroomName = LocalizedString(mapOf("ko" to "(E11) 창의학습관 (104호)강의실-터만홀", "en" to "(E11) Creative Learning B/D (104호)강의실-터만홀")),
                    classroomNameShort = LocalizedString(mapOf("ko" to "(E11) (104호)강의실-터만홀", "en" to "(E11) (104호)강의실-터만홀")),
                    roomName = "(104호)강의실-터만홀",
                    day = DayType.MON,
                    begin = 630,
                    end = 750
                ),
                ClassTime(
                    buildingCode = "E11",
                    classroomName = LocalizedString(mapOf("ko" to "(E11) 창의학습관 (308호)강의실", "en" to "(E11) Creative Learning B/D (308호)강의실")),
                    classroomNameShort = LocalizedString(mapOf("ko" to "(E11) (308호)강의실", "en" to "(E11) (308호)강의실")),
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
            course = 744,
            code = "CS.10001",
            section = "I",
            year = 2025,
            semester = SemesterType.AUTUMN,
            title = LocalizedString(mapOf("ko" to "프로그래밍기초", "en" to "Introduction to Programming")),
            department = Department(
                id = 9945,
                name = LocalizedString(mapOf("ko" to "전산학부", "en" to "School of Computing")),
                code = "CS"
            ),
            isEnglish = true,
            credit = 3,
            creditAu = 0,
            capacity = 48,
            numberOfPeople = 75,
            grade = 12.2092786938052,
            load = 10.821787294177309,
            speech = 10.887041589757208,
            reviewTotalWeight = 153.6584538144532,
            type = LectureType.BR,
            typeDetail = LocalizedString(mapOf("ko" to "기초필수", "en" to "Basic Required")),
            professors = listOf(
                Professor(
                    id = 1544,
                    name = LocalizedString(mapOf("ko" to "백종문", "en" to "Baik, Jongmoon")),
                    reviewTotalWeight = 166.1578013219865
                )
            ),
            classTimes = listOf(
                ClassTime(
                    buildingCode = "E11",
                    classroomName = LocalizedString(mapOf("ko" to "(E11) 창의학습관 (104호)강의실-터만홀", "en" to "(E11) Creative Learning B/D (104호)강의실-터만홀")),
                    classroomNameShort = LocalizedString(mapOf("ko" to "(E11) (104호)강의실-터만홀", "en" to "(E11) (104호)강의실-터만홀")),
                    roomName = "(104호)강의실-터만홀",
                    day = DayType.MON,
                    begin = 630,
                    end = 750
                ),
                ClassTime(
                    buildingCode = "E11",
                    classroomName = LocalizedString(mapOf("ko" to "(E11) 창의학습관 (307호)강의실", "en" to "(E11) Creative Learning B/D (307호)강의실")),
                    classroomNameShort = LocalizedString(mapOf("ko" to "(E11) (307호)강의실", "en" to "(E11) (307호)강의실")),
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
            course = 744,
            code = "CS.10001",
            section = "J",
            year = 2025,
            semester = SemesterType.AUTUMN,
            title = LocalizedString(mapOf("ko" to "프로그래밍기초", "en" to "Introduction to Programming")),
            department = Department(
                id = 9945,
                name = LocalizedString(mapOf("ko" to "전산학부", "en" to "School of Computing")),
                code = "CS"
            ),
            isEnglish = true,
            credit = 3,
            creditAu = 0,
            capacity = 48,
            numberOfPeople = 66,
            grade = 12.2092786938052,
            load = 10.821787294177309,
            speech = 10.887041589757208,
            reviewTotalWeight = 153.6584538144532,
            type = LectureType.BR,
            typeDetail = LocalizedString(mapOf("ko" to "기초필수", "en" to "Basic Required")),
            professors = listOf(
                Professor(
                    id = 1544,
                    name = LocalizedString(mapOf("ko" to "백종문", "en" to "Baik, Jongmoon")),
                    reviewTotalWeight = 166.1578013219865
                )
            ),
            classTimes = listOf(
                ClassTime(
                    buildingCode = "E11",
                    classroomName = LocalizedString(mapOf("ko" to "(E11) 창의학습관 (104호)강의실-터만홀", "en" to "(E11) Creative Learning B/D (104호)강의실-터만홀")),
                    classroomNameShort = LocalizedString(mapOf("ko" to "(E11) (104호)강의실-터만홀", "en" to "(E11) (104호)강의실-터만홀")),
                    roomName = "(104호)강의실-터만홀",
                    day = DayType.MON,
                    begin = 630,
                    end = 750
                ),
                ClassTime(
                    buildingCode = "E11",
                    classroomName = LocalizedString(mapOf("ko" to "(E11) 창의학습관 (308호)강의실", "en" to "(E11) Creative Learning B/D (308호)강의실")),
                    classroomNameShort = LocalizedString(mapOf("ko" to "(E11) (308호)강의실", "en" to "(E11) (308호)강의실")),
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
            course = 763,
            code = "CS.10009",
            section = null,
            year = 2025,
            semester = SemesterType.AUTUMN,
            title = LocalizedString(mapOf("ko" to "프로그래밍 실습", "en" to "Programming Practice")),
            department = Department(
                id = 9945,
                name = LocalizedString(mapOf("ko" to "전산학부", "en" to "School of Computing")),
                code = "CS"
            ),
            isEnglish = true,
            credit = 3,
            creditAu = 0,
            capacity = 40,
            numberOfPeople = 37,
            grade = 0.000001,
            load = 0.000001,
            speech = 0.000001,
            reviewTotalWeight = 0.000001,
            type = LectureType.BE,
            typeDetail = LocalizedString(mapOf("ko" to "기초선택", "en" to "Basic Elective")),
            professors = listOf(
                Professor(
                    id = 39201,
                    name = LocalizedString(mapOf("ko" to "문은영", "en" to "Moon, Eun Young")),
                    reviewTotalWeight = 395.4059859630009
                )
            ),
            classTimes = listOf(
                ClassTime(
                    buildingCode = "E3-5",
                    classroomName = LocalizedString(mapOf("ko" to "(E3-5) 크래프톤 (210호)대형강의실", "en" to "(E3-5) KRAFTON SoC (210호)대형강의실")),
                    classroomNameShort = LocalizedString(mapOf("ko" to "(E3-5) (210호)대형강의실", "en" to "(E3-5) (210호)대형강의실")),
                    roomName = "(210호)대형강의실",
                    day = DayType.TUE,
                    begin = 630,
                    end = 690
                ),
                ClassTime(
                    buildingCode = "E3-5",
                    classroomName = LocalizedString(mapOf("ko" to "(E3-5) 크래프톤 (210호)대형강의실", "en" to "(E3-5) KRAFTON SoC (210호)대형강의실")),
                    classroomNameShort = LocalizedString(mapOf("ko" to "(E3-5) (210호)대형강의실", "en" to "(E3-5) (210호)대형강의실")),
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
            course = 765,
            code = "CS.20300",
            section = null,
            year = 2025,
            semester = SemesterType.AUTUMN,
            title = LocalizedString(mapOf("ko" to "시스템프로그래밍", "en" to "System Programming")),
            department = Department(
                id = 9945,
                name = LocalizedString(mapOf("ko" to "전산학부", "en" to "School of Computing")),
                code = "CS"
            ),
            isEnglish = true,
            credit = 3,
            creditAu = 0,
            capacity = 0,
            numberOfPeople = 140,
            grade = 13.2942608089639,
            load = 8.842953455343785,
            speech = 14.209425118646509,
            reviewTotalWeight = 90.72896377968296,
            type = LectureType.ME,
            typeDetail = LocalizedString(mapOf("ko" to "전공선택", "en" to "Major Elective")),
            professors = listOf(
                Professor(
                    id = 1474,
                    name = LocalizedString(mapOf("ko" to "허재혁", "en" to "Huh, Jaehyuk")),
                    reviewTotalWeight = 174.7105155798753
                )
            ),
            classTimes = listOf(
                ClassTime(
                    buildingCode = "501",
                    classroomName = LocalizedString(mapOf("ko" to "(501) 본원캠퍼스기타(기타) 비대면강의(시험장소 별도)", "en" to "(501) 본원캠퍼스기타(기타) 비대면강의(시험장소 별도)")),
                    classroomNameShort = LocalizedString(mapOf("ko" to "(501) 비대면강의(시험장소 별도)", "en" to "(501) 비대면강의(시험장소 별도)")),
                    roomName = "비대면강의(시험장소 별도)",
                    day = DayType.TUE,
                    begin = 870,
                    end = 960
                ),
                ClassTime(
                    buildingCode = "501",
                    classroomName = LocalizedString(mapOf("ko" to "(501) 본원캠퍼스기타(기타) 비대면강의(시험장소 별도)", "en" to "(501) 본원캠퍼스기타(기타) 비대면강의(시험장소 별도)")),
                    classroomNameShort = LocalizedString(mapOf("ko" to "(501) 비대면강의(시험장소 별도)", "en" to "(501) 비대면강의(시험장소 별도)")),
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
            course = 749,
            code = "CS.30200",
            section = null,
            year = 2025,
            semester = SemesterType.AUTUMN,
            title = LocalizedString(mapOf("ko" to "프로그래밍언어", "en" to "Programming Language")),
            department = Department(
                id = 9945,
                name = LocalizedString(mapOf("ko" to "전산학부", "en" to "School of Computing")),
                code = "CS"
            ),
            isEnglish = true,
            credit = 3,
            creditAu = 0,
            capacity = 260,
            numberOfPeople = 195,
            grade = 12.3725250982223,
            load = 11.04803889115866,
            speech = 13.15433246923384,
            reviewTotalWeight = 355.919006190106,
            type = LectureType.MR,
            typeDetail = LocalizedString(mapOf("ko" to "전공필수", "en" to "Major Required")),
            professors = listOf(
                Professor(
                    id = 1652,
                    name = LocalizedString(mapOf("ko" to "류석영", "en" to "Ryu, Sukyoung")),
                    reviewTotalWeight = 485.9980204275719
                )
            ),
            classTimes = listOf(
                ClassTime(
                    buildingCode = "E3-1",
                    classroomName = LocalizedString(mapOf("ko" to "(E3-1) 정보전자공학동 (1501호)강의실-제1공동강의실", "en" to "(E3-1) Information & Electronics B/D (1501호)강의실-제1공동강의실")),
                    classroomNameShort = LocalizedString(mapOf("ko" to "(E3-1) (1501호)강의실-제1공동강의실", "en" to "(E3-1) (1501호)강의실-제1공동강의실")),
                    roomName = "(1501호)강의실-제1공동강의실",
                    day = DayType.MON,
                    begin = 870,
                    end = 960
                ),
                ClassTime(
                    buildingCode = "E3-1",
                    classroomName = LocalizedString(mapOf("ko" to "(E3-1) 정보전자공학동 (1501호)강의실-제1공동강의실", "en" to "(E3-1) Information & Electronics B/D (1501호)강의실-제1공동강의실")),
                    classroomNameShort = LocalizedString(mapOf("ko" to "(E3-1) (1501호)강의실-제1공동강의실", "en" to "(E3-1) (1501호)강의실-제1공동강의실")),
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
            course = 1984,
            code = "CS.50200",
            section = null,
            year = 2025,
            semester = SemesterType.AUTUMN,
            title = LocalizedString(mapOf("ko" to "프로그래밍언어이론", "en" to "Theory of Programming Language")),
            department = Department(
                id = 9945,
                name = LocalizedString(mapOf("ko" to "전산학부", "en" to "School of Computing")),
                code = "CS"
            ),
            isEnglish = true,
            credit = 3,
            creditAu = 0,
            capacity = 30,
            numberOfPeople = 20,
            grade = 0.000001,
            load = 0.000001,
            speech = 0.000001,
            reviewTotalWeight = 0.000001,
            type = LectureType.ETC,
            typeDetail = LocalizedString(mapOf("ko" to "선택(석/박사)", "en" to "Elective(Graduate)")),
            professors = listOf(
                Professor(
                    id = 2092,
                    name = LocalizedString(mapOf("ko" to "양홍석", "en" to "YANG, HONGSEOK")),
                    reviewTotalWeight = 49.32777926552636
                )
            ),
            classTimes = listOf(
                ClassTime(
                    buildingCode = "E3-1",
                    classroomName = LocalizedString(mapOf("ko" to "(E3-1) 정보전자공학동 (2445호)강의실-4강의실", "en" to "(E3-1) Information & Electronics B/D (2445호)강의실-4강의실")),
                    classroomNameShort = LocalizedString(mapOf("ko" to "(E3-1) (2445호)강의실-4강의실", "en" to "(E3-1) (2445호)강의실-4강의실")),
                    roomName = "(2445호)강의실-4강의실",
                    day = DayType.TUE,
                    begin = 870,
                    end = 960
                ),
                ClassTime(
                    buildingCode = "E3-1",
                    classroomName = LocalizedString(mapOf("ko" to "(E3-1) 정보전자공학동 (2445호)강의실-4강의실", "en" to "(E3-1) Information & Electronics B/D (2445호)강의실-4강의실")),
                    classroomNameShort = LocalizedString(mapOf("ko" to "(E3-1) (2445호)강의실-4강의실", "en" to "(E3-1) (2445호)강의실-4강의실")),
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
            course = 24180,
            code = "EB.50002",
            section = null,
            year = 2025,
            semester = SemesterType.AUTUMN,
            title = LocalizedString(mapOf("ko" to "공학생물학 프로그래밍", "en" to "Programming for Engineering Biology")),
            department = Department(
                id = 22265,
                name = LocalizedString(mapOf("ko" to "공학생물학대학원", "en" to "Graduate School of  Engineering Biology")),
                code = "EB"
            ),
            isEnglish = true,
            credit = 3,
            creditAu = 0,
            capacity = 60,
            numberOfPeople = 10,
            grade = 0.000001,
            load = 0.000001,
            speech = 0.000001,
            reviewTotalWeight = 0.000001,
            type = LectureType.MR,
            typeDetail = LocalizedString(mapOf("ko" to "전공필수", "en" to "Major Required")),
            professors = listOf(
                Professor(
                    id = 75186,
                    name = LocalizedString(mapOf("ko" to "김하성", "en" to "Kim, Haseong")),
                    reviewTotalWeight = 0.000001
                ),
                Professor(
                    id = 2140,
                    name = LocalizedString(mapOf("ko" to "김현욱", "en" to "Kim, Hyun Uk")),
                    reviewTotalWeight = 19.371546211754342
                ),
                Professor(
                    id = 2332,
                    name = LocalizedString(mapOf("ko" to "이영석", "en" to "Lee, Young-suk")),
                    reviewTotalWeight = 35.682182513265225
                )
            ),
            classTimes = listOf(
                ClassTime(
                    buildingCode = "E11",
                    classroomName = LocalizedString(mapOf("ko" to "(E11) 창의학습관 (403호)강의실", "en" to "(E11) Creative Learning B/D (403호)강의실")),
                    classroomNameShort = LocalizedString(mapOf("ko" to "(E11) (403호)강의실", "en" to "(E11) (403호)강의실")),
                    roomName = "(403호)강의실",
                    day = DayType.MON,
                    begin = 870,
                    end = 960
                ),
                ClassTime(
                    buildingCode = "E11",
                    classroomName = LocalizedString(mapOf("ko" to "(E11) 창의학습관 (403호)강의실", "en" to "(E11) Creative Learning B/D (403호)강의실")),
                    classroomNameShort = LocalizedString(mapOf("ko" to "(E11) (403호)강의실", "en" to "(E11) (403호)강의실")),
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
            course = 809,
            code = "EE.20009",
            section = null,
            year = 2025,
            semester = SemesterType.AUTUMN,
            title = LocalizedString(mapOf("ko" to "프로그래밍 및 컴퓨터시스템 개론", "en" to "Introduction to  Programming and  Computer  Systems")),
            department = Department(
                id = 9947,
                name = LocalizedString(mapOf("ko" to "전기및전자공학부", "en" to "School of Electrical Engineering")),
                code = "EE"
            ),
            isEnglish = true,
            credit = 3,
            creditAu = 0,
            capacity = 0,
            numberOfPeople = 169,
            grade = 14.38955367736754,
            load = 9.93522217051653,
            speech = 13.65590001299735,
            reviewTotalWeight = 32.733127112509706,
            type = LectureType.MR,
            typeDetail = LocalizedString(mapOf("ko" to "전공필수", "en" to "Major Required")),
            professors = listOf(
                Professor(
                    id = 1912,
                    name = LocalizedString(mapOf("ko" to "한동수", "en" to "Dongsu, Han")),
                    reviewTotalWeight = 65.3096223794843
                )
            ),
            classTimes = listOf(
                ClassTime(
                    buildingCode = "N1",
                    classroomName = LocalizedString(mapOf("ko" to "(N1) 김병호김삼열IT융합빌딩 (117호) 다목적홀", "en" to "(N1) Kim Beang-Ho & Kim Sam-Youl ITC Building (117호) 다목적홀")),
                    classroomNameShort = LocalizedString(mapOf("ko" to "(N1) (117호) 다목적홀", "en" to "(N1) (117호) 다목적홀")),
                    roomName = "(117호) 다목적홀",
                    day = DayType.MON,
                    begin = 870,
                    end = 960
                ),
                ClassTime(
                    buildingCode = "N1",
                    classroomName = LocalizedString(mapOf("ko" to "(N1) 김병호김삼열IT융합빌딩 (117호) 다목적홀", "en" to "(N1) Kim Beang-Ho & Kim Sam-Youl ITC Building (117호) 다목적홀")),
                    classroomNameShort = LocalizedString(mapOf("ko" to "(N1) (117호) 다목적홀", "en" to "(N1) (117호) 다목적홀")),
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
            course = 23635,
            code = "EE.30009",
            section = null,
            year = 2025,
            semester = SemesterType.AUTUMN,
            title = LocalizedString(mapOf("ko" to "전기공학을 위한 고급 프로그래밍 기술", "en" to "Advanced Programming Techniques for Electrical Engineering")),
            department = Department(
                id = 9947,
                name = LocalizedString(mapOf("ko" to "전기및전자공학부", "en" to "School of Electrical Engineering")),
                code = "EE"
            ),
            isEnglish = true,
            credit = 3,
            creditAu = 0,
            capacity = 30,
            numberOfPeople = 26,
            grade = 0.000001,
            load = 0.000001,
            speech = 0.000001,
            reviewTotalWeight = 0.000001,
            type = LectureType.ME,
            typeDetail = LocalizedString(mapOf("ko" to "전공선택", "en" to "Major Elective")),
            professors = listOf(
                Professor(
                    id = 2342,
                    name = LocalizedString(mapOf("ko" to "윤인수", "en" to "Yun, Insu")),
                    reviewTotalWeight = 164.5377502585618
                )
            ),
            classTimes = listOf(
                ClassTime(
                    buildingCode = "N1",
                    classroomName = LocalizedString(mapOf("ko" to "(N1) 김병호김삼열IT융합빌딩 (111호)강의실-강의실5", "en" to "(N1) Kim Beang-Ho & Kim Sam-Youl ITC Building (111호)강의실-강의실5")),
                    classroomNameShort = LocalizedString(mapOf("ko" to "(N1) (111호)강의실-강의실5", "en" to "(N1) (111호)강의실-강의실5")),
                    roomName = "(111호)강의실-강의실5",
                    day = DayType.TUE,
                    begin = 540,
                    end = 630
                ),
                ClassTime(
                    buildingCode = "N1",
                    classroomName = LocalizedString(mapOf("ko" to "(N1) 김병호김삼열IT융합빌딩 (111호)강의실-강의실5", "en" to "(N1) Kim Beang-Ho & Kim Sam-Youl ITC Building (111호)강의실-강의실5")),
                    classroomNameShort = LocalizedString(mapOf("ko" to "(N1) (111호)강의실-강의실5", "en" to "(N1) (111호)강의실-강의실5")),
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
            course = 824,
            code = "EE.40015",
            section = null,
            year = 2025,
            semester = SemesterType.AUTUMN,
            title = LocalizedString(mapOf("ko" to "전자공학을 위한 운영체제 및 시스템 프로그래밍", "en" to "Operating Systems and System Programming for Electrical Engineering")),
            department = Department(
                id = 9947,
                name = LocalizedString(mapOf("ko" to "전기및전자공학부", "en" to "School of Electrical Engineering")),
                code = "EE"
            ),
            isEnglish = true,
            credit = 3,
            creditAu = 0,
            capacity = 40,
            numberOfPeople = 22,
            grade = 0.000001,
            load = 0.000001,
            speech = 0.000001,
            reviewTotalWeight = 0.000001,
            type = LectureType.ME,
            typeDetail = LocalizedString(mapOf("ko" to "전공선택", "en" to "Major Elective")),
            professors = listOf(
                Professor(
                    id = 2214,
                    name = LocalizedString(mapOf("ko" to "원유집", "en" to "Won, Youjip")),
                    reviewTotalWeight = 56.49033440973643
                )
            ),
            classTimes = listOf(
                ClassTime(
                    buildingCode = "N1",
                    classroomName = LocalizedString(mapOf("ko" to "(N1) 김병호김삼열IT융합빌딩 (102호)강의실-대강의실", "en" to "(N1) Kim Beang-Ho & Kim Sam-Youl ITC Building (102호)강의실-대강의실")),
                    classroomNameShort = LocalizedString(mapOf("ko" to "(N1) (102호)강의실-대강의실", "en" to "(N1) (102호)강의실-대강의실")),
                    roomName = "(102호)강의실-대강의실",
                    day = DayType.MON,
                    begin = 870,
                    end = 960
                ),
                ClassTime(
                    buildingCode = "N1",
                    classroomName = LocalizedString(mapOf("ko" to "(N1) 김병호김삼열IT융합빌딩 (102호)강의실-대강의실", "en" to "(N1) Kim Beang-Ho & Kim Sam-Youl ITC Building (102호)강의실-대강의실")),
                    classroomNameShort = LocalizedString(mapOf("ko" to "(N1) (102호)강의실-대강의실", "en" to "(N1) (102호)강의실-대강의실")),
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
            course = 23998,
            code = "HSS.60011",
            section = null,
            year = 2025,
            semester = SemesterType.AUTUMN,
            title = LocalizedString(mapOf("ko" to "인문사회과학연구를 위한 프로그래밍", "en" to "Programming for the Humanities and Social Sciences")),
            department = Department(
                id = 20686,
                name = LocalizedString(mapOf("ko" to "디지털인문사회과학부", "en" to "School of Digital Humanities and Computational Social Sciences")),
                code = "HSS"
            ),
            isEnglish = true,
            credit = 3,
            creditAu = 0,
            capacity = 15,
            numberOfPeople = 8,
            grade = 0.000001,
            load = 0.000001,
            speech = 0.000001,
            reviewTotalWeight = 0.000001,
            type = LectureType.MR,
            typeDetail = LocalizedString(mapOf("ko" to "전공필수", "en" to "Major Required")),
            professors = listOf(
                Professor(
                    id = 2531,
                    name = LocalizedString(mapOf("ko" to "김태균", "en" to "Kim, Taegyoon")),
                    reviewTotalWeight = 4.000001
                )
            ),
            classTimes = listOf(
                ClassTime(
                    buildingCode = "N4",
                    classroomName = LocalizedString(mapOf("ko" to "(N4) 인문사회과학부동 (1309호)강의실-대학원생 세미나실", "en" to "(N4) School of Humanities & Social Science Buildiing (1309호)강의실-대학원생 세미나실")),
                    classroomNameShort = LocalizedString(mapOf("ko" to "(N4) (1309호)강의실-대학원생 세미나실", "en" to "(N4) (1309호)강의실-대학원생 세미나실")),
                    roomName = "(1309호)강의실-대학원생 세미나실",
                    day = DayType.TUE,
                    begin = 630,
                    end = 720
                ),
                ClassTime(
                    buildingCode = "N4",
                    classroomName = LocalizedString(mapOf("ko" to "(N4) 인문사회과학부동 (1309호)강의실-대학원생 세미나실", "en" to "(N4) School of Humanities & Social Science Buildiing (1309호)강의실-대학원생 세미나실")),
                    classroomNameShort = LocalizedString(mapOf("ko" to "(N4) (1309호)강의실-대학원생 세미나실", "en" to "(N4) (1309호)강의실-대학원생 세미나실")),
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
            course = 23812,
            code = "ME.40055",
            section = null,
            year = 2025,
            semester = SemesterType.AUTUMN,
            title = LocalizedString(mapOf("ko" to "자율모바일시스템 프로그래밍", "en" to "Autonomous Mobile System Programming")),
            department = Department(
                id = 9942,
                name = LocalizedString(mapOf("ko" to "기계공학과", "en" to "Department of Mechanical Engineering")),
                code = "ME"
            ),
            isEnglish = true,
            credit = 3,
            creditAu = 0,
            capacity = 20,
            numberOfPeople = 13,
            grade = 0.000001,
            load = 0.000001,
            speech = 0.000001,
            reviewTotalWeight = 0.000001,
            type = LectureType.ME,
            typeDetail = LocalizedString(mapOf("ko" to "전공선택", "en" to "Major Elective")),
            professors = listOf(
                Professor(
                    id = 2141,
                    name = LocalizedString(mapOf("ko" to "윤국진", "en" to "Yoon  Kuk-Jin")),
                    reviewTotalWeight = 26.21423802683595
                )
            ),
            classTimes = listOf(
                ClassTime(
                    buildingCode = "N7-4",
                    classroomName = LocalizedString(mapOf("ko" to "(N7-4) 기계공학동 (4102호)강의실-Edu 3.0 강의실", "en" to "(N7-4) Mechanical Engineering B/D (4102호)강의실-Edu 3.0 강의실")),
                    classroomNameShort = LocalizedString(mapOf("ko" to "(N7-4) (4102호)강의실-Edu 3.0 강의실", "en" to "(N7-4) (4102호)강의실-Edu 3.0 강의실")),
                    roomName = "(4102호)강의실-Edu 3.0 강의실",
                    day = DayType.MON,
                    begin = 960,
                    end = 1080
                ),
                ClassTime(
                    buildingCode = "N7-4",
                    classroomName = LocalizedString(mapOf("ko" to "(N7-4) 기계공학동 (4102호)강의실-Edu 3.0 강의실", "en" to "(N7-4) Mechanical Engineering B/D (4102호)강의실-Edu 3.0 강의실")),
                    classroomNameShort = LocalizedString(mapOf("ko" to "(N7-4) (4102호)강의실-Edu 3.0 강의실", "en" to "(N7-4) (4102호)강의실-Edu 3.0 강의실")),
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