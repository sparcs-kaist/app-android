package org.sparcs.soap.app.shared.extensions

import com.google.gson.JsonElement
import com.google.gson.JsonObject
import com.google.gson.JsonParser

private val TEXT_NODE_REGEX = Regex("\"text\"\\s*:\\s*\"([\\s\\S]*?)\"(?=\\s*[,}\\]])")
private val HTML_TAG_REGEX = Regex("<[^>]*>")

fun String.araContentToPlainText(): String {
    val trimmed = trim()
    if (trimmed.startsWith("{") || trimmed.startsWith("[")) {
        runCatching {
            val builder = StringBuilder()
            appendProseMirrorText(JsonParser.parseString(trimmed), builder)
            val cleaned = builder.toString().stripTagsAndClean()
            if (cleaned.isNotBlank()) return cleaned
        }
        val fromRegex = TEXT_NODE_REGEX.findAll(trimmed)
            .map { unescapeJson(it.groupValues[1]) }
            .joinToString(" ")
            .stripTagsAndClean()
        if (fromRegex.isNotBlank()) return fromRegex
    }
    return htmlToPlainText()
}

private fun String.stripTagsAndClean(): String =
    replace(HTML_TAG_REGEX, " ")
        .replace(Regex("[ \\t]+"), " ")
        .replace(Regex(" *\\n *"), "\n")
        .replace(Regex("\\n{3,}"), "\n\n")
        .trim()

private fun appendProseMirrorText(element: JsonElement, out: StringBuilder) {
    when {
        element.isJsonArray -> element.asJsonArray.forEach { appendProseMirrorText(it, out) }
        element.isJsonObject -> {
            val obj = element.asJsonObject
            val type = obj.stringIgnoreCase("type")?.lowercase()
            when (type) {
                "text" -> obj.stringIgnoreCase("text")?.let { out.append(it) }
                "hardbreak", "hard_break" -> out.append("\n")
            }
            obj.entrySetIgnoreCase("content")?.let { appendProseMirrorText(it, out) }
            if (type in BLOCK_TYPES) out.append("\n")
        }
    }
}

private val BLOCK_TYPES = setOf(
    "paragraph", "heading", "blockquote", "listitem", "list_item",
    "bulletlist", "orderedlist", "codeblock"
)

private fun unescapeJson(raw: String): String {
    if (!raw.contains('\\')) return raw
    val sb = StringBuilder(raw.length)
    var i = 0
    while (i < raw.length) {
        val c = raw[i]
        if (c == '\\' && i + 1 < raw.length) {
            when (val next = raw[i + 1]) {
                'n' -> sb.append('\n')
                't' -> sb.append('\t')
                'r' -> sb.append('\r')
                'b' -> sb.append('\b')
                'f' -> sb.append(' ')
                '"' -> sb.append('"')
                '\\' -> sb.append('\\')
                '/' -> sb.append('/')
                'u' -> {
                    if (i + 5 < raw.length) {
                        raw.substring(i + 2, i + 6).toIntOrNull(16)?.let { sb.append(it.toChar()) }
                        i += 4
                    }
                }
                else -> sb.append(next)
            }
            i += 2
        } else {
            sb.append(c)
            i++
        }
    }
    return sb.toString()
}

private fun JsonObject.stringIgnoreCase(key: String): String? {
    val el = entrySetIgnoreCase(key) ?: return null
    return if (el.isJsonPrimitive) el.asString else null
}

private fun JsonObject.entrySetIgnoreCase(key: String): JsonElement? {
    for ((k, v) in entrySet()) if (k.equals(key, ignoreCase = true)) return v
    return null
}
