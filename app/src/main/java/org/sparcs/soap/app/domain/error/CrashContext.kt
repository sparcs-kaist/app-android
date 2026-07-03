package org.sparcs.soap.app.domain.error

data class CrashContext(
    val feature: String,
    val action: String = "Unknown Action",
    val metadata: Map<String, String> = emptyMap()
)