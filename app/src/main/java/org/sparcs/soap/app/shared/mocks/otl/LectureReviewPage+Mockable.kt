package org.sparcs.soap.app.shared.mocks.otl

import org.sparcs.soap.app.domain.models.otl.LectureReview
import org.sparcs.soap.app.domain.models.otl.LectureReviewPage

fun LectureReviewPage.Companion.mock(): LectureReviewPage {
    return LectureReviewPage(
        reviews = LectureReview.mockList(),
        averageGrade = 5.0,
        averageLoad = 5.0,
        averageSpeech = 1.0,
        department = null,
        totalCount = 10
    )
}