package com.example.soap.Features.Post

import android.util.Log
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import com.example.soap.Domain.Enums.AraContentReportType
import com.example.soap.Domain.Models.Ara.AraPost
import com.example.soap.Domain.Models.Ara.AraPostComment
import com.example.soap.Domain.Repositories.Ara.AraBoardRepositoryProtocol
import com.example.soap.Domain.Repositories.Ara.AraCommentRepositoryProtocol
import com.example.soap.Networking.RetrofitAPI.Ara.AraBoardTarget
import com.example.soap.Shared.Mocks.mock
import com.google.gson.Gson
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject

@HiltViewModel
class PostViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val araBoardRepository: AraBoardRepositoryProtocol,
    val araCommentRepository: AraCommentRepositoryProtocol,
//    private val foundationModelsUseCase: FoundationModelsUseCaseProtocol
) : ViewModel(), PostViewModelProtocol {

    // MARK: - Initialiser
    private val initialPost: AraPost by lazy {
        val json = savedStateHandle.get<String>("post_json")
            ?: throw IllegalStateException("post_json is null. PostViewModel requires a post_json to initialize.")
        Gson().fromJson(json, AraPost::class.java)
    }

    // MARK: - Properties
    private val _post = MutableStateFlow(initialPost)
    override val post : StateFlow<AraPost> = _post.asStateFlow()

    override val isFoundationModelsAvailable: Boolean
        get() = false // TODO: foundationModelsUseCase.isAvailable

    private fun insertThreadedComment(comments: MutableList<AraPostComment>, comment: AraPostComment): Boolean {
        val parentComment = comment.parentComment ?: return false
        for (idx in comments.indices) {
            if (comments[idx].id == parentComment) {
                comments[idx].comments.add(comment)
                return true
            }
        }
        return false
    }

    // MARK: - Functions
    override suspend fun fetchPost() {
        try {
            val fetchedPost = araBoardRepository.fetchPost(origin = AraBoardTarget.PostOrigin.Board, postID = _post.value.id)
            _post.value = fetchedPost
        } catch (e: Exception) {
            Log.e("PostViewModel", "fetchPost error", e)
        }
    }

    override suspend fun upVote() {
        val current = _post.value
        val previousMyVote = current.myVote
        val previousUpVotes = current.upVotes
        val previousDownVotes = current.downVotes

        val newPost = when (previousMyVote) {
            // cancel upvote
            true -> current.copy(myVote = null, upVotes = current.upVotes - 1)
            // upvote
            false -> current.copy(
                myVote = true,
                upVotes = current.upVotes + 1,
                // remove downvote if there was
                downVotes = current.downVotes - 1
            )
            null -> current.copy(myVote = true, upVotes = current.upVotes + 1)
        }
        _post.value = newPost

        try {
            when (previousMyVote) {
                true -> araBoardRepository.cancelVote(current.id)
                else -> araBoardRepository.upVotePost(current.id)
            }
        } catch (e: Exception) {
            Log.e("PostViewModel", "upvote error", e)
            _post.value = current.copy(
                myVote = previousMyVote,
                upVotes = previousUpVotes,
                downVotes = previousDownVotes
            )
        }
    }

    override suspend fun downVote() {
        val current = _post.value
        val previousMyVote = current.myVote
        val previousUpVotes = current.upVotes
        val previousDownVotes = current.downVotes

        val newPost = when (previousMyVote) {
            // cancel downvote
            false -> current.copy(myVote = null, downVotes = current.downVotes - 1)
            // downvote
            true -> current.copy(
                myVote = false,
                // remove upvote if there was
                upVotes = current.upVotes - 1,
                downVotes = current.downVotes + 1
            )
            null -> current.copy(myVote = false, downVotes = current.downVotes + 1)
        }
        _post.value = newPost

        try {
            when (previousMyVote) {
                false -> araBoardRepository.cancelVote(current.id)
                else -> araBoardRepository.downVotePost(current.id)
            }
        } catch (e: Exception) {
            Log.e("PostViewModel", "downvote error", e)
            _post.value = current.copy(
                myVote = previousMyVote,
                upVotes = previousUpVotes,
                downVotes = previousDownVotes
            )
        }
    }

    override suspend fun writeComment(content: String): AraPostComment {
        val comment = araCommentRepository.writeComment(postID = _post.value.id, content = content)
        comment.isMine = true
        _post.value.comments.add(comment)
        _post.value.commentCount += 1
        return comment
    }

    override suspend fun writeThreadedComment(commentID: Int, content: String): AraPostComment {
        val comment = araCommentRepository.writeThreadedComment(commentID = commentID, content = content)
        comment.isMine = true

        // insert threaded comments
        val comments = _post.value.comments.toMutableList()
        insertThreadedComment(comments, comment)
        _post.value.comments = comments
        _post.value.commentCount += 1

        return comment
    }

    override suspend fun editComment(commentID: Int, content: String): AraPostComment {
        val comment = araCommentRepository.editComment(commentID = commentID, content = content)
        comment.isMine = true

        for (i in _post.value.comments.indices) {
            if (_post.value.comments[i].id == commentID) {
                _post.value.comments[i].content = content
                return _post.value.comments[i]
            }

            // scan through threads
            for (j in _post.value.comments[i].comments.indices) {
                if (_post.value.comments[i].comments[j].id == commentID) {
                    _post.value.comments[i].comments[j].content = content
                    return _post.value.comments[i].comments[j]
                }
            }
        }

        return comment
    }

    override suspend fun report(type: AraContentReportType) {
        araBoardRepository.reportPost(postID = _post.value.id, type = type)
    }

    override suspend fun summarisedContent(): String {
//        return foundationModelsUseCase.summarise(post.content ?: "", maxWords = 50, tone = "concise")
        return ""
    }

    override suspend fun deletePost() {
        araBoardRepository.deletePost(postID = _post.value.id)
    }

    override suspend fun toggleBookmark() {
        val current = _post.value
        val previous = current.myScrap

        _post.value = current.copy(myScrap = !previous)

        try {
            if (previous) {
                val scrapId = current.scrapID ?: return
                araBoardRepository.removeBookmark(scrapId)
                _post.value = _post.value.copy(scrapID = null)
            } else {
                araBoardRepository.addBookmark(current.id)
            }
        } catch (e: Exception) {
            Log.e("PostViewModel", "toggleBookmark error", e)
            _post.value = current
        }
    }
}

class MockPostViewModel(
    initialPost: AraPost = AraPost.mock()
) : PostViewModelProtocol {

    override val isFoundationModelsAvailable = true

    private val _post = MutableStateFlow(initialPost)
    override val post: StateFlow<AraPost> = _post.asStateFlow()

    override suspend fun fetchPost() {}

    override suspend fun upVote() {}

    override suspend fun downVote() {}

    override suspend fun writeComment(content: String): AraPostComment {
        return AraPostComment.mock()
    }

    override suspend fun writeThreadedComment(commentID: Int, content: String): AraPostComment {
        return AraPostComment.mock()
    }

    override suspend fun editComment(commentID: Int, content: String): AraPostComment {
        return AraPostComment.mock()
    }

    override suspend fun report(type: AraContentReportType) {}

    override suspend fun summarisedContent(): String {
        return ""
    }

    override suspend fun deletePost() { }
    override suspend fun toggleBookmark() { }
}
