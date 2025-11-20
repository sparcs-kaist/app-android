package com.sparcs.soap.Shared.Extensions

import org.json.JSONArray
import org.json.JSONObject

fun JSONObject.toMap(): Map<String, Any?> {
    val map = mutableMapOf<String, Any?>()
    val keys = this.keys()
    while (keys.hasNext()) {
        val key = keys.next() as? String ?: continue
        val value = when (val v = this[key]) {
            is JSONArray -> List(v.length()) { i -> v[i] }
            is JSONObject -> v.toMap()
            JSONObject.NULL -> null
            else -> v
        }
        map[key] = value
    }
    return map
}