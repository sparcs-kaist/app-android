package org.sparcs.soap.App.Domain.Error

data class CrashContext(
    val feature: String,
    val action: String = "Unknown Action",
    val metadata: Map<String, String> = emptyMap()
)