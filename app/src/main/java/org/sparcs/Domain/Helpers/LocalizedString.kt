package org.sparcs.Domain.Helpers

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
        val result = translations.values.filter { it.contains(query) }.isNotEmpty()
        return result
    }

    override fun toString(): String {
        return localized()
    }
}
