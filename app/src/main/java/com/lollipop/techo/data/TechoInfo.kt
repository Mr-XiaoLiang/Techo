package com.lollipop.techo.data

import android.graphics.Color
import android.net.Uri
import com.lollipop.gallery.Photo
import com.lollipop.gallery.PhotoGridLayout
import com.lollipop.techo.data.TechoItemType.*
import org.json.JSONArray
import org.json.JSONObject

/**
 * @author lollipop
 * @date 4/29/21 22:04
 */
data class TechoInfo(
    var id: Int = 0,
    var title: String = "",
    var flag: TechoFlag = TechoFlag(),
    val items: MutableList<BaseTechoItem> = mutableListOf()
) {

    companion object {

        private const val KEY_TITLE = "title"
        private const val KEY_FLAG_ID = "flagId"
        private const val KEY_FLAG_COLOR = "flagColor"
        private const val KEY_FLAG_NAME = "flagName"
        private const val KEY_ITEMS = "items"

        fun fromJson(json: String): TechoInfo {
            val info = TechoInfo()
            try {
                val jsonObject = JSONObject(json)
                info.title = jsonObject.optString(KEY_TITLE)
                info.flag.id = jsonObject.optInt(KEY_FLAG_ID, 0)
                info.flag.name = jsonObject.optString(KEY_FLAG_NAME)
                info.flag.color = jsonObject.optInt(KEY_FLAG_COLOR, Color.BLACK)
                val optJSONArray = jsonObject.optJSONArray(KEY_ITEMS)
                if (optJSONArray != null) {
                    for (index in 0 until optJSONArray.length()) {
                        val itemObj = optJSONArray.optJSONObject(index)
                        val itemInfo = getItemByType(BaseTechoItem.getType(itemObj))
                        itemInfo.parse(itemObj)
                        info.items.add(itemInfo)
                    }
                }
            } catch (e: Throwable) {
                e.printStackTrace()
            }
            return info
        }

        private fun getItemByType(type: TechoItemType): BaseTechoItem {
            return when (type) {
                Empty -> {
                    EmptyItem()
                }
                Text -> {
                    TextItem()
                }
                Number -> {
                    NumberItem()
                }
                CheckBox -> {
                    CheckBoxItem()
                }
                Photo -> {
                    PhotoItem()
                }
                Split -> {
                    SplitItem()
                }
            }
        }

        private fun toJson(info: TechoInfo): JSONObject {
            return JSONObject().apply {
                put(KEY_TITLE, info.title)
                put(KEY_FLAG_ID, info.flag.id)
                put(KEY_FLAG_NAME, info.flag.name)
                put(KEY_FLAG_COLOR, info.flag.color)
                val itemArray = JSONArray()
                info.items.forEach {
                    itemArray.put(it.toJson())
                }
                put(KEY_ITEMS, itemArray)
            }
        }
    }

    fun toJson(): JSONObject {
        return toJson(this)
    }
}

data class TechoFlag(
    var name: String = "",
    var color: Int = Color.RED,
    var id: Int = 0
) {
    fun copyTo(newFlag: TechoFlag = TechoFlag()): TechoFlag {
        newFlag.id = id
        newFlag.color = color
        newFlag.name = name
        return newFlag
    }
}

enum class TechoItemType {
    Empty,
    Text,
    Number,
    CheckBox,
    Photo,
    Split;

    companion object {
        fun opt(name: String): TechoItemType {
            try {
                return valueOf(name)
            } catch (e: Throwable) {
                e.printStackTrace()
            }
            return Empty
        }
    }
}

enum class FontStyle {
    /**
     * 加粗
     */
    Bold,

    /**
     * 斜体
     */
    Italic,

    /**
     * 删除线
     */
    Strikethrough,

    /**
     * 下划线
     */
    Underline,

    /**
     * 字体大小
     */
    FontSize,

    /**
     * 颜色
     */
    Color,

    /**
     * 链接
     */
    Link,

    /**
     * 上标
     */
    Superscript,

    /**
     * 下标
     */
    Subscript,

