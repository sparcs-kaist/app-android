package org.sparcs.soap.app.features.post.components

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.Reply
import androidx.compose.material.icons.automirrored.rounded.Reply
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material.icons.outlined.SmsFailed
import androidx.compose.material.icons.outlined.Summarize
import androidx.compose.material.icons.outlined.Translate
import androidx.compose.material.icons.rounded.SubdirectoryArrowRight
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch
import org.sparcs.soap.R
import org.sparcs.soap.app.domain.enums.ara.AraContentReportType
import org.sparcs.soap.app.domain.enums.feed.ReportLabelProvider
import org.sparcs.soap.app.domain.helpers.AlertState
import org.sparcs.soap.app.domain.models.ara.AraPostComment
import org.sparcs.soap.app.domain.models.ara.isHiddenForBlockedAuthor
import org.sparcs.soap.app.shared.extensions.timeAgoDisplay
import org.sparcs.soap.app.shared.extensions.toAlertState
import org.sparcs.soap.app.shared.mocks.ara.mock
import org.sparcs.soap.app.shared.views.contentViews.GlobalAlertDialog
import kotlin.reflect.KClass

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PostCommentCell(
    comment: AraPostComment,
    isThreaded: Boolean,
    onComment: () -> Unit,
    onDelete: () -> Unit,
    onEdit: () -> Unit,
    onUpVote: () -> Unit,
    onDownVote: () -> Unit,
    onReport: (AraContentReportType) -> Unit,
    onDeleteComment: () -> Unit,
    onTranslate: () -> Unit = {},
    onSetAuthorBlocked: ((Boolean) -> Unit)? = null,
) {
    val scope = rememberCoroutineScope()
    val isHiddenForBlock = comment.isHiddenForBlockedAuthor
    val isDeleted = comment.content == null && !isHiddenForBlock

    var alertState by remember { mutableStateOf<AlertState?>(null) }
    var isAlertPresented by remember { mutableStateOf(false) }
    var showsBlockConfirmation by remember { mutableStateOf(false) }

    Row(modifier = Modifier.fillMaxWidth()) {
        if (isThreaded) {
            Icon(
                imageVector = Icons.Rounded.SubdirectoryArrowRight,
                contentDescription = null,
                modifier = Modifier.padding(top = 8.dp, end = 4.dp)
            )
        }

        Column(modifier = Modifier.weight(1f)) {
            HorizontalDivider(
                color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f),
                modifier = Modifier.padding(vertical = 8.dp),
            )

            PostCommentHeader(
                comment = comment,
                isDeleted = isDeleted,
                onEdit = onEdit,
                onDelete = {
                    scope.launch {
                        onDelete.invoke()
                        onDeleteComment()
                    }
                },
                onReport = { type ->
                    scope.launch {
                        try {
                            onReport(type)
                            alertState = AlertState(titleResId = R.string.report_submitted, messageResId = R.string.report_submitted_message)
                        } catch (e: Exception) {
                            alertState = e.toAlertState(R.string.error_unable_to_submit_report)
                        } finally {
                            isAlertPresented = true
                        }
                    }
                },
                onTranslate = onTranslate,
                onReply = if (isThreaded || isHiddenForBlock) null else onComment,
                onSetAuthorBlocked = onSetAuthorBlocked?.let { setBlocked ->
                    { blocked -> if (blocked) showsBlockConfirmation = true else setBlocked(false) }
                }
            )

            PostCommentContent(comment = comment)
            PostCommentFooter(
                comment = comment,
                showsReply = !isThreaded && !isHiddenForBlock,
                isDeleted = comment.content == null,
                onComment = onComment,
                onUpVote = onUpVote,
                onDownVote = onDownVote
            )
        }
    }

    GlobalAlertDialog(
        isPresented = isAlertPresented,
        state = alertState,
        onDismiss = { isAlertPresented = false }
    )

    if (showsBlockConfirmation) {
        BlockAuthorDialog(
            nickname = comment.author.profile.nickname,
            onConfirm = {
                showsBlockConfirmation = false
                onSetAuthorBlocked?.invoke(true)
            },
            onDismiss = { showsBlockConfirmation = false }
        )
    }
}

