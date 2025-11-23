package com.sparcs.soap.Networking.ResponseDTO

import com.google.gson.Gson
import com.google.gson.JsonSyntaxException
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
        if (!errorBody.isNullOrEmpty()) {
            try {
                val parsedError = gson.fromJson(errorBody, ApiErrorResponse::class.java)
                throw parsedError.toDomainError()
            } catch (ex: JsonSyntaxException) {
                try {
                    val parsedArray = gson.fromJson(errorBody, Array<String>::class.java)
                    if (parsedArray.isNotEmpty()) {
                        throw ApiException(parsedArray[0])
                    } else {
                        throw exception
                    }
                } catch (nestedEx: Exception) {
                    throw exception
                }
            }
        } else {
            throw exception
        }
    } else {
        throw exception
    }
}

class ApiException(override val message: String) : Exception(message)

fun parseReportCommentError(exception: Exception): Exception {
    if (exception !is HttpException) return exception

    val body = exception.response()?.errorBody()?.string().orEmpty()
    if (body.isBlank()) return exception

    return try {
        val messages = Gson().fromJson(body, Array<String>::class.java)
        if (messages.isNotEmpty()) Exception(messages[0]) else exception
    } catch (_: Exception) {
        exception
    }
}