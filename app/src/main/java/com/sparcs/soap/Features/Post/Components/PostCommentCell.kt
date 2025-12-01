package com.sparcs.soap.Features.Post

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
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.sparcs.soap.Domain.Enums.Ara.AraContentReportType
import com.sparcs.soap.Domain.Enums.Feed.ReportLabelProvider
import com.sparcs.soap.Domain.Models.Ara.AraPostComment
import com.sparcs.soap.Domain.Repositories.Ara.AraCommentRepositoryProtocol
import com.sparcs.soap.Domain.Repositories.Ara.FakeAraCommentRepository
import com.sparcs.soap.Features.Post.Components.PostCommentButton
import com.sparcs.soap.Features.Post.Components.PostVoteButton
import com.sparcs.soap.R
import com.sparcs.soap.Shared.Extensions.timeAgoDisplay
import com.sparcs.soap.Shared.Mocks.mock
import com.sparcs.soap.ui.theme.grayBB
import com.sparcs.soap.ui.theme.lightGray0
import kotlinx.coroutines.launch
import kotlin.reflect.KClass

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PostCommentCell(
    comment: AraPostComment,
    isThreaded: Boolean,
    onComment: () -> Unit,
    onDelete: () -> Unit,
    onEdit: () -> Unit,
    onTranslate: (String) -> Unit,
    araCommentRepository: AraCommentRepositoryProtocol,
) {
    val scope = rememberCoroutineScope()
    var showReportDialog by remember { mutableStateOf(false) }
    var showTranslateSheet by remember { mutableStateOf(false) }
    var commentState by remember { mutableStateOf(comment) }
    val isDeleted = commentState.content == null

    Row(modifier = Modifier.fillMaxWidth()) {
        if (isThreaded) {
            Icon(
                painter = painterResource(R.drawable.round_subdirectory_arrow_right),
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
                comment = commentState,
                isDeleted = isDeleted,
                onEdit = onEdit,
                onDelete = {
                    scope.launch {
                        val prev = commentState.content
                        try {
                            commentState = commentState.copy(content = null)
                            onDelete()
                            araCommentRepository.deleteComment(commentState.id)
                        } catch (e: Exception) {
                            commentState.content = prev
                        }
                    }
                },
                onReport = { type ->
                    scope.launch {
                        araCommentRepository.reportComment(commentState.id, type)
                        showReportDialog = true
                    }
                },
                onTranslate = {
                    commentState.content?.let { text ->
                        onTranslate(text)
                        showTranslateSheet = true
                    }
                }
            )

            PostCommentContent(
                isDeleted = isDeleted,
                comment = commentState
            )
            PostCommentFooter(
                comment = commentState,
                isThreaded = isThreaded,
                isDeleted = isDeleted,
                onComment = onComment,
                araCommentRepository = araCommentRepository
            )
        }
    }

    if (showReportDialog) {
        AlertDialog(
            onDismissRequest = { showReportDialog = false },
            confirmButton = {
                TextButton(onClick = { showReportDialog = false }) {
                    Text(
                        stringResource(R.string.ok)
                    )
                }
            },
            title = { Text(stringResource(R.string.report_submitted)) },
            text = { Text(stringResource(R.string.reported_successfully)) }
        )
    }

    if (showTranslateSheet) {
        ModalBottomSheet(onDismissRequest = { showTranslateSheet = false }) {
            Text(
                text = commentState.content ?: "",
                modifier = Modifier.padding(16.dp)
            )
        }
    }
}

@Composable
fun PostCommentHeader(
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
            painter = painterResource(R.drawable.more_horiz),
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
                            painter = painterResource(R.drawable.outline_sms_failed),
                            contentDescription = null
                        )
                    },
                    trailingIcon = {
                        Icon(
                            painter = painterResource(R.drawable.arrow_forward_ios),
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
                if (isComment) {
                    DropdownMenuItem(
                        text = { Text(stringResource(R.string.edit)) },
                        onClick = { onEdit(); expanded = false },
                        leadingIcon = {
                            Icon(
                                painter = painterResource(R.drawable.outline_edit),
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
                            painter = painterResource(R.drawable.outline_delete),
                            contentDescription = null
                        )
                    }
                )
            }
        }
    }
}

@Composable
fun ProfilePicture(url: String?) {
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
fun PostCommentContent(isDeleted: Boolean, comment: AraPostComment) {
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
fun PostCommentFooter(
    comment: AraPostComment,
    isThreaded: Boolean,
    isDeleted: Boolean,
    onComment: () -> Unit,
    araCommentRepository: AraCommentRepositoryProtocol,
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
                        handleVote(commentState, true, araCommentRepository) { updated ->
                            commentState = updated
                        }
                    }
                },
                onDownVote = {
                    scope.launch {
                        handleVote(commentState, false, araCommentRepository) { updated ->
                            commentState = updated
                        }
                    }
                },
                enabled = commentState.isMine == true
            )
        }
    }
}

suspend fun handleVote(
    comment: AraPostComment,
    isUpVote: Boolean,
    repo: AraCommentRepositoryProtocol,
    update: (AraPostComment) -> Unit,
) {
    val prev = comment.copy()

    val updated = when {
        isUpVote && comment.myVote == true -> comment.copy(
            myVote = null,
            upVotes = comment.upVotes - 1
        )

        isUpVote && comment.myVote == false -> comment.copy(
            myVote = true,
            upVotes = comment.upVotes + 1,
            downVotes = comment.downVotes - 1
        )

        isUpVote -> comment.copy(myVote = true, upVotes = comment.upVotes + 1)
        !isUpVote && comment.myVote == false -> comment.copy(
            myVote = null,
            downVotes = comment.downVotes - 1
        )

        !isUpVote && comment.myVote == true -> comment.copy(
            myVote = false,
            upVotes = comment.upVotes - 1,
            downVotes = comment.downVotes + 1
        )

        else -> comment.copy(myVote = false, downVotes = comment.downVotes + 1)
    }

    update(updated)

    try {
        if (isUpVote) {
            if (prev.myVote == true) repo.cancelVote(prev.id) else repo.upVoteComment(prev.id)
        } else {
            if (prev.myVote == false) repo.cancelVote(prev.id) else repo.downVoteComment(prev.id)
        }
    } catch (e: Exception) {
        update(prev)
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
        onTranslate = {},
        FakeAraCommentRepository()
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
        onTranslate = {},
        FakeAraCommentRepository()
    )
}
