package org.sparcs.soap.app.domain.models.otl

import kotlinx.serialization.EncodeDefault
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.Serializable

@OptIn(ExperimentalSerializationApi::class)
@Serializable
data class CreditSummarySnapshot(
    val gpa: Double?,
    val earnedCredits: Int,
    val graduationCredits: Int,
    @EncodeDefault val updatedAt: Long = System.currentTimeMillis(),
) {
    fun hasSameValues(other: CreditSummarySnapshot): Boolean =
        gpa == other.gpa && earnedCredits == other.earnedCredits && graduationCredits == other.graduationCredits
}
