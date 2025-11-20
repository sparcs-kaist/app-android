package com.sparcs.soap.Shared.Extensions

import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
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