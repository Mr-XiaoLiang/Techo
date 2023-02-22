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
    private val currentPieces = ArrayList<StitchPiece>()

    init {
        setImageDrawable(colorStitchDrawable)
        if (isInEditMode) {
            resetColor(listOf(Color.RED, Color.BLUE, Color.GREEN))
        }
    }

    fun resetColor(
        colorList: List<Int>,
        pieces: List<StitchPiece>? = null,
        updatePiece: Boolean = false
    ) {
        val piecesList: List<StitchPiece>
        if (pieces != null) {
            piecesList = pieces
            currentPieces.clear()
            currentPieces.addAll(pieces)
        } else {
            if (updatePiece || currentPieces.size != colorList.size) {
                val list = StitchHelper.suture(count = colorList.size)
                piecesList = list
                currentPieces.clear()
                currentPieces.addAll(list)
            } else {
                piecesList = currentPieces
            }
        }
        colorStitchDrawable.resetColor(colorList, piecesList)
    }

}