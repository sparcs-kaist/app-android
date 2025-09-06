package com.example.soap.Features.UserPostList

import com.example.soap.Domain.Models.Ara.AraPost
import com.example.soap.Domain.Models.Ara.AraPostAuthor
import kotlinx.coroutines.flow.StateFlow

interface UserPostListViewModelProtocol {
    val state: StateFlow<UserPostListViewModel.ViewState>
    val user: StateFlow<AraPostAuthor?>
    var posts: StateFlow<List<AraPost>>
    var searchKeyword: StateFlow<String>

    val isLoadingMore: StateFlow<Boolean>
    var hasMorePages: Boolean

    suspend fun fetchInitialPosts()
    suspend fun loadNextPage()
    fun refreshItem(postID: Int)
    fun removePost(postID: Int)
    fun bind()
}