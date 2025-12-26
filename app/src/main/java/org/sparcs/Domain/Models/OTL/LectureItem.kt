package org.sparcs.Domain.Models.OTL

import java.util.UUID

data class LectureItem(
    val id: UUID = UUID.randomUUID(),
    val lecture: Lecture,
    val index: Int
)