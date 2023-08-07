package com.lollipop.lqrdemo.writer

import androidx.collection.LruCache
import androidx.lifecycle.Lifecycle
import com.bumptech.glide.RequestManager

class LayerDelegate<T : QrWriterLayer> : QrWriterLayer.Callback {

    private val cache = LruCache<String, T>(3)

    private var current: T? = null

    private var layerCallback: QrWriterLayer.Callback? = null

    fun setLayerCallback(callback: QrWriterLayer.Callback) {
        this.layerCallback = callback
    }

    fun setLayer(clazz: Class<T>?) {
        if (clazz == null) {
            current = null
            return
        }
        // 获取layer对应的Key
        val key = getKey(clazz)
        // 尝试获取缓存
        val ins = cache.get(key)
        // 缓存不为空，那么使用缓存
        if (ins != null) {
            current = ins
            current?.setLayerCallback(this)
            return
        }
        // 如果没有缓存，那么创建新的实例
        val newInstance = clazz.newInstance()
        // 并且放入缓存
        cache.put(key, newInstance)
        // 将新实例记录为当前
        current = newInstance
        current?.setLayerCallback(this)
    }

    fun get(): T? {
        return current
    }

    fun updateResource() {
        get()?.updateResource()
    }

    fun isResourceReady(): Boolean {
        val impl = get() ?: return true
        return impl.isResourceReady
    }

    private fun getKey(clazz: Class<*>): String {
        return clazz.name
    }

    override fun invalidateLayer(layer: QrWriterLayer) {
        if (layer === current) {
            layerCallback?.invalidateLayer(layer)
        }
    }

    override fun getLifecycle(): Lifecycle? {
        return layerCallback?.getLifecycle()
    }

    override fun createGlideBuilder(): RequestManager? {
        return layerCallback?.createGlideBuilder()
    }

    override fun onResourceReady(layer: QrWriterLayer) {
        if (layer === current) {
            layerCallback?.onResourceReady(layer)
        }
    }

}