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

    @Test
    fun `korean section labels are read`() {
        val digest = ReviewDigestParser.parse("**개요**: 무난한 수업이에요.\n좋은 점:\n- 친절한 설명\n과제량: 적당해요.\n성적: 후한 편이에요.")
        assertEquals("무난한 수업이에요.", digest.overview)
        assertEquals(listOf("친절한 설명"), digest.liked)
        assertEquals("적당해요.", digest.workload)
        assertEquals("후한 편이에요.", digest.grading)
    }

    @Test
    fun `a summary without an overview label keeps its opening text as the overview`() {
        val digest = ReviewDigestParser.parse("전반적으로 만족도가 높아요.\nLIKED:\n- 명확한 강의")
        assertEquals("전반적으로 만족도가 높아요.", digest.overview)
        assertEquals(listOf("명확한 강의"), digest.liked)
    }

    @Test
    fun `markdown headings bullets and emphasis are stripped`() {
        val digest = ReviewDigestParser.parse(
            "## **OVERVIEW:** 대체로 *만족스러운* 수업이에요.\n### 좋은 점\n* **명확한** 설명\n* `과제` 피드백\n#\nDISLIKED:\n- None\n**WORKLOAD:** 매주 과제가 있어요."
        )
        assertEquals("대체로 만족스러운 수업이에요.", digest.overview)
        assertEquals(listOf("명확한 설명", "과제 피드백"), digest.liked)
        assertEquals(emptyList<String>(), digest.disliked)
        assertEquals("매주 과제가 있어요.", digest.workload)
        val text = listOfNotNull(digest.overview, digest.workload).plus(digest.liked).joinToString()
        listOf("#", "*", "`").forEach { assertEquals(false, text.contains(it)) }
    }
}
