package com.example.soap.Networking.ResponseDTO

import com.google.gson.annotations.SerializedName

data class ApiErrorResponse(
    @SerializedName("error")
    val error: String,
    @SerializedName("message")
    val errorDescription: String?
) {
    fun toDomainError(): DomainException {
        return DomainException(errorDescription ?: "Unknown API error", error)
    }
}

data class DomainException(
    override val message: String,
    val errorDescription: String? = null,
    val httpStatusCode: Int? = null
) : Exception(message)
