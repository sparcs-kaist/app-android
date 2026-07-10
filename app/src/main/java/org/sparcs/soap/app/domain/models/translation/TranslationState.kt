package org.sparcs.soap.app.domain.models.translation

sealed interface TranslationState {
    data object Idle : TranslationState
    data object Loading : TranslationState
    data class Translated(val text: String, val sourceLanguage: String) : TranslationState
    data object Unsupported : TranslationState
    data object Failed : TranslationState
}
