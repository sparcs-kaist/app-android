package org.sparcs.soap.app.domain.usecases.reviewDigest

import org.sparcs.soap.app.domain.models.otl.LectureReview

object LectureReviewBatching {
    const val MAXIMUM_REVIEW_LENGTH = 600
    const val MAXIMUM_BATCHES = 3

    fun lines(reviews: List<LectureReview>): List<String> =
        reviews
            .filter { !it.isDeleted }
            .map { it to normalized(it.content) }
            .filter { (_, text) -> text.isNotEmpty() }
            .sortedWith(compareByDescending<Pair<LectureReview, String>> { it.first.like }.thenByDescending { it.first.year })
            .map { (review, text) -> "- [Grade ${review.grade}, Load ${review.load}, Speech ${review.speech}] $text" }

    suspend fun batches(lines: List<String>, budget: Int, cost: suspend (String) -> Int): List<List<String>> {
        val batches = mutableListOf<List<String>>()
        var current = mutableListOf<String>()
        var currentCost = 0
        for (line in lines) {
            val lineCost = cost(line)
            if (current.isNotEmpty() && currentCost + lineCost > budget) {
                batches += current
                if (batches.size == MAXIMUM_BATCHES) return batches
                current = mutableListOf()
                currentCost = 0
            }
            current += line
            currentCost += lineCost
        }
        if (current.isNotEmpty()) batches += current
        return batches
    }

    fun estimatedTokens(text: String): Int {
        var ascii = 0
        var other = 0
        text.forEach { if (it.code < 128) ascii++ else other++ }
        return other + (ascii + 2) / 3
    }

    fun readableCount(reviews: List<LectureReview>): Int =
        reviews.count { !it.isDeleted && it.content.isNotBlank() }

    private fun normalized(content: String): String {
        val text = content.split(Regex("""\s+""")).filter { it.isNotEmpty() }.joinToString(" ")
        return if (text.length > MAXIMUM_REVIEW_LENGTH) text.take(MAXIMUM_REVIEW_LENGTH) + "…" else text
    }
}