@Composable
private fun PostCommentHeader(
    comment: AraPostComment,
    isDeleted: Boolean,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    onReport: (AraContentReportType) -> Unit,
    onTranslate: () -> Unit,
    onReply: (() -> Unit)?,
    onSetAuthorBlocked: ((Boolean) -> Unit)?,
) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        AuthorAvatar(comment.author.profile.profilePictureURL, size = 24.dp)

        Spacer(modifier = Modifier.padding(4.dp))

        Text(
            text = comment.author.profile.nickname,
            fontWeight = FontWeight.Medium,
            style = MaterialTheme.typography.bodyMedium
        )

        Spacer(modifier = Modifier.padding(4.dp))

        Text(
            text = comment.createdAt.timeAgoDisplay(),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(modifier = Modifier.weight(1f))

        if (!isDeleted) {
            PostCommentActionsMenu(
                enumClass = AraContentReportType::class,
                isMine = comment.isMine,
                onEdit = onEdit,
                onDelete = onDelete,
                onReport = onReport,
                onTranslate = onTranslate,
                isComment = true,
                onReply = onReply,
                isAuthorBlocked = comment.author.isBlocked == true,
                onSetAuthorBlocked = onSetAuthorBlocked,
                showsContentActions = comment.content != null
            )
        }
    }
}

@Composable
fun <T> PostCommentActionsMenu(
    modifier: Modifier = Modifier,
    enumClass: KClass<T>,
    isMine: Boolean?,
    onEdit: () -> Unit? = {},
    onDelete: () -> Unit,
    onReport: (T) -> Unit,
    onTranslate: () -> Unit,
    onSummarize: () -> Unit = {},
    isComment: Boolean,
    onReply: (() -> Unit)? = null,
    isAuthorBlocked: Boolean = false,
    onSetAuthorBlocked: ((Boolean) -> Unit)? = null,
    showsContentActions: Boolean = true,
) where T : Enum<T>, T : ReportLabelProvider {
    var expanded by remember { mutableStateOf(false) }
    var reportExpanded by remember { mutableStateOf(false) }

    Box {
        IconButton(onClick = { expanded = true }, modifier = modifier) {
            Icon(
                imageVector = Icons.Filled.MoreVert,
                contentDescription = stringResource(R.string.more),
                tint = if (isComment && isMine == true) MaterialTheme.colorScheme.primary
                else LocalContentColor.current
            )
        }

        DropdownMenu(
            expanded = expanded,
            onDismissRequest = {
                expanded = false
                reportExpanded = false
            }) {
            onReply?.let { reply ->
                DropdownMenuItem(
                    text = { Text(stringResource(R.string.feed_reply_action)) },
                    onClick = { expanded = false; reply() },
                    leadingIcon = { Icon(Icons.AutoMirrored.Outlined.Reply, contentDescription = null) },
                )
            }
            if (isMine == false) {
                onSetAuthorBlocked?.let { setBlocked ->
                    BlockItem(isAuthorBlocked) { expanded = false; setBlocked(!isAuthorBlocked) }
                }
            }
            if (isMine == false && showsContentActions) {
                DropdownMenuItem(
                    text = { Text(stringResource(R.string.report)) },
                    onClick = {
                        reportExpanded = !reportExpanded
                    },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Outlined.SmsFailed,
                            contentDescription = null
                        )
                    },
                    trailingIcon = {
                        Icon(
                            imageVector = if (reportExpanded) Icons.Filled.ExpandLess else Icons.Filled.ExpandMore,
                            contentDescription = null
                        )
                    }
                )

                AnimatedVisibility(
                    visible = reportExpanded,
                    enter = expandVertically() + fadeIn(),
                    exit = shrinkVertically() + fadeOut()
                ) {
                    Column {
                        enumClass.java.enumConstants?.forEach { type ->

                            DropdownMenuItem(
                                text = {
                                    Text(stringResource(type.labelRes))
                                },
                                onClick = {
                                    onReport(type)
                                    expanded = false
                                    reportExpanded = false
                                }
                            )
                        }
                    }
                }

                HorizontalDivider()

                DropdownMenuItem(
                    text = { Text(stringResource(R.string.translate)) },
                    onClick = { onTranslate(); expanded = false },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Outlined.Translate,
                            contentDescription = null
                        )
                    }
                )
                if (!isComment) {
                    DropdownMenuItem(
                        text = { Text(stringResource(R.string.summarize)) },
                        onClick = { onSummarize(); expanded = false },
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Outlined.Summarize,
                                contentDescription = null
                            )
                        }
                    )
                }

            } else if (isMine != false) {
                if (enumClass == AraContentReportType::class) {
                    DropdownMenuItem(
                    text = { Text(stringResource(R.string.edit)) },
                    onClick = { onEdit(); expanded = false },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Outlined.Edit,
                            contentDescription = null
                        )
                    }
                    )
                    HorizontalDivider()
                }

                DropdownMenuItem(
                    text = {
                        Text(
                            text = stringResource(R.string.delete),
                            color = MaterialTheme.colorScheme.error
                        )
                    },
                    onClick = { onDelete(); expanded = false },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Outlined.Delete,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.error
                        )
                    }
                )
            }
        }
    }
}

