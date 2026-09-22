package org.sparcs.soap.app.features.settings.timetable.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.graphics.toColorInt
import org.sparcs.soap.R
import org.sparcs.soap.app.theme.ui.Theme
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.roundToInt
import kotlin.math.sin
import android.graphics.Color as AndroidColor

@Composable
fun AdvancedColorPicker(
    initialHex: String,
    onDismiss: () -> Unit,
    onApply: (String) -> Unit,
    onRemove: (() -> Unit)? = null,
) {
    var hex by rememberSaveable { mutableStateOf(initialHex.uppercase()) }
    val color = remember(hex) {
        try {
            Color("#$hex".toColorInt())
        } catch (_: Exception) {
            Color.Black
        }
    }

    var selectedTab by rememberSaveable { mutableIntStateOf(0) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.theme_change_color)) },
        shape = RoundedCornerShape(24.dp),
        containerColor = MaterialTheme.colorScheme.background,
        text = {
            Column(
                Modifier.verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {

                Box(
                    Modifier
                        .fillMaxWidth()
                        .height(60.dp)
                        .clip(RoundedCornerShape(16.dp))
                        .background(color)
                        .border(1.dp, MaterialTheme.colorScheme.outlineVariant, RoundedCornerShape(16.dp))
                )

                HsvPickerComponent(
                    color = color,
                    onColorChange = { newColor -> hex = newColor.toHex() }
                )

                OutlinedTextField(
                    value = hex,
                    onValueChange = { input ->
                        val cleaned = input.removePrefix("#").take(6).uppercase()
                        if (cleaned.matches(Regex("[0-9A-F]{0,6}"))) {
                            hex = cleaned
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text(stringResource(R.string.theme_hex)) },
                    prefix = { Text(stringResource(R.string.theme_hex_prefix)) },
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp)
                )

                TabRow(
                    selectedTabIndex = selectedTab,
                    containerColor = Color.Transparent,
                    divider = {}
                ) {
                    Tab(selected = selectedTab == 0, onClick = { selectedTab = 0 }) {
                        Text(stringResource(R.string.theme_color_hsb), Modifier.padding(8.dp))
                    }
                    Tab(selected = selectedTab == 1, onClick = { selectedTab = 1 }) {
                        Text(stringResource(R.string.theme_color_rgb), Modifier.padding(8.dp))
                    }
                }

                when (selectedTab) {
                    0 -> HsbSliders(color) { newColor -> hex = newColor.toHex() }
                    1 -> RgbSliders(color) { newColor -> hex = newColor.toHex() }
                }

                if (onRemove != null) {
                    TextButton(
                        onClick = onRemove,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            stringResource(R.string.theme_remove_color),
                            color = MaterialTheme.colorScheme.error
                        )
                    }
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = { onApply(hex) },
                enabled = hex.length == 6
            ) {
                Text(stringResource(R.string.theme_apply), fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(R.string.cancel))
            }
        }
    )
}

