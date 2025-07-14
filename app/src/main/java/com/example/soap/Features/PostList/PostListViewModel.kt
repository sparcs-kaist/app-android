package com.example.soap.Features.PostList

import androidx.lifecycle.ViewModel
import com.example.soap.Domain.Models.Post.Post
import com.example.soap.Shared.Mocks.mockList

class PostListViewModel : ViewModel(){

    var postList: List<Post> = Post.mockList()

    var flairList: List<String> = listOf(
        "SPPANGS",
        "Meal",
        "Money",
        "Gaming",
        "Dating",
        "Lost & Found"
    )

}