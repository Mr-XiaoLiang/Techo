package com.lollipop.web

import android.app.Activity
import android.app.Application
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
            val activity = this
            if (activity is Activity) {
                if (activity.isFinishing || activity.isDestroyed) {
                    return ProcessLifecycleOwner.get().lifecycle
                }
            }
            if (activity is LifecycleOwner) {
                if (activity.lifecycle.currentState.isAtLeast(Lifecycle.State.DESTROYED)) {
                    return ProcessLifecycleOwner.get().lifecycle
                }
                return activity.lifecycle
            }
            return ProcessLifecycleOwner.get().lifecycle
        }

}