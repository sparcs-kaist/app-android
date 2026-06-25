package org.sparcs.soap.app.domain.enums

interface Event {
    val source: String
    val name: String
    val parameters: Map<String, Any>
}