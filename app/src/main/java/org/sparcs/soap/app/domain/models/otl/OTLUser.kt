package org.sparcs.soap.app.domain.models.otl

data class OTLUser(
    val id: Int,
    val name: String,
    val email: String,
    val studentNumber: Int,
    val degree: String,
    val majorDepartments: List<Department>,
    val interestedDepartments: List<Department>
){
    companion object
}