package org.sparcs.soap.app.features.feed

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.navigation.NavHostController
import androidx.navigation.compose.currentBackStackEntryAsState
import org.sparcs.soap.app.features.feedPost.FeedPostView
import org.sparcs.soap.app.features.feedPost.FeedPostViewModelProtocol
import org.sparcs.soap.app.features.navigationBar.Channel

/** Shares the active post with the feed so resizing does not discard the composer. */
@Composable
fun FeedPostRoute(
    postID: String,
    commentID: String?,
    viewModel: FeedPostViewModelProtocol,
    feedViewModel: FeedViewModelProtocol,
    navController: NavHostController,
    split: FeedSplit? = rememberFeedSplit(),
    selection: MutableState<FeedPostSelection>,
) {
    val currentEntry by navController.currentBackStackEntryAsState()
    val isCurrentPost = currentEntry?.destination?.route?.substringBefore('?') == Channel.FeedPost.name &&
        currentEntry?.arguments?.getString("feedId") == postID

    LaunchedEffect(postID, commentID, split, isCurrentPost, feedViewModel.posts) {
        if (!isCurrentPost) return@LaunchedEffect
        // A linked post may not belong to the loaded feed; keep its full detail in that case.
        val showPanel = split != null && feedViewModel.posts.any { it.id == postID }
        selection.value = FeedPostSelection(
            postID,
            commentID ?: selection.value.takeIf { it.postID == postID }?.commentID,
            isPanel = showPanel,
        )
        if (showPanel) navController.popBackStack(Channel.Start.name, inclusive = false)
    }

    FeedPostView(viewModel, feedViewModel, navController, initialCommentID = selection.value.commentID)
}
