package org.sparcs.soap.app.domain.usecases.translation

import com.google.mlkit.nl.languageid.LanguageIdentification
import com.google.mlkit.nl.translate.TranslateLanguage
import com.google.mlkit.nl.translate.Translation
import com.google.mlkit.nl.translate.TranslatorOptions
import kotlinx.coroutines.tasks.await
import org.sparcs.soap.app.shared.extensions.htmlToPlainText
import java.util.Locale
import javax.inject.Inject

sealed interface PostTranslationResult {
    data class Success(val text: String, val sourceLanguage: String) : PostTranslationResult
    data object SameLanguage : PostTranslationResult
    data object Unsupported : PostTranslationResult
    data class Failed(val error: Throwable) : PostTranslationResult
}

interface PostTranslationUseCaseProtocol {
    fun availableLanguages(): List<String>
    fun suggestedLanguages(): List<String>
    fun deviceLanguage(): String
    suspend fun translate(
        text: String,
        targetLanguage: String,
        isHtml: Boolean,
    ): PostTranslationResult
}

class PostTranslationUseCase @Inject constructor() : PostTranslationUseCaseProtocol {

    override fun availableLanguages(): List<String> =
        TranslateLanguage.getAllLanguages().sortedBy {
            Locale.forLanguageTag(it).getDisplayLanguage(Locale.getDefault())
        }

    override fun suggestedLanguages(): List<String> {
        val all = TranslateLanguage.getAllLanguages().toSet()
        return listOf(deviceLanguage(), "en", "ko", "ja", "zh")
            .mapNotNull { TranslateLanguage.fromLanguageTag(it) }
            .filter { it in all }
            .distinct()
    }

    override fun deviceLanguage(): String = Locale.getDefault().language

    override suspend fun translate(
        text: String,
        targetLanguage: String,
        isHtml: Boolean,
    ): PostTranslationResult {
        val plain = if (isHtml) text.htmlToPlainText() else text.trim()
        if (plain.length < MIN_LENGTH) return PostTranslationResult.Unsupported

        val target = TranslateLanguage.fromLanguageTag(targetLanguage)
            ?: return PostTranslationResult.Unsupported

        return try {
            val languageIdentifier = LanguageIdentification.getClient()
            val detected = try {
                languageIdentifier.identifyLanguage(plain).await()
            } finally {
                languageIdentifier.close()
            }
            if (detected == UNDETERMINED) return PostTranslationResult.Unsupported

            val source = TranslateLanguage.fromLanguageTag(detected)
                ?: return PostTranslationResult.Unsupported
            if (source == target) return PostTranslationResult.SameLanguage

            val translator = Translation.getClient(
                TranslatorOptions.Builder()
                    .setSourceLanguage(source)
                    .setTargetLanguage(target)
                    .build()
            )
            try {
                translator.downloadModelIfNeeded().await()
                val translated = translator.translate(plain).await()
                PostTranslationResult.Success(text = translated, sourceLanguage = detected)
            } finally {
                translator.close()
            }
        } catch (e: Exception) {
            PostTranslationResult.Failed(e)
        }
    }

    private companion object {
        const val MIN_LENGTH = 2
        const val UNDETERMINED = "und"
    }
}
