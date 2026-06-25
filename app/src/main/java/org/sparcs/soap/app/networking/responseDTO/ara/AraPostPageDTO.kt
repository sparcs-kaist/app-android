package org.sparcs.soap.app.networking.responseDTO.ara

import com.google.gson.annotations.SerializedName
import org.sparcs.soap.app.domain.models.ara.AraPostPage

data class AraPostPageDTO(
    @SerializedName("num_pages")
    val pages: Int,

    @SerializedName("num_items")
    val items: Int,

    @SerializedName("current")
    val currentPage: Int,

    @SerializedName("results")
    val results: List<AraPostDTO>

){
    fun toModel(): AraPostPage = AraPostPage(
        pages = pages,
        items = items,
        currentPage = currentPage,
        results = results.map { it.toModel() }
    )
}