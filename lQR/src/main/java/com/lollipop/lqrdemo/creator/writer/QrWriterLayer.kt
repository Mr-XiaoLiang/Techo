package com.lollipop.lqrdemo.creator.writer

import androidx.lifecycle.Lifecycle
import com.bumptech.glide.RequestManager
import com.lollipop.base.util.lazyLogD
import com.lollipop.base.util.task

abstract class QrWriterLayer {

    protected val log by lazyLogD()

    protected var callback: Callback? = null
        private set

    var isResourceReady: Boolean = false
        private set

    private val updateResourceTask = task {
        onUpdateResource()
    }

    fun setLayerCallback(callback: Callback) {
        this.callback = callback
    }

    fun invalidateSelf() {
        log("invalidateSelf")
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
        invalidateSelf()
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