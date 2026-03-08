package org.sparcs.soap.App.Shared.Extensions

import android.content.Context
import android.util.Patterns
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.style.TextDecoration
import dagger.hilt.android.qualifiers.ApplicationContext
import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.TimeZone
import javax.inject.Inject

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

fun String.toHTMLParagraphs(): String =
    this.split("\n")
        .joinToString(separator = "") { "<p>$it</p>" }

//UseCase 범주에서 stringResource 지원
interface StringProvider {
    fun get(id: Int, vararg args: Any): String
}

class AndroidStringProvider @Inject constructor(
    @ApplicationContext private val context: Context
) : StringProvider {
    override fun get(id: Int, vararg args: Any): String {
        return context.getString(id, *args)
    }
}

@Composable
fun String.postfixEuroRo(): String {
    if (this.isEmpty()) return this

    val isEn = LocalConfiguration.current.locales[0].language
    if(isEn == "en") return this

    val lastChar = this.last()
    val code = lastChar.code
    val base = code - 0xAC00
    val jong = base % 28

    return this + when (jong) {
        0 -> "로"
        8 -> "로"
        else -> "으로"
    }
}

fun String.escapeHash(): String {
    return this.replace("#", "%23")
}

fun String.unescapeHash(): String {
    return this.replace("%23", "#")
}

fun String.toPhoneNumberFormat(): String {
    val digits = this.filter { it.isDigit() }
    return buildString {
        digits.forEachIndexed { index, c ->
            append(c)
            if (digits.length <= 10) {
                if (index == 2 || index == 5) append('-')
            } else {
                if (index == 2 || index == 6) append('-')
            }
        }
    }.trimEnd('-')
}

fun String.isUpdateRequired(min: String): Boolean {
    val currentParts = this.split(".").map { it.toIntOrNull() ?: 0 }
    val minParts = min.split(".").map { it.toIntOrNull() ?: 0 }

    val length = maxOf(currentParts.size, minParts.size)
    for (i in 0 until length) {
        val c = currentParts.getOrElse(i) { 0 }
        val m = minParts.getOrElse(i) { 0 }
        if (c < m) return true
        if (c > m) return false
    }
    return false
}

fun String.toDetectedAnnotatedString(linkColor: Color): AnnotatedString {
    return buildAnnotatedString {
        append(this@toDetectedAnnotatedString)
        val matcher = Patterns.WEB_URL.matcher(this@toDetectedAnnotatedString)
        while (matcher.find()) {
            val start = matcher.start()
            val end = matcher.end()
            val url = matcher.group() ?: continue

            addStringAnnotation(
                tag = "URL",
                annotation = url,
                start = start,
                end = end
            )
            addStyle(
                style = SpanStyle(
                    color = linkColor,
                    textDecoration = TextDecoration.Underline
                ),
                start = start,
                end = end
            )
        }
    }
}