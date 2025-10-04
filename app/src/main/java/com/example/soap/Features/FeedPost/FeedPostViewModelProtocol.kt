package com.example.soap.Features.FeedPost

import android.graphics.Bitmap
import com.example.soap.Domain.Models.Feed.FeedComment
import kotlinx.coroutines.flow.StateFlow

interface FeedPostViewModelProtocol {
    val state: StateFlow<FeedPostViewModel.ViewState>
    var comments: List<FeedComment>
    var text: String
    var image: Bitmap?
    var isAnonymous: Boolean

    suspend fun fetchComments(postID: String)
    suspend fun writeComment(postID: String): FeedComment
    suspend fun writeReply(commentID: String): FeedComment
}
