package com.sparcs.soap.Shared.ViewModelMocks.Feed

import com.sparcs.soap.Domain.Models.Feed.FeedPost
import com.sparcs.soap.Features.Feed.FeedViewModel
import com.sparcs.soap.Features.Feed.FeedViewModelProtocol
import com.sparcs.soap.Shared.Mocks.mockList
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class MockFeedViewModel(initialState: FeedViewModel.ViewState): FeedViewModelProtocol {
    private val _state = MutableStateFlow(initialState)
    override val state: StateFlow<FeedViewModel.ViewState> = _state

    private val _posts = MutableStateFlow<List<FeedPost>>(FeedPost.mockList())
    override val posts: StateFlow<List<FeedPost>> = _posts.asStateFlow()
    override suspend fun signOut() {}
    override suspend fun fetchInitialData() {}
    override suspend fun deletePost(postID: String) {}
}