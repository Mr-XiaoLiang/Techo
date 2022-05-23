package com.lollipop.techo.split

import android.content.Context
import android.graphics.drawable.Drawable
import androidx.core.content.ContextCompat
import androidx.core.content.res.ResourcesCompat
import com.lollipop.techo.R
import com.lollipop.techo.data.SplitStyle
import com.lollipop.techo.data.SplitStyle.Default
import com.lollipop.techo.data.SplitStyle.DottedLine
import com.lollipop.techo.split.impl.DottedLineDrawable
import java.util.*

object SplitLoader {

    fun getInfo(style: SplitStyle, value: String): SplitInfo {
        return when (style) {
            Default -> SplitInfo(
                drawable = ResourceRenderer(R.color.defaultSplit),
                widthType = SplitView.WidthType.MATCH,
                heightType = SplitView.HeightType.ABSOLUTELY,
                width = 0,
                height = 4,
                ratio = 0F
            )
            DottedLine -> SplitInfo(
                drawable = DottedLineRenderer(
                    colorId = R.color.defaultSplit,
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
    }

    fun loadDrawableById(context: Context, resourceId: Int): Drawable? {
        return try {
            ResourcesCompat.getDrawable(context.resources, resourceId, context.theme)
        } catch (e: Throwable) {
            e.printStackTrace()
            null
        }
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

    class ResourceRenderer(private val resourceId: Int) : SplitRenderer {
        override fun load(context: Context): Drawable? {
            try {
                return loadDrawableById(context, resourceId)
            } catch (e: Throwable) {
                e.printStackTrace()
            }
            return null

        }
    }

    open class CustomRenderer<T : SplitDrawable>(
        private val clazz: Class<T>,
        private val colorId: Int = 0
    ) : SplitRenderer {

        override fun load(context: Context): Drawable? {
            return getInstance(context)
        }

        protected fun getInstance(context: Context): T? {
            try {
                val instance = clazz.getConstructor(Context::class.java).newInstance(context)
                if (colorId != 0) {
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
        colorId: Int = 0,
        private val dottedArray: IntArray,
        private val strokeWidthId: Int,
        private val flag: String
    ) : CustomRenderer<DottedLineDrawable>(DottedLineDrawable::class.java, colorId) {

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
                    val list = value.split(",")
                    val result = list.map { it.trim().toInt() }
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