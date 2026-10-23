package org.sparcs.soap.app.domain.usecases.reviewDigest

import org.sparcs.soap.app.domain.models.reviewDigest.LectureReviewDigest

object ReviewDigestParser {
    private enum class Section(vararg val labels: String) {
        OVERVIEW("overview", "개요", "요약", "총평", "概要"),
        LIKED("liked", "좋은 점", "장점", "좋았던 점", "良い点"),
        DISLIKED("disliked", "아쉬운 점", "단점", "아쉬웠던 점", "悪い点"),
        WORKLOAD("workload", "과제량", "과제", "학습량", "課題"),
        GRADING("grading", "성적", "학점", "評価"),
    }

    private val header = Regex("""^([^:：]{1,12}?)\s*[:：]\s*(.*)$""")
    private val headingMarks = Regex("""^#{1,6}\s*""")
    private val emphasis = Regex("""(\*{1,3}|_{2,3}|`+)""")
    private val bullet = Regex("""^\s*(?:[-*•·]|\d+[.)])\s+""")
    private const val MAXIMUM_POINTS = 4

    fun parse(response: String): LectureReviewDigest {
        val sections = mutableMapOf<Section, MutableList<String>>()
        val untitled = mutableListOf<String>()
        var current: Section? = null
        response.lines().map(::plain).filter { it.isNotEmpty() }.forEach { line ->
            val (section, rest) = headerOf(line)
            if (section != null) {
                current = section
                sections.getOrPut(section) { mutableListOf() }.apply { if (rest.isNotEmpty()) add(rest) }
            } else {
                current?.let { sections.getOrPut(it) { mutableListOf() }.add(line) } ?: untitled.add(line)
            }
        }
        fun lines(section: Section) = sections[section].orEmpty().map { it.replace(bullet, "").trim() }
        fun sentence(section: Section) = lines(section).joinToString(" ").takeIf { it.isNotEmpty() }
        fun points(section: Section) = lines(section).filter { it.isNotEmpty() && !it.isNone() }.take(MAXIMUM_POINTS)
        return LectureReviewDigest(
            overview = sentence(Section.OVERVIEW) ?: untitled.joinToString(" ").takeIf { it.isNotEmpty() },
            liked = points(Section.LIKED),
            disliked = points(Section.DISLIKED),
            workload = sentence(Section.WORKLOAD),
            grading = sentence(Section.GRADING),
        )
    }

    private fun headerOf(line: String): Pair<Section?, String> {
        header.find(line)?.let { match ->
            sectionFor(match.groupValues[1])?.let { return it to match.groupValues[2].trim() }
        }
        return sectionFor(line) to ""
    }

    private fun sectionFor(label: String): Section? {
        val normalized = label.trim().lowercase()
        return Section.entries.firstOrNull { section -> section.labels.any { it == normalized } }
    }

    private fun plain(raw: String): String {
        val line = raw.trim()
        val isBullet = bullet.containsMatchIn(line)
        val text = line.replace(bullet, "").replace(headingMarks, "").replace(emphasis, "").trim('#', '*', ' ')
        return if (isBullet && text.isNotEmpty()) "- $text" else text
    }

    private fun String.isNone() = lowercase().trimEnd('.') in setOf("none", "n/a", "없음", "없다", "-")
}
