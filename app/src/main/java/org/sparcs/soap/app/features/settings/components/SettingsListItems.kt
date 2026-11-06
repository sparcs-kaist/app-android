package org.sparcs.soap.app.features.settings.components

import androidx.annotation.DrawableRes
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.selection.toggleable
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Notifications
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemColors
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SegmentedListItem
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldColors
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.toggleableState
import androidx.compose.ui.state.ToggleableState
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import org.sparcs.soap.R
import org.sparcs.soap.app.theme.ui.Theme

// Building blocks for settings screens. Rows inside a [SettingsGroup] are Material 3 Expressive
// segmented list items: the group's rows share one rounded card, split by small gaps. Rows outside
// a group stay plain ListItems. Text fields and pickers go into a group too, as [SettingsFieldItem]
// rows with [settingsTextFieldColors], so a whole section reads as one card. Section headers line
// up with the content inside the cards.

/** Where a row sits in its [SettingsGroup], so it can take the matching segmented corners. */
@Immutable
data class SettingsSegment(val index: Int, val count: Int)

/** The enclosing group's position for the current row, or null outside a [SettingsGroup]. */
val LocalSettingsSegment = compositionLocalOf<SettingsSegment?> { null }

/** Collects a [SettingsGroup]'s rows; conditionally added rows keep the corners right. */
class SettingsGroupScope internal constructor() {
    internal val rows = mutableListOf<@Composable () -> Unit>()

    /** Adds a row, typically a [SettingsNavigationItem], [SettingsSwitchItem] or [RowElementView]. */
    fun row(content: @Composable () -> Unit) {
        rows += content
    }
}

/**
 * A section's rows as one rounded card of segmented list items, inset 16dp from the screen edges.
 * The first and last rows take the card's outer corners; rows in between are joined by small gaps.
 */
@Composable
fun SettingsGroup(
    modifier: Modifier = Modifier,
    content: SettingsGroupScope.() -> Unit,
) {
    val rows = SettingsGroupScope().apply(content).rows
    Column(
        modifier = modifier.padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(ListItemDefaults.SegmentedGap)
    ) {
        rows.forEachIndexed { index, row ->
            CompositionLocalProvider(LocalSettingsSegment provides SettingsSegment(index, rows.size)) {
                row()
            }
        }
    }
}

/**
 * Colors for a segmented row: the app's card color, surfaceContainer, so groups stand out on the
 * surface-coloured settings screens (the spec's surface default would vanish into them).
 */
@Composable
internal fun settingsSegmentedColors(destructive: Boolean = false): ListItemColors {
    val colors = MaterialTheme.colorScheme
    // A disabled row keeps the card; only its content dims, so the group's outline stays whole.
    return if (destructive) {
        ListItemDefaults.segmentedColors(
            containerColor = colors.surfaceContainer,
            contentColor = colors.error,
            leadingContentColor = colors.error,
            disabledContainerColor = colors.surfaceContainer
        )
    } else {
        ListItemDefaults.segmentedColors(
            containerColor = colors.surfaceContainer,
            disabledContainerColor = colors.surfaceContainer
        )
    }
}

/**
 * One row of a segmented card in a lazy list, where each row is its own item and so cannot sit in a
 * [SettingsGroup]: insets the row 16dp, adds the segmented gap above all but the first, and tells
 * the row its position so it takes the matching corners.
 */
@Composable
fun SettingsSegmentRow(
    index: Int,
    count: Int,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit,
) {
    Box(
        modifier.padding(
            start = 16.dp,
            end = 16.dp,
            top = if (index == 0) 0.dp else ListItemDefaults.SegmentedGap
        )
    ) {
        CompositionLocalProvider(LocalSettingsSegment provides SettingsSegment(index, count)) {
            content()
        }
    }
}

/** Corners for a segmented row at [segment]. */
@Composable
internal fun segmentedShapes(segment: SettingsSegment) =
    ListItemDefaults.segmentedShapes(index = segment.index, count = segment.count)

/** Section title in the style of Android's own settings, with an optional leading [icon]; [trailing] sits after the text. */
@Composable
fun SettingsSectionHeader(
    text: String,
    modifier: Modifier = Modifier,
    icon: ImageVector? = null,
    trailing: (@Composable () -> Unit)? = null,
) {
    Row(
        // 32dp lines the title up with the content of the cards below, which are inset 16dp and pad 16dp.
        modifier = modifier.padding(start = 32.dp, end = 32.dp, top = 24.dp, bottom = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        if (icon != null) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier
                    .padding(end = 4.dp)
                    .size(18.dp)
            )
        }
        Text(
            text = text,
            style = MaterialTheme.typography.titleSmall,
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.semantics { heading() }
        )
        trailing?.invoke()
    }
}

