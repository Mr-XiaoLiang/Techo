package com.lollipop.filechooser

import androidx.activity.ComponentActivity
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContract
import androidx.core.app.ActivityOptionsCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.LifecycleOwner
import java.lang.ref.WeakReference

internal class FileChooserLauncherHelper(
    activity: ComponentActivity,
    private val onResult: (FileChooseResult) -> Unit
) : ActivityResultLauncher<FileChooseRequest>(), LifecycleEventObserver, FileChooseLauncher {

    private val contextReference = WeakReference(activity)

    private var launcherImpl: ActivityResultLauncher<FileChooseRequest>? = null

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
            val activity = contextReference.get() ?: return
            launcherImpl = activity.registerForActivityResult(FileChooseResultContract()) {
                onCallbackResult(it)
            }
        }
    }

    private fun onCallbackResult(result: FileChooseResult) {
        onResult(result)
    }

    override fun launch(input: FileChooseRequest, options: ActivityOptionsCompat?) {
        launcherImpl?.launch(input, options)
    }

    override fun unregister() {
        launcherImpl?.unregister()
    }

    override fun launch(): FileChooser.Builder {
        return FileChooser.Builder(this)
    }

    override fun getContract(): ActivityResultContract<FileChooseRequest, *> {
        return launcherImpl!!.contract
    }

}