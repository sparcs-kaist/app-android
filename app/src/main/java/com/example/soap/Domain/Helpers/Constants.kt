package com.example.soap.Domain.Helpers

object Constants {
    // MARK: Authorization
    val authorizationURL = "https://taxi.dev.sparcs.org/api/auth/sparcsapp/login?codeChallenge="

    // MARK: Taxi
    val taxiBackendURL = "https://taxi.dev.sparcs.org/api/"
    val taxiSocketURL = "https://taxi.dev.sparcs.org/"
    val taxiChatImageURL = "https://sparcs-taxi-dev.s3.ap-northeast-2.amazonaws.com/chat-img/"

    val taxiBankCodeMap: Map<String, String> = mapOf(
        "NH농협" to "011",
        "KB국민" to "004",
        "카카오뱅크" to "090",
        "신한" to "088",
        "우리" to "020",
        "IBK기업" to "003",
        "하나" to "081",
        "토스뱅크" to "092",
        "새마을" to "045",
        "부산" to "032",
        "대구" to "031",
        "케이뱅크" to "089",
        "신협" to "048",
        "우체국" to "071",
        "SC제일" to "023",
        "경남" to "039",
        "수협" to "007",
        "광주" to "034",
        "전북" to "037",
        "저축은행" to "050",
        "씨티" to "027",
        "제주" to "035",
        "KDB산업" to "002",
        "산림" to "064"
    )

    val taxiBankNameList: List<String> = taxiBankCodeMap.keys.toList()
    val taxiInviteURL = "https://taxi.dev.sparcs.org/invite/"
    // MARK: Ara
    val araBackendURL = "https://newara.dev.sparcs.org/api/"

    // MARK: Feed
    val feedBackendURL = "https://app.dev.sparcs.org/v1/"

}
