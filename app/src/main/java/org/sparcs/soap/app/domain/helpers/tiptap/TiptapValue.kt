package org.sparcs.soap.app.domain.helpers.tiptap

import kotlin.math.truncate

sealed interface TiptapValue {
    data class Str(val value: String) : TiptapValue
    data class Num(val value: Double) : TiptapValue
    data class Bool(val value: Boolean) : TiptapValue
    data class Arr(val values: List<TiptapValue>) : TiptapValue
    data class Obj(val values: Map<String, TiptapValue>) : TiptapValue
    data object Null : TiptapValue

    val stringValue: String?
        get() = when (this) {
            is Str -> value
            is Num -> if (value.isWholeLong()) value.toLong().toString() else value.toString()
            is Bool -> value.toString()
            else -> null
        }

    val doubleValue: Double?
        get() = when (this) {
            is Num -> value
            is Str -> value.trim().toDoubleOrNull()
            else -> null
        }?.takeIf { it.isFinite() }

    val intValue: Int?
        get() = doubleValue?.let(::truncate)
            ?.takeIf { it >= Int.MIN_VALUE && it <= Int.MAX_VALUE }
            ?.toInt()

    val boolValue: Boolean?
        get() = when (this) {
            is Bool -> value
            is Str -> value.toBooleanStrictOrNull()
            else -> null
        }
}

private fun Double.isWholeLong(): Boolean =
    isFinite() && this == truncate(this) && this >= Long.MIN_VALUE && this <= Long.MAX_VALUE
