package org.sparcs.soap.App.Networking.ResponseDTO.OTL

import com.google.gson.annotations.SerializedName
import org.sparcs.soap.App.Domain.Models.OTL.OTLUser

data class OTLUserDTO(
    @SerializedName("id")
    val id: Int,

    @SerializedName("name")
    val name: String,

    @SerializedName("mail")
    val mail: String,

    @SerializedName("studentNumber")
    val studentNumber: Number,

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
        email = mail,
        studentNumber = studentNumber,
        majors = majorDepartments.map { it.toModel() },
    )
}