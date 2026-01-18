package org.sparcs.App.Features.Feed

import android.util.Log
import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import org.sparcs.App.Domain.Enums.Feed.FeedVoteType
import org.sparcs.App.Domain.Models.Feed.FeedPost
import org.sparcs.App.Domain.Repositories.Feed.FeedPostRepositoryProtocol
import javax.inject.Inject

interface FeedViewModelProtocol {
    val state: StateFlow<FeedViewModel.ViewState>
    val posts: List<FeedPost>
    var isLoadingMore: Boolean
    var hasNext: Boolean

    suspend fun fetchInitialData()
    suspend fun loadNextPage()
    suspend fun deletePost(postID: String)

    suspend fun upVote(postId: String)
    suspend fun downVote(postId: String)
}

@HiltViewModel
class FeedViewModel @Inject constructor(
    val feedPostRepository: FeedPostRepositoryProtocol,
) : ViewModel(), FeedViewModelProtocol {

    sealed interface ViewState {
        data object Loading : ViewState
        data class Loaded(val posts: List<FeedPost>) : ViewState
        data class Error(val message: String) : ViewState
    }

    private val _state = MutableStateFlow<ViewState>(ViewState.Loading)
    override val state: StateFlow<ViewState> = _state.asStateFlow()

    override var posts: List<FeedPost> = emptyList()

    private var nextCursor: String? = null

    //Infinite Scroll Properties
    override var isLoadingMore: Boolean = false
    override var hasNext: Boolean = false
    private var pageSize: Int = 20

    override suspend fun fetchInitialData() {
        _state.value = ViewState.Loading
        try {
            val page = feedPostRepository.fetchPosts(cursor = null, page = pageSize)
            posts = page.items
            nextCursor = page.nextCursor
            hasNext = page.hasNext
            _state.value = ViewState.Loaded(posts)
        } catch (e: Exception) {
            Log.e("FeedViewModel", "failed to fetch data", e)
            _state.value = ViewState.Error(e.localizedMessage ?: "Unknown error")
        }
    }

    override suspend fun loadNextPage() {
        if (isLoadingMore || !hasNext) return
        isLoadingMore = true
        try {
            val page = feedPostRepository.fetchPosts(cursor = nextCursor, page = pageSize)
            posts = posts + page.items
            nextCursor = page.nextCursor
            hasNext = page.hasNext
            _state.value = ViewState.Loaded(posts)
            isLoadingMore = false

        } catch (e: Exception) {
            Log.e("FeedViewModel", "Error loading next page: $e")
            isLoadingMore = false
        }
    }

    override suspend fun deletePost(postID: String) {
        try {
            feedPostRepository.deletePost(postID)
            posts = posts.filterNot { it.id == postID }
        } catch (e: Exception) {
            Log.e("FeedViewModel", "failed to delete post", e)
            throw e
        }
    }

    // MARK: - Functions
    override suspend fun upVote(postId: String) {
        val currentState = _state.value
        if (currentState !is ViewState.Loaded) return

        val prevPosts = currentState.posts
        val current = prevPosts.firstOrNull { it.id == postId } ?: return

        val updatedPost = when (current.myVote) {
            FeedVoteType.UP ->
                current.copy(myVote = null, upVotes = current.upVotes - 1)

            FeedVoteType.DOWN ->
                current.copy(
                    myVote = FeedVoteType.UP,
                    upVotes = current.upVotes + 1,
                    downVotes = current.downVotes - 1
                )

            null ->
                current.copy(myVote = FeedVoteType.UP, upVotes = current.upVotes + 1)
        }

        val updatedPosts = prevPosts.map {
            if (it.id == postId) updatedPost else it
        }

        posts = updatedPosts
        _state.value = ViewState.Loaded(updatedPosts)

        try {
            if (current.myVote == FeedVoteType.UP) {
                feedPostRepository.deleteVote(postId)
            } else {
                feedPostRepository.vote(postId, FeedVoteType.UP)
            }
        } catch (e: Exception) {
            posts = prevPosts
            _state.value = ViewState.Loaded(prevPosts)
        }
    }

    override suspend fun downVote(postId: String) {
        val currentState = _state.value
        if (currentState !is ViewState.Loaded) return

        val prevPosts = currentState.posts
        val current = prevPosts.firstOrNull { it.id == postId } ?: return

        val updatedPost = when (current.myVote) {
            FeedVoteType.DOWN ->
                current.copy(myVote = null, downVotes = current.downVotes - 1)

            FeedVoteType.UP ->
                current.copy(
                    myVote = FeedVoteType.DOWN,
                    upVotes = current.upVotes - 1,
                    downVotes = current.downVotes + 1
                )

            null ->
                current.copy(myVote = FeedVoteType.DOWN, downVotes = current.downVotes + 1)
        }

        val updatedPosts = prevPosts.map {
            if (it.id == postId) updatedPost else it
        }

        posts = updatedPosts
        _state.value = ViewState.Loaded(updatedPosts)

        try {
            if (current.myVote == FeedVoteType.DOWN) {
                feedPostRepository.deleteVote(postId)
            } else {
                feedPostRepository.vote(postId, FeedVoteType.DOWN)
            }
        } catch (e: Exception) {
            posts = prevPosts
            _state.value = ViewState.Loaded(prevPosts)
        }
    }
}