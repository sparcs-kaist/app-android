package org.sparcs.soap.app.domain.models.ara

data class AraUpdatePost(
    val title: String,
    val content: String,
    val attachmentIds: List<Int>,
    val isNSFW: Boolean,
    val isPolitical: Boolean,
    val marketListing: AraMarketListing? = null,
)
