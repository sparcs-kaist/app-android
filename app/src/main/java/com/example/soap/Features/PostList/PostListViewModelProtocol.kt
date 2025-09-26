package com.example.soap.Features.PostList

import com.example.soap.Domain.Models.Ara.AraBoard
import com.example.soap.Domain.Models.Ara.AraPost
import kotlinx.coroutines.flow.StateFlow

interface PostListViewModelProtocol {
    var state: StateFlow<PostListViewModel.ViewState>
    var board: AraBoard
    var posts: List<AraPost>
    var searchKeyword: String

    var isLoadingMore: Boolean
    var hasMorePages: Boolean

    suspend fun fetchInitialPosts()
    suspend fun loadNextPage()
    fun refreshItem(postID: Int)
    fun removePost(postID: Int)
    fun bind()
}