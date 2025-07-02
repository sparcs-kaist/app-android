package com.example.soap.Utilities.Mocks

import com.example.soap.Models.TimeTable.ClassTime
import com.example.soap.Models.TimeTable.ExamTime
import com.example.soap.Models.TimeTable.Lecture
import com.example.soap.Models.TimeTable.Professor
import com.example.soap.Models.Types.DayType
import com.example.soap.Models.Types.LectureType
import com.example.soap.Models.Types.SemesterType
import com.example.soap.Utilities.Helpers.LocalizedString

fun Lecture.Companion.mock(): Lecture {
    return Lecture(
        id = 1884981,
        course = 3423,
        code = "CE.20091",
        section = null,
        year = 2024,
        semester = SemesterType.AUTUMN,
        title = LocalizedString(
            mapOf(
                "ko" to "지리공간 분석 개론",
                "en" to "Introduction to Geospatial Analysis"
            )
        ),
        department = LocalizedString(
            mapOf(
                "ko" to "건설및환경공학과",
                "en" to "Civil and Environmental Engineering"
            )
        ),
        isEnglish = true,
        credit = 3,
        creditAu = 0,
        capacity= 25,
        numberOfPeople= 35,
        grade = 14.63246654364472,
        load = 14.73445735528331,
        speech = 14.054518611026038,
        reviewTotalWeight = 16.325001,
        type = LectureType.ME,
        typeDetail= LocalizedString(
            mapOf(
                "ko" to "전공선택",
                "en" to "Major Elective"
            )
        ),
        professors = listOf(
                Professor(
                    id = 2368,
                    name = LocalizedString(
                        mapOf(
                            "ko" to "한동훈",
                            "en" to "Albert Tonghoon Han"
                        )
                    ),
                    reviewTotalWeight = 59.009410940575314
                )
            ),
        classTimes = listOf(
            ClassTime(
                classroomName = LocalizedString(
                    mapOf(
                        "ko" to "(W1-2) 건설및환경공학과 1211",
                        "en" to "(W1-2) Dept. of Civil & Environmental Engineering 1211"
                    )
                ),
                classroomNameShort = LocalizedString(
                    mapOf("ko" to "(W1-2) 1211",
                        "en" to "(W1-2) 1211")
                ),
                roomName = "1211",
                day = DayType.MON,
                begin = 780,
                end = 870
            ),
            ClassTime(
                classroomName = LocalizedString(
                    mapOf(
                        "ko" to "(W1-2) 건설및환경공학과 1211",
                        "en" to "(W1-2) Dept. of Civil & Environmental Engineering 1211"
                    )
                ),
                classroomNameShort = LocalizedString(
                    mapOf("ko" to "(W1-2) 1211",
                        "en" to "(W1-2) 1211"
                    )
                ),
                roomName = "1211",
                day = DayType.WED,
                begin = 780,
                end = 870
            )
        ),
        examTimes = listOf(
            ExamTime(
                str = LocalizedString(
                    mapOf(
                        "ko" to "월요일 13:00 ~ 15:45",
                        "en" to "Monday 13:00 ~ 15:45"
                    )
                ),
                day = DayType.MON,
                begin = 780,
                end = 945
            )
        )
    )
}



