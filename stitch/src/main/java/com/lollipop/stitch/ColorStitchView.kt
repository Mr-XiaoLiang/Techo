package com.lollipop.stitch

import android.content.Context
import android.graphics.Color
import android.util.AttributeSet
import androidx.appcompat.widget.AppCompatImageView

/**
 * 一个随机的颜色拼接效果的View
 * 它会用二分法，递归二分，直到拆分出每个块的位置
 * 它将会保证每一块的面积都是一样的
 */
class ColorStitchView @JvmOverloads constructor(
    context: Context, attributeSet: AttributeSet? = null, style: Int = 0
) : AppCompatImageView(context, attributeSet, style) {

    private val colorStitchDrawable = ColorStitchDrawable()

    init {
        setImageDrawable(colorStitchDrawable)
        if (isInEditMode) {
            resetColor(listOf(Color.RED, Color.BLUE, Color.GREEN))
        }
    }

    fun resetColor(colorList: List<Int>, pieces: List<StitchPiece>? = null) {
        val piecesList = pieces ?: StitchHelper.suture(count = colorList.size)
        colorStitchDrawable.resetColor(colorList, piecesList)
    }

}