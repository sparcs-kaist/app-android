package org.sparcs.App.Domain.Models.Ara

import java.net.URL
import java.util.Date

data class AraAttachment(
    val id: Int,
    val file: URL?,
    val size: Int,
    val mimeType: String,
    val createdAt: Date
)