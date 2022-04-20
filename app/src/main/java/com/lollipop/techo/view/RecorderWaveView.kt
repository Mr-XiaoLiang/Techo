package com.lollipop.techo.view

import android.content.Context
import android.graphics.*
import android.graphics.drawable.Drawable
import android.util.AttributeSet
import androidx.annotation.ColorInt
import androidx.annotation.ColorRes
import androidx.appcompat.widget.AppCompatImageView
import com.lollipop.base.util.getColor
import com.lollipop.recorder.wave.WaveInfo

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

        private val waveList = ArrayList<WaveInfo>()
        private val tempList = ArrayList<WaveInfo>()

        fun addData(list: List<WaveInfo>) {
            if (list.size > dateSize) {
                waveList.clear()
                waveList.addAll(list)
            } else {
                tempList.clear()
                tempList.addAll(waveList)
                waveList.clear()
                val offset = tempList.size + list.size - dateSize
                if (offset < 0) {
                    waveList.addAll(tempList)
                    waveList.addAll(list)
                } else {
                    for (i in offset until tempList.size) {
                        waveList.add(tempList[i])
                    }
                    waveList.addAll(list)
                }
            }
            invalidateSelf()
        }

        override fun onBoundsChange(bounds: Rect) {
            super.onBoundsChange(bounds)
            val allLength = bounds.width() / 2 + lineWidth
            dateSize = allLength / (lineWidth + lineSpace) + 1
        }

        override fun draw(canvas: Canvas) {
            TODO("Not yet implemented")
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