package com.lollipop.techo.data

import android.graphics.Color
import android.net.Uri
import com.lollipop.gallery.Photo
import com.lollipop.gallery.PhotoGridLayout
import com.lollipop.techo.data.json.JsonInfo
import com.lollipop.techo.data.json.forEachObj
import com.lollipop.techo.data.json.mapToJson
import org.json.JSONObject

sealed class TechoItem(private val itemType: TechoItemType) : JsonInfo {

    companion object {
        private const val KEY_VALUE = "VALUE"
        private const val KEY_SPANS = "SPANS"

        private const val KEY_ITEM_TYPE = "itemType"

        private fun getType(json: JSONObject): TechoItemType {
            return TechoItemType.opt(json.optString(KEY_ITEM_TYPE))
        }

        fun getItem(jsonObject: JSONObject): TechoItem {
            val item = createItem(getType(jsonObject))
            item.parse(jsonObject)
            return item
        }

        fun createItem(type: TechoItemType): TechoItem {
            return when (type) {
                TechoItemType.Empty -> {
                    Empty()
                }
                TechoItemType.Text -> {
                    Text()
                }
                TechoItemType.Number -> {
                    Number()
                }
                TechoItemType.CheckBox -> {
                    CheckBox()
                }
                TechoItemType.Photo -> {
                    Photo()
                }
                TechoItemType.Split -> {
                    Split()
                }
            }
        }
    }

    var value: String = ""

    val spans: MutableList<TextSpan> = ArrayList()

    override fun toJson(): JSONObject {
        return JSONObject().apply {
            put(KEY_ITEM_TYPE, itemType.name)
            put(KEY_VALUE, value)
            put(KEY_SPANS, spans.mapToJson())
        }
    }

    override fun parse(json: JSONObject) {
        value = json.optString(KEY_VALUE)
        spans.clear()
        json.optJSONArray(KEY_SPANS)?.forEachObj { obj, _ ->
            spans.add(TextSpan.fromJson(obj))
        }
    }

    class Text : TechoItem(TechoItemType.Text)

    class Empty : TechoItem(TechoItemType.Empty) {
        override fun toJson(): JSONObject {
            return JSONObject()
        }

        override fun parse(json: JSONObject) {
            // do nothing
        }
    }

    class Number : TechoItem(TechoItemType.Number) {

        companion object {
            private const val KEY_NUMBER = "number"
        }

        var number: Int = 0

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

    class CheckBox : TechoItem(TechoItemType.CheckBox) {

        companion object {
            private const val KEY_CHECKED = "checked"
        }

        var isChecked: Boolean = false

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

    class Split : TechoItem(TechoItemType.Split) {

        companion object {
            private const val KEY_COLOR = "color"

            fun create(value: String, color: Int): Split {
                val split = Split()
                split.value = value
                split.color = color
                return split
            }
        }

        var color: Int = Color.BLACK

        override fun toJson(): JSONObject {
            return super.toJson().apply {
                put(KEY_COLOR, color)
            }
        }

        override fun parse(json: JSONObject) {
            super.parse(json)
            color = json.optInt(KEY_COLOR, Color.BLACK)
        }
    }

    class Photo : TechoItem(TechoItemType.Photo) {

        companion object {
            private const val KEY_VALUES = "values"
            private const val KEY_STYLE = "style"

            private const val KEY_PHOTO_URL = "photoUrl"
            private const val KEY_PHOTO_TITLE = "photoTitle"
        }

        val values: MutableList<com.lollipop.gallery.Photo> = mutableListOf()
        var style: PhotoGridLayout.Style = PhotoGridLayout.Style.Playbill

        override fun toJson(): JSONObject {
            return super.toJson().apply {
                put(KEY_STYLE, style.name)
                put(KEY_VALUES, values.mapToJson { toJson(it) })
            }
        }

        override fun parse(json: JSONObject) {
            super.parse(json)
            style = optStyle(json.optString(KEY_STYLE))
            values.clear()
            json.optJSONArray(KEY_VALUES)?.forEachObj { jsonObject, _ ->
                values.add(
                    Photo(
                        uri = Uri.parse(jsonObject.optString(KEY_PHOTO_URL)),
                        title = jsonObject.optString(KEY_PHOTO_TITLE)
                    )
                )
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

        private fun toJson(photo: com.lollipop.gallery.Photo): JSONObject {
            return JSONObject().apply {
                put(KEY_PHOTO_URL, photo.uri.toString())
                put(KEY_PHOTO_TITLE, photo.title)
            }
        }
    }

}

