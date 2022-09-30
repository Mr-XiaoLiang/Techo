package com.lollipop.techo.view

import android.content.Context
import android.graphics.*
import android.graphics.drawable.Drawable
import android.util.AttributeSet
import androidx.annotation.ColorRes
import androidx.appcompat.widget.AppCompatImageView
import com.lollipop.base.util.getColor
import com.lollipop.techo.R

/**
 * 录音是波纹显示组件
 */
class RecorderWaveView(
    context: Context, attributeSet: AttributeSet?, style: Int
) : AppCompatImageView(context, attributeSet, style) {

    constructor(context: Context, attributeSet: AttributeSet?) : this(context, attributeSet, 0)

    constructor(context: Context) : this(context, null)

    private val waveDrawable = WaveDrawable()

    init {
        setImageDrawable(waveDrawable)

        attributeSet?.let { attrs ->
            val typeArray = context.obtainStyledAttributes(attrs, R.styleable.RecorderWaveView)
            lineSpace = typeArray.getDimensionPixelSize(R.styleable.RecorderWaveView_lineSpace, 1)
            lineWidth = typeArray.getDimensionPixelSize(R.styleable.RecorderWaveView_lineWidth, 1)
            color = typeArray.getColor(R.styleable.RecorderWaveView_lineColor, Color.BLACK)
            typeArray.recycle()
        }

        if (isInEditMode) {
            addData(listOf(0.5F, 0.7F, 0.4F, 0.9F, 0.3F, 0.4F, 0.5F))
        }
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

    fun addData(maxAmplitude: Float) {
        waveDrawable.addData(maxAmplitude)
    }

    fun addData(data: List<Float>) {
        waveDrawable.addData(data)
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

        var color: Int
            get() {
                return paint.color
            }
            set(value) {
                paint.color = value
            }

        private var dateSize = Int.MAX_VALUE

        private val waveList = ArrayList<Wave>()

        private val lineRect = RectF()

        fun addData(list: List<Float>) {
            if (list.size > dateSize) {
                waveList.clear()
                val start = list.size - dateSize
                val subList = list.subList(start, list.size - 1)
                subList.forEach {
                    waveList.add(createWave(it))
                }
            } else {
                val allSize = waveList.size + list.size
                if (allSize > dateSize && waveList.isNotEmpty()) {
                    val start = allSize - dateSize
                    if (start < waveList.size) {
                        waveList.subList(0, start).clear()
                    }
                }
                list.forEach {
                    waveList.add(createWave(it))
                }
            }
            invalidateSelf()
        }

        fun addData(info: Float) {
            waveList.add(createWave(info))
            // 超出10个就清理一次，不要太频繁了
            if (waveList.size > (dateSize + 10)) {
                val start = waveList.size - dateSize
                if (start < waveList.size) {
                    waveList.subList(0, start).clear()
                }
            }
            invalidateSelf()
        }

        private fun createWave(info: Float): Wave {
            return Wave(info)
        }

        override fun onBoundsChange(bounds: Rect) {
            super.onBoundsChange(bounds)
            val allLength = bounds.width() / 2 + lineWidth
            dateSize = allLength / (lineWidth + lineSpace) + 1
            invalidateSelf()
        }

        override fun draw(canvas: Canvas) {
            val r = lineWidth * 0.5F
            var left = 1F * bounds.exactCenterX() - r
            val centerY = bounds.centerY()
            val maxLength = bounds.height() * 0.5F
            for (wave in waveList) {
                val right = left + lineWidth
                if (right < 0) {
                    break
                }
                lineRect.set(
                    left,
                    centerY - maxLength * wave.top,
                    right,
                    centerY + maxLength * wave.bottom
                )
                canvas.drawRoundRect(lineRect, r, r, paint)
                left -= lineWidth
                left -= lineSpace
            }
        }

        override fun setAlpha(alpha: Int) {
            paint.alpha = alpha
        }

        override fun setColorFilter(colorFilter: ColorFilter?) {
            paint.colorFilter = colorFilter
        }

        @Deprecated(
            "Deprecated in Java",
            ReplaceWith("PixelFormat.TRANSPARENT", "android.graphics.PixelFormat")
        )
        override fun getOpacity(): Int {
            return PixelFormat.TRANSPARENT
        }

        private class Wave(amplitude: Float) {
            var top: Float = amplitude
                private set
            var bottom: Float = amplitude
                private set
        }

    }
}