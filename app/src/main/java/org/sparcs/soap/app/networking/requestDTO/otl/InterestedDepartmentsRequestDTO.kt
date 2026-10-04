package org.sparcs.soap.app.networking.requestDTO.otl

import com.google.gson.annotations.SerializedName

data class InterestedDepartmentsRequestDTO(
    @SerializedName("interestedDepartmentIds") val departmentIDs: List<Int>,
)
