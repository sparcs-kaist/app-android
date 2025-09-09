
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import com.example.soap.Domain.Enums.AraContentReportType
import com.example.soap.Domain.Models.Ara.AraPostComment
import com.example.soap.Domain.Repositories.Ara.AraCommentRepositoryProtocol
import com.example.soap.Domain.Repositories.Ara.FakeAraCommentRepository
import com.example.soap.Features.Post.Components.PostCommentButton
import com.example.soap.Features.Post.Components.PostVoteButton
import com.example.soap.R
import com.example.soap.Shared.Extensions.timeAgoDisplay
import com.example.soap.Shared.Mocks.mock
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PostCommentCell(
    comment: AraPostComment,
    isThreaded: Boolean,
    onComment: () -> Unit,
    onDelete: () -> Unit,
    onEdit: () -> Unit,
    onTranslate: (String) -> Unit,
    araCommentRepository: AraCommentRepositoryProtocol = hiltViewModel()
) {
    val scope = rememberCoroutineScope()
    var showReportDialog by remember { mutableStateOf(false) }
    var showTranslateSheet by remember { mutableStateOf(false) }
    val isDeleted = comment.content == null

    Row(modifier = Modifier.fillMaxWidth()) {
        if (isThreaded) {
            Icon(
                painter = painterResource(R.drawable.round_subdirectory_arrow_right),
                contentDescription = null,
                modifier = Modifier.padding(top = 8.dp, end = 4.dp)
            )
        }

        Column(modifier = Modifier.weight(1f)) {
            HorizontalDivider()

            PostCommentHeader(
                comment = comment,
                isDeleted = isDeleted,
                onEdit = onEdit,
                onDelete = {
                    scope.launch {
                        val prev = comment.content
                        try {
                            comment.content = null
                            onDelete()
                            araCommentRepository.deleteComment(comment.id)
                        } catch (e: Exception) {
                            comment.content = prev
                        }
                    }
                },
                onReport = { type ->
                    scope.launch {
                        araCommentRepository.reportComment(comment.id, type)
                        showReportDialog = true
                    }
                },
                onTranslate = {
                    comment.content?.let { text ->
                        onTranslate(text)
                        showTranslateSheet = true
                    }
                }
            )

            PostCommentContent(comment = comment)
            PostCommentFooter(
                comment = comment,
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
            confirmButton = { TextButton(onClick = { showReportDialog = false }) { Text("OK") } },
            title = { Text("Report Submitted") },
            text = { Text("Your report has been submitted successfully.") }
        )
    }

    if (showTranslateSheet) {
        ModalBottomSheet(onDismissRequest = { showTranslateSheet = false }) {
            Text(
                text = comment.content ?: "",
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
    onTranslate: () -> Unit
) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        ProfilePicture(url = comment.author.profile.profilePictureURL.toString())

        Text(
            text = comment.author.profile.nickname,
            fontWeight = FontWeight.Medium,
            style = MaterialTheme.typography.bodyMedium
        )

        Spacer(modifier = Modifier.width(4.dp))

        Text(
            text = comment.createdAt.timeAgoDisplay(),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(modifier = Modifier.weight(1f))

        if (!isDeleted) {
            PostCommentActionsMenu(
                comment = comment,
                onEdit = onEdit,
                onDelete = onDelete,
                onReport = onReport,
                onTranslate = onTranslate
            )
        }
    }
}

@Composable
fun PostCommentActionsMenu(
    comment: AraPostComment,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    onReport: (AraContentReportType) -> Unit,
    onTranslate: () -> Unit
) {
    var expanded by remember { mutableStateOf(false) }
    var reportExpanded by remember { mutableStateOf(false) }

    Box {
        IconButton(onClick = { expanded = true }) {
            Icon(Icons.Default.MoreVert, contentDescription = "More")
        }

        DropdownMenu(expanded = expanded, onDismissRequest = {
            expanded = false
            reportExpanded = false
        }) {
            if (comment.isMine == false) {
                DropdownMenuItem(
                    text = { Text("Report") },
                    onClick = { reportExpanded = true },
                    leadingIcon = {
                        Icon(
                            painter = painterResource(R.drawable.outline_sms_failed),
                            contentDescription = null
                        )
                    }
                )
                if (reportExpanded) {
                    AraContentReportType.entries.forEach { type ->
                        DropdownMenuItem(
                            text = { Text(type.name.replace("_", " ").replaceFirstChar { it.uppercase() }) },
                            onClick = {
                                onReport(type)
                                expanded = false
                                reportExpanded = false
                            }
                        )
                    }
                }
            } else {
                DropdownMenuItem(
                    text = { Text("Edit") },
                    onClick = { onEdit(); expanded = false },
                    leadingIcon = {
                        Icon(
                            painter = painterResource(R.drawable.outline_edit),
                            contentDescription = null
                        )
                    }
                )
            }

            HorizontalDivider()

            DropdownMenuItem(
                text = { Text("Translate") },
                onClick = { onTranslate(); expanded = false },
                leadingIcon = {
                    Icon(
                        painter = painterResource(R.drawable.baseline_translate),
                        contentDescription = null
                    )
                }
            )

            if (comment.isMine == true) {
                HorizontalDivider()
                DropdownMenuItem(
                    text = { Text("Delete") },
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
            modifier = Modifier.size(21.dp).clip(CircleShape)
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
fun PostCommentContent(comment: AraPostComment) {
    val isDeleted = comment.content == null
    Text(
        text = comment.content ?: "This comment has been deleted.",
        style = MaterialTheme.typography.bodySmall,
        color = if (isDeleted) MaterialTheme.colorScheme.onSurfaceVariant
        else MaterialTheme.colorScheme.onSurface
    )
}

@Composable
fun PostCommentFooter(
    comment: AraPostComment,
    isThreaded: Boolean,
    isDeleted: Boolean,
    onComment: () -> Unit,
    araCommentRepository: AraCommentRepositoryProtocol
) {
    val scope = rememberCoroutineScope()

    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
        Spacer(modifier = Modifier.weight(1f))

        if (!isThreaded) {
            PostCommentButton(commentCount = comment.comments.size, onClick = onComment)
        }

        if (!isDeleted) {
            PostVoteButton(
                myVote = comment.myVote,
                votes = comment.upVotes - comment.downVotes,
                onUpVote = {
                    scope.launch { handleVote(comment, true, araCommentRepository) }
                },
                onDownVote = {
                    scope.launch { handleVote(comment, false, araCommentRepository) }
                }
            )
        }
    }
}

suspend fun handleVote(comment: AraPostComment, isUpVote: Boolean, repo: AraCommentRepositoryProtocol) {
    val prevVote = comment.myVote
    val prevUp = comment.upVotes
    val prevDown = comment.downVotes

    try {
        if (isUpVote) {
            if (prevVote == true) {
                comment.myVote = null
                comment.upVotes--
                repo.cancelVote(comment.id)
            } else {
                if (prevVote == false) comment.downVotes--
                comment.myVote = true
                comment.upVotes++
                repo.upVoteComment(comment.id)
            }
        } else {
            if (prevVote == false) {
                comment.myVote = null
                comment.downVotes--
                repo.cancelVote(comment.id)
            } else {
                if (prevVote == true) comment.upVotes--
                comment.myVote = false
                comment.downVotes++
                repo.downVoteComment(comment.id)
            }
        }
    } catch (e: Exception) {
        comment.upVotes = prevUp
        comment.downVotes = prevDown
        comment.myVote = prevVote
    }
}

@Composable
@Preview(showBackground = true)
private fun Preview1(){
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
private fun Preview2(){
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
