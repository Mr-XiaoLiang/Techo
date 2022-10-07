package com.lollipop.techo.view

import android.graphics.*
import android.graphics.drawable.Drawable
import android.view.SurfaceHolder
import kotlin.math.max

class RecorderWave {

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

        var centerX: Float = 0F
            private set

        fun check(bounds: Rect, lineWidth: Int, lineSpace: Int) {
            radius = lineWidth * 0.5F
            centerY = bounds.exactCenterY()
            centerX = bounds.exactCenterX()
            maxLength = bounds.height() * 0.5F
            maxDotCount = ((bounds.width() / 2) / (lineWidth + lineSpace) + 1)
        }
    }

    private class WaveCore {
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
        }

        private fun createWave(info: Float): Wave {
            return Wave(info)
        }

        fun onBoundsChange(bounds: Rect) {
            val allLength = bounds.width() / 2 + lineWidth
            dateSize = allLength / (lineWidth + lineSpace) + 1
            option.check(bounds, lineWidth, lineSpace)
        }

        fun draw(canvas: Canvas) {
            val r = option.radius
            val centerY = option.centerY
            val maxLength = option.maxLength
            val maxDotCount = option.maxDotCount

            var left = 1F * option.centerX + r + lineSpace

            for (index in 0..maxDotCount) {
                drawDefaultWave(canvas, left, centerY, r)
                left += lineWidth
                left += lineSpace
            }

            left = 1F * option.centerX - r
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

        fun setAlpha(alpha: Int) {
            paint.alpha = alpha
        }

        fun setColorFilter(colorFilter: ColorFilter?) {
            paint.colorFilter = colorFilter
        }
    }

    class WaveDrawable : Drawable() {

        private val core = WaveCore()

        var lineWidth: Int
            set(value) {
                core.lineWidth = value
            }
            get() {
                return core.lineWidth
            }

        var lineSpace: Int
            set(value) {
                core.lineSpace = value
            }
            get() {
                return core.lineSpace
            }

        var color: Int
            get() {
                return core.color
            }
            set(value) {
                core.color = value
            }

        fun addData(list: List<Float>) {
            core.addData(list)
            invalidateSelf()
        }

        fun addData(info: Float) {
            core.addData(info)
            invalidateSelf()
        }

        override fun onBoundsChange(bounds: Rect) {
            super.onBoundsChange(bounds)
            core.onBoundsChange(bounds)
            invalidateSelf()
        }

        override fun draw(canvas: Canvas) {
            core.draw(canvas)
        }

        override fun setAlpha(alpha: Int) {
            core.setAlpha(alpha)
        }

        override fun setColorFilter(colorFilter: ColorFilter?) {
            core.setColorFilter(colorFilter)
        }

        @Deprecated(
            "Deprecated in Java",
            ReplaceWith("PixelFormat.TRANSPARENT", "android.graphics.PixelFormat")
        )
        override fun getOpacity(): Int {
            return PixelFormat.TRANSPARENT
        }

    }

    /**
     * 基于Surface的实现，但是用不上，就先这样吧
     */
    class SurfaceHelper : SurfaceHolder.Callback2 {

        private val core = WaveCore()

        var lineWidth: Int
            set(value) {
                core.lineWidth = value
            }
            get() {
                return core.lineWidth
            }

        var lineSpace: Int
            set(value) {
                core.lineSpace = value
            }
            get() {
                return core.lineSpace
            }

        var waveUpdateDuration = 50L

        var color: Int
            get() {
                return core.color
            }
            set(value) {
                core.color = value
            }

        private var surfaceHolder: SurfaceHolder? = null

        private var commandMode = 0

        private val bounds = Rect()

        fun start() {
            // TODO
        }

        fun stop() {
            // TODO
        }

        fun addData(list: List<Float>) {
            core.addData(list)
        }

        fun addData(info: Float) {
            core.addData(info)
        }

        fun setAlpha(alpha: Int) {
            core.setAlpha(alpha)
        }

        fun setColorFilter(colorFilter: ColorFilter?) {
            core.setColorFilter(colorFilter)
        }

        override fun surfaceCreated(holder: SurfaceHolder) {
            surfaceHolder = holder
        }

        override fun surfaceChanged(holder: SurfaceHolder, format: Int, width: Int, height: Int) {
            surfaceHolder = holder
            bounds.set(0, 0, width, height)
            core.onBoundsChange(bounds)
        }

        override fun surfaceDestroyed(holder: SurfaceHolder) {
            if (surfaceHolder === holder) {
                surfaceHolder = null
            }
        }

        override fun surfaceRedrawNeeded(holder: SurfaceHolder) {
            draw(holder)
        }

        private fun draw(holder: SurfaceHolder) {
            val canvas = holder.lockCanvas(bounds)
            core.draw(canvas)
            holder.unlockCanvasAndPost(canvas)
        }

    }

}