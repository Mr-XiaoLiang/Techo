package com.lollipop.base.util

import android.content.Intent
import androidx.activity.ComponentActivity
import androidx.activity.result.ActivityResult
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContract
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.app.ActivityOptionsCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.LifecycleOwner
import java.lang.ref.WeakReference

class ActivityLauncherHelper<I, O>(
    activity: ComponentActivity,
    private val contract: Class<out ActivityResultContract<I, O>>,
    private val onResult: (O) -> Unit
) : ActivityResultLauncher<I>(), LifecycleEventObserver {

    private val contextReference = WeakReference(activity)

    private var launcherImpl: ActivityResultLauncher<I>? = null

    init {
        initByState(activity.lifecycle.currentState)
        if (launcherImpl == null) {
            activity.lifecycle.addObserver(this)
        }
    }

    override fun onStateChanged(source: LifecycleOwner, event: Lifecycle.Event) {
        initByState(event.targetState)
    }

    private fun initByState(targetState: Lifecycle.State) {
        if (launcherImpl == null && targetState.isAtLeast(Lifecycle.State.CREATED)) {
            contextReference.get()?.registerForActivityResult(contract.newInstance()) {
                onCallbackResult(it)
            }
        }
    }

    private fun onCallbackResult(result: O) {
        onResult(result)
    }

    override fun launch(input: I, options: ActivityOptionsCompat?) {
        launcherImpl?.launch(input, options)
    }

    override fun unregister() {
        launcherImpl?.unregister()
    }

    override fun getContract(): ActivityResultContract<I, *> {
        return launcherImpl!!.contract
    }

}

inline fun <reified I, reified O> ComponentActivity.registerResult(
    clazz: Class<out ActivityResultContract<I, O>>,
    noinline callback: (O) -> Unit
): ActivityLauncherHelper<I, O> {
    return ActivityLauncherHelper(this, clazz, callback)
}

fun ComponentActivity.registerSimpleResult(
    callback: (ActivityResult) -> Unit
): ActivityLauncherHelper<Intent, ActivityResult> {
    return registerResult(
        ActivityResultContracts.StartActivityForResult::class.java,
        callback
    )
}
