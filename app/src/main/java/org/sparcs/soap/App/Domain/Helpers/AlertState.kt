package org.sparcs.soap.App.Domain.Helpers

import java.util.UUID

data class AlertState(
    val id: String = UUID.randomUUID().toString(),
    val titleResId: Int,
    val message: String? = null,
    val messageResId: Int? = null
)