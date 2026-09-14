package org.sparcs.soap.app.features.settings.timetable

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import org.sparcs.soap.R
import org.sparcs.soap.app.domain.helpers.TimetableTheme
import kotlin.math.roundToInt

internal fun Color.hex(): String = "%06X".format(toArgb() and 0xFFFFFF)

@Composable
internal fun ThemeColorDialog(initial: String, onDismiss: () -> Unit, onApply: (String) -> Unit, onRemove: (() -> Unit)?) {
    var hex by rememberSaveable { mutableStateOf(initial) }
    var validHex by rememberSaveable { mutableStateOf(initial) }
    val valid = hex.matches(Regex("[0-9A-Fa-f]{6}"))
    val value = validHex.toInt(16)
    AlertDialog(onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.theme_change_color)) },
        text = {
            Column(Modifier.verticalScroll(rememberScrollState()), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Box(Modifier.fillMaxWidth().height(64.dp).background(TimetableTheme.color(validHex), MaterialTheme.shapes.medium))
                OutlinedTextField(hex, { input ->
                    hex = input.removePrefix("#").take(6).uppercase()
                    if (hex.matches(Regex("[0-9A-F]{6}"))) validHex = hex
                }, Modifier.fillMaxWidth(), label = { Text(stringResource(R.string.theme_hex)) }, prefix = { Text("#") }, singleLine = true, isError = !valid,
                    supportingText = { if (!valid) Text(stringResource(R.string.theme_hex_error)) })
                listOf(R.string.theme_red, R.string.theme_green, R.string.theme_blue).forEachIndexed { index, res ->
                    val shift = (2 - index) * 8
                    val channel = (value shr shift) and 255
                    val label = stringResource(res)
                    Text("$label · $channel", style = MaterialTheme.typography.labelMedium)
                    Slider(channel.toFloat(), { number ->
                        val updated = (value and (255 shl shift).inv()) or (number.roundToInt() shl shift)
                        hex = "%06X".format(updated); validHex = hex
                    }, valueRange = 0f..255f, modifier = Modifier.fillMaxWidth().semantics { contentDescription = label })
                }
                if (onRemove != null) TextButton(onClick = onRemove) { Text(stringResource(R.string.theme_remove_color), color = MaterialTheme.colorScheme.error) }
            }
        },
        confirmButton = { TextButton(onClick = { onApply(hex.uppercase()) }, enabled = valid) { Text(stringResource(R.string.theme_apply)) } },
        dismissButton = { TextButton(onClick = onDismiss) { Text(stringResource(R.string.cancel)) } }
    )
}
