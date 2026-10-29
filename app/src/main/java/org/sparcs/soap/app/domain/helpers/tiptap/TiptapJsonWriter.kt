package org.sparcs.soap.app.domain.helpers.tiptap

import kotlin.math.abs
import kotlin.math.truncate

fun TiptapDocument.toJson(sanitizerSafe: Boolean = true): String =
    TiptapJsonWriter(sanitizerSafe).apply { write(TiptapNode(TiptapNode.DOC, content = content)) }.output

private class TiptapJsonWriter(private val sanitizerSafe: Boolean) {
    private val builder = StringBuilder()

    val output: String
        get() = builder.toString()

    fun write(node: TiptapNode) {
        builder.append("{\"type\":")
        writeString(node.type)
        if (node.attrs.isNotEmpty()) {
            builder.append(",\"attrs\":")
            writeObject(node.attrs)
        }
        if (node.content.isNotEmpty()) {
            builder.append(",\"content\":[")
            node.content.forEachIndexed { index, child ->
                if (index > 0) builder.append(',')
                write(child)
            }
            builder.append(']')
        }
        if (node.marks.isNotEmpty()) {
            builder.append(",\"marks\":[")
            node.marks.forEachIndexed { index, mark ->
                if (index > 0) builder.append(',')
                builder.append("{\"type\":")
                writeString(mark.type)
                if (mark.attrs.isNotEmpty()) {
                    builder.append(",\"attrs\":")
                    writeObject(mark.attrs)
                }
                builder.append('}')
            }
            builder.append(']')
        }
        node.text?.let {
            builder.append(",\"text\":")
            writeString(it)
        }
        builder.append('}')
    }

    private fun writeObject(values: Map<String, TiptapValue>) {
        builder.append('{')
        values.keys.sorted().forEachIndexed { index, key ->
            if (index > 0) builder.append(',')
            writeString(key)
            builder.append(':')
            writeValue(values.getValue(key))
        }
        builder.append('}')
    }

    private fun writeValue(value: TiptapValue) {
        when (value) {
            is TiptapValue.Str -> writeString(value.value)
            is TiptapValue.Num -> builder.append(formatNumber(value.value))
            is TiptapValue.Bool -> builder.append(value.value)
            TiptapValue.Null -> builder.append("null")
            is TiptapValue.Arr -> {
                builder.append('[')
                value.values.forEachIndexed { index, element ->
                    if (index > 0) builder.append(',')
                    writeValue(element)
                }
                builder.append(']')
            }
            is TiptapValue.Obj -> writeObject(value.values)
        }
    }

    private fun formatNumber(number: Double): String = when {
        !number.isFinite() -> "null"
        number == truncate(number) && abs(number) < 1e15 -> number.toLong().toString()
        else -> number.toString()
    }

    private fun writeString(string: String) {
        builder.append('"')
        string.forEach { char ->
            when (char) {
                '"' -> builder.append("\\\"")
                '\\' -> builder.append("\\\\")
                '\n' -> builder.append("\\n")
                '\r' -> builder.append("\\r")
                '\t' -> builder.append("\\t")
                else -> if (char.code < 0x20 || (sanitizerSafe && char in SANITIZER_SENSITIVE)) {
                    builder.append("\\u%04X".format(char.code))
                } else {
                    builder.append(char)
                }
            }
        }
        builder.append('"')
    }

    private companion object {
        val SANITIZER_SENSITIVE = setOf('&', '<', '>', '.', '/')
    }
}
