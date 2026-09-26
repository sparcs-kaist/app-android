package org.sparcs.soap.app.domain.models.otl

import kotlinx.serialization.Serializable

@Serializable
data class CreditRequirements(
    val graduation: Int = 138,
    val basicRequired: Int = 23,
    val basicElective: Int = 9,
    val hseCore: Int = 3,
    val hseGeneral: Int = 18,
    val au: Int = 4,
    val majorRequired: Map<Int, Int> = emptyMap(),
    val majorElective: Map<Int, Int> = emptyMap(),
) {
    fun majorRequired(department: Department): Int = majorRequired[department.id] ?: 19
    fun majorElective(department: Department): Int = majorElective[department.id] ?: 24

    val isValid: Boolean get() = listOf(graduation, basicRequired, basicElective, hseCore, hseGeneral, au)
        .plus(majorRequired.values).plus(majorElective.values).all { it >= 0 }
}
