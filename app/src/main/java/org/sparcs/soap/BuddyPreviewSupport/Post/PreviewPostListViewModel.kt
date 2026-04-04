package org.sparcs.soap.BuddyPreviewSupport.Post

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import org.sparcs.soap.App.Domain.Models.Ara.AraBoard
import org.sparcs.soap.App.Domain.Models.Ara.AraPost
import org.sparcs.soap.App.Features.PostList.PostListViewModel
import org.sparcs.soap.App.Features.PostList.PostListViewModelProtocol

class PreviewPostListViewModel(
    initialState: PostListViewModel.ViewState = PostListViewModel.ViewState.Loading,
    override var board: AraBoard,
    override var posts: List<AraPost> = emptyList(),
    override var isLoadingMore: Boolean = false,
    override var hasMorePages: Boolean = false,
    initialSearchKeyword: String = ""
) : PostListViewModelProtocol {

    private val _state = MutableStateFlow(initialState)
    override var state: StateFlow<PostListViewModel.ViewState> = _state.asStateFlow()

    private val _searchKeyword = MutableStateFlow(initialSearchKeyword)
    override val searchKeyword: StateFlow<String> = _searchKeyword

    override var lastClickedPostId: Int? = null

    override fun fetchInitialPosts() {}

    override suspend fun loadNextPage() {}

    override fun refreshItem(postID: Int) {}

    override fun removePost(postID: Int) {
        posts = posts.filter { it.id != postID }
        _state.value = PostListViewModel.ViewState.Loaded(posts)
    }

    override fun onSearchTextChange(text: String) {
        _searchKeyword.value = text
    }

    override fun bind() {}

    fun updateState(newState: PostListViewModel.ViewState) {
        _state.value = newState
    }
}