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

object SplitLoader {

    fun getInfo(style: SplitStyle): SplitInfo {
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
                    solidLengthId = R.dimen.dotted_line_solid,
                    intervalLengthId = R.dimen.dotted_line_interval,
                    strokeWidthId = R.dimen.dotted_line_width
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
        private val solidLengthId: Int = 0,
        private val intervalLengthId: Int = 0,
        private val strokeWidthId: Int = 0,
    ) : CustomRenderer<DottedLineDrawable>(DottedLineDrawable::class.java, colorId) {

        override fun load(context: Context): Drawable? {
            val instance = getInstance(context) ?: return null
            if (solidLengthId != 0) {
                instance.solidLength = context.resources.getDimensionPixelSize(solidLengthId)
            }
            if (intervalLengthId != 0) {
                instance.intervalLength = context.resources.getDimensionPixelSize(intervalLengthId)
            }
            if (strokeWidthId != 0) {
                instance.strokeWidth =
                    context.resources.getDimensionPixelSize(strokeWidthId).toFloat()
            }
            return instance
        }

    }

}