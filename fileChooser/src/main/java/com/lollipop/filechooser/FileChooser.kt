package com.lollipop.filechooser

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.activity.ComponentActivity
import androidx.activity.result.ActivityResultLauncher
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream
import java.io.OutputStream

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

    fun open(context: Context, from: Uri): InputStream? {
        return context.contentResolver.openInputStream(from)
    }

    fun save(context: Context, from: Uri, toFile: File) {
        save(open(context, from) ?: return, toFile)
    }

    fun save(inputStream: InputStream, file: File) {
        var outputStream: OutputStream? = null
        try {
            file.parentFile?.mkdirs()
            file.delete()
            outputStream = FileOutputStream(file)
            val buffer = ByteArray(4096)
            do {
                val read = inputStream.read(buffer)
                if (read >= 0) {
                    outputStream.write(buffer, 0, read)
                }
            } while (read >= 0)
            outputStream.flush()
        } catch (e: Throwable) {
            e.printStackTrace()
        } finally {
            try {
                inputStream.close()
            } catch (e: Throwable) {
                e.printStackTrace()
            }
            try {
                outputStream?.close()
            } catch (e: Throwable) {
                e.printStackTrace()
            }
        }
    }

}