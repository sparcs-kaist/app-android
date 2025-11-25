package com.sparcs.soap.Shared.ViewModelMocks.Ara

import com.sparcs.soap.Domain.Models.Ara.AraBoard
import com.sparcs.soap.Domain.Models.Ara.AraPost
import com.sparcs.soap.Features.PostList.PostListViewModel
import com.sparcs.soap.Features.PostList.PostListViewModelProtocol
import com.sparcs.soap.Shared.Mocks.mock
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class MockPostListViewModel(initialState: PostListViewModel.ViewState) : PostListViewModelProtocol {
    //MARK: - ViewModel Properties
    override var state: StateFlow<PostListViewModel.ViewState> = MutableStateFlow(initialState)
    override var board: AraBoard = AraBoard.mock()
    override var posts: List<AraPost> = emptyList()

    private val _searchKeyword = MutableStateFlow("")
    override val searchKeyword: StateFlow<String> = _searchKeyword

    override var isLoadingMore: Boolean = false
    override var hasMorePages: Boolean = true

    // MARK: - Functions
    override fun onSearchTextChange(text: String) {}
    override suspend fun fetchInitialPosts() {}
    override suspend fun loadNextPage(){}
    override fun refreshItem(postID: Int){}
    override fun removePost(postID: Int){}
    override fun bind(){}
}