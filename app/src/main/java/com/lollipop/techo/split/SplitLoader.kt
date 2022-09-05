package com.lollipop.techo.split

import android.content.Context
import android.graphics.Color
import android.graphics.drawable.Drawable
import androidx.annotation.ColorInt
import androidx.annotation.ColorRes
import androidx.core.content.ContextCompat
import com.lollipop.base.util.dp2px
import com.lollipop.techo.R
import com.lollipop.techo.data.TechoItem
import com.lollipop.techo.split.impl.DottedLineDrawable
import org.json.JSONArray
import java.io.ByteArrayOutputStream
import java.util.*

object SplitLoader {

    private const val CONFIG_NAME = "split/SplitConfig.json"
    private const val KEY_FLAG = "flag"
    private const val KEY_COLOR = "color"
    private const val FLAG_SEPARATOR = ","

    fun read(context: Context): List<TechoItem.Split> {
        val result = ArrayList<TechoItem.Split>()
        try {
            val inputStream = context.assets.open(CONFIG_NAME)
            val byteArray = ByteArrayOutputStream()
            val buffer = ByteArray(2048)
            var readLength: Int
            do {
                readLength = inputStream.read(buffer)
                byteArray.write(buffer, 0, readLength)
            } while (readLength > 0)
            val jsonValue = byteArray.toString()
            byteArray.flush()
            inputStream.close()
            val jsonArray = JSONArray(jsonValue)
            for (index in 0 until jsonArray.length()) {
                try {
                    val obj = jsonArray.optJSONObject(index)
                    val flag = obj.optString(KEY_FLAG)
                    val color = obj.optString(KEY_COLOR)
                    if (flag.isEmpty()) {
                        continue
                    }
                    val colorValue = if (color.isEmpty()) {
                        Color.BLACK
                    } else {
                        Color.parseColor(color)
                    }
                    result.add(TechoItem.Split.create(flag, colorValue))
                } catch (e: Throwable) {
                    e.printStackTrace()
                }
            }
        } catch (e: Throwable) {
            e.printStackTrace()
        }
        return result
    }

    fun getInfo(color: Int, value: String): SplitInfo {
        return SplitInfo(
            drawable = DottedLineRenderer(
                color = color,
                colorId = R.color.gray_7,
                dottedArray = intArrayOf(
                    R.dimen.dotted_line_solid,
                    R.dimen.dotted_line_interval,
                    R.dimen.dotted_line_dot,
                    R.dimen.dotted_line_interval
                ),
                strokeWidthId = R.dimen.dotted_line_width,
                value
            ),
            widthType = SplitView.WidthType.MATCH,
            heightType = SplitView.HeightType.ABSOLUTELY,
            width = 0,
            height = 3,
            ratio = 0F
        )
    }

    class SplitInfo(
        val drawable: SplitRenderer,
        val widthType: SplitView.WidthType,
        val heightType: SplitView.HeightType,
        val width: Int,
        val height: Int,
        val ratio: Float
    )

    interface SplitRenderer {
        fun load(context: Context): Drawable?
    }

    open class CustomRenderer<T : SplitDrawable>(
        private val clazz: Class<T>,
        @ColorInt
        private val color: Int,
        @ColorRes
        private val colorId: Int,
    ) : SplitRenderer {

        override fun load(context: Context): Drawable? {
            return getInstance(context)
        }

        protected fun getInstance(context: Context): T? {
            try {
                val instance = clazz.getConstructor(Context::class.java).newInstance(context)
                if (color != 0) {
                    instance.color = color
                } else if (colorId != 0) {
                    instance.color = ContextCompat.getColor(context, colorId)
                }
                return instance
            } catch (e: Throwable) {
                e.printStackTrace()
            }
            return null
        }

    }

    class DottedLineRenderer(
        @ColorInt
        color: Int = 0,
        @ColorRes
        colorId: Int = 0,
        private val dottedArray: IntArray,
        private val strokeWidthId: Int,
        private val flag: String
    ) : CustomRenderer<DottedLineDrawable>(DottedLineDrawable::class.java, color, colorId) {

        companion object {
            private val cacheFlag = LinkedList<String>()
            private val cacheMap = HashMap<String, List<Int>>()
        }

        override fun load(context: Context): Drawable? {
            try {
                if (dottedArray.isEmpty()) {
                    return null
                }
                val instance = getInstance(context) ?: return null
                val custom = getCustomDotted(flag, context) ?: return null
                instance.updateDottedInfo(custom)
                if (strokeWidthId != 0) {
                    instance.strokeWidth =
                        context.resources.getDimensionPixelSize(strokeWidthId).toFloat()
                }
                return instance
            } catch (e: Throwable) {
                e.printStackTrace()
            }
            return null
        }

        private fun getCustomDotted(value: String, context: Context): List<Int>? {
            val oldInfo = getCache(value)
            if (oldInfo != null) {
                return oldInfo
            }
            if (value.isNotEmpty()) {
                try {
                    val list = value.split(FLAG_SEPARATOR)
                    val result = list.map { it.trim().toInt().dp2px }
                    saveCache(value, result)
                    return result
                } catch (e: Throwable) {
                    e.printStackTrace()
                }
            }
            try {
                val result = dottedArray.map {
                    context.resources.getDimensionPixelSize(it)
                }
                saveCache(value, result)
                return result
            } catch (e: Throwable) {
                e.printStackTrace()
            }
            return null
        }

        private fun getCache(value: String): List<Int>? {
            val result = cacheMap[value]
            if (result != null) {
                cacheFlag.remove(value)
                cacheFlag.addLast(value)
            }
            return result
        }

        private fun saveCache(value: String, dotted: List<Int>) {
            cacheMap[value] = dotted
            cacheFlag.addLast(value)
            while (cacheFlag.isNotEmpty() && cacheFlag.size > 200) {
                val first = cacheFlag.removeFirst()
                cacheMap.remove(first)
            }
        }

    }

}