package org.sparcs.soap.app.domain.models.otl

data class Friend(
    val id: Int,
    val name: String,
    val isFavorite: Boolean,
    /** Whether the friend has a lecture or activity in progress right now. */
    val hasScheduleNow: Boolean,
) {
    companion object
}
