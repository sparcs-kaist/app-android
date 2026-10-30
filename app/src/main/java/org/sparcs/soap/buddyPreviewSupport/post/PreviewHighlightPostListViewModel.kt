package org.sparcs.soap.buddyPreviewSupport.post

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import org.sparcs.soap.app.domain.models.ara.BoardListSection
import org.sparcs.soap.app.features.highlightPostList.HighlightPostListViewModel
import org.sparcs.soap.app.features.highlightPostList.HighlightPostListViewModelProtocol

class PreviewHighlightPostListViewModel(
    initialState: HighlightPostListViewModel.ViewState,
    override val section: BoardListSection = BoardListSection.Trending,
) : HighlightPostListViewModelProtocol {

    private val _state = MutableStateFlow(initialState)
    override val state: StateFlow<HighlightPostListViewModel.ViewState> = _state.asStateFlow()
    override val isRefreshing: StateFlow<Boolean> = MutableStateFlow(false).asStateFlow()
    override val isLoadingMore: StateFlow<Boolean> = MutableStateFlow(false).asStateFlow()
    override var lastClickedPostId: Int? = null

    override fun fetchInitialPosts() {}
    override fun refresh() {}
    override fun loadNextPage() {}
    override fun refreshItem(postID: Int) {}
    override fun markRead(postID: Int) {}
}
