package org.sparcs.soap.app.domain.models.ara

data class AraPostPage(
    val pages: Int,
    val items: Int,
    val currentPage: Int,
    val results: List<AraPost>
)