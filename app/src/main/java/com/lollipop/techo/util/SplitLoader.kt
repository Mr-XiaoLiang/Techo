package com.lollipop.techo.util

import com.lollipop.techo.data.SplitStyle
import com.lollipop.techo.data.SplitStyle.*

object SplitLoader {

    fun getInfo(style: SplitStyle): SplitInfo {
        return when (style) {
            Default -> TODO()
        }
    }

    class SplitInfo(
        val resourceId: Int,
        val widthType: LayoutType,
        val heightType: LayoutType,
        val width: Int,
        val height: Int,
        val ratio: Float
    )

    enum class LayoutType {
        MATCH, ABSOLUTELY, RATIO
    }

}