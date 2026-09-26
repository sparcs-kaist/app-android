package org.sparcs.soap.app.domain.models.otl

import kotlinx.serialization.Serializable

@Serializable
data class CreditSummarySnapshot(
    val gpa: Double?,
    val earnedCredits: Int,
    val graduationCredits: Int,
    val updatedAt: Long = System.currentTimeMillis(),
) {
    fun hasSameValues(other: CreditSummarySnapshot): Boolean =
        gpa == other.gpa && earnedCredits == other.earnedCredits && graduationCredits == other.graduationCredits
}
