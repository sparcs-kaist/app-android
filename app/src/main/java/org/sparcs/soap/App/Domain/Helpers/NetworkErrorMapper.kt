package org.sparcs.soap.App.Domain.Helpers

import org.sparcs.soap.App.Domain.Error.NetworkError
import retrofit2.HttpException
import java.net.ConnectException
import java.net.SocketTimeoutException
import java.net.UnknownHostException

object NetworkErrorMapper {
    fun map(throwable: Throwable): NetworkError {
        return when (throwable) {
            is NetworkError -> throwable

            is UnknownHostException,
            is ConnectException -> NetworkError.NoConnection

            is SocketTimeoutException -> NetworkError.Timeout

            is HttpException -> {
                when (throwable.code()) {
                    401 -> NetworkError.Unauthorized
                    404 -> NetworkError.NotFound
                    else -> NetworkError.ServerError(throwable.code())
                }
            }

            else -> NetworkError.Unknown(throwable)
        }
    }
}