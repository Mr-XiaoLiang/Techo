package com.lollipop.techo.view

import android.content.Context
import android.graphics.*
import android.graphics.drawable.Drawable
import android.util.AttributeSet
import androidx.annotation.ColorRes
import androidx.appcompat.widget.AppCompatImageView
import com.lollipop.base.util.getColor
import com.lollipop.recorder.wave.WaveInfo
import kotlin.math.min

/**
 * 录音是波纹显示组件
 */
class RecorderWaveView(context: Context, attributeSet: AttributeSet?, style: Int) :
    AppCompatImageView(context, attributeSet, style) {

    constructor(context: Context, attributeSet: AttributeSet?) : this(context, attributeSet, 0)

    constructor(context: Context) : this(context, null)

    private val waveDrawable = WaveDrawable()

    init {
        setImageDrawable(waveDrawable)
    }

    var lineWidth: Int
        get() {
            return waveDrawable.lineWidth
        }
        set(value) {
            waveDrawable.lineWidth = value
        }

    var lineSpace: Int
        get() {
            return waveDrawable.lineSpace
        }
        set(value) {
            waveDrawable.lineSpace = value
        }

    var color: Int
        get() {
            return waveDrawable.color
        }
        set(value) {
            waveDrawable.color = value
        }

    fun addData(list: List<WaveInfo>) {
        waveDrawable.addData(list)
    }

    fun setColorByResource(@ColorRes id: Int) {
        color = getColor(id)
    }

    private class WaveDrawable : Drawable() {

        private val paint = Paint().apply {
            isDither = true
            isAntiAlias = true
        }

        var lineWidth = 1

        var lineSpace = 1

        var color = Color.BLACK

        private var dateSize = 1

        private val waveList = ArrayList<Wave>()
        private val tempList = ArrayList<Wave>()

        private val lineRect = RectF()

        fun addData(list: List<WaveInfo>) {
            if (list.size > dateSize) {
                waveList.clear()
                reversal(list, waveList)
            } else {
                tempList.clear()
                tempList.addAll(waveList)
                waveList.clear()
                val vacancy = dateSize - tempList.size
                reversal(list, waveList)
                accumulate(vacancy, tempList, waveList)
            }
            invalidateSelf()
        }

        private fun reversal(from: List<WaveInfo>, to: MutableList<Wave>) {
            for (index in from.size - 1 downTo 0) {
                to.add(createWave(from[index], to.size))
            }
        }

        private fun accumulate(limit: Int, from: List<Wave>, to: MutableList<Wave>) {
            val end = min(limit, from.size)
            for (i in 0 until end) {
                val wave = from[i]
                wave.offset(bounds, to.size, lineWidth, lineSpace)
                to.add(wave)
            }
        }

        private fun createWave(info: WaveInfo, index: Int): Wave {
            return Wave(info).apply {
                update(bounds, index, lineWidth, lineSpace)
            }
        }

        override fun onBoundsChange(bounds: Rect) {
            super.onBoundsChange(bounds)
            val allLength = bounds.width() / 2 + lineWidth
            dateSize = allLength / (lineWidth + lineSpace) + 1
            waveList.forEachIndexed { index, info ->
                info.update(bounds, index, lineWidth, lineSpace)
            }
        }

        override fun draw(canvas: Canvas) {
            val r = lineWidth * 0.5F
            waveList.forEach { wave ->
                lineRect.set(wave.left, wave.top, wave.right, wave.bottom)
                canvas.drawRoundRect(lineRect, r, r, paint)
            }
        }

        override fun setAlpha(alpha: Int) {
            paint.alpha = alpha
        }

        override fun setColorFilter(colorFilter: ColorFilter?) {
            paint.colorFilter = colorFilter
        }

        @Deprecated("Deprecated in Java",
            ReplaceWith("PixelFormat.TRANSPARENT", "android.graphics.PixelFormat")
        )
        override fun getOpacity(): Int {
            return PixelFormat.TRANSPARENT
        }

        private class Wave(val info: WaveInfo) {
            var top: Float = 0F
                private set
            var bottom: Float = 0F
                private set

            var left: Float = 0F
                private set

            var right: Float = 0F
                private set

            fun update(bounds: Rect, index: Int, lineWidth: Int, lineSpace: Int) {
                if (bounds.isEmpty) {
                    return
                }
                val height = bounds.height()
                val maxLength = height / 2
                val lineY = bounds.exactCenterY()
                top = lineY - getOffset(maxLength,  info.left)
                bottom = lineY + getOffset(maxLength,  info.right)
                offset(bounds, index, lineWidth, lineSpace)
            }

            fun offset(bounds: Rect, index: Int, lineWidth: Int, lineSpace: Int) {
                if (bounds.isEmpty) {
                    return
                }
                val centerX = bounds.exactCenterX()
                left = centerX - ((lineWidth + lineSpace) * index) - lineWidth
                right = left + lineWidth
            }

            private fun getOffset(max: Int, weight: Float): Float {
                val w = weight.rangeTo(0F, 1F)
                return max * w
            }

            private fun Float.rangeTo(min: Float, max: Float): Float {
                if (this < min) {
                    return min
                }
                if (this > max) {
                    return max
                }
                return this
            }
        }

    }

}