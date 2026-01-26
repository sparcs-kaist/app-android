package org.sparcs.soap.App.Domain.Models.OTL

import org.sparcs.soap.App.Domain.Enums.OTL.SemesterType

object ReportMailComposer {

    fun compose(
        title: String,
        code: String,
        year: Int,
        semester: SemesterType,
        professorName: String,
        content: String
    ): String {
        val body = """
            *Please describe a reason for reporting.
            ------------------------------------------------------------------
            
            
            ------------------------------------------------------------------
            [Review Information]
            *This information has been filled automatically. Please do NOT edit.
            Lecture:  $title ($code)
            Semester:  $year ${semester.name}
            Prof.: $professorName
            Content:
            $content
        """.trimIndent()

        return "mailto:otlplus@sparcs.org?subject=[Reason for Reporting]&body=$body"
    }
}