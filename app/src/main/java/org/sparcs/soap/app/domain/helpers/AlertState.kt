package org.sparcs.soap.app.domain.helpers

import org.sparcs.soap.R
import java.util.UUID

data class AlertState(
    val id: String = UUID.randomUUID().toString(),
    val titleResId: Int = R.string.error,
    val message: String? = null,
    val messageResId: Int? = null
)