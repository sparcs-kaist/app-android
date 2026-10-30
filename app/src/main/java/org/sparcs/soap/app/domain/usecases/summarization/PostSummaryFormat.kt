package org.sparcs.soap.app.domain.usecases.summarization

object PostSummaryFormat {
    const val MAXIMUM_FACTS = 4

    private val gistLabel = Regex("""^\s*(?:gist|요지|요약|要旨)\s*[:：]\s*""", RegexOption.IGNORE_CASE)
    private val factsLabel = Regex("""^\s*(?:facts|key facts|핵심 사실|사실|要点)\s*[:：]\s*""", RegexOption.IGNORE_CASE)
    private val bullet = Regex("""^\s*(?:[-*•·]|\d+[.)])\s+""")
    private val markdown = Regex("""(\*{1,3}|_{2,3}|`+|^#{1,6}\s*)""")
    private val emptyFact = Regex("""^(?:none|없음|なし)\.?$""", RegexOption.IGNORE_CASE)

    fun text(response: String): String {
        var gist = ""
        val facts = mutableListOf<String>()
        var inFacts = false
        response.lines().map { it.replace(markdown, "").trim() }.filter { it.isNotEmpty() }.forEach { line ->
            when {
                gistLabel.containsMatchIn(line) -> {
                    inFacts = false
                    gist = line.replaceFirst(gistLabel, "").trim()
                }
                factsLabel.containsMatchIn(line) -> {
                    inFacts = true
                    line.replaceFirst(factsLabel, "").trim().takeIf { it.isNotEmpty() }?.let { facts += it }
                }
                inFacts || bullet.containsMatchIn(line) -> facts += line.replaceFirst(bullet, "").trim()
                gist.isEmpty() -> gist = line
            }
        }
        val shownFacts = facts.filter { it.isNotEmpty() && !emptyFact.matches(it) }.distinct().take(MAXIMUM_FACTS)
        return (listOf(gist) + shownFacts.map { "• $it" }).filter { it.isNotEmpty() }.joinToString("\n")
    }
}
