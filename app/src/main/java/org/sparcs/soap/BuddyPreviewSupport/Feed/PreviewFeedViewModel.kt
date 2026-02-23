package org.sparcs.soap.BuddyPreviewSupport.Feed

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import org.sparcs.soap.App.Domain.Helpers.AlertState
import org.sparcs.soap.App.Domain.Models.Feed.FeedPost
import org.sparcs.soap.App.Features.Feed.FeedViewModel
import org.sparcs.soap.App.Features.Feed.FeedViewModelProtocol

class PreviewFeedViewModel(
    state: FeedViewModel.ViewState = FeedViewModel.ViewState.Loaded(emptyList()),
    override var posts: List<FeedPost> = emptyList(),
) : FeedViewModelProtocol {
    override var state: StateFlow<FeedViewModel.ViewState> = MutableStateFlow(state)
    override var alertState: AlertState? = null
    override var isAlertPresented: Boolean = false
    override var isLoadingMore: Boolean = false

    override suspend fun fetchInitialData() {}
    override suspend fun loadNextPage() {}
    override suspend fun deletePost(postID: String) {}
    override suspend fun upVote(postId: String) {}
    override suspend fun downVote(postId: String) {}

    override fun openSettingsTapped() {}
    override suspend fun refreshFeed() {}
    override fun writeFeedButtonTapped() {}
}