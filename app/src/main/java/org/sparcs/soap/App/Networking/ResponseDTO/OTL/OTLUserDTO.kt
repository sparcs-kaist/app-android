package org.sparcs.soap.App.Networking.ResponseDTO.OTL

import com.google.gson.annotations.SerializedName
import org.sparcs.soap.App.Domain.Models.OTL.OTLUser

data class OTLUserDTO(
    @SerializedName("id")
    val id: Int,

    @SerializedName("name")
    val name: String,

    @SerializedName("mail")
    val email: String,

    @SerializedName("studentNumber")
    val studentNumber: Int,

    @SerializedName("degree")
    val degree: String,

    @SerializedName("majorDepartments")
    val majorDepartments: List<DepartmentDTO>,

    @SerializedName("interestedDepartments")
    val interestedDepartments: List<DepartmentDTO>,
) {
    fun toModel(): OTLUser = OTLUser(
        id = id,
        name = name,
        email = email,
        studentNumber = studentNumber,
        degree = degree,
        majorDepartments = majorDepartments.map { it.toModel() },
        interestedDepartments = interestedDepartments.map { it.toModel() }
    )
}