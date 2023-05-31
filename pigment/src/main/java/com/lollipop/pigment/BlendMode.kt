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
    }

    abstract fun original(src: Int): Int

    abstract fun variant(src: Int): Int

    abstract fun title(src: Int): Int

    abstract fun body(src: Int): Int

    abstract fun background(src: Int): Int

    object Light : BlendMode() {
        override fun original(src: Int): Int {
            return src
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

    object Dark : BlendMode() {
        override fun original(src: Int): Int {
            return blend(src, Color.BLACK, 0.5F)
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
