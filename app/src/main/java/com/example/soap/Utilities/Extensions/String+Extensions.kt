package com.example.soap.Utilities.Extensions

import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.TimeZone

fun String.toDate(): Date? {
    val simpleDateFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'",
        Locale.US)

    simpleDateFormat.timeZone = TimeZone.getTimeZone("UTC")

    return try {
        simpleDateFormat.parse(this)
    } catch (e: ParseException) {
        null
    } catch (e: Exception) {
        null
    }
}