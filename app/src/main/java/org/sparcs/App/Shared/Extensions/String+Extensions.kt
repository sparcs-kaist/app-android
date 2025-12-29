package org.sparcs.App.Shared.Extensions

import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalConfiguration
import dagger.hilt.android.qualifiers.ApplicationContext
import java.net.URLEncoder
import java.nio.charset.StandardCharsets
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

val String.urlEscaped: String
    get() = URLEncoder.encode(this, StandardCharsets.UTF_8.toString())

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