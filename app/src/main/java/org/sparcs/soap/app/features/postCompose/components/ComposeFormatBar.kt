package org.sparcs.soap.app.features.postCompose.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.FormatIndentDecrease
import androidx.compose.material.icons.automirrored.filled.FormatIndentIncrease
import androidx.compose.material.icons.automirrored.filled.FormatListBulleted
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.FormatBold
import androidx.compose.material.icons.filled.FormatColorReset
import androidx.compose.material.icons.filled.FormatColorText
import androidx.compose.material.icons.filled.FormatItalic
import androidx.compose.material.icons.filled.FormatListNumbered
import androidx.compose.material.icons.filled.FormatQuote
import androidx.compose.material.icons.filled.FormatStrikethrough
import androidx.compose.material.icons.filled.FormatUnderlined
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconToggleButton
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.VerticalDivider
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import org.sparcs.soap.R
import org.sparcs.soap.app.features.postCompose.ComposeBlock
import org.sparcs.soap.app.features.postCompose.ComposeBlockStyle
import org.sparcs.soap.app.features.postCompose.ComposeColors
import org.sparcs.soap.app.features.postCompose.ComposeEditorState
import org.sparcs.soap.app.features.postCompose.ComposeList
import org.sparcs.soap.app.features.postCompose.ComposeStyle
import org.sparcs.soap.app.theme.ui.Theme

@Composable
fun ComposeFormatBar(editor: ComposeEditorState, modifier: Modifier = Modifier) {
    val block = editor.focusedText
    val isEnabled = block != null
    val innermostList = block?.lists?.lastOrNull()
    val label = stringResource(R.string.text_formatting)

    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(2.dp),
        modifier = modifier
            .horizontalScroll(rememberScrollState())
            .padding(horizontal = 4.dp)
            .semantics { contentDescription = label }
    ) {
        BlockStyleMenu(block?.style, enabled = isEnabled, onSelect = editor::setBlockStyle)
        BarDivider()
        INLINE_STYLES.forEach { (style, icon, labelRes) ->
            FormatToggle(icon, stringResource(labelRes), style in editor.activeStyles, isEnabled) { editor.toggle(style) }
        }
        ColorMenu(editor.activeColor, enabled = isEnabled, onSelect = editor::setColor)
        BarDivider()
        FormatToggle(Icons.AutoMirrored.Filled.FormatListBulleted, stringResource(R.string.format_bullet_list), innermostList?.isOrdered == false, isEnabled) {
            editor.toggleList(ComposeList.BULLET)
        }
        FormatToggle(Icons.Filled.FormatListNumbered, stringResource(R.string.format_numbered_list), innermostList?.isOrdered == true, isEnabled) {
            editor.toggleList(ComposeList.NUMBERED)
        }
        if (innermostList != null) {
            IconButton(onClick = editor::outdent) {
                Icon(Icons.AutoMirrored.Filled.FormatIndentDecrease, contentDescription = stringResource(R.string.compose_format_outdent))
            }
            IconButton(onClick = editor::indent, enabled = block.lists.size < ComposeList.MAX_DEPTH) {
                Icon(Icons.AutoMirrored.Filled.FormatIndentIncrease, contentDescription = stringResource(R.string.compose_format_indent))
            }
        }
        FormatToggle(Icons.Filled.FormatQuote, stringResource(R.string.format_quote), block?.style == ComposeBlockStyle.QUOTE, isEnabled) {
            editor.setBlockStyle(ComposeBlockStyle.QUOTE)
        }
    }
}

@Composable
private fun BarDivider() {
    VerticalDivider(
        Modifier
            .height(24.dp)
            .padding(horizontal = 4.dp)
    )
}

@Composable
private fun FormatToggle(icon: ImageVector, label: String, checked: Boolean, enabled: Boolean, onClick: () -> Unit) {
    IconToggleButton(checked = checked, onCheckedChange = { onClick() }, enabled = enabled) {
        Icon(icon, contentDescription = label)
    }
}

