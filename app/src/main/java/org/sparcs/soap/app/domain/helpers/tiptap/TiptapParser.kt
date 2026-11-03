package org.sparcs.soap.app.domain.helpers.tiptap

import com.google.gson.JsonArray
import com.google.gson.JsonElement
import com.google.gson.JsonObject
import com.google.gson.JsonParser

sealed class TiptapParseException : Exception() {
    class NotJson : TiptapParseException()
    class Malformed : TiptapParseException()
}

class TiptapParser(
    private val repairLinkifiedUrls: Boolean = true,
    private val decodeHtmlEntities: Boolean = true,
) {
    fun parse(string: String): TiptapDocument {
        if (!isTiptapJson(string)) throw TiptapParseException.NotJson()

        var root = decodeRoot(string)
        if (root == null && repairLinkifiedUrls) {
            val repaired = unwrapAnchors(string)
            root = decodeRoot(repaired) ?: decodeRoot(removeTrailingCommas(repaired))
        }
        val nodes = root ?: throw TiptapParseException.Malformed()

        return TiptapDocument(if (decodeHtmlEntities) nodes.map { it.decodingHtmlEntities() } else nodes)
    }

    fun parseOrNull(string: String): TiptapDocument? = runCatching { parse(string) }.getOrNull()

    companion object {
        private val anchorPattern = Regex("<a\\b[^>]*>(.*?)</a>", setOf(RegexOption.IGNORE_CASE, RegexOption.DOT_MATCHES_ALL))
        private val trailingCommaPattern = Regex(",(\\s*[}\\]])")

        fun isTiptapJson(string: String): Boolean = string.firstOrNull { !it.isWhitespace() } == '{'

        fun decodeHtmlEntities(string: String): String = HtmlEntities.decode(string)

        internal fun unwrapAnchors(string: String): String {
            if (!string.contains("<a", ignoreCase = true)) return string
            return anchorPattern.replace(string) { it.groupValues[1] }
        }

        internal fun removeTrailingCommas(string: String): String =
            trailingCommaPattern.replace(string) { it.groupValues[1] }

        private fun decodeRoot(string: String): List<TiptapNode>? {
            val root = runCatching { JsonParser.parseString(string) }.getOrNull()
                ?.takeIf { it.isJsonObject }?.asJsonObject ?: return null

            val wrapped = root["content"]?.takeIf { it.isJsonObject && root["type"] == null }?.asJsonObject
            val node = (wrapped ?: root).toNode() ?: return null
            return if (node.type == TiptapNode.DOC || node.type.isEmpty()) node.content else listOf(node)
        }

        private fun JsonObject.toNode(): TiptapNode? = runCatching {
            TiptapNode(
                type = this["type"].primitiveString().orEmpty(),
                attrs = this["attrs"].toAttributes(),
                content = this["content"].arrayOrEmpty().mapNotNull { child ->
                    child.takeIf { it.isJsonObject }?.asJsonObject?.toNode()
                },
                marks = this["marks"].arrayOrEmpty().mapNotNull { it.toMark() },
                text = this["text"].primitiveString(),
            )
        }.getOrNull()

        private fun JsonElement.toMark(): TiptapMark? {
            if (!isJsonObject) return null
            val type = asJsonObject["type"].primitiveString() ?: return null
            return TiptapMark(type, asJsonObject["attrs"].toAttributes())
        }

        private fun JsonElement?.toAttributes(): Map<String, TiptapValue> {
            if (this == null || !isJsonObject) return emptyMap()
            return asJsonObject.entrySet().associate { (key, value) -> key to value.toValue() }
        }

        private fun JsonElement.toValue(): TiptapValue = when {
            isJsonNull -> TiptapValue.Null
            isJsonArray -> TiptapValue.Arr(asJsonArray.map { it.toValue() })
            isJsonObject -> TiptapValue.Obj(asJsonObject.entrySet().associate { (key, value) -> key to value.toValue() })
            asJsonPrimitive.isBoolean -> TiptapValue.Bool(asBoolean)
            asJsonPrimitive.isNumber -> TiptapValue.Num(asDouble)
            else -> TiptapValue.Str(asString)
        }

        private fun JsonElement?.primitiveString(): String? =
            this?.takeIf { it.isJsonPrimitive }?.asString

        private fun JsonElement?.arrayOrEmpty(): JsonArray =
            this?.takeIf { it.isJsonArray }?.asJsonArray ?: JsonArray()
    }
}

private fun TiptapNode.decodingHtmlEntities(): TiptapNode = copy(
    text = text?.let(HtmlEntities::decode),
    attrs = attrs.decodingHtmlEntities(),
    marks = marks.map { it.copy(attrs = it.attrs.decodingHtmlEntities()) },
    content = content.map { it.decodingHtmlEntities() },
)

private fun Map<String, TiptapValue>.decodingHtmlEntities(): Map<String, TiptapValue> =
    mapValues { (_, value) -> if (value is TiptapValue.Str) TiptapValue.Str(HtmlEntities.decode(value.value)) else value }
