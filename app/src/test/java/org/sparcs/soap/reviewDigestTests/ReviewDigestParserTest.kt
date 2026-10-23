package org.sparcs.soap.reviewDigestTests

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.sparcs.soap.app.domain.models.reviewDigest.LectureReviewDigest
import org.sparcs.soap.app.domain.usecases.reviewDigest.ReviewDigestParser

class ReviewDigestParserTest {
    @Test
    fun `parses every section`() {
        val digest = ReviewDigestParser.parse(
            """
            OVERVIEW: 강의가 명확하고 성적이 후합니다.
            LIKED:
            - 친절한 설명
            * 빠른 피드백
            DISLIKED:
            1. 과제가 많음
            WORKLOAD: 매주 과제가 있습니다.
            GRADING: 후한 편입니다.
            """.trimIndent()
        )
        assertEquals(
            LectureReviewDigest(
                overview = "강의가 명확하고 성적이 후합니다.",
                liked = listOf("친절한 설명", "빠른 피드백"),
                disliked = listOf("과제가 많음"),
                workload = "매주 과제가 있습니다.",
                grading = "후한 편입니다.",
            ),
            digest
        )
    }

    @Test
    fun `partial responses fill in what has arrived`() {
        val digest = ReviewDigestParser.parse("OVERVIEW: Clear lectures.\nLIKED:\n- Helpful")
        assertEquals("Clear lectures.", digest.overview)
        assertEquals(listOf("Helpful"), digest.liked)
        assertTrue(digest.disliked.isEmpty())
        assertNull(digest.workload)
    }

    @Test
    fun `none and markdown emphasis are cleaned`() {
        val digest = ReviewDigestParser.parse("**OVERVIEW:** **Good**\nDISLIKED:\n- None\nGRADING: 없음 아님")
        assertEquals("Good", digest.overview)
        assertTrue(digest.disliked.isEmpty())
        assertEquals("없음 아님", digest.grading)
    }

    @Test
    fun `liked points are capped at four`() {
        val digest = ReviewDigestParser.parse("LIKED:\n- a\n- b\n- c\n- d\n- e")
        assertEquals(listOf("a", "b", "c", "d"), digest.liked)
    }

    @Test
    fun `text before any header is ignored`() {
        val digest = ReviewDigestParser.parse("Sure, here it is.\nOVERVIEW: Fine.")
        assertEquals("Fine.", digest.overview)
    }

    @Test
    fun `empty response gives an empty digest`() {
        assertEquals(LectureReviewDigest(), ReviewDigestParser.parse(""))
    }
}
