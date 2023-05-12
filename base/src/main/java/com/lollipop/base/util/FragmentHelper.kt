package com.lollipop.base.util

import android.content.Context
import androidx.fragment.app.Fragment

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
    if (FragmentHelper.check(parentFragment, callback)) {
        return
    }
    if (FragmentHelper.check(getContext(), callback)) {
        return
    }
    if (FragmentHelper.check(context, callback)) {
        return
    }
}