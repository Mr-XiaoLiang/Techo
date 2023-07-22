package com.lollipop.lqrdemo.writer

import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Rect
import com.lollipop.lqrdemo.writer.background.BackgroundWriterLayer
import com.lollipop.lqrdemo.writer.background.ColorBackgroundWriterLayer
import com.lollipop.qr.writer.LBitMatrix

abstract class QrWriter {

    protected val bounds: Rect = Rect()

    protected var bitMatrix: LBitMatrix? = null
        private set

    protected val backgroundLayer = LayerDelegate<BackgroundWriterLayer>()

    private val defaultBackgroundWriterLayer by lazy {
        ColorBackgroundWriterLayer()
    }

    fun setBitMatrix(matrix: LBitMatrix?) {
        this.bitMatrix = matrix
        onBitMatrixChanged()
    }

    protected open fun onBitMatrixChanged() {}

    fun setBounds(left: Int, top: Int, right: Int, bottom: Int) {
        bounds.set(left, top, right, bottom)
        updateLayoutBounds()
        onBoundsChanged()
    }

    open fun draw(canvas: Canvas) {
        getBackgroundLayer().draw(canvas)
        onDraw(canvas)
    }

    private fun getBackgroundLayer(): BackgroundWriterLayer {
        return backgroundLayer.get() ?: getDefaultBackgroundLayer()
    }

    protected open fun getDefaultBackgroundColor(): Int {
        return Color.WHITE
    }

    protected open fun getDefaultBackgroundLayer(): BackgroundWriterLayer {
        defaultBackgroundWriterLayer.customColor(getDefaultBackgroundColor())
        return defaultBackgroundWriterLayer
    }

    open fun onDraw(canvas: Canvas) {}

    open fun onBoundsChanged() {}

    fun setBackground(layer: Class<BackgroundWriterLayer>?) {
        backgroundLayer.setLayer(layer)
        updateLayoutBounds()
    }

    protected fun updateLayoutBounds() {
        backgroundLayer.get()?.onBoundsChanged(bounds)
    }

    protected open fun onBackgroundChanged() {}

}