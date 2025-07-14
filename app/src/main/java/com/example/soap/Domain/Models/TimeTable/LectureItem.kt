package com.example.soap.Domain.Models.TimeTable

import java.util.UUID

data class LectureItem(
    val id: UUID = UUID.randomUUID(),
    val lecture: Lecture,
    val index: Int
)
