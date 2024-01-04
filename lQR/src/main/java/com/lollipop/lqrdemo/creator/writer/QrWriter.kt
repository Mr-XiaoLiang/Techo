package com.lollipop.lqrdemo.creator.writer

import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Rect
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import com.bumptech.glide.RequestManager
import com.lollipop.base.util.lazyLogD
import com.lollipop.lqrdemo.creator.background.BackgroundCorner
import com.lollipop.lqrdemo.creator.background.BackgroundStore
import com.lollipop.lqrdemo.creator.layer.QrWriterLayerStore
import com.lollipop.lqrdemo.creator.writer.background.BackgroundWriterLayer
import com.lollipop.lqrdemo.creator.writer.background.DefaultBackgroundWriterLayer
import com.lollipop.qr.writer.LBitMatrix
import com.lollipop.qr.writer.LQrBitMatrix

abstract class QrWriter(
    private val lifecycleOwner: LifecycleOwner
) : QrWriterLayer.Callback {

    protected val bounds: Rect = Rect()

    protected var bitMatrix: LBitMatrix? = null
        private set

    protected var scaleValue = 1F

    protected val backgroundLayer = LayerDelegate<BackgroundWriterLayer>(
        DefaultBackgroundWriterLayer::class.java
    )

    private val writerLayer = QrWriterLayerStore.fork(
        lifecycleOwner.lifecycle,
        onLayerChangedCallback = ::onLayerChanged
    )

    private var readyCallback: ResourceReadyCallback? = null

    private var contextProvider: ContextProvider? = null

    protected var darkColor: Int = Color.BLACK
        private set
    protected var lightColor: Int = Color.TRANSPARENT
        private set

    protected val log by lazyLogD()

    init {
        initLayerCallback()
    }

    fun setContextProvider(provider: ContextProvider?) {
        this.contextProvider = provider
    }

    fun setQrPointColor(dark: Int = this.darkColor, light: Int = this.lightColor) {
        this.darkColor = dark
        this.lightColor = light
        writerLayer.setQrPointColor(dark, light)
        onBitMatrixChanged()
    }

    private fun initLayerCallback() {
        backgroundLayer.setLayerCallback(this)
        writerLayer.setLayerCallback(this)
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
        if (!writerLayer.isResourceReady()) {
            return false
        }
        return isAllReady(
            backgroundLayer
        )
    }

    private fun onLayerChanged() {
        initLayerCallback()
        notifyBackgroundChanged()
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
        writerLayer.setBitMatrix(matrix)
        onBitMatrixChanged()
    }

    protected open fun onLayerChanged(fork: QrWriterLayerStore.Fork, type: QrWriterLayerType) {
        when (type) {
            QrWriterLayerType.ALIGNMENT -> {
                invalidateLayer(fork.alignmentLayer.get())
            }

            QrWriterLayerType.CONTENT -> {
                invalidateLayer(fork.contentLayer.get())
            }

            QrWriterLayerType.POSITION -> {
                invalidateLayer(fork.positionLayer.get())
            }
        }
    }

    protected open fun onBitMatrixChanged() {
        updateScale()
        notifyBackgroundChanged()
    }

    private fun updateScale() {
        scaleValue = bitMatrix?.getScale(bounds.width().toFloat(), bounds.height().toFloat()) ?: 1F
    }

    fun setBounds(left: Int, top: Int, right: Int, bottom: Int) {
        bounds.set(left, top, right, bottom)
        updateScale()
        updateLayerBounds()
        onBoundsChanged()
    }

    open fun draw(canvas: Canvas) {
        onDrawBackground(canvas)
        onDraw(canvas)
    }

    open fun onDrawBackground(canvas: Canvas) {
        getBackgroundLayer().draw(canvas)
    }

    private fun getBackgroundLayer(): BackgroundWriterLayer {
        return backgroundLayer.get()
    }

    /**
     * 更新绘制层的圆角属性信息
     */
    private fun updateLayerCorner(layer: QrWriterLayer) {
        val matrix = bitMatrix
        val backgroundCorner: BackgroundCorner? = BackgroundStore.getCorner()
        if (backgroundCorner != null) {
            layer.setCorner(backgroundCorner)
            return
        }
        if (matrix is LQrBitMatrix) {
            val quietZone = (matrix.quietZone * scaleValue).toInt()
            val radius = BackgroundCorner.Radius.Absolute(quietZone.toFloat())
            val corner = BackgroundCorner.Round(radius, radius, radius, radius)
            layer.setCorner(corner)
        }
    }

    open fun onDraw(canvas: Canvas) {
        // 应该在这里剪裁和分发不同的绘制对象
        // 比如在这里按顺序绘制每个部分，如果没有被正确绘制或者没有设置相关绘制器，那么就执行默认的绘制内容
        writerLayer.draw(canvas)
    }

    open fun onBoundsChanged() {}

    fun setBackground(layer: Class<out BackgroundWriterLayer>?) {
        backgroundLayer.setLayer(layer)
        onLayerChanged()
    }

    fun updateLayerResource(callback: ResourceReadyCallback) {
        this.readyCallback = callback
        // 所有的都需要触发一下
        backgroundLayer.updateResource()
        writerLayer.updateResource()
    }

    protected fun updateLayerBounds() {
        backgroundLayer.get().let {
            it.onBoundsChanged(bounds)
            updateLayerCorner(it)
        }
        writerLayer.onBoundsChanged(bounds)
    }

    protected open fun onBackgroundChanged() {}

    override fun invalidateLayer(layer: QrWriterLayer) {
        contextProvider?.invalidateWriter()
    }

    override fun getLifecycle(): Lifecycle? {
        return contextProvider?.getContextLifecycle()
    }

    override fun createGlideBuilder(): RequestManager? {
        return contextProvider?.createGlideBuilder()
    }

    override fun onResourceReady(layer: QrWriterLayer) {
        onAnyLayerReady()
    }

    fun notifyBackgroundChanged() {
        log("notifyBackgroundChanged")
        val writerLayer = backgroundLayer.get()
        writerLayer.onBoundsChanged(bounds)
        writerLayer.updateResource()
        updateLayerCorner(writerLayer)
        onBackgroundChanged()
        writerLayer.invalidateSelf()
    }

    fun notifyStyleChanged() {
        log("notifyStyleChanged")
        writerLayer.onBoundsChanged(bounds)
        writerLayer.updateResource()
        writerLayer.invalidateSelf()
    }

    fun interface ResourceReadyCallback {
        fun onResourceReady()
    }

    interface ContextProvider {
        fun getContextLifecycle(): Lifecycle?

        fun createGlideBuilder(): RequestManager?

        fun invalidateWriter()
    }

}