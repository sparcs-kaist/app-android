package com.example.soap.Shared.Extensions

import java.net.URLEncoder
import java.nio.charset.StandardCharsets
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

val String.urlEscaped: String
    get() = URLEncoder.encode(this, StandardCharsets.UTF_8.toString())

fun String.toHTMLParagraphs(): String =
    this.split("\n")
        .joinToString(separator = "") { "<p>$it</p>" }
