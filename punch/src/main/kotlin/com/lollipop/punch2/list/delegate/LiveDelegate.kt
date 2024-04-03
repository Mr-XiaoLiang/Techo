package com.lollipop.punch2.list.delegate

import androidx.annotation.CallSuper
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.Lifecycle.Event.ON_ANY
import androidx.lifecycle.Lifecycle.Event.ON_CREATE
import androidx.lifecycle.Lifecycle.Event.ON_DESTROY
import androidx.lifecycle.Lifecycle.Event.ON_PAUSE
import androidx.lifecycle.Lifecycle.Event.ON_RESUME
import androidx.lifecycle.Lifecycle.Event.ON_START
import androidx.lifecycle.Lifecycle.Event.ON_STOP
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.LifecycleOwner
import com.lollipop.punch2.utils.tryUI

abstract class LiveDelegate {

    protected var currentState: Lifecycle.State = Lifecycle.State.DESTROYED
        private set

    private val lifecycleEventListener = LifecycleEventObserver { source, event ->
        onLifecycleStateChanged(source, event)
    }

    protected val isActive: Boolean
        get() {
            return currentState.isAtLeast(Lifecycle.State.STARTED)
        }

    fun bind(lifecycle: Lifecycle) {
        lifecycle.addObserver(lifecycleEventListener)
    }

    @CallSuper
    protected open fun onLifecycleStateChanged(source: LifecycleOwner, event: Lifecycle.Event) {
        currentState = event.targetState
        when (event) {
            ON_CREATE -> {}
            ON_START -> {
                onStart()
            }

            ON_RESUME -> {}

            ON_PAUSE -> {}

            ON_STOP -> {
                onStop()
            }

            ON_DESTROY -> {}
            ON_ANY -> {}
        }
    }

    protected abstract fun onStart()

    protected open fun onStop() {}

    protected inline fun <reified T> T.invokeOnActive(crossinline callback: T.() -> Unit) {
        tryUI {
            if (isActive) {
                try {
                    callback()
                } catch (e: Throwable) {
                    e.printStackTrace()
                }
            }
        }
    }

}