package com.lollipop.techo.data

import android.graphics.Color
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
            val jsonObject = JSONObject()
            jsonObject.put(KEY_TITLE, info.title)
            jsonObject.put(KEY_FLAG_ID, info.flag.id)
            jsonObject.put(KEY_FLAG_NAME, info.flag.name)
            jsonObject.put(KEY_FLAG_COLOR, info.flag.color)
            val itemArray = JSONArray()
            info.items.forEach {
                itemArray.put(it.toJson())
            }
            jsonObject.put(KEY_ITEMS, itemArray)
            return jsonObject
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
        fun pauseFromOrdinal(ordinal: Int): TechoItemType {
            val valueArray = values()
            if (ordinal in valueArray.indices) {
                return valueArray[ordinal]
            }
            return valueArray[0]
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

abstract class BaseTechoItem() {

    companion object {
        private const val KEY_ITEM_TYPE = "itemType"

        fun getType(json: JSONObject): TechoItemType {
            return TechoItemType.pauseFromOrdinal(json.optInt(KEY_ITEM_TYPE))
        }

    }

    abstract val itemType: TechoItemType

    open fun toJson(): JSONObject {
        return JSONObject().apply {
            put(KEY_ITEM_TYPE, itemType.ordinal)
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
    override val itemType: TechoItemType = Empty
}

open class TextSpan(
    var text: String = "",
    var color: Int = Color.BLACK,
    var style: Int
)

open class NumberItem(
    var number: Int = 0
) : TextItem() {
    override val itemType: TechoItemType = Number
}

open class CheckBoxItem(
    var isChecked: Boolean = false
) : TextItem() {
    override val itemType: TechoItemType = CheckBox
}

class SplitItem : EmptyItem() {
    override val itemType: TechoItemType = Split
}

class PhotoItem(
    val values: MutableList<Photo> = mutableListOf(),
    val style: PhotoGridLayout.Style = PhotoGridLayout.Style.Playbill
): BaseTechoItem() {
    override val itemType: TechoItemType = Photo
}
