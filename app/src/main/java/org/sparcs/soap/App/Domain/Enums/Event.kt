package org.sparcs.soap.App.Domain.Enums

interface Event {
    val source: String
    val name: String
    val parameters: Map<String, Any>
}