    /**
     * 模糊
     */
    Blur;

    companion object {

        const val NORMAL = 0

        fun has(value: Int, vararg flag: FontStyle): Boolean {
            for (index in flag.indices) {
                val style = flag[index]
                val key = 1 shl style.flag
                if (value and key != 0) {
                    return true
                }
            }
            return false
        }

        fun hasAll(value: Int, vararg flag: FontStyle): Boolean {
            for (index in flag.indices) {
                val style = flag[index]
                val key = 1 shl style.flag
                if (value and key == 0) {
                    return false
                }
            }
            return true
        }

        fun build(base: Int = 0, vararg flag: FontStyle): Int {
            var value = base
            flag.forEach {
                value = value or it.flag
            }
            return value
        }

    }

    private val flag: Int
        get() {
            return 1 shl ordinal
        }

}

abstract class BaseTechoItem {

    companion object {
        private const val KEY_ITEM_TYPE = "itemType"

        fun getType(json: JSONObject): TechoItemType {
            return TechoItemType.opt(json.optString(KEY_ITEM_TYPE))
        }

    }

    abstract val itemType: TechoItemType

    open fun toJson(): JSONObject {
        return JSONObject().apply {
            put(KEY_ITEM_TYPE, itemType.name)
        }
    }

    open fun parse(json: JSONObject) {

    }

}

class EmptyItem : BaseTechoItem() {
    override val itemType: TechoItemType = Empty
}

open class BaseTextItem(
    var value: String,
    val spans: MutableList<TextSpan>
) : BaseTechoItem() {

    companion object {
        private const val KEY_VALUE = "VALUE"
        private const val KEY_SPANS = "SPANS"
    }

    override val itemType: TechoItemType = Empty

    override fun toJson(): JSONObject {
        return super.toJson().apply {
            put(KEY_VALUE, value)
            val spanArray = JSONArray()
            spans.forEach {
                spanArray.put(it.toJson())
            }
            put(KEY_SPANS, spanArray)
        }
    }

    override fun parse(json: JSONObject) {
        super.parse(json)
        value = json.optString(KEY_VALUE)
        spans.clear()
        json.optJSONArray(KEY_SPANS)?.let { valueArray ->
            for (index in 0 until valueArray.length()) {
                val obj = valueArray.optJSONObject(index) ?: continue
                spans.add(TextSpan.fromJson(obj))
            }
        }
    }

}

class TextItem(
    value: String = "",
    spans: MutableList<TextSpan> = mutableListOf()
): BaseTextItem(value, spans) {
    override val itemType: TechoItemType = Text
}

open class TextSpan(
    /**
     * 开始位置
     */
    var start: Int = 0,
    /**
     * 结束位置
     */
    var end: Int = 0,
    /**
     * 颜色
     */
    var color: Int = Color.BLACK,
    /**
     * 样式
     */
    var style: Int = 0,
    /**
     * 字体大小
     */
    var fontSize: Int = 16,
    /**
     * 链接
     */
    var link: String = "",
) {

    companion object {
        private const val KEY_START = "start"
        private const val KEY_END = "end"
        private const val KEY_COLOR = "color"
        private const val KEY_STYLE = "style"
        private const val KEY_SIZE = "size"
        private const val KEY_LINK = "link"

        fun fromJson(jsonObject: JSONObject): TextSpan {
            return TextSpan(
                start = jsonObject.optInt(KEY_START, 0),
                end = jsonObject.optInt(KEY_END, 0),
                color = jsonObject.optInt(KEY_COLOR, Color.BLACK),
                style = jsonObject.optInt(KEY_COLOR, FontStyle.NORMAL),
                fontSize = jsonObject.optInt(KEY_SIZE, 16),
                link = jsonObject.optString(KEY_LINK, "")
            )
        }
    }

    val length: Int
        get() {
            return end - start
        }

    fun toJson(): JSONObject {
        return JSONObject().apply {
            put(KEY_START, start)
            put(KEY_END, end)
            put(KEY_COLOR, color)
            put(KEY_STYLE, style)
            put(KEY_SIZE, fontSize)
            put(KEY_LINK, link)
        }
    }

    fun hasStyle(flag: FontStyle): Boolean {
        return FontStyle.has(style, flag)
    }

}

