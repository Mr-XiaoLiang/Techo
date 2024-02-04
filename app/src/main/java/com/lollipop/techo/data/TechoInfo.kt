package com.lollipop.techo.data

import com.lollipop.techo.data.json.JsonInfo
import com.lollipop.techo.data.json.forEachObj
import com.lollipop.techo.data.json.forEachString
import com.lollipop.techo.data.json.mapToJson
import org.json.JSONObject

class TechoInfo : JsonInfo {

    companion object {
        private const val KEY_TITLE = "title"
        private const val KEY_FLAG = "flag"
        private const val KEY_ITEMS = "items"
        private const val KEY_KEY_WORDS = "keyWords"
        private const val KEY_CREATE_TIME = "createTime"
        private const val KEY_UPDATE_TIME = "updateTime"
        private const val KEY_THEME = "theme"
    }

    var id: Int = 0
    var title: String = ""
    var flag: TechoFlag = TechoFlag()
    val items: MutableList<TechoItem> = mutableListOf()
    val keyWords: MutableList<String> = mutableListOf()
    var createTime: Long = 0
    var updateTime: Long = 0
    var theme: TechoTheme = TechoTheme.DEFAULT


    override fun toJson(): JSONObject {
        return JSONObject().apply {
            put(KEY_TITLE, title)
            put(KEY_FLAG, flag.toJson())
            put(KEY_ITEMS, items.mapToJson())
            put(KEY_KEY_WORDS, keyWords.mapToJson())
            put(KEY_CREATE_TIME, createTime)
            put(KEY_UPDATE_TIME, updateTime)
            put(KEY_THEME, theme.key)
        }
    }

    override fun parse(json: JSONObject) {
        title = json.optString(KEY_TITLE)
        flag = TechoFlag().apply { parse(json.getJSONObject(KEY_FLAG)) }
        items.clear()
        json.getJSONArray(KEY_ITEMS).forEachObj { jsonObject, _ ->
            items.add(TechoItem.getItem(jsonObject))
        }
        keyWords.clear()
        json.getJSONArray(KEY_KEY_WORDS).forEachString { s, _ ->
            keyWords.add(s)
        }
        createTime = json.optLong(KEY_CREATE_TIME)
        updateTime = json.optLong(KEY_UPDATE_TIME)
        theme = TechoTheme.find(json.optString(KEY_THEME))
    }

}