package org.sparcs.soap.data.models

import kotlinx.serialization.Serializable

@Serializable
data class TimetableActivity(
    val id: Int,
    val title: String,
    val day: String,
    val begin: Int,
    val end: Int,
    val location: String,
    val color: String? = null
)
