package org.sparcs.soap.app.shared.views.contentViews

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import org.sparcs.soap.R

/**
 * Message-style composer: a rounded filled text field with an optional [leadingContent] (such as
 * the author's avatar) and [trailingContent], and a round send button beside it. [replyPreview]
 * sits above the field; [bottomLeadingContent] appears under it while composing.
 */
@Composable
fun CommentInputBar(
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String,
    onSend: () -> Unit,
    modifier: Modifier = Modifier,
    focusRequester: FocusRequester = remember { FocusRequester() },
    isSubmitting: Boolean = false,
    enabled: Boolean = value.isNotEmpty() && !isSubmitting,
    replyPreview: (@Composable () -> Unit)? = null,
    leadingContent: (@Composable () -> Unit)? = null,
    trailingContent: (@Composable () -> Unit)? = null,
    bottomLeadingContent: (@Composable RowScope.() -> Unit)? = null,
    showBottomRowAlways: Boolean = false,
    onFocusChange: ((Boolean) -> Unit)? = null,
) {
    var isFocused by remember { mutableStateOf(false) }
    val showBottomRow = bottomLeadingContent != null &&
        (showBottomRowAlways || isFocused || value.isNotEmpty())

    Column(
        modifier = modifier
            .fillMaxWidth()
            .imePadding()
            .padding(horizontal = 8.dp, vertical = 8.dp),
    ) {
        replyPreview?.invoke()

        Row(
            verticalAlignment = Alignment.Bottom,
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            TextField(
                value = value,
                onValueChange = onValueChange,
                placeholder = {
                    Text(placeholder, maxLines = 1, overflow = TextOverflow.Ellipsis)
                },
                leadingIcon = leadingContent,
                trailingIcon = trailingContent,
                maxLines = 6,
                shape = MaterialTheme.shapes.extraLarge,
                colors = TextFieldDefaults.colors(
                    focusedContainerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
                    unfocusedContainerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
                    disabledContainerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
                    focusedIndicatorColor = Color.Transparent,
                    unfocusedIndicatorColor = Color.Transparent,
                    disabledIndicatorColor = Color.Transparent,
                ),
                modifier = Modifier
                    .weight(1f)
                    .focusRequester(focusRequester)
                    .onFocusChanged {
                        isFocused = it.isFocused
                        onFocusChange?.invoke(it.isFocused)
                    },
            )

            // Centered on a single-line field; stays at the bottom as the field grows.
            Box(modifier = Modifier.padding(bottom = 4.dp)) {
                SendButton(onClick = onSend, enabled = enabled, isSubmitting = isSubmitting)
            }
        }

        if (bottomLeadingContent != null) {
            AnimatedVisibility(visible = showBottomRow) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 4.dp),
                    content = bottomLeadingContent
                )
            }
        }
    }
}

/** The filled send button of the comment and chat bars, with a spinner while sending. */
@Composable
internal fun SendButton(
    onClick: () -> Unit,
    enabled: Boolean,
    isSubmitting: Boolean,
) {
    FilledIconButton(
        onClick = onClick,
        enabled = enabled,
        modifier = Modifier.size(48.dp),
    ) {
        if (isSubmitting) {
            CircularProgressIndicator(modifier = Modifier.size(20.dp), strokeWidth = 2.dp)
        } else {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.Send,
                contentDescription = stringResource(R.string.send),
            )
        }
    }
}
