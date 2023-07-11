package com.lollipop.lqrdemo.writer

import androidx.collection.LruCache

class LayerDelegate<T : QrWriterLayer> {

    private val cache = LruCache<String, T>(3)

    private var current: T? = null

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
            return
        }
        // 如果没有缓存，那么创建新的实例
        val newInstance = clazz.newInstance()
        // 并且放入缓存
        cache.put(key, newInstance)
        // 将新实例记录为当前
        current = newInstance
    }

    fun get(): T? {
        return current
    }

    private fun getKey(clazz: Class<*>): String {
        return clazz.name
    }

}