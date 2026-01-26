package org.sparcs.soap.App.Shared.Mocks

import org.sparcs.soap.App.Domain.Models.Taxi.TaxiUser
import java.net.URL
import java.util.Calendar

fun TaxiUser.Companion.mock(): TaxiUser {
    return TaxiUser.mockList()[0]
}

fun TaxiUser.Companion.mockList(): List<TaxiUser>{
    return listOf(
        TaxiUser(
            id = "mock-taxi-user-1",
            oid = "user1",
            name = "Alice Kim",
            nickname = "Alice",
            badge = true,
            residence = "기숙사",
            phoneNumber = "010-1234-5678",
            email = "alice@example.com",
            withdraw = false,
            ban = false,
            agreeOnTermsOfService = true,
            joinedAt = Calendar.getInstance().apply { add(Calendar.MONTH, -6) }.time,
            profileImageURL = URL("https://sparcs-taxi-dev.s3.ap-northeast-2.amazonaws.com/profile-img/default/NupjukOTL.png"),
            account = "카카오뱅크 3333-01-1234567"
        ),
        TaxiUser(
            id = "mock-taxi-user-2",
            oid = "user2",
            name = "Bob Lee",
            nickname = "Bob",
            badge = true,
            residence = "기숙사",
            phoneNumber = "010-9876-5432",
            email = "bob@example.com",
            withdraw = false,
            ban = false,
            agreeOnTermsOfService = true,
            joinedAt = Calendar.getInstance().apply { add(Calendar.MONTH, -3) }.time,
            profileImageURL = URL("https://sparcs-taxi-dev.s3.ap-northeast-2.amazonaws.com/profile-img/default/GooseOTL.png"),
            account = "신한은행 110-123-456789"
        ),
        TaxiUser(
            id = "mock-taxi-user-3",
            oid = "user3",
            name = "Charlie Park",
            nickname = "Charlie",
            badge = true,
            residence = "기숙사",
            phoneNumber = "010-5555-6666",
            email = "charlie@example.com",
            withdraw = false,
            ban = false,
            agreeOnTermsOfService = true,
            joinedAt = Calendar.getInstance().apply { add(Calendar.MONTH, -2) }.time,
            profileImageURL = URL("https://sparcs-taxi-dev.s3.ap-northeast-2.amazonaws.com/profile-img/default/NupjukOTL.png"),
            account = "국민은행 123456-04-123456"
        )
    )
}