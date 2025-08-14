package com.example.soap.Domain.Helpers

import java.net.URL

object Constants {
    // MARK: Authorization
    val authorizationURL: URL = URL("https://taxi.dev.sparcs.org/api/auth/sparcsapp/login/")

    // MARK: Taxi
    val taxiBackendURL: URL = URL("https://taxi.dev.sparcs.org/api/")
    val taxiSocketURL: URL = URL("https://taxi.dev.sparcs.org/")
    val taxiChatImageURL: URL = URL("https://sparcs-taxi-dev.s3.ap-northeast-2.amazonaws.com/chat-img/")

    // MARK: Ara
    val araBackendURL: URL = URL("https://newara.dev.sparcs.org/api/")

    val taxiBankNameList: List<String> = listOf(
        "NH농협",
        "KB국민",
        "카카오뱅크",
        "신한",
        "우리",
        "IBK기업",
        "하나",
        "토스뱅크",
        "새마을",
        "부산",
        "대구",
        "케이뱅크",
        "신협",
        "우체국",
        "SC제일",
        "경남",
        "수협",
        "광주",
        "전북",
        "저축은행",
        "씨티",
        "제주",
        "KDB산업"
    )
}
