package com.lollipop.techo.data.json

import org.json.JSONObject

interface JsonInfo {
    fun toJson(): JSONObject
    fun parse(json: JSONObject)
}