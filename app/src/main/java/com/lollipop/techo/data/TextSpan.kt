package com.lollipop.techo.data

import android.graphics.Color
import com.lollipop.techo.data.json.JsonInfo
import org.json.JSONObject

open class TextSpan : JsonInfo {

    companion object {
        private const val KEY_START = "start"
        private const val KEY_END = "end"
        private const val KEY_COLOR = "color"
        private const val KEY_STYLE = "style"
        private const val KEY_SIZE = "size"
        private const val KEY_LINK = "link"

        fun fromJson(jsonObject: JSONObject): TextSpan {
            return TextSpan().apply {
                parse(jsonObject)
            }
        }
    }

    /**
     * 开始位置
     */
    var start: Int = 0

    /**
     * 结束位置
     */
    var end: Int = 0

    /**
     * 颜色
     */
    var color: Int = Color.BLACK

    /**
     * 样式
     */
    var style: Int = 0

    /**
     * 字体大小
     */
    var fontSize: Int = 16

    /**
     * 链接
     */
    var link: String = ""

    /**
     * Span的长度
     */
    val length: Int
        get() {
            return end - start
        }

    override fun toJson(): JSONObject {
        return JSONObject().apply {
            put(KEY_START, start)
            put(KEY_END, end)
            put(KEY_COLOR, color)
            put(KEY_STYLE, style)
            put(KEY_SIZE, fontSize)
            put(KEY_LINK, link)
        }
    }

    override fun parse(json: JSONObject) {
        start = json.optInt(KEY_START, 0)
        end = json.optInt(KEY_END, 0)
        color = json.optInt(KEY_COLOR, Color.BLACK)
        style = json.optInt(KEY_COLOR, FontStyle.NORMAL)
        fontSize = json.optInt(KEY_SIZE, 16)
        link = json.optString(KEY_LINK, "")
    }

    fun hasStyle(flag: FontStyle): Boolean {
        return FontStyle.has(style, flag)
    }

    fun addStyle(flag: FontStyle) {
        style = FontStyle.add(style, flag)
    }

    fun clearStyle(flag: FontStyle) {
        style = FontStyle.clear(style, flag)
    }

}