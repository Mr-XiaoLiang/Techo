package com.lollipop.techo.view

import android.content.Context
import android.graphics.Canvas
import android.graphics.ColorFilter
import android.graphics.Paint
import android.graphics.drawable.Drawable
import android.util.AttributeSet
import androidx.appcompat.widget.AppCompatImageView
import com.lollipop.recorder.VisualizerHelper
import com.lollipop.recorder.visualizer.VisualizerRenderer

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
        }

        var barCount: Int = DEFAULT_BAR_COUNT

        var intervalWeight: Float = DEFAULT_INTERVAL_WEIGHT

        var color: Int
            set(value) {
                paint.color = value
            }
            get() {
                return paint.color
            }

        override fun draw(canvas: Canvas) {
            TODO("Not yet implemented")
        }

        override fun setAlpha(alpha: Int) {
            TODO("Not yet implemented")
        }

        override fun setColorFilter(colorFilter: ColorFilter?) {
            TODO("Not yet implemented")
        }

        override fun getOpacity(): Int {
            TODO("Not yet implemented")
        }

    }

}