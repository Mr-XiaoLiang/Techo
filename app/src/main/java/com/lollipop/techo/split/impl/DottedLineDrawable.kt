package com.lollipop.techo.split.impl

import android.content.Context
import android.graphics.Canvas
import com.lollipop.techo.split.SplitDrawable

class DottedLineDrawable(context: Context) : SplitDrawable(context) {

    companion object {
        private val EMPTY_ARRAY = FloatArray(0)
    }

    var solidLength = 1
        set(value) {
            field = value
            updatePath()
        }

    var intervalLength = 1
        set(value) {
            field = value
            updatePath()
        }

    var strokeWidth: Float
        get() {
            return paint.strokeWidth
        }
        set(value) {
            paint.strokeWidth = value
        }

    private val tempList = ArrayList<Float>()

    private var linesArray: FloatArray = EMPTY_ARRAY

    private fun updatePath() {
        if (bounds.isEmpty) {
            linesArray = EMPTY_ARRAY
            return
        }
        val centerY = bounds.exactCenterY()
        var left = bounds.left.toFloat()
        val right = bounds.right.toFloat()
        val solid = solidLength
        val step = solid + intervalLength
        tempList.clear()
        while (left < right) {
            tempList.add(left)
            tempList.add(centerY)
            tempList.add(left + solid)
            tempList.add(centerY)
            left += step
        }
        linesArray = tempList.toFloatArray()
        tempList.clear()
        invalidateSelf()
    }

    override fun draw(canvas: Canvas) {
        if (linesArray.isEmpty()) {
            return
        }
        canvas.drawLines(linesArray, paint)
    }

}