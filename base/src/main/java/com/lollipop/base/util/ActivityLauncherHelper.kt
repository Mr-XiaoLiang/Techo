package com.lollipop.base.util

import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
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

    companion object {
        inline fun <reified T : ActivityResultContract<I, O>, I, O> launcher(): Class<out ActivityResultContract<I, O>> {
            return T::class.java
        }
    }

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
            launcherImpl =
                contextReference.get()?.registerForActivityResult(
                    contract.getDeclaredConstructor().newInstance()
                ) {
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

    abstract class Simple<I, O> : ActivityResultContract<I, O>() {

        protected abstract val activityClass: Class<out Activity>

        override fun createIntent(context: Context, input: I): Intent {
            val intent = Intent(context, activityClass)
            putParams(intent, input)
            if (!context.isActivity()) {
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            return intent
        }

        protected fun Context.isActivity(): Boolean {
            var c: Context = this
            do {
                if (c is Activity) {
                    return true
                }
                if (c is ContextWrapper) {
                    c = c.baseContext
                } else {
                    return false
                }
            } while (true)
        }

        open fun putParams(intent: Intent, input: I) {}

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
