package com.lollipop.techo.split.impl

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Rect
import com.lollipop.techo.split.SplitDrawable

class DottedLineDrawable(context: Context) : SplitDrawable(context) {

    companion object {
        private val EMPTY_ARRAY = FloatArray(0)
    }

    init {
        paint.strokeCap = Paint.Cap.ROUND
    }

    private var dotArray = ArrayList<Int>()

    var strokeWidth: Float
        get() {
            return paint.strokeWidth
        }
        set(value) {
            paint.strokeWidth = value
        }

    private val tempList = ArrayList<Float>()

    private var linesArray: FloatArray = EMPTY_ARRAY

    fun updateDottedInfo(array: List<Int>) {
        this.dotArray.clear()
        this.dotArray.addAll(array)
        updatePath()
    }

    private fun updatePath() {
        if (bounds.isEmpty || dotArray.isEmpty()) {
            linesArray = EMPTY_ARRAY
            return
        }
        val centerY = bounds.exactCenterY()
        var left = bounds.left.toFloat()
        val right = bounds.right.toFloat()
        var dotIndex = 0
        tempList.clear()
        while (left < right) {
            tempList.add(left)
            tempList.add(centerY)
            val solid = getNextLength(dotIndex++)
            tempList.add(left + solid)
            tempList.add(centerY)
            left += solid
            val interval = getNextLength(dotIndex++)
            left += interval
            if (dotIndex > dotArray.size) {
                dotIndex %= dotArray.size
            }
        }
        linesArray = tempList.toFloatArray()
        tempList.clear()
        invalidateSelf()
    }

    override fun onBoundsChange(bounds: Rect?) {
        super.onBoundsChange(bounds)
        updatePath()
    }

    private fun getNextLength(index: Int): Int {
        if (dotArray.isEmpty()) {
            return 1
        }
        val i = index % dotArray.size
        return dotArray[i]
    }

    override fun draw(canvas: Canvas) {
        if (linesArray.isEmpty()) {
            return
        }
        canvas.drawLines(linesArray, paint)
    }

}