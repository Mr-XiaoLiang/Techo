package com.lollipop.lqrdemo.creator.layer

import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.LifecycleOwner
import com.lollipop.base.util.ListenerManager
import com.lollipop.lqrdemo.creator.writer.QrWriterLayer

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
        minLifecycle: Lifecycle.State = Lifecycle.State.DESTROYED
    ): Fork {
        val fork = Fork(minLifecycle)
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
        private val minLifecycle: Lifecycle.State
    ) : LifecycleEventObserver {

        fun onAlignmentLayerChanged(clazz: Class<QrWriterLayer>?) {
            // TODO
        }

        fun onContentLayerChanged(clazz: Class<QrWriterLayer>?) {
            // TODO
        }

        fun onPositionLayerChanged(clazz: Class<QrWriterLayer>?) {
            // TODO
        }

        override fun onStateChanged(source: LifecycleOwner, event: Lifecycle.Event) {
            if (event.targetState <= minLifecycle) {
                listenerManager.removeListener(this)
                source.lifecycle.removeObserver(this)
            }
        }

    }

}