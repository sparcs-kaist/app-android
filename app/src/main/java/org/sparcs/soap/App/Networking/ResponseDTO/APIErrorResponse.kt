package org.sparcs.soap.App.Networking.ResponseDTO

import com.google.gson.Gson
import com.google.gson.annotations.SerializedName
import org.sparcs.soap.App.Domain.Error.NetworkError
import org.sparcs.soap.App.Domain.Helpers.NetworkErrorMapper
import retrofit2.HttpException
import timber.log.Timber

data class ApiErrorResponse(
    @SerializedName("error")
    val error: String
) : Exception() {

    override val message: String
        get() = error
}

class ApiException(override val message: String) : Exception(message)


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
    val errorBody = response?.errorBody()?.string()

    if (code == 401) {
        val refresher = AuthRetryConfig.tokenRefresher

        if (AuthRetryConfig.isRefreshing || refresher == null) {
            Timber.e("Unauthorized: Skipping retry (isRefreshing: ${AuthRetryConfig.isRefreshing})")
            throw NetworkError.Unauthorized
        }

        return try {
            AuthRetryConfig.isRefreshing = true
            Timber.d("401 detected: Refreshing token...")

            refresher()

            AuthRetryConfig.isRefreshing = false
            call()
        } catch (e: Exception) {
            AuthRetryConfig.isRefreshing = false
            throw NetworkErrorMapper.map(e)
        }
    }
    if (errorBody.isNullOrEmpty()) throw NetworkError.ServerError(code)

    try {
        val json = gson.fromJson(errorBody, com.google.gson.JsonObject::class.java)
        if (json.has("detail")) {
            val detail = json.get("detail")
            val message = if (detail.isJsonObject) {
                val detailObj = detail.asJsonObject
                if (detailObj.has("error")) {
                    detailObj.getAsJsonObject("error").get("message")?.asString
                } else {
                    detailObj.get("message")?.asString
                }
            } else {
                detail.asString
            }
            if (!message.isNullOrEmpty()) throw ApiException(message)
        }
    } catch (e: Exception) { if (e is ApiException) throw e }

    try {
        val parsedError = gson.fromJson(errorBody, ApiErrorResponse::class.java)
        throw ApiException(parsedError.error)
    } catch (e: Exception) { if (e is ApiException) throw e }

    try {
        val parsedArray = gson.fromJson(errorBody, Array<String>::class.java)
        if (parsedArray.isNotEmpty()) throw ApiException(parsedArray[0])
    } catch (e: Exception) { if (e is ApiException) throw e }

    throw NetworkError.ServerError(code)
}