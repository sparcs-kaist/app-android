package org.sparcs.soap.app.shared.sharing

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import org.sparcs.soap.R
import org.sparcs.soap.app.domain.helpers.TimetableTheme
import java.util.Locale
import kotlin.math.roundToInt

enum class StoryBackground(val label: Int) {
    ThemeColors(R.string.share_background_palette),
    ThemeBackground(R.string.share_background_theme),
    Default(R.string.share_background_default);

    fun colors(theme: TimetableTheme): Pair<String, String> {
        if (this == ThemeColors) {
            return "#${theme.hexColors.first()}" to "#${theme.hexColors.getOrElse(1) { theme.hexColors.first() }}"
        }
        val hex = if (this == ThemeBackground) theme.backgroundColorHex ?: "F2F2F7" else "F2F2F7"
        val rgb = hex.toInt(16)
        fun blend(target: Int, amount: Float): String {
            val channels = listOf(16, 8, 0).map { shift ->
                val value = (rgb shr shift) and 255
                (value + (target - value) * amount).roundToInt()
            }
            return String.format(Locale.ROOT, "#%02X%02X%02X", *channels.toTypedArray())
        }
        return blend(255, .08f) to blend(0, .06f)
    }

    companion object {
        fun initial(theme: TimetableTheme) =
            if (theme.id in setOf("builtin.default", "builtin.legacy")) Default else ThemeColors
    }
}

@Composable
fun StoryBackgroundOptions(theme: TimetableTheme, selected: StoryBackground, enabled: Boolean, onSelect: (StoryBackground) -> Unit) {
    val description = stringResource(R.string.share_story_background)
    Row(
        Modifier.fillMaxWidth().padding(horizontal = 24.dp).selectableGroup()
            .semantics { contentDescription = description },
        horizontalArrangement = Arrangement.spacedBy(12.dp, Alignment.CenterHorizontally),
    ) {
        StoryBackground.entries.forEach { option ->
            val label = stringResource(option.label)
            val (top, bottom) = option.colors(theme)
            Box(
                Modifier.size(48.dp).alpha(if (enabled) 1f else .4f).clip(CircleShape)
                    .selectable(selected = selected == option, enabled = enabled, role = Role.RadioButton,
                        onClick = { onSelect(option) })
                    .semantics { contentDescription = label }
                    .border(2.dp, if (selected == option) MaterialTheme.colorScheme.onSurface else Color.Transparent, CircleShape)
                    .padding(5.dp)
                    .background(Brush.verticalGradient(listOf(
                        TimetableTheme.color(top.removePrefix("#")),
                        TimetableTheme.color(bottom.removePrefix("#")),
                    )), CircleShape)
                    .border(1.dp, MaterialTheme.colorScheme.onSurface.copy(alpha = .15f), CircleShape)
            ) {
            }
        }
    }
}
