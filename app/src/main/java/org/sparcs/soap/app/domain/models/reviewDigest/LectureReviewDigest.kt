package org.sparcs.soap.app.domain.models.reviewDigest

data class LectureReviewDigest(
    val overview: String? = null,
    val liked: List<String> = emptyList(),
    val disliked: List<String> = emptyList(),
    val workload: String? = null,
    val grading: String? = null,
)

sealed interface LectureReviewDigestProgress {
    data class Started(val reviewCount: Int) : LectureReviewDigestProgress
    data class Reading(val completed: Int, val total: Int) : LectureReviewDigestProgress
    data class Writing(val digest: LectureReviewDigest) : LectureReviewDigestProgress
}

enum class LectureReviewDigestError { UNAVAILABLE, NO_REVIEWS, UNSAFE_CONTENT, BUSY, FAILED }

class LectureReviewDigestException(val reason: LectureReviewDigestError) : Exception(reason.name)
