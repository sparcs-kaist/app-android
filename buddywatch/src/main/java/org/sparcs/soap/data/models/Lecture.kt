package org.sparcs.soap.data.models

import kotlinx.serialization.Serializable

@Serializable
data class Lecture(
    val id: Int,
    val name: String,
    val code: String = "",
    val classes: List<LectureClass>,
    val color: String? = null // Hex color string
) {
    companion object {
        fun mock(
            id: Int = 1,
            name: String = "소프트웨어 프로젝트",
            code: String = "CS220",
            classes: List<LectureClass> = listOf(LectureClass.mock()),
            color: String? = "#4A90E2"
        ) = Lecture(
            id = id,
            name = name,
            code = code,
            classes = classes,
            color = color
        )

        fun mockList() = listOf(
            mock(id = 1926802, name = "프로그래밍언어", code = "CS.30200", color = "#4A90E2",
                classes = listOf("MON", "WED").map { LectureClass(it, 870, 960, "E11 104") }),
            mock(
                id = 1928379,
                name = "일반물리학 II",
                code = "PH.10042",
                color = "#FF6B6B",
                classes = listOf("TUE", "THU").map { LectureClass(it, 630, 720, "E11 311") }
            ),
            mock(id = 1928998, name = "미적분학 II", code = "MAS.10002", color = "#51CF66",
                classes = listOf("TUE", "THU").map { LectureClass(it, 780, 870, "E11 412") }),
            mock(id = 1927923, name = "일반화학실험 I", code = "CH.10002", color = "#FCC419",
                classes = listOf(LectureClass("FRI", 780, 960, "E6-5 402"))),
            mock(id = 1932260, name = "인성/리더십 III", code = "HSS.10074", color = "#845EF7",
                classes = listOf(LectureClass("WED", 1200, 1320, "E11 210")))
        )
    }
}
