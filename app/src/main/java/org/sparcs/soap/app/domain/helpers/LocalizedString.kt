package org.sparcs.soap.app.domain.helpers

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

    fun contains(query: String): Boolean {
        val result = translations.values.any { it.contains(query) }
        return result
    }

    override fun toString(): String {
        return localized()
    }
}
