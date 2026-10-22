package org.sparcs.soap.app.networking.responseDTO

import com.google.gson.Gson
import com.google.gson.JsonObject
import org.sparcs.soap.app.domain.error.NetworkError
import org.sparcs.soap.app.domain.helpers.NetworkErrorMapper
import retrofit2.HttpException
import timber.log.Timber

suspend inline fun <T> safeApiCall(
    gson: Gson,
    crossinline call: suspend () -> T
): T {
    return try {
        call()
    } catch (e: Exception) {
        handleApiError(gson, e)
    }
}

fun handleApiError(gson: Gson, exception: Exception): Nothing {
    if (exception !is HttpException) throw NetworkErrorMapper.map(exception)

    val response = exception.response()
    val code = response?.code() ?: 500
    val errorBody = try { response?.errorBody()?.string() } catch (_: Exception) { null }

    if (code == 401) throw NetworkError.Unauthorized()

    var errorMessage: String? = null
    if (!errorBody.isNullOrEmpty()) {
        try {
            val json = gson.fromJson(errorBody, JsonObject::class.java)
            if (json.has("detail")) {
                val detail = json.get("detail")
                if (detail.isJsonObject) {
                    val detailObj = detail.asJsonObject
                    errorMessage = if (detailObj.has("error") && detailObj.get("error").isJsonObject) {
                        detailObj.getAsJsonObject("error").get("message")?.asString
                    } else {
                        detailObj.get("message")?.asString
                    }
                } else {
                    errorMessage = detail.asString
                }
            } else if (json.has("error") && json.get("error").isJsonPrimitive) {
                errorMessage = json.get("error").asString
            }
        } catch (e: Exception) {
            Timber.e(e, "Error parsing error body")
        }
    }

    throw NetworkError.ServerError(code, errorMessage)
}