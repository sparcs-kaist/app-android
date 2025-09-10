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
    private val araCommentRepository: AraCommentRepositoryProtocol,
//    private val foundationModelsUseCase: FoundationModelsUseCaseProtocol
) : ViewModel(), PostViewModelProtocol {

    override val isFoundationModelsAvailable: Boolean
        get() = false // TODO: foundationModelsUseCase.isAvailable

    private val initialPost: AraPost by lazy {
        val json = savedStateHandle.get<String>("post_json")
            ?: throw IllegalStateException("post_json is null. PostViewModel requires a post_json to initialize.")
        Gson().fromJson(json, AraPost::class.java)
    }

    private val _post = MutableStateFlow(initialPost)
    override val post : StateFlow<AraPost> = _post.asStateFlow()

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

    override suspend fun fetchPost() {
        try {
            val fetchedPost = araBoardRepository.fetchPost(origin = AraBoardTarget.PostOrigin.Board, postID = _post.value.id)
            _post.value = fetchedPost
        } catch (e: Exception) {
            Log.e("PostViewModel", "fetchPost error", e)
        }
    }

    override suspend fun upVote() {
        val previousMyVote: Boolean? = _post.value.myVote
        val previousUpVotes: Int = _post.value.upVotes

        try {
            if (previousMyVote == true) {
                _post.value.myVote = null
                _post.value.upVotes -= 1
                araBoardRepository.cancelVote(postID = _post.value.id)
            } else {
                if (previousMyVote == false) {
                    _post.value.downVotes -= 1
                }
                _post.value.myVote = true
                _post.value.upVotes += 1
                araBoardRepository.upVotePost(postID = _post.value.id)
            }
        } catch (e: Exception) {
            Log.e("PostViewModel", "upvote error", e)
            _post.value.upVotes = previousUpVotes
            _post.value.myVote = previousMyVote
        }
    }

    override suspend fun downVote() {
        val previousMyVote: Boolean? = _post.value.myVote
        val previousDownvotes: Int = _post.value.downVotes

        try {
            if (previousMyVote == false) {
                _post.value.myVote = null
                _post.value.downVotes -= 1
                araBoardRepository.cancelVote(postID = _post.value.id)
            } else {
                if (previousMyVote == true) {
                    _post.value.upVotes -= 1
                }
                _post.value.myVote = false
                _post.value.downVotes += 1
                araBoardRepository.downVotePost(postID = _post.value.id)
            }
        } catch (e: Exception) {
            Log.e("PostViewModel", "downvote error", e)
            _post.value.downVotes = previousDownvotes
            _post.value.myVote = previousMyVote
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
}
