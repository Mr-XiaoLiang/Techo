package com.lollipop.filechooser

import android.app.Activity
import android.content.Context
import android.content.Intent
import androidx.activity.ComponentActivity
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContract
import androidx.core.app.ActivityOptionsCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.LifecycleOwner
import java.lang.ref.WeakReference

object FileChooser {

    /**
     * 注册一个文件选择的启动器
     */
    fun registerChooserLauncher(
        activity: ComponentActivity,
        onResult: (ChooseResult) -> Unit
    ): FileChooseLauncher {
        return FileChooserLauncherHelper(activity, onResult)
    }

    class Builder(
        private val launcher: ActivityResultLauncher<ChooseRequest>
    ) {

        private val intent = Intent(Intent.ACTION_GET_CONTENT)
        private var title: CharSequence = ""

        init {
            intent.addCategory(Intent.CATEGORY_OPENABLE)
        }

        fun localOnly(): Builder {
            intent.putExtra(Intent.EXTRA_LOCAL_ONLY, true)
            return this
        }

        fun title(title: CharSequence): Builder {
            this.title = title
            return this
        }

        fun multiple(): Builder {
            intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true)
            return this
        }

        fun type(type: FileMime): Builder {
            intent.type = type.value
            return this
        }

        fun start() {
            val target = if (title.isNotEmpty()) {
                Intent.createChooser(intent, title)
            } else {
                intent
            }
            launcher.launch(ChooseRequest(target))
        }

    }

    class ChooseRequest(val intent: Intent)

    sealed class ChooseResult {

        object Empty : ChooseResult()

    }

    private class ChooseResultContract : ActivityResultContract<ChooseRequest, ChooseResult>() {
        override fun createIntent(context: Context, input: ChooseRequest): Intent {
            return input.intent
        }

        override fun parseResult(resultCode: Int, intent: Intent?): ChooseResult {
            if (resultCode != Activity.RESULT_OK || intent == null) {
                return ChooseResult.Empty
            }
            // 如果是EXTRA_ALLOW_MULTIPLE，需要从clipData中取
            TODO("Not yet implemented")
        }

    }

    interface FileChooseLauncher {
        fun unregister()
        fun launch(): Builder
    }

    private class FileChooserLauncherHelper(
        activity: ComponentActivity,
        private val onResult: (ChooseResult) -> Unit
    ) : ActivityResultLauncher<ChooseRequest>(), LifecycleEventObserver, FileChooseLauncher {

        private val contextReference = WeakReference(activity)

        private var launcherImpl: ActivityResultLauncher<ChooseRequest>? = null

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
                launcherImpl = activity.registerForActivityResult(ChooseResultContract()) {
                    onCallbackResult(it)
                }
            }
        }

        private fun onCallbackResult(result: ChooseResult) {
            onResult(result)
        }

        override fun launch(input: ChooseRequest, options: ActivityOptionsCompat?) {
            launcherImpl?.launch(input, options)
        }

        override fun unregister() {
            launcherImpl?.unregister()
        }

        override fun launch(): Builder {
            return Builder(this)
        }

        override fun getContract(): ActivityResultContract<ChooseRequest, *> {
            return launcherImpl!!.contract
        }

    }

}