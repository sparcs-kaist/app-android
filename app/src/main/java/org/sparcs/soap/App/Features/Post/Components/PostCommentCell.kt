package org.sparcs.soap.Features.Post

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowForwardIos
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material.icons.outlined.SmsFailed
import androidx.compose.material.icons.rounded.MoreHoriz
import androidx.compose.material.icons.rounded.SubdirectoryArrowRight
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import kotlinx.coroutines.launch
import org.sparcs.soap.App.Domain.Enums.Ara.AraContentReportType
import org.sparcs.soap.App.Domain.Enums.Feed.ReportLabelProvider
import org.sparcs.soap.App.Domain.Helpers.AlertState
import org.sparcs.soap.App.Domain.Models.Ara.AraPostComment
import org.sparcs.soap.App.Features.Post.Components.PostCommentButton
import org.sparcs.soap.App.Features.Post.Components.PostVoteButton
import org.sparcs.soap.App.Shared.Extensions.timeAgoDisplay
import org.sparcs.soap.App.Shared.Mocks.mock
import org.sparcs.soap.App.theme.ui.grayBB
import org.sparcs.soap.App.theme.ui.lightGray0
import org.sparcs.soap.R
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
) {
    val scope = rememberCoroutineScope()
    val isDeleted = comment.content == null
    val context = LocalContext.current

    var alertState by remember { mutableStateOf<AlertState?>(null) }
    var isAlertPresented by remember { mutableStateOf(false) }
    var showTranslateSheet by remember { mutableStateOf(false) }

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
                color = MaterialTheme.colorScheme.lightGray0,
                modifier = Modifier.padding(vertical = 4.dp)
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
                            alertState = AlertState(titleResId = R.string.error_unable_to_submit_report, message = e.localizedMessage ?: context.getString(R.string.error_unknown_try_again))
                        } finally {
                            isAlertPresented = true
                        }
                    }
                },
                onTranslate = { showTranslateSheet = true }
            )

            PostCommentContent(
                isDeleted = isDeleted,
                comment = comment
            )
            PostCommentFooter(
                comment = comment,
                isThreaded = isThreaded,
                isDeleted = isDeleted,
                onComment = onComment,
                onUpVote = onUpVote,
                onDownVote = onDownVote
            )
        }
    }

    if (showTranslateSheet) {
        ModalBottomSheet(onDismissRequest = { showTranslateSheet = false }) {
            Text(
                text = comment.content ?: "",
                modifier = Modifier.padding(16.dp)
            )
        }
    }

    if (isAlertPresented) {
        AlertDialog(
            onDismissRequest = { isAlertPresented = false },
            title = { Text(stringResource(alertState?.titleResId ?: R.string.error_unknown_try_again)) },
            text = {
                alertState?.message?.let { Text(it) } ?: alertState?.messageResId?.let {
                    Text(
                        stringResource(it)
                    )
                }
            },
            confirmButton = {
                TextButton(onClick = { isAlertPresented = false }) {
                    Text(stringResource(R.string.ok))
                }
            }
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
) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        ProfilePicture(url = comment.author.profile.profilePictureURL.toString())

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
                isComment = true
            )
        }
    }
}

@Composable
fun <T> PostCommentActionsMenu(
    enumClass: KClass<T>, //ara or feed report type
    isMine: Boolean?,
    onEdit: () -> Unit? = {},
    onDelete: () -> Unit,
    onReport: (T) -> Unit,
    onTranslate: () -> Unit,
    isComment: Boolean,
    modifier: Modifier = Modifier,
) where T : Enum<T>, T : ReportLabelProvider {
    var expanded by remember { mutableStateOf(false) }
    var reportExpanded by remember { mutableStateOf(false) }

    Box {
        Icon(
            imageVector = Icons.Rounded.MoreHoriz,
            contentDescription = stringResource(R.string.more),
            modifier = modifier
                .clickable { expanded = true },
            tint = if (isMine == true) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
        )

        DropdownMenu(
            modifier = Modifier.background(MaterialTheme.colorScheme.surface),
            shape = RoundedCornerShape(16.dp),
            expanded = expanded,
            onDismissRequest = {
                expanded = false
                reportExpanded = false
            }) {
            if (isMine == false) {
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
                            imageVector = Icons.AutoMirrored.Rounded.ArrowForwardIos,
                            contentDescription = "show Report",
                            modifier = Modifier
                                .size(18.dp)
                                .rotate(if (reportExpanded) 270f else 0f)
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
            } else {
                if (isComment && enumClass == AraContentReportType::class) {
                    //Only for Ara comments
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
            }
//
//            DropdownMenuItem(
//                text = { Text(stringResource(R.string.translate)) },
//                onClick = { onTranslate(); expanded = false },
//                leadingIcon = {
//                    Icon(
//                        painter = painterResource(R.drawable.baseline_translate),
//                        contentDescription = null
//                    )
//                }
//            ) TODO - TRANSLATE(API)
//            HorizontalDivider()

            if (isMine == true) {
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
                            contentDescription = null
                        )
                    }
                )
            }
        }
    }
}

@Composable
private fun ProfilePicture(url: String?) {
    if (!url.isNullOrEmpty()) {
        AsyncImage(
            model = url,
            contentDescription = null,
            modifier = Modifier
                .size(21.dp)
                .clip(CircleShape)
        )
    } else {
        Box(
            modifier = Modifier
                .size(21.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f))
        )
    }
}

@Composable
private fun PostCommentContent(isDeleted: Boolean, comment: AraPostComment) {
    AnimatedContent(targetState = comment.content) { content ->
        Text(
            text = content ?: stringResource(R.string.this_comment_has_been_deleted),
            style = MaterialTheme.typography.bodyMedium,
            modifier = Modifier.padding(vertical = 8.dp),
            color = if (isDeleted) MaterialTheme.colorScheme.grayBB.copy(alpha = 0.7f)
            else MaterialTheme.colorScheme.onSurface
        )
    }
}

@Composable
private fun PostCommentFooter(
    comment: AraPostComment,
    isThreaded: Boolean,
    isDeleted: Boolean,
    onComment: () -> Unit,
    onUpVote: () -> Unit,
    onDownVote: () -> Unit,
) {
    val scope = rememberCoroutineScope()
    var commentState by remember { mutableStateOf(comment) }

    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
        Spacer(modifier = Modifier.weight(1f))

        if (!isThreaded) {
            PostCommentButton(commentCount = comment.comments.size, onClick = onComment)
            Spacer(modifier = Modifier.padding(4.dp))
        }

        if (!isDeleted) {
            PostVoteButton(
                myVote = commentState.myVote,
                votes = commentState.upVotes - commentState.downVotes,
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
                enabled = commentState.isMine != true
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
