package org.sparcs.soap.app.domain.helpers.tiptap

internal object HtmlEntities {
    private val named = mapOf(
        "amp" to "&",
        "lt" to "<",
        "gt" to ">",
        "quot" to "\"",
        "apos" to "'",
        "nbsp" to " ",
    )

    private const val MAX_REFERENCE_LENGTH = 10

    fun decode(string: String): String {
        if ('&' !in string) return string

        val result = StringBuilder(string.length)
        var index = 0
        while (index < string.length) {
            val semicolon = if (string[index] == '&') {
                string.indexOf(';', index).takeIf { it in index + 1..index + MAX_REFERENCE_LENGTH }
            } else null
            val decoded = semicolon?.let { decodeReference(string.substring(index + 1, it)) }
            if (semicolon != null && decoded != null) {
                result.append(decoded)
                index = semicolon + 1
            } else {
                result.append(string[index])
                index++
            }
        }
        return result.toString()
    }

    private fun decodeReference(name: String): String? {
        named[name]?.let { return it }
        if (!name.startsWith("#")) return null

        val digits = name.drop(1)
        val codePoint = if (digits.startsWith("x", ignoreCase = true)) {
            digits.drop(1).toIntOrNull(16)
        } else {
            digits.toIntOrNull(10)
        }
        return codePoint
            ?.takeIf { Character.isValidCodePoint(it) && it !in Char.MIN_SURROGATE.code..Char.MAX_SURROGATE.code }
            ?.let { String(Character.toChars(it)) }
    }
}
