package com.lollipop.lqrdemo.writer

import androidx.lifecycle.Lifecycle
import com.bumptech.glide.RequestManager
import com.lollipop.base.util.task

abstract class QrWriterLayer {

    protected var callback: Callback? = null
        private set

    protected var lifecycle: Lifecycle? = null

    var isResourceReady: Boolean = false
        private set

    private val updateResourceTask = task {
        onUpdateResource()
    }

    fun setLayerCallback(callback: Callback) {
        this.callback = callback
    }

    fun invalidateSelf() {
        callback?.invalidateLayer(this)
    }

    protected fun getLifecycle(): Lifecycle? {
        return callback?.getLifecycle()
    }

    protected fun glide(): RequestManager? {
        return callback?.createGlideBuilder()
    }

    protected fun onResourceReady() {
        isResourceReady = true
        callback?.onResourceReady(this)
    }

    protected fun notifyResourceChanged() {
        isResourceReady = false
        updateResourceTask.cancel()
        updateResourceTask.sync()
    }

    fun updateResource() {
        notifyResourceChanged()
    }

    protected open fun onUpdateResource() {
        onResourceReady()
    }

    interface Callback {
        fun invalidateLayer(layer: QrWriterLayer)

        fun getLifecycle(): Lifecycle?

        fun createGlideBuilder(): RequestManager?

        fun onResourceReady(layer: QrWriterLayer)

    }

}