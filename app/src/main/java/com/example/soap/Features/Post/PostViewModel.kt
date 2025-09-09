package com.example.soap.Features.Post

import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
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
import javax.inject.Inject

@HiltViewModel
class PostViewModel @Inject constructor(
    private val savedStateHandle: SavedStateHandle,
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
            ?: throw IllegalStateException("Failed to parse post_json into AraPost")
    }

    override var post by mutableStateOf(initialPost)


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
            val fetchedPost = araBoardRepository.fetchPost(origin = AraBoardTarget.PostOrigin.Board, postID = post.id)
            post = fetchedPost
        } catch (e: Exception) {
            Log.e("PostViewModel", "fetchPost error", e)
        }
    }

    override suspend fun upVote() {
        val previousMyVote: Boolean? = post.myVote
        val previousUpVotes: Int = post.upVotes

        try {
            if (previousMyVote == true) {
                post.myVote = null
                post.upVotes -= 1
                araBoardRepository.cancelVote(postID = post.id)
            } else {
                if (previousMyVote == false) {
                    post.downVotes -= 1
                }
                post.myVote = true
                post.upVotes += 1
                araBoardRepository.upVotePost(postID = post.id)
            }
        } catch (e: Exception) {
            Log.e("PostViewModel", "upvote error", e)
            post.upVotes = previousUpVotes
            post.myVote = previousMyVote
        }
    }

    override suspend fun downVote() {
        val previousMyVote: Boolean? = post.myVote
        val previousDownvotes: Int = post.downVotes

        try {
            if (previousMyVote == false) {
                post.myVote = null
                post.downVotes -= 1
                araBoardRepository.cancelVote(postID = post.id)
            } else {
                if (previousMyVote == true) {
                    post.upVotes -= 1
                }
                post.myVote = false
                post.downVotes += 1
                araBoardRepository.downVotePost(postID = post.id)
            }
        } catch (e: Exception) {
            Log.e("PostViewModel", "downvote error", e)
            post.downVotes = previousDownvotes
            post.myVote = previousMyVote
        }
    }

    override suspend fun writeComment(content: String): AraPostComment {
        val comment = araCommentRepository.writeComment(postID = post.id, content = content)
        comment.isMine = true
        post.comments.add(comment)
        post.commentCount += 1
        return comment
    }

    override suspend fun writeThreadedComment(commentID: Int, content: String): AraPostComment {
        val comment = araCommentRepository.writeThreadedComment(commentID = commentID, content = content)
        comment.isMine = true

        val comments = post.comments.toMutableList()
        insertThreadedComment(comments, comment)
        post.comments = comments
        post.commentCount += 1

        return comment
    }

    override suspend fun editComment(commentID: Int, content: String): AraPostComment {
        val comment = araCommentRepository.editComment(commentID = commentID, content = content)
        comment.isMine = true

        for (i in post.comments.indices) {
            if (post.comments[i].id == commentID) {
                post.comments[i].content = content
                return post.comments[i]
            }
            for (j in post.comments[i].comments.indices) {
                if (post.comments[i].comments[j].id == commentID) {
                    post.comments[i].comments[j].content = content
                    return post.comments[i].comments[j]
                }
            }
        }

        return comment
    }

    override suspend fun report(type: AraContentReportType) {
        araBoardRepository.reportPost(postID = post.id, type = type)
    }

    override suspend fun summarisedContent(): String {
//        return foundationModelsUseCase.summarise(post.content ?: "", maxWords = 50, tone = "concise")
        return ""
    }

    override suspend fun deletePost() {
        araBoardRepository.deletePost(postID = post.id)
    }
}



class MockPostViewModel() : PostViewModelProtocol {

    override val isFoundationModelsAvailable = true

    override var post by mutableStateOf(AraPost.mock())

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
