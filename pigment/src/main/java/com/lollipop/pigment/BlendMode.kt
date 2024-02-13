package com.lollipop.pigment

import android.graphics.Color
import androidx.core.graphics.ColorUtils


sealed class BlendMode {

    companion object {
        fun blend(color1: Int, color2: Int, ratio: Float = 0.5F): Int {
            return ColorUtils.blendARGB(color1, color2, ratio)
        }

        fun titleOnColor(color: Int): Int {
            val hsl = FloatArray(3)
            ColorUtils.colorToHSL(color, hsl)
            if (hsl[2] > 0.5F) {
                hsl[2] = 0.0F
            } else {
                hsl[2] = 1F
            }
            hsl[1] = 0.05F
            return ColorUtils.HSLToColor(hsl)
        }

        fun contentOnColor(color: Int): Int {
            val hsl = FloatArray(3)
            ColorUtils.colorToHSL(color, hsl)
            if (hsl[2] > 0.5F) {
                hsl[2] = 0.0F
            } else {
                hsl[2] = 1F
            }
            hsl[1] = 0.1F
            return ColorUtils.HSLToColor(hsl)
        }

        fun limit(color: Int, maxS: Float, minS: Float, maxL: Float, minL: Float): Int {
            // [H, S, L]
            val hsl = FloatArray(3)
            ColorUtils.colorToHSL(color, hsl)
            hsl[1] = limit(target = hsl[1], max = maxS, min = minS)
            hsl[2] = limit(target = hsl[2], max = maxL, min = minL)
            return ColorUtils.HSLToColor(hsl)
        }

        private fun limit(target: Float, max: Float, min: Float): Float {
            if (target < min) {
                return min
            }
            if (target > max) {
                return max
            }
            return target
        }

        fun flow(baseColor: Int): Builder {
            return Builder(baseColor)
        }
    }

    fun startFlow(baseColor: Int): Builder {
        return flow(baseColor)
    }

    class Builder(private var themeColor: Int) {

        fun blend(
            color2: Int,
            ratio: Float = 0.5F,
            remember: Boolean = true,
            callback: (Int) -> Unit = {}
        ): Builder {
            val color = blend(themeColor, color2, ratio)
            if (remember) {
                themeColor = color
            }
            callback(color)
            return this
        }

        fun title(callback: (Int) -> Unit): Builder {
            callback(titleOnColor(themeColor))
            return this
        }

        fun content(callback: (Int) -> Unit): Builder {
            callback(contentOnColor(themeColor))
            return this
        }

        fun limit(
            maxS: Float,
            minS: Float,
            maxL: Float,
            minL: Float,
            remember: Boolean = true,
            callback: (Int) -> Unit = {}
        ): Builder {
            val color = limit(themeColor, maxS, minS, maxL, minL)
            if (remember) {
                themeColor = color
            }
            callback(color)
            return this
        }

    }

    abstract val extreme: Int

    abstract val extremeReversal: Int

    abstract fun original(src: Int): Int

    abstract fun variant(src: Int): Int

    abstract fun title(src: Int): Int

    abstract fun body(src: Int): Int

    abstract fun background(src: Int): Int

    protected fun Int.limitColor(): Int {
        val color = this
        return limit(color, 0.5F, 0.2F, 0.6F, 0.2F)
    }

    data object Light : BlendMode() {

        override val extreme: Int = Color.WHITE
        override val extremeReversal: Int = Color.BLACK

        override fun original(src: Int): Int {
            return blend(src.limitColor(), Color.WHITE, 0.3F)
        }

        override fun variant(src: Int): Int {
            return blend(src, Color.BLACK, 0.1F)
        }

        override fun title(src: Int): Int {
            return titleOnColor(src)
        }

        override fun body(src: Int): Int {
            return contentOnColor(src)
        }

        override fun background(src: Int): Int {
            return blend(src, Color.WHITE, 0.9F)
        }

    }

    data object Dark : BlendMode() {

        override val extreme: Int = Color.BLACK
        override val extremeReversal: Int = Color.WHITE

        override fun original(src: Int): Int {
            return blend(src.limitColor(), Color.BLACK, 0.5F)
        }

        override fun variant(src: Int): Int {
            return blend(src, Color.BLACK, 0.7F)
        }

        override fun title(src: Int): Int {
            return titleOnColor(original(src))
        }

        override fun body(src: Int): Int {
            return contentOnColor(original(src))
        }

        override fun background(src: Int): Int {
            return blend(src, Color.BLACK, 0.9F)
        }
    }

}
