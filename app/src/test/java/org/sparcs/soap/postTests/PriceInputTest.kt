package org.sparcs.soap.postTests

import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.input.TextFieldValue
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test
import org.sparcs.soap.app.features.postCompose.PriceInput
import java.util.Locale

class PriceInputTest {

    private val input = PriceInput(Locale.US)
    private var field = TextFieldValue("")
    private var value: Long? = null

    private fun edit(start: Int, end: Int, replacement: String) {
        val text = field.text.replaceRange(start, end, replacement)
        val (newValue, newField) = input.edit(field, TextFieldValue(text, TextRange(start + replacement.length)))
        value = newValue
        field = newField
    }

    private fun type(digits: String) = digits.forEach { edit(field.text.length, field.text.length, it.toString()) }

    private fun backspace() = edit(field.text.length - 1, field.text.length, "")

    @Test
    fun groupsDigitsAsTheyAreTyped() {
        type("1250000")
        assertEquals("1,250,000", field.text)
        assertEquals(1_250_000L, value)
        assertEquals(TextRange(field.text.length), field.selection)
    }

    @Test
    fun regroupsAfterADelete() {
        type("1250000")
        backspace()
        backspace()
        assertEquals("12,500", field.text)
        assertEquals(12_500L, value)
    }

    @Test
    fun clearingLeavesNoPrice() {
        type("7")
        backspace()
        assertEquals("", field.text)
        assertNull(value)
    }

    @Test
    fun ignoresAnythingButAsciiDigits() {
        type("12")
        edit(2, 2, "a.-٣３")
        assertEquals("12", field.text)
        assertEquals(12L, value)
        assertEquals(TextRange(2), field.selection)
    }

    @Test
    fun capsTheNumberOfDigits() {
        type("12345678901234")
        assertEquals(123_456_789_012L, value)
        assertEquals("123,456,789,012", field.text)
    }

    @Test
    fun keepsTheCursorAmongTheSameDigits() {
        type("125000")
        edit(2, 2, "9")
        assertEquals("1,295,000", field.text)
        assertEquals(TextRange(4), field.selection)
    }

    @Test
    fun deletingASeparatorKeepsTheSameDigitsAfterTheCursor() {
        type("1250000")
        edit(1, 2, "")
        assertEquals("1,250,000", field.text)
        assertEquals(TextRange(2), field.selection)
    }

    @Test
    fun pastingAFormattedNumberKeepsOnlyItsDigits() {
        edit(0, 0, "₩ 3,000원")
        assertEquals(3_000L, value)
        assertEquals("3,000", field.text)
    }

    @Test
    fun movingTheCursorKeepsTheText() {
        type("1000")
        val moved = field.copy(selection = TextRange(1))
        val (newValue, newField) = input.edit(field, moved)
        assertEquals(1_000L, newValue)
        assertEquals(moved, newField)
    }

    @Test
    fun showsAnExistingPrice() {
        assertEquals("999,999,999,999", input.field(999_999_999_999).text)
        assertEquals("", input.field(null).text)
    }
}
