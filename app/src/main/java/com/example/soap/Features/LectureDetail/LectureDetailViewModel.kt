package com.example.soap.Features.LectureDetail

import com.example.soap.Domain.Models.TimeTable.Lecture
import com.example.soap.Shared.Mocks.mockList

class LectureDetailViewModel {
    private val lectures = Lecture.mockList()

    fun getLectureById(id: Int): Lecture? {
        return lectures.find { it.id == id }
    }
}
