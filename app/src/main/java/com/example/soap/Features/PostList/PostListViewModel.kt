package com.example.soap.Features.PostList

import androidx.lifecycle.ViewModel
import com.example.soap.Models.Post

class PostListViewModel : ViewModel(){

    var postList: List<Post> = Post.mocklist()

    var flairList: List<String> = listOf(
        "SPPANGS",
        "Meal",
        "Money",
        "Gaming",
        "Dating",
        "Lost & Found"
    )

}