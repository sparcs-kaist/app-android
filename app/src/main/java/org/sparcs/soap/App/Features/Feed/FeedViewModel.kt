package org.sparcs.soap.App.Features.Feed

import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import org.sparcs.soap.App.Domain.Enums.Feed.FeedVoteType
import org.sparcs.soap.App.Domain.Error.Feed.FeedPostUseCaseError
import org.sparcs.soap.App.Domain.Helpers.AlertState
import org.sparcs.soap.App.Domain.Models.Feed.FeedPost
import org.sparcs.soap.App.Domain.Services.AnalyticsServiceProtocol
import org.sparcs.soap.App.Domain.Services.CrashlyticsServiceProtocol
import org.sparcs.soap.App.Domain.Usecases.Feed.FeedPostUseCaseProtocol
import org.sparcs.soap.App.Features.Feed.Event.FeedPostRowEvent
import org.sparcs.soap.App.Features.Feed.Event.FeedViewEvent
import org.sparcs.soap.R
import javax.inject.Inject

interface FeedViewModelProtocol {
    val state: StateFlow<FeedViewModel.ViewState>
    var posts: List<FeedPost>
    val alertState: AlertState?
    var isAlertPresented: Boolean
    var isLoadingMore: Boolean

    suspend fun fetchInitialData()
    suspend fun loadNextPage()
    suspend fun deletePost(postID: String)

    suspend fun upVote(postId: String)
    suspend fun downVote(postId: String)

    fun openSettingsTapped()
    suspend fun refreshFeed()
    fun writeFeedButtonTapped()
}

@HiltViewModel
class FeedViewModel @Inject constructor(
    private val feedPostUseCase: FeedPostUseCaseProtocol,
    private val crashlyticsService: CrashlyticsServiceProtocol,
    private val analyticsService: AnalyticsServiceProtocol,
) : ViewModel(), FeedViewModelProtocol {

    sealed interface ViewState {
        data object Loading : ViewState
        data class Loaded(val posts: List<FeedPost>) : ViewState
        data class Error(val message: String) : ViewState
    }

    // MARK: - Properties
    private var _state = MutableStateFlow<ViewState>(ViewState.Loading)
    override val state: StateFlow<ViewState> = _state.asStateFlow()

    override var posts: List<FeedPost> by mutableStateOf(emptyList())
    override var alertState: AlertState? by mutableStateOf(null)
    override var isAlertPresented: Boolean by mutableStateOf(false)

    //Infinite Scroll Properties
    private var nextCursor: String? = null
    override var isLoadingMore: Boolean = false
    private var hasNext: Boolean = false
    private var pageSize: Int = 20

    // MARK: - Functions
    override suspend fun fetchInitialData() {
        _state.value = ViewState.Loading
        try {
            val page = feedPostUseCase.fetchPosts(cursor = null, page = pageSize)
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
            val page = feedPostUseCase.fetchPosts(cursor = nextCursor, page = pageSize)
            posts = posts + page.items
            nextCursor = page.nextCursor
            hasNext = page.hasNext
            _state.value = ViewState.Loaded(posts)
            isLoadingMore = false
        } catch (e: Exception) {
            this.alertState = AlertState(
                titleResId = R.string.error,
                messageResId = R.string.unable_to_load_more_posts
            )
            this.isAlertPresented = true
            isLoadingMore = false
        }
    }

    override suspend fun deletePost(postID: String) {
        try {
            feedPostUseCase.deletePost(postID = postID)
            this.posts = this.posts.filterNot { it.id == postID }
        } catch (e: Exception) {
            val useCaseError = e as? FeedPostUseCaseError

            this.alertState = AlertState(
                titleResId = R.string.error,
                messageResId =  useCaseError?.messageRes ?: R.string.unexpected_error_deleting_post,
            )
            this.isAlertPresented = true
        }
    }

    // MARK: - Functions
    override suspend fun upVote(postId: String) {
        val prevPosts = this.posts
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

        this.posts = prevPosts.map {
            if (it.id == postId) updatedPost else it
        }


        try {
            if (current.myVote == FeedVoteType.UP) {
                feedPostUseCase.deleteVote(postID = postId)
            } else {
                feedPostUseCase.vote(postID = postId, type = FeedVoteType.UP)
            }
            analyticsService.logEvent(FeedPostRowEvent.PostUpVoted)
        } catch (e: Exception) {
            this.posts = prevPosts
            this.alertState = AlertState(
                titleResId = R.string.error_failed_to_upvote,
                message = e.localizedMessage ?: "Unknown error"
            )
            this.isAlertPresented = true
            crashlyticsService.recordException(e)
        }
    }

    override suspend fun downVote(postId: String) {
        val prevPosts = this.posts
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

        this.posts = prevPosts.map {
            if (it.id == postId) updatedPost else it
        }

        try {
            if (current.myVote == FeedVoteType.DOWN) {
                feedPostUseCase.deleteVote(postID = postId)
            } else {
                feedPostUseCase.vote(postID = postId, type = FeedVoteType.DOWN)
            }
            analyticsService.logEvent(FeedPostRowEvent.PostDownVoted)
        } catch (e: Exception) {
            this.posts = prevPosts
            this.alertState = AlertState(
                titleResId = R.string.error_failed_to_downvote,
                message = e.localizedMessage ?: "Unknown error"
            )
            this.isAlertPresented = true
            crashlyticsService.recordException(e)
        }
    }

    override fun openSettingsTapped() {
        analyticsService.logEvent(FeedViewEvent.OpenSettingsButtonTapped)
    }

    override suspend fun refreshFeed() {
            fetchInitialData()
        analyticsService.logEvent(FeedViewEvent.FeedRefreshed)
    }

    override fun writeFeedButtonTapped() {
        analyticsService.logEvent(FeedViewEvent.WriteFeedButtonTapped)
    }

}