package org.sparcs.soap.App.Domain.Models.OTL

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