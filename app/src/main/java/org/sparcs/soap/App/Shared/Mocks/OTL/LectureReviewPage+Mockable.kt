package org.sparcs.soap.App.Shared.Mocks.OTL

import org.sparcs.soap.App.Domain.Models.OTL.LectureReview
import org.sparcs.soap.App.Domain.Models.OTL.LectureReviewPage

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