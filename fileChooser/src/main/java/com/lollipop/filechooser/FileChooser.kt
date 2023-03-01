package com.lollipop.filechooser

import android.content.Intent
import androidx.activity.ComponentActivity
import androidx.activity.result.ActivityResultLauncher

object FileChooser {

    /**
     * 注册一个文件选择的启动器
     */
    fun registerChooserLauncher(
        activity: ComponentActivity,
        onResult: (FileChooseResult) -> Unit
    ): FileChooseLauncher {
        return FileChooserLauncherHelper(activity, onResult)
    }

    class Builder(
        private val launcher: ActivityResultLauncher<FileChooseRequest>
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
            launcher.launch(FileChooseRequest(target))
        }
    }

}