package org.sparcs.soap.app.domain.models.otl

import java.time.Instant

data class FriendList(
    /** When the server last recomputed the friends' current-schedule status. */
    val checkedAt: Instant?,
    val friends: List<Friend>,
)