@Composable
private fun BlockStyleMenu(current: ComposeBlockStyle?, enabled: Boolean, onSelect: (ComposeBlockStyle) -> Unit) {
    var expanded by remember { mutableStateOf(false) }
    val selected = current?.takeIf { it in PARAGRAPH_STYLES.keys } ?: ComposeBlockStyle.PARAGRAPH

    Box {
        TextButton(onClick = { expanded = true }, enabled = enabled) {
            Text(stringResource(PARAGRAPH_STYLES.getValue(selected)))
            Icon(Icons.Filled.ArrowDropDown, contentDescription = null)
        }
        DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
            PARAGRAPH_STYLES.forEach { (style, labelRes) ->
                DropdownMenuItem(
                    text = { Text(stringResource(labelRes), style = menuTextStyle(style)) },
                    trailingIcon = if (style == selected) { { Icon(Icons.Filled.Check, contentDescription = null) } } else null,
                    onClick = {
                        if (style != selected) onSelect(style)
                        expanded = false
                    }
                )
            }
        }
    }
}

@Composable
private fun menuTextStyle(style: ComposeBlockStyle): TextStyle = when (style) {
    ComposeBlockStyle.TITLE -> MaterialTheme.typography.titleLarge
    ComposeBlockStyle.HEADING -> MaterialTheme.typography.titleMedium
    ComposeBlockStyle.SUBHEADING -> MaterialTheme.typography.titleSmall
    ComposeBlockStyle.MONOSPACED -> MaterialTheme.typography.bodyMedium.copy(fontFamily = FontFamily.Monospace)
    else -> MaterialTheme.typography.bodyLarge
}

@Composable
private fun ColorMenu(current: String?, enabled: Boolean, onSelect: (String?) -> Unit) {
    var expanded by remember { mutableStateOf(false) }

    Box {
        IconButton(onClick = { expanded = true }, enabled = enabled) {
            Icon(
                imageVector = Icons.Filled.FormatColorText,
                contentDescription = stringResource(R.string.compose_format_text_color),
                tint = ComposeColors.color(current)?.takeIf { enabled } ?: LocalContentColor.current
            )
        }
        DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
            DropdownMenuItem(
                text = { Text(stringResource(R.string.compose_color_default)) },
                leadingIcon = { Icon(Icons.Filled.FormatColorReset, contentDescription = null) },
                trailingIcon = if (current == null) { { Icon(Icons.Filled.Check, contentDescription = null) } } else null,
                onClick = {
                    onSelect(null)
                    expanded = false
                }
            )
            ComposeColors.PALETTE.zip(COLOR_NAMES).forEach { (hex, nameRes) ->
                DropdownMenuItem(
                    text = { Text(stringResource(nameRes)) },
                    leadingIcon = { ColorSwatch(ComposeColors.color(hex) ?: Color.Unspecified) },
                    trailingIcon = if (current == hex) { { Icon(Icons.Filled.Check, contentDescription = null) } } else null,
                    onClick = {
                        onSelect(hex)
                        expanded = false
                    }
                )
            }
        }
    }
}

@Composable
private fun ColorSwatch(color: Color) {
    Box(
        Modifier
            .size(20.dp)
            .background(color, CircleShape)
            .border(1.dp, MaterialTheme.colorScheme.outlineVariant, CircleShape)
    )
}

private val PARAGRAPH_STYLES = linkedMapOf(
    ComposeBlockStyle.TITLE to R.string.compose_style_title,
    ComposeBlockStyle.HEADING to R.string.compose_style_heading,
    ComposeBlockStyle.SUBHEADING to R.string.compose_style_subheading,
    ComposeBlockStyle.PARAGRAPH to R.string.compose_style_body,
    ComposeBlockStyle.MONOSPACED to R.string.compose_style_monospaced,
)

private val INLINE_STYLES = listOf(
    Triple(ComposeStyle.BOLD, Icons.Filled.FormatBold, R.string.format_bold),
    Triple(ComposeStyle.ITALIC, Icons.Filled.FormatItalic, R.string.format_italic),
    Triple(ComposeStyle.UNDERLINE, Icons.Filled.FormatUnderlined, R.string.format_underline),
    Triple(ComposeStyle.STRIKE, Icons.Filled.FormatStrikethrough, R.string.format_strikethrough),
    Triple(ComposeStyle.CODE, Icons.Filled.Code, R.string.compose_format_code),
)

private val COLOR_NAMES = listOf(
    R.string.compose_color_red,
    R.string.compose_color_orange,
    R.string.compose_color_green,
    R.string.compose_color_blue,
    R.string.compose_color_purple,
    R.string.compose_color_gray,
)

@Preview(showBackground = true)
@Composable
private fun Preview() {
    Theme {
        ComposeFormatBar(ComposeEditorState(listOf(ComposeBlock.Text("항목", lists = listOf(ComposeList.BULLET)))))
    }
}
