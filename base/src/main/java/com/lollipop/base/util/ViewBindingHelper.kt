package com.lollipop.base.util

import android.app.Activity
import android.app.Dialog
import android.view.InflateException
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.viewbinding.ViewBinding


inline fun <reified T : ViewBinding> Activity.lazyBind(): Lazy<T> = lazy { bind() }

inline fun <reified T : ViewBinding> Fragment.lazyBind(): Lazy<T> = lazy { bind() }

inline fun <reified T : ViewBinding> View.lazyBind(): Lazy<T> = lazy { bind() }

inline fun <reified T : ViewBinding> Dialog.lazyBind(): Lazy<T> = lazy { bind() }

inline fun <reified T : ViewBinding> Activity.bind(): T {
    return this.layoutInflater.bind()
}

inline fun <reified T : ViewBinding> Fragment.bind(): T {
    return this.layoutInflater.bind()
}

inline fun <reified T : ViewBinding> Dialog.bind(): T {
    return this.layoutInflater.bind()
}

inline fun <reified T : ViewBinding> View.bind(): T {
    return LayoutInflater.from(this.context).bind()
}

inline fun <reified T : ViewBinding> ViewGroup.bind(attach: Boolean = false): T {
    return LayoutInflater.from(this.context).bind(this, attach)
}

inline fun <reified T : ViewBinding> LayoutInflater.bind(
    parent: ViewGroup? = null,
    attach: Boolean = false
): T {
    val layoutInflater: LayoutInflater = this
    val bindingClass = T::class.java
    if (parent == null) {
        val inflateMethod = bindingClass.getMethod("inflate", LayoutInflater::class.java)
        val invokeObj = inflateMethod.invoke(null, layoutInflater)
        if (invokeObj is T) {
            return invokeObj
        }
    } else {
        val inflateMethod = bindingClass.getMethod(
            "inflate",
            LayoutInflater::class.java,
            ViewGroup::class.java,
            Boolean::class.java
        )
        val invokeObj = inflateMethod.invoke(null, layoutInflater, parent, false)
        if (invokeObj is T) {
            if (attach) {
                parent.addView(invokeObj.root)
            }
            return invokeObj
        }
    }
    throw InflateException("Cant inflate ViewBinding ${bindingClass.name}")
}

inline fun <reified T : ViewBinding> View.withThis(inflate: Boolean = false): Lazy<T> = lazy {
    val bindingClass = T::class.java
    val view: View = this
    if (view is ViewGroup && inflate) {
        val bindMethod = bindingClass.getMethod(
            "inflate",
            LayoutInflater::class.java,
            ViewGroup::class.java,
            Boolean::class.javaPrimitiveType
        )
        val bindObj = bindMethod.invoke(null, LayoutInflater.from(context), view, true)
        if (bindObj is T) {
            return@lazy bindObj
        }
    } else {
        val bindMethod = bindingClass.getMethod(
            "bind",
            View::class.java
        )
        val bindObj = bindMethod.invoke(null, view)
        if (bindObj is T) {
            return@lazy bindObj
        }
    }
    throw InflateException("Cant inflate ViewBinding ${bindingClass.name}")
}