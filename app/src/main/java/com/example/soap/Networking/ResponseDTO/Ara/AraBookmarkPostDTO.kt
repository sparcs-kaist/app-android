package com.example.soap.Networking.ResponseDTO.Ara

import com.example.soap.Domain.Models.Ara.AraPost
import com.google.gson.annotations.SerializedName

data class AraBookmarkPostDTO(
    @SerializedName("parent_article")
    val posts: AraPostDTO
){
    fun toModel(): AraPost{
        return posts.toModel()
    }
}