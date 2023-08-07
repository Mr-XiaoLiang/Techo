package com.lollipop.lqrdemo.writer

import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Rect
import androidx.lifecycle.Lifecycle
import com.bumptech.glide.RequestManager
import com.lollipop.lqrdemo.writer.background.BackgroundWriterLayer
import com.lollipop.lqrdemo.writer.background.ColorBackgroundWriterLayer
import com.lollipop.qr.writer.LBitMatrix
import com.lollipop.qr.writer.LQrBitMatrix

abstract class QrWriter : QrWriterLayer.Callback {

    protected val bounds: Rect = Rect()

    protected var bitMatrix: LBitMatrix? = null
        private set

    protected open val scaleValue = 1F

    protected val backgroundLayer = LayerDelegate<BackgroundWriterLayer>()

    private val defaultBackgroundWriterLayer by lazy {
        ColorBackgroundWriterLayer()
    }

    protected var lastQuietZone = 0

    private var readyCallback: ResourceReadyCallback? = null

    private var contextProvider: ContextProvider? = null

    init {
        initLayerCallback()
    }

    fun setContextProvider(provider: ContextProvider) {
        this.contextProvider = provider
    }

    private fun initLayerCallback() {
        backgroundLayer.setLayerCallback(this)
    }

    private fun onAnyLayerReady() {
        readyCallback?.let {
            if (checkLayerReady()) {
                it.onResourceReady()
                readyCallback = null
            }
        }
    }

    protected open fun checkLayerReady(): Boolean {
        return isAllReady(backgroundLayer)
    }

    protected fun isAllReady(vararg layer: LayerDelegate<*>): Boolean {
        for (l in layer) {
            if (!l.isResourceReady()) {
                return false
            }
        }
        return true
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
        val backgroundWriterLayer = defaultBackgroundWriterLayer
        backgroundWriterLayer.customColor(getDefaultBackgroundColor())
        val matrix = bitMatrix
        if (matrix is LQrBitMatrix) {
            val quietZone = (matrix.quietZone * scaleValue).toInt()
            if (quietZone != lastQuietZone) {
                lastQuietZone = quietZone
                val radius = BackgroundWriterLayer.Radius.Absolute(quietZone.toFloat())
                val corner = BackgroundWriterLayer.Corner.Round(radius, radius, radius, radius)
                backgroundWriterLayer.setCorner(corner)
                backgroundWriterLayer.onBoundsChanged(bounds)
            }
        }
        return backgroundWriterLayer
    }

    open fun onDraw(canvas: Canvas) {}

    open fun onBoundsChanged() {}

    fun setBackground(layer: Class<BackgroundWriterLayer>?) {
        backgroundLayer.setLayer(layer)
        backgroundLayer.get()?.onBoundsChanged(bounds)
        onBackgroundChanged()
    }

    fun updateLayerResource(callback: ResourceReadyCallback) {
        this.readyCallback = callback
        // 所有的都需要触发一下
        backgroundLayer.updateResource()
    }

    protected fun updateLayoutBounds() {
        backgroundLayer.get()?.onBoundsChanged(bounds)
        getDefaultBackgroundLayer().onBoundsChanged(bounds)
    }

    protected open fun onBackgroundChanged() {}

    override fun invalidateLayer(layer: QrWriterLayer) {
        contextProvider?.invalidateWriter()
    }

    override fun getLifecycle(): Lifecycle? {
        return contextProvider?.getLifecycle()
    }

    override fun createGlideBuilder(): RequestManager? {
        return contextProvider?.createGlideBuilder()
    }

    override fun onResourceReady(layer: QrWriterLayer) {
        onAnyLayerReady()
    }

    fun interface ResourceReadyCallback {
        fun onResourceReady()
    }

    interface ContextProvider {
        fun getLifecycle(): Lifecycle?

        fun createGlideBuilder(): RequestManager?

        fun invalidateWriter()
    }

}