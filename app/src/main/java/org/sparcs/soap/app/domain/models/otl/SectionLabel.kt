package org.sparcs.soap.app.domain.models.otl

data class SectionLabel(val title: String?, val professors: String?) {
    companion object {
        fun of(subtitle: String, professors: List<Professor>): SectionLabel {
            val names = professors.map { it.name.trim() }.filter { it.isNotEmpty() }
            val topic = subtitle.subtitleTopic ?: return SectionLabel(names.joinedOrNull(), null)
            return SectionLabel(topic, names.filterNot { it.equals("Staff", ignoreCase = true) }.joinedOrNull())
        }

        private fun List<String>.joinedOrNull(): String? = takeIf { it.isNotEmpty() }?.joinToString(", ")
    }
}

val String.subtitleTopic: String?
    get() {
        val trimmed = trim()
        val topic = if (trimmed.length >= 2 && trimmed.startsWith("<") && trimmed.endsWith(">")) {
            trimmed.substring(1, trimmed.length - 1).trim()
        } else {
            trimmed
        }
        return topic.ifEmpty { null }
    }

val Lecture.sectionLabel: SectionLabel get() = SectionLabel.of(subtitle, professors)

val CourseHistoryClass.sectionLabel: SectionLabel get() = SectionLabel.of(subtitle, professors)

val Course.sectionSubtitles: Map<Int, String>
    get() = history.flatMap { it.classes }
        .filter { it.subtitle.isNotBlank() }
        .associate { it.lectureID to it.subtitle }
