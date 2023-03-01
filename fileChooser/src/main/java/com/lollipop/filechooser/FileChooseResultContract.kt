package com.lollipop.filechooser

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.activity.result.contract.ActivityResultContract

internal class FileChooseResultContract :
    ActivityResultContract<FileChooseRequest, FileChooseResult>() {

    override fun createIntent(context: Context, input: FileChooseRequest): Intent {
        return input.intent
    }

    override fun parseResult(resultCode: Int, intent: Intent?): FileChooseResult {
        if (resultCode != Activity.RESULT_OK || intent == null) {
            return FileChooseResult.Empty
        }
        val result = ArrayList<Uri>()
        val dataUri = intent.data
        if (dataUri != null) {
            result.add(dataUri)
        }
        val clipData = intent.clipData
        if (clipData != null) {
            for (i in 0 until clipData.itemCount) {
                val item = clipData.getItemAt(i)
                val uri = item.uri
                if (uri != null && uri != dataUri) {
                    result.add(uri)
                }
            }
        }
        if (result.isEmpty()) {
            return FileChooseResult.Empty
        }
        if (result.size == 1) {
            return FileChooseResult.Single(result[0])
        }
        return FileChooseResult.Multiple(result)
    }

}