@Composable
fun HsvPickerComponent(
    color: Color,
    onColorChange: (Color) -> Unit,
) {
    val hsv = remember(color) {
        val hsv = FloatArray(3)
        AndroidColor.colorToHSV(color.toArgb(), hsv)
        hsv
    }
    val hue = hsv[0]
    val saturation = hsv[1]
    val value = hsv[2]

    Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
        Box(modifier = Modifier.size(220.dp)) {

            Canvas(modifier = Modifier.fillMaxSize().pointerInput(Unit) {
                detectDragGestures { change, _ ->
                    val center = Offset(size.width / 2f, size.height / 2f)
                    val pos = change.position - center
                    var angle = Math.toDegrees(atan2(pos.y.toDouble(), pos.x.toDouble())).toFloat()
                    if (angle < 0) angle += 360f
                    onColorChange(Color(AndroidColor.HSVToColor(floatArrayOf(angle, saturation, value))))
                }
            }.pointerInput(Unit) {
                detectTapGestures { offset ->
                    val center = Offset(size.width / 2f, size.height / 2f)
                    val pos = offset - center
                    var angle = Math.toDegrees(atan2(pos.y.toDouble(), pos.x.toDouble())).toFloat()
                    if (angle < 0) angle += 360f
                    onColorChange(Color(AndroidColor.HSVToColor(floatArrayOf(angle, saturation, value))))
                }
            }) {
                val radius = size.minDimension / 2
                val thickness = 24.dp.toPx()
                drawCircle(
                    brush = Brush.sweepGradient(
                        colors = List(360) { Color.hsv(it.toFloat(), 1f, 1f) }
                    ),
                    radius = radius - thickness / 2,
                    style = Stroke(width = thickness)
                )

                val angleRad = Math.toRadians(hue.toDouble())
                val handleRadius = radius - thickness / 2
                val handleCenter = Offset(
                    (handleRadius * cos(angleRad)).toFloat() + size.width / 2,
                    (handleRadius * sin(angleRad)).toFloat() + size.height / 2
                )
                drawCircle(Color.White, radius = 10.dp.toPx(), center = handleCenter)
                drawCircle(Color.Black, radius = 10.dp.toPx(), center = handleCenter, style = Stroke(2.dp.toPx()))
            }

            Box(
                modifier = Modifier
                    .size(110.dp)
                    .align(Alignment.Center)
                    .clip(RoundedCornerShape(4.dp))
                    .pointerInput(Unit) {
                        detectDragGestures { change, _ ->
                            val s = (change.position.x / size.width).coerceIn(0f, 1f)
                            val v = (1f - change.position.y / size.height).coerceIn(0f, 1f)
                            onColorChange(Color(AndroidColor.HSVToColor(floatArrayOf(hue, s, v))))
                        }
                    }
                    .pointerInput(Unit) {
                        detectTapGestures { offset ->
                            val s = (offset.x / size.width).coerceIn(0f, 1f)
                            val v = (1f - offset.y / size.height).coerceIn(0f, 1f)
                            onColorChange(Color(AndroidColor.HSVToColor(floatArrayOf(hue, s, v))))
                        }
                    }
            ) {
                Canvas(modifier = Modifier.fillMaxSize()) {

                    drawRect(
                        brush = Brush.horizontalGradient(
                            colors = listOf(Color.White, Color.hsv(hue, 1f, 1f))
                        )
                    )

                    drawRect(
                        brush = Brush.verticalGradient(
                            colors = listOf(Color.Transparent, Color.Black)
                        )
                    )

                    val handleX = saturation * size.width
                    val handleY = (1f - value) * size.height
                    drawCircle(
                        if (value > 0.5f) Color.Black else Color.White,
                        radius = 8.dp.toPx(),
                        center = Offset(handleX, handleY),
                        style = Stroke(2.dp.toPx())
                    )
                }
            }
        }
    }
}

@Composable
fun HsbSliders(color: Color, onColorChange: (Color) -> Unit) {
    val hsv = remember(color) {
        val hsv = FloatArray(3)
        AndroidColor.colorToHSV(color.toArgb(), hsv)
        hsv
    }
    val hue = hsv[0]
    val saturation = hsv[1]
    val value = hsv[2]

    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        ColorSliderWithButtons(
            value = hue,
            range = 0f..360f,
            unit = stringResource(R.string.theme_unit_degrees),
            onValueChange = { onColorChange(Color.hsv(it, saturation, value)) },
            backgroundBrush = Brush.horizontalGradient(
                colors = List(360) { Color.hsv(it.toFloat(), 1f, 1f) }
            )
        )
        ColorSliderWithButtons(
            value = saturation * 100f,
            range = 0f..100f,
            unit = stringResource(R.string.theme_unit_percent),
            onValueChange = { onColorChange(Color.hsv(hue, it / 100f, value)) },
            backgroundBrush = Brush.horizontalGradient(
                colors = listOf(Color.hsv(hue, 0f, value), Color.hsv(hue, 1f, value))
            )
        )
        ColorSliderWithButtons(
            value = value * 100f,
            range = 0f..100f,
            unit = stringResource(R.string.theme_unit_percent),
            onValueChange = { onColorChange(Color.hsv(hue, saturation, it / 100f)) },
            backgroundBrush = Brush.horizontalGradient(
                colors = listOf(Color.Black, Color.hsv(hue, saturation, 1f))
            )
        )
    }
}

