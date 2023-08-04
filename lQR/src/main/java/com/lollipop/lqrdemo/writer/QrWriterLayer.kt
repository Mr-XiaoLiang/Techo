package com.lollipop.lqrdemo.writer

import androidx.lifecycle.Lifecycle
import com.bumptech.glide.RequestManager

abstract class QrWriterLayer {

    protected var callback: Callback? = null
        private set

    protected var lifecycle: Lifecycle? = null

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

    interface Callback {
        fun invalidateLayer(layer: QrWriterLayer)

        fun getLifecycle(): Lifecycle

        fun createGlideBuilder(): RequestManager

    }

}