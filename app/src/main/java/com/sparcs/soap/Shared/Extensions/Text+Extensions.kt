package com.sparcs.soap.Shared.Extensions

import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.OffsetMapping
import androidx.compose.ui.text.input.TransformedText
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.TextUnit
import com.sparcs.soap.Domain.Helpers.LocalizedString

@Composable
fun LocalizedText(
    text: LocalizedString,
    style: TextStyle = TextStyle.Default,
    modifier: Modifier = Modifier,
    color: Color = Color.Unspecified,
    fontSize: TextUnit = TextUnit.Unspecified,
    fontFamily: FontFamily? = null,
    maxLines: Int = Int.MAX_VALUE,
    overflow: TextOverflow = TextOverflow.Clip,
    fontWeight: FontWeight? = null
) {
    Text(
        text = text.localized(),
        style = style,
        modifier = modifier,
        color = color,
        fontSize = fontSize,
        fontFamily = fontFamily,
        maxLines = maxLines,
        overflow = overflow,
        fontWeight = fontWeight
    )
}

class PhoneNumberVisualTransformation : VisualTransformation {
    override fun filter(text: AnnotatedString): TransformedText {
        val digits = text.text.filter { it.isDigit() }

        val formatted = buildString {
            digits.forEachIndexed { index, c ->
                append(c)
                if (index == 2 || index == 6) append('-') // 010-1234-5678 형태
            }
        }

        val hyphenPositions = listOf(3, 8) // 하이픈이 들어간 위치 (0-based)

        val offsetMapping = object : OffsetMapping {
            override fun originalToTransformed(offset: Int): Int {
                var transformedOffset = offset
                for (pos in hyphenPositions) {
                    if (offset > pos - hyphenPositions.indexOf(pos)) {
                        transformedOffset++
                    }
                }
                return transformedOffset.coerceIn(0, formatted.length)
            }

            override fun transformedToOriginal(offset: Int): Int {
                var originalOffset = offset
                for (pos in hyphenPositions) {
                    if (offset > pos) {
                        originalOffset--
                    }
                }
                return originalOffset.coerceIn(0, digits.length)
            }
        }

        return TransformedText(AnnotatedString(formatted), offsetMapping)
    }
}
