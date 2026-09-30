package org.sparcs.soap.app.domain.models.otl

data class SemesterGradeSummary(
    val gpa: Double?,
    val gradedCount: Int,
    val lectureCount: Int,
    val recordedCredits: Int,
    val earnedCredits: Int,
    val recordedAUs: Int = 0,
) {
    val isComplete: Boolean get() = gradedCount == lectureCount

    companion object {
        fun calculate(lectures: List<Lecture>, grades: Map<Int, LectureGrade>): SemesterGradeSummary {
            var weightedPoints = 0.0
            var gpaCredits = 0
            var gradedCount = 0
            var recordedCredits = 0
            var earnedCredits = 0
            var recordedAUs = 0
            lectures.forEach { lecture ->
                val grade = grades[lecture.id]
                if (grade != LectureGrade.NON_RECORD) {
                    recordedCredits += lecture.credit
                    recordedAUs += lecture.creditAU
                    if (grade != LectureGrade.FAIL && grade != LectureGrade.UNSATISFIED) {
                        earnedCredits += lecture.credit
                    }
                }
                if (grade != null) gradedCount++
                grade?.gradePoint?.takeIf { lecture.credit > 0 }?.let { point ->
                    weightedPoints += point * lecture.credit
                    gpaCredits += lecture.credit
                }
            }
            return SemesterGradeSummary(
                if (gpaCredits > 0) weightedPoints / gpaCredits else null,
                gradedCount, lectures.size, recordedCredits, earnedCredits, recordedAUs
            )
        }
    }
}
