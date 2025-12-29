package org.sparcs.App.Domain.Models.Taxi

import java.net.URL
import java.util.Date

data class TaxiNotice(
    val id: String,
    val title: String,
    val notionURL: URL,
    val isPinned: Boolean,
    val isActive: Boolean,
    val createdAt: Date,
    val updatedAt: Date
)