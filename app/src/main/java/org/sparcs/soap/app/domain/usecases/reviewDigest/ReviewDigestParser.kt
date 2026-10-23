package org.sparcs.soap.app.domain.usecases.reviewDigest

import org.sparcs.soap.app.domain.models.reviewDigest.LectureReviewDigest

object ReviewDigestParser {
    private enum class Section { OVERVIEW, LIKED, DISLIKED, WORKLOAD, GRADING }

    private val headers = Section.entries.associateBy { it.name }
    private val header = Regex("""^\s*\**\s*([A-Za-z]+)\s*\**\s*:\s*(.*)$""")
    private val bullet = Regex("""^\s*(?:[-*•·]|\d+[.)])\s*""")
    private const val MAXIMUM_POINTS = 4

    fun parse(response: String): LectureReviewDigest {
        val sections = mutableMapOf<Section, MutableList<String>>()
        var current: Section? = null
        response.lines().forEach { raw ->
            val match = header.find(raw)
            val section = match?.groupValues?.get(1)?.uppercase()?.let(headers::get)
            if (section != null) {
                current = section
                val rest = match.groupValues[2].trim()
                sections.getOrPut(section) { mutableListOf() }.apply { if (rest.isNotEmpty()) add(rest) }
            } else {
                val line = raw.trim()
                if (line.isNotEmpty()) current?.let { sections.getOrPut(it) { mutableListOf() }.add(line) }
            }
        }
        fun sentence(section: Section) = sections[section]?.joinToString(" ")?.cleaned()?.takeIf { it.isNotEmpty() }
        fun points(section: Section) = sections[section].orEmpty()
            .map { it.replace(bullet, "").cleaned() }
            .filter { it.isNotEmpty() && !it.isNone() }
            .take(MAXIMUM_POINTS)
        return LectureReviewDigest(
            overview = sentence(Section.OVERVIEW),
            liked = points(Section.LIKED),
            disliked = points(Section.DISLIKED),
            workload = sentence(Section.WORKLOAD),
            grading = sentence(Section.GRADING),
        )
    }

    private fun String.cleaned() = replace("**", "").trim()

    private fun String.isNone() = lowercase().trimEnd('.') in setOf("none", "n/a", "없음", "없다", "-")
}
