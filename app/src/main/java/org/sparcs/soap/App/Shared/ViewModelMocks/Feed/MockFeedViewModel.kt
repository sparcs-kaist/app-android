package org.sparcs.soap.App.Shared.ViewModelMocks.Feed

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import org.sparcs.soap.App.Domain.Models.Feed.FeedPost
import org.sparcs.soap.App.Features.Feed.FeedViewModel
import org.sparcs.soap.App.Features.Feed.FeedViewModelProtocol

class MockFeedViewModel(initialState: FeedViewModel.ViewState): FeedViewModelProtocol {
    private val _state = MutableStateFlow(initialState)
    override val state: StateFlow<FeedViewModel.ViewState> = _state
    override val posts: List<FeedPost> = emptyList()
    override var isLoadingMore: Boolean = false
    override var hasNext: Boolean = false

    override suspend fun fetchInitialData() {}
    override suspend fun loadNextPage() {}
    override suspend fun deletePost(postID: String) {}

    override suspend fun upVote(postId: String) {}
    override suspend fun downVote(postId: String) {}
}