package com.lollipop.web.host

import android.app.Activity
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import com.lollipop.web.WebHost

interface ActivityHost : WebHost {

    override val hostActivity: Activity?
        get() {
            val activity = this
            if (activity is Activity) {
                if (activity.isFinishing || activity.isDestroyed) {
                    return null
                }
                return activity
            }
            return null
        }
    override val hostLifecycleOwner: LifecycleOwner?
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