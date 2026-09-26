package org.sparcs.soap.app.domain.models.otl

object RetakeResolver {
    fun countedLectures(lectures: List<Lecture>, grades: Map<Int, LectureGrade>): List<Lecture> {
        val countedIDs = lectures.groupBy { it.code }.values.mapNotNull { attempts ->
            if (attempts.size == 1) return@mapNotNull attempts.single().id
            val onRecord = attempts.filter { grades[it.id] != LectureGrade.NON_RECORD }
            val best = onRecord.asReversed().filter { grades[it.id] != null }
                .maxByOrNull { rank(grades[it.id]) }
            val pending = onRecord.lastOrNull { grades[it.id] == null }
            when {
                best != null && isPassed(grades[best.id]) -> best.id
                pending != null -> pending.id
                else -> best?.id
            }
        }.toSet()
        return lectures.filter { it.id in countedIDs }
    }

    fun supersededLectureIDs(lectures: List<Lecture>, grades: Map<Int, LectureGrade>): Set<Int> {
        val countedIDs = countedLectures(lectures, grades).map { it.id }.toSet()
        val retakenCodes = lectures.groupingBy { it.code }.eachCount().filterValues { it > 1 }.keys
        return lectures.filter {
            it.code in retakenCodes && it.id !in countedIDs && grades[it.id] != LectureGrade.NON_RECORD
        }.map { it.id }.toSet()
    }

    private fun rank(grade: LectureGrade?): Double = when (grade) {
        LectureGrade.PASS, LectureGrade.SATISFIED -> 0.5
        null -> -1.0
        else -> grade.gradePoint ?: 0.0
    }

    private fun isPassed(grade: LectureGrade?): Boolean = when (grade) {
        null, LectureGrade.FAIL, LectureGrade.UNSATISFIED, LectureGrade.NON_RECORD -> false
        else -> true
    }
}
