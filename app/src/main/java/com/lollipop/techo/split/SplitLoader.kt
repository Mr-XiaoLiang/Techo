package com.lollipop.techo.split

import android.content.Context
import android.graphics.drawable.Drawable
import androidx.core.content.res.ResourcesCompat
import com.lollipop.techo.R
import com.lollipop.techo.data.SplitStyle
import com.lollipop.techo.data.SplitStyle.Default

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

    class CustomRenderer(private val clazz: Class<out SplitDrawable>) : SplitRenderer {
        override fun load(context: Context): Drawable? {
            try {
                return clazz.getConstructor(Context::class.java).newInstance(context)
            } catch (e: Throwable) {
                e.printStackTrace()
            }
            return null
        }
    }

}