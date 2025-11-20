package com.sparcs.soap.Domain.Repositories.Feed

import com.sparcs.soap.Domain.Enums.FeedReportType
import com.sparcs.soap.Domain.Enums.FeedVoteType
import com.sparcs.soap.Domain.Models.Feed.FeedCreatePost
import com.sparcs.soap.Domain.Models.Feed.FeedPost
import com.sparcs.soap.Domain.Models.Feed.FeedPostPage
import com.sparcs.soap.Networking.RequestDTO.Feed.FeedPostRequestDTO
import com.sparcs.soap.Networking.RetrofitAPI.Feed.FeedPostApi
import javax.inject.Inject

interface FeedPostRepositoryProtocol {
    suspend fun fetchPosts(cursor: String?, page: Int): FeedPostPage
    suspend fun fetchPost(postID: String): FeedPost
    suspend fun writePost(request: FeedCreatePost)
    suspend fun deletePost(postID: String)
    suspend fun vote(postID: String, type: FeedVoteType)
    suspend fun deleteVote(postID: String)
    suspend fun reportPost(postID: String, reason: FeedReportType)
}

class FeedPostRepository @Inject constructor(
    private val api: FeedPostApi,
) : FeedPostRepositoryProtocol {

    override suspend fun fetchPosts(cursor: String?, page: Int): FeedPostPage {
        val dto = api.fetchPosts(cursor, page)
        return dto.toModel()
    }

    override suspend fun fetchPost(postID: String): FeedPost {
        val dto = api.fetchPost(postID)
        return dto.toModel()
    }

    override suspend fun writePost(request: FeedCreatePost) {
        val dto = FeedPostRequestDTO.fromModel(request)
        api.writePost(dto)
    }

    override suspend fun deletePost(postID: String) {
        api.deletePost(postID)
    }

    override suspend fun vote(postID: String, type: FeedVoteType) {
        api.vote(postID, mapOf("vote" to type.name))
    }

    override suspend fun deleteVote(postID: String) {
        api.deleteVote(postID)
    }

    override suspend fun reportPost(postID: String, reason: FeedReportType) {
        api.reportPost(postID, mapOf("reason" to reason.name))
    }
}