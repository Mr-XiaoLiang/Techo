package com.lollipop.techo.util

import com.lollipop.techo.R
import com.lollipop.techo.data.SplitStyle
import com.lollipop.techo.data.SplitStyle.Default
import com.lollipop.techo.view.SplitView

object SplitLoader {

    fun getInfo(style: SplitStyle): SplitInfo {
        return when (style) {
            Default -> SplitInfo(
                resourceId = R.color.defaultSplit,
                widthType = SplitView.WidthType.MATCH,
                heightType = SplitView.HeightType.ABSOLUTELY,
                width = 0,
                height = 4,
                ratio = 0F
            )
        }
    }

    class SplitInfo(
        val resourceId: Int,
        val widthType: SplitView.WidthType,
        val heightType: SplitView.HeightType,
        val width: Int,
        val height: Int,
        val ratio: Float
    )

}