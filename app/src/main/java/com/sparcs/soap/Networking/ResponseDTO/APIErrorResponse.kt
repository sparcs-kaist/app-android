package com.sparcs.soap.Networking.ResponseDTO

import com.google.gson.Gson
import com.google.gson.annotations.SerializedName
import retrofit2.HttpException

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

fun handleApiError(gson: Gson, exception: Exception): Nothing {
    if (exception is HttpException) {
        val errorBody = exception.response()?.errorBody()?.string()
        if (errorBody != null) {
            val parsedError = gson.fromJson(errorBody, ApiErrorResponse::class.java)
            throw parsedError.toDomainError()
        } else {
            throw exception
        }
    } else {
        throw exception
    }
}
