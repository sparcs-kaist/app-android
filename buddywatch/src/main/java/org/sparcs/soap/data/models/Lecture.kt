package org.sparcs.soap.data.models

import kotlinx.serialization.Serializable

@Serializable
data class Lecture(
    val id: Int,
    val name: String,
    val classes: List<LectureClass>,
    val color: String? = null // Hex color string
) {
    companion object {
        fun mock(
            id: Int = 1,
            name: String = "소프트웨어 프로젝트",
            classes: List<LectureClass> = listOf(LectureClass.mock()),
            color: String? = "#4A90E2"
        ) = Lecture(
            id = id,
            name = name,
            classes = classes,
            color = color
        )


        fun mockList() = listOf(
            mock(id = 1, name = "소프트웨어 프로젝트", color = "#4A90E2"),
            mock(
                id = 2,
                name = "운영체제",
                color = "#FF6B6B",
                classes = listOf(LectureClass.mock(day = "TUE"))
            ),
            mock(id = 3, name = "데이터베이스", color = "#51CF66"),
            mock(id = 4, name = "이산구조", color = "#FCC419"),
            mock(id = 5, name = "컴퓨터 네트워크", color = "#845EF7")
        )
    }
}