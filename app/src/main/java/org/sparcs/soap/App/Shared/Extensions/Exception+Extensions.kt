package org.sparcs.soap.App.Shared.Extensions

import java.io.IOException
import java.net.ConnectException
import java.net.SocketTimeoutException
import java.net.UnknownHostException

fun Exception.isNetworkError(): Boolean {
    return when (this) {
        is UnknownHostException,
        is SocketTimeoutException,
        is ConnectException -> true
        is IOException -> {
            val msg = message?.lowercase() ?: ""
            listOf("network", "host", "connection", "timeout").any { msg.contains(it) }
        }
        else -> false
    }
}