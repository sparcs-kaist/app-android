package org.sparcs.Domain.Helpers

import org.sparcs.BuildConfig

object Constants {
    // MARK: Authorization
    val authorizationURL: String
        get() = if (BuildConfig.DEBUG) {
            "https://taxi.dev.sparcs.org/api/auth/sparcsapp/login?codeChallenge="
        } else {
            "https://taxi.sparcs.org/api/auth/sparcsapp/login?codeChallenge="
        }

    // MARK: Terms
    val privacyPolicyURL = "https://github.com/sparcs-kaist/privacy/blob/main/Privacy.md"
    val termsOfUseURL = "https://github.com/sparcs-kaist/privacy/blob/main/TermsOfUse.md"

    // MARK: Taxi
    val taxiBackendURL: String
        get() = if (BuildConfig.DEBUG) {
            "https://taxi.dev.sparcs.org/api/"
        } else {
            "https://taxi.sparcs.org/api/"
        }

    val taxiSocketURL: String
        get() = if (BuildConfig.DEBUG) {
            "https://taxi.dev.sparcs.org/"
        } else {
            "https://taxi.sparcs.org/"
        }

    val taxiChatImageURL: String
        get() = if (BuildConfig.DEBUG) {
            "https://sparcs-taxi-dev.s3.ap-northeast-2.amazonaws.com/chat-img/"
        } else {
            "https://sparcs-taxi-prod.s3.ap-northeast-2.amazonaws.com/chat-img/"
        }

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
    val taxiInviteURL: String
        get() = if (BuildConfig.DEBUG) {
            "https://taxi.dev.sparcs.org/invite/"
        } else {
            "https://taxi.sparcs.org/invite/"
        }

    val taxiRoomNameRegex = Regex("^[A-Za-z0-9가-힣ㄱ-ㅎㅏ-ㅣ,.?! _~/#'@=\"^()+*<>{}\\[\\]\\-]{1,50}$")

    val taxiMaxRoomCount = 5

    // MARK: Ara
    val araBackendURL: String
        get() = if (BuildConfig.DEBUG) {
            "https://newara.dev.sparcs.org/api/"
        } else {
            "https://newara.sparcs.org/api/"
        }

    val araShareURL: String
        get() = if (BuildConfig.DEBUG) {
            "https://newara.dev.sparcs.org/post/"
        } else {
            "https://newara.sparcs.org/post/"
        }

    // MARK: Feed
    val feedBackendURL: String
        get() = if (BuildConfig.DEBUG) {
            "https://buddy.dev.sparcs.org/v1/"
        } else {
            "https://buddy.sparcs.org/v1/"
        }

    val feedShareURL = "https://sparcs.org/feed/"

    // MARK: OTL
    val otlBackendURL: String
        get() = if (BuildConfig.DEBUG) {
            "https://api.otl.dev.sparcs.org/"
        } else {
            "https://otl.sparcs.org/"
        }

    // MARK: MAPS
    val mapsURL = "https://api.openrouteservice.org/v2/directions/driving-car?"

}
