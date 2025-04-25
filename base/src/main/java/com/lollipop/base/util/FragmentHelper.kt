package com.lollipop.base.util

import android.content.Context
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.fragment.app.FragmentManager

object FragmentHelper {

    inline fun <reified T : Any> check(any: Any?, callback: (T) -> Unit): Boolean {
        if (any is T) {
            callback(any)
            return true
        }
        return false
    }

    inline fun <reified T : Any> check(any: Any?): T? {
        if (any is T) {
            return any
        }
        return null
    }

    inline fun <reified T : Any> findByParent(current: Fragment): T? {
        var fragment = current.parentFragment
        while (fragment != null) {
            val target = check<T>(fragment)
            if (target != null) {
                return target
            }
            fragment = fragment.parentFragment
        }
        return null
    }

    inline fun <reified T : Any> findByType(fragmentManager: FragmentManager): List<T> {
        val list = ArrayList<T>()
        fragmentManager.fragments.forEach { fragment ->
            if (fragment is T) {
                list.add(fragment)
            }
        }
        return list
    }

}

inline fun <reified T : Any> Fragment.checkCallback(context: Context?): T? {
    checkCallback<T>(context) {
        return it
    }
    return null
}

inline fun <reified T : Any> Fragment.checkCallback(
    context: Context?,
    callback: (T) -> Unit
) {
    val target = FragmentHelper.findByParent<T>(this)
    if (target != null) {
        callback(target)
        return
    }
    if (FragmentHelper.check(getContext(), callback)) {
        return
    }
    if (FragmentHelper.check(context, callback)) {
        return
    }
}

inline fun <reified T : Any> FragmentActivity.findFragmentByType(): List<T> {
    return FragmentHelper.findByType(supportFragmentManager)
}

inline fun <reified T : Any> Fragment.findFragmentByType(): List<T> {
    return FragmentHelper.findByType(childFragmentManager)
}
