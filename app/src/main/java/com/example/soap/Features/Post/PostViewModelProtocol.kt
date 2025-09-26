package com.example.soap.Features.Post

import com.example.soap.Domain.Enums.AraContentReportType
import com.example.soap.Domain.Models.Ara.AraPost
import com.example.soap.Domain.Models.Ara.AraPostComment
import kotlinx.coroutines.flow.StateFlow

interface PostViewModelProtocol{
    val post: StateFlow<AraPost>
    val isFoundationModelsAvailable: Boolean

    suspend fun fetchPost()
    suspend fun upVote()
    suspend fun downVote()
    suspend fun writeComment(content: String): AraPostComment
    suspend fun writeThreadedComment(commentID: Int, content: String): AraPostComment
    suspend fun editComment(commentID: Int, content: String): AraPostComment
    suspend fun report(type: AraContentReportType)
    suspend fun summarisedContent(): String
    suspend fun deletePost()

}