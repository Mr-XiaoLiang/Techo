package com.lollipop.web

import android.app.Activity
import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.ProcessLifecycleOwner

interface WebHost {

    val hostActivity: Activity?
        get() {
            val target = this
            if (target is Activity) {
                if (target.isFinishing || target.isDestroyed) {
                    return null
                }
                return target
            }
            if (target is Fragment) {
                return target.activity
            }
            return null
        }

    val hostLifecycle: Lifecycle
        get() {
            val host = this
            if (host is LifecycleOwner) {
                return host.lifecycle
            }
            return ProcessLifecycleOwner.get().lifecycle
        }

}