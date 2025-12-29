package org.sparcs.App.Domain.Models.Ara

data class AraPostPage(
    val pages: Int,
    val items: Int,
    val currentPage: Int,
    val results: List<AraPost>
)