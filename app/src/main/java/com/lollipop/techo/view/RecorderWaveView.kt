package com.lollipop.techo.view

import android.content.Context
import android.graphics.*
import android.graphics.drawable.Drawable
import android.util.AttributeSet
import androidx.annotation.ColorRes
import androidx.appcompat.widget.AppCompatImageView
import com.lollipop.base.util.getColor
import com.lollipop.techo.R
import kotlin.math.max

/**
 * 录音是波纹显示组件
 */
class RecorderWaveView(
    context: Context, attributeSet: AttributeSet?, style: Int
) : AppCompatImageView(context, attributeSet, style) {

    constructor(context: Context, attributeSet: AttributeSet?) : this(context, attributeSet, 0)

    constructor(context: Context) : this(context, null)

    private val waveDrawable = WaveDrawable()

    private var waveProvider: WaveProvider? = null

    var waveUpdateDuration = 50L

    private val updateTask = Runnable {
        updateWave()
    }

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

    private fun updateWave() {
        val lastWave = waveProvider?.getLastWave() ?: return
        addData(lastWave)
        start()
    }

    fun start() {
        stop()
        waveProvider ?: return
        postDelayed(updateTask, waveUpdateDuration)
    }

    fun stop() {
        handler?.removeCallbacks(updateTask)
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

    fun setWaveProvider(provider: WaveProvider?) {
        this.waveProvider = provider
    }

    fun interface WaveProvider {
        fun getLastWave(): Float
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

        private val option = DrawOption()

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
            // 超出一倍就清理一次，不要太频繁了
            val maxSize = if (dateSize > Int.MAX_VALUE / 2) {
                Int.MAX_VALUE
            } else {
                dateSize * 2
            }
            if (waveList.size > maxSize) {
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
            option.check(bounds, lineWidth, lineSpace)
            invalidateSelf()
        }

        override fun draw(canvas: Canvas) {
            val r = option.radius
            val centerY = option.centerY
            val maxLength = option.maxLength
            val maxDotCount = option.maxDotCount

            var left = 1F * bounds.exactCenterX() + r + lineSpace

            for (index in 0..maxDotCount) {
                drawDefaultWave(canvas, left, centerY, r)
                left += lineWidth
                left += lineSpace
            }

            left = 1F * bounds.exactCenterX() - r
            for (index in waveList.size - 1 downTo 0) {
                val wave = waveList[index]
                val right = left + lineWidth
                if (right < 0) {
                    break
                }
                lineRect.set(
                    left,
                    centerY - max(maxLength * wave.top, r),
                    right,
                    centerY + max(maxLength * wave.bottom, r)
                )
                canvas.drawRoundRect(lineRect, r, r, paint)
                left -= lineWidth
                left -= lineSpace
            }
            if (waveList.size < maxDotCount) {
                for (index in waveList.size..maxDotCount) {
                    drawDefaultWave(canvas, left, centerY, r)
                    left -= lineWidth
                    left -= lineSpace
                }
            }
        }

        private fun drawDefaultWave(canvas: Canvas, left: Float, centerY: Float, r: Float) {
            canvas.drawOval(left, centerY - r, left + lineWidth, centerY + r, paint)
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

        private class DrawOption {

            var radius: Float = 0F
                private set
            var centerY: Float = 0F
                private set
            var maxLength: Float = 0F
                private set
            var maxDotCount: Int = 0
                private set

            fun check(bounds: Rect, lineWidth: Int, lineSpace: Int) {
                radius = lineWidth * 0.5F
                centerY = bounds.exactCenterY()
                maxLength = bounds.height() * 0.5F
                maxDotCount = ((bounds.width() / 2) / (lineWidth + lineSpace) + 1)
            }
        }

    }
}