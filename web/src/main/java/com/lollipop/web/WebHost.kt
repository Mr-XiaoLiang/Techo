package com.lollipop.web

import android.app.Activity
import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner

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
    val hostLifecycleOwner: LifecycleOwner?
        get() {
            val activity = this
            if (activity is Activity) {
                if (activity.isFinishing || activity.isDestroyed) {
                    return null
                }
            }
            if (activity is LifecycleOwner) {
                if (activity.lifecycle.currentState.isAtLeast(Lifecycle.State.DESTROYED)) {
                    return null
                }
                return activity
            }
            return null
        }

}