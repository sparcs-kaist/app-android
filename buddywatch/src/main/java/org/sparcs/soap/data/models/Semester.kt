package org.sparcs.soap.data.models

import kotlinx.serialization.Serializable

@Serializable
data class Semester(
    val name: String,
    val beginDateMillis: Long,
    val endDateMillis: Long
)
