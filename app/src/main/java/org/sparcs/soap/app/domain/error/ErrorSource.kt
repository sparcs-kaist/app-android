package org.sparcs.soap.app.domain.error

enum class ErrorSource(val value: String) {
    Network("network"),
    Repository("repository"),
    UseCase("useCase"),
    Domain("domain"),
    Unknown("unknown")
}