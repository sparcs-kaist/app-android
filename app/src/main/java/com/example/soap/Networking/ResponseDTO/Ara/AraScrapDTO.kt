package com.example.soap.Networking.ResponseDTO.Ara

import com.google.gson.annotations.SerializedName


data class AraScrapDTO(
    @SerializedName("id")
    val id: Int,

    @SerializedName("created_at")
    val createdAt: String,

    @SerializedName("updated_at")
    val updatedAt: String,

    @SerializedName("deleted_at")
    val deletedAt: String,

    @SerializedName("parent_article")
    val parentArticle: Int,

    @SerializedName("scrapped_by")
    val scrappedBy: Int
)