@Composable
fun RgbSliders(color: Color, onColorChange: (Color) -> Unit) {
    val r = (color.red * 255).roundToInt()
    val g = (color.green * 255).roundToInt()
    val b = (color.blue * 255).roundToInt()

    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        ColorSliderWithButtons(
            value = r.toFloat(),
            range = 0f..255f,
            onValueChange = { onColorChange(Color(it.roundToInt(), g, b)) },
            backgroundBrush = Brush.horizontalGradient(
                colors = listOf(Color(0, g, b), Color(255, g, b))
            )
        )
        ColorSliderWithButtons(
            value = g.toFloat(),
            range = 0f..255f,
            onValueChange = { onColorChange(Color(r, it.roundToInt(), b)) },
            backgroundBrush = Brush.horizontalGradient(
                colors = listOf(Color(r, 0, b), Color(r, 255, b))
            )
        )
        ColorSliderWithButtons(
            value = b.toFloat(),
            range = 0f..255f,
            onValueChange = { onColorChange(Color(r, g, it.roundToInt())) },
            backgroundBrush = Brush.horizontalGradient(
                colors = listOf(Color(r, g, 0), Color(r, g, 255))
            )
        )
    }
}

@Composable
fun ColorSliderWithButtons(
    value: Float,
    range: ClosedFloatingPointRange<Float>,
    onValueChange: (Float) -> Unit,
    backgroundBrush: Brush,
    unit: String = "",
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text(
            text = stringResource(R.string.theme_color_value_unit, value.roundToInt(), unit),
            modifier = Modifier.width(40.dp),
            style = MaterialTheme.typography.labelMedium,
            fontSize = 12.sp
        )
        
        IconButton(
            onClick = { onValueChange((value - 1f).coerceIn(range)) },
            modifier = Modifier.size(24.dp)
        ) {
            Icon(Icons.Default.Remove, null, modifier = Modifier.size(16.dp))
        }

        Box(
            modifier = Modifier
                .weight(1f)
                .height(20.dp)
                .clip(CircleShape)
                .background(backgroundBrush)
                .border(1.dp, Color.LightGray.copy(alpha = 0.3f), CircleShape)
        ) {
            Slider(
                value = value,
                onValueChange = onValueChange,
                valueRange = range,
                modifier = Modifier.fillMaxSize(),
                colors = SliderDefaults.colors(
                    activeTrackColor = Color.Transparent,
                    inactiveTrackColor = Color.Transparent,
                    thumbColor = Color.White
                )
            )
        }

        IconButton(
            onClick = { onValueChange((value + 1f).coerceIn(range)) },
            modifier = Modifier.size(24.dp)
        ) {
            Icon(Icons.Default.Add, null, modifier = Modifier.size(16.dp))
        }
    }
}

private fun Color.toHex(): String = "%06X".format(toArgb() and 0xFFFFFF)

@Preview(showBackground = true)
@Composable
fun AdvancedColorPickerPreview() {
    var show by remember { mutableStateOf(true) }
    Theme {
        Box(Modifier.fillMaxSize().padding(16.dp)) {
            TextButton(onClick = { show = true }) {
                Text(stringResource(R.string.theme_change_color))
            }
            if (show) {
                AdvancedColorPicker(
                    initialHex = "FF5722",
                    onDismiss = { show = false },
                    onApply = { show = false },
                    onRemove = {}
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun HsbSlidersPreview() {
    Theme {
        Column(Modifier.padding(16.dp).width(300.dp)) {
            HsbSliders(color = Color.Red) {}
        }
    }
}

@Preview(showBackground = true)
@Composable
fun RgbSlidersPreview() {
    Theme {
        Column(Modifier.padding(16.dp).width(300.dp)) {
            RgbSliders(color = Color.Blue) {}
        }
    }
}