class NumberItem(
    var number: Int = 0,
    value: String = "",
    spans: MutableList<TextSpan> = mutableListOf()
) : BaseTextItem(value, spans) {

    companion object {
        private const val KEY_NUMBER = "number"
    }

    override val itemType: TechoItemType = Number

    override fun toJson(): JSONObject {
        return super.toJson().apply {
            put(KEY_NUMBER, number)
        }
    }

    override fun parse(json: JSONObject) {
        super.parse(json)
        number = json.optInt(KEY_NUMBER, 0)
    }

}

class CheckBoxItem(
    var isChecked: Boolean = false,
    value: String = "",
    spans: MutableList<TextSpan> = mutableListOf()
) : BaseTextItem(value, spans) {

    companion object {
        private const val KEY_CHECKED = "checked"
    }

    override val itemType: TechoItemType = CheckBox

    override fun toJson(): JSONObject {
        return super.toJson().apply {
            put(KEY_CHECKED, isChecked)
        }
    }

    override fun parse(json: JSONObject) {
        super.parse(json)
        isChecked = json.optBoolean(KEY_CHECKED, false)
    }
}

class SplitItem(
    var style: SplitStyle = SplitStyle.Default
) : BaseTechoItem() {

    companion object {
        private const val KEY_STYLE = "style"
    }

    override val itemType: TechoItemType = Split

    override fun toJson(): JSONObject {
        return super.toJson().apply {
            put(KEY_STYLE, style.name)
        }
    }

    override fun parse(json: JSONObject) {
        super.parse(json)
        style = optStyle(json.optString(KEY_STYLE))
    }

    private fun optStyle(name: String): SplitStyle {
        try {
            return SplitStyle.valueOf(name)
        } catch (e: Throwable) {
            e.printStackTrace()
        }
        return SplitStyle.Default
    }
}

enum class SplitStyle {
    Default
}

class PhotoItem(
    val values: MutableList<Photo> = mutableListOf(),
    var style: PhotoGridLayout.Style = PhotoGridLayout.Style.Playbill
) : BaseTechoItem() {

    companion object {
        private const val KEY_VALUES = "values"
        private const val KEY_STYLE = "style"

        private const val KEY_PHOTO_URL = "photoUrl"
        private const val KEY_PHOTO_TITLE = "photoTitle"
    }

    override val itemType: TechoItemType = Photo

    override fun toJson(): JSONObject {
        return super.toJson().apply {
            put(KEY_STYLE, style.name)
            put(KEY_VALUES, photoToJson())
        }
    }

    override fun parse(json: JSONObject) {
        super.parse(json)
        style = optStyle(json.optString(KEY_STYLE))
        json.optJSONArray(KEY_VALUES)?.let {
            parseFromJson(it)
        }
    }

    private fun optStyle(name: String): PhotoGridLayout.Style {
        try {
            return PhotoGridLayout.Style.valueOf(name)
        } catch (e: Throwable) {
            e.printStackTrace()
        }
        return PhotoGridLayout.Style.Playbill
    }

    private fun photoToJson(): JSONArray {
        return JSONArray().apply {
            values.forEach {
                put(toJson(it))
            }
        }
    }

    private fun toJson(photo: Photo): JSONObject {
        return JSONObject().apply {
            put(KEY_PHOTO_URL, photo.uri.toString())
            put(KEY_PHOTO_TITLE, photo.title)
        }
    }

    private fun parseFromJson(jsonArray: JSONArray) {
        values.clear()
        for (index in 0 until jsonArray.length()) {
            val obj = jsonArray.optJSONObject(index) ?: continue
            values.add(
                Photo(
                    uri = Uri.parse(obj.optString(KEY_PHOTO_URL)),
                    title = obj.optString(KEY_PHOTO_TITLE)
                )
            )
        }
    }

}
