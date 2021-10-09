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
)

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

object FontStyle {

    /**
     * 什么都没有
     */
    const val NORMAL = 0

    /**
     * 加粗
     */
    const val BOLD = 1

    /**
     * 斜体
     */
    const val ITALIC = 1 shl 1

    /**
     * 删除线
     */
    const val STRIKETHROUGH = 1 shl 2

    fun has(value: Int, flag: Int): Boolean {
        return value and flag != 0
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

open class EmptyItem : BaseTechoItem() {
    override val itemType: TechoItemType = Empty
}

open class TextItem(
    val values: MutableList<TextSpan> = mutableListOf()
) : BaseTechoItem() {

    companion object {
        private const val KEY_VALUES = "values"
    }

    override val itemType: TechoItemType = Empty

    override fun toJson(): JSONObject {
        return super.toJson().apply {
            val valueArray = JSONArray()
            values.forEach {
                valueArray.put(it.toJson())
            }
            put(KEY_VALUES, valueArray)
        }
    }

    override fun parse(json: JSONObject) {
        super.parse(json)
        values.clear()
        json.optJSONArray(KEY_VALUES)?.let { valueArray ->
            for (index in 0 until valueArray.length()) {
                val obj = valueArray.optJSONObject(index) ?: continue
                values.add(TextSpan.fromJson(obj))
            }
        }
    }

}

open class TextSpan(
    var text: String = "",
    var color: Int = Color.BLACK,
    var style: Int
) {

    companion object {
        private const val KEY_TEXT = "text"
        private const val KEY_COLOR = "color"
        private const val KEY_STYLE = "style"

        fun fromJson(jsonObject: JSONObject): TextSpan {
            return TextSpan(
                text = jsonObject.optString(KEY_TEXT),
                color = jsonObject.optInt(KEY_COLOR, Color.BLACK),
                style = jsonObject.optInt(KEY_COLOR, FontStyle.NORMAL)
            )
        }
    }

    fun toJson(): JSONObject {
        return JSONObject().apply {
            put(KEY_TEXT, text)
            put(KEY_COLOR, color)
            put(KEY_STYLE, style)
        }
    }

}

open class NumberItem(
    var number: Int = 0
) : TextItem() {

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

open class CheckBoxItem(
    var isChecked: Boolean = false
) : TextItem() {

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

class SplitItem : EmptyItem() {
    override val itemType: TechoItemType = Split
}

class PhotoItem(
    val values: MutableList<Photo> = mutableListOf(),
    var style: PhotoGridLayout.Style = PhotoGridLayout.Style.Playbill
): BaseTechoItem() {

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
