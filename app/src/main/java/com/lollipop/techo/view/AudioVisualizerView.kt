package com.lollipop.techo.view

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.*
import android.graphics.drawable.Drawable
import android.util.AttributeSet
import android.view.MotionEvent
import androidx.appcompat.widget.AppCompatImageView
import com.lollipop.base.util.SingleTouchSlideHelper
import com.lollipop.recorder.VisualizerHelper
import com.lollipop.recorder.VisualizerRenderer
import com.lollipop.techo.R
import kotlin.math.min

class AudioVisualizerView(
    context: Context, attributeSet: AttributeSet?, style: Int
) : AppCompatImageView(context, attributeSet, style),
    VisualizerRenderer,
    SingleTouchSlideHelper.OnTouchMoveListener,
    SingleTouchSlideHelper.OnTouchEndListener {

    companion object {
        const val DEFAULT_BAR_COUNT = 64
        const val DEFAULT_INTERVAL_WEIGHT = 0.5F
    }

    constructor(context: Context, attributeSet: AttributeSet?) : this(context, attributeSet, 0)

    constructor(context: Context) : this(context, null)

    private val visualizerDrawable = AudioVisualizerDrawable()

    private val touchHelper = SingleTouchSlideHelper(
        context,
        SingleTouchSlideHelper.Slider.Horizontally
    )

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

    var baseLineHeight: Float
        get() {
            return visualizerDrawable.baseLineHeight
        }
        set(value) {
            visualizerDrawable.baseLineHeight = value
        }

    var baseLineInterval: Float
        get() {
            return visualizerDrawable.baseLineInterval
        }
        set(value) {
            visualizerDrawable.baseLineInterval = value
        }

    var selectedColor: Int
        get() {
            return visualizerDrawable.selectedColor
        }
        set(value) {
            visualizerDrawable.selectedColor = value
        }

    var defaultColor: Int
        get() {
            return visualizerDrawable.defaultColor
        }
        set(value) {
            visualizerDrawable.defaultColor = value
        }

    private var dragProgress = 0F

    private var dragMode = false

    private var onSeekChangedCallback: OnSeekChangedCallback? = null

    init {
        setImageDrawable(visualizerDrawable)
        attributeSet?.let { a ->
            val typeArray = context.obtainStyledAttributes(a, R.styleable.AudioVisualizerView)
            barCount = typeArray.getInt(R.styleable.AudioVisualizerView_barCount, DEFAULT_BAR_COUNT)
            intervalWeight = typeArray.getFloat(
                R.styleable.AudioVisualizerView_intervalWeight,
                DEFAULT_INTERVAL_WEIGHT
            )
            baseLineHeight = typeArray.getDimensionPixelSize(
                R.styleable.AudioVisualizerView_baseLineHeight,
                0
            ).toFloat()
            baseLineInterval = typeArray.getDimensionPixelSize(
                R.styleable.AudioVisualizerView_baseLineInterval,
                0
            ).toFloat()
            selectedColor = typeArray.getColor(
                R.styleable.AudioVisualizerView_selectedColor,
                Color.BLUE
            )
            defaultColor = typeArray.getColor(
                R.styleable.AudioVisualizerView_defaultColor,
                Color.GRAY
            )
            typeArray.recycle()
        }

        touchHelper.addMoveListener(this)
        touchHelper.addEndListener(this)

        if (isInEditMode) {
            onValueChanged(listOf(0.5F, 0.6F, 0.7F, 0.8F, 0.9F, 1F, 0.8F))
            onProgressChanged(0.4F)
        }
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun onTouchEvent(event: MotionEvent?): Boolean {
        return touchHelper.onTouch(event) || super.onTouchEvent(event)
    }

    override fun onRender(data: VisualizerHelper.Frequency) {
        super.onRender(data)
        val dataSize = data.magnitudes.size
        if (dataSize == 0) {
            onValueChanged(emptyList())
        } else {
            val step = dataSize * 1F / barCount
            val values = ArrayList<Float>()
            for (index in 0 until barCount) {
                val dataIndex = (index * step).toInt().coerceAtMost(dataSize - 1).coerceAtLeast(0)
                values.add(data.magnitudes[dataIndex] / VisualizerHelper.Frequency.MAX)
            }
            onValueChanged(values)
        }
    }

    fun onValueChanged(newValue: List<Float>) {
        visualizerDrawable.onValueChanged(newValue)
    }

    fun onProgressChanged(progress: Float) {
        if (!dragMode) {
            visualizerDrawable.onProgressChanged(progress)
        }
    }

    fun onSeekChanged(callback: OnSeekChangedCallback?) {
        this.onSeekChangedCallback = callback
    }

    override fun onTouchMoved(x: Float, y: Float) {
        dragMode = true
        val progress = (x - paddingLeft) / (width - paddingLeft - paddingRight)
        dragProgress = progress.coerceAtLeast(0F).coerceAtMost(1F)
        visualizerDrawable.onProgressChanged(dragProgress)
    }

    override fun onTouchEnd(isCancel: Boolean) {
        if (!isCancel) {
            onSeekChangedCallback?.onSeekChanged(dragProgress)
        }
    }

    fun interface OnSeekChangedCallback {
        fun onSeekChanged(progress: Float)
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

        private var progress = 0F

        var selectedColor: Int = Color.BLUE

        var defaultColor: Int = Color.GRAY

        private val valueList = ArrayList<Float>()

        private val progressClipBounds = Rect()

        override fun draw(canvas: Canvas) {
            // 背景
            drawLines(canvas, defaultColor)

            val saveCount = canvas.save()
            canvas.clipRect(progressClipBounds)
            // 前景
            drawLines(canvas, selectedColor)
            canvas.restoreToCount(saveCount)
        }

        private fun drawLines(canvas: Canvas, color: Int) {
            paint.color = color
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
            barWidth = bounds.width() / (barCount * (1 + intervalWeight) - intervalWeight)
            barMaxHeight = (bounds.height() - baseLineHeight - baseLineInterval)
            barBottom = barMaxHeight
            intervalWidth = barWidth * intervalWeight

            buildLines()
            updateClipBounds()
        }

        fun onValueChanged(newValue: List<Float>) {
            valueList.clear()
            valueList.addAll(newValue)
            buildLines()
            invalidateSelf()
        }

        fun onProgressChanged(progress: Float) {
            this.progress = progress.coerceAtLeast(0F).coerceAtMost(1F)
            updateClipBounds()
            invalidateSelf()
        }

        private fun updateClipBounds() {
            val right = ((bounds.width() * progress) + bounds.left).toInt()
            progressClipBounds.set(bounds.left, bounds.top, right, bounds.bottom)
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
                val i = index * 4
                barArray[i] = bottomX
                barArray[i + 1] = barBottom - halfBarWidth
                barArray[i + 2] = bottomX
                barArray[i + 3] = barBottom - (barMaxHeight * value) + halfBarWidth
            }

            val pointCount = barCount * 4
            if (baseLineArray.size != pointCount) {
                baseLineArray = FloatArray(pointCount)
            }

            val halfBaseLineHeight = baseLineHeight * 0.5F
            val baseLineY = bounds.bottom - halfBaseLineHeight
            for (index in 0 until barCount) {
                val left = index * barXStep + halfBaseLineHeight
                val i = index * 4
                baseLineArray[i] = left
                baseLineArray[i + 1] = baseLineY
                baseLineArray[i + 2] = left + barWidth - baseLineHeight
                baseLineArray[i + 3] = baseLineY
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

    }

}