fun Lecture.Companion.mockList(): List<Lecture> {
    return listOf(
        Lecture(
            id = 1882911,
            course = 774,
            code = "CS.20002",
            section = null,
            year = 2024,
            semester = SemesterType.AUTUMN,
            title = LocalizedString(mapOf("ko" to "문제해결기법", "en" to "Problem Solving")),
            department = LocalizedString(mapOf("ko" to "전산학부", "en" to "School of Computing")),
            isEnglish = false,
            credit = 3,
            creditAu = 0,
            capacity = 0,
            numberOfPeople = 225,
            grade = 15.000001,
            load = 13.176537392839839,
            speech = 14.54781488787623,
            reviewTotalWeight = 26.5377465836487,
            type = LectureType.ME,
            typeDetail = LocalizedString(mapOf("ko" to "전공선택", "en" to "Major Elective")),
            professors = listOf(
                Professor(
                    id = 1652,
                    name = LocalizedString(mapOf("ko" to "류석영", "en" to "Sukyoung Ryu")),
                    reviewTotalWeight = 518.8789149987371
                )
            ),
            classTimes = listOf(
                ClassTime(
                    classroomName = LocalizedString(mapOf("ko" to "(E3) 정보전자공학동 1501", "en" to "(E3) Information Science and Electronics Bldg. 1501")),
                    classroomNameShort = LocalizedString(mapOf("ko" to "(E3) 1501", "en" to "(E3) 1501")),
                    roomName = "1501",
                    day = DayType.FRI,
                    begin = 780,
                    end = 870
                ),
                ClassTime(
                    classroomName = LocalizedString(mapOf("ko" to "(E3) 정보전자공학동 1501", "en" to "(E3) Information Science and Electronics Bldg. 1501")),
                    classroomNameShort = LocalizedString(mapOf("ko" to "(E3) 1501", "en" to "(E3) 1501")),
                    roomName = "1501",
                    day = DayType.FRI,
                    begin = 870,
                    end = 960
                )
            ),
            examTimes = emptyList()
            ),
        Lecture(
            id = 1882913,
            course = 745,
            code = "CS.20004",
            section = "A",
            year = 2024,
            semester = SemesterType.AUTUMN,
            title = LocalizedString(mapOf("ko" to "이산구조", "en" to "Discrete Mathematics")),
            department = LocalizedString(mapOf("ko" to "전산학부", "en" to "School of Computing")),
            isEnglish = true,
            credit = 3,
            creditAu = 0,
            capacity = 100,
            numberOfPeople = 158,
            grade = 14.12613402076768,
            load = 14.13128727171274,
            speech = 14.173633461345839,
            reviewTotalWeight = 113.9682314202921,
            type = LectureType.MR,
            typeDetail = LocalizedString(mapOf("ko" to "전공필수", "en" to "Major Required")),
            professors = listOf(
                Professor(
                    id = 1534,
                    name = LocalizedString(mapOf("ko" to "박진아", "en" to "Park Jinah")),
                    reviewTotalWeight = 123.2450553207218
                )
            ),
            classTimes = listOf(
                ClassTime(
                    classroomName = LocalizedString(mapOf("ko" to "(E11) 창의학습관 터만홀", "en" to "(E11) Creative Learning Bldg. 터만홀")),
                    classroomNameShort = LocalizedString(mapOf("ko" to "(E11) 터만홀", "en" to "(E11) 터만홀")),
                    roomName = "터만홀",
                    day = DayType.TUE,
                    begin = 780,
                    end = 870
                ),
                ClassTime(
                    classroomName = LocalizedString(mapOf("ko" to "(E11) 창의학습관 터만홀", "en" to "(E11) Creative Learning Bldg. 터만홀")),
                    classroomNameShort = LocalizedString(mapOf("ko" to "(E11) 터만홀", "en" to "(E11) 터만홀")),
                    roomName = "터만홀",
                    day = DayType.THU,
                    begin = 780,
                    end = 870
                )
            ),
            examTimes = listOf(
                ExamTime(
                    str = LocalizedString(mapOf("ko" to "화요일 13:00 ~ 15:45", "en" to "Tuesday 13:00 ~ 15:45")),
                    day = DayType.TUE,
                    begin = 780,
                    end = 945
                )
            )
        ),
        Lecture(
            id = 1882915,
            course = 746,
            code = "CS.20006",
            section = null,
            year = 2024,
            semester = SemesterType.AUTUMN,
            title = LocalizedString(mapOf("ko" to "데이타구조", "en" to "Data Structure")),
            department = LocalizedString(mapOf("ko" to "전산학부", "en" to "School of Computing")),
            isEnglish = true,
            credit = 3,
            creditAu = 0,
            capacity = 0,
            numberOfPeople = 199,
            grade = 12.75854087583137,
            load = 14.35217200637648,
            speech = 13.98624399640883,
            reviewTotalWeight = 344.9845019592524,
            type = LectureType.MR,
            typeDetail = LocalizedString(mapOf("ko" to "전공필수", "en" to "Major Required")),
            professors = listOf(
                Professor(
                    id = 39201,
                    name = LocalizedString(mapOf("ko" to "문은영", "en" to "Eun Young Moon")),
                    reviewTotalWeight = 397.4670559664702
                )
            ),
            classTimes = listOf(
                ClassTime(
                    classroomName = LocalizedString(mapOf("ko" to "정보 없음", "en" to "Unknown")),
                    classroomNameShort = LocalizedString(mapOf("ko" to "정보 없음", "en" to "Unknown")),
                    roomName = "",
                    day = DayType.MON,
                    begin = 630,
                    end = 720
                ),
                ClassTime(
                    classroomName = LocalizedString(mapOf("ko" to "정보 없음", "en" to "Unknown")),
                    classroomNameShort = LocalizedString(mapOf("ko" to "정보 없음", "en" to "Unknown")),
                    roomName = "",
                    day = DayType.WED,
                    begin = 630,
                    end = 720
                )
            ),
            examTimes = listOf(
                ExamTime(
                    str = LocalizedString(mapOf("ko" to "수요일 09:00 ~ 11:45", "en" to "Wednesday 09:00 ~ 11:45")),
                    day = DayType.WED,
                    begin = 540,
                    end = 705
                )
            )
        ),
        Lecture(
            id = 1882917,
            course = 765,
            code = "CS.20300",
            section = null,
            year = 2024,
            semester = SemesterType.AUTUMN,
            title = LocalizedString(mapOf("ko" to "시스템프로그래밍", "en" to "System Programming")),
            department = LocalizedString(mapOf("ko" to "전산학부", "en" to "School of Computing")),
            isEnglish = true,
            credit = 3,
            creditAu = 0,
            capacity = 0,
            numberOfPeople = 240,
            grade = 13.115049833673599,
            load = 8.975561454273896,
            speech = 13.81878472492449,
            reviewTotalWeight = 119.1822984880496,
            type = LectureType.ME,
            typeDetail = LocalizedString(mapOf("ko" to "전공선택", "en" to "Major Elective")),
            professors = listOf(
                Professor(
                    id = 2268,
                    name = LocalizedString(mapOf("ko" to "박종세", "en" to "Jongse Park")),
                    reviewTotalWeight = 221.731841341158
                )
            ),
            classTimes = listOf(
                ClassTime(
                    classroomName = LocalizedString(mapOf("ko" to "정보 없음", "en" to "Unknown")),
                    classroomNameShort = LocalizedString(mapOf("ko" to "정보 없음", "en" to "Unknown")),
                    roomName = "",
                    day = DayType.TUE,
                    begin = 870,
                    end = 960
                ),
                ClassTime(
                    classroomName = LocalizedString(mapOf("ko" to "정보 없음", "en" to "Unknown")),
                    classroomNameShort = LocalizedString(mapOf("ko" to "정보 없음", "en" to "Unknown")),
                    roomName = "",
                    day = DayType.THU,
                    begin = 870,
                    end = 960
                )
            ),
            examTimes = listOf(
                ExamTime(
                    str = LocalizedString(mapOf("ko" to "목요일 13:00 ~ 15:45", "en" to "Thursday 13:00 ~ 15:45")),
                    day = DayType.THU,
                    begin = 780,
                    end = 945
                )
            )
        ),
        Lecture(
            id = 1884981,
            course = 3423,
            code = "CE.20091",
            section = null,
            year = 2024,
            semester = SemesterType.AUTUMN,
            title = LocalizedString(mapOf("ko" to "지리공간 분석 개론", "en" to "Introduction to Geospatial Analysis")),
            department = LocalizedString(mapOf("ko" to "건설및환경공학과", "en" to "Civil and Environmental Engineering")),
            isEnglish = true,
            credit = 3,
            creditAu = 0,
            capacity = 25,
            numberOfPeople = 35,
            grade = 14.63246654364472,
            load = 14.73445735528331,
            speech = 14.054518611026038,
            reviewTotalWeight = 16.325001,
            type = LectureType.ME,
            typeDetail = LocalizedString(mapOf("ko" to "전공선택", "en" to "Major Elective")),
            professors = listOf(
                Professor(
                    id = 2368,
                    name = LocalizedString(mapOf("ko" to "한동훈", "en" to "Albert Tonghoon Han")),
                    reviewTotalWeight = 59.009410940575314
                )
            ),
            classTimes = listOf(
                ClassTime(
                    classroomName = LocalizedString(mapOf("ko" to "(W1-2) 건설및환경공학과 1211", "en" to "(W1-2) Dept. of Civil & Environmental Engineering 1211")),
                    classroomNameShort = LocalizedString(mapOf("ko" to "(W1-2) 1211", "en" to "(W1-2) 1211")),
                    roomName = "1211",
                    day = DayType.MON,
                    begin = 780,
                    end = 870
                ),
                ClassTime(
                    classroomName = LocalizedString(mapOf("ko" to "(W1-2) 건설및환경공학과 1211", "en" to "(W1-2) Dept. of Civil & Environmental Engineering 1211")),
                    classroomNameShort = LocalizedString(mapOf("ko" to "(W1-2) 1211", "en" to "(W1-2) 1211")),
                    roomName = "1211",
                    day = DayType.WED,
                    begin = 780,
                    end = 870
                )
            ),
            examTimes = listOf(
                ExamTime(
                    str = LocalizedString(mapOf("ko" to "월요일 13:00 ~ 15:45", "en" to "Monday 13:00 ~ 15:45")),
                    day = DayType.MON,
                    begin = 780,
                    end = 945
                )
            )
        )
    )
}
