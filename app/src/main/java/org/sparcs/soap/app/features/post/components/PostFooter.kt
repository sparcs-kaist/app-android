package org.sparcs.soap.app.features.post.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import org.sparcs.soap.app.domain.helpers.Constants
import org.sparcs.soap.app.domain.models.ara.AraPost
import org.sparcs.soap.app.shared.mocks.ara.mock
import org.sparcs.soap.app.theme.ui.Theme

@Composable
fun PostFooter(
    post: AraPost,
    onUpVote: () -> Unit,
    onDownVote: () -> Unit,
    onToggleBookmark: () -> Unit,
    onCommentClick: () -> Unit,
) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        PostVoteButton(
            myVote = post.myVote,
            votes = post.upVotes - post.downVotes,
            onUpVote = onUpVote,
            onDownVote = onDownVote,
            enabled = true
        )
        PostCommentButton(commentCount = post.commentCount, onClick = onCommentClick)
        Spacer(Modifier.weight(1f))
        PostBookmarkButton(post.myScrap, onToggleBookmark = onToggleBookmark)
        PostShareButton(url = Constants.ARA_SHARE_URL + post.id.toString(), context = LocalContext.current)
    }
}

@Preview(showBackground = true)
@Composable
private fun PostFooterPreview() {
    Theme {
        PostFooter(AraPost.mock(), onUpVote = {}, onDownVote = {}, onToggleBookmark = {}, onCommentClick = {})
    }
}
