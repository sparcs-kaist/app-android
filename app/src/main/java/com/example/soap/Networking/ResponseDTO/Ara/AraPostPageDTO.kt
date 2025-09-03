package com.example.soap.Networking.ResponseDTO.Ara

import com.example.soap.Domain.Models.Ara.AraPostPage
import com.google.gson.annotations.SerializedName

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