package org.sparcs.soap.app.features.post.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import org.sparcs.soap.R
import org.sparcs.soap.app.domain.models.ara.AraPostComment
import org.sparcs.soap.app.shared.mocks.ara.mockList
import org.sparcs.soap.app.shared.views.contentViews.CommentInputBar
import org.sparcs.soap.app.theme.ui.Theme
import java.net.URL

@Composable
fun PostInputBar(
    comment: String,
    onCommentChange: (String) -> Unit,
    onFocusChange: (Boolean) -> Unit,
    isEditing: Boolean,
    onCancelEdit: () -> Unit,
    targetComment: AraPostComment?,
    onCancelReply: () -> Unit,
    isUploadingComment: Boolean,
    onUploadComment: () -> Unit,
    profilePictureUrl: URL?,
    placeholder: String,
    focusRequester: FocusRequester,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.background)
            .navigationBarsPadding(),
        contentAlignment = Alignment.Center,
    ) {
        CommentInputBar(
            value = comment,
            onValueChange = onCommentChange,
            placeholder = placeholder,
            onSend = onUploadComment,
            focusRequester = focusRequester,
            isSubmitting = isUploadingComment,
            onFocusChange = onFocusChange,
            replyPreview = {
                AnimatedVisibility(targetComment != null) {
                    targetComment?.let { PostCommentReplyPreview(comment = it, onCancel = onCancelReply) }
                }
            },
            leadingContent = { AuthorAvatar(profilePictureUrl, size = 24.dp) },
            trailingContent = if (isEditing) {
                {
                    IconButton(onClick = onCancelEdit) {
                        Icon(imageVector = Icons.Default.Close, contentDescription = stringResource(R.string.cancel))
                    }
                }
            } else null,
            modifier = Modifier.widthIn(max = 600.dp),
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun PostInputBarPreview() {
    Theme {
        PostInputBar(
            comment = "",
            onCommentChange = {},
            onFocusChange = {},
            isEditing = false,
            onCancelEdit = {},
            targetComment = AraPostComment.mockList().first(),
            onCancelReply = {},
            isUploadingComment = false,
            onUploadComment = {},
            profilePictureUrl = null,
            placeholder = "Reply as anonymous",
            focusRequester = remember { FocusRequester() },
        )
    }
}
