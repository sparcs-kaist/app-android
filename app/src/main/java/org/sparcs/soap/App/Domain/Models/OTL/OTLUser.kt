package org.sparcs.soap.App.Domain.Models.OTL

data class OTLUser(
    val id: Int,
    val name: String,
    val email: String,
    val studentNumber: Number,
    val majors: List<Department>,
){
    companion object
}