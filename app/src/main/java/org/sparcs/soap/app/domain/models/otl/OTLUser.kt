package org.sparcs.soap.app.domain.models.otl

data class OTLUser(
    val id: Int,
    val name: String,
    val email: String,
    val studentNumber: Int,
    /** `null` when OTL has no degree on record. */
    val degree: String?,
    val majorDepartments: List<Department>,
    val interestedDepartments: List<Department>
){
    companion object
}