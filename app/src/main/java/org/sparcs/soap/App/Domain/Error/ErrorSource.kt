package org.sparcs.soap.App.Domain.Error

enum class ErrorSource(val value: String) {
    Network("network"),
    Repository("repository"),
    UseCase("useCase"),
    Domain("domain"),
    Unknown("unknown")
}