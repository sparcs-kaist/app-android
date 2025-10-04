package com.example.soap.Domain.Models.OTL

import com.example.soap.Domain.Enums.SemesterType
import java.net.URLEncoder
import java.nio.charset.StandardCharsets

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

        val encodedBody = URLEncoder.encode(body, StandardCharsets.UTF_8.toString())

        return "mailto:otlplus@sparcs.org?subject=[Reason for Reporting]&body=$encodedBody"
    }
}