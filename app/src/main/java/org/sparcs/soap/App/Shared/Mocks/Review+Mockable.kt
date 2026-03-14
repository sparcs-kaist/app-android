package org.sparcs.soap.App.Shared.Mocks

import org.sparcs.soap.App.Domain.Models.OTL.Review
import org.sparcs.soap.App.Domain.Models.OTL.ReviewResponse

fun ReviewResponse.Companion.mock(): ReviewResponse {
    return ReviewResponse(
        reviews = Review.mockList(),
        grade = 4.5,
        load = 3.0,
        speech = 2.5
    )
}