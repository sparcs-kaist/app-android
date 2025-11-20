package com.sparcs.soap.Domain.Models.Ara

data class AraPostPage(
    val pages: Int,
    val items: Int,
    val currentPage: Int,
    val results: List<AraPost>
)