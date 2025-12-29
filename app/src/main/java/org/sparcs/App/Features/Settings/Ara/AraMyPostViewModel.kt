package org.sparcs.App.Features.Settings.Ara

import android.net.Uri
import android.util.Log
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.gson.Gson
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import org.sparcs.App.Domain.Enums.Ara.PostListType
import org.sparcs.App.Domain.Enums.Ara.PostOrigin
import org.sparcs.App.Domain.Models.Ara.AraPost
import org.sparcs.App.Domain.Models.Ara.AraPostPage
import org.sparcs.App.Domain.Models.Ara.AraUser
import org.sparcs.App.Domain.Repositories.Ara.AraBoardRepositoryProtocol
import org.sparcs.App.Domain.Usecases.UserUseCase
import javax.inject.Inject

interface AraMyPostViewModelProtocol {
    val posts: StateFlow<List<AraPost>>
    val state: StateFlow<AraMyPostViewModel.ViewState>
    var type: AraMyPostViewModel.PostType
    var user: AraUser?
    val searchKeyword: StateFlow<String>

    fun onSearchTextChange(text: String)
    fun bind()
    suspend fun fetchInitialPosts()
    suspend fun loadNextPage()
    fun refreshItem(postID: Int)
}

@HiltViewModel
class AraMyPostViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    userUseCase: UserUseCase,
    private val araBoardRepository: AraBoardRepositoryProtocol,
) : ViewModel(), AraMyPostViewModelProtocol {

    sealed class ViewState {
        data object Loading : ViewState()
        data class Loaded(val posts: List<AraPost>) : ViewState()
        data class Error(val message: String) : ViewState()
    }

    private val _state = MutableStateFlow<ViewState>(ViewState.Loading)
    override val state: StateFlow<ViewState> = _state

    private val _posts = MutableStateFlow<List<AraPost>>(emptyList())
    override val posts: StateFlow<List<AraPost>> = _posts

    enum class PostType { ALL, BOOKMARK }

    private val initialType: PostType by lazy {
        val json = savedStateHandle.get<String>("type_json")
            ?: throw IllegalStateException("type_json is null. AraMyPostViewModel requires a type_json to initialize.")
        Gson().fromJson(Uri.decode(json), PostType::class.java)
    }
    override var type: PostType = initialType
    override var user: AraUser? = userUseCase.araUser

    private val _searchKeyword = MutableStateFlow("")
    override val searchKeyword: StateFlow<String> = _searchKeyword

    override fun onSearchTextChange(text: String) {
        _searchKeyword.value = text
    }

    private var isLoadingMore = false
    private var hasMorePages = true
    private var currentPage = 1
    private var totalPages = 0
    private val pageSize = 30

    @OptIn(FlowPreview::class)
    override fun bind() {
        viewModelScope.launch {
            _searchKeyword
                .debounce(350)
                .map { it.trim() }
                .distinctUntilChanged()
                .collect {
                    fetchInitialPosts()
                }
        }
    }

    override suspend fun fetchInitialPosts() {
        val currentUser = user ?: return
        _state.value = ViewState.Loading

        try {
            val page: AraPostPage = when (type) {
                PostType.ALL -> araBoardRepository.fetchPosts(
                    type = PostListType.User(currentUser.id),
                    page = 1,
                    pageSize = pageSize,
                    searchKeyword = _searchKeyword.value.takeIf { it.isNotEmpty() }
                )

                PostType.BOOKMARK -> araBoardRepository.fetchBookmarks(
                    page = 1,
                    pageSize = pageSize
                )
            }

            totalPages = page.pages
            currentPage = page.currentPage
            _posts.value = page.results
            hasMorePages = currentPage < totalPages
            _state.value = ViewState.Loaded(_posts.value)

        } catch (e: Exception) {
            _state.value = ViewState.Error(e.message ?: "Unknown error")
            Log.e("AraMyPostViewModel", "fetchInitialPosts failed$e")
        }
    }

    override suspend fun loadNextPage() {
        val user = user ?: return
        if (isLoadingMore || !hasMorePages) return

        isLoadingMore = true
        viewModelScope.launch {
            try {
                val nextPage = currentPage + 1
                val page: AraPostPage = when (type) {
                    PostType.ALL -> araBoardRepository.fetchPosts(
                        type = PostListType.User(user.id),
                        page = nextPage,
                        pageSize = pageSize,
                        searchKeyword = _searchKeyword.value.takeIf { it.isNotEmpty() }
                    )

                    PostType.BOOKMARK -> araBoardRepository.fetchBookmarks(
                        page = nextPage,
                        pageSize = pageSize
                    )
                }
                totalPages = page.pages
                currentPage = page.currentPage
                _posts.value += page.results
                hasMorePages = currentPage < totalPages
                _state.value = ViewState.Loaded(_posts.value)
            } catch (e: Exception) {
                _state.value = ViewState.Error(e.message ?: "Unknown error")
            } finally {
                isLoadingMore = false
            }
        }
    }

    override fun refreshItem(postID: Int) {
        viewModelScope.launch {
            try {
                val updated = araBoardRepository.fetchPost(PostOrigin.None, postID)
                val idx = _posts.value.indexOfFirst { it.id == updated.id }
                if (idx != -1) {
                    val newPosts = _posts.value.toMutableList()
                    val previous = newPosts[idx]
                    newPosts[idx] = previous.copy(
                        upVotes = updated.upVotes,
                        downVotes = updated.downVotes,
                        commentCount = updated.commentCount
                    )
                    _posts.value = newPosts
                    _state.value = ViewState.Loaded(_posts.value)
                }
            } catch (_: Exception) {
                Log.e("AraMyPostViewModel", "refreshItem failed")
            }
        }
    }
}
