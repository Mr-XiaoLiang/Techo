package com.lollipop.qr.comm

import android.os.Handler
import android.os.HandlerThread
import android.os.Looper
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.LifecycleOwner
import java.util.concurrent.Executor

abstract class BarcodeExecutor(
    protected val lifecycleOwner: LifecycleOwner
) {

    protected var currentStatus = Lifecycle.State.DESTROYED
        private set

    protected val isResumed: Boolean
        get() {
            return currentStatus.isAtLeast(Lifecycle.State.RESUMED)
        }

    private val lifecycleObserver = LifecycleEventObserver { source, _ ->
        currentStatus = source.lifecycle.currentState
        onLifecycleStateChanged()
    }

    protected val mainExecutor = MainExecutor()
    private var analyzerExecutorImpl: SingleExecutor? = null

    init {
        currentStatus = lifecycleOwner.lifecycle.currentState
        lifecycleOwner.lifecycle.addObserver(lifecycleObserver)
    }

    protected val analyzerExecutor: Executor
        get() {
            return getOrCreateAnalyzerExecutor()
        }

    private fun getOrCreateAnalyzerExecutor(): Executor {
        val executor = analyzerExecutorImpl
        if (executor != null && !executor.isDestroy) {
            return executor
        }
        val newExecutor = SingleExecutor(lifecycleOwner, "AnalyzerExecutor")
        analyzerExecutorImpl = newExecutor
        return newExecutor
    }

    protected open fun onLifecycleStateChanged() {}

    protected fun doAsync(command: Runnable) {
        analyzerExecutor.execute(command)
    }

    protected fun onUI(command: Runnable) {
        mainExecutor.execute(command)
    }

    private class SingleExecutor(lifecycleOwner: LifecycleOwner, threadName: String) : Executor {

        private val thread = HandlerThread(threadName).apply {
            start()
        }

        private val handler = Handler(thread.looper)

        var isDestroy = false
            private set

        init {
            lifecycleOwner.lifecycle.addObserver(object : LifecycleEventObserver {
                override fun onStateChanged(source: LifecycleOwner, event: Lifecycle.Event) {
                    if (event.targetState == Lifecycle.State.DESTROYED) {
                        destroy()
                    }
                }
            })
        }

        override fun execute(command: Runnable?) {
            if (isDestroy) {
                return
            }
            command ?: return
            handler.post(command)
        }

        fun destroy() {
            if (isDestroy) {
                return
            }
            isDestroy = true
            thread.quitSafely()
        }
    }

    protected class MainExecutor : Executor {

        private val handler = Handler(Looper.getMainLooper())

        override fun execute(command: Runnable?) {
            command ?: return
            handler.post(command)
        }

        fun delay(delay: Long, command: Runnable) {
            handler.postDelayed(command, delay)
        }

        fun cancel(command: Runnable) {
            handler.removeCallbacks(command)
        }

    }

}