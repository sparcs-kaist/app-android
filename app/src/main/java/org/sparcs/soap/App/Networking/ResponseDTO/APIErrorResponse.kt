package org.sparcs.soap.App.Networking.ResponseDTO

import com.google.gson.Gson
import com.google.gson.JsonObject
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
    if (exception !is HttpException) throw exception

    val response = exception.response()
    val errorBody = response?.errorBody()?.string()

    if (errorBody.isNullOrBlank()) throw exception

    try {
        val parsedError = gson.fromJson(errorBody, ApiErrorResponse::class.java)
        if (parsedError?.error != null) throw ApiException(parsedError.error)
    } catch (_: Exception) { }

    try {
        val json = gson.fromJson(errorBody, JsonObject::class.java)
        if (json.has("detail")) {
            val detail = json.get("detail")
            val message = if (detail.isJsonObject) {
                val detailObj = detail.asJsonObject
                val errorObj = if (detailObj.has("error")) detailObj.getAsJsonObject("error") else detailObj
                errorObj.get("message")?.asString
            } else {
                detail.asString
            }
            if (message != null) throw ApiException(message)
        }
    } catch (_: Exception) { }

    try {
        val parsedArray = gson.fromJson(errorBody, Array<String>::class.java)
        if (parsedArray != null && parsedArray.isNotEmpty()) {
            throw ApiException(parsedArray[0])
        }
    } catch (_: Exception) { }

    throw exception
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