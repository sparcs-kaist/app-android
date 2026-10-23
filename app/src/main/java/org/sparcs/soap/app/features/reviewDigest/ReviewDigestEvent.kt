package org.sparcs.soap.app.features.reviewDigest

import org.sparcs.soap.app.domain.enums.Event
import org.sparcs.soap.app.domain.models.reviewDigest.LectureReviewDigestError

sealed class ReviewDigestEvent : Event {
    data class Requested(val screen: String, val reviewCount: Int) : ReviewDigestEvent()
    data class Finished(val screen: String) : ReviewDigestEvent()
    data class Failed(val screen: String, val reason: LectureReviewDigestError) : ReviewDigestEvent()

    override val source: String
        get() = "ReviewDigest"

    override val name: String
        get() = when (this) {
            is Requested -> "review_digest_requested"
            is Finished -> "review_digest_finished"
            is Failed -> "review_digest_failed"
        }

    override val parameters: Map<String, Any>
        get() = when (this) {
            is Requested -> mapOf("source" to screen, "review_count" to reviewCount)
            is Finished -> mapOf("source" to screen)
            is Failed -> mapOf("source" to screen, "reason" to reason.name.lowercase())
        }
}
