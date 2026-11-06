package org.sparcs.soap.app.features.settings.timetable.components

import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import org.sparcs.soap.app.theme.ui.Theme
import android.graphics.Color as AndroidColor

/**
 * A grid of ready-made colours, like SwiftUI's colour picker: a row of greys from white to black,
 * then a column per hue running from dark at the top to pale at the bottom.
 */
object ColorGridPalette {
    /** Hues from cyan round through blue, purple, red, orange and yellow to green. */
    private val hues = listOf(200f, 220f, 250f, 280f, 330f, 5f, 20f, 35f, 45f, 58f, 72f, 100f)

    /** Saturation and brightness per shaded row, dark to pale. */
    private val shades = listOf(
        0.80f to 0.28f, 0.82f to 0.42f, 0.84f to 0.58f, 0.82f to 0.74f, 0.80f to 0.90f,
        0.66f to 1.00f, 0.48f to 1.00f, 0.32f to 1.00f, 0.16f to 1.00f,
    )

    val columns = hues.size

    val rows: List<List<Color>> = buildList {
        add(List(columns) { i -> Color(1f - i / (columns - 1f), 1f - i / (columns - 1f), 1f - i / (columns - 1f)) })
        shades.forEach { (s, v) ->
            add(hues.map { h -> Color(AndroidColor.HSVToColor(floatArrayOf(h, s, v))) })
        }
    }
}

@Composable
fun ColorGrid(selected: Color, onSelect: (Color) -> Unit, modifier: Modifier = Modifier) {
    val selectedArgb = selected.toArgb()
    Column(
        modifier
            .fillMaxWidth()
            .clip(MaterialTheme.shapes.medium)
            .selectableGroup()
    ) {
        ColorGridPalette.rows.forEach { row ->
            Row {
                row.forEach { color ->
                    val isSelected = color.toArgb() == selectedArgb
                    // A ring that shows on light and dark cells alike.
                    val ring = if (color.luminance() > 0.5f) Color.Black else Color.White
                    Box(
                        Modifier
                            .weight(1f)
                            .aspectRatio(1f)
                            .drawBehind { drawRect(color) }
                            .then(if (isSelected) Modifier.padding(2.dp).border(2.dp, ring, RectangleShape) else Modifier)
                            .semantics { contentDescription = "#%06X".format(color.toArgb() and 0xFFFFFF) }
                            .selectable(selected = isSelected, role = Role.RadioButton) { onSelect(color) }
                    )
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun ColorGridPreview() {
    Theme { ColorGrid(selected = ColorGridPalette.rows[5][5], onSelect = {}, modifier = Modifier.width(300.dp)) }
}
