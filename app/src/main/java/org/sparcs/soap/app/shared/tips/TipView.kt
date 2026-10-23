package org.sparcs.soap.app.shared.tips

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import org.sparcs.soap.R
import org.sparcs.soap.app.shared.extensions.glassBorder
import org.sparcs.soap.app.theme.ui.Theme

val LocalTipStore = staticCompositionLocalOf<TipStore?> { null }

data class TipAction(val label: String, val onClick: () -> Unit)

@Composable
fun rememberTipVisible(tip: BuddyTip, enabled: Boolean = true): Boolean {
    val store = LocalTipStore.current ?: return false
    val revision by store.revision.collectAsState()
    val visible = enabled && remember(revision, tip) { store.isEligible(tip) }
    LaunchedEffect(visible, tip) { if (visible) store.markShown(tip) }
    return visible
}

@Composable
fun TipView(
    tip: BuddyTip,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    message: String = stringResource(tip.message),
    actions: List<TipAction> = emptyList(),
) {
    val store = LocalTipStore.current
    AnimatedVisibility(
        visible = rememberTipVisible(tip, enabled),
        enter = fadeIn() + expandVertically(),
        exit = fadeOut() + shrinkVertically(),
        modifier = modifier,
    ) {
        TipCard(
            tip = tip,
            message = message,
            actions = actions,
            onClose = { store?.invalidate(tip) },
        )
    }
}

@Composable
fun TipCard(
    tip: BuddyTip,
    message: String,
    onClose: () -> Unit,
    modifier: Modifier = Modifier,
    actions: List<TipAction> = emptyList(),
) {
    Surface(
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.background,
        modifier = modifier
            .fillMaxWidth()
            .glassBorder(shape = RoundedCornerShape(16.dp)),
    ) {
        Row(
            modifier = Modifier.padding(start = 16.dp, top = 12.dp, bottom = 12.dp, end = 4.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Icon(
                tip.icon,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier
                    .padding(top = 2.dp)
                    .size(24.dp),
            )
            Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Text(stringResource(tip.title), style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold)
                Text(message, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                if (actions.isNotEmpty()) {
                    Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                        actions.forEach { action -> TextButton(onClick = action.onClick) { Text(action.label) } }
                    }
                }
            }
            IconButton(onClick = onClose, modifier = Modifier.align(Alignment.Top)) {
                Icon(
                    Icons.Rounded.Close,
                    contentDescription = stringResource(R.string.close),
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(18.dp),
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun TipCardPreview() {
    Theme {
        TipCard(BuddyTip.ResizePreview, stringResource(BuddyTip.ResizePreview.message), onClose = {}, modifier = Modifier.padding(16.dp))
    }
}

@Preview(showBackground = true)
@Composable
private fun TipCardActionsPreview() {
    Theme {
        TipCard(
            BuddyTip.StartEditing,
            stringResource(BuddyTip.StartEditing.message),
            onClose = {},
            actions = listOf(
                TipAction(stringResource(R.string.tip_new_table)) {},
                TipAction(stringResource(R.string.tip_duplicate_my_table)) {},
            ),
            modifier = Modifier.padding(16.dp),
        )
    }
}
