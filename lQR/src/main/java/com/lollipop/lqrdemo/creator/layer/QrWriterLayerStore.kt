package com.lollipop.lqrdemo.creator.layer

import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.LifecycleOwner
import com.lollipop.base.util.ListenerManager
import com.lollipop.lqrdemo.creator.writer.LayerDelegate
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
        onLayerChangedCallback: (Fork, QrWriterLayerType) -> Unit
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
        private val onLayerChangedCallback: (Fork, QrWriterLayerType) -> Unit
    ) : LifecycleEventObserver {

        private val alignmentLayer = LayerDelegate<AlignmentWriterLayer>(
            DefaultAlignmentWriterLayer::class.java
        )

        private val contentLayer = LayerDelegate<ContentWriterLayer>(
            DefaultContentWriterLayer::class.java
        )

        private val positionLayer = LayerDelegate<PositionWriterLayer>(
            DefaultPositionWriterLayer::class.java
        )

        internal fun onAlignmentLayerChanged(clazz: Class<AlignmentWriterLayer>?) {
            alignmentLayer.setLayer(clazz)
            onLayerChangedCallback(this, QrWriterLayerType.ALIGNMENT)
        }

        internal fun onContentLayerChanged(clazz: Class<ContentWriterLayer>?) {
            contentLayer.setLayer(clazz)
            onLayerChangedCallback(this, QrWriterLayerType.CONTENT)
        }

        internal fun onPositionLayerChanged(clazz: Class<PositionWriterLayer>?) {
            positionLayer.setLayer(clazz)
            onLayerChangedCallback(this, QrWriterLayerType.POSITION)
        }

        override fun onStateChanged(source: LifecycleOwner, event: Lifecycle.Event) {
            if (event.targetState <= minLifecycle) {
                listenerManager.removeListener(this)
                source.lifecycle.removeObserver(this)
            }
        }

    }

}