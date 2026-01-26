package org.sparcs.soap.App.Networking.ResponseDTO.Ara

import com.google.gson.annotations.SerializedName
import org.sparcs.soap.App.Domain.Models.Ara.AraPost

data class AraBookmarkPostDTO(
    @SerializedName("parent_article")
    val posts: AraPostDTO
){
    fun toModel(): AraPost {
        return posts.toModel()
    }
}