package org.sparcs.soap.app.features.friends.addFriends.components

import android.content.ClipData
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.ContentCopy
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.ClipEntry
import androidx.compose.ui.platform.LocalClipboard
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.sparcs.soap.R
import org.sparcs.soap.app.theme.ui.Theme

/**
 * The user's own code on one line, above the "Add friend with code" button.
 * Tapping the code copies it.
 */
@Composable
fun MyFriendCodeRow(
    code: String?,
    isUnavailable: Boolean,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.CenterHorizontally),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            stringResource(R.string.add_friends_your_code),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        AnimatedContent(
            targetState = code to isUnavailable,
            transitionSpec = { fadeIn() togetherWith fadeOut() },
            label = "MyFriendCode"
        ) { (code, isUnavailable) ->
            when {
                code != null -> CopyableCode(code)
                isUnavailable -> Text(
                    stringResource(R.string.add_friends_code_unavailable),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                else -> CircularProgressIndicator(modifier = Modifier.size(18.dp), strokeWidth = 2.dp)
            }
        }
    }
}

@Composable
private fun CopyableCode(code: String) {
    val clipboard = LocalClipboard.current
    val scope = rememberCoroutineScope()
    var didCopy by remember { mutableStateOf(false) }
    val copyLabel = stringResource(R.string.add_friends_copy_code)
    val copiedLabel = stringResource(R.string.add_friends_code_copied)

    // The checkmark is only confirmation, so it reverts to the copy icon.
    LaunchedEffect(didCopy) {
        if (didCopy) {
            delay(2_000)
            didCopy = false
        }
    }

    Surface(
        onClick = {
            scope.launch {
                clipboard.setClipEntry(ClipEntry(ClipData.newPlainText(copyLabel, code)))
                didCopy = true
            }
        },
        shape = RoundedCornerShape(8.dp),
        color = MaterialTheme.colorScheme.secondaryContainer,
        modifier = Modifier.semantics { contentDescription = "$code, ${if (didCopy) copiedLabel else copyLabel}" }
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                code,
                style = MaterialTheme.typography.titleMedium,
                fontFamily = FontFamily.Monospace,
                fontWeight = FontWeight.Medium,
                letterSpacing = 2.sp
            )
            Icon(
                if (didCopy) Icons.Rounded.Check else Icons.Rounded.ContentCopy,
                contentDescription = null,
                modifier = Modifier.size(18.dp)
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun MyFriendCodeRowPreview() {
    Theme { MyFriendCodeRow(code = "ACD347", isUnavailable = false, modifier = Modifier.padding(16.dp)) }
}
