package org.sparcs.soap.tipsTests

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.sparcs.soap.app.domain.models.otl.LectureReview
import org.sparcs.soap.app.features.lectureDetail.components.summariseTipReviewID
import org.sparcs.soap.app.shared.mocks.otl.mock
import org.sparcs.soap.app.shared.tips.PostToolsTipRule
import java.util.Locale

class TipPlacementRulesTest {
    private val long = "강의 내용이 정말 알찼고 과제도 적당했습니다. ".repeat(10)

    private fun review(id: Int, content: String) = LectureReview.mock().copy(id = id, content = content)

    @Test fun `summarise tip goes to the first long review that is not mine`() {
        val reviews = listOf(review(1, long), review(2, "짧아요"), review(3, long), review(4, long))
        assertEquals(3, summariseTipReviewID(reviews.drop(1), myReviewID = null))
        assertEquals(3, summariseTipReviewID(reviews, myReviewID = 1))
    }

    @Test fun `summarise tip has no review when none is long enough`() {
        assertNull(summariseTipReviewID(listOf(review(1, "좋아요"), review(2, "  ")), myReviewID = null))
        assertNull(summariseTipReviewID(emptyList(), myReviewID = null))
    }

    @Test fun `post tools tip applies to long posts`() {
        assertTrue(PostToolsTipRule.applies("가".repeat(400), Locale.KOREAN))
        assertFalse(PostToolsTipRule.applies("짧은 글입니다", Locale.KOREAN))
    }

    @Test fun `post tools tip applies to short posts in another language`() {
        assertTrue(PostToolsTipRule.applies("Looking for a study partner", Locale.KOREAN))
        assertTrue(PostToolsTipRule.applies("스터디 같이 하실 분", Locale.ENGLISH))
        assertFalse(PostToolsTipRule.applies("Looking for a study partner", Locale.ENGLISH))
    }

    @Test fun `post tools tip ignores posts without letters`() {
        assertFalse(PostToolsTipRule.applies("   ", Locale.KOREAN))
        assertFalse(PostToolsTipRule.applies("2026-10-20 12:00", Locale.KOREAN))
    }
}
