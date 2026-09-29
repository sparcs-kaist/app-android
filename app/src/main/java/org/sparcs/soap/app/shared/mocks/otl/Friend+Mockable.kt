package org.sparcs.soap.app.shared.mocks.otl

import org.sparcs.soap.app.domain.models.otl.Friend

fun Friend.Companion.mock(): Friend =
    Friend(id = 100, name = "Test Friend", isFavorite = false, hasScheduleNow = true)

fun Friend.Companion.mockList(): List<Friend> = listOf(
    Friend(id = 1, name = "Sujin Park", isFavorite = true, hasScheduleNow = true),
    Friend(id = 2, name = "Minho Kim", isFavorite = true, hasScheduleNow = false),
    Friend(id = 3, name = "Jiwoo Lee", isFavorite = false, hasScheduleNow = false),
    Friend(id = 4, name = "Haeun Choi", isFavorite = false, hasScheduleNow = true)
)
