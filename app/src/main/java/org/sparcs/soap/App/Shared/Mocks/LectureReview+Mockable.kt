package org.sparcs.soap.App.Shared.Mocks

import org.sparcs.soap.App.Domain.Enums.OTL.SemesterType
import org.sparcs.soap.App.Domain.Models.OTL.Professor
import org.sparcs.soap.App.Domain.Models.OTL.Review

fun Review.Companion.mock(): Review {
    return Review(
        id = 28073,
        courseId = 16614,
        lectureId = 1884766,
        courseName = "항공우주공학 실험Ⅱ",
        professors = listOf(
            Professor(
                id = 2269,
                name = "이상봉",
            ),
            Professor(
                id = 2438,
                name = "이전윤",
            )
        ),
        year = 2024,
        semester = SemesterType.AUTUMN,
        content = """
        실험 짧고, 성적도 후하고, 어느 정도 쓰기만 하면 좋습니다.

        공기역학은 압축성 공기역학 지식이 좀 많이 나와서 그걸 같이 듣거나 주변에 압공을 듣는 사람이 있다면 아주 좋습니다.

        공기역학 보고서는 성적이 안나와서 모르겠는데 그냥 무난히 쓰니까 A0 나옵니다. 잘 주는 것 같습니다.

        수업 시간에 시험이 없다보니 학생들이 잘 안들으나, 들으면 좋은 내용이 너무 많이 나와서 듣는 것을 추천드립니다.
        """.trimIndent(),
        like = 1,
        grade = 4,
        load = 4,
        speech = 5,
        isDeleted = false,
        isLiked = false
    )
}

fun Review.Companion.mockList(): List<Review> {
    return listOf(
        Review(
            id = 28073,
            courseId = 16614,
            lectureId = 1884766,
            courseName = "항공우주공학 실험Ⅱ",
            professors = listOf(
                Professor(
                    id = 2269,
                    name = "이상봉",
                ),
                Professor(
                    id = 2438,
                    name = "이전윤",
                )
            ),
            year = 2024,
            semester = SemesterType.AUTUMN,
            content = """
            실험 짧고, 성적도 후하고, 어느 정도 쓰기만 하면 좋습니다.

            공기역학은 압축성 공기역학 지식이 좀 많이 나와서 그걸 같이 듣거나 주변에 압공을 듣는 사람이 있다면 아주 좋습니다.

            공기역학 보고서는 성적이 안나와서 모르겠는데 그냥 무난히 쓰니까 A0 나옵니다. 잘 주는 것 같습니다.

            수업 시간에 시험이 없다보니 학생들이 잘 안들으나, 들으면 좋은 내용이 너무 많이 나와서 듣는 것을 추천드립니다.
            """.trimIndent(),
            like = 1,
            grade = 4,
            load = 4,
            speech = 5,
            isDeleted = false,
            isLiked = false
        ),
        Review(
            id = 24208,
            courseId = 16614,
            lectureId = 1884766,
            courseName = "항공우주공학 실험Ⅱ",
            professors = listOf(
                Professor(
                    id = 2269,
                    name = "이상봉",
                ),
                Professor(
                    id = 2438,
                    name = "이전윤",
                )
            ),
            year = 2024,
            semester = SemesterType.AUTUMN,

            content = """
            중간 이전에는 공기역학, 중간 이후에는 구조 관련 실험을 합니다. 공기 실험은 조교님들께서 세팅해 놓은 것에 간단한 조작만 하면 되고 구조 실험은 조교님들께서 다 해주십니다. 실험 자체는 매우 간단하고 빠르게 끝나지만 보고서 쓰는게 조금 빡셉니다. 특히, 공기 실험은 주어진 양식도 맞춰야해서 신경쓸게 많습니다. 점수 평균이 워낙 높아서(164.09/170) 조금만(2점 이상) 감점되어도 그레이드가 낮아지니 감점 안 당하게 꼼꼼히 쓰세요.
            """.trimIndent(),
            like = 2,
            grade = 4,
            load = 4,
            speech = 5,
            isDeleted = false,
            isLiked = false
        ),
        Review(
            id = 24190,
            courseId = 16614,
            lectureId = 1884766,
            courseName = "항공우주공학 실험Ⅱ",
            professors = listOf(
                Professor(
                    id = 2269,
                    name = "이상봉",
                ),
                Professor(
                    id = 2438,
                    name = "이전윤",
                )
            ),
            year = 2024,
            semester = SemesterType.AUTUMN,
            content = """
            박기수 교수님, 이전윤 교수님께서 각각 8주씩 수업을 진행하시고 총 8개의 보고서를 작성합니다.

            성적  
            166/170 A0  
            총점에서 4점 깎여 A0 받았는데, 평균이 164/170으로 워낙 높아서 A+은 받지 못한 것으로 생각됩니다.

            로드  
            하나의 실험이 끝나면 2주 후 까지 보고서를 작성하여 제출해야 하며, 주어진 양식에 맞게 제출해야 합니다.  
            제가 보고서를 적는데 오래 걸리는 편이라 하나의 보고서를 작성하는데 10시간 이상은 걸렸습니다.

            강의 및 실험  
            1~8주 박기수 교수님: 풍동을 이용한 공기역학 실험  
            9~18주 이전윤 교수님: 재료 관련 실험 (출석 체크 없음, 보고서 5개 작성)
            """.trimIndent(),
            like = 1,
            grade = 4,
            load = 3,
            speech = 5,
            isDeleted = false,
            isLiked = false
        )
    )
}
