package com.lollipop.lqrdemo.creator.layer

import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.LifecycleOwner
import com.lollipop.base.util.ListenerManager
import com.lollipop.lqrdemo.creator.writer.LayerDelegate
import com.lollipop.lqrdemo.creator.writer.QrWriterLayer
import com.lollipop.lqrdemo.creator.writer.QrWriterLayerType

object QrWriterLayerStore {

    private val listenerManager = ListenerManager<Fork>()

    private var alignment: Class<QrWriterLayer>? = null
    private var content: Class<QrWriterLayer>? = null
    private var position: Class<QrWriterLayer>? = null

    fun setAlignmentLayer(clazz: Class<QrWriterLayer>?) {
        alignment = clazz
        listenerManager.invoke { it.onAlignmentLayerChanged(clazz) }
    }

    fun setContentLayer(clazz: Class<QrWriterLayer>?) {
        content = clazz
        listenerManager.invoke { it.onContentLayerChanged(clazz) }
    }

    fun setPositionLayer(clazz: Class<QrWriterLayer>?) {
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

        private val alignmentLayer = LayerDelegate<QrWriterLayer>(
            TODO("需要真正的默认实现")
        )

        private val contentLayer = LayerDelegate<QrWriterLayer>(
            TODO("需要真正的默认实现")
        )

        private val positionLayer = LayerDelegate<QrWriterLayer>(
            TODO("需要真正的默认实现")
        )

        internal fun onAlignmentLayerChanged(clazz: Class<QrWriterLayer>?) {
            alignmentLayer.setLayer(clazz)
            onLayerChangedCallback(this, QrWriterLayerType.ALIGNMENT)
        }

        internal fun onContentLayerChanged(clazz: Class<QrWriterLayer>?) {
            contentLayer.setLayer(clazz)
            onLayerChangedCallback(this, QrWriterLayerType.CONTENT)
        }

        internal fun onPositionLayerChanged(clazz: Class<QrWriterLayer>?) {
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