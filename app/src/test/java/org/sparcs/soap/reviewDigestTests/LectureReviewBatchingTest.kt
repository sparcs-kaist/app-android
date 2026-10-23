package org.sparcs.soap.reviewDigestTests

import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.sparcs.soap.app.domain.models.otl.LectureReview
import org.sparcs.soap.app.domain.usecases.reviewDigest.LectureReviewBatching
import org.sparcs.soap.app.shared.mocks.otl.mock

class LectureReviewBatchingTest {
    private fun review(id: Int, content: String, like: Int = 0, year: Int = 2024, deleted: Boolean = false) =
        LectureReview.mock().copy(id = id, content = content, like = like, year = year, isDeleted = deleted)

    @Test
    fun `lines skip deleted and empty reviews and order most liked then newest`() {
        val lines = LectureReviewBatching.lines(
            listOf(
                review(1, "old", like = 2, year = 2022),
                review(2, "   \n "),
                review(3, "gone", like = 9, deleted = true),
                review(4, "new", like = 2, year = 2025),
                review(5, "popular", like = 7),
            )
        )
        assertEquals(3, lines.size)
        assertTrue(lines[0].endsWith("popular"))
        assertTrue(lines[1].endsWith("new"))
        assertTrue(lines[2].endsWith("old"))
    }

    @Test
    fun `lines carry ratings and collapse whitespace into one line`() {
        val line = LectureReviewBatching.lines(listOf(review(1, "a\n\n  b\tc"))).single()
        assertTrue(line.startsWith("- [Grade A+, Load"))
        assertTrue(line.endsWith("] a b c"))
    }

    @Test
    fun `long reviews are cut short`() {
        val line = LectureReviewBatching.lines(listOf(review(1, "x".repeat(1000)))).single()
        assertTrue(line.endsWith("x".repeat(LectureReviewBatching.MAXIMUM_REVIEW_LENGTH) + "…"))
    }

    @Test
    fun `batches pack lines up to the budget in order`() = runTest {
        val batches = LectureReviewBatching.batches(listOf("a", "b", "c", "d"), budget = 2) { 1 }
        assertEquals(listOf(listOf("a", "b"), listOf("c", "d")), batches)
    }

    @Test
    fun `an oversized line still gets its own batch`() = runTest {
        val batches = LectureReviewBatching.batches(listOf("big", "a"), budget = 2) { if (it == "big") 5 else 1 }
        assertEquals(listOf(listOf("big"), listOf("a")), batches)
    }

    @Test
    fun `batches stop at the maximum`() = runTest {
        val batches = LectureReviewBatching.batches((1..10).map { "$it" }, budget = 1) { 1 }
        assertEquals(LectureReviewBatching.MAXIMUM_BATCHES, batches.size)
        assertEquals(listOf("3"), batches.last())
    }

    @Test
    fun `estimated tokens count hangul per character and latin per three`() {
        assertEquals(3, LectureReviewBatching.estimatedTokens("한국어"))
        assertEquals(2, LectureReviewBatching.estimatedTokens("abcd"))
        assertEquals(0, LectureReviewBatching.estimatedTokens(""))
    }

    @Test
    fun `readable count ignores deleted and blank reviews`() {
        val count = LectureReviewBatching.readableCount(
            listOf(review(1, "ok"), review(2, " "), review(3, "x", deleted = true))
        )
        assertEquals(1, count)
    }
}
