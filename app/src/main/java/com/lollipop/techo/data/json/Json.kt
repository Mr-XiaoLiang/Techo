package com.lollipop.techo.data.json

import org.json.JSONArray
import org.json.JSONObject

inline fun <reified T : Any> Collection<T>.mapToJson(): JSONArray {
    return mapToJson {
        if (it is JsonInfo) {
            it.toJson()
        } else {
            it
        }
    }
}

inline fun <reified T : Any> Collection<T>.mapToJson(callback: (T) -> Any): JSONArray {
    val array = JSONArray()
    this.forEach {
        array.put(callback(it))
    }
    return array
}

inline fun JSONArray.forEach(callback: (JSONArray, Int) -> Unit) {
    for (index in 0 until length()) {
        callback(this, index)
    }
}

inline fun JSONArray.forEachObj(callback: (JSONObject, Int) -> Unit) {
    forEach { jsonArray, i ->
        jsonArray.optJSONObject(i)?.let { obj ->
            callback(obj, i)
        }
    }
}

inline fun JSONArray.forEachString(callback: (String, Int) -> Unit) {
    forEach { jsonArray, i ->
        jsonArray.optString(i)?.let { obj ->
            callback(obj, i)
        }
    }
}

inline fun JSONArray.forEachInt(callback: (Int, Int) -> Unit) {
    forEach { jsonArray, i ->
        callback(jsonArray.optInt(i, 0), i)
    }
}

inline fun JSONArray.forEachBoolean(callback: (Boolean, Int) -> Unit) {
    forEach { jsonArray, i ->
        callback(jsonArray.optBoolean(i, false), i)
    }
}

fun String.toJsonObject(): JSONObject {
    try {
        if (this.length > 1) {
            return JSONObject(this)
        }
    } catch (e: Throwable) {
        e.printStackTrace()
    }
    return JSONObject()
}

fun String.toJsonArray(): JSONArray {
    try {
        if (this.length > 1) {
            return JSONArray(this)
        }
    } catch (e: Throwable) {
        e.printStackTrace()
    }
    return JSONArray()
}