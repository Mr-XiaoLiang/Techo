package com.lollipop.techo.recorder

import android.content.Context
import android.graphics.Color
import android.util.AttributeSet
import androidx.annotation.ColorRes
import androidx.appcompat.widget.AppCompatImageView
import com.lollipop.techo.R

/**
 * 录音时波纹显示组件
 */
class RecorderWaveView(
    context: Context, attributeSet: AttributeSet?, style: Int
) : AppCompatImageView(context, attributeSet, style) {

    constructor(context: Context, attributeSet: AttributeSet?) : this(context, attributeSet, 0)

    constructor(context: Context) : this(context, null)

    private val waveDrawable = RecorderWave.WaveDrawable()

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
        color = context.getColor(id)
    }

    fun setWaveProvider(provider: WaveProvider?) {
        this.waveProvider = provider
    }

    fun interface WaveProvider {
        fun getLastWave(): Float
    }

}