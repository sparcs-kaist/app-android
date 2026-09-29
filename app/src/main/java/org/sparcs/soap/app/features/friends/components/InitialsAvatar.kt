package org.sparcs.soap.app.features.friends.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import org.sparcs.soap.app.theme.ui.Theme
import java.util.Locale

/**
 * There are no profile photos, so each person gets a letter avatar, like the
 * Contacts app. The colour is picked from their name so the same person keeps
 * the same colour across launches.
 */
@Composable
fun InitialsAvatar(
    name: String,
    modifier: Modifier = Modifier,
    size: Dp = 40.dp,
    /** Hidden under a spinner or checkmark so the two don't overlap. */
    showsInitials: Boolean = true,
) {
    val initialsAlpha by animateFloatAsState(if (showsInitials) 1f else 0f, label = "InitialsAlpha")
    val initials = remember(name) { initials(name) }
    val color = remember(name) { avatarColor(name) }
    val fontSize = with(LocalDensity.current) { (size * if (initials.length > 1) 0.36f else 0.44f).toSp() }

    Box(
        modifier = modifier
            .size(size)
            .background(color, CircleShape),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = initials,
            color = Color.White,
            maxLines = 1,
            modifier = Modifier.alpha(initialsAlpha),
            style = TextStyle(fontSize = fontSize, fontWeight = FontWeight.Medium)
        )
    }
}

/**
 * "김수진" → "수진" (given name, as Korean speakers usually address friends),
 * "Minho Lee" → "ML", "Haeun" → "H".
 */
internal fun initials(name: String): String {
    val trimmed = name.trim()
    val words = trimmed.split(Regex("\\s+")).filter { it.isNotEmpty() }

    if (words.size >= 2) {
        return words.take(2).joinToString("") { it.firstGrapheme() }.uppercase(Locale.ROOT)
    }
    if (trimmed.any { it in '가'..'힣' } && trimmed.length >= 3) {
        return trimmed.drop(1)
    }
    return trimmed.firstGrapheme().uppercase(Locale.ROOT).ifEmpty { "?" }
}

private fun String.firstGrapheme(): String =
    if (isEmpty()) "" else substring(0, Character.charCount(codePointAt(0)))

// Mid-tone colours that keep white text legible in light and dark themes.
private val avatarPalette = listOf(
    Color(0xFFD81B60),
    Color(0xFF8E24AA),
    Color(0xFF3949AB),
    Color(0xFF1E88E5),
    Color(0xFF00897B),
    Color(0xFF43A047),
    Color(0xFFF4511E),
    Color(0xFF6D4C41),
)

/** `hashCode` isn't guaranteed stable across versions, so sum the code points. */
private fun avatarColor(name: String): Color {
    val seed = name.codePoints().reduce(0) { acc, cp -> acc + cp }
    return avatarPalette[Math.floorMod(seed, avatarPalette.size)]
}

@Preview(showBackground = true)
@Composable
private fun InitialsAvatarPreview() {
    Theme {
        Row(
            modifier = Modifier.padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            InitialsAvatar(name = "김수진", size = 72.dp)
            InitialsAvatar(name = "Minho Lee", size = 72.dp)
            InitialsAvatar(name = "Haeun", size = 72.dp)
        }
    }
}
