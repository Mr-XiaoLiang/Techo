package com.lollipop.lqrdemo.creator.layer

import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Rect
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.LifecycleOwner
import com.bumptech.glide.RequestManager
import com.lollipop.base.util.ListenerManager
import com.lollipop.base.util.checkExtends
import com.lollipop.lqrdemo.creator.writer.LayerDelegate
import com.lollipop.lqrdemo.creator.writer.QrWriterLayer
import com.lollipop.lqrdemo.creator.writer.QrWriterLayerType
import com.lollipop.qr.writer.LBitMatrix

object QrWriterLayerStore {

    private val listenerManager = ListenerManager<Fork>()

    private var alignment: Class<BitMatrixWriterLayer>? = null
    private var content: Class<BitMatrixWriterLayer>? = null
    private var position: Class<BitMatrixWriterLayer>? = null

    fun setLayer(clazz: Class<BitMatrixWriterLayer>?) {
        clazz ?: return
        if (clazz.checkExtends<AlignmentWriterLayer>()) {
            alignment = clazz
            listenerManager.invoke { it.onAlignmentLayerChanged(clazz) }
        }
        if (clazz.checkExtends<ContentWriterLayer>()) {
            content = clazz
            listenerManager.invoke { it.onContentLayerChanged(clazz) }
        }
        if (clazz.checkExtends<PositionWriterLayer>()) {
            position = clazz
            listenerManager.invoke { it.onPositionLayerChanged(clazz) }
        }
    }

    fun clear(type: QrWriterLayerType) {
        when (type) {
            QrWriterLayerType.ALIGNMENT -> {
                alignment = null
                listenerManager.invoke { it.onAlignmentLayerChanged(null) }
            }

            QrWriterLayerType.CONTENT -> {
                content = null
                listenerManager.invoke { it.onContentLayerChanged(null) }
            }

            QrWriterLayerType.POSITION -> {
                position = null
                listenerManager.invoke { it.onPositionLayerChanged(null) }
            }
        }
    }

    fun fork(
        lifecycle: Lifecycle,
        minLifecycle: Lifecycle.State = Lifecycle.State.DESTROYED,
        onLayerChangedCallback: OnLayerChangedCallback
    ): Fork {
        val fork = Fork(minLifecycle, onLayerChangedCallback)
        if (lifecycle.currentState <= minLifecycle) {
            return fork
        }
        listenerManager.addListener(fork)
        lifecycle.addObserver(fork)
        fork.onAlignmentLayerChanged(alignment)
        fork.onContentLayerChanged(content)
        fork.onPositionLayerChanged(position)
        return fork
    }

    class Fork(
        private val minLifecycle: Lifecycle.State,
        private val onLayerChangedCallback: OnLayerChangedCallback
    ) : LifecycleEventObserver, QrWriterLayer.Callback {

        val alignmentLayer = LayerDelegate<BitMatrixWriterLayer>(
            DefaultWriterLayer::class.java
        )

        val contentLayer = LayerDelegate<BitMatrixWriterLayer>(
            DefaultWriterLayer::class.java
        )

        val positionLayer = LayerDelegate<BitMatrixWriterLayer>(
            DefaultWriterLayer::class.java
        )

        private var layerCallback: QrWriterLayer.Callback? = null

        private val lastBounds = Rect()
        private var lastMatrix: LBitMatrix? = null
        protected var darkColor: Int = Color.BLACK
        protected var lightColor: Int = Color.TRANSPARENT

        init {
            alignmentLayer.setLayerCallback(this)
            contentLayer.setLayerCallback(this)
            positionLayer.setLayerCallback(this)
        }

        inline fun <reified T> LayerDelegate<*>.typedLayer(): T? {
            val layer = get()
            if (layer is T) {
                return layer
            }
            return null
        }

        fun findAlignmentLayer(): AlignmentWriterLayer? {
            return alignmentLayer.typedLayer<AlignmentWriterLayer>()
        }

        fun findContentLayer(): ContentWriterLayer? {
            return contentLayer.typedLayer<ContentWriterLayer>()
        }

        fun findPositionLayer(): PositionWriterLayer? {
            return positionLayer.typedLayer<PositionWriterLayer>()
        }

        fun draw(canvas: Canvas) {
            findAlignmentLayer()?.drawAlignment(canvas)
            findContentLayer()?.drawContent(canvas)
            findPositionLayer()?.drawPosition(canvas)
        }

        fun setLayerCallback(callback: QrWriterLayer.Callback) {
            this.layerCallback = callback
        }

        fun updateResource() {
            alignmentLayer.updateResource()
            contentLayer.updateResource()
            positionLayer.updateResource()
        }

        fun onBoundsChanged(bounds: Rect) {
            lastBounds.set(bounds)
            alignmentLayer.get().onBoundsChanged(bounds)
            contentLayer.get().onBoundsChanged(bounds)
            positionLayer.get().onBoundsChanged(bounds)
        }

        fun setBitMatrix(matrix: LBitMatrix?) {
            lastMatrix = matrix
            alignmentLayer.get().setBitMatrix(matrix)
            contentLayer.get().setBitMatrix(matrix)
            positionLayer.get().setBitMatrix(matrix)
        }

        fun setQrPointColor(dark: Int, light: Int) {
            darkColor = dark
            lightColor = light
            alignmentLayer.get().setQrPointColor(dark, light)
            contentLayer.get().setQrPointColor(dark, light)
            positionLayer.get().setQrPointColor(dark, light)
        }

        private fun updateNewLayer(layer: BitMatrixWriterLayer) {
            if (!lastBounds.isEmpty) {
                layer.onBoundsChanged(lastBounds)
            }
            layer.setBitMatrix(lastMatrix)
            layer.setQrPointColor(darkColor, lightColor)
        }

        internal fun onAlignmentLayerChanged(clazz: Class<BitMatrixWriterLayer>?) {
            alignmentLayer.setLayer(clazz)
            updateNewLayer(alignmentLayer.get())
            onLayerChangedCallback.onLayerChanged(this, QrWriterLayerType.ALIGNMENT)
        }

        internal fun onContentLayerChanged(clazz: Class<BitMatrixWriterLayer>?) {
            contentLayer.setLayer(clazz)
            updateNewLayer(contentLayer.get())
            onLayerChangedCallback.onLayerChanged(this, QrWriterLayerType.CONTENT)
        }

        internal fun onPositionLayerChanged(clazz: Class<BitMatrixWriterLayer>?) {
            positionLayer.setLayer(clazz)
            updateNewLayer(positionLayer.get())
            onLayerChangedCallback.onLayerChanged(this, QrWriterLayerType.POSITION)
        }

        override fun onStateChanged(source: LifecycleOwner, event: Lifecycle.Event) {
            if (event.targetState <= minLifecycle) {
                listenerManager.removeListener(this)
                source.lifecycle.removeObserver(this)
            }
        }

        override fun invalidateLayer(layer: QrWriterLayer) {
            layerCallback?.invalidateLayer(layer)
        }

        override fun getLifecycle(): Lifecycle? {
            return layerCallback?.getLifecycle()
        }

        override fun createGlideBuilder(): RequestManager? {
            return layerCallback?.createGlideBuilder()
        }

        override fun onResourceReady(layer: QrWriterLayer) {
            layerCallback?.onResourceReady(layer)
        }

    }

    fun interface OnLayerChangedCallback {
        fun onLayerChanged(fork: Fork, type: QrWriterLayerType)
    }

}