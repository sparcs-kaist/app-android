package com.example.soap.Networking.ResponseDTO

import com.google.gson.annotations.SerializedName

data class ApiErrorResponse(
    @SerializedName("error")
    val error: String
) : Exception() {

    override val message: String
        get() = error

    fun toDomainError(): Exception {
        return Exception(error)
    }
}
