package org.sparcs.soap.app.features.timetable.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.RoundRect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.clipPath
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp

@Composable
fun TentativeBlock(color: Color, modifier: Modifier = Modifier) {
    Canvas(modifier) { drawTentativeBlock(color) }
}

internal fun DrawScope.drawTentativeBlock(
    color: Color,
    topLeft: Offset = Offset.Zero,
    blockSize: Size = size,
    showsStripes: Boolean = true,
) {
    val radius = CornerRadius(4.dp.toPx())
    val path = Path().apply {
        addRoundRect(RoundRect(topLeft.x, topLeft.y, topLeft.x + blockSize.width, topLeft.y + blockSize.height, radius))
    }
    clipPath(path) {
        drawRoundRect(color.copy(alpha = 0.2f), topLeft, blockSize, radius)
        if (showsStripes) {
            var x = -blockSize.height
            while (x < blockSize.width) {
                drawLine(color.copy(alpha = 0.3f), topLeft + Offset(x, blockSize.height), topLeft + Offset(x + blockSize.height, 0f), 2.dp.toPx())
                x += 7.dp.toPx()
            }
        }
        drawRoundRect(color, topLeft, blockSize, radius, style = Stroke(1.5.dp.toPx(), pathEffect = PathEffect.dashPathEffect(floatArrayOf(4.dp.toPx(), 3.dp.toPx()))))
    }
}

@Preview(showBackground = true)
@Composable
private fun TentativeBlockPreview() { TentativeBlock(Color(0xFF6581CC), Modifier.size(88.dp, 105.dp)) }
