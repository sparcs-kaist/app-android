package org.sparcs.Shared.ViewModelMocks.Ara

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import org.sparcs.Domain.Models.Ara.AraPost
import org.sparcs.Domain.Models.Ara.AraPostAuthor
import org.sparcs.Domain.Models.Ara.AraPostAuthorProfile
import org.sparcs.Features.UserPostList.UserPostListViewModel.ViewState
import org.sparcs.Features.UserPostList.UserPostListViewModelProtocol

class MockUserPostListViewModel(initialState: ViewState) :
    UserPostListViewModelProtocol {

    private val _state = MutableStateFlow<ViewState>(initialState)
    override val state: StateFlow<ViewState> = _state.asStateFlow()

    override val user = AraPostAuthor(
        id = "1", username = "MockUser", profile = AraPostAuthorProfile(
            id = "12",
            profilePictureURL = null,
            nickname = "nickName",
            isOfficial = false,
            isSchoolAdmin = false

        ),
        isBlocked = false
    )

    private val _posts = MutableStateFlow<List<AraPost>>(emptyList())
    override var posts: StateFlow<List<AraPost>> = _posts

    private val _searchKeyword = MutableStateFlow("")
    override var searchKeyword: StateFlow<String> = _searchKeyword

    private val _isLoadingMore = MutableStateFlow(false)
    override val isLoadingMore: StateFlow<Boolean> = _isLoadingMore

    override var hasMorePages: Boolean = false

    override fun onSearchTextChange(text: String) {}
    override suspend fun fetchInitialPosts() {}
    override suspend fun loadNextPage() {}
    override fun refreshItem(postID: Int) {}
    override fun removePost(postID: Int) {}
    override fun bind() {}
}
