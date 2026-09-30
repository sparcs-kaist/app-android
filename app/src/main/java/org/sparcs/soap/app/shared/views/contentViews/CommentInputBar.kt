package org.sparcs.soap.app.shared.views.contentViews

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import org.sparcs.soap.R

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
    val hasBottomRow = bottomLeadingContent != null
    val showBottomRow = hasBottomRow && (showBottomRowAlways || isFocused || value.isNotEmpty())

    Column(
        modifier = modifier
            .fillMaxWidth()
            .imePadding()
            .padding(8.dp),
    ) {
        if (replyPreview != null) {
            replyPreview()
        }

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(16.dp))
                .background(MaterialTheme.colorScheme.surface)
                .border(1.dp, MaterialTheme.colorScheme.outlineVariant, RoundedCornerShape(16.dp))
                .padding(start = 12.dp, end = 4.dp, top = 4.dp, bottom = 4.dp),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.Bottom,
            ) {
                if (leadingContent != null) {
                    Box(
                        modifier = Modifier.padding(bottom = 6.dp, end = 8.dp),
                        contentAlignment = Alignment.Center,
                    ) {
                        leadingContent()
                    }
                }

                BasicTextField(
                    value = value,
                    onValueChange = onValueChange,
                    modifier = Modifier
                        .weight(1f)
                        .heightIn(min = 40.dp)
                        .focusRequester(focusRequester)
                        .onFocusChanged {
                            isFocused = it.isFocused
                            onFocusChange?.invoke(it.isFocused)
                        }
                        .padding(vertical = 8.dp),
                    maxLines = 6,
                    textStyle = MaterialTheme.typography.bodyLarge.copy(color = MaterialTheme.colorScheme.onSurface),
                    cursorBrush = SolidColor(MaterialTheme.colorScheme.primary),
                    decorationBox = { innerTextField ->
                        Box(contentAlignment = Alignment.CenterStart) {
                            if (value.isEmpty()) {
                                Text(
                                    text = placeholder,
                                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
                                    style = MaterialTheme.typography.bodyMedium,
                                    overflow = TextOverflow.Ellipsis,
                                    maxLines = 1,
                                )
                            }
                            innerTextField()
                        }
                    },
                )

                if (!hasBottomRow) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(start = 4.dp),
                    ) {
                        if (trailingContent != null) {
                            trailingContent()
                            Spacer(modifier = Modifier.width(4.dp))
                        }
                        SendButton(
                            onClick = onSend,
                            enabled = enabled,
                            isSubmitting = isSubmitting,
                        )
                    }
                }
            }

            if (hasBottomRow) {
                AnimatedVisibility(visible = showBottomRow) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 4.dp, bottom = 2.dp, end = 4.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        bottomLeadingContent()
                        Spacer(modifier = Modifier.weight(1f))
                        SendButton(
                            onClick = onSend,
                            enabled = enabled,
                            isSubmitting = isSubmitting,
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun SendButton(
    onClick: () -> Unit,
    enabled: Boolean,
    isSubmitting: Boolean,
    modifier: Modifier = Modifier,
) {
    Button(
        onClick = onClick,
        enabled = enabled,
        shape = RoundedCornerShape(12.dp),
        contentPadding = PaddingValues(0.dp),
        modifier = modifier.size(40.dp),
    ) {
        if (isSubmitting) {
            CircularProgressIndicator(
                modifier = Modifier.size(16.dp),
                strokeWidth = 2.dp,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        } else {
            Icon(
                painter = painterResource(id = R.drawable.outline_send),
                modifier = Modifier.size(20.dp),
                contentDescription = stringResource(R.string.send),
            )
        }
    }
}
