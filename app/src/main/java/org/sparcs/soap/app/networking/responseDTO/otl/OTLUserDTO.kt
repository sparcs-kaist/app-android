package org.sparcs.soap.app.networking.responseDTO.otl

import com.google.gson.annotations.SerializedName
import org.sparcs.soap.app.domain.models.otl.OTLUser

data class OTLUserDTO(
    @SerializedName("id")
    val id: Int,

    @SerializedName("name")
    val name: String,

    @SerializedName("mail")
    val email: String,

    @SerializedName("studentNumber")
    val studentNumber: Int,

    // Nullable on the server (`string | null`); Gson ignores Kotlin
    // non-null types, so a null here used to crash `toModel()`.
    @SerializedName("degree")
    val degree: String?,

    @SerializedName("majorDepartments")
    val majorDepartments: List<DepartmentDTO>?,

    @SerializedName("interestedDepartments")
    val interestedDepartments: List<DepartmentDTO>?,
) {
    fun toModel(): OTLUser = OTLUser(
        id = id,
        name = name,
        email = email,
        studentNumber = studentNumber,
        degree = degree,
        majorDepartments = majorDepartments.orEmpty().map { it.toModel() },
        interestedDepartments = interestedDepartments.orEmpty().map { it.toModel() }
    )
}