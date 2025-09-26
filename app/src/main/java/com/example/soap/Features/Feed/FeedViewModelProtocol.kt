package com.example.soap.Features.Feed

import com.example.soap.Domain.Models.Feed.FeedPost
import kotlinx.coroutines.flow.StateFlow

interface FeedViewModelProtocol {
    val state: StateFlow<FeedViewModel.ViewState>
    val posts: StateFlow<List<FeedPost>>

    suspend fun signOut()
    suspend fun fetchInitialData()
    suspend fun deletePost(postID: String)
}
