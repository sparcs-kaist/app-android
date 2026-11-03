package org.sparcs.soap.buddyPreviewSupport.post

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import org.sparcs.soap.app.domain.models.ara.AraBoard
import org.sparcs.soap.app.domain.models.ara.AraBoardTopic
import org.sparcs.soap.app.domain.models.ara.AraPost
import org.sparcs.soap.app.features.postList.PostListViewModel
import org.sparcs.soap.app.features.postList.PostListViewModelProtocol

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

    override fun markRead(postID: Int) {
        posts = posts.map { if (it.id == postID) it.copy(readStatus = AraPost.ReadStatus.READ) else it }
        _state.value = PostListViewModel.ViewState.Loaded(posts)
    }

    override fun removePost(postID: Int) {
        posts = posts.filter { it.id != postID }
        _state.value = PostListViewModel.ViewState.Loaded(posts)
    }

    override fun onSearchTextChange(text: String) {
        _searchKeyword.value = text
    }

    override var selectedTopic: AraBoardTopic? = null

    override fun selectTopic(topic: AraBoardTopic?) {
        selectedTopic = topic
    }

    override fun bind() {}

    fun updateState(newState: PostListViewModel.ViewState) {
        _state.value = newState
    }
}