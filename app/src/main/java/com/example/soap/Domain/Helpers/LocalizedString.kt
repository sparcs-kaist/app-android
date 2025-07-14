package com.example.soap.Domain.Helpers

import java.util.Locale

data class LocalizedString(
    private val translations: Map<String, String>
) {
    fun localized(languageCode: String? = null): String {
        val localeLanguageCode = languageCode
            ?: Locale.getDefault().language
            ?: "ko"
        return translations[localeLanguageCode] ?: translations["ko"] ?: "Untitled"
    }

    override fun toString(): String {
        return localized()
    }
}
