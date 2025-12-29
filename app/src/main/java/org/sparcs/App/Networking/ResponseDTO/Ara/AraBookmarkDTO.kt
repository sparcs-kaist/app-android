package org.sparcs.App.Networking.ResponseDTO.Ara

import com.google.gson.annotations.SerializedName
import org.sparcs.App.Domain.Models.Ara.AraPostPage

data class AraBookmarkDTO(
    @SerializedName("num_pages")
    val pages: Int,

    @SerializedName("num_items")
    val items: Int,

    @SerializedName("current")
    val currentPage: Int,

    @SerializedName("results")
    val results: List<AraBookmarkPostDTO>
){
    fun toModel(): AraPostPage {
        return AraPostPage(
            pages = pages,
            items = items,
            currentPage = currentPage,
            results = results.map { it.toModel() }
        )
    }
}