package com.sparcs.soap.Domain.Models.Ara

import java.net.URL
import java.util.Date

data class AraPostAttachment(
    val id: Int,
    val createdAt: Date,
    val file: URL?,
    val filename: String,
    val size: Int,
    val mimeType: String
)