package org.sparcs.soap.app.domain.models.otl

enum class LectureGrade(val rawValue: String, val gradePoint: Double? = null) {
    PASS("P"), FAIL("F", 0.0), NON_RECORD("NR"),
    A_PLUS("A+", 4.3), A("A0", 4.0), A_MINUS("A-", 3.7),
    B_PLUS("B+", 3.3), B("B0", 3.0), B_MINUS("B-", 2.7),
    C_PLUS("C+", 2.3), C("C0", 2.0), C_MINUS("C-", 1.7),
    D_PLUS("D+", 1.3), D("D0", 1.0), D_MINUS("D-", 0.7),
    SATISFIED("S"), UNSATISFIED("U");

    val title: String get() = rawValue.removeSuffix("0")

    companion object {
        val creditOptions = entries.filter { it != SATISFIED && it != UNSATISFIED }
        val auOptions = listOf(SATISFIED, UNSATISFIED)
        fun options(lecture: Lecture): List<LectureGrade> =
            if (lecture.isAUOnly) auOptions else creditOptions
    }
}

val Lecture.isAUOnly: Boolean get() = credit == 0 && creditAU > 0
