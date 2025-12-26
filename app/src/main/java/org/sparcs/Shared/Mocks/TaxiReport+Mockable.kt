package org.sparcs.Shared.Mocks

import org.sparcs.Domain.Models.Taxi.TaxiReport
import org.sparcs.Domain.Models.Taxi.TaxiReportedUser
import java.net.URL
import java.util.Date

fun TaxiReport.Companion.mock(): TaxiReport {
    return TaxiReport(
        id = "689cc4d514a641e076f953c0",
        creatorId = "689cc28b14a641e076f951ed",
        reportedUser = TaxiReportedUser(
            id = "test2eae3eea958ec71c2263",
            oid = "689cc28b14a641e076f951ed",
            nickname = "귀여운 운영체제 및 실험_c7360",
            profileImageUrl = URL("https://sparcs-taxi-dev.s3.ap-northeast-2.amazonaws.com/profile-img/default/NupjukOTL.png"),
            withdraw = false
        ),
        reason = TaxiReport.Reason.ETC_REASON,
        etcDetails = "방에 초대되지 않은 사람을 데리고 옴",
        time = Date(),
        roomId = "689cc49c14a641e076f952bf"
    )
}

fun TaxiReport.Companion.mockList(): List<TaxiReport>{
    return listOf(
        TaxiReport(
            id = "689cc6f914a641e076f9552c",
            creatorId = "689cc48f14a641e076f952a5",
            reportedUser = TaxiReportedUser(
                id = "test2eae3eea958ec71c2263",
                oid = "689cc28b14a641e076f951ed",
                nickname = "귀여운 운영체제 및 실험_c7360",
                profileImageUrl = URL("https://sparcs-taxi-dev.s3.ap-northeast-2.amazonaws.com/profile-img/default/NupjukOTL.png"),
                withdraw = false
            ),
            reason = TaxiReport.Reason.ETC_REASON,
            etcDetails = "방에 초대되지 않은 사람을 데리고 옴",
            time = Date(),
            roomId = "689cc49c14a641e076f952bf"
        ),
        TaxiReport(
            id = "689cc4d514a641e076f953c0",
            creatorId = "689cc28b14a641e076f951ed",
            reportedUser = TaxiReportedUser(
                id = "test2eae3eea958ec71c2263",
                oid = "689cc28b14a641e076f951ed",
                nickname = "귀여운 운영체제 및 실험_c7360",
                profileImageUrl = URL("https://sparcs-taxi-dev.s3.ap-northeast-2.amazonaws.com/profile-img/default/NupjukOTL.png"),
                withdraw = false
            ),
            reason = TaxiReport.Reason.NO_SETTLEMENT,
            etcDetails = "",
            time = Date(),
            roomId = "689cc49c14a641e076f952bf"
        )
    )
}