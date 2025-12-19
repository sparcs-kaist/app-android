package com.sparcs.soap.Shared.ViewModelMocks.Feed

import com.sparcs.soap.Domain.Models.Feed.FeedPost
import com.sparcs.soap.Features.Feed.FeedViewModel
import com.sparcs.soap.Features.Feed.FeedViewModelProtocol
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

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