package org.sparcs.soap.App.Networking.ResponseDTO

import com.google.gson.Gson
import org.sparcs.soap.App.Domain.Error.NetworkError
import org.sparcs.soap.App.Domain.Helpers.NetworkErrorMapper
import retrofit2.HttpException

object AuthRetryConfig {
    var tokenRefresher: (suspend () -> Unit)? = null
    var isRefreshing = false
}

suspend inline fun <T> safeApiCall(
    gson: Gson,
    crossinline call: suspend () -> T
): T {
    return try {
        call()
    } catch (e: Exception) {
        handleApiError(gson, e) { call() }
    }
}

suspend fun <T> handleApiError(
    gson: Gson,
    exception: Exception,
    call: suspend () -> T
): T {
    if (exception !is HttpException) throw NetworkErrorMapper.map(exception)

    val response = exception.response()
    val code = response?.code() ?: 500
    val errorBody = try { response?.errorBody()?.string() } catch (e: Exception) { null }

    if (code == 401) {
        val refresher = AuthRetryConfig.tokenRefresher
        if (AuthRetryConfig.isRefreshing || refresher == null) {
            throw NetworkError.Unauthorized
        }
        return try {
            AuthRetryConfig.isRefreshing = true
            refresher()
            AuthRetryConfig.isRefreshing = false
            call()
        } catch (e: Exception) {
            AuthRetryConfig.isRefreshing = false
            throw NetworkErrorMapper.map(e)
        }
    }

    var errorMessage: String? = null
    if (!errorBody.isNullOrEmpty()) {
        try {
            val json = gson.fromJson(errorBody, com.google.gson.JsonObject::class.java)
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
            e.printStackTrace()
        }
    }

    throw NetworkError.ServerError(code, errorMessage)
}