/** Row that opens another screen or performs an action; [destructive] shows it in the error color. */
@Composable
fun SettingsNavigationItem(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    supportingText: String? = null,
    leadingContent: (@Composable () -> Unit)? = null,
    trailingContent: (@Composable () -> Unit)? = null,
    destructive: Boolean = false,
) {
    val error = MaterialTheme.colorScheme.error
    val segment = LocalSettingsSegment.current
    if (segment != null) {
        SegmentedListItem(
            onClick = onClick,
            shapes = segmentedShapes(segment),
            modifier = modifier,
            enabled = enabled,
            leadingContent = leadingContent,
            trailingContent = trailingContent,
            supportingContent = supportingText?.let { { Text(it) } },
            colors = settingsSegmentedColors(destructive),
        ) { Text(text) }
        return
    }
    val disabled = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.38f)
    ListItem(
        headlineContent = { Text(text) },
        supportingContent = supportingText?.let { { Text(it) } },
        leadingContent = leadingContent,
        trailingContent = trailingContent,
        colors = when {
            !enabled -> ListItemDefaults.colors(
                headlineColor = disabled,
                supportingColor = disabled,
                leadingIconColor = disabled
            )
            destructive -> ListItemDefaults.colors(headlineColor = error, leadingIconColor = error)
            else -> ListItemDefaults.colors()
        },
        modifier = modifier.clickable(enabled = enabled, onClick = onClick)
    )
}

/**
 * A group row holding free-form content, typically a [TextField] with [settingsTextFieldColors] or a
 * picker built on one. It takes the row's segmented corners and the card color, and adds no
 * horizontal padding: the field's own 16dp inset lines its text up with the other rows.
 */
@Composable
fun SettingsFieldItem(
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit,
) {
    val segment = LocalSettingsSegment.current
    Surface(
        shape = segment?.let { segmentedShapes(it).shape } ?: MaterialTheme.shapes.large,
        color = MaterialTheme.colorScheme.surfaceContainer,
        modifier = modifier.fillMaxWidth()
    ) {
        Box(Modifier.padding(vertical = 4.dp)) { content() }
    }
}

/**
 * Colors for a [TextField] inside a [SettingsFieldItem]: no fill and no underline, so the field reads
 * as part of the card. Focus shows through the primary-colored label and the cursor; errors through
 * the label and supporting text.
 */
@Composable
fun settingsTextFieldColors(): TextFieldColors = TextFieldDefaults.colors(
    focusedContainerColor = Color.Transparent,
    unfocusedContainerColor = Color.Transparent,
    disabledContainerColor = Color.Transparent,
    errorContainerColor = Color.Transparent,
    focusedIndicatorColor = Color.Transparent,
    unfocusedIndicatorColor = Color.Transparent,
    disabledIndicatorColor = Color.Transparent,
    errorIndicatorColor = Color.Transparent,
)

/** Row with a trailing switch; the whole row toggles it. */
@Composable
fun SettingsSwitchItem(
    text: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    supportingText: String? = null,
    leadingContent: (@Composable () -> Unit)? = null,
    headlineTrailing: (@Composable () -> Unit)? = null,
) {
    val headline: @Composable () -> Unit = {
        if (headlineTrailing == null) {
            Text(text)
        } else {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(text)
                headlineTrailing()
            }
        }
    }
    val switch: @Composable () -> Unit = { Switch(checked = checked, onCheckedChange = null, enabled = enabled) }
    val segment = LocalSettingsSegment.current
    if (segment != null) {
        // The click overload rather than the checkable one, which would tint the whole row when on.
        SegmentedListItem(
            onClick = { onCheckedChange(!checked) },
            shapes = segmentedShapes(segment),
            modifier = modifier.semantics {
                role = Role.Switch
                toggleableState = ToggleableState(checked)
            },
            enabled = enabled,
            colors = settingsSegmentedColors(),
            leadingContent = leadingContent,
            trailingContent = switch,
            supportingContent = supportingText?.let { { Text(it) } },
            content = headline,
        )
        return
    }
    ListItem(
        headlineContent = headline,
        supportingContent = supportingText?.let { { Text(it) } },
        leadingContent = leadingContent,
        trailingContent = switch,
        modifier = modifier.toggleable(
            value = checked,
            enabled = enabled,
            role = Role.Switch,
            onValueChange = onCheckedChange
        )
    )
}

/** A service's brand mark at icon size, keeping its own colours. */
@Composable
fun ServiceLogo(@DrawableRes logo: Int) {
    Icon(
        painterResource(logo),
        contentDescription = null,
        tint = Color.Unspecified,
        modifier = Modifier.size(24.dp)
    )
}

@Preview(showBackground = true)
@Composable
private fun Preview() {
    Theme {
        Column {
            SettingsSectionHeader("Section")
            SettingsGroup {
                row {
                    SettingsNavigationItem(
                        text = "Notifications",
                        onClick = {},
                        leadingContent = { Icon(Icons.Outlined.Notifications, null) }
                    )
                }
                row {
                    SettingsNavigationItem(
                        text = "Taxi",
                        onClick = {},
                        leadingContent = { ServiceLogo(R.drawable.taxi_logo) }
                    )
                }
                row {
                    SettingsSwitchItem(
                        text = "Automatic",
                        checked = true,
                        onCheckedChange = {},
                        supportingText = "Explains what the switch does."
                    )
                }
            }
        }
    }
}
