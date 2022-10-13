package com.lollipop.techo.view

import android.content.Context
import android.graphics.*
import android.graphics.drawable.Drawable
import android.util.AttributeSet
import androidx.appcompat.widget.AppCompatImageView
import com.lollipop.recorder.VisualizerHelper
import com.lollipop.recorder.visualizer.VisualizerRenderer
import kotlin.math.min

class AudioVisualizerView(
    context: Context, attributeSet: AttributeSet?, style: Int
) : AppCompatImageView(context, attributeSet, style), VisualizerRenderer {

    companion object {
        const val DEFAULT_BAR_COUNT = 64
        const val DEFAULT_INTERVAL_WEIGHT = 0.5F
    }

    constructor(context: Context, attributeSet: AttributeSet?) : this(context, attributeSet, 0)

    constructor(context: Context) : this(context, null)

    private val visualizerDrawable = AudioVisualizerDrawable()

    var barCount: Int
        get() {
            return visualizerDrawable.barCount
        }
        set(value) {
            visualizerDrawable.barCount = value
        }

    var intervalWeight: Float
        get() {
            return visualizerDrawable.intervalWeight
        }
        set(value) {
            visualizerDrawable.intervalWeight = value
        }

    var color: Int
        get() {
            return visualizerDrawable.color
        }
        set(value) {
            visualizerDrawable.color = value
        }

    override fun onRender(data: VisualizerHelper.Frequency) {
        super.onRender(data)

    }

    private class AudioVisualizerDrawable : Drawable() {

        private val paint = Paint().apply {
            isAntiAlias = true
            isDither = true
            strokeCap = Paint.Cap.ROUND
            style = Paint.Style.STROKE
        }

        var barCount: Int = DEFAULT_BAR_COUNT

        var intervalWeight: Float = DEFAULT_INTERVAL_WEIGHT

        var baseLineHeight = 0F
        var baseLineInterval = 0F

        private var barBottom = 0F
        private var barMaxHeight = 0F
        private var barWidth = 0F
        private var intervalWidth = 0F

        private var barArray = FloatArray(0)
        private var baseLineArray = FloatArray(0)

        var color: Int
            set(value) {
                paint.color = value
            }
            get() {
                return paint.color
            }

        private val valueList = ArrayList<Float>()

        override fun draw(canvas: Canvas) {
            paint.strokeWidth = barWidth
            canvas.drawLines(barArray, paint)
            paint.strokeWidth = baseLineHeight
            canvas.drawLines(baseLineArray, paint)
        }

        override fun onBoundsChange(bounds: Rect) {
            super.onBoundsChange(bounds)
            if (bounds.isEmpty) {
                return
            }
            barMaxHeight = (bounds.height() - baseLineHeight - baseLineInterval).toFloat()
            barBottom = barMaxHeight
            val width = bounds.width()
            barWidth = width / (barCount * (1 + intervalWeight) - intervalWeight)
            intervalWidth = barWidth * intervalWeight
        }

        fun onValueChanged(newValue: List<Float>) {
            valueList.clear()
            valueList.addAll(newValue)
            buildLines()
            invalidateSelf()
        }

        private fun buildLines() {
            val barXStep = barWidth + intervalWidth
            val halfBarWidth = barWidth * 0.5F

            val valuePointCount = min(valueList.size, barCount) * 4
            if (barArray.size != valuePointCount) {
                barArray = FloatArray(valuePointCount)
            }
            for (index in 0 until min(barCount, valueList.size)) {
                val value = valueList[index].coerceAtLeast(0F).coerceAtMost(1F)
                val bottomX = index * barXStep + halfBarWidth
                barArray[index] = bottomX
                barArray[index + 1] = barBottom - halfBarWidth
                barArray[index + 2] = bottomX
                barArray[index + 3] = barBottom - (barMaxHeight * value) + halfBarWidth
            }

            val pointCount = barCount * 4
            if (baseLineArray.size != pointCount) {
                baseLineArray = FloatArray(pointCount)
            }

            val halfBaseLineHeight = baseLineHeight * 0.5F
            val baseLineY = bounds.bottom - halfBaseLineHeight
            for (index in 0 until barCount) {
                val left = index * barXStep + halfBaseLineHeight
                baseLineArray[index] = left
                baseLineArray[index + 1] = baseLineY
                baseLineArray[index + 2] = left + barWidth - halfBaseLineHeight
                baseLineArray[index + 3] = baseLineY
            }
        }

        override fun setAlpha(alpha: Int) {
            paint.alpha = alpha
        }

        override fun setColorFilter(colorFilter: ColorFilter?) {
            paint.colorFilter = colorFilter
        }

        override fun getOpacity(): Int {
            return PixelFormat.TRANSPARENT
        }

    }

}