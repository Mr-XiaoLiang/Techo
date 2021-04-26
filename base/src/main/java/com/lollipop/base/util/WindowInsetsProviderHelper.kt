package com.lollipop.base.util

import android.graphics.Rect
import android.view.View
import com.lollipop.base.listener.OnInsetsChangeListener
import com.lollipop.base.provider.WindowInsetsProvider
import java.lang.ref.WeakReference

/**
 * @author lollipop
 * @date 4/18/21 20:02
 */
class WindowInsetsProviderHelper(
        firstListener: OnInsetsChangeListener? = null
) : WindowInsetsProvider, OnInsetsChangeListener {

    private val listenerList = ArrayList<OnInsetsChangeListener>().apply {
        if (firstListener != null) {
            add(firstListener)
        }
    }

    private val windowInsets = Rect()

    private var rootTarget = WeakReference<View>(null)

    override fun addInsetsChangeListener(listener: OnInsetsChangeListener) {
        listenerList.add(listener)
        rootTarget.get()?.let { root ->
            listener.onInsetsChanged(
                    root,
                    windowInsets.left,
                    windowInsets.top,
                    windowInsets.right,
                    windowInsets.bottom
            )
        }
    }

    override fun removeInsetsChangeListener(listener: OnInsetsChangeListener) {
        listenerList.remove(listener)
    }

    override fun onInsetsChanged(root: View, left: Int, top: Int, right: Int, bottom: Int) {
        windowInsets.set(left, top, right, bottom)
        rootTarget = WeakReference<View>(root)
        listenerList.forEach {
            it.onInsetsChanged(root, left, top, right, bottom)
        }
    }

    fun destroy() {
        rootTarget = WeakReference<View>(null)
        windowInsets.set(0, 0, 0, 0)
        listenerList.clear()
    }

}