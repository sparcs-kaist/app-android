package com.example.soap.Shared.Mocks

import com.example.soap.Domain.Helpers.LocalizedString
import com.example.soap.Domain.Models.Ara.AraBoard
import com.example.soap.Domain.Models.Ara.AraBoardGroup
import com.example.soap.Domain.Models.Ara.AraBoardTopic

fun AraBoard.Companion.mock(): AraBoard{
    return AraBoard(
        id = 1,
        slug = "portal-notice",
        name = LocalizedString(mapOf("ko" to "포탈공지", "en" to "Portal Notice")),
        group = AraBoardGroup(
            id = 1,
            slug = "notice",
            name = LocalizedString(mapOf(
                "ko" to "공지",
                "en" to "Notices"
            ))
        ),
        topics = emptyList(),
        isReadOnly = false,
        userReadable = true,
        userWritable = true
    )
}

fun AraBoard.Companion.mockList(): List<AraBoard> {
    return listOf(
        AraBoard(
            id = 1,
            slug = "portal-notice",
            name = LocalizedString(mapOf("ko" to "포탈공지", "en" to "Portal Notice")),
            group = AraBoardGroup(
                id = 1,
                slug = "notice",
                name = LocalizedString(
                    mapOf(
                        "ko" to "공지",
                        "en" to "Notices"
                    )
                )
            ),
            topics = emptyList(),
            isReadOnly = true,
            userReadable = true,
            userWritable = false
        ),
        AraBoard(
            id = 2,
            slug = "students-group",
            name = LocalizedString(mapOf("ko" to "학생 단체", "en" to "Student Organizations")),
            group = AraBoardGroup(
                id = 3,
                slug = "club",
                name = LocalizedString(
                    mapOf(
                        "ko" to "학생 단체 및 동아리",
                        "en" to "Organizations and Clubs"
                    )
                )
            ),
            topics = listOf(
                AraBoardTopic(
                    24,
                    "grad-assoc",
                    LocalizedString(mapOf("ko" to "원총", "en" to "Grad Assoc"))
                ),
                AraBoardTopic(
                    9,
                    "imageffect",
                    LocalizedString(mapOf("ko" to "상상효과", "en" to "IMAGEFFECT"))
                ),
                AraBoardTopic(8, "times", LocalizedString(mapOf("ko" to "신문사", "en" to "Times"))),
                AraBoardTopic(7, "kcoop", LocalizedString(mapOf("ko" to "협동조합", "en" to "Kcoop"))),
                AraBoardTopic(
                    6,
                    "scspace",
                    LocalizedString(mapOf("ko" to "공간위", "en" to "SCSpace"))
                ),
                AraBoardTopic(
                    5,
                    "freshman-council",
                    LocalizedString(mapOf("ko" to "새학", "en" to "Freshman Council"))
                ),
                AraBoardTopic(
                    4,
                    "welfare-cmte",
                    LocalizedString(mapOf("ko" to "학복위", "en" to "Welfare Cmte"))
                ),
                AraBoardTopic(
                    3,
                    "dorm-council",
                    LocalizedString(mapOf("ko" to "생자회", "en" to "Dorm Council"))
                ),
                AraBoardTopic(
                    2,
                    "clubs-union",
                    LocalizedString(mapOf("ko" to "동연", "en" to "Clubs Union"))
                ),
                AraBoardTopic(
                    1,
                    "undergrad-assoc",
                    LocalizedString(mapOf("ko" to "총학", "en" to "Undergrad Assoc"))
                )
            ),
            isReadOnly = false,
            userReadable = true,
            userWritable = true
        ),
        AraBoard(
            id = 3,
            slug = "wanted",
            name = LocalizedString(mapOf("ko" to "구인구직", "en" to "Jobs & Hiring")),
            group = AraBoardGroup(
                id = 4,
                slug = "trade",
                name = LocalizedString(mapOf("ko" to "거래", "en" to "Marketplace"))
            ),
            topics = listOf(
                AraBoardTopic(
                    id = 19,
                    slug = "carpool",
                    name = LocalizedString(mapOf("ko" to "카풀", "en" to "Carpool"))
                ),
                AraBoardTopic(
                    id = 18,
                    slug = "dorm",
                    name = LocalizedString(mapOf("ko" to "기숙사", "en" to "Dorm"))
                ),
                AraBoardTopic(
                    id = 17,
                    slug = "experiment",
                    name = LocalizedString(mapOf("ko" to "실험", "en" to "Experiment"))
                ),
                AraBoardTopic(
                    id = 16,
                    slug = "job",
                    name = LocalizedString(mapOf("ko" to "채용", "en" to "Job"))
                ),
                AraBoardTopic(
                    id = 15,
                    slug = "intern",
                    name = LocalizedString(mapOf("ko" to "인턴", "en" to "Intern"))
                ),
                AraBoardTopic(
                    id = 14,
                    slug = "tutoring",
                    name = LocalizedString(mapOf("ko" to "과외", "en" to "Tutoring"))
                )
            ),
            isReadOnly = false,
            userReadable = true,
            userWritable = true
        ),

        AraBoard(
            id = 4,
            slug = "market",
            name = LocalizedString(mapOf("ko" to "장터", "en" to "Buy & Sell")),
            group = AraBoardGroup(
                id = 4,
                slug = "trade",
                name = LocalizedString(mapOf("ko" to "거래", "en" to "Marketplace"))
            ),
            topics = listOf(
                AraBoardTopic(
                    id = 22,
                    slug = "sell",
                    name = LocalizedString(mapOf("ko" to "팝니다", "en" to "Sell"))
                ),
                AraBoardTopic(
                    id = 21,
                    slug = "buy",
                    name = LocalizedString(mapOf("ko" to "삽니다", "en" to "Buy"))
                ),
                AraBoardTopic(
                    id = 20,
                    slug = "housing",
                    name = LocalizedString(mapOf("ko" to "부동산", "en" to "Housing"))
                )
            ),
            isReadOnly = false,
            userReadable = true,
            userWritable = true
        ),

        AraBoard(
            id = 5,
            slug = "facility-feedback",
            name = LocalizedString(mapOf("ko" to "입주 업체 피드백", "en" to "Tenant Feedback")),
            group = AraBoardGroup(
                id = 5,
                slug = "communication",
                name = LocalizedString(mapOf("ko" to "소통", "en" to "Communications"))
            ),
            topics = listOf(
                AraBoardTopic(
                    id = 23,
                    slug = "events",
                    name = LocalizedString(mapOf("ko" to "이벤트", "en" to "Event"))
                )
            ),
            isReadOnly = false,
            userReadable = true,
            userWritable = true
        ),

        AraBoard(
            id = 7,
            slug = "talk",
            name = LocalizedString(mapOf("ko" to "자유게시판", "en" to "Talk")),
            group = AraBoardGroup(
                id = 2,
                slug = "talk",
                name = LocalizedString(mapOf("ko" to "자유게시판", "en" to "Talks"))
            ),
            topics = listOf(
                AraBoardTopic(
                    id = 26,
                    slug = "spangs",
                    name = LocalizedString(mapOf("ko" to "스빵스", "en" to "SPANGS"))
                ),
                AraBoardTopic(
                    id = 25,
                    slug = "meal",
                    name = LocalizedString(mapOf("ko" to "식사", "en" to "Meal"))
                ),
                AraBoardTopic(
                    id = 13,
                    slug = "money",
                    name = LocalizedString(mapOf("ko" to "돈", "en" to "Money"))
                ),
                AraBoardTopic(
                    id = 12,
                    slug = "game",
                    name = LocalizedString(mapOf("ko" to "게임", "en" to "Game"))
                ),
                AraBoardTopic(
                    id = 11,
                    slug = "love",
                    name = LocalizedString(mapOf("ko" to "연애", "en" to "Dating"))
                ),
                AraBoardTopic(
                    id = 10,
                    slug = "lostfound",
                    name = LocalizedString(mapOf("ko" to "분실물", "en" to "Lost & Found"))
                )
            ),
            isReadOnly = false,
            userReadable = true,
            userWritable = true
        ),

        AraBoard(
            id = 8,
            slug = "ara-notice",
            name = LocalizedString(mapOf("ko" to "운영진 공지", "en" to "Staff Notice")),
            group = AraBoardGroup(
                id = 1,
                slug = "notice",
                name = LocalizedString(mapOf("ko" to "공지", "en" to "Notices"))
            ),
            topics = emptyList(),
            isReadOnly = true,
            userReadable = true,
            userWritable = false
        ),

        AraBoard(
            id = 11,
            slug = "facility-notice",
            name = LocalizedString(mapOf("ko" to "입주 업체 공지", "en" to "Tenant Notice")),
            group = AraBoardGroup(
                id = 1,
                slug = "notice",
                name = LocalizedString(mapOf("ko" to "공지", "en" to "Notices"))
            ),
            topics = emptyList(),
            isReadOnly = false,
            userReadable = true,
            userWritable = true
        ),

        AraBoard(
            id = 12,
            slug = "club",
            name = LocalizedString(mapOf("ko" to "동아리", "en" to "Club")),
            group = AraBoardGroup(
                id = 3,
                slug = "club",
                name = LocalizedString(
                    mapOf(
                        "ko" to "학생 단체 및 동아리",
                        "en" to "Organizations and Clubs"
                    )
                )
            ),
            topics = emptyList(),
            isReadOnly = false,
            userReadable = true,
            userWritable = true
        ),

        AraBoard(
            id = 13,
            slug = "real-estate",
            name = LocalizedString(mapOf("ko" to "부동산", "en" to "Real Estate")),
            group = AraBoardGroup(
                id = 4,
                slug = "trade",
                name = LocalizedString(mapOf("ko" to "거래", "en" to "Marketplace"))
            ),
            topics = emptyList(),
            isReadOnly = false,
            userReadable = true,
            userWritable = true
        ),

        AraBoard(
            id = 14,
            slug = "with-school",
            name = LocalizedString(mapOf("ko" to "학교에게 전합니다", "en" to "Speak to the School")),
            group = AraBoardGroup(
                id = 5,
                slug = "communication",
                name = LocalizedString(mapOf("ko" to "소통", "en" to "Communications"))
            ),
            topics = emptyList(),
            isReadOnly = false,
            userReadable = true,
            userWritable = true
        ),

        AraBoard(
            id = 10,
            slug = "ara-feedback",
            name = LocalizedString(mapOf("ko" to "아라 피드백", "en" to "Ara Feedback")),
            group = AraBoardGroup(
                id = 5,
                slug = "communication",
                name = LocalizedString(mapOf("ko" to "소통", "en" to "Communications"))
            ),
            topics = emptyList(),
            isReadOnly = false,
            userReadable = true,
            userWritable = true
        ),

        AraBoard(
            id = 17,
            slug = "kaist-news",
            name = LocalizedString(mapOf("ko" to "카이스트 뉴스", "en" to "KAIST News")),
            group = AraBoardGroup(
                id = 5,
                slug = "communication",
                name = LocalizedString(mapOf("ko" to "소통", "en" to "Communications"))
            ),
            topics = emptyList(),
            isReadOnly = false,
            userReadable = true,
            userWritable = false
        ),

        AraBoard(
            id = 18,
            slug = "external-company-advertisement",
            name = LocalizedString(
                mapOf(
                    "ko" to "외부 업체 홍보",
                    "en" to "External Company Advertisement"
                )
            ),
            group = AraBoardGroup(
                id = 1,
                slug = "notice",
                name = LocalizedString(mapOf("ko" to "공지", "en" to "Notices"))
            ),
            topics = emptyList(),
            isReadOnly = false,
            userReadable = true,
            userWritable = false
        )
    )
}