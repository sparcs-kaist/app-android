package org.sparcs.soap.app.domain.models.otl

import java.util.UUID

data class LectureItem(
    val id: UUID = UUID.randomUUID(),
    val lecture: Lecture,
    val lectureClass: LectureClass,
    val index: Int
) {
    companion object
}