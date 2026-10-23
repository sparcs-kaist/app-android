package org.sparcs.soap.app.shared.tips

import org.sparcs.soap.app.domain.usecases.summarization.SummarizationUseCase
import java.util.Locale

object PostToolsTipRule {
    fun applies(plainText: String, locale: Locale = Locale.getDefault()): Boolean {
        val text = plainText.trim()
        if (text.isEmpty()) return false
        return text.length >= SummarizationUseCase.MIN_POST_LENGTH || isInOtherLanguage(text, locale)
    }

    private fun isInOtherLanguage(text: String, locale: Locale): Boolean {
        val letters = text.filter(Char::isLetter)
        if (letters.isEmpty()) return false
        val hangulShare = letters.count { Character.UnicodeScript.of(it.code) == Character.UnicodeScript.HANGUL }.toDouble() / letters.length
        val isKorean = hangulShare >= KOREAN_SHARE
        return isKorean != (locale.language == Locale.KOREAN.language)
    }

    private const val KOREAN_SHARE = 0.3
}
