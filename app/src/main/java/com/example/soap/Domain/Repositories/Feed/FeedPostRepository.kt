package com.example.soap.Domain.Repositories.Feed

import com.example.soap.Domain.Enums.FeedVoteType
import com.example.soap.Domain.Models.Feed.FeedCreatePost
import com.example.soap.Domain.Models.Feed.FeedPostPage
import com.example.soap.Networking.RequestDTO.Feed.FeedPostRequestDTO
import com.example.soap.Networking.RetrofitAPI.Feed.FeedPostApi

interface FeedPostRepositoryProtocol {
    suspend fun fetchPosts(cursor: String?, page: Int): FeedPostPage
    suspend fun writePost(request: FeedCreatePost)
    suspend fun deletePost(postID: String)
    suspend fun vote(postID: String, type: FeedVoteType)
    suspend fun deleteVote(postID: String)
}

class FeedPostRepository (
    private val api: FeedPostApi
) : FeedPostRepositoryProtocol {

    override suspend fun fetchPosts(cursor: String?, page: Int): FeedPostPage {
        val dto = api.fetchPosts(cursor, page)
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
}