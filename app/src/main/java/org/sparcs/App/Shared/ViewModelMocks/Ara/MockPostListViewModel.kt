package org.sparcs.App.Shared.ViewModelMocks.Ara

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import org.sparcs.App.Domain.Models.Ara.AraBoard
import org.sparcs.App.Domain.Models.Ara.AraPost
import org.sparcs.App.Features.PostList.PostListViewModel
import org.sparcs.App.Features.PostList.PostListViewModelProtocol
import org.sparcs.App.Shared.Mocks.mock

class MockPostListViewModel(initialState: PostListViewModel.ViewState) : PostListViewModelProtocol {
    //MARK: - ViewModel Properties
    override var state: StateFlow<PostListViewModel.ViewState> = MutableStateFlow(initialState)
    override var board: AraBoard = AraBoard.mock()
    override var posts: List<AraPost> = emptyList()

    override var lastClickedPostId: Int? = null
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