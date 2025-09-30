package com.example.soap.Features.Settings.Ara

import com.example.soap.Domain.Models.Ara.AraPost
import com.example.soap.Domain.Models.Ara.AraUser
import kotlinx.coroutines.flow.StateFlow

interface AraMyPostViewModelProtocol {
    val posts: StateFlow<List<AraPost>>
    val state: StateFlow<AraMyPostViewModel.ViewState>
    var type: AraMyPostViewModel.PostType
    var user: AraUser?
    var searchKeyword: String

    fun bind()
    suspend fun fetchInitialPosts()
    suspend fun loadNextPage()
    fun refreshItem(postID: Int)
}