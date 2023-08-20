package com.lollipop.lqrdemo.writer

import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Rect
import androidx.lifecycle.Lifecycle
import com.bumptech.glide.RequestManager
import com.lollipop.lqrdemo.creator.background.BackgroundCorner
import com.lollipop.lqrdemo.creator.background.BackgroundGravity
import com.lollipop.lqrdemo.writer.background.BackgroundWriterLayer
import com.lollipop.lqrdemo.writer.background.BitmapBackgroundWriterLayer
import com.lollipop.lqrdemo.writer.background.ColorBackgroundWriterLayer
import com.lollipop.qr.writer.LBitMatrix
import com.lollipop.qr.writer.LQrBitMatrix

abstract class QrWriter : QrWriterLayer.Callback {

    protected val bounds: Rect = Rect()

    protected var bitMatrix: LBitMatrix? = null
        private set

    protected open val scaleValue = 1F

    protected val backgroundLayer = LayerDelegate<BackgroundWriterLayer>(
        ColorBackgroundWriterLayer::class.java
    )

    protected var lastQuietZone = 0

    private var readyCallback: ResourceReadyCallback? = null

    private var contextProvider: ContextProvider? = null

    private var backgroundCorner: BackgroundCorner? = null

    protected var darkColor: Int = Color.BLACK
        private set
    protected var lightColor: Int = Color.WHITE
        private set

    init {
        initLayerCallback()
    }

    fun setContextProvider(provider: ContextProvider?) {
        this.contextProvider = provider
    }

    fun setQrPointColor(dark: Int = this.darkColor, light: Int = this.lightColor) {
        this.darkColor = dark
        this.lightColor = light
        onBitMatrixChanged()
    }

    fun setBackgroundCorner(corner: BackgroundCorner?) {
        this.backgroundCorner = corner
        updateLayerCorner(backgroundLayer.get())
        onBackgroundChanged()
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

    private fun onLayerChanged() {
        initLayerCallback()
        updateLayerCorner(backgroundLayer.get())
        backgroundLayer.get().onBoundsChanged(bounds)
        backgroundLayer.updateResource()
        onBackgroundChanged()
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
        return backgroundLayer.get()
    }

    private fun updateLayerCorner(layer: BackgroundWriterLayer) {
        val matrix = bitMatrix
        if (matrix is LQrBitMatrix) {
            val quietZone = (matrix.quietZone * scaleValue).toInt()
            lastQuietZone = quietZone
            val radius = BackgroundCorner.Radius.Absolute(quietZone.toFloat())
            val corner = backgroundCorner ?: BackgroundCorner.Round(
                radius,
                radius,
                radius,
                radius
            )
            layer.setCorner(corner)
            layer.onBoundsChanged(bounds)
        }
    }

    open fun onDraw(canvas: Canvas) {}

    open fun onBoundsChanged() {}

    fun setBackground(layer: Class<out BackgroundWriterLayer>?) {
        backgroundLayer.setLayer(layer)
        onLayerChanged()
    }

    fun updateLayerResource(callback: ResourceReadyCallback) {
        this.readyCallback = callback
        // 所有的都需要触发一下
        backgroundLayer.updateResource()
    }

    protected fun updateLayoutBounds() {
        backgroundLayer.get().onBoundsChanged(bounds)
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

    fun setBackgroundPhoto(url: String) {
        backgroundLayer.get().let {
            if (it is BitmapBackgroundWriterLayer) {
                it.setBitmapUrl(url)
            }
        }
    }

    fun setBackgroundGravity(gravity: BackgroundGravity) {
        backgroundLayer.get().let {
            if (it is BitmapBackgroundWriterLayer) {
                it.setGravity(gravity)
            }
        }
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