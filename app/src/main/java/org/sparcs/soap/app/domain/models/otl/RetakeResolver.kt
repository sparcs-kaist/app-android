package org.sparcs.soap.app.domain.models.otl

object RetakeResolver {
    fun countedLectures(lectures: List<Lecture>, grades: Map<Int, LectureGrade>): List<Lecture> {
        val uniqueLectures = lectures.distinctBy { it.id }
        val countedIDs = uniqueLectures.groupBy { courseKey(it) }.values.mapNotNull { attempts ->
            if (attempts.size == 1) return@mapNotNull attempts.single().id
            attempts.lastOrNull { grades[it.id] != LectureGrade.NON_RECORD }?.id
        }.toSet()
        return uniqueLectures.filter { it.id in countedIDs }
    }

    fun supersededLectureIDs(lectures: List<Lecture>, grades: Map<Int, LectureGrade>): Set<Int> {
        val countedIDs = countedLectures(lectures, grades).map { it.id }.toSet()
        val retakenCodes = lectures.distinctBy { it.id }.groupingBy { courseKey(it) }
            .eachCount().filterValues { it > 1 }.keys
        return lectures.filter {
            courseKey(it) in retakenCodes && it.id !in countedIDs && grades[it.id] != LectureGrade.NON_RECORD
        }.map { it.id }.toSet()
    }

    private fun courseKey(lecture: Lecture): String = lecture.code.trim().takeIf { it.isNotEmpty() }
        ?.let { "code:$it" }
        ?: if (lecture.courseID > 0) "course:${lecture.courseID}" else "lecture:${lecture.id}"
}
