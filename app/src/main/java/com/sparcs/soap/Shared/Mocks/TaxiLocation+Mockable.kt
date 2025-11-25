package com.sparcs.soap.Shared.Mocks

import com.sparcs.soap.Domain.Helpers.LocalizedString
import com.sparcs.soap.Domain.Models.Taxi.TaxiLocation

fun TaxiLocation.Companion.mockList(): List<TaxiLocation>{
    return listOf(
        TaxiLocation(
            id = "686d4d8f56fd773a8bd9d78e",
            title = LocalizedString(mapOf("en" to "Taxi Stand", "ko" to "택시승강장")),
            priority = 0.0,
            latitude = 36.373199,
            longitude = 127.359507
        ),
        TaxiLocation(
            id = "686d4d8f56fd773a8bd9d790",
            title = LocalizedString(mapOf("en" to "Daejeon Station", "ko" to "대전역")),
            priority = 0.0,
            latitude = 36.331894,
            longitude = 127.434522
        ),
        TaxiLocation(
            id = "686d4d8f56fd773a8bd9d792",
            title = LocalizedString(mapOf("en" to "Galleria Timeworld", "ko" to "갤러리아 타임월드")),
            priority = 0.0,
            latitude = 36.351938,
            longitude = 127.378188
        ),
        TaxiLocation(
            id = "686d4d8f56fd773a8bd9d794",
            title = LocalizedString(mapOf("en" to "Gung-dong Rodeo Street", "ko" to "궁동 로데오거리")),
            priority = 0.0,
            latitude = 36.362785,
            longitude = 127.350161
        ),
        TaxiLocation(
            id = "686d4d8f56fd773a8bd9d796",
            title = LocalizedString(mapOf("en" to "Daejeon Terminal Complex", "ko" to "대전복합터미널")),
            priority = 0.0,
            latitude = 36.349766,
            longitude = 127.43688
        ),
        TaxiLocation(
            id = "686d4d8f56fd773a8bd9d798",
            title = LocalizedString(mapOf("en" to "Mannyon Middle School", "ko" to "만년중학교")),
            priority = 0.0,
            latitude = 36.36699,
            longitude = 127.375993
        ),
        TaxiLocation(
            id = "686d4d8f56fd773a8bd9d79a",
            title = LocalizedString(mapOf("en" to "Seodaejeon Station", "ko" to "서대전역")),
            priority = 0.0,
            latitude = 36.322517,
            longitude = 127.403933
        ),
        TaxiLocation(
            id = "686d4d8f56fd773a8bd9d79c",
            title = LocalizedString(mapOf("en" to "Shinsegae Department Store", "ko" to "신세계백화점")),
            priority = 0.0,
            latitude = 36.375168,
            longitude = 127.381905
        ),
        TaxiLocation(
            id = "686d4d8f56fd773a8bd9d79e",
            title = LocalizedString(mapOf("en" to "Duck Pond", "ko" to "오리연못")),
            priority = 0.0,
            latitude = 36.367715,
            longitude = 127.362371
        ),
        TaxiLocation(
            id = "686d4d8f56fd773a8bd9d7a0",
            title =LocalizedString( mapOf("en" to "Wolpyeong Station", "ko" to "월평역")),
            priority = 0.0,
            latitude = 36.358271,
            longitude = 127.364352
        ),
        TaxiLocation(
            id = "686d4d8f56fd773a8bd9d7a2",
            title = LocalizedString(mapOf("en" to "Yuseong-gu Office", "ko" to "유성구청")),
            priority = 0.0,
            latitude = 36.362084,
            longitude = 127.356384
        ),
        TaxiLocation(
            id = "686d4d8f56fd773a8bd9d7a4",
            title = LocalizedString(mapOf("en" to "Yuseong Express Bus Terminal", "ko" to "유성 고속버스터미널")),
            priority = 0.0,
            latitude = 36.358279,
            longitude = 127.336467
        ),
        TaxiLocation(
            id = "686d4d8f56fd773a8bd9d7a6",
            title = LocalizedString(mapOf("en" to "Yuseong Intercity Bus Terminal", "ko" to "유성 시외버스터미널")),
            priority = 0.0,
            latitude = 36.355604,
            longitude = 127.335971
        ),
        TaxiLocation(
            id = "686d4d8f56fd773a8bd9d7a8",
            title = LocalizedString(mapOf("en" to "Government Complex Express Bus Terminal", "ko" to "대전청사 고속버스터미널")),
            priority = 0.0,
            latitude = 36.361462,
            longitude = 127.390504
        ),
        TaxiLocation(
            id = "686d4d8f56fd773a8bd9d7aa",
            title = LocalizedString(mapOf("en" to "Government Complex Intercity Bus Terminal", "ko" to "대전청사 시외버스터미널")),
            priority = 0.0,
            latitude = 36.361512,
            longitude = 127.379759
        )
    )
}

fun TaxiLocation.Companion.mock(): TaxiLocation{
    return TaxiLocation.mockList()[0]
}