@Composable
private fun PostCommentContent(comment: AraPostComment) {
    val isBlocked = comment.isHiddenForBlockedAuthor
    AnimatedContent(targetState = comment.content) { content ->
        Text(
            text = when {
                content != null -> content
                isBlocked -> stringResource(R.string.comment_hidden_blocked_author)
                else -> stringResource(R.string.this_comment_has_been_deleted)
            },
            style = MaterialTheme.typography.bodyMedium,
            fontStyle = if (content == null) FontStyle.Italic else null,
            modifier = Modifier.padding(vertical = 8.dp),
            color = if (content == null) MaterialTheme.colorScheme.onSurfaceVariant
            else MaterialTheme.colorScheme.onSurface
        )
    }
}

@Composable
private fun PostCommentFooter(
    comment: AraPostComment,
    showsReply: Boolean,
    isDeleted: Boolean,
    onComment: () -> Unit,
    onUpVote: () -> Unit,
    onDownVote: () -> Unit,
) {
    val scope = rememberCoroutineScope()

    FlowRow(
        horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.End),
        verticalArrangement = Arrangement.spacedBy(8.dp),
        itemVerticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 4.dp, end = 4.dp)
    ) {
        if (showsReply) {
            PostCommentButton(commentCount = comment.comments.size, onClick = onComment, isCompact = true)
        }

        if (!isDeleted) {
            PostVoteButton(
                myVote = comment.myVote,
                votes = comment.upVotes - comment.downVotes,
                onUpVote = {
                    scope.launch {
                        onUpVote()
                    }
                },
                onDownVote = {
                    scope.launch {
                        onDownVote()
                    }
                },
                enabled = true,
                isCompact = true
            )
        }
    }
}
@Composable
@Preview(showBackground = true)
private fun Preview1() {
    PostCommentCell(
        comment = AraPostComment.mock(),
        isThreaded = false,
        onComment = {},
        onDelete = {},
        onEdit = {},
        onUpVote = {},
        onDownVote = {},
        onReport = {},
        onDeleteComment = {}
    )
}


@Composable
@Preview(showBackground = true)
private fun Preview2() {
    PostCommentCell(
        comment = AraPostComment.mock(),
        isThreaded = true,
        onComment = {},
        onDelete = {},
        onEdit = {},
        onUpVote = {},
        onDownVote = {},
        onReport = {},
        onDeleteComment = {}
    )
}
