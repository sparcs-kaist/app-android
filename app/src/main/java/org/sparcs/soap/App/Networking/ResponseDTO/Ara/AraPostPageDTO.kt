package org.sparcs.soap.App.Networking.ResponseDTO.Ara

import com.google.gson.annotations.SerializedName
import org.sparcs.soap.App.Domain.Models.Ara.AraPostPage

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