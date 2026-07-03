package org.sparcs.soap.app.networking.responseDTO.ara

import com.google.gson.annotations.SerializedName
import org.sparcs.soap.app.domain.models.ara.AraPost

data class AraBookmarkPostDTO(
    @SerializedName("parent_article")
    val posts: AraPostDTO
){
    fun toModel(): AraPost {
        return posts.toModel()
    }
}