package org.sparcs.soap.app.features.postCompose

import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.input.TextFieldValue
import java.text.NumberFormat
import java.util.Locale

class PriceInput(private val locale: Locale = Locale.getDefault(), private val maxDigits: Int = MAX_DIGITS) {

    fun text(value: Long?): String = value?.let { NumberFormat.getIntegerInstance(locale).format(it) }.orEmpty()

    fun field(value: Long?): TextFieldValue = text(value).let { TextFieldValue(it, TextRange(it.length)) }

    fun edit(current: TextFieldValue, edited: TextFieldValue): Pair<Long?, TextFieldValue> {
        val digits = edited.text.filter(::isAsciiDigit).take(maxDigits)
        val value = digits.toLongOrNull()
        if (edited.text == current.text) return value to edited
        val text = text(value)
        val digitsAfter = edited.text.substring(edited.selection.end.coerceIn(0, edited.text.length)).count(::isAsciiDigit)
        val keptAfter = digitsAfter.coerceAtMost(digits.length)
        return value to TextFieldValue(text, TextRange(cursor(text, keptAfter)))
    }

    private fun cursor(text: String, digitsAfter: Int): Int {
        var position = text.length
        var remaining = digitsAfter
        while (remaining > 0 && position > 0) {
            position--
            if (isAsciiDigit(text[position])) remaining--
        }
        return position
    }

    companion object {
        const val MAX_DIGITS = 12

        fun isAsciiDigit(char: Char): Boolean = char in '0'..'9'
    }
}
