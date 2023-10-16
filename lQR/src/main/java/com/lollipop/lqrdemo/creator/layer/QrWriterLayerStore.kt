package com.lollipop.lqrdemo.creator.layer

import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.LifecycleOwner
import com.lollipop.base.util.ListenerManager

object QrWriterLayerStore {

    private val listenerManager = ListenerManager<Fork>()

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
        return fork
    }

    class Fork(
        private val minLifecycle: Lifecycle.State
    ) : LifecycleEventObserver {

        override fun onStateChanged(source: LifecycleOwner, event: Lifecycle.Event) {
            if (event.targetState <= minLifecycle) {
                listenerManager.removeListener(this)
                source.lifecycle.removeObserver(this)
            }
        }

    }

}