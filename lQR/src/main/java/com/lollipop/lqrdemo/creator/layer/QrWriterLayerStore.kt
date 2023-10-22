package com.lollipop.lqrdemo.creator.layer

import android.graphics.Rect
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.LifecycleOwner
import com.bumptech.glide.RequestManager
import com.lollipop.base.util.ListenerManager
import com.lollipop.lqrdemo.creator.writer.LayerDelegate
import com.lollipop.lqrdemo.creator.writer.QrWriterLayer
import com.lollipop.lqrdemo.creator.writer.QrWriterLayerType

object QrWriterLayerStore {

    private val listenerManager = ListenerManager<Fork>()

    private var alignment: Class<AlignmentWriterLayer>? = null
    private var content: Class<ContentWriterLayer>? = null
    private var position: Class<PositionWriterLayer>? = null

    fun setAlignmentLayer(clazz: Class<AlignmentWriterLayer>?) {
        alignment = clazz
        listenerManager.invoke { it.onAlignmentLayerChanged(clazz) }
    }

    fun setContentLayer(clazz: Class<ContentWriterLayer>?) {
        content = clazz
        listenerManager.invoke { it.onContentLayerChanged(clazz) }
    }

    fun setPositionLayer(clazz: Class<PositionWriterLayer>?) {
        position = clazz
        listenerManager.invoke { it.onPositionLayerChanged(clazz) }
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

        val alignmentLayer = LayerDelegate<AlignmentWriterLayer>(
            DefaultAlignmentWriterLayer::class.java
        )

        val contentLayer = LayerDelegate<ContentWriterLayer>(
            DefaultContentWriterLayer::class.java
        )

        val positionLayer = LayerDelegate<PositionWriterLayer>(
            DefaultPositionWriterLayer::class.java
        )

        private var layerCallback: QrWriterLayer.Callback? = null

        private val lastBounds = Rect()

        init {
            alignmentLayer.setLayerCallback(this)
            contentLayer.setLayerCallback(this)
            positionLayer.setLayerCallback(this)
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

        internal fun onAlignmentLayerChanged(clazz: Class<AlignmentWriterLayer>?) {
            alignmentLayer.setLayer(clazz)
            if (!lastBounds.isEmpty) {
                alignmentLayer.get().onBoundsChanged(lastBounds)
            }
            onLayerChangedCallback.onLayerChanged(this, QrWriterLayerType.ALIGNMENT)
        }

        internal fun onContentLayerChanged(clazz: Class<ContentWriterLayer>?) {
            contentLayer.setLayer(clazz)
            if (!lastBounds.isEmpty) {
                contentLayer.get().onBoundsChanged(lastBounds)
            }
            onLayerChangedCallback.onLayerChanged(this, QrWriterLayerType.CONTENT)
        }

        internal fun onPositionLayerChanged(clazz: Class<PositionWriterLayer>?) {
            positionLayer.setLayer(clazz)
            if (!lastBounds.isEmpty) {
                positionLayer.get().onBoundsChanged(lastBounds)
            }
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