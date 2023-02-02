package com.lollipop.base.util

import java.lang.ref.WeakReference

class ListenerManager<T : Any> {

    private val list = ArrayList<WeakReference<T>>()

    val isEmpty: Boolean
        get() {
            return list.isEmpty()
        }

    fun addListener(listener: T) {
        synchronized(list) {
            list.add(WeakReference(listener))
        }
    }

    fun removeListener(listener: T) {
        synchronized(list) {
            val temp = HashSet<WeakReference<T>>(list.size)
            list.forEach {
                if (it.get() == null || it.get() == listener) {
                    temp.add(it)
                }
            }
            list.removeAll(temp)
        }
    }

    fun invoke(callback: (T) -> Unit) {
        synchronized(list) {
            list.forEach {
                it.get()?.let(callback)
            }
        }
    }

    fun clear() {
        synchronized(list) {
            list.clear()
        }
    }

}