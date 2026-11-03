package org.sparcs.soap.app.features.postCompose.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
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
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.FormatBold
import androidx.compose.material.icons.filled.FormatColorReset
import androidx.compose.material.icons.filled.FormatColorText
import androidx.compose.material.icons.filled.FormatItalic
import androidx.compose.material.icons.filled.FormatListNumbered
import androidx.compose.material.icons.filled.FormatStrikethrough
import androidx.compose.material.icons.filled.FormatUnderlined
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconToggleButton
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
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
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.PopupProperties
import org.sparcs.soap.R
import org.sparcs.soap.app.features.postCompose.ComposeBlock
import org.sparcs.soap.app.features.postCompose.ComposeBlockStyle
import org.sparcs.soap.app.features.postCompose.ComposeColors
import org.sparcs.soap.app.features.postCompose.ComposeEditorState
import org.sparcs.soap.app.features.postCompose.ComposeList
import org.sparcs.soap.app.features.postCompose.ComposeStyle
import org.sparcs.soap.app.theme.ui.Theme

@Composable
fun ComposeFormatPanel(editor: ComposeEditorState, modifier: Modifier = Modifier) {
    val block = editor.focusedText
    val isEnabled = block != null
    val label = stringResource(R.string.text_formatting)

    Column(
        verticalArrangement = Arrangement.spacedBy(2.dp),
        modifier = modifier
            .padding(vertical = 4.dp)
            .semantics { contentDescription = label }
    ) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier
                .horizontalScroll(rememberScrollState())
                .padding(horizontal = 12.dp)
        ) {
            BLOCK_STYLES.forEach { (style, labelRes) ->
                FilterChip(
                    selected = (block?.style ?: ComposeBlockStyle.PARAGRAPH) == style,
                    onClick = { editor.setBlockStyle(style) },
                    enabled = isEnabled,
                    label = {
                        Text(
                            text = stringResource(labelRes),
                            fontFamily = FontFamily.Monospace.takeIf { style == ComposeBlockStyle.MONOSPACED }
                        )
                    }
                )
            }
        }
        PanelRow {
            INLINE_STYLES.forEach { (style, icon, labelRes) ->
                FormatToggle(icon, stringResource(labelRes), style in editor.activeStyles, isEnabled) { editor.toggle(style) }
            }
            PanelDivider()
            ColorMenu(editor.activeColor, enabled = isEnabled, onSelect = editor::setColor)
        }
        PanelRow {
            BulletListToggle(editor)
            FormatToggle(
                Icons.Filled.FormatListNumbered,
                stringResource(R.string.format_numbered_list),
                block?.lists?.lastOrNull()?.isOrdered == true,
                isEnabled
            ) { editor.toggleList(ComposeList.NUMBERED) }
            PanelDivider()
            IconButton(onClick = editor::outdent, enabled = !block?.lists.isNullOrEmpty()) {
                Icon(Icons.AutoMirrored.Filled.FormatIndentDecrease, contentDescription = stringResource(R.string.compose_format_outdent))
            }
            IconButton(
                onClick = editor::indent,
                enabled = block != null && block.lists.isNotEmpty() && block.lists.size < ComposeList.MAX_DEPTH
            ) {
                Icon(Icons.AutoMirrored.Filled.FormatIndentIncrease, contentDescription = stringResource(R.string.compose_format_indent))
            }
        }
    }
}

@Composable
fun BulletListToggle(editor: ComposeEditorState) {
    val block = editor.focusedText
    FormatToggle(
        Icons.AutoMirrored.Filled.FormatListBulleted,
        stringResource(R.string.format_bullet_list),
        block?.lists?.lastOrNull()?.isOrdered == false,
        block != null
    ) { editor.toggleList(ComposeList.BULLET) }
}

@Composable
private fun PanelRow(content: @Composable () -> Unit) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .horizontalScroll(rememberScrollState())
            .padding(horizontal = 4.dp)
    ) {
        content()
    }
}

@Composable
private fun PanelDivider() {
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
        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
            properties = PopupProperties(focusable = false)
        ) {
            ColorItem(stringResource(R.string.compose_color_default), current == null, { Icon(Icons.Filled.FormatColorReset, contentDescription = null) }) {
                onSelect(null)
                expanded = false
            }
            ComposeColors.PALETTE.zip(COLOR_NAMES).forEach { (hex, nameRes) ->
                ColorItem(stringResource(nameRes), current == hex, { ColorSwatch(ComposeColors.color(hex) ?: Color.Unspecified) }) {
                    onSelect(hex)
                    expanded = false
                }
            }
        }
    }
}

@Composable
private fun ColorItem(name: String, selected: Boolean, leadingIcon: @Composable () -> Unit, onClick: () -> Unit) {
    DropdownMenuItem(
        text = { Text(name) },
        leadingIcon = leadingIcon,
        trailingIcon = if (selected) { { Icon(Icons.Filled.Check, contentDescription = null) } } else null,
        onClick = onClick
    )
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

private val BLOCK_STYLES = listOf(
    ComposeBlockStyle.TITLE to R.string.compose_style_title,
    ComposeBlockStyle.HEADING to R.string.compose_style_heading,
    ComposeBlockStyle.SUBHEADING to R.string.compose_style_subheading,
    ComposeBlockStyle.PARAGRAPH to R.string.compose_style_body,
    ComposeBlockStyle.MONOSPACED to R.string.compose_style_monospaced,
    ComposeBlockStyle.QUOTE to R.string.format_quote,
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
        ComposeFormatPanel(ComposeEditorState(listOf(ComposeBlock.Text("항목", lists = listOf(ComposeList.BULLET)))))
    }
}

@Preview(showBackground = true)
@Composable
private fun BulletListTogglePreview() {
    Theme {
        BulletListToggle(ComposeEditorState(listOf(ComposeBlock.Text("항목", lists = listOf(ComposeList.BULLET)))))
    }
}
