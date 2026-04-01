package org.sparcs.soap.BuddyPreviewSupport.Post

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import org.sparcs.soap.App.Domain.Models.Ara.AraPost
import org.sparcs.soap.App.Domain.Models.Ara.AraPostAuthor
import org.sparcs.soap.App.Features.UserPostList.UserPostListViewModel
import org.sparcs.soap.App.Features.UserPostList.UserPostListViewModelProtocol
import org.sparcs.soap.App.Shared.Mocks.Ara.mock

class PreviewUserPostListViewModel(
    initialState: UserPostListViewModel.ViewState,
    override val user: AraPostAuthor = AraPost.mock().author,
    initialPosts: List<AraPost> = emptyList(),
    initialSearchKeyword: String = "",
    initialIsLoadingMore: Boolean = false,
    override var hasMorePages: Boolean = false
) : UserPostListViewModelProtocol {

    private val _state = MutableStateFlow(initialState)
    override val state: StateFlow<UserPostListViewModel.ViewState> = _state.asStateFlow()

    private val _posts = MutableStateFlow(initialPosts)
    override var posts: StateFlow<List<AraPost>> = _posts.asStateFlow()

    private val _searchKeyword = MutableStateFlow(initialSearchKeyword)
    override var searchKeyword: StateFlow<String> = _searchKeyword.asStateFlow()

    private val _isLoadingMore = MutableStateFlow(initialIsLoadingMore)
    override val isLoadingMore: StateFlow<Boolean> = _isLoadingMore.asStateFlow()

    override var lastClickedPostId: Int? = null

    override fun onSearchTextChange(text: String) {
        _searchKeyword.value = text
    }

    override suspend fun fetchInitialPosts() { }
    override suspend fun loadNextPage() { }

    override fun refreshItem(postID: Int) { }
    override fun removePost(postID: Int) { }
    override fun bind() { }
}