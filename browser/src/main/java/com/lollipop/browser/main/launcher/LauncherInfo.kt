package com.lollipop.browser.main.launcher

import android.graphics.Color
import androidx.annotation.ColorInt
import com.lollipop.browser.utils.FileInfoManager.Companion.optFile
import org.json.JSONArray
import org.json.JSONObject
import java.io.File
import kotlin.math.max
import kotlin.math.min

class LauncherInfo(
    val id: Int,
    val label: String,
    val icon: File?,
    @ColorInt
    val iconTint: Int,
    val backgroundFile: File?,
    val backgroundColor: List<Int>,
    val url: String
) {

    companion object {
        private const val KEY_LABEL = "label"
        private const val KEY_ICON = "icon"
        private const val KEY_BACKGROUND = "background"
        private const val KEY_BACKGROUND_COLOR = "color"
        private const val KEY_ICON_TINT = "tint"
        private const val KEY_URL = "url"

        internal fun parse(string: String, idProvider: () -> Int): LauncherInfo? {
            try {
                if (string.isEmpty()) {
                    return null
                }
                return parse(JSONObject(string), idProvider)
            } catch (e: Throwable) {
                e.printStackTrace()
            }
            return null
        }

        internal fun parse(obj: JSONObject, idProvider: () -> Int): LauncherInfo? {
            if (obj.length() < 1 || !obj.has(KEY_URL)) {
                return null
            }
            val colorArray = obj.optJSONArray(KEY_BACKGROUND_COLOR)
            val colors = ArrayList<Int>()
            if (colorArray != null && colorArray.length() > 0) {
                for (i in 0 until colorArray.length()) {
                    val color = stringToColor(colorArray.optString(i, ""))
                    if (color != null) {
                        colors.add(color)
                    }
                }
            }
            return LauncherInfo(
                id = idProvider(),
                label = obj.optString(KEY_LABEL),
                icon = obj.optString(KEY_ICON).optFile(),
                iconTint = stringToColor(obj.optString(KEY_ICON_TINT, "")) ?: 0,
                backgroundFile = obj.optString(KEY_BACKGROUND).optFile(),
                backgroundColor = colors,
                url = obj.optString(KEY_URL),
            )
        }


        private fun colorToString(color: Int): String {
            val builder = StringBuilder("#")
            val alpha = getColorHex(Color.alpha(color))
            if (alpha.length < 2) {
                builder.append("0")
            }
            builder.append(alpha)

            val red = getColorHex(Color.red(color))
            if (red.length < 2) {
                builder.append("0")
            }
            builder.append(red)

            val green = getColorHex(Color.green(color))
            if (green.length < 2) {
                builder.append("0")
            }
            builder.append(green)

            val blue = getColorHex(Color.blue(color))
            if (blue.length < 2) {
                builder.append("0")
            }
            builder.append(blue)

            return builder.toString()
        }

        private fun getColorHex(value: Int): String {
            val color = min(255, max(0, value))
            return color.toString(16)
        }

        private fun stringToColor(value: String): Int? {
            if (value.isEmpty()) {
                return null
            }
            try {
                return Color.parseColor(value)
            } catch (e: Throwable) {
                e.printStackTrace()
            }
            return null
        }

    }

    fun toJson(): JSONObject {
        val info = this
        val colorArray = JSONArray()
        info.backgroundColor.forEach {
            colorArray.put(colorToString(it))
        }
        return JSONObject()
            .put(KEY_LABEL, info.label)
            .put(KEY_ICON, info.icon?.path ?: "")
            .put(KEY_BACKGROUND, info.backgroundFile?.path ?: "")
            .put(KEY_URL, info.url)
            .put(KEY_ICON_TINT, colorToString(info.iconTint))
            .put(KEY_BACKGROUND_COLOR, colorArray)
    }

    override fun toString(): String {
        return toJson().toString()
    }

}