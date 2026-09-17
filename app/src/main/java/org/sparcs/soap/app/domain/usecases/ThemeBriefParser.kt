package org.sparcs.soap.app.domain.usecases

import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import org.sparcs.soap.app.domain.helpers.TimetableThemeBrief

internal object ThemeBriefParser {
    private val json = Json { ignoreUnknownKeys = true }
    private val token = Regex("\"(?:[^\"\\\\]|\\\\.)*\"")
    private val colorsStart = Regex("\"colors\"\\s*:\\s*\\[")

    fun partial(response: String): TimetableThemeBrief {
        fun field(key: String): String? {
            val start = Regex("\"$key\"\\s*:\\s*").find(response)?.range?.last?.plus(1) ?: return null
            val match = token.find(response, start)?.takeIf { it.range.first == start } ?: return null
            return runCatching { json.decodeFromString<String>(match.value) }.getOrNull()
        }
        val start = colorsStart.find(response)?.range?.last?.plus(1)
        val colors = start?.let { offset ->
            token.findAll(response.substring(offset).substringBefore(']')).mapNotNull {
                runCatching { json.decodeFromString<String>(it.value) }.getOrNull()
            }.take(6).toList()
        }.orEmpty()
        return brief(field("name"), field("appearance"), colors)
    }

    fun complete(response: String): TimetableThemeBrief {
        val text = jsonObject(response) ?: throw ThemeGenerationException(ThemeGenerationError.INCOMPLETE)
        val result = try { json.decodeFromString<Response>(text) }
        catch (_: Exception) { throw ThemeGenerationException(ThemeGenerationError.INCOMPLETE) }
        val brief = brief(result.name, result.appearance, result.colors)
        if (brief.appearance == null || brief.anchorHexColors.none { TimetableThemeBrief.normalizeHex(it) != null }) {
            throw ThemeGenerationException(ThemeGenerationError.INCOMPLETE)
        }
        return brief
    }

    private fun jsonObject(response: String): String? {
        val start = response.indexOf('{').takeIf { it >= 0 } ?: return null
        var depth = 0
        var quoted = false
        var escaped = false
        for (index in start until response.length) {
            val character = response[index]
            if (quoted) {
                when {
                    escaped -> escaped = false
                    character == '\\' -> escaped = true
                    character == '"' -> quoted = false
                }
            } else {
                when (character) {
                    '"' -> quoted = true
                    '{' -> depth++
                    '}' -> if (--depth == 0) return response.substring(start, index + 1)
                }
            }
        }
        return null
    }

    private fun brief(name: String?, appearance: String?, colors: List<String>) = TimetableThemeBrief(
        name = name,
        appearance = when (appearance) {
            "light" -> TimetableThemeBrief.Appearance.LIGHT
            "dark" -> TimetableThemeBrief.Appearance.DARK
            else -> null
        },
        anchorHexColors = colors.take(6),
    )

    @Serializable
    private data class Response(val name: String? = null, val appearance: String, val colors: List<String>)
}
